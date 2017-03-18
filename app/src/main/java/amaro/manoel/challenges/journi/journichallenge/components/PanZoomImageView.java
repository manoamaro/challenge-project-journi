package amaro.manoel.challenges.journi.journichallenge.components;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by manoel on 18/03/17.
 */

public class PanZoomImageView extends View {

    private Bitmap imageBitmap = null;

    private int imageWidth;
    private int imageHeight;

    Paint background;

    // Will be used to move and zoom
    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();

    PointF start = new PointF();

    float currentScale;
    float currentX;
    float currentY;

    // States
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;

    // Animation
    float targetX;
    float targetY;
    float targetScale;
    float targetScaleX;
    float targetScaleY;
    float scaleChange;
    float targetRatio;
    boolean isAnimating = false;

    //Pinch and zoom
    float oldDist = 1f;
    PointF mid = new PointF();

    private Handler mHandler = new Handler();

    float minScale;
    float maxScale = 8.0f;

    private GestureDetector doubleTapDetector;

    public PanZoomImageView(Context context) {
        super(context);
        setFocusable(true);
        setFocusableInTouchMode(true);
        background = new Paint();
        doubleTapDetector = new GestureDetector(context, new DoubleTapDetector());
    }

    public void setImageBitmap(Bitmap bitmap) {
        if(bitmap != null) {
            this.imageBitmap = bitmap;
            invalidate();
        }
    }


    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);

        imageWidth = width;
        imageHeight = height;

        if(imageBitmap != null) {
            int imageBitmapHeight = imageBitmap.getHeight();
            int imageBitmapWidth = imageBitmap.getWidth();

            float scale;
            int initX = 0;
            int initY = 0;

            if(imageBitmapHeight > imageHeight) {
                scale = (float) imageHeight / imageBitmapHeight;
                float newWidth = imageBitmapWidth * scale;
                initX = (imageWidth - (int)newWidth)/2;

                matrix.setScale(scale, scale);
                matrix.postTranslate(initX, 0);
            }
            else {
                scale = (float) imageWidth / imageBitmapWidth;
                float newHeight = imageBitmapHeight * scale;
                initY = (imageHeight - (int)newHeight)/2;

                matrix.setScale(scale, scale);
                matrix.postTranslate(0, initY);
            }

            currentX = initX;
            currentY = initY;

            currentScale = scale;
            minScale = scale;

            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(imageBitmap != null && canvas != null)
            canvas.drawBitmap(imageBitmap, matrix, background);
    }

    //Checks and sets the target image x and y co-ordinates if out of bounds
    private void checkImageConstraints() {
        if(imageBitmap == null) {
            return;
        }

        float[] mvals = new float[9];
        matrix.getValues(mvals);

        currentScale = mvals[0];

        if(currentScale < minScale) {
            float deltaScale = minScale / currentScale;
            float px = imageWidth /2;
            float py = imageHeight /2;
            matrix.postScale(deltaScale, deltaScale, px, py);
            invalidate();
        }

        matrix.getValues(mvals);
        currentScale = mvals[0];
        currentX = mvals[2];
        currentY = mvals[5];

        int rangeLimitX = imageWidth - (int)(imageBitmap.getWidth() * currentScale);
        int rangeLimitY = imageHeight - (int)(imageBitmap.getHeight() * currentScale);


        boolean toMoveX = false;
        boolean toMoveY = false;

        if(rangeLimitX < 0) {
            if(currentX > 0) {
                targetX = 0;
                toMoveX = true;
            }
            else if(currentX < rangeLimitX) {
                targetX = rangeLimitX;
                toMoveX = true;
            }
        }
        else {
            targetX = rangeLimitX / 2;
            toMoveX = true;
        }

        if(rangeLimitY < 0) {
            if(currentY > 0) {
                targetY = 0;
                toMoveY = true;
            }
            else if(currentY < rangeLimitY) {
                targetY = rangeLimitY;
                toMoveY = true;
            }
        }
        else {
            targetY = rangeLimitY / 2;
            toMoveY = true;
        }

        if(toMoveX || toMoveY) {
            if(!toMoveY) {
                targetY = currentY;
            }
            if(!toMoveX) {
                targetX = currentX;
            }

            //Disable touch event actions
            isAnimating = true;
            //Initialize timer
            mHandler.removeCallbacks(updateImageTranslationTask);
            mHandler.postDelayed(updateImageTranslationTask, 100);
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(doubleTapDetector.onTouchEvent(event)) {
            return true;
        }

        if(isAnimating) {
            return true;
        }

        //Handle touch events here
        float[] mvals = new float[9];
        switch(event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                if(!isAnimating) {
                    savedMatrix.set(matrix);
                    start.set(event.getX(), event.getY());
                    mode = DRAG;
                }
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                if(oldDist > 10f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;

                matrix.getValues(mvals);
                currentX = mvals[2];
                currentY = mvals[5];
                currentScale = mvals[0];

                if(!isAnimating) {
                    checkImageConstraints();
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if(mode == DRAG && !isAnimating) {
                    matrix.set(savedMatrix);
                    float diffX = event.getX() - start.x;
                    float diffY = event.getY() - start.y;

                    matrix.postTranslate(diffX, diffY);

                    matrix.getValues(mvals);
                    currentX = mvals[2];
                    currentY = mvals[5];
                    currentScale = mvals[0];
                }
                else if(mode == ZOOM && !isAnimating) {
                    float newDist = spacing(event);
                    if(newDist > 10f) {
                        matrix.set(savedMatrix);
                        float scale = newDist / oldDist;
                        matrix.getValues(mvals);
                        currentScale = mvals[0];

                        if(currentScale * scale <= minScale) {
                            matrix.postScale(minScale/currentScale, minScale/currentScale, mid.x, mid.y);
                        }
                        else if(currentScale * scale >= maxScale) {
                            matrix.postScale(maxScale/currentScale, maxScale/currentScale, mid.x, mid.y);
                        }
                        else {
                            matrix.postScale(scale, scale, mid.x, mid.y);
                        }


                        matrix.getValues(mvals);
                        currentX = mvals[2];
                        currentY = mvals[5];
                        currentScale = mvals[0];
                    }
                }

                break;
        }
        invalidate();
        return true;
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x/2, y/2);
    }



    private class DoubleTapDetector extends SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent event) {
            if(isAnimating) {
                return true;
            }

            scaleChange = 1;
            isAnimating = true;
            targetScaleX = event.getX();
            targetScaleY = event.getY();

            if(Math.abs(currentScale - maxScale) > 0.1)
                targetScale = maxScale;
            else
                targetScale = minScale;

            targetRatio = targetScale / currentScale;
            mHandler.removeCallbacks(updateImageScaleTask);
            mHandler.post(updateImageScaleTask);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }
    }

    private Runnable updateImageTranslationTask = new Runnable() {
        public void run() {
            float[] mvals;

            if(Math.abs(targetX - currentX) < 5 && Math.abs(targetY - currentY) < 5) {
                isAnimating = false;
                mHandler.removeCallbacks(updateImageTranslationTask);

                mvals = new float[9];
                matrix.getValues(mvals);

                currentScale = mvals[0];
                currentX = mvals[2];
                currentY = mvals[5];

                //Set the image parameters and invalidate display
                float diffX = (targetX - currentX);
                float diffY = (targetY - currentY);

                matrix.postTranslate(diffX, diffY);
            }
            else {
                isAnimating = true;
                mvals = new float[9];
                matrix.getValues(mvals);

                currentScale = mvals[0];
                currentX = mvals[2];
                currentY = mvals[5];

                //Set the image parameters and invalidate display
                float diffX = (targetX - currentX) * 0.3f;
                float diffY = (targetY - currentY) * 0.3f;

                matrix.postTranslate(diffX, diffY);
                mHandler.postDelayed(this, 25);
            }

            invalidate();
        }
    };

    private Runnable updateImageScaleTask = new Runnable() {
        public void run() {
            float transitionalRatio = targetScale / currentScale;
            float dx;
            if(Math.abs(transitionalRatio - 1) > 0.05) {
                isAnimating = true;
                if(targetScale > currentScale) {
                    dx = transitionalRatio - 1;
                    scaleChange = 1 + dx * 0.2f;

                    currentScale *= scaleChange;

                    if(currentScale > targetScale) {
                        currentScale = currentScale / scaleChange;
                        scaleChange = 1;
                    }
                }
                else {
                    dx = 1 - transitionalRatio;
                    scaleChange = 1 - dx * 0.5f;
                    currentScale *= scaleChange;

                    if(currentScale < targetScale) {
                        currentScale = currentScale / scaleChange;
                        scaleChange = 1;
                    }
                }


                if(scaleChange != 1) {
                    matrix.postScale(scaleChange, scaleChange, targetScaleX, targetScaleY);
                    mHandler.postDelayed(updateImageScaleTask, 15);
                    invalidate();
                }
                else {
                    isAnimating = false;
                    scaleChange = 1;
                    matrix.postScale(targetScale/currentScale, targetScale/currentScale, targetScaleX, targetScaleY);
                    currentScale = targetScale;
                    mHandler.removeCallbacks(updateImageScaleTask);
                    invalidate();
                    checkImageConstraints();
                }
            }
            else {
                isAnimating = false;
                scaleChange = 1;
                matrix.postScale(targetScale/currentScale, targetScale/currentScale, targetScaleX, targetScaleY);
                currentScale = targetScale;
                mHandler.removeCallbacks(updateImageScaleTask);
                invalidate();
                checkImageConstraints();
            }
        }
    };
}
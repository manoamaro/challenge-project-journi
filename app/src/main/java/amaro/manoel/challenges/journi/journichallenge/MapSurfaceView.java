package amaro.manoel.challenges.journi.journichallenge;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import amaro.manoel.challenges.journi.journichallenge.model.Feature;
import amaro.manoel.challenges.journi.journichallenge.model.GeoJson;

/**
 * Created by manoel on 18/03/17.
 */

public class MapSurfaceView extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener {
    public Bitmap mapImage;
    private float scale = 10000f;
    private float mScaleFactor = 1.f;

    public Double mapViewWidth;
    public Double mapViewHeight;

    private float mPosX;
    private float mPosY;
    private float mLastTouchX;
    private float mLastTouchY;

    public MapSurfaceView(Context context, GeoJson geoJson) {
        super(context);
        float xOffset = (float) ((geoJson.getBbox().get(0) * -1) / scale);
        float yOffset = (float) ((geoJson.getBbox().get(1) * -1) / scale);

        mapViewWidth = ((geoJson.getBbox().get(0) * -1) + geoJson.getBbox().get(2)) / scale;
        mapViewHeight = ((geoJson.getBbox().get(1) * -1) + geoJson.getBbox().get(3)) / scale;

        this.mapImage = drawBitmap(geoJson, xOffset, yOffset);
        getHolder().addCallback(this);
        setOnTouchListener(this);
    }

    private Bitmap drawBitmap(GeoJson geoJson, float xOffset, float yOffset) {

        Bitmap bitmap = Bitmap.createBitmap(mapViewWidth.intValue(), mapViewHeight.intValue(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);

        Paint mapPaint = new Paint();
        mapPaint.setStyle(Paint.Style.FILL);
        mapPaint.setColor(Color.DKGRAY);
        mapPaint.setStrokeWidth(1);

        Paint labelPaint = new Paint();
        labelPaint.setStyle(Paint.Style.STROKE);
        labelPaint.setColor(Color.GRAY);

        for (Feature feature : geoJson.getFeatures()) {
            for (Feature.Geometry geometry : feature.geometries) {
                int size = geometry.coordinates.size();
                Path p = new Path();
                for (int i = 0; i < size; i++) {
                    Feature.Geometry.Coordinates coordinates = geometry.coordinates.get(i);
                    float x = (float) (coordinates.x / scale) + xOffset;
                    float y = (float) ((coordinates.y * -1) / scale) + yOffset;
                    if (i == 0) {
                        p.moveTo(x, y);
                    } else {
                        p.lineTo(x, y);
                    }
                }
                p.close();
                canvas.drawPath(p, mapPaint);
                canvas.drawPath(p, labelPaint);
            }
        }
        return bitmap;
    }

    private void drawBitmap(SurfaceHolder holder, Bitmap bitmap, float x, float y) {
        Canvas canvas = holder.lockCanvas();
        canvas.drawBitmap(bitmap, x, y, null);
        holder.unlockCanvasAndPost(canvas);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        drawBitmap(holder, this.mapImage, this.mPosX, this.mPosX);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.v("MapSurfaceView", "Surface Changed");
        drawBitmap(holder, this.mapImage, this.mPosX, this.mPosX);

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        this.mPosX += event.getX();
        this.mPosX += event.getY();
        this.invalidate();
        return false;
    }
}

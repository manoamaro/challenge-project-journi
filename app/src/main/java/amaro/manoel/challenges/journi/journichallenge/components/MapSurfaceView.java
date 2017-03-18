package amaro.manoel.challenges.journi.journichallenge.components;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by manoel on 18/03/17.
 */

public class MapSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    public Bitmap mapImage;
    public MapSurfaceView(Context context, Bitmap mapImage) {
        super(context);
        this.mapImage = mapImage;
        getHolder().addCallback(this);
    }

    private void drawBitmap(SurfaceHolder holder, Bitmap bitmap, float x, float y) {
        Canvas canvas = holder.lockCanvas();
        canvas.drawBitmap(bitmap, x, y, null);
        holder.unlockCanvasAndPost(canvas);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        drawBitmap(holder, this.mapImage, 0, 0);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}

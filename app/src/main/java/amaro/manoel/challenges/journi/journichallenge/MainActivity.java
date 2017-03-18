package amaro.manoel.challenges.journi.journichallenge;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.InputStream;

import amaro.manoel.challenges.journi.journichallenge.components.PanZoomImageView;
import amaro.manoel.challenges.journi.journichallenge.model.Feature;
import amaro.manoel.challenges.journi.journichallenge.model.GeoJson;
import amaro.manoel.challenges.journi.journichallenge.utils.GeoJsonParser;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final FrameLayout container = (FrameLayout) findViewById(R.id.container);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        LoadGeoJsonAndBitmapTask loadingTask = new LoadGeoJsonAndBitmapTask(container, progressBar);

        try {
            loadingTask.execute(getAssets().open("countries_small.geojson"));
        } catch (Exception e) {
            Toast.makeText(this, R.string.failed_open_geojson, Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    // AsyncTask to load the GeoJSON file and create the Bitmap
    private class LoadGeoJsonAndBitmapTask extends AsyncTask<InputStream, Void, Bitmap> {

        private final FrameLayout container;
        private final ProgressBar progressBar;

        LoadGeoJsonAndBitmapTask(FrameLayout container, ProgressBar progressBar) {
            this.container = container;
            this.progressBar = progressBar;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Bitmap doInBackground(InputStream... params) {
            GeoJsonParser geoJsonParser = new GeoJsonParser();
            GeoJson geoJson = geoJsonParser.parse(params[0]);
            // Scale down the image to a size that the Bitmap and android render can handle
            float scale = 10000f;
            // Find the offset so the polygons can be rendered in the right position
            float xOffset = (float) ((geoJson.getBbox().get(0) * -1) / scale);
            float yOffset = (float) ((geoJson.getBbox().get(1) * -1) / scale);
            // Find the map's size based on the bbox properties
            Double mapViewWidth = ((geoJson.getBbox().get(0) * -1) + geoJson.getBbox().get(2)) / scale;
            Double mapViewHeight = ((geoJson.getBbox().get(1) * -1) + geoJson.getBbox().get(3)) / scale;
            return drawBitmap(geoJson, xOffset, yOffset, mapViewWidth.intValue(),
                    mapViewHeight.intValue(), scale);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            // Create a custom view that handles the Pan and Zoom
            PanZoomImageView imageView = new PanZoomImageView(MainActivity.this);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            imageView.setImageBitmap(bitmap);
            this.progressBar.setVisibility(View.GONE);
            container.addView(imageView);
        }

        // Method that creates a Bitmap with the complete map drawn
        private Bitmap drawBitmap(GeoJson geoJson, float xOffset, float yOffset, int mapViewWidth,
                                  int mapViewHeight, float scale) {

            Bitmap bitmap = Bitmap.createBitmap(mapViewWidth, mapViewHeight, Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(Color.WHITE);

            Paint mapPaint = new Paint();
            mapPaint.setStyle(Paint.Style.FILL);
            mapPaint.setColor(Color.DKGRAY);
            mapPaint.setStrokeWidth(1.5f);

            Paint labelPaint = new Paint();
            labelPaint.setStyle(Paint.Style.STROKE);
            labelPaint.setColor(Color.GRAY);

            for (Feature feature : geoJson.getFeatures()) {
                for (Feature.Geometry geometry : feature.getGeometries()) {
                    int size = geometry.getCoordinates().size();
                    Path p = new Path();
                    for (int i = 0; i < size; i++) {
                        Feature.Geometry.Coordinates coordinates = geometry.getCoordinates().get(i);
                        float x = (float) (coordinates.getX() / scale) + xOffset;
                        float y = (float) ((coordinates.getY() * -1) / scale) + yOffset;
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

    }
}

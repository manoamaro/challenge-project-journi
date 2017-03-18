package amaro.manoel.challenges.journi.journichallenge;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import amaro.manoel.challenges.journi.journichallenge.model.GeoJson;
import amaro.manoel.challenges.journi.journichallenge.utils.GeoJsonParser;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FrameLayout container = (FrameLayout) findViewById(R.id.container);

        try {
            GeoJsonParser geoJsonParser = new GeoJsonParser();
            GeoJson geoJson = geoJsonParser.parse(getAssets().open("countries_small.geojson"));

            MapSurfaceView view = new MapSurfaceView(this, geoJson);
            view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));




            container.addView(view);

        } catch (Exception e) {
            Toast.makeText(this, R.string.failed_open_geojson, Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}

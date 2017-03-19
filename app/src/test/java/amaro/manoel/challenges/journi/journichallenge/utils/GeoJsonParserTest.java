package amaro.manoel.challenges.journi.journichallenge.utils;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.InputStream;

import amaro.manoel.challenges.journi.journichallenge.BuildConfig;
import amaro.manoel.challenges.journi.journichallenge.model.GeoJson;

/**
 * Created by manoel on 18/03/17.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class GeoJsonParserTest {
    @Test
    public void parse() throws Exception {
        InputStream file = RuntimeEnvironment.application.getAssets().open("countries_small.geojson");
        GeoJsonParser parser = new GeoJsonParser();
        GeoJson result = parser.parse(file);

        Assert.assertNotNull(result);
        Assert.assertEquals(242, result.getFeatures().size());
    }

}
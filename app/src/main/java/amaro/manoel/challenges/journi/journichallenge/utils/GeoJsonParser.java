package amaro.manoel.challenges.journi.journichallenge.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import amaro.manoel.challenges.journi.journichallenge.model.Feature;
import amaro.manoel.challenges.journi.journichallenge.model.GeoJson;

/**
 * Created by manoel on 18/03/17.
 */

public class GeoJsonParser {
    public GeoJson parse(InputStream file) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(file));
            String line;
            StringBuilder sb = new StringBuilder();
            while((line = reader.readLine()) != null) {
                sb.append(line);
            }
            JSONObject jsonObject = new JSONObject(sb.toString());
            GeoJson geoJson = new GeoJson();

            JSONArray bbox = jsonObject.getJSONArray("bbox");
            for (int i = 0; i < bbox.length(); i++) {
                geoJson.getBbox().add(bbox.getDouble(i));
            }

            JSONArray features = jsonObject.getJSONArray("features");
            for (int i = 0; i < features.length(); i++) {
                JSONObject feature = features.getJSONObject(i);
                geoJson.getFeatures().add(buildFeature(feature));
            }

            return geoJson;
        } catch (Exception e) {
            return null;
        }
    }

    private Feature buildFeature(JSONObject src) throws JSONException {
        Feature feature = new Feature();
        feature.setName(src.getJSONObject("properties").getString("name"));
        JSONObject geometry = src.getJSONObject("geometry");
        String type = geometry.getString("type");
        if (type.equals("Polygon")) {
            feature.getGeometries().add(buildPolygon(geometry));
        } else if (type.equals("MultiPolygon")) {
            feature.getGeometries().addAll(buildMultiPolygon(geometry));
        }
        return  feature;
    }

    private Feature.Geometry buildPolygon(JSONObject src) throws JSONException {
        Feature.Geometry geometry = new Feature.Geometry();
        JSONArray coordinates = src.getJSONArray("coordinates");
        geometry.getCoordinates().addAll(buildCoordinates(coordinates));
        return geometry;
    }

    private List<Feature.Geometry.Coordinates> buildCoordinates(JSONArray coordinates) throws JSONException {
        List<Feature.Geometry.Coordinates> coordinatesList = new ArrayList<>();
        for (int i = 0; i < coordinates.length(); i++) {
            JSONArray coordinate = coordinates.getJSONArray(i);
            for (int j = 0; j < coordinate.length(); j++) {
                JSONArray points = coordinate.getJSONArray(j);
                coordinatesList.add(new Feature.Geometry.Coordinates(points.getDouble(0), points.getDouble(1)));
            }
        }
        return coordinatesList;
    }

    private List<Feature.Geometry> buildMultiPolygon(JSONObject src) throws JSONException {
        List<Feature.Geometry> geometries = new ArrayList<>();
        JSONArray coordinates = src.getJSONArray("coordinates");
        for (int i = 0; i < coordinates.length(); i++) {
            JSONArray innerCoordinates = coordinates.getJSONArray(i);
            Feature.Geometry innerGeometry = new Feature.Geometry();
            innerGeometry.getCoordinates().addAll(buildCoordinates(innerCoordinates));
            geometries.add(innerGeometry);
        }
        return geometries;
    }
}

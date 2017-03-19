package amaro.manoel.challenges.journi.journichallenge.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by manoel on 18/03/17.
 */

public class GeoJson {
    private List<Double> bbox = new ArrayList<>();
    private List<Feature> features = new ArrayList<>();

    public List<Double> getBbox() {
        return bbox;
    }

    public void setBbox(List<Double> bbox) {
        this.bbox = bbox;
    }

    public List<Feature> getFeatures() {
        return features;
    }

    public void setFeatures(List<Feature> features) {
        this.features = features;
    }
}

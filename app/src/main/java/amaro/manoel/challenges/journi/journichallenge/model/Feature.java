package amaro.manoel.challenges.journi.journichallenge.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by manoel on 18/03/17.
 */

public class Feature {
    public String name;
    public List<Geometry> geometries = new ArrayList<>();

    public static class Geometry {
        public List<Coordinates> coordinates = new ArrayList<>();

        public static class Coordinates {
            public double x;
            public double y;

            public Coordinates(double x, double y) {
                this.x = x;
                this.y = y;
            }
        }

    }
}

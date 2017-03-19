package amaro.manoel.challenges.journi.journichallenge.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by manoel on 18/03/17.
 */

public class Feature {
    private String name;
    private List<Geometry> geometries = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Geometry> getGeometries() {
        return geometries;
    }

    public void setGeometries(List<Geometry> geometries) {
        this.geometries = geometries;
    }

    public static class Geometry {
        private List<Coordinates> coordinates = new ArrayList<>();

        public List<Coordinates> getCoordinates() {
            return coordinates;
        }

        public void setCoordinates(List<Coordinates> coordinates) {
            this.coordinates = coordinates;
        }

        public static class Coordinates {
            private double x;
            private double y;

            public Coordinates(double x, double y) {
                this.x = x;
                this.y = y;
            }

            public double getX() {
                return x;
            }

            public void setX(double x) {
                this.x = x;
            }

            public double getY() {
                return y;
            }

            public void setY(double y) {
                this.y = y;
            }
        }

    }
}

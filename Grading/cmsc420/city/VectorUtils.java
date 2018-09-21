package cmsc420.city;

import java.awt.geom.Point2D;

public class VectorUtils {

    private VectorUtils() {

    }

    /*
     * Adds the two vectors, and dumps the result in the first parameter.
     */
    public static Point2D.Double addVector(final Point2D.Double dest, final Point2D src) {
        if (dest != null && src != null) {
            dest.x += src.getX();
            dest.y += src.getY();
        }
        return dest;
    }

    public static Point2D.Double scaleVector(final Point2D.Double vector, final double scalar) {
        if (vector != null) {
            vector.x *= scalar;
            vector.y *= scalar;
        }
        return vector;
    }
}

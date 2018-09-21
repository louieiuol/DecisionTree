package cmsc420.pmquadtree;

/**
 * A PM Quadtree of order 1 has the following rules:
 * <p>
 * 1. At most, one vertex can lie in a region represented by a quadtree leaf
 * node.
 * <p>
 * 2. If a quadtree leaf node's region contains a vertex, then it can contain no
 * q-edge that does not contain that vertex.
 * <p>
 * 3. If a quadtree leaf node's region contains no vertices, then it can
 * contain, at most, one q-edge.
 * <p>
 * 4. Each region's quadtree leaf node is maximal.
 * 
 * @author Ben Zoller
 * @version 1.0, 14 Mar 2007
 */
public class PM1Quadtree extends PMQuadtree {
	/**
	 * Constructs and initializes this PM Quadtree of order 1.
	 * 
	 * @param spatialWidth
	 *            width of the spatial map
	 * @param spatialHeight
	 *            height of the spatial map
	 */
	public PM1Quadtree(final int spatialWidth, final int spatialHeight) {
		super(new PM1Validator(), spatialWidth, spatialHeight, 1);
	}
}
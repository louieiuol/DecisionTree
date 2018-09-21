package cmsc420.pmquadtree;

import java.util.LinkedList;
import java.util.Iterator;

import cmsc420.city.City;
import cmsc420.pmquadtree.PMQuadtree.Black;
import cmsc420.road.Road;

public class PM1Validator implements Validator {
	public boolean valid(final Black node) {
		if (node.getNumPoints() > 1) {
			return false;
		} else {
			final LinkedList<Road> geometry = node.getGeometry();
			final Road first = geometry.getFirst();
			if (first.isCity()) {
				final City city = first.getStart();
				/* check whether all q-edges contain this point */
				final Iterator<Road> iter = geometry.iterator();
				/* skip the first road */
				iter.next();
				while (iter.hasNext()) {
					final Road road = iter.next();
					if (!road.contains(city)) {
						return false;
					}
				}
				return true;
			} else {
				/* check if there is only 1 q-edge */
				return (geometry.size() == 1);
			}
		}
	}
}

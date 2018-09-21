package cmsc420.portal;

import java.util.Comparator;

public class PortalNameComparator implements Comparator<Portal>{

	@Override
	public int compare(Portal a, Portal b) {
		return a.name.compareTo(b.name);
	}


	
}

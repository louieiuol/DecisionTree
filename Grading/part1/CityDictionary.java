package cmsc420.meeshquest.part1;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeMap;


public class CityDictionary extends TreeMap<City, String> {

	
	private static final long serialVersionUID = 1L;

	public CityDictionary(Comparator <City> c){
		super(c);
	}

	
}

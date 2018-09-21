package cmsc420.meeshquest.part1;
import java.util.Comparator;


public class NameComparator implements Comparator<String> {

	@Override
	public int compare(String city1,String city2) {
		
	return city2.compareTo(city1);
	}

}

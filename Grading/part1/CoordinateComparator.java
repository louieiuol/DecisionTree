package cmsc420.meeshquest.part1;
import java.util.Comparator;


public class CoordinateComparator implements Comparator<City> {

	@Override
	public int compare(City city1, City city2) {
		if(city1.y==city2.y){
			if(city1.x>city2.x){
				return 1;
			}
			else if(city1.x<city2.x){
				return -1;
			}
			else return 0;
		}
		else if(city1.y>city2.y){
			return 1;
		}
		else{
			return -1;
		}
		
	}

}

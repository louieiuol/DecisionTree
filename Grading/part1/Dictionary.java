package cmsc420.meeshquest.part1;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.Map;


public class Dictionary {

	public CityDictionary cd ;
	public StringDictionary sd;
	
	

public Dictionary(){
	this.cd= new CityDictionary(new CoordinateComparator());	
	this.sd = new StringDictionary(new NameComparator());
}

public String getName(City c){
	return cd.get(c);
	
}

public City getCity(String name){
	return sd.get(name);
}

public int add(City c) {
	if((cd.containsKey(c))){
		return 1;
	
	}
	else if ((sd.containsKey(c.getName()))){
		return 2;
		
	}
	else {
		this.cd.put(c, c.getName());
		this.sd.put(c.getName(), c);
		return 0;
	}
}
public String getName(float x, float y){
	City g= new City(null,null,x,y,0);
	return cd.get(g);
	
}
public boolean containsCoordinates(City city) {
	// only checks coordinates (equals method is not overwritten)
	return this.cd.containsKey(city);
}
public int remove(City c){
	if(c==null){
		return 0;
	}
	City d= this.sd.remove(this.cd.get(c));
	String s= this.cd.remove(c);
	if(d!=null && s!=null){
		return 0;
	}
	else {
		return 1;
	}
	
}
public int remove(String c){
	if(c==null){
		return 0;
	}
	if(this.sd.get(c)==null){
		return 1;
	}
	String s= this.cd.remove(this.sd.get(c));
	City d= this.sd.remove(c);
	
	if(d!=null && s!=null){
		return 0;
	}
	else {
		return 1;
	}
	
}

public void clearAll(){
	this.cd.clear();
	this.sd.clear();
	
}

public String toString(){
	StringBuilder str = new StringBuilder();
	str.append("<cityList>\n");
	for(Map.Entry<City,String> entry : cd.entrySet()) {
		  City key = entry.getKey();
		  str.append("\t" + "<" + key.toString()+ "/>" + "\n");
	}
	str.append("<cityList/>");
	return str.toString();
}
public String toString1(){
	StringBuilder str = new StringBuilder();
	str.append("<cityList>\n");
	for(Map.Entry<String,City> entry : sd.entrySet()) {
		  City key = entry.getValue();
		  str.append("\t" + "<" + key.toString()+ "/>" + "\n");
	}
	str.append("<cityList/>");
	return str.toString();
}


	

}

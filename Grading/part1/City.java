package cmsc420.meeshquest.part1;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Float;


public class City extends Point2D.Float {
	private static final long serialVersionUID = 1L;
	private String name;
	private String color;
	private float radius;
	
	public City(String name,String color, float x, float y, float radius) {
		super(x, y);
		this.name = name;
		this.radius = radius;
		this.color=color;
	}
	
	public String getName() {
		return this.name;
	}
	public float getRadius() {
		return this.radius;
	}
	public String getColor() {
		return this.color;
	}
	public boolean equals (Object obj){
		if (obj==null){
			return false;
		}
		else if(this==obj){
			return true;
		}
		else if(obj instanceof City){
			City c= (City) obj;
			if(c.x==this.x && c.y==this.y)
				return true;
		}
		return false;
	}
	public String toString(){
		StringBuilder str = new StringBuilder("city name=\"");
		str.append(name+"\"");
		str.append(" x=\"" + this.x + "\"");
		str.append(" y=\"" + this.y + "\"");
		str.append(" color=\"" + this.color + "\"");
		str.append(" radius=\"" + this.radius + "\"");
		return str.toString();
		
		
	}

}

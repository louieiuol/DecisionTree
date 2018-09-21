package cmsc420.portal;

import java.awt.geom.Point2D;

public class Portal {
	
	String name; 
	Point2D point;
	int z;
	
	public Portal(String n, int x, int y, int z){
		name = n;
		point = new Point2D.Double(x,y);
		this.z = z;
	}
	
	public String getName(){
		return name;
	}
	public int getX(){
		return (int)point.getX();
	}
	public int getY(){
		return (int)point.getY();
	}
	public int getZ(){
		return z;
	}
	
	//checks if 2 portals are equal
	public boolean equals(Portal p){
		return p.z == this.z && p.point.getX() == this.point.getX() 
		&& p.point.getY() == this.point.getY() && p.name.equals(this.name);
	}
	
	public String toString(){
		String toRet = "";
		toRet += "Name: " + name + " ,(" + getX() + "," + getY() + "," + getZ()+ ")";
		return toRet;
	}
}

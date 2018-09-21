package cmsc420.meeshquest.part1;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;

import cmsc420.drawing.CanvasPlus;


public class PRQuadTree {

	CanvasPlus canvas;
	int width, height;
	PRNode root;
	ArrayList<PRNode> list = new ArrayList<PRNode>();
	ArrayList<PRNode> blacklist = new ArrayList<PRNode>();

	public PRQuadTree(int width, int height,CanvasPlus c){
		this.width=width;
		this.height=height;
		this.canvas=c;
	}

	public int insert(City val){
		
		if(this.root==null){
			this.root = new PRNode(val,0,0,width,height,null,null,null,null,1);
			
			canvas.addPoint(val.getName(), val.x, val.y, Color.BLACK);
			return 0;
		}
		else if(search((int)val.x, (int)val.y)!=null){
			return 2;
		}
		else if(val.x>=width || val.y>=height || val.x<0 || val.y<0){
			return 3;
		}
		else{
			insert(this.root,0,0,width,height,val);
			return 0;
		}
		
	}

	public void insert(PRNode a,int p1x,int p1y,int p2x,int p2y,City value) {
		if (a == null){ // If there is empty space, add the point there.
			a = new PRNode(value,p1x,p1y,p2x,p2y,null,null,null,null,1);
			canvas.addPoint(value.getName(), value.x, value.y, Color.BLACK);
			
			canvas.addLine(p1x,(p2y+p1y)/2,p2x,(p2y+p1y)/2, Color.BLACK);
			canvas.addLine((p1x+p2x)/2, p1y, (p1x+p2x)/2,p2y, Color.BLACK);

		}
		/* If the space is occupied by a leaf, make it a gray node, setting node type 
		 * to 2. Then, find where the point that leaf used to occupy would fall into (NE,NW,SW,or SE).
		 * After that, check where the current value would occupy, and send the leaf into that specific recursive call.
		 */
		else if((a.nodeType==1)){ // If the space is occupied by a leaf make it a gray node  
		canvas.addLine(p1x,(p2y+p1y)/2,p2x,(p2y+p1y)/2, Color.BLACK);
			canvas.addLine((p1x+p2x)/2, p1y, (p1x+p2x)/2,p2y, Color.BLACK);
			//canvas.addPoint((p1x+p2x)/2 + "," + (p1y+p2y)/2, (p1x+p2x)/2, (p1y+p2y)/2, Color.RED);
			a.nodeType=2;

			if(a.city.x>=((p1x+p2x)/2) && a.city.y>=((p1y+p2y)/2)){//NE: Falls in the top right quad, y bottom bound included
				a.NE=new PRNode(a.city,(p1x+p2x)/2,(p1y+p2y)/2,p2x,p2y,null,null,null,null,1);
				a.NW= new PRNode(null,p1x,(p1y+p2y)/2,(p1x+p2x)/2,p2y,null,null,null,null,3);
				a.SW = new PRNode(null,p1x,p1y,(p1x+p2x)/2,(p1y+p2y)/2,null,null,null,null,3);
				a.SE= new PRNode(null,(p1x+p2x)/2,p1y,p2x,(p1y+p2y)/2,null,null,null,null,3);
				a.city=null;
				
			}
			else if(a.city.x<((p1x+p2x)/2) && a.city.y>=((p1y+p2y)/2)){//NW: Falls in the top left quad, x right bound included
				a.NE=new PRNode(null,(p1x+p2x)/2,(p1y+p2y)/2,p2x,p2y,null,null,null,null,3);
				a.NW= new PRNode(a.city,p1x,(p1y+p2y)/2,(p1x+p2x)/2,p2y,null,null,null,null,1);
				a.SW = new PRNode(null,p1x,p1y,(p1x+p2x)/2,(p1y+p2y)/2,null,null,null,null,3);
				a.SE= new PRNode(null,(p1x+p2x)/2,p1y,p2x,(p1y+p2y)/2,null,null,null,null,3);
				a.city=null;
			}
			else if(a.city.x>=((p1x+p2x)/2) && a.city.y<((p1y+p2y)/2)){//SE: Falls in the bottom right Quad, x left bound included
				a.NE=new PRNode(null,(p1x+p2x)/2,(p1y+p2y)/2,p2x,p2y,null,null,null,null,3);
				a.NW= new PRNode(null,p1x,(p1y+p2y)/2,(p1x+p2x)/2,p2y,null,null,null,null,3);
				a.SW = new PRNode(null,p1x,p1y,(p1x+p2x)/2,(p1y+p2y)/2,null,null,null,null,3);
				a.SE= new PRNode(a.city,(p1x+p2x)/2,p1y,p2x,(p1y+p2y)/2,null,null,null,null,1);
				a.city=null;
			}
			else if(a.city.x<((p1x+p2x)/2) && a.city.y<((p1y+p2y)/2)){//SW: If it falls in the bottom left quad, including y upper line bound
				a.NE=new PRNode(null,(p1x+p2x)/2,(p1y+p2y)/2,p2x,p2y,null,null,null,null,3);
				a.NW= new PRNode(null,p1x,(p1y+p2y)/2,(p1x+p2x)/2,p2y,null,null,null,null,3);
				a.SW = new PRNode(a.city,p1x,p1y,(p1x+p2x)/2,(p1y+p2y)/2,null,null,null,null,1);
				a.SE= new PRNode(null,(p1x+p2x)/2,p1y,p2x,(p1y+p2y)/2,null,null,null,null,3);
				a.city=null;
			}
			
			/*if (value.x>=((p1x+p2x)/2) && value.y>=((p1y+p2y)/2)){
				insert(a.NE,(p1x+p2x)/2,(p1y+p2y)/2,p2x,p2y,value);
			}*/
			if (value.x<((p1x+p2x)/2) && value.y>=((p1y+p2y)/2)){
				insert(a.NW,p1x,(p1y+p2y)/2,(p1x+p2x)/2,p2y,value);
			}
			else if (value.x>=((p1x+p2x)/2) && value.y<((p1y+p2y)/2)){
				insert(a.SE,(p1x+p2x)/2,p1y,p2x,(p1y+p2y)/2,value);
			}
			else if (value.x<((p1x+p2x)/2) && value.y<((p1y+p2y)/2)){
				insert(a.SW,p1x,p1y,(p1x+p2x)/2,(p1y+p2y)/2,value);
			}
			else if (value.x>=((p1x+p2x)/2) && value.y>=((p1y+p2y)/2)){
				insert(a.NE,(p1x+p2x)/2,(p1y+p2y)/2,p2x,p2y,value);
			}

		}
		else if (a.nodeType==2){ // nodeType 2 is Internal Nodes

			if (value.x>=((p1x+p2x)/2) && value.y>=((p1y+p2y)/2)){
				insert(a.NE,(p1x+p2x)/2,(p1y+p2y)/2,p2x,p2y,value);
			}
			else if (value.x<((p1x+p2x)/2) && value.y>=((p1y+p2y)/2)){
				insert(a.NW,p1x,(p1y+p2y)/2,(p1x+p2x)/2,p2y,value);
			}
			else if (value.x<((p1x+p2x)/2) && value.y<((p1y+p2y)/2)){
				insert(a.SW,p1x,p1y,(p1x+p2x)/2,(p1y+p2y)/2,value);
			}
			else if (value.x>=((p1x+p2x)/2) && value.y<((p1y+p2y)/2)){
				insert(a.SE,(p1x+p2x)/2,p1y,p2x,(p1y+p2y)/2,value);
			}      	
		}
		else{
			a.nodeType=1;
			a.city=value;
			canvas.addPoint(value.getName() , value.x, value.y, Color.BLACK);
		} 
	}
	public void makeBlackList(){
		this.blacklist=new ArrayList<PRNode>();
		makeBlackList(this.root);
	}
	public void makeBlackList(PRNode node){
		if(node==null){
			return;
		}
		else if(node.nodeType==1 ){
			blacklist.add(node);
		}
		else{
			makeBlackList(node.NE);
			//list.add(node.NW);
			makeBlackList(node.NW);
			//list.add(node.SW);
			makeBlackList(node.SW);
			//list.add(node.SE);
			makeBlackList(node.SE);
		}
	}

	
public void makeRoads(){
	for(PRNode n: blacklist){
		for (PRNode i: blacklist)
		canvas.addLine(n.city.x, n.city.y, i.city.x, i.city.y, Color.BLACK);
	}
}
public StringDictionary inRange(int x,int y, int radius){
	NameComparator c = new NameComparator();
	StringDictionary result=new StringDictionary(c);
	
		for(PRNode n: this.blacklist){
			int xdist=Math.abs((int)n.city.x-x);
			int ydist=Math.abs((int)n.city.y-y);
			double magnitude = Math.sqrt((xdist*xdist)+ (ydist*ydist));
			
			
			if(magnitude<=radius){
				result.put(n.city.getName(), n.city);
			}
		}
		return result;
}

public City closestCity(int x, int y){
	City minCity=null;
	if(!blacklist.isEmpty()){
	 minCity = blacklist.get(0).city;
	double minLength;
		int xdis=Math.abs((int)minCity.x-x);
		int ydis=Math.abs((int)minCity.y-y);
	minLength = Math.sqrt((xdis*xdis)+ (ydis*ydis));
	for(PRNode n: this.blacklist){
		int xdist=Math.abs((int)n.city.x-x);
		int ydist=Math.abs((int)n.city.y-y);
		double magnitude = Math.sqrt((xdist*xdist)+ (ydist*ydist));
		if(magnitude<minLength){
			minLength = magnitude;
			minCity = n.city;
		}
		else if(magnitude==minLength){
			if(minCity.getName().compareTo(n.city.getName())<1){
				minCity = n.city;
				
			}
		}
	}
	}
	return minCity;
}
	
	
public void toList(PRNode node){
	if(node==null){
		return;
	}
	else if(node.nodeType==1 || node.nodeType==3){
		list.add(node);
	}
	else{
		list.add(node);
		toList(node.NE);
		//list.add(node.NW);
		toList(node.NW);
		//list.add(node.SW);
		toList(node.SW);
		//list.add(node.SE);
		toList(node.SE);
	}
}

	public PRNode search(int x,int y){
		return searchHelper(x,y,this.root);
	}
	public PRNode searchHelper(int x, int y, PRNode node){
		if(node==null){
			return null;
		}
		else if (node.nodeType==1 && (float)x==node.city.x && (float)y==node.city.y)
			return node;

		else {
			if (x>=((node.x1+node.x2)/2) && y>=((node.y1+node.y2)/2)){
				return searchHelper(x,y,node.NE);

			}
			else if (x<((node.x1+node.x2)/2) && y>=((node.y1+node.y2)/2)){
				return searchHelper(x,y,node.NW);

			}
			else if (x<((node.x1+node.x2)/2) && y<((node.y1+node.y2)/2)){
				return searchHelper(x,y,node.SW);

			}
			else if (x>=((node.x1+node.x2)/2) && y<((node.y1+node.y2)/2)){
				return searchHelper(x,y,node.SE);
			}
			else{
				return null;
			}
		}
	}  


	public PRNode childSearch(int x, int y, PRNode node){
		//System.out.println(node + " child Seraching.");
		if(node==null){
			return null;
		}
		
		else if (node.nodeType==2 && node.NE!=null && node.NE.nodeType==1  &&  (float)x==node.NE.city.x && (float)y==node.NE.city.y)
			return node;
		else if (node.nodeType==2 && node.NW!=null && node.NW.nodeType==1  && (float)x==node.NW.city.x && (float)y==node.NW.city.y)
			return node;
		else if (node.nodeType==2 && node.SW!=null && node.SW.nodeType==1  && (float)x==node.SW.city.x && (float)y==node.SW.city.y)
			return node;
		else if (node.nodeType==2 && node.SE!=null && node.SE.nodeType==1  && (float)x==node.SE.city.x && (float)y==node.SE.city.y)
			return node;

		else {
			if (x>=((node.x1+node.x2)/2) && y>=((node.y1+node.y2)/2)){
				return childSearch(x,y,node.NE);

			}
			else if (x<((node.x1+node.x2)/2) && y>=((node.y1+node.y2)/2)){
				return childSearch(x,y,node.NW);

			}
			else if (x<((node.x1+node.x2)/2) && y<((node.y1+node.y2)/2)){
				return childSearch(x,y,node.SW);

			}
			else if (x>=((node.x1+node.x2)/2) && y<((node.y1+node.y2)/2)){
				return childSearch(x,y,node.SE);

			}
			else{
				return null;
			}
		}
	}  

public boolean delete(City c){
	if (this.root==null){
		return false;
	}
	else if ( this.root.nodeType==1 && this.root.city.x==c.x && this.root.city.y==c.y){
		this.root=null;
		return true;
	}
	else {
		return deleteHelper((int)c.x, (int)c.y,this.root);
	}
}
public boolean deleteHelper(int x, int y, PRNode node){
	PRNode p = childSearch(x,y,node);
	//System.out.println(p + " This is p");
	
	if (p!=null){
		/*System.out.println(p.NE);
		System.out.println(p.NW);
		System.out.println(p.SW);
		System.out.println(p.SE);*/
		if ( (p.NE==null || p.NE.nodeType==1) && (p.NW==null ||p.NW.nodeType==1) && (p.SW==null ||p.SW.nodeType==3) &&(p.SE==null || p.SE.nodeType==3)){
			if(p.NE.city.x==x && p.NE.city.y==y){
			p.city=p.NW.city;
			p.nodeType=1;
			p.NW=null;
			p.SW=null;
			p.SE=null;
			p.NE=null;
			}
			else if(p.NW.city.x==x && p.NW.city.y==y){
				p.city=p.NE.city;
				p.nodeType=1;
				p.NW=null;
				p.SW=null;
				p.SE=null;
				p.NE=null;
				}
			//System.out.println("halla1");
			canvas.removeLine(p.x1,(p.y2+p.y1)/2,p.x2,(p.y2+p.y1)/2, Color.BLACK);
			canvas.removeLine((p.x1+p.x2)/2, p.y1, (p.x1+p.x2)/2,p.y2, Color.BLACK);
			
			 makeParent(p.city);
			 return true;
		}
		else if ((p.NE==null || p.NE.nodeType==1) && (p.NW==null ||p.NW.nodeType==3) && (p.SW==null || p.SW.nodeType==1) && (p.SE==null || p.SE.nodeType==3)){
			if(p.NE.city.x==x && p.NE.city.y==y){
				p.city=p.SW.city;
				p.nodeType=1;
				p.NW=null;
				p.SW=null;
				p.SE=null;
				p.NE=null;
				}
				else if(p.SW.city.x==x && p.SW.city.y==y){
					canvas.removePoint(p.SW.city.getName(), x, y, Color.BLACK);
					p.city=p.NE.city;
					p.nodeType=1;
					p.NW=null;
					p.SW=null;
					p.SE=null;
					p.NE=null;
					}
			//System.out.println("halla2");
			canvas.removeLine(p.x1,(p.y2+p.y1)/2,p.x2,(p.y2+p.y1)/2, Color.BLACK);
			canvas.removeLine((p.x1+p.x2)/2, p.y1, (p.x1+p.x2)/2,p.y2, Color.BLACK);
			
			 makeParent(p.city);
			 return true;
		}
		else if ((p.NE==null ||p.NE.nodeType==1) && (p.NW==null || p.NW.nodeType==3) && (p.SW==null ||p.SW.nodeType==3) && (p.SE==null || p.SE.nodeType==1)){
			if(p.NE.city.x==x && p.NE.city.y==y){
				canvas.removePoint(p.NE.city.getName(), x, y, Color.BLACK);
				p.city=p.SE.city;
				p.nodeType=1;
				p.NW=null;
				p.SW=null;
				p.SE=null;
				p.NE=null;
				}
				else if(p.SE.city.x==x && p.SE.city.y==y){
					p.city=p.NE.city;
					p.nodeType=1;
					p.NW=null;
					p.SW=null;
					p.SE=null;
					p.NE=null;
					}
			//System.out.println("halla3");
			canvas.removeLine(p.x1,(p.y2+p.y1)/2,p.x2,(p.y2+p.y1)/2, Color.BLACK);
			canvas.removeLine((p.x1+p.x2)/2, p.y1, (p.x1+p.x2)/2,p.y2, Color.BLACK);
			makeParent(p.city);
			return true;
		}
		else if ((p.NE==null || p.NE.nodeType==3) && (p.NW==null || p.NW.nodeType==1) && (p.SW==null ||p.SW.nodeType==1) && (p.SW==null || p.SE.nodeType==3)){
			if(p.SW.city.x==x && p.SW.city.y==y){
				p.city=p.NW.city;
				p.nodeType=1;
				p.NW=null;
				p.SW=null;
				p.SE=null;
				p.NE=null;
				}
				else if(p.NW.city.x==x && p.NW.city.y==y){
					p.city=p.SW.city;
					p.nodeType=1;
					p.NW=null;
					p.SW=null;
					p.SE=null;
					p.NE=null;
					}
			//System.out.println("halla4");
			canvas.removeLine(p.x1,(p.y2+p.y1)/2,p.x2,(p.y2+p.y1)/2, Color.BLACK);
			canvas.removeLine((p.x1+p.x2)/2, p.y1, (p.x1+p.x2)/2,p.y2, Color.BLACK);
			makeParent(p.city);
			return true;
		}else if ((p.NE==null || p.NE.nodeType==3) && (p.NW==null ||p.NW.nodeType==1) && (p.SW==null || p.SW.nodeType==3) && (p.SW==null ||p.SE.nodeType==1)){
			if(p.SE.city.x==x && p.SE.city.y==y){
				canvas.removePoint(p.SE.city.getName(), x, y, Color.BLACK);
				p.city=p.NW.city;
				p.nodeType=1;
				p.NW=null;
				p.SW=null;
				p.SE=null;
				p.NE=null;
				}
				else if(p.NW.city.x==x && p.NW.city.y==y){
					canvas.removePoint(p.NW.city.getName(), x, y, Color.BLACK);
					p.city=p.SE.city;
					p.nodeType=1;
					p.NW=null;
					p.SW=null;
					p.SE=null;
					p.NE=null;
					}
			//System.out.println("halla5");
			
			canvas.removeLine(p.x1,(p.y2+p.y1)/2,p.x2,(p.y2+p.y1)/2, Color.BLACK);
			canvas.removeLine((p.x1+p.x2)/2, p.y1, (p.x1+p.x2)/2,p.y2, Color.BLACK);
			makeParent(p.city);
			return true;
		}
		else if ((p.NE == null || p.NE.nodeType==3) && (p.NW==null || p.NW.nodeType==3) && (p.SW==null || p.SW.nodeType==1) && (p.SE==null || p.SE.nodeType==1)){
			if(p.SW.city.x==x && p.SW.city.y==y){
				p.city=p.SE.city;
				p.nodeType=1;
				p.NW=null;
				p.SW=null;
				p.SE=null;
				p.NE=null;
				}
				else if(p.SE.city.x==x && p.SE.city.y==y){
					p.city=p.SW.city;
					p.nodeType=1;
					p.NW=null;
					p.SW=null;
					p.SE=null;
					p.NE=null;
					}
			//System.out.println("halla6");
			canvas.removeLine(p.x1,(p.y2+p.y1)/2,p.x2,(p.y2+p.y1)/2, Color.BLACK);
			canvas.removeLine((p.x1+p.x2)/2, p.y1, (p.x1+p.x2)/2,p.y2, Color.BLACK);
			makeParent(p.city);
			return true;
		}
		else {
			/*System.out.println(p.NE + "this is P.NE");
			System.out.println(p.NW + "this is P.NW");
			System.out.println(p.SE + "this is P.SE");
			System.out.println(p.SW + "this is P.SW");*/
			
			if(p.NE.city!=null && (float)x==p.NE.city.x && (float)y==p.NE.city.y){
				p.NE=new PRNode(null,p.NE.x1,p.NE.y1,p.NE.x2,p.NE.y2,null,null,null,null,3);
				//System.out.println("halla walla");
				return true;
			}
		else if(p.NW.city!=null && (float)x==p.NW.city.x && (float)y==p.NW.city.y){
			p.NW=new PRNode(null,p.NE.x1,p.NE.y1,p.NE.x2,p.NE.y2,null,null,null,null,3);
				//System.out.println("halla walla");
				return true;
			}
		else if(p.SW.city!=null && (float)x==p.SW.city.x && (float)y==p.SW.city.y){
			p.SW=new PRNode(null,p.NE.x1,p.NE.y1,p.NE.x2,p.NE.y2,null,null,null,null,3);
			//System.out.println("halla walla");
			return true;
			}
		else if(p.SE.city!=null && (float)x==p.SE.city.x && (float)y==p.SE.city.y){
			p.SE=new PRNode(null,p.NE.x1,p.NE.y1,p.NE.x2,p.NE.y2,null,null,null,null,3);
			//System.out.println("halla walla");
			return true;
			}
			
		}
	}
	return false;
}

public void makeParent(City c){
	PRNode p = childSearch((int)c.x,(int)c.y,this.root);
	if (p!=null){
	if (p.NE.nodeType==1 && p.NW.nodeType==3 && p.SW.nodeType==3 && p.SE.nodeType==3){
		p.nodeType=1;
		p.city=p.NE.city;
		p.NW=null;
		p.SW=null;
		p.SE=null;
		p.NE=null;
		canvas.removeLine(p.x1,(p.y2+p.y1)/2,p.x2,(p.y2+p.y1)/2, Color.BLACK);
		canvas.removeLine((p.x1+p.x2)/2, p.y1, (p.x1+p.x2)/2,p.y2, Color.BLACK);
		makeParent(p.city);
	}
	else if (p.NE.nodeType==3 && p.NW.nodeType==1 && p.SW.nodeType==3 && p.SE.nodeType==3){
		p.nodeType=1;
		p.city=p.NW.city;
		p.NW=null;
		p.SW=null;
		p.SE=null;
		p.NE=null;
		canvas.removeLine(p.x1,(p.y2+p.y1)/2,p.x2,(p.y2+p.y1)/2, Color.BLACK);
		canvas.removeLine((p.x1+p.x2)/2, p.y1, (p.x1+p.x2)/2,p.y2, Color.BLACK);
		makeParent(p.city);
	}
	else if (p.NE.nodeType==3 && p.NW.nodeType==3 && p.SW.nodeType==1 && p.SE.nodeType==3){
		p.nodeType=1;
		p.city=p.SW.city;
		p.NW=null;
		p.SW=null;
		p.SE=null;
		p.NE=null;
		//canvas.removePoint((p.x1+p.x2)/2 + "," + (p.y1+p.y2)/2, (p.x1+p.x2)/2, (p.y1+p.y2)/2, Color.RED);
		canvas.removeLine(p.x1,(p.y2+p.y1)/2,p.x2,(p.y2+p.y1)/2, Color.BLACK);
		canvas.removeLine((p.x1+p.x2)/2, p.y1, (p.x1+p.x2)/2,p.y2, Color.BLACK);
		makeParent(p.city);
	}
	else if (p.NE.nodeType==3 && p.NW.nodeType==3 && p.SW.nodeType==3 && p.SE.nodeType==1){
		p.nodeType=1;
		p.city=p.SE.city;
		p.NW=null;
		p.SW=null;
		p.SE=null;
		p.NE=null;
		canvas.removeLine(p.x1,(p.y2+p.y1)/2,p.x2,(p.y2+p.y1)/2, Color.BLACK);
		canvas.removeLine((p.x1+p.x2)/2, p.y1, (p.x1+p.x2)/2,p.y2, Color.BLACK);
		makeParent(p.city);
	}
	}
}

}

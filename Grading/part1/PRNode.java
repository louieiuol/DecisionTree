package cmsc420.meeshquest.part1;


public class PRNode {
	  City city;
	  int x1,y1,x2,y2; //size of the Node
	  PRNode NE,NW,SE,SW;
	  int nodeType ;// Nodetype 1 is a leaf (black), 2 is an Internal Node(grey), and 3 is an empty Node (white)
	  
	  public PRNode(City city,int x1,int y1,int x2,int y2,PRNode NE,
			  PRNode NW, PRNode SE, PRNode SW,int ntype){
		  	this.NW=NW;
		  	this.NE=NE;
			this.SE=SE;
			this.SW=SW;
			this.city=city;
		  	this.x1= x1;
			this.y1=y1;
			this.x2=x2;
			this.y2=y2;
			this.nodeType=ntype;
	  }
	  
	 public String toString(){
		 StringBuilder str = new StringBuilder();
		 if (this.city!=null){
		 str.append("City: " + city.toString());
		 }
		 float x3 = (x1+x2)/2;
		 float y3 = (y1+y2)/2;
		 
		 
		 str.append(" bottom left: " + "(" + x1 + "," + y1 + ")");
		 str.append(" top right: " + "(" + x2 + "," + y2 + ")");
		 if (nodeType==1){
		 str.append(" nodeType: " + nodeType + " (Black Node)");
		 }
		 else if (nodeType==2){
			 str.append(" midpoint " +  "(" + x3 + "," + y3 + ")");
			 str.append(" nodeType: " + nodeType + " (Gray Node)");
			 }
		 if (nodeType==3){
			 str.append(" nodeType: " + nodeType + " (whiteNode)");
			 }
		 return str.toString();
	 }
	 
  }


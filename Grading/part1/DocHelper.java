package cmsc420.meeshquest.part1;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import cmsc420.xml.XmlUtility;


public class DocHelper {

Document r=null;

public DocHelper(){

	try {
		r= XmlUtility.getDocumentBuilder().newDocument();
	} catch (ParserConfigurationException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	Element quadTree = r.createElement("quadtree");
	r.appendChild(quadTree);
}

public void printDoc(PRNode node, Node e){
	if(node==null){
		return;
	}
	if(node.nodeType==3){
		Element white =r.createElement("white");
		e.appendChild(white);
	}
	else if (node.nodeType==1){
		Element black = r.createElement("black");
		black.setAttribute("name", node.city.getName());
		black.setAttribute("x", Integer.toString((int)node.city.x));
		black.setAttribute("y", Integer.toString((int)node.city.y));
		e.appendChild(black);
		
	}
	else{
	Element gray = r.createElement("gray");
	gray.setAttribute("x", Integer.toString((int)((node.x1 + node.x2)/2)));
	gray.setAttribute("y", Integer.toString((int)((node.y1+node.y2)/2)));
	printDoc(node.NW,gray);
	printDoc(node.NE,gray);
	printDoc(node.SW,gray);
	printDoc(node.SE,gray);
	
	e.appendChild(gray);
	
	
	
	}
}
}



package cmsc420.meeshquest.part1;


import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cmsc420.drawing.CanvasPlus;
import cmsc420.xml.XmlUtility;

public class MeeshQuest {

	public static void main(String[] args)  {

		Document results = null;

		try {
			File f = new File("temp.xml");
			//System.out.println(f.length());
			Dictionary dictionary = new Dictionary();
			PRQuadTree tree=null;
			Document doc = XmlUtility.validateNoNamespace(System.in);
			//Document doc = XmlUtility.validateNoNamespace(f);
			results = XmlUtility.getDocumentBuilder().newDocument();
			Document bruh = XmlUtility.getDocumentBuilder().newDocument();
			Element commandNode = doc.getDocumentElement();
			Element res = results.createElement("results");
			results.appendChild(res);
			AvlGTree<String,City> avl = new AvlGTree<String,City>(new NameComparator(), 1);
			int spatialWidth = Integer.parseInt(commandNode.getAttribute("spatialWidth"));
			int spatialHeight = Integer.parseInt(commandNode.getAttribute("spatialHeight"));
			CanvasPlus canv = new CanvasPlus("MeeshQuest", spatialWidth,spatialHeight);
			tree = new PRQuadTree(spatialWidth,spatialHeight,canv);
			//Document rock = XmlUtility.getDocumentBuilder().newDocument();
			//Element o = rock.createElement("output");
			//Element fam = rock.createElement("fam");
			/*rock.appendChild(o);
        	o.appendChild(fam);
        	results.adoptNode(o);
        	res.appendChild(o);*/




			final NodeList nl = commandNode.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				if (nl.item(i).getNodeType() == Document.ELEMENT_NODE) {
					commandNode = (Element) nl.item(i);
					Element success = results.createElement("success");
					Element parameters = results.createElement("parameters");
					Element comm = results.createElement("command");
					Element nam= results.createElement("name");
					Element ex= results.createElement("x");
					Element why = results.createElement("y");
					Element rad = results.createElement("radius");
					Element col= results.createElement("color");
					Element output= results.createElement("output");
					Element sortBy= results.createElement("sortBy");
					Element cityList = results.createElement("cityList");
					Element quadTree= results.createElement("quadtree");
					Element error = results.createElement("error");
					Element cityUnmapped = results.createElement("cityUnmapped");
					Element city = results.createElement("city");
					Element saveMap= results.createElement("saveMap");

				
					if (commandNode.getNodeName().equals("createCity"))
					{	
						String name =commandNode.getAttribute("name");
						float xco = Float.parseFloat(commandNode.getAttribute("x"));
						float yco = Float.parseFloat(commandNode.getAttribute("y"));
						String color =commandNode.getAttribute("color");
						float radius = Float.parseFloat(commandNode.getAttribute("radius"));
						City c = new City(name,color,xco,yco,radius);
						int x= dictionary.add(c);
						if(x==0){
							res.appendChild(success);
							success.appendChild(comm);
							comm.setAttribute("name", "createCity");
							success.appendChild(parameters);
							parameters.appendChild(nam);
							nam.setAttribute("value", name);
							parameters.appendChild(ex);
							ex.setAttribute("value",Integer.toString((int)xco));
							parameters.appendChild(why);
							why.setAttribute("value",Integer.toString((int)yco));
							parameters.appendChild(rad);
							rad.setAttribute("value",Integer.toString((int)radius));
							parameters.appendChild(col);
							col.setAttribute("value",color);
							success.appendChild(output);
						}
						else if(x==1){
							error.setAttribute("type", "duplicateCityCoordinates");
							res.appendChild(error);
							error.appendChild(comm);
							comm.setAttribute("name", "createCity");
							error.appendChild(parameters);
							parameters.appendChild(nam);
							nam.setAttribute("value", name);
							parameters.appendChild(ex);
							ex.setAttribute("value",Integer.toString((int)xco));
							parameters.appendChild(why);
							why.setAttribute("value",Integer.toString((int)yco));
							parameters.appendChild(rad);
							rad.setAttribute("value",Integer.toString((int)radius));
							parameters.appendChild(col);
							col.setAttribute("value",color);
						}
						else if (x==2){
							error.setAttribute("type", "duplicateCityName");
							res.appendChild(error);
							error.appendChild(comm);
							comm.setAttribute("name", "createCity");
							error.appendChild(parameters);
							parameters.appendChild(nam);
							nam.setAttribute("value", name);
							parameters.appendChild(ex);
							ex.setAttribute("value",Integer.toString((int)xco));
							parameters.appendChild(why);
							why.setAttribute("value",Integer.toString((int)yco));
							parameters.appendChild(rad);
							rad.setAttribute("value",Integer.toString((int)radius));
							parameters.appendChild(col);
							col.setAttribute("value",color);
						}

					}

					else if (commandNode.getNodeName().equals("listCities")){
						String sort =commandNode.getAttribute("sortBy");
						if (dictionary.sd.size()==0){
							error.setAttribute("type", "noCitiesToList");
							res.appendChild(error);
							error.appendChild(comm);
							comm.setAttribute("name", "listCities");
							error.appendChild(parameters);
							parameters.appendChild(sortBy);
							sortBy.setAttribute("value", sort);
						}
						else{
						
						res.appendChild(success);
						success.appendChild(comm);
						comm.setAttribute("name", "listCities");
						success.appendChild(parameters);
						sortBy.setAttribute("value", sort);
						parameters.appendChild(sortBy);
						success.appendChild(output);
						output.appendChild(cityList);
						
						if(sort.equalsIgnoreCase("coordinate")){
							for(Map.Entry<City,String> entry : dictionary.cd.entrySet()) {

								City key = entry.getKey();
								Element c= results.createElement("city");
								c.setAttribute("name", key.getName());
								c.setAttribute("x", Integer.toString((int)key.x));
								c.setAttribute("y", Integer.toString((int)key.y));
								c.setAttribute("color", key.getColor());
								c.setAttribute("radius", Integer.toString((int)key.getRadius()));
								cityList.appendChild(c);
							}
						}
						else if(sort.equals("name")){
							for(Map.Entry<String,City> entry : dictionary.sd.entrySet()) {

								City key = entry.getValue();
								Element c= results.createElement("city");
								c.setAttribute("name",key.getName() );
								c.setAttribute("x", Integer.toString((int)key.x));
								c.setAttribute("y", Integer.toString((int)key.y));
								c.setAttribute("color", key.getColor());
								c.setAttribute("radius", Integer.toString((int)key.getRadius()));
								cityList.appendChild(c);
							}
						}
						}
						
					}

					
        			else if(commandNode.getNodeName().equals("deleteCity")){

        				String name =commandNode.getAttribute("name");
        				City c = dictionary.getCity(name);
        				tree.toList(tree.root);
        				//System.out.println(dictionary.toString());
        				//System.out.println(name + "yoyoyoyoy");
        				int num= dictionary.remove(name);
        				//System.out.println(dictionary.toString());
        				
        				if(num==0){
        					
        				res.appendChild(success);
            			success.appendChild(comm);
            			comm.setAttribute("name", "deleteCity");
            			success.appendChild(parameters);
            			parameters.appendChild(nam);
            			nam.setAttribute("value", name);
            			success.appendChild(output);
            			
            			boolean del = tree.delete(c);
            			if(del==true){
            				
            				
        					cityUnmapped.setAttribute("name", c.getName());
        					cityUnmapped.setAttribute("x", Integer.toString((int)c.x));
        					cityUnmapped.setAttribute("y", Integer.toString((int)c.y));
        					cityUnmapped.setAttribute("color", c.getColor());
        					cityUnmapped.setAttribute("radius", Integer.toString((int)c.getRadius()));
        					output.appendChild(cityUnmapped);
        					
        					}
        				}
        				else{
        					error.setAttribute("type", "cityDoesNotExist");
							res.appendChild(error);
							error.appendChild(comm);
							comm.setAttribute("name", "deleteCity");
							error.appendChild(parameters);
							parameters.appendChild(nam);
							nam.setAttribute("value",name);
        				}
        			}
					
        			else if(commandNode.getNodeName().equals("clearAll")){
        				//tree.canvas.clear();
        				tree=new PRQuadTree(spatialWidth,spatialHeight,canv);
        				avl.clear();
        				dictionary=new Dictionary();
        				res.appendChild(success);
            			success.appendChild(comm);
            			comm.setAttribute("name", "clearAll");
            			success.appendChild(parameters);
            			success.appendChild(output);
        			}
					else if(commandNode.getNodeName().equals("mapCity")){
						String name =commandNode.getAttribute("name");
						// If command fails with name not in dictionary
						City c =dictionary.getCity(name);
						
						if(c==null){
							error.setAttribute("type", "nameNotInDictionary");
							res.appendChild(error);
							error.appendChild(comm);
							comm.setAttribute("name", "mapCity");
							error.appendChild(parameters);
							parameters.appendChild(nam);
							nam.setAttribute("value",name);
						}
						else{
							int num = tree.insert(c);
							
						// If command fails with cityAlreadyMapped
						 if(num==2){
							error.setAttribute("type", "cityAlreadyMapped");
							res.appendChild(error);
							error.appendChild(comm);
							comm.setAttribute("name", "mapCity");
							error.appendChild(parameters);
							parameters.appendChild(nam);
							nam.setAttribute("value",name);
						}
						// If command fails with cityOutOfBounds
						else if(num==3){
							error.setAttribute("type", "cityOutOfBounds");
							res.appendChild(error);
							error.appendChild(comm);
							comm.setAttribute("name", "mapCity");
							error.appendChild(parameters);
							parameters.appendChild(nam);
							nam.setAttribute("value",name);
						}
						else {
							//if successful
							avl.put(c.getName(),c);
							res.appendChild(success);
							success.appendChild(comm);
							comm.setAttribute("name", "mapCity");
							success.appendChild(parameters);
							parameters.appendChild(nam);
							nam.setAttribute("value", name);
							success.appendChild(output);

						}
						}

					}
					else if(commandNode.getNodeName().equals("unmapCity")){
						String name =commandNode.getAttribute("name");
						// If command fails with name not in dictionary
						City c =dictionary.getCity(name);
						// If command fails with cityNotInDictionary
						if (c==null){
							error.setAttribute("type", "nameNotInDictionary");
							res.appendChild(error);
							error.appendChild(comm);
							comm.setAttribute("name", "unmapCity");
							error.appendChild(parameters);
							parameters.appendChild(nam);
							nam.setAttribute("value",name);
						}
						// If command fails with cityNotMapped
						else if((tree.search((int)c.x, (int)c.y))==null){
							error.setAttribute("type", "cityNotMapped");
							res.appendChild(error);
							error.appendChild(comm);
							comm.setAttribute("name", "unmapCity");
							error.appendChild(parameters);
							parameters.appendChild(nam);
							nam.setAttribute("value",name);
						}

						else{
							res.appendChild(success);
							success.appendChild(comm);
							comm.setAttribute("name", "unmapCity");
							success.appendChild(parameters);
							parameters.appendChild(nam);
							nam.setAttribute("value", name);
							success.appendChild(output);
							tree.delete(c);
						}

					}
					else if(commandNode.getNodeName().equals("printPRQuadtree")){
						if(tree.root==null){
							error.setAttribute("type", "mapIsEmpty");
							res.appendChild(error);
							error.appendChild(comm);
							comm.setAttribute("name", "printPRQuadtree");
							error.appendChild(parameters);
							
						}
						
						
						else {
							res.appendChild(success);
							success.appendChild(comm);
							comm.setAttribute("name", "printPRQuadtree");
							success.appendChild(parameters);
							success.appendChild(output);
							
							DocHelper hello = new DocHelper();
							hello.printDoc(tree.root, hello.r.getFirstChild());
							Node kk= results.adoptNode(hello.r.getFirstChild());
							output.appendChild(kk);
						}
					}
					
				else if(commandNode.getNodeName().equals("nearestCity")){
							tree.makeBlackList();
							int xco = Integer.parseInt(commandNode.getAttribute("x"));
							int yco = Integer.parseInt(commandNode.getAttribute("y"));
							if(tree.blacklist.size()==0){
							error.setAttribute("type", "mapIsEmpty");
							res.appendChild(error);
							error.appendChild(comm);
							comm.setAttribute("name", "nearestCity");
							error.appendChild(parameters);
							parameters.appendChild(ex);
							ex.setAttribute("value", Integer.toString(xco));
							parameters.appendChild(why);
							why.setAttribute("value",Integer.toString(yco));
							}
							else{
								
								
								City c= tree.closestCity(xco, yco);
								res.appendChild(success);
								success.appendChild(comm);
								comm.setAttribute("name", "nearestCity");
								success.appendChild(parameters);
								parameters.appendChild(ex);
								ex.setAttribute("value",Integer.toString(xco));
								parameters.appendChild(why);
								why.setAttribute("value",Integer.toString(yco));
								success.appendChild(output);
								output.appendChild(city);
								city.setAttribute("name", c.getName());
								city.setAttribute("x",Integer.toString((int)c.x));
								city.setAttribute("y",Integer.toString((int)c.y));
								city.setAttribute("color", c.getColor());
								city.setAttribute("radius",Integer.toString((int)c.getRadius()));
								
							}
						
						}
				else if(commandNode.getNodeName().equals("saveMap")){
					String name =commandNode.getAttribute("name");
					//tree.makeRoads();
					//tree.canvas.draw();
					//tree.canvas.save(name);
					//canvas.dispose();
					res.appendChild(success);
					success.appendChild(comm);
					comm.setAttribute("name", "saveMap");
					success.appendChild(parameters);
					parameters.appendChild(nam);
					nam.setAttribute("value", name);
					success.appendChild(output);
				}
				else if(commandNode.getNodeName().equals("rangeCities")){
					
					float xco = Float.parseFloat(commandNode.getAttribute("x"));
					float yco = Float.parseFloat(commandNode.getAttribute("y"));
					float radius = Float.parseFloat(commandNode.getAttribute("radius"));
					tree.makeBlackList();
					StringDictionary dict = tree.inRange((int)xco, (int)yco, (int)radius);
					if (dict.size()==0){
						error.setAttribute("type", "noCitiesExistInRange");
						res.appendChild(error);
						error.appendChild(comm);
						comm.setAttribute("name", "rangeCities");
						error.appendChild(parameters);
						parameters.appendChild(ex);
						ex.setAttribute("value", Integer.toString((int)xco));
						parameters.appendChild(why);
						why.setAttribute("value",Integer.toString((int)yco));
						parameters.appendChild(rad);
						rad.setAttribute("value", Integer.toString((int)radius));
						if(commandNode.hasAttribute("saveMap")){
							String save = commandNode.getAttribute("saveMap");
						parameters.appendChild(saveMap);
						saveMap.setAttribute("value", save);
						
						}
					}
					else{
					
						res.appendChild(success);
						success.appendChild(comm);
						comm.setAttribute("name", "rangeCities");
						success.appendChild(parameters);
						parameters.appendChild(ex);
						ex.setAttribute("value",Integer.toString((int)xco));
						parameters.appendChild(why);
						why.setAttribute("value",Integer.toString((int)yco));
						parameters.appendChild(rad);
						rad.setAttribute("value", Integer.toString((int)radius));
							if(commandNode.hasAttribute("saveMap")){
								String save = commandNode.getAttribute("saveMap");
							parameters.appendChild(saveMap);
							saveMap.setAttribute("value", save);
							//tree.canvas.addCircle(xco, yco, radius, Color.BLUE, false);
							//tree.canvas.save(save);
							//tree.canvas.dispose();
							}
						
						success.appendChild(output);
						output.appendChild(cityList);
						for(Map.Entry<String,City> entry : dict.entrySet()) {
							City key = entry.getValue();
							Element c= results.createElement("city");
							c.setAttribute("name", key.getName());
							c.setAttribute("x", Integer.toString((int)key.x));
							c.setAttribute("y", Integer.toString((int)key.y));
							c.setAttribute("color", key.getColor());
							c.setAttribute("radius", Integer.toString((int)key.getRadius()));
							cityList.appendChild(c);
						
						}
					}
					
					
				}
				else if(commandNode.getNodeName().equals("printAvlTree")){
					if(avl.size()==0){
					res.appendChild(error);
					error.setAttribute("type", "emptyTree");
					error.appendChild(comm);
					comm.setAttribute("name", "printAvlTree");
					error.appendChild(parameters);
					}
					else{
					res.appendChild(success);
					success.appendChild(comm);
					comm.setAttribute("name", "printAvlTree");
					success.appendChild(parameters);
					success.appendChild(output);
					Element e = avl.elementize(results);
					output.appendChild(e);
					}
				}
					/*TODO: Process your commandNode here  */
				}
			}
		} catch (SAXException | IOException | ParserConfigurationException e) {
			try {
				results = XmlUtility.getDocumentBuilder().newDocument();
				results.appendChild(results.createElement("fatalError"));
			} catch (ParserConfigurationException k) {
			}



		} finally {
			try {
				XmlUtility.print(results);
				
			} catch (TransformerException e) {
				e.printStackTrace();
			}
		}
	}


}


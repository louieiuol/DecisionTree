/*
 * @(#)Command.java 1.0 2007/01/23
 * 
 * Copyright Ben Zoller (University of Maryland, College Park), 2007 All rights
 * reserved. Permission is granted for use and modification in CMSC420 at the
 * University of Maryland.
 */
package cmsc420.command;

import java.awt.Color;
import java.awt.geom.Arc2D;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cmsc420.city.City;
import cmsc420.city.CityLocationComparator;
import cmsc420.city.CityNameComparator;
import cmsc420.dijkstra.Dijkstranator;
import cmsc420.dijkstra.Path;
import cmsc420.drawing.CanvasPlus;
import cmsc420.geom.Circle2D;
import cmsc420.geom.Inclusive2DIntersectionVerifier;
import cmsc420.geom.Shape2DDistanceCalculator;
import cmsc420.pmquadtree.IntersectingRoadsThrowable;
import cmsc420.pmquadtree.InvalidPartitionThrowable;
import cmsc420.pmquadtree.IsolatedCityAlreadyExistsThrowable;
import cmsc420.pmquadtree.IsolatedCityNotMappedThrowable;
import cmsc420.pmquadtree.IsolatedCityOutOfSpatialBoundsThrowable;
import cmsc420.pmquadtree.PM1Quadtree;
import cmsc420.pmquadtree.PM3Quadtree;
import cmsc420.pmquadtree.PMQuadtree;
import cmsc420.pmquadtree.RoadAlreadyExistsThrowable;
import cmsc420.pmquadtree.RoadNotMappedThrowable;
import cmsc420.pmquadtree.RoadOutOfSpatialBoundsThrowable;
import cmsc420.pmquadtree.PMQuadtree.Black;
import cmsc420.pmquadtree.PMQuadtree.Gray;
import cmsc420.pmquadtree.PMQuadtree.Node;
import cmsc420.portal.Portal;
import cmsc420.road.Road;
import cmsc420.road.RoadAdjacencyList;
import cmsc420.road.RoadNameComparator;
import cmsc420.sortedmap.AvlGTree;
import cmsc420.sortedmap.GuardedAvlGTree;
import cmsc420.sortedmap.StringComparator;
import cmsc420.xml.XmlUtility;

/**
 * Processes each command in the MeeshQuest program. Takes in an XML command
 * node, processes the node, and outputs the results.
 * 
 * Modified by Alan Jackoway to include a BP tree.
 * 
 * @author Ben Zoller
 * @version 2.0, 23 Jan 2007
 */
public class Command {
	/** output DOM Document tree */
	protected Document results;

	/** root node of results document */
	protected Element resultsNode;

	/** stores created cities sorted by name */
	protected AvlGTree<String, City> citiesByName;

	/** stores created cities sorted by location */
	protected final TreeSet<City> citiesByLocation =
		new TreeSet<City>(new CityLocationComparator());

	/** stores all mapped roads in a graph-style adjacency list */
	private final RoadAdjacencyList roads = new RoadAdjacencyList();

	/** stores mapped cities in a spatial data structure */
	protected TreeMap<Integer, PMQuadtree> pmQuadtree;

	protected TreeMap<String, City> portalDictionary;

	/** PM Quadtree order */
	protected int pmOrder;

	/** PM Quadtree spatial width */
	protected int spatialWidth;

	/** spatial height of the PM Quadtree */
	protected int spatialHeight;

	/**
	 * Creates the root results node and sets the DOM Document tree to send the
	 * results of processed commands to said node.
	 * 
	 * @param results			DOM Document tree
	 */
	public void setResults(Document results) {
		this.results = results;
		resultsNode = results.createElement("results");
		results.appendChild(resultsNode);
	}

	/**
	 * Creates a command result element. Initializes its command name and, if
	 * present, its id.
	 * 
	 * @param node				command node to be processed
	 * @return command result element
	 */
	private Element getCommandNode(final Element node) {
		final Element commandNode = results.createElement("command");
		commandNode.setAttribute("name", node.getNodeName());

		if (node.hasAttribute("id")) {
			commandNode.setAttribute("id", node.getAttribute("id"));
		}
		return commandNode;
	}

	/**
	 * Processes an integer attribute for a command. Appends the parameter to
	 * the parameters result element. Should not throw a number format
	 * exception if the attribute has been defined as an integer in the schema
	 * and the XML has been validated beforehand.
	 * 
	 * @param commandNode		node with information about the command
	 * @param attributeName		integer attribute to be processed
	 * @param parametersNode		node to append parameter information to
	 * @return integer attribute value
	 */
	private int processIntegerAttribute(final Element commandNode,
			final String attributeName, final Element parametersNode) {
		final String value = commandNode.getAttribute(attributeName);

		if (parametersNode != null) {
			final Element attributeNode = results.createElement(attributeName);
			attributeNode.setAttribute("value", value);
			parametersNode.appendChild(attributeNode);
		}
		return Integer.parseInt(value);
	}

	/**
	 * Processes a string attribute for a command. Appends the parameter to the
	 * parameters result element.
	 * 
	 * @param commandNode		node with information about the command
	 * @param attributeName		integer attribute to be processed
	 * @param parametersNode		node to append parameter information to
	 * @return string attribute value
	 */
	private String processStringAttribute(final Element commandNode,
			final String attributeName, final Element parametersNode) {
		final String value = commandNode.getAttribute(attributeName);

		if (parametersNode != null) {
			final Element attributeNode = results.createElement(attributeName);
			attributeNode.setAttribute("value", value);
			parametersNode.appendChild(attributeNode);
		}
		return value;
	}

	/**
	 * Reports that the requested command could not be performed due to an
	 * error. Appends information about the error to the results.
	 * 
	 * @param type				type of error that occurred
	 * @param command			command node being processed
	 * @param parameters			parameters of command
	 */
	private void addErrorNode(final String type, final Element command,
			final Element parameters) {
		final Element error = results.createElement("error");
		error.setAttribute("type", type);
		error.appendChild(command);
		error.appendChild(parameters);
		resultsNode.appendChild(error);
	}

	/**
	 * Reports that a command was successfully performed. Appends the report to
	 * the results.
	 * 
	 * @param command
	 *            command not being processed
	 * @param parameters
	 *            parameters used by the command
	 * @param output
	 *            any details to be reported about the command processed
	 */
	private Element addSuccessNode(final Element command, final Element parameters, final Element output) {
		final Element success = results.createElement("success");
		success.appendChild(command);
		success.appendChild(parameters);
		success.appendChild(output);
		resultsNode.appendChild(success);
		return success;
	}

	/**
	 * Processes the commands node (root of all commands). Gets the spatial
	 * width and height of the map, among other configuration parameters,
	 * and feeds them to the appropriate data structures.
	 * 
	 * @param node
	 *            commands node to be processed
	 */
	public void processCommands(final Element node) {
		spatialWidth = Integer.parseInt(node.getAttribute("spatialWidth"));
		spatialHeight = Integer.parseInt(node.getAttribute("spatialHeight"));
		pmOrder = 3;
		//        pmOrder = Integer.parseInt(node.getAttribute("pmOrder"));

		//        if (pmOrder == 3) {
		pmQuadtree = new TreeMap<Integer, PMQuadtree>();
		//        } else {

		//}
		portalDictionary = new TreeMap<String, City>();
//		System.out.print("g=" + node.getAttribute("g"));
	//	citiesByName = new GuardedAvlGTree<String, City>(null, Integer.parseInt(node.getAttribute("g")));
	
		 citiesByName = new AvlGTree<String, City>(null, Integer.parseInt(node.getAttribute("g")));
	}

	/**
	 * Processes a createCity command. Creates a city in the dictionary (Note:
	 * does not map the city). An error occurs if a city with that name or
	 * location is already in the dictionary.
	 * 
	 * @param node
	 *            createCity node to be processed
	 */
	public void processCreateCity(final Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");

		final String name = processStringAttribute(node, "name", parametersNode);
		final int x = processIntegerAttribute(node, "x", parametersNode);
		final int y = processIntegerAttribute(node, "y", parametersNode);
		final int z = processIntegerAttribute(node, "z", parametersNode);
		final int radius = processIntegerAttribute(node, "radius", parametersNode);
		final String color = processStringAttribute(node, "color", parametersNode);

		/* create the city */
		final City city = new City(name, x, y, z, radius, color);

		if (citiesByName.containsKey(name)) {
			addErrorNode("duplicateCityName", commandNode, parametersNode);
		} else if (citiesByLocation.contains(city)) {
			addErrorNode("duplicateCityCoordinates", commandNode, parametersNode);
		} else {
			final Element outputNode = results.createElement("output");

			/* add city to dictionary */
			citiesByName.put(name, city);
			citiesByLocation.add(city);

			/* add success node to results */
			addSuccessNode(commandNode, parametersNode, outputNode);
		}
	}

	/**
	 * Processes a deleteCity command. Deletes a city from the dictionary. An
	 * error occurs if the city does not exist or is currently mapped.
	 * 
	 * @param node
	 *            deleteCity node being processed
	 */
public void processDeleteCity(final Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final String name = processStringAttribute(node, "name", parametersNode);

		if (!citiesByName.containsKey(name)) {
			 //city with name does not exist 
			addErrorNode("cityDoesNotExist", commandNode, parametersNode);
		} else {
			 //delete city and all associated roads 
			final Element outputNode = results.createElement("output");
			final City deletedCity = citiesByName.get(name);
			final Set<Road> associatedRoads = roads.removeRoadsForCity(deletedCity);
			int z = deletedCity.getZ();

			//builds xml if quadTree has level z we are looking for and z level has city looking for
			if (associatedRoads != null 
					&& pmQuadtree.containsKey(z) 
					&& pmQuadtree.get(z).containsCity(name)) {
				try {
					final Element cityDeletedNode = results.createElement("cityUnmapped");
					cityDeletedNode.setAttribute("name", deletedCity.getName());
					cityDeletedNode.setAttribute("x", Integer.toString(deletedCity.getX()));
					cityDeletedNode.setAttribute("y", Integer.toString(deletedCity.getY()));
					cityDeletedNode.setAttribute("z", Integer.toString(deletedCity.getZ()));
					cityDeletedNode.setAttribute("radius", Integer.toString(deletedCity.getRadius()));
					cityDeletedNode.setAttribute("color", deletedCity.getColor());
					outputNode.appendChild(cityDeletedNode);

					//removes the associated roads to the city from the spatial map
					for (Road road : associatedRoads) {

						if(pmQuadtree.get(z).containsRoad(road)){

							//deletes the road from spatial map and from adjacency list
							pmQuadtree.get(z).remove(road);
							roads.removeRoad(road);
							
							final Element roadDeletedNode = results.createElement("roadUnmapped");
							roadDeletedNode.setAttribute("start", road.getStart().getName());
							roadDeletedNode.setAttribute("end", road.getEnd().getName());
							outputNode.appendChild(roadDeletedNode);
						}
					}

					//removes the city from the spatial map
					if(pmQuadtree.containsKey(z) && pmQuadtree.get(z).containsCity(deletedCity.getName())){
						pmQuadtree.get(z).remove(deletedCity);
						
					}

				} catch(IsolatedCityNotMappedThrowable e){
					// won't happen; already checked
				} catch (RoadNotMappedThrowable e) {
					// won't happen; already checked
				}
			}
			
			//removes the city from the dictionary
			citiesByName.remove(name);
			citiesByLocation.remove(deletedCity);

			// add success node to results 
			addSuccessNode(commandNode, parametersNode, outputNode);
		}
	}

	
	
	/**
	 * Clears all the data structures do there are not cities or roads in
	 * existence in the dictionary or on the map.
	 * 
	 * @param node
	 *            clearAll node to be processed
	 */
	public void processClearAll(final Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		/* clear all data structures */
		citiesByName.clear();
		citiesByLocation.clear();
		pmQuadtree.clear();
		roads.clear();

		/* add success node to results */
		addSuccessNode(commandNode, parametersNode, outputNode);
	}

	/**
	 * Lists all the cities, either by name or by location.
	 * 
	 * @param node
	 *            listCities node to be processed
	 */
	public void processListCities(final Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final String sortBy = processStringAttribute(node, "sortBy", parametersNode);

		if (citiesByName.isEmpty()) {
			addErrorNode("noCitiesToList", commandNode, parametersNode);
		} else {
			final Element outputNode = results.createElement("output");
			final Element cityListNode = results.createElement("cityList");
			Collection<City> cityCollection = null;

			if (sortBy.equals("name")) {
				cityCollection = citiesByName.values();
			} else if (sortBy.equals("coordinate")) {
				cityCollection = citiesByLocation;
			} else {
				/* XML validator failed */
				System.exit(-1);
			}

			for (City c : cityCollection) {
				addCityNode(cityListNode, c);
			}
			outputNode.appendChild(cityListNode);

			/* add success node to results */
			addSuccessNode(commandNode, parametersNode, outputNode);
		}
	}

	/**
	 * Creates a city node containing information about a city. Appends the city
	 * node to the passed in node.
	 * 
	 * @param node
	 *            node which the city node will be appended to
	 * @param cityNodeName
	 *            name of city node
	 * @param city
	 *            city which the city node will describe
	 */
	private void addCityNode(final Element node, final String cityNodeName, final City city) {
		final Element cityNode = results.createElement(cityNodeName);
		cityNode.setAttribute("name", city.getName());
		cityNode.setAttribute("x", Integer.toString((int) city.getX()));
		cityNode.setAttribute("y", Integer.toString((int) city.getY()));
		cityNode.setAttribute("z", Integer.toString((int) city.getZ()));
		cityNode.setAttribute("radius", Integer.toString((int) city.getRadius()));
		cityNode.setAttribute("color", city.getColor());
		node.appendChild(cityNode);
	}

	/**
	 * Creates a city node containing information about a city. Appends the city
	 * node to the passed in node.
	 * 
	 * @param node
	 *            node which the city node will be appended to
	 * @param cityNodeName
	 *            name of city node
	 * @param city
	 *            city which the city node will describe
	 */
	private void addRoadNode(final Element node, final String cityNodeName, final Road city) {
		final Element cityNode = results.createElement(cityNodeName);
		cityNode.setAttribute("start", city.getStart().getName());
		cityNode.setAttribute("end", city.getEnd().getName());
		node.appendChild(cityNode);
	}

	/**
	 * Creates a city node containing information about a city. Appends the city
	 * node to the passed in node.
	 * 
	 * @param node
	 *            node which the city node will be appended to
	 * @param city
	 *            city which the city node will describe
	 */
	private void addCityNode(final Element node, final City city) {
		addCityNode(node, "city", city);
	}

	private void addIsolatedCityNode(final Element node, final City city) {
		addCityNode(node, "isolatedCity", city);
	}

	public void processMapRoad(Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		final String start = processStringAttribute(node, "start", parametersNode);
		final String end = processStringAttribute(node, "end", parametersNode);

		City startCity = citiesByName.get(start);
		City endCity = citiesByName.get(end);

		if (!citiesByName.containsKey(start) && startCity == null) {
			addErrorNode("startPointDoesNotExist", commandNode, parametersNode);
		} else if (!citiesByName.containsKey(end) && endCity == null) {
			addErrorNode("endPointDoesNotExist", commandNode, parametersNode);
		} else if (start.equals(end)) {
			addErrorNode("startPointMapsToItself", commandNode, parametersNode);
		} else if(startCity.getZ() != endCity.getZ()){
			addErrorNode("roadNotOnOneLevel", commandNode, parametersNode);
		} else {
			try {

				//if quadtree does not contain z level, puts that z level in
				if(!pmQuadtree.containsKey(startCity.getZ())){
					pmQuadtree.put(startCity.getZ(), new PM3Quadtree(this.spatialHeight, spatialWidth));
				}

				for(Integer z : pmQuadtree.keySet()){
					if(startCity.getZ() == z && endCity.getZ()== z){
						
					
						/* add road to spatial map */
						pmQuadtree.get(z).add(new Road((City) citiesByName.get(start), (City) citiesByName.get(end)));

					}
				}
				/* add road to adjacency list */
				roads.addRoad((City) citiesByName.get(start), (City) citiesByName.get(end));

				/* create roadCreated element */
				final Element roadCreatedNode = results.createElement("roadCreated");
				roadCreatedNode.setAttribute("start", start);
				roadCreatedNode.setAttribute("end", end);
				outputNode.appendChild(roadCreatedNode);

				/* add success node to results */
				addSuccessNode(commandNode, parametersNode, outputNode);
			} catch (RoadAlreadyExistsThrowable e) {
				addErrorNode("roadAlreadyMapped", commandNode, parametersNode);
			} catch (IntersectingRoadsThrowable e) {
				addErrorNode("roadIntersectsAnotherRoad", commandNode, parametersNode);
			} catch (InvalidPartitionThrowable e) {
				addErrorNode("roadViolatesPMRules", commandNode, parametersNode);
			} catch (RoadOutOfSpatialBoundsThrowable e) {
				addErrorNode("roadOutOfSpatialBounds", commandNode, parametersNode);
			} catch (IsolatedCityAlreadyExistsThrowable e) {
				addErrorNode("roadIntersectsPortal", commandNode, parametersNode);
				// will never reach here (this is map road, not isolated city)
			}
		}
	}

	public void processPrintAvlTree(Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		if (citiesByName.isEmpty()) {
			addErrorNode("emptyTree", commandNode, parametersNode);
		} else {

			outputNode.appendChild(citiesByName.createXml(outputNode));
			addSuccessNode(commandNode, parametersNode, outputNode);
		}
	}

	public void processUnmapRoad(Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		final String start = processStringAttribute(node, "start", parametersNode);
		final String end = processStringAttribute(node, "end", parametersNode);

		if (!citiesByName.containsKey(start)) {
			addErrorNode("startPointDoesNotExist", commandNode, parametersNode);
		} else if (!citiesByName.containsKey(end)) {
			addErrorNode("endPointDoesNotExist", commandNode, parametersNode);
		} else if (start.equals(end)) {
			addErrorNode("startPointMapsToItself", commandNode, parametersNode);
		} else {

			try {
				City a = (City) citiesByName.get(start);
				City b = (City) citiesByName.get(end);
				int z = a.getZ();
				Road deletedRoad = new Road(a, b);

				if(a.getZ() != b.getZ()){
					throw new RoadNotMappedThrowable();
				}
				
				//checks for non existant road
				if(!pmQuadtree.containsKey(z)
						|| !pmQuadtree.get(z).containsRoad(deletedRoad)){
					throw new RoadNotMappedThrowable();
				}


				pmQuadtree.get(z).remove(deletedRoad);

				roads.removeRoad(deletedRoad);
				final Element roadDeletedNode = results.createElement("roadDeleted");
				roadDeletedNode.setAttribute("start", start);
				roadDeletedNode.setAttribute("end", end);
				outputNode.appendChild(roadDeletedNode);
				addSuccessNode(commandNode, parametersNode, outputNode);


			} catch (RoadNotMappedThrowable e) {
				addErrorNode("roadNotMapped", commandNode, parametersNode);
			}  	

		}
	}

	public void processUnmapCity(Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		final String name = processStringAttribute(node, "name", parametersNode);

		if (!citiesByName.containsKey(name)) {
			addErrorNode("cityDoesNotExist", commandNode, parametersNode);
		} else {
			try {
				final City c = (City) citiesByName.get(name);

				for(int z : pmQuadtree.keySet()){
					if(pmQuadtree.get(z).containsCity(c.getName())){
						pmQuadtree.get(z).remove(c);
						final Element cityDeletedNode = results.createElement("cityDeleted");
						cityDeletedNode.setAttribute("name", name);
						outputNode.appendChild(cityDeletedNode);
						addSuccessNode(commandNode, parametersNode, outputNode);
					}
				}
			} catch (IsolatedCityNotMappedThrowable e) {
				addErrorNode("isolatedCityNotMapped", commandNode, parametersNode);
			}
		}
	}

	public void processShortestPath(final Element node) throws IOException, ParserConfigurationException,
	TransformerException {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");

		final String start = processStringAttribute(node, "start", parametersNode);
		final String end = processStringAttribute(node, "end", parametersNode);

		String saveMapName = "";
		if (!node.getAttribute("saveMap").equals("")) {
			saveMapName = processStringAttribute(node, "saveMap", parametersNode);
		}

		String saveHTMLName = "";
		if (!node.getAttribute("saveHTML").equals("")) {
			saveHTMLName = processStringAttribute(node, "saveHTML", parametersNode);
		}

		int startZ = -1;
		int endZ = -1;

		//goes through all the possible Quadtrees in the treeMap
		for(Integer z : pmQuadtree.keySet()){
			if(pmQuadtree.get(z).containsCity(start)){
				startZ = z;
			}
			if(pmQuadtree.get(z).containsCity(end)){
				endZ = z;
			}
		}


		if (startZ == -1) {
			addErrorNode("nonExistentStart", commandNode, parametersNode);
		} else if (endZ == -1) {
			addErrorNode("nonExistentEnd", commandNode, parametersNode);
		} else if (!roads.getCitySet().contains(citiesByName.get(start))
				|| !roads.getCitySet().contains(citiesByName.get(end))) {
			// start or end is isolated
			if (start.equals(end)) {
				final Element outputNode = results.createElement("output");
				final Element pathNode = results.createElement("path");
				pathNode.setAttribute("length", "0.000");
				pathNode.setAttribute("hops", "0");

				LinkedList<City> cityList = new LinkedList<City>();
				cityList.add(citiesByName.get(start));
				/* if required, save the map to an image */
				if (!saveMapName.equals("")) {
					saveShortestPathMap(saveMapName, cityList);
				}
				if (!saveHTMLName.equals("")) {
					saveShortestPathMap(saveHTMLName, cityList);
				}

				outputNode.appendChild(pathNode);
				Element successNode = addSuccessNode(commandNode, parametersNode, outputNode);

				if (!saveHTMLName.equals("")) {
					/* save shortest path to HTML */
					Document shortestPathDoc = XmlUtility.getDocumentBuilder().newDocument();
					org.w3c.dom.Node spNode = shortestPathDoc.importNode(successNode, true);
					shortestPathDoc.appendChild(spNode);
					XmlUtility.transform(shortestPathDoc, new File("shortestPath.xsl"),
							new File(saveHTMLName + ".html"));
				}
			} else {
				addErrorNode("noPathExists", commandNode, parametersNode);
			}
		} else {

			final DecimalFormat decimalFormat = new DecimalFormat("#0.000");

			final Dijkstranator dijkstranator = new Dijkstranator(roads);

			final City startCity = (City) citiesByName.get(start);
			final City endCity = (City) citiesByName.get(end);

			if(startCity.getZ() == endCity.getZ()){
				final Path path = dijkstranator.getShortestPath(startCity, endCity);

				if (path == null) {
					addErrorNode("noPathExists", commandNode, parametersNode);
				} else {
					final Element outputNode = results.createElement("output");

					final Element pathNode = results.createElement("path");
					pathNode.setAttribute("length", decimalFormat.format(path.getDistance()));
					pathNode.setAttribute("hops", Integer.toString(path.getHops()));

					final LinkedList<City> cityList = path.getCityList();

					/* if required, save the map to an image */
					if (!saveMapName.equals("")) {
						saveShortestPathMap(saveMapName, cityList);
					}
					if (!saveHTMLName.equals("")) {
						saveShortestPathMap(saveHTMLName, cityList);
					}

					if (cityList.size() > 1) {

						/* add the first road */
						City city1 = cityList.remove();
						City city2 = cityList.remove();
						Element roadNode = results.createElement("road");
						roadNode.setAttribute("start", city1.getName());
						roadNode.setAttribute("end", city2.getName());
						pathNode.appendChild(roadNode);

						while (!cityList.isEmpty()) {
							City city3 = cityList.remove();

							/* process the angle */
							Arc2D.Float arc = new Arc2D.Float();
							arc.setArcByTangent(city1.toPoint2D(), city2.toPoint2D(), city3.toPoint2D(), 1);

							/* print out the direction */
							double angle = arc.getAngleExtent();
							final String direction;
							if (angle < -45) {
								direction = "left";
							} else if (angle >= 45) {
								direction = "right";
							} else {
								direction = "straight";
							}
							Element directionNode = results.createElement(direction);
							pathNode.appendChild(directionNode);

							/* print out the next road */
							roadNode = results.createElement("road");
							roadNode.setAttribute("start", city2.getName());
							roadNode.setAttribute("end", city3.getName());
							pathNode.appendChild(roadNode);

							/* increment city references */
							city1 = city2;
							city2 = city3;
						}
					}
					outputNode.appendChild(pathNode);
					Element successNode = addSuccessNode(commandNode, parametersNode, outputNode);

					if (!saveHTMLName.equals("")) {
						/* save shortest path to HTML */
						Document shortestPathDoc = XmlUtility.getDocumentBuilder().newDocument();
						org.w3c.dom.Node spNode = shortestPathDoc.importNode(successNode, true);
						shortestPathDoc.appendChild(spNode);
						XmlUtility.transform(shortestPathDoc, new File("shortestPath.xsl"),
								new File(saveHTMLName + ".html"));
					}
				}
			}
			else{
				throw new IllegalArgumentException("Cities are on different Z level!");
			}
		}
	}

	private void saveShortestPathMap(final String mapName, final List<City> cityList) throws IOException {
		final CanvasPlus map = new CanvasPlus();
		/* initialize map */
		map.setFrameSize(spatialWidth, spatialHeight);
		/* add a rectangle to show where the bounds of the map are located */
		map.addRectangle(0, 0, spatialWidth, spatialHeight, Color.BLACK, false);

		final Iterator<City> it = cityList.iterator();
		City city1 = it.next();

		/* map green starting point */
		map.addPoint(city1.getName(), city1.getX(), city1.getY(), Color.GREEN);

		if (it.hasNext()) {
			City city2 = it.next();
			/* map blue road */
			map.addLine(city1.getX(), city1.getY(), city2.getX(), city2.getY(), Color.BLUE);

			while (it.hasNext()) {
				/* increment cities */
				city1 = city2;
				city2 = it.next();

				/* map point */
				map.addPoint(city1.getName(), city1.getX(), city1.getY(), Color.BLUE);

				/* map blue road */
				map.addLine(city1.getX(), city1.getY(), city2.getX(), city2.getY(), Color.BLUE);
			}

			/* map red end point */
			map.addPoint(city2.getName(), city2.getX(), city2.getY(), Color.RED);

		}

		/* save map to image file */
		map.save(mapName);

		map.dispose();
	}

	/**
	 * Processes a saveMap command. Saves the graphical map to a given file.
	 * 
	 * @param node
	 *            saveMap command to be processed
	 * @throws IOException
	 *             problem accessing the image file
	 */
	public void processSaveMap(final Element node) throws IOException {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");

		final int z = processIntegerAttribute(node, "z", parametersNode);
		final String name = processStringAttribute(node, "name", parametersNode);

		final Element outputNode = results.createElement("output");

		CanvasPlus canvas = drawPMQuadtree(z);

		/* save canvas to '(name).png' */
		canvas.save(name);

		canvas.dispose();

		/* add success node to results */
		addSuccessNode(commandNode, parametersNode, outputNode);
	}

	private CanvasPlus drawPMQuadtree(int z) {
		final CanvasPlus canvas = new CanvasPlus("MeeshQuest");

		/* initialize canvas */
		canvas.setFrameSize(spatialWidth, spatialHeight);

		/* add a rectangle to show where the bounds of the map are located */
		canvas.addRectangle(0, 0, spatialWidth, spatialHeight, Color.BLACK, false);

		if(pmQuadtree.containsKey(z)){

			/* draw PM Quadtree */
			drawPMQuadtreeHelper(pmQuadtree.get(z).getRoot(), canvas);
		}
		return canvas;
	}

	private void drawPMQuadtreeHelper(Node node, CanvasPlus canvas) {
		/* I realize this is horribly inefficient; feel free to fix it */
		if (node.getType() == Node.BLACK) {
			Black blackNode = (Black) node;
			for (Road road : blackNode.getGeometry()) {
				if (road.isCity()) {
					City city = road.getStart();
					canvas.addPoint(city.getName(), city.getX(), city.getY(), Color.BLACK);
				} else {
					canvas.addLine(road.getStart().getX(), road.getStart().getY(), road.getEnd().getX(), road.getEnd()
							.getY(), Color.BLACK);
				}
			}
		} else if (node.getType() == Node.GRAY) {
			Gray grayNode = (Gray) node;
			canvas.addCross(grayNode.getCenterX(), grayNode.getCenterY(), grayNode.getHalfWidth(), Color.GRAY);
			for (int i = 0; i < 4; i++) {
				drawPMQuadtreeHelper(grayNode.getChild(i), canvas);
			}
		}
	}

	/**
	 * Prints out the structure of the PM Quadtree in an XML format.
	 * 
	 * @param node
	 *            printPMQuadtree command to be processed
	 */

	public void processPrintPMQuadtree(final Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");
		final int z = processIntegerAttribute(node, "z", parametersNode);

		if (!pmQuadtree.containsKey(z) || pmQuadtree.get(z).isEmpty()) {
			/* empty PR Quadtree */
			addErrorNode("mapIsEmpty", commandNode, parametersNode);
		} else {
			/* print PR Quadtree */
			final Element quadtreeNode = results.createElement("quadtree");
			quadtreeNode.setAttribute("order", Integer.toString(pmOrder));
			printPMQuadtreeHelper(pmQuadtree.get(z).getRoot(), quadtreeNode);

			outputNode.appendChild(quadtreeNode);

			/* add success node to results */
			addSuccessNode(commandNode, parametersNode, outputNode);
		}
	}

	/**
	 * Traverses each node of the PM Quadtree.
	 * 
	 * @param currentNode
	 *            PM Quadtree node being printed
	 * @param xmlNode
	 *            XML node representing the current PM Quadtree node
	 */
	private void printPMQuadtreeHelper(final Node currentNode, final Element xmlNode) {
		if (currentNode.getType() == Node.WHITE) {
			Element white = results.createElement("white");
			xmlNode.appendChild(white);
		} else {
			if (currentNode.getType() == Node.BLACK) {
				Black currentLeaf = (Black) currentNode;
				Element blackNode = results.createElement("black");
				blackNode.setAttribute("cardinality", Integer.toString(currentLeaf.getGeometry().size()));
				for (Road g : currentLeaf.getGeometry()) {
					if (g.isCity()) {
						City c = (City) g.getStart();
						Element city = results.createElement(g.isIsolatedCity() ? "portal" : "city");
						//if portal
						if(g.isIsolatedCity()){
							city.setAttribute("name", c.getName());
							city.setAttribute("x", Integer.toString((int) c.getX()));
							city.setAttribute("y", Integer.toString((int) c.getY()));
							city.setAttribute("z", Integer.toString((int) c.getZ()));
						}
						//if regular city
						else{
							city.setAttribute("name", c.getName());
							city.setAttribute("x", Integer.toString((int) c.getX()));
							city.setAttribute("y", Integer.toString((int) c.getY()));
							city.setAttribute("z", Integer.toString((int) c.getZ()));
							city.setAttribute("radius", Integer.toString((int) c.getRadius()));
							city.setAttribute("color", c.getColor());
						}
						blackNode.appendChild(city);
					} else {
						City c1 = (City) g.getStart();
						City c2 = (City) g.getEnd();
						Element road = results.createElement("road");
						road.setAttribute("start", c1.getName());
						road.setAttribute("end", c2.getName());
						blackNode.appendChild(road);
					}
				}

				xmlNode.appendChild(blackNode);

			} else {
				final Gray currentInternal = (Gray) currentNode;
				final Element gray = results.createElement("gray");
				gray.setAttribute("x", Integer.toString((int) currentInternal.getCenterX()));
				gray.setAttribute("y", Integer.toString((int) currentInternal.getCenterY()));
				for (int i = 0; i < 4; i++) {
					printPMQuadtreeHelper(currentInternal.getChild(i), gray);
				}
				xmlNode.appendChild(gray);
			}
		}
	}

	/**
	 * Finds the mapped cities within the range of a given point.
	 * 
	 * @param node
	 *            rangeCities command to be processed
	 * @throws IOException
	 */
	public void processRangeCities(final Element node) throws IOException {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		final TreeSet<City> citiesInRange = new TreeSet<City>(new CityNameComparator());

		/* extract values from command */
		final int x = processIntegerAttribute(node, "x", parametersNode);
		final int y = processIntegerAttribute(node, "y", parametersNode);
		final int z = processIntegerAttribute(node, "z", parametersNode);
		final int radius = processIntegerAttribute(node, "radius", parametersNode);

		String pathFile = "";
		if (!node.getAttribute("saveMap").equals("")) {
			pathFile = processStringAttribute(node, "saveMap", parametersNode);
		}

		if (radius == 0) {
			addErrorNode("noCitiesExistInRange", commandNode, parametersNode);
		} else {
			/* get cities within range */
			final Point2D.Double point = new Point2D.Double(x, y);

			/*gets all cities within the range*/
			int start = z - radius;
			int end = z + radius;
			for(; start<=end; start++){

				if (pmQuadtree.containsKey(start)){
					rangeCitiesHelper(point, Math.sqrt(Math.pow(radius,2) - Math.pow((Math.abs(z - start)),2)), 
							pmQuadtree.get(start).getRoot(), citiesInRange);
				}

			}

			/* print out cities within range */
			if (citiesInRange.isEmpty()) {
				addErrorNode("noCitiesExistInRange", commandNode, parametersNode);
			} else {
				/* get city list */
				final Element cityListNode = results.createElement("cityList");
				for (City city : citiesInRange) {
					addCityNode(cityListNode, city);
				}
				outputNode.appendChild(cityListNode);

				/* add success node to results */
				addSuccessNode(commandNode, parametersNode, outputNode);

				if (pathFile.compareTo("") != 0) {
					/* save canvas to file with range circle */
					CanvasPlus canvas = drawPMQuadtree(z);
					canvas.addCircle(x, y, radius, Color.BLUE, false);
					canvas.save(pathFile);
					canvas.dispose();
				}
			}
		}
	}

	/**
	 * Determines if any cities within the PM Quadtree not are within the radius
	 * of a given point.
	 * 
	 * @param point
	 *            point from which the cities are measured
	 * @param radius
	 *            radius from which the given points are measured
	 * @param node
	 *            PM Quadtree node being examined
	 * @param citiesInRange
	 *            a list of cities found to be in range
	 */
	private void rangeCitiesHelper(final Point2D.Double point, final double radius, final Node node,
			final TreeSet<City> citiesInRange) {
		if (node.getType() == Node.BLACK) {
			final Black leaf = (Black) node;
			if (leaf.containsCity()) {
				final double distance = point.distance(leaf.getCity().toPoint2D());
				if (distance <= radius && !leaf.getCity().isPortal()) {
					/* city is in range */
					final City city = leaf.getCity();
					citiesInRange.add(city);
				}
			}
		} else if (node.getType() == Node.GRAY) {
			/* check each quadrant of internal node */
			final Gray internal = (Gray) node;

			final Circle2D.Double circle = new Circle2D.Double(point, radius);
			for (int i = 0; i < 4; i++) {
				if (Inclusive2DIntersectionVerifier.intersects(internal.getChildRegion(i), circle)) {
					rangeCitiesHelper(point, radius, internal.getChild(i), citiesInRange);
				}
			}
		}
	}

	/**
	 * Finds the mapped roads within the range of a given point.
	 * 
	 * @param node
	 *            rangeRoads command to be processed
	 * @throws IOException
	 */
	public void processRangeRoads(final Element node) throws IOException {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");
		final TreeSet<Road> roadsInRange = new TreeSet<Road>(new RoadNameComparator());

		/* extract values from command */
		final int x = processIntegerAttribute(node, "x", parametersNode);
		final int y = processIntegerAttribute(node, "y", parametersNode);
		final int z = processIntegerAttribute(node, "z", parametersNode);
		final int radius = processIntegerAttribute(node, "radius", parametersNode);

		String pathFile = "";
		if (!node.getAttribute("saveMap").equals("")) {
			pathFile = processStringAttribute(node, "saveMap", parametersNode);
		}

		if (radius == 0) {
			addErrorNode("noRoadsExistInRange", commandNode, parametersNode);
		} else {
			/* get roads within range */
			final Point2D.Double point = new Point2D.Double(x, y);

			/*gets all cities within the range*/
			int start = z - radius;
			int end = z + radius;
			for(; start<=end; start++){
//				if(start == z && pmQuadtree.containsKey(start)){
//					rangeRoadsHelper(point, radius, pmQuadtree.get(start).getRoot(), roadsInRange);	
//				}
				if (pmQuadtree.containsKey(start)){
					rangeRoadsHelper(point, Math.sqrt(Math.pow(radius,2) - 
							Math.pow(Math.abs(z - start),2)), pmQuadtree.get(start).getRoot(), roadsInRange);
				}
			}

			/* print out roads within range */
			if (roadsInRange.isEmpty()) {
				addErrorNode("noRoadsExistInRange", commandNode, parametersNode);
			} else {
				/* get road list */
				final Element roadListNode = results.createElement("roadList");
				for (final Road road : roadsInRange) {
					addRoadNode(roadListNode, "road", road);
				}
				outputNode.appendChild(roadListNode);

				/* add success node to results */
				addSuccessNode(commandNode, parametersNode, outputNode);

				if (pathFile.compareTo("") != 0) {
					/* save canvas to file with range circle */
					CanvasPlus canvas = drawPMQuadtree(z);
					canvas.addCircle(x, y, radius, Color.BLUE, false);
					canvas.save(pathFile);
					canvas.dispose();
				}
			}
		}
	}

	/**
	 * Determines if any roads within the PM Quadtree are within the radius
	 * of a given point.
	 * 
	 * @param point
	 *            point from which the roads are measured
	 * @param radius
	 *            radius from which the given points are measured
	 * @param node
	 *            PM Quadtree node being examined
	 * @param roadsInRange
	 *            a list of roads found to be in range
	 */
	private void rangeRoadsHelper(final Point2D.Double point, final double radius,
			final Node node, final TreeSet<Road> roadsInRange) {
		if (node.getType() == Node.BLACK) {
			final Black leaf = (Black) node;
			for (final Road road : leaf.getGeometry()) {
				/* only care about roads! */
				if ((road.isIsolatedCity() ||
						(!road.isIsolatedCity() && !road.isCity()))
						&& road.toLine2D().ptSegDist(point) <= radius) {
					roadsInRange.add(road);
				}
			}
		} else if (node.getType() == Node.GRAY) {
			/* check each quadrant of internal node */
			final Gray internal = (Gray) node;
			final Circle2D.Double circle = new Circle2D.Double(point, radius);

			for (int i = 0; i < 4; i++) {
				if (Inclusive2DIntersectionVerifier.intersects(internal.getChildRegion(i), circle)) {
					rangeRoadsHelper(point, radius, internal.getChild(i), roadsInRange);
				}
			}
		}
	}

	/**
	 * Finds the nearest city to a given point.
	 * 
	 * @param node
	 *            nearestCity command being processed
	 */
	public void processNearestCity(Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		/* extract attribute values from command */
		final int x = processIntegerAttribute(node, "x", parametersNode);
		final int y = processIntegerAttribute(node, "y", parametersNode);
		final int z = processIntegerAttribute(node, "z", parametersNode);

		final Point2D.Float point = new Point2D.Float(x, y);

		if (pmQuadtree.isEmpty() || pmQuadtree.get(z) == null 
				|| pmQuadtree.get(z).getNumCities() - pmQuadtree.get(z).getNumIsolatedCities() == 0) {
			addErrorNode("cityNotFound", commandNode, parametersNode);
		} else {
			addCityNode(outputNode, nearestCityHelper(point, z, false));
			addSuccessNode(commandNode, parametersNode, outputNode);
		}
	}

	public void processNearestIsolatedCity(Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		/* extract attribute values from command */
		final int x = processIntegerAttribute(node, "x", parametersNode);
		final int y = processIntegerAttribute(node, "y", parametersNode);
		final int z = processIntegerAttribute(node, "z", parametersNode);
		final Point2D.Float point = new Point2D.Float(x, y);

		if (pmQuadtree.get(z).getNumIsolatedCities() == 0) {
			addErrorNode("cityNotFound", commandNode, parametersNode);
		} else {
			addIsolatedCityNode(outputNode, nearestCityHelper(point,z, true));
			addSuccessNode(commandNode, parametersNode, outputNode);
		}
	}

	/**
	 * 3/3/10
	 */
	private City nearestCityHelper(Point2D.Float point, int z, boolean isNearestIsolatedCity) {
		Node n = pmQuadtree.get(z).getRoot();
		PriorityQueue<NearestRegion> nearCities = new PriorityQueue<NearestRegion>();

		if (n.getType() == Node.BLACK) {
			Black b = (Black) n;
			if (b.getCity() != null && b.isIsolated() == isNearestIsolatedCity) {
				return b.getCity();
			}
		}

		while (n.getType() == Node.GRAY) {
			Gray g = (Gray) n;
			Node kid;
			double dist = Double.MAX_VALUE;

			for (int i = 0; i < 4; i++) {
				kid = g.getChild(i);
				if (kid.getType() == Node.WHITE) {
					continue;
				}
				if (kid.getType() == Node.BLACK
						&& (((Black) kid).getCity() == null || ((Black) kid).isIsolated() != isNearestIsolatedCity)) {
					continue;
				}
				if (kid.getType() == Node.GRAY) {
					dist = Shape2DDistanceCalculator.distance(point, g.getChildRegion(i));
				} else if (kid.getType() == Node.BLACK) {
					dist = point.distance(((Black) kid).getCity().toPoint2D());
				}
				nearCities.add(new NearestRegion(kid, dist));
			}
			try {
				n = nearCities.remove().node;
			} catch (Exception ex) {
				System.err.println(nearCities.size());
				throw new IllegalStateException();
			}
		}
		return ((Black) n).getCity();
	}

	public void processListBorderCities(final Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final int z = processIntegerAttribute(node, "z", parametersNode);
		final String sortBy = processStringAttribute(node, "sortBy", parametersNode);


		if (pmQuadtree.isEmpty() || pmQuadtree.get(z) == null 
				|| pmQuadtree.get(z).isEmpty()) {
			addErrorNode("noCitiesToList", commandNode, parametersNode);
		} else {
			final Element outputNode = results.createElement("output");
			final Element cityListNode = results.createElement("cityList");

			//set of border cities that helper returns
			Set<City> borderCities = listBorderCitiesHelper(sortBy, true, z);
			borderCities.addAll(listBorderCitiesHelper(sortBy, false, z));

			if(borderCities.isEmpty()){
				addErrorNode("noCitiesToList", commandNode, parametersNode);
			}
			else{
				for (City city : borderCities) {
					addCityNode(cityListNode, city);
				}
				outputNode.appendChild(cityListNode);
				addSuccessNode(commandNode, parametersNode, outputNode);
			}
		}
	}

	private Set<City> listBorderCitiesHelper(final String sortBy, final boolean findMaxima, int z) {
		List<City> current = new ArrayList<City>();
		Set<City> sorted = new TreeSet<City>(sortBy.equals("name") ? new CityNameComparator()
		: new CityLocationComparator());
		Set<City> cities = new TreeSet<City>(findMaxima ? new CityLocationComparator() : new Comparator<City>() {
			private Comparator<City> increasing = new CityLocationComparator();

			public int compare(City c1, City c2) {
				return -increasing.compare(c1, c2);
			}
		});


		cities.addAll(roads.getCitySet(z));

		for (City city : cities) {
			int i = current.size() - 1;
			int y = city.getY();

			while (!current.isEmpty() && i >= 0) {
				int currentY = current.get(i).getY();

				if ((findMaxima && currentY <= y) || (!findMaxima && currentY >= y)) {
					current.remove(i);
					i--;
				} else {
					break;
				}
			}
			current.add(city);
		}
		sorted.addAll(current);

		return sorted;
	}

	//command to map a portal
	public void processMapPortal(final Element node){
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		/* extract attribute values from command */
		final String name = processStringAttribute(node, "name", parametersNode);
		final int x = processIntegerAttribute(node, "x", parametersNode);
		final int y = processIntegerAttribute(node, "y", parametersNode);
		final int z = processIntegerAttribute(node, "z", parametersNode);

		//creates a portal
		City portal = new City(name, x, y, z, -1, null);


		if(portalDictionary.containsKey(name)){
			addErrorNode("duplicatePortalName", commandNode, parametersNode);
			//checks for a portal with the coordinate or name
		}else if(pmQuadtree.containsKey(z) && pmQuadtree.get(z).containsPortalByName(name)){
			addErrorNode("duplicatePortalName", commandNode, parametersNode);
		}else if(pmQuadtree.containsKey(z) && pmQuadtree.get(z).containsCity(name)){
			addErrorNode("duplicatePortalName", commandNode, parametersNode);
		}else if(pmQuadtree.containsKey(z) && pmQuadtree.get(z).containsPortalByCoordinate(x, y)){
			addErrorNode("duplicatePortalCoordinates", commandNode, parametersNode);
			//checks if the portal has the same name as a city
		} 
		else{
			//adds portal to our portalDictionary
			portalDictionary.put(name, portal);

			//adds the portal to spatial map
			if(!pmQuadtree.containsKey(z)){
				pmQuadtree.put(z, new PM3Quadtree(spatialWidth, spatialHeight));
			}
			try {
				pmQuadtree.get(z).addPortal(portal);
			} catch (InvalidPartitionThrowable e) {
				addErrorNode("portalViolatesPMRules", commandNode, parametersNode);
			} catch (RoadAlreadyExistsThrowable e) {
				// TODO Auto-generated catch block
				//should not get here!!
				e.printStackTrace();
			} catch (IsolatedCityAlreadyExistsThrowable e) {
				// TODO Auto-generated catch block
				//should not get here!!
				e.printStackTrace();
			} catch (IsolatedCityOutOfSpatialBoundsThrowable e) {
				addErrorNode("portalViolatesPMRules", commandNode, parametersNode);
			} catch (IntersectingRoadsThrowable e) {
				// TODO Auto-generated catch block
				//should not get here!!
				e.printStackTrace();
			}
			addSuccessNode(commandNode, parametersNode, outputNode);
		}

	}
	public void processUnmapPortal(final Element node){
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		/* extract attribute values from command */
		final String name = processStringAttribute(node, "name", parametersNode);
		boolean success = false;
		try {
			for(Integer z : pmQuadtree.keySet()){
				if(pmQuadtree.get(z).containsPortalByName(name)){
					pmQuadtree.get(z).removePortal(name);
					//removes portal from our portal dictionary
					portalDictionary.remove(name);
					success = true;
				}
			}
		} catch (IsolatedCityNotMappedThrowable e) {
			//should not get here
			addErrorNode("portalDoesNotExist", commandNode, parametersNode);
		}
		if(!success){
			addErrorNode("portalDoesNotExist", commandNode, parametersNode);
		}else{
			addSuccessNode(commandNode, parametersNode, outputNode);
		}

	}
	public void processNearestPortal(){


	}

	private class NearestRegion implements Comparable<NearestRegion> {

		private Node node;
		private double distance;

		public NearestRegion(Node node, double distance) {
			this.node = node;
			this.distance = distance;
		}

		public int compareTo(NearestRegion o) {
			if (distance == o.distance) {
				if (node.getType() == Node.BLACK && o.node.getType() == Node.BLACK) {
					Black b1 = (Black) node;
					Black b2 = (Black) o.node;
					return b1.getCity().getName().compareTo(b2.getCity().getName());
				} else if (node.getType() == Node.BLACK && o.node.getType() == Node.GRAY) {
					return 1;
				} else if (node.getType() == Node.GRAY && o.node.getType() == Node.BLACK) {
					return -1;
				} else {
					Gray g1 = (Gray) node;
					Gray g2 = (Gray) o.node;
					return g2.hashCode() - g1.hashCode();
				}
			}
			return (distance < o.distance) ? -1 : 1;
		}

	}

}

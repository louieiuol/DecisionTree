/*
 * @(#)Road.java        1.0 2007/01/23
 *
 * Copyright Ben Zoller (University of Maryland, College Park), 2007
 * All rights reserved. Permission is granted for use and modification in CMSC420 
 * at the University of Maryland.
 */
package cmsc420.road;

import java.awt.geom.Line2D;

import cmsc420.city.City;
import cmsc420.geom.Inclusive2DIntersectionVerifier;

/**
 * Road class provides an analogue to real-life roads on a map. A Road connects
 * one {@link cmsc420.city.City} to another city. The distance between the two
 * cities is calculated when the road is constructed to save time in distance
 * calculations. Note: roads are not interchangeable. That is, Road (A,B) is not
 * the same as Road (B,A).
 * 
 * @author Ben Zoller
 * @version 1.0, 23 Jan 2007
 */
public class Road {
	/** start city */
	protected City start;

	/** end city */
	protected City end;

	/** distance from start city to end city */
	protected double distance;
	
	protected boolean isIsolatedCity;

	/**
	 * Constructs a new road based on start city and end city. Calculates and
	 * stores the distance between them.
	 * 
	 * @param start
	 *            start city
	 * @param end
	 *            end city
	 */
	public Road(final City start, final City end) {
		if (end.getName().compareTo(start.getName()) < 0) {
			this.start = end;
			this.end = start;
		} else {
			this.start = start;
			this.end = end;
		}
		distance = start.toPoint2D().distance(end.toPoint2D());
		isIsolatedCity = false;
	}

	public Road(final City isolatedCity) {
		this(isolatedCity, isolatedCity);
		isIsolatedCity = true;
	}
	/**
	 * Gets the start city.
	 * 
	 * @return start city
	 */
	public City getStart() {
		return start;
	}

	/**
	 * Gets the end city.
	 * 
	 * @return end city
	 */
	public City getEnd() {
		return end;
	}

	/**
	 * Gets the distance between the start and end cities.
	 * 
	 * @return distance between the two cities
	 */
	public double getDistance() {
		return distance;
	}

	/**
	 * Returns a string representing a road. For example, a road from city A to
	 * city B will print out as: (A,B).
	 * 
	 * @return road string
	 */
	public String getCityNameString() {
		return "(" + start.getName() + "," + end.getName() + ")" + (isIsolatedCity? "-Isolated" : "");
	}

	/**
	 * If the name of the start city is passed in, returns the name of the end
	 * city. If the name of the end city is passed in, returns the name of the
	 * start city. Else throws an <code>IllegalArgumentException</code>.
	 * 
	 * @param cityName
	 *            name of city contained by the road
	 * @return name of the other city contained by the road
	 * @throws IllegalArgumentException
	 *             city name passed in was not contained by the road
	 */
	public City getOtherCity(final String cityName) {
		if (start.getName().equals(cityName)) {
			return end;
		} else if (end.getName().equals(cityName)) {
			return start;
		} else {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Returns a line segment representation of the road which extends
	 * Line2D.Double.
	 * 
	 * @return line segment representation of road
	 */
	public Line2D toLine2D() {
		return new Line2D.Float(start.toPoint2D(), end.toPoint2D());
	}

	/**
	 * Determines if one road is equal to another.
	 * 
	 * @param other
	 *            the other road
	 * @return <code>true</code> if the roads are equal, false otherwise
	 */
	public boolean equals(Object obj) {
		if (obj != null && (obj.getClass().equals(this.getClass()))) {
			Road r = (Road) obj;
			return (start.equals(r.start) && end.equals(r.end) && distance == r.distance);
		}
		return false;
	}

	/**
	 * Returns the hash code value of a road.
	 */
	public int hashCode() {
		final long dBits = Double.doubleToLongBits(distance);
		int hash = 35;
		hash = 37 * hash + start.hashCode();
		hash = 37 * hash + end.hashCode();
		hash = 37 * hash + (int) (dBits ^ (dBits >>> 32));
		hash = 2 * hash + (isIsolatedCity? 1 : 0); 
		return hash;
	}

	public boolean isCity() {
		return start.equals(end);
	}

	public boolean isIsolatedCity() {
		return isIsolatedCity;
	}

	public boolean contains(City city) {
		return (city.equals(start) || city.equals(end));
	}

	public String toString() {
		return getCityNameString();
	}

	/**
	 * If two roads share a city, then the shared city is returned. Else null is
	 * returned.
	 * 
	 * @param r1
	 * @param r2
	 * @return shared city is there is one, else null
	 */
	public static City getSharedCity(final Road r1, final Road r2) {
		if (r1.start.equals(r2.start) || r1.start.equals(r2.end)) {
			return r1.start;
		} else if (r1.end.equals(r2.start) || r1.end.equals(r2.end)) {
			return r1.end;
		}
		return null;
	}

	/**
	 * Returns if two roads intersect. Roads (A,B) and (C,D) intersect if the
	 * line segments intersect and either: 1. If (A,B) and (C,D) share an
	 * endpoint, say A=C, then B intersects with (C,D) or D intersects with
	 * (A,B). 2. Else true.
	 * 
	 * @param r1
	 * @param r2
	 * @return if two roads intersect
	 */
	public static boolean intersects(final Road r1, final Road r2) {
		if (r1.toLine2D().intersectsLine(r2.toLine2D())) {
			final City sharedCity = getSharedCity(r1, r2);
			if (sharedCity == null) {
				/* do not share an endpoint */
				return true;
			} else {
				/* share an endpoint */
				final City r1OtherCity = r1.getOtherCity(sharedCity.getName());
				final City r2OtherCity = r2.getOtherCity(sharedCity.getName());
				return Inclusive2DIntersectionVerifier.intersects(r1OtherCity
						.toPoint2D(), r2.toLine2D())
						|| Inclusive2DIntersectionVerifier.intersects(
								r2OtherCity.toPoint2D(), r1.toLine2D());
			}
		}
		return false;
	}
}

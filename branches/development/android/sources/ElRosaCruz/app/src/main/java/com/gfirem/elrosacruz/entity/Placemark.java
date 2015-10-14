package com.gfirem.elrosacruz.entity;

import com.google.android.gms.maps.model.LatLng;

public class Placemark implements Comparable<Placemark> {
	String title;
	public String id;
	String description;
	LatLng coordinates;
	String address;
	double distance;
	String country;
	boolean isCountry;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public LatLng getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(String coordinates) {
		String[] items = coordinates.split("\\s*,\\s*");
		LatLng placePosition = new LatLng(Float.parseFloat(items[1]), Float.parseFloat(items[0]));
		this.coordinates = placePosition;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}
	
	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	@Override
	public int compareTo(Placemark another) {
		double compareDistance = ((Placemark) another).getDistance();
		return (int) (this.distance - compareDistance);
	}

	public boolean isCountry() {
		return isCountry;
	}

	public void setCountry(boolean isCountry) {
		this.isCountry = isCountry;
	}
	
	
}

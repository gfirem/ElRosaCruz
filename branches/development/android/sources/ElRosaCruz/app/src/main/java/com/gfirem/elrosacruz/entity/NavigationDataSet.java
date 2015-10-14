package com.gfirem.elrosacruz.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import com.gfirem.elrosacruz.utils.MapHelper;
import com.google.android.gms.maps.model.LatLng;

public class NavigationDataSet {
	private ArrayList<Placemark> placemarks = new ArrayList<Placemark>();
	private Placemark currentPlacemark;
	public Placemark myPlaceMarks;

	public String toString() {
		String s = "";
		for (Iterator<Placemark> iter = placemarks.iterator(); iter.hasNext();) {
			Placemark p = (Placemark) iter.next();
			s += p.getTitle() + "\n" + p.getDescription() + "\n\n";
		}
		return s;
	}

	public void addCurrentPlacemark() {
		placemarks.add(currentPlacemark);
	}

	public ArrayList<Placemark> getPlacemarks() {
		return placemarks;
	}

	public void setPlacemarks(ArrayList<Placemark> placemarks) {
		this.placemarks = placemarks;
	}

	public Placemark getCurrentPlacemark() {
		return currentPlacemark;
	}

	public void setCurrentPlacemark(Placemark currentPlacemark) {
		this.currentPlacemark = currentPlacemark;
	}

	public Placemark findById(String anId) {
		Placemark result = null;
		for (Iterator<Placemark> iter = placemarks.iterator(); iter.hasNext();) {
			Placemark p = (Placemark) iter.next();
			if (p.id.equalsIgnoreCase(anId)) {
				result = p;
				break;
			}
		}
		return result;
	}

	public Placemark findByTitle(String Title) {
		Placemark result = null;
		for (Iterator<Placemark> iter = placemarks.iterator(); iter.hasNext();) {
			Placemark p = (Placemark) iter.next();
			if (p.title.equalsIgnoreCase(Title)) {
				result = p;
				break;
			}
		}
		return result;
	}

	public ArrayList<Placemark> findByCountry(String ContryName) {
		ArrayList<Placemark> result = new ArrayList<Placemark>();
		for (Iterator<Placemark> iter = placemarks.iterator(); iter.hasNext();) {
			Placemark p = (Placemark) iter.next();
			if (p.country.equalsIgnoreCase(ContryName)) {
				result.add(p);
			}
		}
		return result;
	}

	

	public void orderByDistances(LatLng currentPosition) {
		if (placemarks.size() > 0) {
			for (Iterator<Placemark> iter = placemarks.iterator(); iter.hasNext();) {
				Placemark p = (Placemark) iter.next();
				p.setDistance(MapHelper.getDistanceBetweenLatLongs_Kilometers(currentPosition, p.getCoordinates()));
			}
			Collections.sort(placemarks, new Comparator<Placemark>() {
				@Override
				public int compare(Placemark lhs, Placemark rhs) {
					Double dist1 = ((Placemark) lhs).getDistance();
					Double dist2 = ((Placemark) rhs).getDistance();
					return dist1.compareTo(dist2);
				}
			});
		}
	}

	public String[] getTemplesName() {
		String[] resutl = new String[placemarks.size()];
		int i = 0;
		if (placemarks.size() > 0) {
			for (Iterator<Placemark> iter = placemarks.iterator(); iter.hasNext();) {
				Placemark p = (Placemark) iter.next();
				resutl[i] = p.title;
				i++;
			}
		}
		return resutl;
	}

}

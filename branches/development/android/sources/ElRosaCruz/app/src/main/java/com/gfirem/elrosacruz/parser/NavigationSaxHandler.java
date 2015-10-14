package com.gfirem.elrosacruz.parser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.gfirem.elrosacruz.entity.NavigationDataSet;
import com.gfirem.elrosacruz.entity.Placemark;

public class NavigationSaxHandler extends DefaultHandler {

	// ===========================================================
	// Fields
	// ===========================================================

	private boolean in_kmltag = false;
	private boolean in_placemarktag = false;
	private boolean in_nametag = false;
	private boolean in_descriptiontag = false;
	private boolean in_geometrycollectiontag = false;
	private boolean in_linestringtag = false;
	private boolean in_pointtag = false;
	private boolean in_coordinatestag = false;
	private boolean in_conutry = false;
	private boolean is_conutry = false;
	private String last_country = "";

	private StringBuffer buffer;

	private NavigationDataSet navigationDataSet = new NavigationDataSet();

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public NavigationDataSet getParsedData() {
		// navigationDataSet.getCurrentPlacemark().setCoordinates(buffer.toString().trim());
		return this.navigationDataSet;
	}

	// ===========================================================
	// Methods
	// ===========================================================
	@Override
	public void startDocument() throws SAXException {
		this.navigationDataSet = new NavigationDataSet();
	}

	@Override
	public void endDocument() throws SAXException {
		// Nothing to do
	}

	/**
	 * Gets be called on opening tags like: <tag> Can provide attribute(s), when
	 * xml was like: <tag attribute="attributeValue">
	 */
	@Override
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
		if (localName.equalsIgnoreCase("kml")) {
			this.in_kmltag = true;
		} else if (localName.equalsIgnoreCase("Placemark")) {
			this.in_placemarktag = true;
			navigationDataSet.setCurrentPlacemark(new Placemark());
		} else if (localName.equalsIgnoreCase("name")) {
			this.in_nametag = true;
		} else if (localName.equalsIgnoreCase("description")) {
			this.in_descriptiontag = true;
		} else if (localName.equalsIgnoreCase("point")) {
			this.in_pointtag = true;
		} else if (localName.equalsIgnoreCase("country")) {
			this.in_conutry = true;
			this.is_conutry = true;
		} else if (localName.equalsIgnoreCase("coordinates")) {
			buffer = new StringBuffer();
			this.in_coordinatestag = true;
		}
	}

	/**
	 * Gets be called on closing tags like: </tag>
	 */
	@Override
	public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
		if (localName.equalsIgnoreCase("kml")) {
			this.in_kmltag = false;
		} else if (localName.equalsIgnoreCase("Placemark")) {
			this.in_placemarktag = false;
		} else if (localName.equalsIgnoreCase("name")) {
			this.in_nametag = false;
		} else if (localName.equalsIgnoreCase("description")) {
			this.in_descriptiontag = false;
		} else if (localName.equalsIgnoreCase("point")) {
			this.in_pointtag = false;
		} else if (localName.equalsIgnoreCase("country")) {
			this.in_conutry = false;
		} else if (localName.equalsIgnoreCase("coordinates")) {
			this.in_coordinatestag = false;
		}
	}

	/**
	 * Gets be called on the following structure: <tag>characters</tag>
	 */
	@Override
	public void characters(char ch[], int start, int length) {
		if (this.in_nametag) {

			if (navigationDataSet.getCurrentPlacemark() == null)
				navigationDataSet.setCurrentPlacemark(new Placemark());

			navigationDataSet.getCurrentPlacemark().setTitle(new String(ch, start, length));

		} else if (this.in_descriptiontag) {

			if (navigationDataSet.getCurrentPlacemark() == null)
				navigationDataSet.setCurrentPlacemark(new Placemark());

			navigationDataSet.getCurrentPlacemark().setDescription(new String(ch, start, length));
		} else if (this.in_conutry) {

			if (navigationDataSet.getCurrentPlacemark() == null)
				navigationDataSet.setCurrentPlacemark(new Placemark());

			String temp_country = new String(ch, start, length);
			navigationDataSet.getCurrentPlacemark().setCountry(true);
			if (temp_country.compareTo(last_country) != 0)
				last_country = temp_country;

		} else if (this.in_coordinatestag) {

			if (navigationDataSet.getCurrentPlacemark() == null)
				navigationDataSet.setCurrentPlacemark(new Placemark());

			navigationDataSet.getCurrentPlacemark().setCoordinates(new String(ch, start, length));
			navigationDataSet.getCurrentPlacemark().setCountry(last_country);

			navigationDataSet.addCurrentPlacemark();

			buffer.append(ch, start, length);
		}
	}
}

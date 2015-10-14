package com.gfirem.elrosacruz;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.gfirem.elrosacruz.entity.NavigationDataSet;
import com.gfirem.elrosacruz.entity.Placemark;
import com.gfirem.elrosacruz.parser.NavigationSaxHandler;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import android.content.Context;
import android.util.Log;

public class MapServices {
	private static final String TAG = "MapServices";

	public static String inputStreamToString(InputStream in) throws IOException {
		StringBuffer out = new StringBuffer();
		byte[] b = new byte[4096];
		for (int n; (n = in.read(b)) != -1;) {
			out.append(new String(b, 0, n));
		}
		return out.toString();
	}

	/**
	 * Retrieve navigation data set from either remote URL or String
	 *
	 * @return navigation set
	 */
	public static NavigationDataSet getNavigationDataSet(Context aContext) {
		NavigationDataSet navigationDataSet = null;
		try {

			/* Get a SAXParser from the SAXPArserFactory. */
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();

			/* Get the XMLReader of the SAXParser we created. */
			XMLReader xr = sp.getXMLReader();

			/* Create a new ContentHandler and apply it to the XML-Reader */
			NavigationSaxHandler navSax2Handler = new NavigationSaxHandler();
			xr.setContentHandler(navSax2Handler);

			/* Parse the xml-data from our URL. */
			xr.parse(new InputSource(aContext.getAssets().open("map.xml")));

			/* Our NavigationSaxHandler now provides the parsed data to us. */
			navigationDataSet = navSax2Handler.getParsedData();

			/* Set the result to be displayed in our GUI. */
			Log.d(TAG, "navigationDataSet: " + navigationDataSet.toString());

		} catch (Exception e) {
			// Log.e(TAG, "error with kml xml", e);
			navigationDataSet = null;
		}

		return navigationDataSet;
	}
}

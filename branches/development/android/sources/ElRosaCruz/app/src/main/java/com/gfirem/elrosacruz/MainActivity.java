package com.gfirem.elrosacruz;

import android.app.Dialog;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.gfirem.elrosacruz.entity.NavigationDataSet;
import com.gfirem.elrosacruz.entity.Placemark;
import com.gfirem.elrosacruz.utils.MapHelper;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MainActivity";
    private GoogleMap mMap;
    private Tracker mTracker;
    private View spinnerCenterLoading;
    private NavigationDataSet markerDataSet;
    private LatLng my_location;
    private MapHelper mapHelper;
    private ArrayList<Placemark> places;
    private AutoCompleteTextView txtFind;
    private ImageButton btnClear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinnerCenterLoading = findViewById(R.id.progress);
        txtFind = (AutoCompleteTextView) findViewById(R.id.txtFind);
        txtFind.setText("");
        txtFind.setHint("Encuentra tu templo");
        txtFind.setCursorVisible(false);
        btnClear = (ImageButton) findViewById(R.id.btnClear);

        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());

        // Showing status
        if (status != ConnectionResult.SUCCESS) {
            // Google Play Services are not available
            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
            dialog.show();
            System.runFinalization();
            System.exit(2);
        }

        markerDataSet = MapServices.getNavigationDataSet(this);

        Location location = getMyLocation();
        if (location != null) {
            my_location = new LatLng(location.getLatitude(), location.getLongitude());
            markerDataSet.orderByDistances(my_location);
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnClear.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                txtFind.setText("");
                txtFind.setHint("Encuentra tu templo");
                if (mMap != null && places != null && mapHelper != null) {
                    places = markerDataSet.getPlacemarks();
                    addMarkers(places);
                    txtFind.setCursorVisible(false);
                }
            }
        });

        final String[] templesName = markerDataSet.getTemplesName();
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, templesName);
        txtFind.setThreshold(3);
        txtFind.setAdapter(adapter);

        txtFind.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (txtFind.getText().length() > 0) {
                    btnClear.setVisibility(View.VISIBLE);
                    btnClear.bringToFront();
                } else {
                    btnClear.setVisibility(View.GONE);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

//        txtFind.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (hasFocus) {
//                    txtFind.setHint("");
//                    txtFind.setCursorVisible(true);
//                } else {
//                    txtFind.setHint("Encuentra tu templo");
//                }
//            }
//        });

        txtFind.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mMap != null && places != null && mapHelper != null) {
                    String templeName = adapter.getItem(position);
                    Placemark target = markerDataSet.findByTitle(templeName);
                    if (!target.isCountry())
                        moveToLocation(mMap, target.getCoordinates());
                    else {
                        ArrayList<Placemark> temples = markerDataSet.findByCountry(target.getCountry());
                        ArrayList<LatLng> temples_coordinate = mapHelper.extractLocations(temples);
                        mapHelper.zoomToFitLatLongs(temples_coordinate);
                        mMap.clear();
                        addMarkers(temples);
                    }
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(txtFind.getWindowToken(), 0);
                }
            }
        });

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        places = markerDataSet.getPlacemarks();
        mapHelper = new MapHelper(googleMap);

        markerDataSet.setPlacemarks(places);
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                // setCurrentLocation(map);
                if (my_location != null) {
                    LatLng near_places = places.get(0).getCoordinates();
                    mapHelper.zoomToFitLatLongs(my_location, near_places);
                    addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.my_position))
                            .position(my_location)
                            .snippet(
                                    "El templo te queda a " + Math.ceil(places.get(0).getDistance()) + "KM")
                            .title("Mi posición")).showInfoWindow();
                }
                addMarkers(places);
            }
        });

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        // InfoWindowMap infoWindow = new
        // InfoWindowMap(getActivity());
        // map.setInfoWindowAdapter(infoWindow);
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

            @Override
            public void onInfoWindowClick(Marker marker) {

                Placemark current = markerDataSet.findById(marker.getId());
                if (current != null) {
                    Toast.makeText(MainActivity.this, "Nombre " + current.getTitle(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(base));
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Obtain the shared Tracker instance.
        ApplicationController application = (ApplicationController) getApplication();
        mTracker = application.getDefaultTracker();

        mTracker.setScreenName("MainActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }


    /**
     * Show the waiting spinnerCenterLoading in the middle of the screen <br/>
     * There is also the spinnerLoading on the bottom of the list for pagination
     * <br/>
     * There is also the spinnerLoading on the actionBar, used for details<br/>
     */
    public void showCenterWaiting() {
        if (spinnerCenterLoading != null) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Show center loading");
            }
            spinnerCenterLoading.setVisibility(View.VISIBLE);
            spinnerCenterLoading.bringToFront();
        }
    }

    /**
     * Hide the waiting spinnerCenterLoading in the middle of the screen <br/>
     * There is also the spinnerLoading on the bottom of the list for pagination
     * <br/>
     * There is also the spinnerLoading on the actionBar, used for details<br/>
     */
    public void hideCenterWaiting() {
        if (spinnerCenterLoading != null) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Hide center loading");
            }
            spinnerCenterLoading.setVisibility(View.GONE);
        }
    }

    public void showCenterWaiting(boolean status) {
        if (spinnerCenterLoading != null) {
            if (status) {
                showCenterWaiting();
            } else {
                hideCenterWaiting();
            }
        }
    }

    public void setCurrentLocation(GoogleMap map) {
        map.setMyLocationEnabled(true);
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, true));
        if (location != null) {
            CameraPosition position = new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude())).build();
            map.animateCamera(CameraUpdateFactory.newCameraPosition(position));
        }
    }

    public Location getMyLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        return locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
    }

    public void moveToLocation(final GoogleMap map, final LatLng latLng) {
        CameraPosition position = new CameraPosition.Builder().target(new LatLng(latLng.latitude, latLng.longitude))
                .zoom(11f).build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(position));

    }

    public void addMarkers(ArrayList<Placemark> places) {
        for (Placemark item : places) {
            item.id = addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                    .position(item.getCoordinates()).snippet("Toca para mas información")
                    .title(item.getTitle())).getId();
        }
    }

    public Marker addMarker(MarkerOptions options) {
        return mMap.addMarker(options);
    }
}

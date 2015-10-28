package com.gfirem.elrosacruz;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {
    public static final String PREFS_NAME = "ElRosaCruz.pref";
    private static final String TAG = "MainActivity";
    private GoogleMap mMap;
    private Tracker mTracker;
    private View spinnerCenterLoading;
    private NavigationDataSet markerDataSet;
    private LatLng my_location;
    private LatLng init_1;
    private LatLng init_2;
    private MapHelper mapHelper;
    private ArrayList<Placemark> places;
    private AutoCompleteTextView txtFind;
    private ImageButton btnClear;
    private InputMethodManager imm;
    private SlidingUpPanelLayout lytPanel;
    private TextView txtPlaceTitle;
    private TextView txtPlaceDescription;
    private RelativeLayout lytOverMenu, detailsContainer;
    private LocationManager mLocationManager;

    private static Activity ctx;
    public static Context getContext(){
        return ctx.getApplicationContext();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ctx = this;

        // Restore preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        // Get bbox defaults
        float bound_top = settings.getFloat("bound_top", (float) 41.692712);
        float bound_left = settings.getFloat("bound_left", (float) -130.250764);
        float bound_bottom = settings.getFloat("bound_bottom", (float) -55.872848);
        float bound_right = settings.getFloat("bound_right", (float) -32.580848);

        init_1 = new LatLng(bound_top, bound_left);
        init_2 = new LatLng(bound_bottom, bound_right);


        spinnerCenterLoading = findViewById(R.id.progress);
        txtFind = (AutoCompleteTextView) findViewById(R.id.txtFind);
        btnClear = (ImageButton) findViewById(R.id.btnClear);
        lytPanel = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        txtPlaceTitle = (TextView) findViewById(R.id.txtPlaceTitle);
        txtPlaceDescription = (TextView) findViewById(R.id.txtPlaceDescription);
        lytOverMenu = (RelativeLayout) findViewById(R.id.over_menu);
        detailsContainer = (RelativeLayout) findViewById(R.id.detailsContainer);

        lytPanel.setPanelSlideListener(slidePanelListener);
        lytPanel.setPanelHeight(0);
        lytPanel.setShadowHeight(0);
        lytPanel.setCoveredFadeColor(getResources().getColor(android.R.color.transparent));
        txtFind.setText("");
        txtFind.setHint(R.string.text_find_temple);
        txtFind.setCursorVisible(true);

        lytOverMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lytPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                hideOverMenu();
            }
        });
        showCenterWaiting();

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);


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
                txtFind.setHint(R.string.text_find_temple);
                if (mMap != null && places != null && mapHelper != null) {
                    places = markerDataSet.getPlacemarks();
                    mMap.clear();
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
                    txtFind.setHint("");
                    txtFind.setCursorVisible(true);
                    btnClear.setVisibility(View.VISIBLE);
                    btnClear.bringToFront();
                } else {
                    btnClear.setVisibility(View.GONE);
                    txtFind.setHint(R.string.text_find_temple);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

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
                    imm.hideSoftInputFromWindow(txtFind.getWindowToken(), 0);
                }
            }
        });

    }

    SlidingUpPanelLayout.PanelSlideListener slidePanelListener = new SlidingUpPanelLayout.PanelSlideListener() {

        @Override
        public void onPanelSlide(View view, float v) {

        }

        @Override
        public void onPanelCollapsed(View view) {
            hideOverMenu();
        }

        @Override
        public void onPanelExpanded(View view) {

        }

        @Override
        public void onPanelAnchored(View view) {

        }

        @Override
        public void onPanelHidden(View view) {

        }
    };

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

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                hideCenterWaiting();
                if (my_location != null) {
                    LatLng near_places = places.get(0).getCoordinates();
                    mapHelper.zoomToFitLatLongs(my_location, near_places);
                    addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.my_position))
                            .position(my_location)
                            .snippet(getString(R.string.text_the_temple_is) + Math.ceil(places.get(0).getDistance()) + getString(R.string.text_measure_units_kilometer))
                            .title(getString(R.string.text_my_location))).showInfoWindow();
                } else {
                    mapHelper.zoomToFitLatLongs(init_1, init_2);
                }
                addMarkers(places);
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                /*Projection projection = mMap.getProjection();
                Point p = projection.toScreenLocation(point);
                LatLng topLeft = projection.fromScreenLocation(new Point(p.x - halfWidth, p.y - halfHeight));
                LatLng bottomRight = projection.fromScreenLocation(new Point(p.x + halfWidth, p.y + halfHeight));*/
                LatLngBounds curScreen = mMap.getProjection().getVisibleRegion().latLngBounds;

                // All objects are from android.context.Context
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();

                editor.putFloat("bound_top",(float)curScreen.northeast.latitude);
                editor.putFloat("bound_left",(float)curScreen.northeast.longitude);
                editor.putFloat("bound_bottom",(float)curScreen.southwest.longitude);
                editor.putFloat("bound_right",(float)curScreen.southwest.longitude);

                // Commit the edits!
                editor.commit();
            }
        });

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        // InfoWindowMap infoWindow = new
        // InfoWindowMap(getActivity());
        // map.setInfoWindowAdapter(infoWindow);
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

            @Override
            public void onInfoWindowClick(Marker marker) {
                Placemark current = markerDataSet.findById(marker.getId());
                if (current != null) {
                    marker.hideInfoWindow();
                    if (lytPanel != null && !current.isCountry()) {
                        showOverMenu();

//                        Size size1 = BaseUtils.getTextSize(current.getTitle());
//                        Size size2 = BaseUtils.getTextSize(current.getDescription());
//
//                        int percentHeight = ((50+size1.getHeight()+size2.getHeight())*100)/DisplayUtil.getDisplayHeight(MainActivity.this);
//
//                        DisplayUtil.setSizeByPercent(detailsContainer, ViewGroup.LayoutParams.MATCH_PARENT, percentHeight);

                        txtPlaceTitle.setText(current.getTitle());
                        txtPlaceDescription.setText(Html.fromHtml(current.getDescription()));
                        lytPanel.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);

                        return;
                    }
                }

                Toast.makeText(MainActivity.this, getString(R.string.text_no_details), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(lytPanel != null && lytPanel.getPanelState().equals(SlidingUpPanelLayout.PanelState.EXPANDED)){
            lytPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }
        else super.onBackPressed();
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

        imm.hideSoftInputFromWindow(txtFind.getWindowToken(), 0);
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

    public void showOverMenu() {
        lytOverMenu.setBackgroundColor(getResources().getColor(R.color.app_text_color_dark_alpha));
        lytOverMenu.setVisibility(View.VISIBLE);
        btnClear.setVisibility(View.GONE);
    }

    public void hideOverMenu() {
        lytOverMenu.setVisibility(View.GONE);
        if (txtFind.getText().length() > 0) {
            btnClear.setVisibility(View.VISIBLE);
        }
    }

    public boolean isOverMenuShow() {
        return lytOverMenu.getVisibility() == View.VISIBLE;
    }

    private Location getMyLocation() {
        mLocationManager = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
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
                    .position(item.getCoordinates()).snippet(getString(R.string.text_more_info))
                    .title(item.getTitle())).getId();
        }
    }

    public Marker addMarker(MarkerOptions options) {
        return mMap.addMarker(options);
    }
}

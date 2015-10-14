package com.gfirem.elrosacruz;

import android.app.Dialog;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
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
import com.gfirem.elrosacruz.entity.Size;
import com.gfirem.elrosacruz.utils.BaseUtils;
import com.gfirem.elrosacruz.utils.DisplayUtil;
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
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MainActivity";
    private GoogleMap mMap;
    private Tracker mTracker;
    private View spinnerCenterLoading;
    private NavigationDataSet markerDataSet;
    private LatLng my_location;
    private LatLng init_1 = new LatLng(41.692712, -130.250764);
    private LatLng init_2 = new LatLng(-55.872848, -32.580848);
    private MapHelper mapHelper;
    private ArrayList<Placemark> places;
    private AutoCompleteTextView txtFind;
    private ImageButton btnClear;
    private InputMethodManager imm;
    private SlidingUpPanelLayout lytPanel;
    private TextView txtPlaceTitle;
    private TextView txtPlaceDescription;
    private RelativeLayout lytOverMenu, detailsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        txtFind.setHint("Encuentra tu templo");
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
                    txtFind.setHint("");
                    txtFind.setCursorVisible(true);
                    btnClear.setVisibility(View.VISIBLE);
                    btnClear.bringToFront();
                } else {
                    btnClear.setVisibility(View.GONE);
                    txtFind.setHint("Encuentra tu templo");
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

        markerDataSet.setPlacemarks(places);
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                hideCenterWaiting();
//                 setCurrentLocation(mMap);
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
                else{
                    mapHelper.zoomToFitLatLongs(init_1, init_2);
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
                    marker.hideInfoWindow();
                    if(lytPanel != null && !current.isCountry()){
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
                    }
                    else{
                        Toast.makeText(MainActivity.this, "No hay detalles! ", Toast.LENGTH_SHORT).show();
                    }
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
        Location location = null;
        String provider = locationManager.getBestProvider(criteria, true);
        if (provider != null) {

            locationManager.requestLocationUpdates(provider, 100, 1, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    my_location = new LatLng(location.getLatitude(), location.getLongitude());
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            });
            location = locationManager.getLastKnownLocation(provider);
        }
        return location;
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

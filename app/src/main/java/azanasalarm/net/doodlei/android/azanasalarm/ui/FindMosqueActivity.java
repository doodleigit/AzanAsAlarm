package azanasalarm.net.doodlei.android.azanasalarm.ui;

import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.SphericalUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import azanasalarm.net.doodlei.android.azanasalarm.R;
import azanasalarm.net.doodlei.android.azanasalarm.adapter.PlacePagerAdapter;
import azanasalarm.net.doodlei.android.azanasalarm.interfaces.OnPlaceClick;
import azanasalarm.net.doodlei.android.azanasalarm.model.MarkerInfo;
import azanasalarm.net.doodlei.android.azanasalarm.model.NearByPlace;
import azanasalarm.net.doodlei.android.azanasalarm.util.AppContants;

public class FindMosqueActivity extends AppCompatActivity implements OnMapReadyCallback {

    private CardView cvDirection;
    private ViewPager viewpager;

    private GoogleMap mMap;
    private LocationManager locationManager;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationCallback locationCallback;
    private boolean mLocationPermissionGranted;

    private PlacePagerAdapter placePagerAdapter;

    private Bitmap marketIcon;

    private final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 5465;
    private String TAG = "FindMosqueActivity";
    private Location mLastKnownLocation, mCurrentLastLocation;
    private LatLng mDefaultLocation = new LatLng(-34, 151);
    private Marker mDestinationMarker = null;
    private float DEFAULT_ZOOM = 14f;
    private int DEFAULT_RADIUS = 1000;
    private String DEFAULT_PLACE_TYPE = "mosque";

    private ArrayList<NearByPlace> nearByPlaces;
    private List<Marker> markerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_mosque);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        nearByPlaces = new ArrayList<>();
        markerList = new ArrayList<>();
        marketIcon = getSmallSizeMarker();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        cvDirection = findViewById(R.id.direction);
        viewpager = findViewById(R.id.viewpager);
        viewpager.setClipToPadding(false);
        viewpager.setPadding(100, 0, 100, 0);
        viewpager.setPageMargin(40);

        OnPlaceClick onPlaceClick = new OnPlaceClick() {
            @Override
            public void onPlaceClick(String placeId) {
                if (mDestinationMarker != null) {
                    LatLng mOriginLocation = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                    LatLng mDestinationLocation = new LatLng(mDestinationMarker.getPosition().latitude, mDestinationMarker.getPosition().longitude);

                    MarkerInfo originMarkerInfo = new MarkerInfo(mOriginLocation, "", "");
                    MarkerInfo destinationMarkerInfo = new MarkerInfo(mDestinationLocation, mDestinationMarker.getTitle(), mDestinationMarker.getSnippet());

                    PlaceDetailsFragment placeDetailsFragment = PlaceDetailsFragment.newInstance(placeId, originMarkerInfo, destinationMarkerInfo);
                    placeDetailsFragment.show(getSupportFragmentManager(), "placeDetails");
                }
            }

            @Override
            public void onDirectionClick() {
                if (mDestinationMarker != null) {
                    handleDirectionClick();
                }
            }

            @Override
            public void onCallClick(String phoneNumber) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null));
                startActivity(intent);
            }
        };

        placePagerAdapter = new PlacePagerAdapter(this, nearByPlaces, onPlaceClick);
        viewpager.setAdapter(placePagerAdapter);

        showGpsSetting();

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                boolean isLocationNull = false;
                if (mCurrentLastLocation == null && mLastKnownLocation != null) {
                    mCurrentLastLocation = mLastKnownLocation;
                } else if (mLastKnownLocation == null) {
                    isLocationNull = true;
                }
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    // ...
                    mLastKnownLocation = location;
                }
                if (mCurrentLastLocation != null && mLastKnownLocation != null) {
                    float distance = mLastKnownLocation.distanceTo(mCurrentLastLocation);
                    if (distance > 500) { // 500m = 0.5km. minimum distance for call nearby api
                        mCurrentLastLocation = mLastKnownLocation;
                        getNearByMosque();
                    }
                } else {
                    if (isLocationNull) {
                        getNearByMosque();
                    }
                }
            }
        };

        cvDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDestinationMarker != null) {
                    handleDirectionClick();
                }
            }
        });

        viewpager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                mDestinationMarker = setDestinationMarker(i);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

    }

    private Marker setDestinationMarker(int i) {
        if (mDestinationMarker != null) {
            mDestinationMarker.remove();
        }
        return mMap.addMarker(new MarkerOptions()
                .position(nearByPlaces.get(i).getLatLng()));
    }

    private void handleDirectionClick() {
        LatLng mOriginLocation = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
        LatLng mDestinationLocation = new LatLng(mDestinationMarker.getPosition().latitude, mDestinationMarker.getPosition().longitude);

        MarkerInfo originMarkerInfo = new MarkerInfo(mOriginLocation, "", "");
        MarkerInfo destinationMarkerInfo = new MarkerInfo(mDestinationLocation, mDestinationMarker.getTitle(), mDestinationMarker.getSnippet());

        Intent intent = new Intent(FindMosqueActivity.this, PlaceDirectionActivity.class);
        intent.putExtra("origin", originMarkerInfo);
        intent.putExtra("destination", destinationMarkerInfo);
        startActivity(intent);
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
        mMap.getUiSettings().setMapToolbarEnabled(false);
        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            // Return null here, so that getInfoContents() is called next.
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                if (mDestinationMarker != null && mDestinationMarker.equals(marker)) {
                    return null;
                }
                // Inflate the layouts for the info window, title and snippet.
                View infoWindow = getLayoutInflater().inflate(R.layout.marker_info_item, null);

                TextView title = infoWindow.findViewById(R.id.title);
                title.setText(marker.getTitle());

                TextView snippet = infoWindow.findViewById(R.id.snippet);
                snippet.setText(marker.getSnippet());
//
//                if (cvDirection.getVisibility() != View.VISIBLE) {
//                    Animation slideIn = AnimationUtils.loadAnimation(FindMosqueActivity.this, R.anim.slide_in_right);
//                    cvDirection.setVisibility(View.VISIBLE);
//                    cvDirection.startAnimation(slideIn);
//                }

                for (int i = 0; i < markerList.size(); i++) {
                    if (marker.equals(markerList.get(i))) {
                        viewpager.setCurrentItem(i, true);
//                        setDestinationMarker(i);
                        break;
                    }
                }

//                if (mDestinationMarker != null) {
//                    mDestinationMarker.remove();
//                }
//                mDestinationMarker = marker;
                return infoWindow;
            }
        });

//        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
//            @Override
//            public void onMapClick(LatLng latLng) {
//                if (cvDirection.getVisibility() != View.GONE) {
//                    Animation slideOut = AnimationUtils.loadAnimation(FindMosqueActivity.this, R.anim.slide_out_right);
//                    cvDirection.setVisibility(View.GONE);
//                    cvDirection.startAnimation(slideOut);
//                }
//            }
//        });

        getLocationPermission();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mLocationPermissionGranted)
            startLocationUpdates();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    private void showGpsSetting() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            LocationRequest mLocationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(10 * 1000)
                    .setFastestInterval(5 * 1000);
            LocationSettingsRequest.Builder settingsBuilder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(mLocationRequest);
            settingsBuilder.setAlwaysShow(true);

            Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(this)
                    .checkLocationSettings(settingsBuilder.build());
            result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
                @Override
                public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                    try {
                        LocationSettingsResponse response =
                                task.getResult(ApiException.class);
                    } catch (ApiException exception) {
                        if (exception.getStatusCode() == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {// Location settings are not satisfied. But could be fixed by showing the
                            // user a dialog.
                            try {
                                // Cast to a resolvable exception.
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                resolvable.startResolutionForResult(
                                        FindMosqueActivity.this,
                                        LocationRequest.PRIORITY_HIGH_ACCURACY);
                            } catch (IntentSender.SendIntentException | ClassCastException e) {
                                // Ignore the error.
                            } // Ignore, should be an impossible error.

                        }

                    }
                }
            });
        }
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = (Location) task.getResult();
                            if (mLastKnownLocation != null) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(mLastKnownLocation.getLatitude(),
                                                mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                                getNearByMosque();
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            // Turn on the My Location layer and the related control on the map.
            updateLocationUI();

            // Get the current location of the device and set the position of the map.
            getDeviceLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    private void startLocationUpdates() {
        LocationRequest mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)
                .setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder settingsBuilder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        settingsBuilder.setAlwaysShow(true);
        mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest,
                locationCallback,
                Looper.getMainLooper());
    }

    private void addAllMarker(ArrayList<NearByPlace> nearByPlaces) {
        if (nearByPlaces.size() > 0) {
            mMap.clear(); // Clear previous mosque markers to set new.
        }
        for (NearByPlace nearByPlace : nearByPlaces) {
            markerList.add(mMap.addMarker(new MarkerOptions()
                    .title(nearByPlace.getName())
                    .position(nearByPlace.getLatLng())
                    .snippet(nearByPlace.getVicinity())
                    .icon(BitmapDescriptorFactory.fromBitmap(marketIcon))));
        }
        placePagerAdapter.notifyDataSetChanged();
    }

    private void showAlertMessage(String message) {
        Snackbar snackbar = Snackbar
                .make(findViewById(R.id.main), message, Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.retry), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getNearByMosque();
                    }
                });

        snackbar.show();
    }

    private void getNearByMosque() {
        String url = AppContants.googleNearByPlaceApi + "location=" + mLastKnownLocation.getLatitude() + "," + mLastKnownLocation.getLongitude() + "&radius=" + DEFAULT_RADIUS +
                "&type=" + DEFAULT_PLACE_TYPE + "&key=" + getString(R.string.google_maps_key);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    if (status.equals("OK")) {
                        nearByPlaces.clear();
                        markerList.clear();
                        JSONArray jsonArray = jsonObject.getJSONArray("results");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject object = jsonArray.getJSONObject(i);
                            LatLng latLng;
                            String id, name, icon, vicinity, rating, review, placeType, distance, phoneNumber;
                            double lat, lng;
                            lat = object.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                            lng = object.getJSONObject("geometry").getJSONObject("location").getDouble("lng");
                            latLng = new LatLng(lat, lng);
                            id = object.getString("place_id");
                            name = object.getString("name");
                            icon = jsonValidator(object, "icon"); //object.getString("icon");
                            vicinity = jsonValidator(object, "vicinity"); // object.getString("vicinity");
                            rating = jsonValidator(object, "rating"); //  object.getString("rating");
                            review = jsonValidator(object, "user_ratings_total"); // object.getString("user_ratings_total");
                            placeType = "Mosque";
                            distance = String.valueOf(SphericalUtil.computeDistanceBetween(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), latLng));
                            phoneNumber = "";
                            nearByPlaces.add(new NearByPlace(latLng, id, name, icon, vicinity, rating, review, placeType, distance, phoneNumber));
                        }
                        addAllMarker(nearByPlaces);
                    } else {
                        //SnakeBar not found alert
                        showAlertMessage(getString(R.string.an_error_occurred_while_searching_for_mosque_near_you));
                    }

                } catch (JSONException e) {
                    //SnakeBar data error
                    showAlertMessage(getString(R.string.an_error_occurred_while_searching_for_mosque_near_you));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //SnakeBar server error
                showAlertMessage(getString(R.string.server_is_not_responding));
            }
        });
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.getCache().clear();
        requestQueue.add(stringRequest);
    }

    private String jsonValidator(JSONObject object, String key) {
        try {
            if (object.has(key)) {
                return object.getString(key);
            } else {
                return "";
            }
        } catch (JSONException e) {
            return "";
        }
    }

    private Bitmap getSmallSizeMarker() {
        int height = 80;
        int width = 65;
        BitmapDrawable bitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.mosque_marker);
        Bitmap b = bitmapDrawable.getBitmap();
        return Bitmap.createScaledBitmap(b, width, height, false);
    }

}

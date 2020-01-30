package azanasalarm.net.doodlei.android.azanasalarm.ui;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.suke.widget.SwitchButton;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import azanasalarm.net.doodlei.android.azanasalarm.R;
import azanasalarm.net.doodlei.android.azanasalarm.adapter.AlarmAdapter;
import azanasalarm.net.doodlei.android.azanasalarm.database.AlarmDB;
import azanasalarm.net.doodlei.android.azanasalarm.model.AlarmModel;
import azanasalarm.net.doodlei.android.azanasalarm.util.AppContants;
import azanasalarm.net.doodlei.android.azanasalarm.util.Tools;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.VIBRATE;
import static android.Manifest.permission.WAKE_LOCK;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class AzanListActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    NavigationView navigationView;
    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    AlarmDB alarmDatabase;
    List<AlarmModel> alarmModelList = new ArrayList<>();
    private AlarmAdapter alarmAdapter;
    private InterstitialAd mPublisherInterstitialAd;
    private AdView mAdView;
    private SwitchButton switchAutoManual, switchSalahReminder;
    private TextView tvNoInternetMessage, tvAlarmType;

//    private FusedLocationProviderClient fusedLocationClient;
//    private LocationCallback locationCallback;

    private SharedPreferences preferences;

    private ProgressDialog progressDialog;

    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    public boolean canGetLocation = false;
    public boolean isManualControl = false;

    Location location;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;

    protected LocationManager locationManager;
    private LocationListener locationListener;

    private Handler handler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_azan_list);
        initialComponent();
    }

    private void initialComponent() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AppContants.networkConnectionChange);
        registerReceiver(networkChangeReceiver, intentFilter);

        preferences = getSharedPreferences(AppContants.preferenceKey, Context.MODE_PRIVATE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        MobileAds.initialize(this, getResources().getString(R.string.admob_app_id));

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);

        navigationView = findViewById(R.id.nav_view);
        switchAutoManual = navigationView.getHeaderView(0).findViewById(R.id.switchAutoManual);
        switchSalahReminder = navigationView.getHeaderView(0).findViewById(R.id.switchReminder);
        tvAlarmType = navigationView.getHeaderView(0).findViewById(R.id.alarmType);
        tvNoInternetMessage = findViewById(R.id.no_internet_message);
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mPublisherInterstitialAd = new InterstitialAd(this);
        mPublisherInterstitialAd.setAdUnitId(getResources().getString(R.string.interestitial));
        mPublisherInterstitialAd.loadAd(new AdRequest.Builder().build());

//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mPublisherInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
//                displayInterstitial();
            }
        });

        if (!checkPermission()) {
            requestPermission();
        } else {
            addDefaultAzanTimes(true);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        Tools.setRepeatAlarm(this, false);

        switchAutoManual.setChecked(preferences.getBoolean(AppContants.auto, true));
        switchSalahReminder.setChecked(preferences.getBoolean(AppContants.reminder, true));
//        tvAlarmType.setText(preferences.getBoolean(AppContants.auto, true) ? getString(R.string.auto) : getString(R.string.manual));

        if (Tools.isNetworkConnected(getApplicationContext())) {
            tvNoInternetMessage.setVisibility(View.GONE);
        } else {
            tvNoInternetMessage.setVisibility(View.VISIBLE);
        }

        switchAutoManual.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if (isChecked && !isGPSEnabled) {
                    //If user manually change auto detect ON.
                    isManualControl = true;
                    showGpsSetting();
                }
                setAutoManual(isChecked);
            }
        });
        switchSalahReminder.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                setReminder(isChecked);
            }
        });

    }

    private void getGpsSetting() {
        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (locationManager != null && !isGPSEnabled) {
                showGpsSetting();
            } else {
                getLocation();
            }
        } catch (Exception e) {
            getLocation();
        }
    }

    private void showGpsSetting() {
        LocationRequest mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(3 * 1000)
                .setFastestInterval(1 * 1000);
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
                    getLocation();
                } catch (ApiException exception) {
                    if (exception.getStatusCode() == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {// Location settings are not satisfied. But could be fixed by showing the
                        // user a dialog.
                        try {
                            // Cast to a resolvable exception.
                            ResolvableApiException resolvable = (ResolvableApiException) exception;
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            resolvable.startResolutionForResult(
                                    AzanListActivity.this,
                                    LocationRequest.PRIORITY_HIGH_ACCURACY);
                        } catch (IntentSender.SendIntentException | ClassCastException e) {
                            // Ignore the error.
                            getLocation();
                        } // Ignore, should be an impossible error.

                    } else {
                        getLocation();
                    }

                }
            }
        });
    }

    public void getLocation() {
        progressDialog.show();
        try {
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    handler.removeCallbacksAndMessages(null);
                    requestLocationWiseTime(location.getLatitude(), location.getLongitude());
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            };

            if (!isGPSEnabled && !isNetworkEnabled) {
                setAzanList();
            } else {
                this.canGetLocation = true;
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    if (isNetworkEnabled) {
                        locationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);

                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                            if (location != null) {
                                requestLocationWiseTime(location.getLatitude(), location.getLongitude());
//                                latitude = location.getLatitude();
//                                longitude = location.getLongitude();
                            }
                        }

                    }

                    if (isGPSEnabled) {
                        if (location == null) {
                            locationManager.requestLocationUpdates(
                                    LocationManager.GPS_PROVIDER,
                                    MIN_TIME_BW_UPDATES,
                                    MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
                            if (locationManager != null) {
                                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                                if (location != null) {
                                    requestLocationWiseTime(location.getLatitude(), location.getLongitude());
//                                    latitude = location.getLatitude();
//                                    longitude = location.getLongitude();
                                } else {
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            stopUsingGPS();
                                            setAzanList();
                                        }
                                    }, 10 * 1000);
                                }
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopUsingGPS() {
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
    }


    private void displayInterstitial() {

        if (mPublisherInterstitialAd.isLoaded()) {
            mPublisherInterstitialAd.show();
        }
    }

//    private void getCurrentLocation() {
//        progressDialog.show();
//        final LocationRequest locationRequest = LocationRequest.create();
//        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        locationRequest.setInterval(5000);
//        locationRequest.setFastestInterval(1000);
//
//        locationCallback = new LocationCallback() {
//            @Override
//            public void onLocationResult(LocationResult locationResult) {
//                if (locationResult == null) {
//                    return;
//                }
//                for (Location location : locationResult.getLocations()) {
//                    // Update UI with location data
//                    // ...
//                    requestLocationWiseTime(location.getLatitude(), location.getLongitude());
//                    break;
//                }
//            }
//        };
//
//        fusedLocationClient.getLastLocation()
//                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
//                    @Override
//                    public void onSuccess(Location location) {
//                        // Got last known location. In some rare situations this can be null.
//                        if (location != null) {
//                            // Logic to handle location object
//                            requestLocationWiseTime(location.getLatitude(), location.getLongitude());
//                        } else {
////                            fusedLocationClient.requestLocationUpdates(locationRequest,
////                                    locationCallback,
////                                    Looper.getMainLooper());
//                            setAzanList();
//                        }
//                    }
//                })
//                .addOnFailureListener(this, new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
////                        fusedLocationClient.requestLocationUpdates(locationRequest,
////                                locationCallback,
////                                Looper.getMainLooper());
//                        setAzanList();
//                    }
//                });
//    }

    private void addDefaultAzanTimes(boolean locationPermission) {
        alarmDatabase = new AlarmDB(AzanListActivity.this);
        alarmModelList = alarmDatabase.getAllHistory();
        recyclerView = findViewById(R.id.recycle_view);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        if (locationPermission) {
            if (alarmModelList.size() < 5) {
//                getCurrentLocation();
                getGpsSetting();
            } else {
                alarmAdapter = new AlarmAdapter(alarmModelList, this);
                recyclerView.setAdapter(alarmAdapter);
            }
        } else {
            if (alarmModelList.size() < 5) {
                setAzanList();
            } else {
                alarmAdapter = new AlarmAdapter(alarmModelList, this);
                recyclerView.setAdapter(alarmAdapter);
            }
        }
    }

    private void requestLocationWiseTime(double latitude, double longitude) {
//        stopLocationUpdates();
        stopUsingGPS();
        String method = "2";
        String month = "2";
        int year = Calendar.getInstance().get(Calendar.YEAR);

        getAzanTime(latitude, longitude, method, month, String.valueOf(year));
    }

    private void insertAzanList(String[] hour, String[] min) {
        progressDialog.dismiss();
        Resources res = getResources();
        String[] name = res.getStringArray(R.array.azan_list_english);
//        String[] hour = res.getStringArray(R.array.azan_hour);
//        String[] min = res.getStringArray(R.array.azan_min);
        String[] salah_reminder = res.getStringArray(R.array.salah_reminder);
        String defaultRingtone = "default";

        Field[] fields = R.raw.class.getFields();
        for (int count = 0; count < 1; count++) {
            Log.i("Raw Asset: ", fields[count].getName());
            defaultRingtone = "android.resource://" + getPackageName() + "/raw/" + fields[count].getName();
        }

//        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        for (int i = 1; i <= 5; i++) {
//            final Calendar calendar = Calendar.getInstance();

            if (i == 1) {
                alarmDatabase.addHistory(new AlarmModel(hour[i], "am", " Notes for " + name[i] + " salat", "false", "true", min[i], defaultRingtone, "Sound", "never", name[i], salah_reminder[i], i));
            } else {
                alarmDatabase.addHistory(new AlarmModel(hour[i], "pm", " Notes for " + name[i] + " salat", "false", "true", min[i], defaultRingtone, "Sound", "never", name[i], salah_reminder[i], i));
            }


//            Intent myIntent = new Intent(getApplicationContext(), AlarmReceiver.class);
//            myIntent.putExtra("eid", i + "");
//            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), i, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);
//
//            calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour[i]));
//            calendar.set(Calendar.MINUTE, Integer.parseInt(min[i]));
//            calendar.set(Calendar.SECOND, 0);
//
//            long difference = calendar.getTimeInMillis() - System.currentTimeMillis();
//            String timefromnow = new PrepareTwelveHourFormat().differerence(difference);
//            if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
//                calendar.add(Calendar.DAY_OF_YEAR, 1);
//            }
//            long timeInMin = calendar.getTimeInMillis();
//            alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMin, pendingIntent);
        }
        alarmModelList = alarmDatabase.getAllHistory();
        alarmAdapter = new AlarmAdapter(alarmModelList, this);
        recyclerView.setAdapter(alarmAdapter);

        new LovelyStandardDialog(this, LovelyStandardDialog.ButtonLayout.VERTICAL)
                .setTopColorRes(R.color.colorAccent)
                .setButtonsColorRes(R.color.colorPrimary)
                .setIcon(R.drawable.adhan)
                .setTitle(R.string.app_name)
                .setMessage(R.string.first_msg)
                .setPositiveButton(android.R.string.ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                })
                .show();

    }

//    private void stopLocationUpdates() {
//        fusedLocationClient.removeLocationUpdates(locationCallback);
//    }

    private void requestPermission() {
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.VIBRATE,
                        Manifest.permission.WAKE_LOCK,
                        Manifest.permission.READ_PHONE_STATE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            Toast.makeText(getApplicationContext(), "All permissions are granted!", Toast.LENGTH_SHORT).show();
                            addDefaultAzanTimes(true);
                        } else {
                            int result = ContextCompat.checkSelfPermission(AzanListActivity.this, ACCESS_COARSE_LOCATION);
                            if (result == PackageManager.PERMISSION_GRANTED) {
                                addDefaultAzanTimes(true);
                            } else {
                                addDefaultAzanTimes(false);
                            }
                        }

                        if (report.isAnyPermissionPermanentlyDenied()) {

                            Toast.makeText(getApplicationContext(), "Permissions are required to set alarm with vibration and alarm tone.You need to allow to use this app.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).
                withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Toast.makeText(getApplicationContext(), "Error occurred! ", Toast.LENGTH_SHORT).show();
                    }
                })
                .onSameThread()
                .check();


    }


    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE);
        int result2 = ContextCompat.checkSelfPermission(this, WAKE_LOCK);
        int result3 = ContextCompat.checkSelfPermission(this, VIBRATE);
        int result4 = ContextCompat.checkSelfPermission(this, READ_PHONE_STATE);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED && result3 == PackageManager.PERMISSION_GRANTED && result4 == PackageManager.PERMISSION_GRANTED;
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.azan_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_azaan_dua) {
            showAd();

            startActivity(new Intent(AzanListActivity.this, AzanDuaActivity.class));
        } else if (id == R.id.nav_azaan_benifits) {
            showAd();
            startActivity(new Intent(AzanListActivity.this, BenifitsActivity.class));
        } else if (id == R.id.nav_salah_waqt) {
            showAd();
            startActivity(new Intent(AzanListActivity.this, SalahWaqtActivity.class));
        } else if (id == R.id.nav_rabbana_dua) {
            showAd();
            startActivity(new Intent(AzanListActivity.this, DuaActivity.class));
        } else if (id == R.id.nav_compass) {
            startActivity(new Intent(AzanListActivity.this, CompassActivity.class));
        } else if (id == R.id.nav_find_mosque) {
            startActivity(new Intent(AzanListActivity.this, FindMosqueActivity.class));
        } else if (id == R.id.nav_other_apps) {
            Uri uri = Uri.parse("https://play.google.com/store/apps/developer?id=Doodle+Inc.");
            Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
            try {
                startActivity(myAppLinkToMarket);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, " unable to find  app in play store ", Toast.LENGTH_LONG).show();
            }

        } else if (id == R.id.nav_share) {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, "Invitation ");
            i.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text));
            try {
                startActivity(Intent.createChooser(i, "Share Using"));
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, " unable to find  app in play store ", Toast.LENGTH_LONG).show();
            }
        } else if (id == R.id.nav_rate) {
            Uri uri = Uri.parse("https://goo.gl/6uLJyH");
            Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
            try {
                startActivity(myAppLinkToMarket);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, " unable to find  app in play store ", Toast.LENGTH_LONG).show();
            }
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showAd() {
        if (mPublisherInterstitialAd.isLoaded()) {
            mPublisherInterstitialAd.show();
            mPublisherInterstitialAd.loadAd(new AdRequest.Builder().build());
        }
    }

    String city = "", country = "";

    private void getAzanTime(final double latitude, final double longitude, final String method, final String month, final String year) {

        Geocoder geoCoder = new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            List<Address> addresses = geoCoder.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                city = addresses.get(0).getLocality();
                country = addresses.get(0).getCountryName();
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        String url;
        if (city.isEmpty() || country.isEmpty()) {
            url = AppContants.azanTimeUrl + "?latitude=" + latitude + "&longitude=" + longitude + "&method=" + method + "&month=" + month + "&year=" + year;
        } else {
            url = AppContants.azanTimeByCityUrl + "?city=" + city + "&country=" + country + "&method=" + method + "&month=" + month + "&year=" + year;
        }
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    if (jsonArray.length() >= 1) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject object = jsonArray.getJSONObject(i);
                            JSONObject timings = object.getJSONObject("timings");
                            String[] hours = new String[6];
                            String[] minutes = new String[6];
                            hours[0] = "0";
                            hours[1] = timings.getString("Fajr").substring(0, 2);
                            hours[2] = timings.getString("Dhuhr").substring(0, 2);
                            hours[3] = timings.getString("Asr").substring(0, 2);
                            hours[4] = timings.getString("Maghrib").substring(0, 2);
                            hours[5] = timings.getString("Isha").substring(0, 2);
                            minutes[0] = "0";
                            minutes[1] = timings.getString("Fajr").substring(3, 5);
                            minutes[2] = timings.getString("Dhuhr").substring(3, 5);
                            minutes[3] = timings.getString("Asr").substring(3, 5);
                            minutes[4] = timings.getString("Maghrib").substring(3, 5);
                            minutes[5] = timings.getString("Isha").substring(3, 5);
                            setCityAndCountry(city, country);
                            insertAzanList(hours, minutes);
                            break;
                        }
                    } else {
                        setAzanList();
                    }
                } catch (JSONException ignored) {
                    setAzanList();
                }
                progressDialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                setAzanList();
                progressDialog.dismiss();
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

    private void setCityAndCountry(String city, String country) {
        SharedPreferences preferences = getSharedPreferences(AppContants.preferenceKey, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(AppContants.city, city);
        editor.putString(AppContants.country, country);
        editor.apply();
    }

    private void setAzanList() {
        String[] hours = {"12", "12", "12", "12", "12", "12", "12"};
        String[] minutes = {"00", "00", "00", "00", "00", "00", "00"};
        insertAzanList(hours, minutes);
    }

    private void setAutoManual(boolean isChecked) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(AppContants.auto, isChecked);
        editor.apply();
//        tvAlarmType.setText(isChecked ? getString(R.string.auto) : getString(R.string.manual));
        for (int i = 0; i < alarmModelList.size(); i++) {
            alarmModelList.get(i).setIsAuto(String.valueOf(isChecked));
            alarmDatabase.updateAlarm(alarmModelList.get(i).getId(), alarmModelList.get(i));
        }
        if (alarmAdapter != null)
            alarmAdapter.notifyDataSetChanged();
    }

    private void setReminder(boolean isChecked) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(AppContants.reminder, isChecked);
        editor.apply();
    }

    BroadcastReceiver networkChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isConnected = intent.getBooleanExtra("is_connected", true);
            if (isConnected) {
                tvNoInternetMessage.setVisibility(View.GONE);
            } else {
                tvNoInternetMessage.setVisibility(View.VISIBLE);
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case LocationRequest.PRIORITY_HIGH_ACCURACY:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        Log.i("TAG", "onActivityResult: GPS Enabled by user");
                        isGPSEnabled = true;
                        switchAutoManual.setChecked(true);
                        checkManualControl();
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        Log.i("TAG", "onActivityResult: User rejected GPS request");
                        isGPSEnabled = false;
                        switchAutoManual.setChecked(false);
                        checkManualControl();
                        break;
                    default:
                        checkManualControl();
                        break;
                }
                break;
        }
    }

    private void checkManualControl() {
        //Check if user manually change auto detect ON and request GPS setting.
        if (!isManualControl)
            getLocation();
        else
            isManualControl = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkChangeReceiver);
    }
}

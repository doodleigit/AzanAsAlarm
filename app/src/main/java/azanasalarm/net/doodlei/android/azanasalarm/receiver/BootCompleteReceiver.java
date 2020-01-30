package azanasalarm.net.doodlei.android.azanasalarm.receiver;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import azanasalarm.net.doodlei.android.azanasalarm.database.AlarmDB;
import azanasalarm.net.doodlei.android.azanasalarm.model.AlarmModel;
import azanasalarm.net.doodlei.android.azanasalarm.util.AppContants;
import azanasalarm.net.doodlei.android.azanasalarm.util.PrepareTwelveHourFormat;
import azanasalarm.net.doodlei.android.azanasalarm.util.Tools;

import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.LOCATION_SERVICE;

public class BootCompleteReceiver extends BroadcastReceiver {

    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    public boolean canGetLocation = false;

    Location location;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;

    protected LocationManager locationManager;
    private LocationListener locationListener;

    private Handler handler = new Handler();

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            try {
                Tools.setRepeatAlarm(context, true);
                locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);

                isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

                isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                locationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        handler.removeCallbacksAndMessages(null);
                        requestLocationWiseTime(context, location.getLatitude(), location.getLongitude());
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

                if (isGPSEnabled || isNetworkEnabled) {
                    this.canGetLocation = true;
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        if (isNetworkEnabled) {
                            locationManager.requestLocationUpdates(
                                    LocationManager.NETWORK_PROVIDER,
                                    MIN_TIME_BW_UPDATES,
                                    MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);

                            if (locationManager != null) {
                                location = locationManager
                                        .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                                if (location != null) {
                                    requestLocationWiseTime(context, location.getLatitude(), location.getLongitude());
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
                                        requestLocationWiseTime(context, location.getLatitude(), location.getLongitude());
//                                    latitude = location.getLatitude();
//                                    longitude = location.getLongitude();
                                    } else {
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                stopUsingGPS();
                                            }
                                        }, 30 * 1000);
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

    }

    public void stopUsingGPS() {
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
    }

    private void setReAlarm(Context context, boolean isUpdate, String[] hour, String[] min) {
        String previousTimeZone, currentTimeZone;
        SharedPreferences preferences = context.getSharedPreferences(AppContants.preferenceKey, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        previousTimeZone = preferences.getString(AppContants.timeZone, "");
        currentTimeZone = getTimeZone();
        editor.putString(AppContants.timeZone, currentTimeZone);
        editor.apply();

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        PrepareTwelveHourFormat prepareTwelveHourFormat = new PrepareTwelveHourFormat();

        if (!Objects.requireNonNull(previousTimeZone).equals(currentTimeZone)) {
            AlarmDB alarmDatabase = new AlarmDB(context);
            List<AlarmModel> alarmModelList = new ArrayList<>();
            alarmModelList = alarmDatabase.getAllHistory();

            for (int i = 1; i < alarmModelList.size(); i++) {
                AlarmModel alarmModel = alarmModelList.get(i);
                if (alarmModel.getIsSet().equals("true") && alarmModel.getIsAuto().equals("true")) {
                    if (isUpdate) {
                        prepareTwelveHourFormat.twelveHourformat(Integer.parseInt(hour[i]), Integer.parseInt(min[i]));
                        alarmModel.setTime(hour[i]);
                        alarmModel.setMin(min[i]);
                        alarmModel.setAmpm(prepareTwelveHourFormat.AmPm());
                    }
                    alarmDatabase.updateAlarm(alarmModel.getId(), alarmModel);
                    Intent myIntent;
                    PendingIntent pendingIntent;
                    myIntent = new Intent(context, AlarmReceiver.class);

                    myIntent.putExtra("eid", alarmModel.getId() + "");
                    calendar.set(Calendar.HOUR_OF_DAY, isUpdate ? Integer.parseInt(hour[i]) : Integer.parseInt(alarmModel.getTime()));
                    calendar.set(Calendar.MINUTE, isUpdate ? Integer.parseInt(min[i]) : Integer.parseInt(alarmModel.getMin()));
                    pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), alarmModel.getId(), myIntent, PendingIntent.FLAG_CANCEL_CURRENT);

                    long difference = calendar.getTimeInMillis() - System.currentTimeMillis();
                    String timefromnow = prepareTwelveHourFormat.differerence(difference);
                    Toast.makeText(context, timefromnow, Toast.LENGTH_SHORT).show();

                    if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                        calendar.add(Calendar.DAY_OF_YEAR, 1);
                    }
                    long timeInMin = calendar.getTimeInMillis();
                    if (alarmModel.getRepeat().equals("never")) {
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMin, pendingIntent);
                    } else {
                        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, timeInMin, AlarmManager.INTERVAL_DAY, pendingIntent);
                    }
//                    alarmModel.setIsSet("true");
//                    alarmDatabase.updateCheckedAlarm(alarmModel);
                }
            }
        }
    }

    private String getTimeZone() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"),
                Locale.getDefault());
        Date currentLocalTime = calendar.getTime();
        DateFormat date = new SimpleDateFormat("Z", Locale.getDefault());
        return date.format(currentLocalTime);
    }

    private void requestLocationWiseTime(Context context, double latitude, double longitude) {
        String method = "2";
        String month = "2";
        int year = Calendar.getInstance().get(Calendar.YEAR);
        getAzanTime(context, latitude, longitude, method, month, String.valueOf(year));
    }

    String city = "", country = "";

    private void getAzanTime(final Context context, final double latitude, final double longitude, final String method, final String month, final String year) {
        Geocoder geoCoder = new Geocoder(context, Locale.getDefault());
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
        if (!city.isEmpty() && !country.isEmpty()) {
            url = AppContants.azanTimeUrl + "?latitude=" + latitude + "&longitude=" + longitude + "&method=" + method + "&month=" + month + "&year=" + year;
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
                                hours[1] = timings.getString("Fajr").substring(0, 2);
                                hours[2] = timings.getString("Dhuhr").substring(0, 2);
                                hours[3] = timings.getString("Asr").substring(0, 2);
                                hours[4] = timings.getString("Maghrib").substring(0, 2);
                                hours[5] = timings.getString("Isha").substring(0, 2);
                                minutes[1] = timings.getString("Fajr").substring(3, 5);
                                minutes[2] = timings.getString("Dhuhr").substring(3, 5);
                                minutes[3] = timings.getString("Asr").substring(3, 5);
                                minutes[4] = timings.getString("Maghrib").substring(3, 5);
                                minutes[5] = timings.getString("Isha").substring(3, 5);
                                setCityAndCountry(context, city, country);
                                setReAlarm(context, true, hours, minutes);
                                break;
                            }
                        } else {
                            setReAlarm(context, false, new String[5], new String[5]);
                        }
                    } catch (JSONException ignored) {
                        setReAlarm(context, false, new String[5], new String[5]);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    setReAlarm(context, false, new String[5], new String[5]);
                }
            });
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                    10000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            RequestQueue requestQueue = Volley.newRequestQueue(context);
            requestQueue.getCache().clear();
            requestQueue.add(stringRequest);
        }
    }

    private void setCityAndCountry(Context context, String city, String country) {
        SharedPreferences preferences = context.getSharedPreferences(AppContants.preferenceKey, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(AppContants.city, city);
        editor.putString(AppContants.country, country);
        editor.apply();
    }

}

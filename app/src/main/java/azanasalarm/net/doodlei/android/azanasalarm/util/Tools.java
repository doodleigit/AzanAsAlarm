package azanasalarm.net.doodlei.android.azanasalarm.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;

import azanasalarm.net.doodlei.android.azanasalarm.receiver.LocationChangeReceiver;

import static android.content.Context.ALARM_SERVICE;

public class Tools {

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager mConnectManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (mConnectManager != null) {
            NetworkInfo[] mNetworkInfo = mConnectManager.getAllNetworkInfo();
            for (int i = 0; i < mNetworkInfo.length; i++) {
                if (mNetworkInfo[i].getState() == NetworkInfo.State.CONNECTED)
                    return true;
            }
        }
        return false;
    }

    public static void setRepeatAlarm(Context context, boolean bootComplete) {
        long INTERVAL_TWO_HOUR = 2 * 3600000L;
        SharedPreferences preferences = context.getSharedPreferences(AppContants.preferenceKey, Context.MODE_PRIVATE);
        boolean isFirstTime = preferences.getBoolean(AppContants.isFirstTime, true);
        if (isFirstTime || bootComplete) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(AppContants.isFirstTime, false);
            editor.apply();
            Intent myIntent;
            AlarmManager alarmManager;
            PendingIntent pendingIntent;
            myIntent = new Intent(context, LocationChangeReceiver.class);
            alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
            pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), AppContants.repeatAlarmId, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime(), INTERVAL_TWO_HOUR, pendingIntent);
        }
    }

}

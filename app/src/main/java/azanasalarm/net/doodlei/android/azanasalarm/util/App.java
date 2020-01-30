package azanasalarm.net.doodlei.android.azanasalarm.util;

import android.app.Application;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import com.google.android.gms.ads.MobileAds;


import azanasalarm.net.doodlei.android.azanasalarm.R;
import azanasalarm.net.doodlei.android.azanasalarm.receiver.NetworkConnectionReceiver;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        MobileAds.initialize(this,getString(R.string.admob_app_id));
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(new NetworkConnectionReceiver(), intentFilter);
    }
}

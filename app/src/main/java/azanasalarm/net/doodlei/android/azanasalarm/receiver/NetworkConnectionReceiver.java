package azanasalarm.net.doodlei.android.azanasalarm.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import azanasalarm.net.doodlei.android.azanasalarm.util.AppContants;
import azanasalarm.net.doodlei.android.azanasalarm.util.Tools;

public class NetworkConnectionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.sendBroadcast((new Intent().putExtra("is_connected", Tools.isNetworkConnected(context))).setAction(AppContants.networkConnectionChange));
    }
}

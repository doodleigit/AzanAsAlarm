package azanasalarm.net.doodlei.android.azanasalarm.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import java.util.Calendar;
import java.util.Locale;

import azanasalarm.net.doodlei.android.azanasalarm.database.AlarmDB;
import azanasalarm.net.doodlei.android.azanasalarm.model.AlarmModel;
import azanasalarm.net.doodlei.android.azanasalarm.ui.AzanRingingActivity;

import static android.content.Context.POWER_SERVICE;


public class AlarmReceiver extends BroadcastReceiver {
    private AlarmModel alarmModel;

    @Override
    public void onReceive(final Context context, Intent intent) {


        AlarmDB alarmDatabase = new AlarmDB(context);
        alarmModel = alarmDatabase.getAlarm(Integer.parseInt(intent.getStringExtra("eid")));
        String repeateddays = alarmModel.getRepeat();
        Calendar sCalendar = Calendar.getInstance();
        String today = sCalendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
        if (repeateddays.equals("never")) {
            PowerManager pm;
            PowerManager.WakeLock wl;
            pm = (PowerManager) context.getSystemService(POWER_SERVICE);
            wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Alarm Is Ringing");
            wl.acquire();

            Intent startRingingActivityIntent = new Intent(context,
                    AzanRingingActivity.class);

            startRingingActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startRingingActivityIntent.putExtra("eid", intent.getStringExtra("eid"));
            context.startActivity(startRingingActivityIntent);


        } else {
            if (repeateddays.contains(today)) {
                PowerManager pm;
                PowerManager.WakeLock wl;
                pm = (PowerManager) context.getSystemService(POWER_SERVICE);
                wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Alarm Is Ringing");
                wl.acquire();

                Intent startRingingActivityIntent = new Intent(context,
                        AzanRingingActivity.class);

                startRingingActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startRingingActivityIntent.putExtra("eid", intent.getStringExtra("eid"));
                context.startActivity(startRingingActivityIntent);


            }
        }

        //Log.e("Alarm Notice ",new Date().toString()+" for "+intent.getStringExtra("eid"));

    }


}
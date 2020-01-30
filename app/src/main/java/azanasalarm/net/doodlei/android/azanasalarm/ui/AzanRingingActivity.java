package azanasalarm.net.doodlei.android.azanasalarm.ui;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.ncorti.slidetoact.SlideToActView;

import azanasalarm.net.doodlei.android.azanasalarm.R;
import azanasalarm.net.doodlei.android.azanasalarm.database.AlarmDB;
import azanasalarm.net.doodlei.android.azanasalarm.model.AlarmModel;
import azanasalarm.net.doodlei.android.azanasalarm.receiver.Salahreminder;
import azanasalarm.net.doodlei.android.azanasalarm.service.MediaPlayerService;
import azanasalarm.net.doodlei.android.azanasalarm.util.AppContants;
import azanasalarm.net.doodlei.android.azanasalarm.util.PrepareTwelveHourFormat;

public class AzanRingingActivity extends AppCompatActivity {

    PowerManager pm;
    PowerManager.WakeLock wl;
    private SlideToActView button;
    private TextView time, ampm, note;
    private AlarmDB alarmDB;
    private PrepareTwelveHourFormat prepareTwelveHourFormat;
    private String id;
    private Intent playingAlarmTone;
    private AlarmModel alarmModel;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        preferences = getSharedPreferences(AppContants.preferenceKey, Context.MODE_PRIVATE);

        pm = (PowerManager) getSystemService(POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Alarm Is Ringing");
        wl.acquire();

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_FULLSCREEN |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        setContentView(R.layout.activity_azan_ringing);


        playingAlarmTone = new Intent(this, MediaPlayerService.class);

        prepareTwelveHourFormat = new PrepareTwelveHourFormat();

        id = getIntent().getStringExtra("eid");

        int alarmid = Integer.parseInt(id);

        alarmDB = new AlarmDB(this);
        alarmModel = alarmDB.getAlarm(alarmid);
        String alarmtime = prepareTwelveHourFormat.twelveHourformat(Integer.parseInt(alarmModel.getTime()), Integer.parseInt(alarmModel.getMin()));
        String ampmtext = prepareTwelveHourFormat.AmPm();

        time = findViewById(R.id.alarm_time);
        time.setKeepScreenOn(true);
        ampm = findViewById(R.id.am_pm);
        note = findViewById(R.id.alarm_note);

        time.setText(alarmtime);
        ampm.setText(ampmtext);
        note.setText(alarmModel.getNotes());

        if (preferences.getBoolean(AppContants.reminder, true)) {
            long sec = 0;
            try {
                sec = Long.parseLong(alarmModel.getSalahreminder()) * 60 * 1000;
            } catch (Exception ex) {
                sec = 300;
            }
            long salahremindertime = System.currentTimeMillis() + sec;

            Intent myIntent = new Intent(getBaseContext(), Salahreminder.class);
            myIntent.putExtra("eid", alarmModel.getId() + "");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getBaseContext(), alarmModel.getId() + 10, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP, salahremindertime, pendingIntent);
        }

        if (alarmModel.getAlarmtype().equals("Sound")) {
            playingAlarmTone.putExtra("commandType", 3);
            playingAlarmTone.putExtra("tone", alarmModel.getRingtone());
        } else if (alarmModel.getAlarmtype().equals("Vibrate")) {
            playingAlarmTone.putExtra("commandType", 2);
            playingAlarmTone.putExtra("tone", alarmModel.getRingtone());
        } else if (alarmModel.getAlarmtype().equals("Sound and Vibrate")) {
            playingAlarmTone.putExtra("commandType", 1);
            playingAlarmTone.putExtra("tone", alarmModel.getRingtone());
        }
        startService(playingAlarmTone);

        button = findViewById(R.id.example);
        button.setOnSlideCompleteListener(new SlideToActView.OnSlideCompleteListener() {
            @Override
            public void onSlideComplete(SlideToActView slideToActView) {
                stop();
            }
        });
    }


    private void stop() {
        playingAlarmTone.putExtra("commandType", 4);
        playingAlarmTone.putExtra("tone", "default");
        startService(playingAlarmTone);
        wl.release();
        if (alarmModel.getRepeat().equals("never")) {
            alarmModel.setIsSet("false");
            alarmDB.updateAlarm(alarmModel.getId(), alarmModel);
        }


        if (android.os.Build.VERSION.SDK_INT >= 21) {
            finishAndRemoveTask();
        } else {
            finish();
        }


    }


    @Override
    public void onBackPressed() {

    }

    @Override
    public void onPause() {

        super.onPause();
    }


}

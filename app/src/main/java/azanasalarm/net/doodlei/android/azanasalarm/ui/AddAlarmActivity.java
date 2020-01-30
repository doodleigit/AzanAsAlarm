package azanasalarm.net.doodlei.android.azanasalarm.ui;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.yarolegovich.lovelydialog.LovelyTextInputDialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import azanasalarm.net.doodlei.android.azanasalarm.R;
import azanasalarm.net.doodlei.android.azanasalarm.database.AlarmDB;
import azanasalarm.net.doodlei.android.azanasalarm.model.AlarmModel;
import azanasalarm.net.doodlei.android.azanasalarm.receiver.AlarmReceiver;
import azanasalarm.net.doodlei.android.azanasalarm.util.PrepareTwelveHourFormat;

public class AddAlarmActivity extends AppCompatActivity {

    private static final int SELECT_TONE = 200;

    private final Calendar calendar = Calendar.getInstance();
    private final PrepareTwelveHourFormat prepareTwelveHourFormat = new PrepareTwelveHourFormat();
    private final List<String> days = new ArrayList<>();
    private final String[] daysWeek = {"Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
    private final CharSequence[] alaramType = {"Sound", "Vibrate", "Sound and Vibrate"};
    MediaPlayer mediaPlayer = new MediaPlayer();
    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
        }
    };
    private int hour = 0, min = 0;
    private AudioManager audioManager;
    //from system
    private AlarmManager alarmManager;

    private MenuItem item;
    //utitl
    private AlarmDB alarmDatabase;
    private AlarmModel alarmModel;
    private int alarmId;
    private String time;
    private String repeatvalue = "never";
    private String alarmtypevalue = "Sound";
    private String AM_PM = "";
    private String ringtone = "default";
    private boolean[] checked = {false, false, false, false, false, false, false};
    //view variables
    private EditText editText;
    private TextView pickedtime;
    private TextView am_pm;
    private TextView repeating_days;
    private TextView alarmtype;
    private TextView tone_name;
    private TextView salah_reminder;
    private Dialog levelDialog;
    private SeekBar seekBar;
    private boolean isMovingSeekBar = false;
    private boolean isManual = false;

    //  private AdView mAdView;
    private InterstitialAd mPublisherInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_alarm);

        //view initialize
        am_pm = findViewById(R.id.am_pm);
        pickedtime = findViewById(R.id.pickedtime);
        editText = findViewById(R.id.inputSearch);
        repeating_days = findViewById(R.id.repeating_days);
        alarmtype = findViewById(R.id.alarm_type);
        tone_name = findViewById(R.id.tone_name);
        salah_reminder = findViewById(R.id.salah_reminder);


        //utility intialize
        alarmDatabase = new AlarmDB(this);

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmId = getIntent().getIntExtra("id", -1);
        alarmModel = alarmDatabase.getAlarm(alarmId);
        hour = Integer.parseInt(alarmModel.getTime());
        min = Integer.parseInt(alarmModel.getMin());
        isManual = !alarmModel.getIsAuto().equals("true");

        intializeTime();
        intializeCal();
        initializeView();


        seekBar = findViewById(R.id.seekBar);
        audioManager = (AudioManager) AddAlarmActivity.this.getSystemService(
                Context.AUDIO_SERVICE);
        seekBar.setMax(audioManager
                .getStreamMaxVolume(AudioManager.STREAM_ALARM));
        seekBar.setProgress(audioManager
                .getStreamVolume(AudioManager.STREAM_ALARM));
        seekBar.incrementProgressBy(1);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isMovingSeekBar = true;
                menuItemEnable(true);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isMovingSeekBar = false;
                if (mediaPlayer.isPlaying()) {
                    handler.removeCallbacks(runnable);
                    handler.postDelayed(runnable, 2500);
                }

            }

            @Override
            public void onProgressChanged(SeekBar arg0, int progress,
                                          boolean arg2) {
                if (isMovingSeekBar) {
                    audioManager.setStreamVolume(AudioManager.STREAM_ALARM,
                            progress, 0);

                    Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

                    if (!alarmModel.getRingtone().equals("default")) {
                        try {
                            alarmUri = Uri.parse(alarmModel.getRingtone());
                        } catch (Exception ignored) {
                        }
                    }
                    if (alarmUri == null) {
                        alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                    }

                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.setVolume(progress, progress);
                    } else {
                        try {
                            mediaPlayer.reset();
                            mediaPlayer.setDataSource(getApplicationContext(), alarmUri);
                            mediaPlayer.prepare();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        mediaPlayer.start();

                    }


                }
            }

        });

        //  mAdView = findViewById(R.id.adView);
        // AdRequest adRequest = new AdRequest.Builder().build();
        //mAdView.loadAd(adRequest);

        mPublisherInterstitialAd = new InterstitialAd(this);
        mPublisherInterstitialAd.setAdUnitId(getResources().getString(R.string.interestitial));
//        mPublisherInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
//        mPublisherInterstitialAd.loadAd(new AdRequest.Builder().build());
        AdRequest adRequest = new AdRequest.Builder().build();
        mPublisherInterstitialAd.loadAd(adRequest);
        mPublisherInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                displayInterstitial();
            }
        });


    }

    private void displayInterstitial() {
        if (mPublisherInterstitialAd.isLoaded()) {
            mPublisherInterstitialAd.show();
        }
    }

    private void initializeView() {
        //exixsting values from alarm model
        alarmModel = alarmDatabase.getAlarm(alarmId);
        repeatvalue = alarmModel.getRepeat();
        ringtone = alarmModel.getRingtone();
        alarmtypevalue = alarmModel.getAlarmtype();

        //setting it to view
        editText.setText(alarmModel.getNotes());
        repeating_days.setText(repeatvalue);
        alarmtype.setText(alarmtypevalue);


        if (!alarmModel.getRingtone().equals("default")) {

            try {
                final Uri uri = Uri.parse(alarmModel.getRingtone());
                MediaMetadataRetriever metaRetriver;
                metaRetriver = new MediaMetadataRetriever();
                metaRetriver.setDataSource(this, uri);
                String title = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                tone_name.setText(title);
            } catch (Exception e) {
                tone_name.setText("default");
            }
        } else {
            tone_name.setText("default");
        }

        String text = "Remind me after " + alarmModel.getSalahreminder() + " min";

        salah_reminder.setText(text);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                menuItemEnable(true);
            }
        });

    }


    private void intializeTime() {
        time = prepareTwelveHourFormat.twelveHourformat(hour, min);
        pickedtime.setText(time);
        AM_PM = prepareTwelveHourFormat.AmPm();
        am_pm.setText(AM_PM);
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(AddAlarmActivity.this, R.style.TimePickerDialogTheme,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int phour, int pmin) {
                        hour = phour;
                        min = pmin;
                        intializeTime();
                        intializeCal();
                        menuItemEnable(true);
                        isManual = true;
                    }
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);
        timePickerDialog.show();

    }

    private void intializeCal() {
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, min);
        calendar.set(Calendar.SECOND, 0);
    }

    public void pickTime(View view) {
        showTimePicker();
    }

    public void pickdays(View view) {
        days.clear();
        boolean[] chekeddays = {false, false, false, false, false, false, false};
        if (repeatvalue.equals("never")) {
            checked = chekeddays;
        } else {
            String[] array = repeatvalue.split(" , ");
            for (String anArray : array) {
                int index = findindex(anArray);
                checked[index] = true;
                days.add(daysWeek[index]);
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.TimePickerDialogTheme);
        builder.setCancelable(false);
        builder.setTitle("Select Days to Repeat Alarm:");
        builder.setMultiChoiceItems(daysWeek, checked,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int selectedItemId,
                                        boolean isSelected) {
                        if (isSelected) {
                            days.add(daysWeek[selectedItemId]);
                        } else if (days.contains(daysWeek[selectedItemId])) {
                            days.remove(daysWeek[selectedItemId]);
                        }
                    }
                })
                .setPositiveButton("Done!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        StringBuilder temp = new StringBuilder();
                        if (days.size() > 0) {
                            for (int i = 0; i < days.size(); i++) {
                                if (i < days.size() - 1) {
                                    temp.append(days.get(i)).append(" , ");
                                } else {
                                    temp.append(days.get(i));
                                }
                            }
                            repeating_days.setText(temp.toString());
                            repeatvalue = temp.toString();
                        } else {
                            repeatvalue = "never";
                            repeating_days.setText(repeatvalue);
                            days.clear();
                            dialog.dismiss();
                        }
                        menuItemEnable(true);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        repeatvalue = "never";
                        repeating_days.setText(repeatvalue);
                        days.clear();
                        dialog.dismiss();
                    }
                });


        Dialog dialog = builder.create();
        dialog.show();

    }

    public void salah_reminder(View view) {
        new LovelyTextInputDialog(this, R.style.CreateplayListDailog)
                .setTopColorRes(R.color.dailogtheme)
                .setTitle(R.string.set_salah_reminder)
                .setIcon(R.drawable.adhan)
                .setHint(R.string.number_hint)
                .setInputFilter(R.string.number_input, new LovelyTextInputDialog.TextFilter() {
                    @Override
                    public boolean check(String text) {
                        return text.matches("[0-9]{0,6}");
                    }
                })
                .setConfirmButton(android.R.string.ok, new LovelyTextInputDialog.OnTextInputConfirmListener() {
                    @Override
                    public void onTextInputConfirmed(String text) {
                        alarmModel.setSalahreminder(text);
                        text = "Remind me after " + text + " min";
                        salah_reminder.setText(text);
                        menuItemEnable(true);
                    }
                }).setNegativeButton("Cancel", null)
                .show();


    }

    private int findindex(String s) {
        for (int i = 0; i < daysWeek.length; i++) {
            if (daysWeek[i].equals(s)) {
                return i;
            }
        }
        return 0;
    }

    public void select_alarmtype(View view) {
        int checked = 0;
        if (alarmtypevalue.equals("Vibrate")) {
            checked = 1;
        } else if (alarmtypevalue.equals("Sound and Vibrate")) {
            checked = 2;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.TimePickerDialogTheme);
        builder.setTitle("Select Alarm Type");
        builder.setSingleChoiceItems(alaramType, checked, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                    case 0:
                        alarmtypevalue = "Sound";
                        break;
                    case 1:
                        alarmtypevalue = "Vibrate";
                        break;
                    case 2:
                        alarmtypevalue = "Sound and Vibrate";
                        break;
                }
            }
        }).setPositiveButton("Done!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                alarmtype.setText(alarmtypevalue);
                menuItemEnable(true);
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                levelDialog.dismiss();
            }
        });

        levelDialog = builder.create();
        levelDialog.show();
    }


    private void save() {
        alarmId = alarmModel.getId();
        prepareAlarmModel();
        alarmDatabase.updateAlarm(alarmId, alarmModel);


        Intent myIntent = new Intent(getBaseContext(), AlarmReceiver.class);
        myIntent.putExtra("eid", alarmId + "");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getBaseContext(), alarmId, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        long difference = calendar.getTimeInMillis() - System.currentTimeMillis();
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        showToast(prepareTwelveHourFormat.differerence(difference));
        long timeInMin = calendar.getTimeInMillis();
        if (repeatvalue.equals("never")) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMin, pendingIntent);
        } else {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, timeInMin, AlarmManager.INTERVAL_DAY, pendingIntent);
        }

    }

    private void prepareAlarmModel() {
        alarmModel.setNotes(editText.getText().toString());
        alarmModel.setIsSet("true");
        alarmModel.setIsAuto(isManual ? "false" : "true");
        alarmModel.setTime(calendar.get(Calendar.HOUR_OF_DAY) + "");
        alarmModel.setMin("" + calendar.get(Calendar.MINUTE));
        alarmModel.setAmpm(AM_PM);
        alarmModel.setRingtone(ringtone);
        alarmModel.setAlarmtype(alarmtypevalue);
        alarmModel.setRepeat(repeatvalue);

    }


    public void select_tone(View view) {

        Intent intent = new Intent(AddAlarmActivity.this, SelectAzanTone.class);
        intent.putExtra("id", alarmId);
        startActivity(intent);
        if (mPublisherInterstitialAd.isLoaded()) {
            mPublisherInterstitialAd.show();
            mPublisherInterstitialAd.loadAd(new AdRequest.Builder().build());
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        alarmModel = alarmDatabase.getAlarm(alarmModel.getId());
        initializeView();


    }


    @Override
    protected void onPause() {
//        isManual = true;
        super.onPause();

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.done, menu);
        item = menu.findItem(R.id.done);
        menuItemEnable(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        switch (item.getItemId()) {
            case R.id.done:
                if (mPublisherInterstitialAd.isLoaded()) {
                    mPublisherInterstitialAd.show();
                }
                save();
                NavUtils.navigateUpFromSameTask(this);
                return true;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);


    }

    private void menuItemEnable(boolean enable) {
        if (item != null) {
            if (enable) {
                item.setEnabled(true);
//                item.getIcon().setAlpha(130);
            } else {
                item.setEnabled(false);
//                item.getIcon().setAlpha(255);
            }
        }
    }


    private void showToast(String msg) {
        Toast.makeText(AddAlarmActivity.this, msg, Toast.LENGTH_SHORT).show();
    }


}

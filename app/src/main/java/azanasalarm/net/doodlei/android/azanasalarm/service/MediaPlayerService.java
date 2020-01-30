package azanasalarm.net.doodlei.android.azanasalarm.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.Nullable;

import java.io.IOException;

import azanasalarm.net.doodlei.android.azanasalarm.R;


public class MediaPlayerService extends Service {


    private MediaPlayer mediaPlayer;
    private Vibrator v;
    private long[] pattern = {0, 200, 150, 343, 230, 100, 0};


    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {

            int commandType = intent.getIntExtra("commandType", 0);
            String uri = intent.getStringExtra("tone");

            if (commandType == 1) {
                startPlaying(uri);
                setupVibration();
            } else if (commandType == 2) {
                setupVibration();
            } else if (commandType == 3) {
                startPlaying(uri);
            } else if (commandType == 4) {
                v.cancel();
                stopMedia();
            }
        }
        return super.onStartCommand(intent, flags, startId);

    }


    private void stopMedia() {


        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.setLooping(false);
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        this.stopSelf();

    }


    private void startPlaying(String alarmUri) {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            mediaPlayer.setLooping(true);

            if (alarmUri.equals("default")) {

                mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.makkah);
                mediaPlayer.start();
            } else {
                try {
                    mediaPlayer.setDataSource(getApplicationContext(), Uri.parse(alarmUri));
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }


    }

    private void setupVibration() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createWaveform(pattern, 0));
        } else {
            v.vibrate(pattern, 0);
        }
    }


}

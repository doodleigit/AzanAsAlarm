package azanasalarm.net.doodlei.android.azanasalarm.receiver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import azanasalarm.net.doodlei.android.azanasalarm.R;
import azanasalarm.net.doodlei.android.azanasalarm.database.AlarmDB;
import azanasalarm.net.doodlei.android.azanasalarm.model.AlarmModel;
import azanasalarm.net.doodlei.android.azanasalarm.ui.AzanListActivity;
import azanasalarm.net.doodlei.android.azanasalarm.util.PrepareTwelveHourFormat;

public class Salahreminder extends BroadcastReceiver {
    private static final String NOTIFICATION_CHANNEL_ID = "azaan";
    private AlarmModel alarmModel;
    private long[] pattern = {0, 200, 150, 343, 230, 100, 0};

    @Override
    public void onReceive(final Context context, Intent intent) {


        AlarmDB alarmDatabase = new AlarmDB(context);
        alarmModel = alarmDatabase.getAlarm(Integer.parseInt(intent.getStringExtra("eid")));
        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(),
                R.mipmap.new_ic_launcher);

        Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmUri == null) {
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            if (alarmUri == null) {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
        }

        String azantime = alarmModel.getName() + " azaan was at " + new PrepareTwelveHourFormat().twelveHourformat(Integer.parseInt(alarmModel.getTime()), Integer.parseInt(alarmModel.getMin()));

        // Create a new Notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setShowWhen(false)

                // Set the Notification color
                .setColor(context.getResources().getColor(R.color.colorPrimary))
                .setColorized(true)
                // Set the large and small icons
                .setLargeIcon(largeIcon)
                .setSmallIcon(R.drawable.adhan)
                .setVisibility(Notification.VISIBILITY_PUBLIC)

                .setContentText(azantime)
                .setContentTitle("Did you read your " + alarmModel.getName() + " salah")

                .setSound(alarmUri)
                .setVibrate(pattern);


        final Intent openPlayerIntent = new Intent(context, AzanListActivity.class);
        final PendingIntent contentIntent = PendingIntent.getActivity(context, 12345,
                openPlayerIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        notificationBuilder.setContentIntent(contentIntent);
        NotificationManager notificationManager = ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "NOTIFICATION_CHANNEL_NAME", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.BLUE);
            notificationChannel.enableVibration(true);

            notificationChannel.setSound(alarmUri, null);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(notificationChannel);
        }

        assert notificationManager != null;
        notificationManager.notify(1247, notificationBuilder.build());


    }


}
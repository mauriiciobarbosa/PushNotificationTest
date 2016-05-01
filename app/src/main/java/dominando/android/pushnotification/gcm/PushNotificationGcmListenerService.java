package dominando.android.pushnotification.gcm;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.google.android.gms.gcm.GcmListenerService;

import java.util.ArrayList;
import java.util.List;

import dominando.android.pushnotification.MainActivity;
import dominando.android.pushnotification.R;

/**
 * Created by mauricio on 01/05/16.
 */
public class PushNotificationGcmListenerService extends GcmListenerService {

    public List<String> messages = new ArrayList<>();
    private static final int NOTIFICATION_ID = 1;
    public static final String NEW_MESSAGE = "newMessage";
    public static final String NOTIFICATION = "dominando.android.pushnotification.gcm.NOTIFICATION";


    @Override
    public void onMessageReceived(String from, Bundle data) {
        super.onMessageReceived(from, data);
        if (data != null) {
            String message = data.getString("message");
            showNotification(message);
            notifyActivity(message);
        }
    }

    private void notifyActivity(String message) {
        Intent it = new Intent(NOTIFICATION);
        it.putExtra(NEW_MESSAGE, message);
        sendBroadcast(it);
    }

    private void showNotification(String message) {
        NotificationManagerCompat nm = NotificationManagerCompat.from(this);
        Intent it = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(it);
        PendingIntent pit = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pit)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.texto_notificacao))
                .setContentText(message);
        nm.notify(NOTIFICATION_ID, mBuilder.build());
    }

}

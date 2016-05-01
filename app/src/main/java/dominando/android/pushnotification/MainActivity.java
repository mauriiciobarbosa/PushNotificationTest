package dominando.android.pushnotification;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.ArrayList;
import java.util.List;

import dominando.android.pushnotification.gcm.PushNotificationGcmListenerService;
import dominando.android.pushnotification.gcm.PushNotificationIdListenerService;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_PLAY_SERVICES = 1;

    ListView messageList;
    ArrayAdapter<String> adapter;
    List<String> messages;
    BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        messageList = (ListView) findViewById(R.id.messageList);
        messages = new ArrayList<>();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, messages);
        messageList.setAdapter(adapter);
        receiver = new MyReceiver();

        iniciarGooglePlayServices();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, new IntentFilter(PushNotificationGcmListenerService.NOTIFICATION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    private void iniciarGooglePlayServices() {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int resultCode = api.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (api.isUserResolvableError(resultCode)) {
                Dialog dialog = api.getErrorDialog(this, resultCode, REQUEST_PLAY_SERVICES);
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                });
                dialog.show();
            } else {
                Toast.makeText(this, R.string.gcm_nao_suportado, Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Intent it = new Intent(this, PushNotificationIdListenerService.class);
            it.putExtra(PushNotificationIdListenerService.EXTRA_REGISTRAR, true);
            startService(it);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PLAY_SERVICES) {
            iniciarGooglePlayServices();
        }
    }

    public void sync(View v) {

    }

    class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String newMessage = intent.getStringExtra(PushNotificationGcmListenerService.NEW_MESSAGE);
            if (newMessage != null) {
                messages.add(newMessage);
                adapter.notifyDataSetChanged();
            }
        }
    }

}

package dominando.android.pushnotification.gcm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.google.android.gms.iid.InstanceIDListenerService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import dominando.android.pushnotification.R;

/**
 * Created by mauricio on 01/05/16.
 */
public class PushNotificationIdListenerService extends InstanceIDListenerService {
    public static final String URL_DO_SERVIDOR = "http://192.168.0.10:8080/PushNotificationServer/GcmRegister";
    public static final String REGISTRATION_ID = "registrationId";
    public static final String ENVIADO_PRO_SERVIDOR = "enviadoProServidor";
    public static final String EXTRA_REGISTRAR = "registrar";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent.getBooleanExtra(EXTRA_REGISTRAR, false)) {
            if (getRegistrationId() == null) {
                obterToken();
            } else if (!enviadoServidor()) {
                enviarRegistrationParaServidor(getRegistrationId());
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        setRegistrationId(null);
        setEnviadoProServidor(false);
        obterToken();
    }

    private void obterToken() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    InstanceID instanceID = InstanceID.getInstance(PushNotificationIdListenerService.this);
                    String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                            GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                    setRegistrationId(token);
                    enviarRegistrationParaServidor(token);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void enviarRegistrationParaServidor(final String token) {
        new Thread() {
            @Override
            public void run() {
                try {
                    URL url = new URL(URL_DO_SERVIDOR);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    OutputStream os = connection.getOutputStream();
                    os.write(("regID=" + token).getBytes());
                    os.flush();
                    os.close();
                    connection.connect();
                    int responseCode = connection.getResponseCode();

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        setEnviadoProServidor(true);
                    } else {
                        throw new RuntimeException("Erro ao salvar no servidor");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private String getRegistrationId() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.getString(REGISTRATION_ID, null);
    }

    private void setRegistrationId(String token) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(REGISTRATION_ID, token);
        editor.apply();
    }

    private boolean enviadoServidor() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.getBoolean(ENVIADO_PRO_SERVIDOR, false);
    }

    private void setEnviadoProServidor(boolean enviado) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(ENVIADO_PRO_SERVIDOR, enviado);
        editor.apply();
    }

}

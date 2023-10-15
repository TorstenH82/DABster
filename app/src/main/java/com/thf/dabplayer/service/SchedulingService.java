package com.thf.dabplayer.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Build;
import android.os.StrictMode;
import android.util.Log;
import com.thf.dabplayer.activity.AlarmReceiver;
import com.thf.dabplayer.activity.ShowNote;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

/* renamed from: com.ex.dabplayer.pad.service.SchedulingService */
/* loaded from: classes.dex */
public class SchedulingService extends IntentService {
    public SchedulingService() {
        super("SchedulingService");
    }

    @Override // android.app.IntentService
    protected void onHandleIntent(Intent intent) {
        String message;
        HttpURLConnection conn;
        OutputStream os;
        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        try {
            URL url = new URL("http://androidautoshop.com/dab/aas/version.php");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("version", 1);
            message = jsonObject.toString();
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setFixedLengthStreamingMode(message.getBytes().length);
            conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
            conn.connect();
            os = new BufferedOutputStream(conn.getOutputStream());
       
            os.write(message.getBytes());
            Log.d("SEND TO SERVER:", String.valueOf(message.getBytes()));
            os.flush();
            InputStream is = conn.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuffer response = new StringBuffer();
            while (true) {
                String inputLine = in.readLine();
                if (inputLine == null) {
                    break;
                }
                response.append(inputLine);
            }
            int serverversion = Integer.parseInt(response.toString());
            Log.d("SERVER Response:", String.valueOf(Integer.parseInt(response.toString())));
            os.close();
            is.close();
            conn.disconnect();
            if (serverversion > 1) {
                Intent myIntent = new Intent(this, ShowNote.class);
                myIntent.addFlags(268435456);
                startActivity(myIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlarmReceiver.completeWakefulIntent(intent);
        }
        AlarmReceiver.completeWakefulIntent(intent);
    }
}
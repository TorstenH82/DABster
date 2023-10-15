package com.thf.dabplayer.activity;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
 
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.os.EnvironmentCompat;
import com.thf.dabplayer.R;
import com.thf.dabplayer.service.DabService;
import com.thf.dabplayer.utils.C0162a;
import com.thf.dabplayer.utils.Strings;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

/* renamed from: com.ex.dabplayer.pad.activity.SettingsAboutActivity */
/* loaded from: classes.dex */
public class SettingsAboutActivity extends Activity {
    @Override // android.app.Activity
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Intent mainActivityStartIntent;
        String action;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_about);
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            TextView versionname = (TextView) findViewById(R.id.versionname);
            if (versionname != null) {
                versionname.setText(pInfo.versionName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        TextView textView = (TextView) findViewById(R.id.text_credits_authors);
        if (textView != null) {
            textView.setMovementMethod(LinkMovementMethod.getInstance());
        }
        TextView textView2 = (TextView) findViewById(R.id.text_credits_translations);
        if (textView2 != null) {
            textView2.setMovementMethod(LinkMovementMethod.getInstance());
        }
        TextView textView3 = (TextView) findViewById(R.id.startaction);
        if (textView3 != null) {
            textView3.setText(EnvironmentCompat.MEDIA_UNKNOWN);
            WeakReference<Intent> mainActivityStartIntentRef = Player.getMainActivityStartIntentWeakRef();
            if (mainActivityStartIntentRef != null && (mainActivityStartIntent = mainActivityStartIntentRef.get()) != null && (action = mainActivityStartIntent.getAction()) != null) {
                if (action.equals("android.hardware.usb.action.USB_DEVICE_ATTACHED")) {
                    textView3.setText("USB DEVICE ATTACHED");
                } else if (action.equals("android.intent.action.MAIN") && mainActivityStartIntent.getCategories() != null && mainActivityStartIntent.getCategories().contains("android.intent.category.LAUNCHER")) {
                    textView3.setText("LAUNCHER");
                }
            }
        }
        Button button = (Button) findViewById(getResources().getIdentifier("btnHints", DabService.EXTRA_ID, getPackageName()));
        if (button != null) {
            button.setOnClickListener(new View.OnClickListener() { // from class: com.ex.dabplayer.pad.activity.SettingsAboutActivity.1
                @Override // android.view.View.OnClickListener
                public void onClick(View v) {
                    AssetManager assetManager = SettingsAboutActivity.this.getApplicationContext().getAssets();
                    if (assetManager != null) {
                        try {
                            try {
                                InputStream inputStream = assetManager.open("hints.jpg");
                                FileOutputStream fileOutputStream = new FileOutputStream(Strings.DAB_path() + File.separator + "hints.jpg");
                                byte[] buff = new byte[inputStream.available()];
                                inputStream.read(buff);
                                fileOutputStream.write(buff);
                                fileOutputStream.close();
                                inputStream.close();
                                File nomedia = new File(Strings.DAB_path() + File.separator + ".nomedia");
                                if (!nomedia.exists()) {
                                    try {
                                        nomedia.createNewFile();
                                    } catch (IOException | SecurityException e2) {
                                        e2.printStackTrace();
                                    }
                                }
                                Intent intent = new Intent();
                                intent.setAction("android.intent.action.VIEW");
                                intent.setDataAndType(Uri.parse("file://" + Strings.DAB_path() + File.separator + "hints.jpg"), "image/*");
                                SettingsAboutActivity.this.startActivity(intent);
                            } catch (IOException e3) {
                                Toast.makeText(SettingsAboutActivity.this, "Error displaying hints", 1).show();
                            }
                        } catch (ActivityNotFoundException e4) {
                            Toast.makeText(SettingsAboutActivity.this, "Error displaying hints", 1).show();
                        }
                    }
                }
            });
        }
    }

    @Override // android.app.Activity
    protected void onResume() {
        super.onResume();
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        C0162a.m9a("display metrics: " + metrics.toString());
        TextView textView = (TextView) findViewById(R.id.displaymetrics);
        if (textView != null) {
            CharSequence text = "" + metrics.widthPixels + "x" + metrics.heightPixels + " @ " + metrics.densityDpi + " dpi";
            textView.setText(text);
        }
    }
}

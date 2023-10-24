package com.thf.dabplayer.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.hardware.usb.UsbDevice;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import com.thf.dabplayer.R;
import com.thf.dabplayer.utils.C0162a;
import com.thf.dabplayer.utils.SharedPreferencesHelper;
import com.thf.dabplayer.utils.UsbDeviceHandling;
/* renamed from: com.ex.dabplayer.pad.activity.MainActivity */
/* loaded from: classes.dex */
public class MainActivity extends Activity {

  /* renamed from: a */
  private Context context;

  /* renamed from: e */
  private SimpleDialog progressDialog;

  private Intent startedByIntent;
  private UsbDeviceHandling usbDeviceHandling = null;
  private UsbDeviceHandling.OnUsbDeviceHandlingResultListener usbDeviceResultListener =
      new UsbDeviceHandling.OnUsbDeviceHandlingResultListener() { // from class:
        // com.ex.dabplayer.pad.activity.MainActivity.1
        @Override // com.thf.dabplayer.utils.UsbDeviceHandling.OnUsbDeviceHandlingResultListener
        public void onUsbDeviceFound(UsbDevice usbDevice) {
          MainActivity.this.usbDeviceHandling.stop();
          MainActivity.this.startPlayerWithUsbDevice(usbDevice);
        }

        @Override // com.thf.dabplayer.utils.UsbDeviceHandling.OnUsbDeviceHandlingResultListener
        public void onNoUsbDevice() {
          MainActivity.this.toastAndFinish(context.getString(R.string.ConnectDevice));
        }

        @Override // com.thf.dabplayer.utils.UsbDeviceHandling.OnUsbDeviceHandlingResultListener
        public void onNoUsbPermissionGranted() {
          MainActivity.this.toastAndFinish("No USB permission");
        }

        @Override // com.thf.dabplayer.utils.UsbDeviceHandling.OnUsbDeviceHandlingResultListener
        public void onUsbConnectAttemptStarted(int attempt, int maxAttempts) {
          String text = ""; //MainActivity.this.getResources().getString(R.string.Connecting);
          if (attempt > 1) {
            updateProgress(
                TextUtils.concat(
                        text,
                        MainActivity.this
                            .getResources()
                            .getString(
                                R.string.attemptXofY,
                                Integer.valueOf(attempt),
                                Integer.valueOf(maxAttempts)))
                    .toString());
          }
        }

        @Override // com.thf.dabplayer.utils.UsbDeviceHandling.OnUsbDeviceHandlingResultListener
        public void onUsbPermissionRequestAttemptStarted(int attempt, int maxAttempts) {
          String text = MainActivity.this.getResources().getString(R.string.UsbPermission);
          if (attempt > 1) {
            text =
                TextUtils.concat(
                        text,
                        MainActivity.this
                            .getResources()
                            .getString(
                                R.string.attemptXofY,
                                Integer.valueOf(attempt),
                                Integer.valueOf(maxAttempts)))
                    .toString();
          }
          updateProgress(text);
        }

        private void updateProgress(final String str) {
          if (MainActivity.this.progressDialog != null) {
            MainActivity.this.runOnUiThread(
                new Runnable() { // from class: com.ex.dabplayer.pad.activity.MainActivity.1.1
                  @Override // java.lang.Runnable
                  public void run() {
                    MainActivity.this.progressDialog.setMessage(str);
                  }
                });
          }
        }
      };
  private static int DAB_USB_VID = 5824;
  private static int DAB_USB_PID = 1500;

  /* JADX INFO: Access modifiers changed from: private */
  public void toastAndFinish(String toastText) {
    if (toastText != null) {
      C0162a.m9a("Toast: " + toastText);
      Toast.makeText(this.context, toastText, 1).show();
    }
    this.progressDialog.dismiss();
    finish();
  }

  @Override // android.app.Activity
  protected void onCreate(Bundle bundle) {
    super.onCreate(bundle);

    if ("RMX3301EEAx".equals(Build.PRODUCT)) {
      Intent intentTest = new Intent();
      intentTest.setClass(this, Player.class);
      // intentTest.putExtra("UsbDevice", usbDevice);
      intentTest.putExtra("StartedByIntent", this.startedByIntent);
      intentTest.addFlags(536870912);
      startActivity(intentTest);
    }

    C0162a.m9a("MainActivity:onCreate");
    C0162a.m9a("board: " + Build.BOARD);
    C0162a.m9a("device: " + Build.DEVICE);
    C0162a.m9a("OS release: " + Build.VERSION.RELEASE);
    C0162a.m9a("product: " + Build.PRODUCT);
    try {
      PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
      C0162a.m9a("App version: " + pInfo.versionName);
    } catch (Exception e) {
      e.printStackTrace();
    }
    this.startedByIntent = getIntent();
    C0162a.m9a("Started by: " + this.startedByIntent.toString());
    this.context = getApplicationContext();

    this.progressDialog = new SimpleDialog(this, "Connecting");
    this.progressDialog.show();

    //this.progressDialog = ProgressDialog.show(this, "", "Connecting...", true, true);
    //this.progressDialog.setIndeterminateDrawable(
    //    getResources().getDrawable(R.anim.progress_dialog_anim));
    //setContentView(R.layout.main);
    this.usbDeviceHandling =
        new UsbDeviceHandling(
            getApplicationContext(), DAB_USB_VID, DAB_USB_PID, this.usbDeviceResultListener);
    this.usbDeviceHandling.start();

    //this.progressDialog.show();

    //    Intent intent = new Intent("com.microntek.app");
    //    intent.putExtra("app", DabService.SENDER_DAB);
    //    intent.putExtra("state", "ENTER");
    //    this.context.sendBroadcast(intent);
    //    Intent intent2 = new Intent("com.microntek.bootcheck");
    //    intent2.putExtra("class", DabService.SENDER_DAB);
    //    this.context.sendBroadcast(intent2);
  }

  @Override // android.app.Activity
  protected void onDestroy() {
    C0162a.m9a("MainActivity:onDestroy");
    super.onDestroy();
    this.usbDeviceHandling.stop();
    this.progressDialog.dismiss();
  }

  @Override // android.app.Activity
  protected void onPause() {
    C0162a.m9a("MainActivity:onPause");
    super.onPause();
    this.usbDeviceHandling.pause();
  }

  @Override // android.app.Activity
  protected void onResume() {
    C0162a.m9a("MainActivity:onResume");
    super.onResume();
    if (isPlayerRunning()) {
      bringPlayerActivityToFrontAndFinish();
    } else {
      this.usbDeviceHandling.resume();
    }
  }

  @Override // android.app.Activity
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    C0162a.m9a("MainActivity:onNewIntent " + intent.toString());
    this.usbDeviceHandling.reset();
    this.usbDeviceHandling.resume();
  }

  private boolean isPlayerRunning() {
    boolean isPlayerRunning = Player.getPlayerHandler() != null;
    C0162a.m9a("isPlayerRunning: " + isPlayerRunning);
    return isPlayerRunning;
  }

  private void bringPlayerActivityToFrontAndFinish() {
    Intent intent = new Intent();
    intent.setClass(this, Player.class);
    intent.setFlags(131072);
    try {
      startActivity(intent);
    } catch (ActivityNotFoundException e) {
      C0162a.m9a(e.toString());
    }
    this.progressDialog.dismiss();
    finish();
  }

  public void startPlayerWithUsbDevice(UsbDevice usbDevice) {

    boolean startOnUsbAttached = SharedPreferencesHelper.getInstance().getBoolean("startUsb");
    if ("android.hardware.usb.action.USB_DEVICE_ATTACHED".equals(this.startedByIntent.getAction())
        && !startOnUsbAttached) {
      C0162a.m9a("start on USB device attached NOT allowed by settings");
      toastAndFinish(null);
      return;
    }
    Intent intent = new Intent();
    intent.setClass(this, Player.class);
    intent.putExtra("UsbDevice", usbDevice);
    intent.putExtra("StartedByIntent", this.startedByIntent);
    intent.addFlags(536870912);
    startActivity(intent);
    this.progressDialog.dismiss();
    finish();
  }

  public boolean startedByUsbAttachedIntent() {
    String action;
    if (this.startedByIntent == null || (action = this.startedByIntent.getAction()) == null) {
      return false;
    }
    boolean ret = action.equals("android.hardware.usb.action.USB_DEVICE_ATTACHED");
    return ret;
  }
}

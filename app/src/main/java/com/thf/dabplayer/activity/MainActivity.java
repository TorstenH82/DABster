package com.thf.dabplayer.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import com.thf.dabplayer.R;
import com.thf.dabplayer.dab.DabSubChannelInfo;
import com.thf.dabplayer.dab.LogoDbAssets;
import com.thf.dabplayer.utils.Logger;
import com.thf.dabplayer.utils.SharedPreferencesHelper;
import com.thf.dabplayer.utils.UsbDeviceHandling;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
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
          String text = MainActivity.this.getResources().getString(R.string.Connecting);
          if (attempt > 1) {
            updateProgress(
                text
                    + " "
                    + MainActivity.this
                        .getResources()
                        .getString(
                            R.string.attemptXofY,
                            Integer.valueOf(attempt),
                            Integer.valueOf(maxAttempts)));
          }
        }

        @Override // com.thf.dabplayer.utils.UsbDeviceHandling.OnUsbDeviceHandlingResultListener
        public void onUsbPermissionRequestAttemptStarted(int attempt, int maxAttempts) {
          String text = MainActivity.this.getResources().getString(R.string.UsbPermission);
          if (attempt > 1) {
            text =
                text
                    + " "
                    + MainActivity.this
                        .getResources()
                        .getString(
                            R.string.attemptXofY,
                            Integer.valueOf(attempt),
                            Integer.valueOf(maxAttempts));
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
      Logger.d("Toast: " + toastText);
      Toast.makeText(this.context, toastText, 1).show();
    }
    this.progressDialog.dismiss();
    finish();
  }

  /*
  final int PERMISSION_REQUEST_CODE = 112;
  public void getNotificationPermission() {
    try {
      if (Build.VERSION.SDK_INT > 32) {
        ActivityCompat.requestPermissions(
            this, new String[] {Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_CODE);
      }
    } catch (Exception e) {
    }
  }

  @Override
   public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
       super.onRequestPermissionsResult(requestCode, permissions, grantResults);

       switch (requestCode) {
           case PERMISSION_REQUEST_CODE:
               // If request is cancelled, the result arrays are empty.
               if (grantResults.length > 0 &&
                       grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                   // allow
               }  else {
                   //deny
               }
               return;
       }

   }
   */

  @Override // android.app.Activity
  protected void onCreate(Bundle bundle) {
    super.onCreate(bundle);

    /*
    if (Build.VERSION.SDK_INT > 32) {
      if (!shouldShowRequestPermissionRationale("112")) {
        getNotificationPermission();
      }
    }
    */

    if ("RMX3301EEAx".equals(Build.PRODUCT)) {

      Intent intentTest = new Intent();
      intentTest.setClass(this, PlayerActivity.class);
      // intentTest.putExtra("UsbDevice", usbDevice);
      intentTest.putExtra("StartedByIntent", this.startedByIntent);
      intentTest.addFlags(536870912);
      startActivity(intentTest);

      /*
      List<DabSubChannelInfo> list = new ArrayList<>();
      DabSubChannelInfo dummy = new DabSubChannelInfo();
      dummy.mLabel = "This is a very long station name";
      list.add(dummy);
      dummy = new DabSubChannelInfo();
      dummy.mLabel = "Absolut HOT";
      list.add(dummy);
      dummy = new DabSubChannelInfo();
      dummy.mLabel = "Beats Radio";
      list.add(dummy);

      Intent intentTest = new Intent();
      intentTest.setClass(this, PopupActivity.class);
      // intentTest.putExtra("UsbDevice", usbDevice);

      intentTest.putExtra("stationList", (Serializable) list);
      intentTest.addFlags(536870912);
      startActivity(intentTest);
 */           
    }

    Logger.d("MainActivity:onCreate");
    Logger.d("board: " + Build.BOARD);
    Logger.d("device: " + Build.DEVICE);
    Logger.d("OS release: " + Build.VERSION.RELEASE);
    Logger.d("product: " + Build.PRODUCT);
    try {
      PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
      Logger.d("App version: " + pInfo.versionName);
    } catch (Exception e) {
      e.printStackTrace();
    }

    this.startedByIntent = getIntent();
    Logger.d("Started by: " + this.startedByIntent.toString());

    this.context = getApplicationContext();
    this.progressDialog = new SimpleDialog(this); // , "Connecting");
    this.progressDialog.setMessage(this.getResources().getString(R.string.Connecting));
    this.progressDialog.showProgress(true);
    this.progressDialog.show();

    this.usbDeviceHandling =
        new UsbDeviceHandling(
            getApplicationContext(), DAB_USB_VID, DAB_USB_PID, this.usbDeviceResultListener);
    this.usbDeviceHandling.start();
  }

  @Override // android.app.Activity
  protected void onDestroy() {
    Logger.d("MainActivity:onDestroy");
    super.onDestroy();
    this.usbDeviceHandling.stop();
    this.progressDialog.dismiss();
  }

  @Override // android.app.Activity
  protected void onPause() {
    Logger.d("MainActivity:onPause");
    super.onPause();
    this.usbDeviceHandling.pause();
  }

  @Override // android.app.Activity
  protected void onResume() {
    Logger.d("MainActivity:onResume");
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
    Logger.d("MainActivity:onNewIntent " + intent.toString());
    this.usbDeviceHandling.reset();
    this.usbDeviceHandling.resume();
  }

  private boolean isPlayerRunning() {
    boolean isPlayerRunning = PlayerActivity.getPlayerHandler() != null;
    Logger.d("isPlayerRunning: " + isPlayerRunning);
    return isPlayerRunning;
  }

  private void bringPlayerActivityToFrontAndFinish() {
    Intent intent = new Intent();
    intent.setClass(this, PlayerActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
    try {
      startActivity(intent);
    } catch (ActivityNotFoundException e) {
      Logger.d(e.toString());
    }
    this.progressDialog.dismiss();
    finish();
  }

  public void startPlayerWithUsbDevice(UsbDevice usbDevice) {

    boolean startOnUsbAttached = SharedPreferencesHelper.getInstance().getBoolean("startUsb");
    if ("android.hardware.usb.action.USB_DEVICE_ATTACHED".equals(this.startedByIntent.getAction())
        && !startOnUsbAttached) {
      Logger.d("start on USB device attached NOT allowed by settings");
      toastAndFinish(null);
      return;
    }
    Intent intent = new Intent();
    intent.setClass(this, PlayerActivity.class);
    intent.putExtra("UsbDevice", usbDevice);
    intent.putExtra("StartedByIntent", this.startedByIntent);
    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
    startActivity(intent);
    this.progressDialog.dismiss();
    finish();
  }

  public boolean startedByUsbAttachedIntent() {
    String action;
    if (this.startedByIntent == null || (action = this.startedByIntent.getAction()) == null) {
      return false;
    }
    return action.equals("android.hardware.usb.action.USB_DEVICE_ATTACHED");
  }
}

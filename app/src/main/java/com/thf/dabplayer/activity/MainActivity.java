package com.thf.dabplayer.activity;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.hardware.usb.UsbDevice;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;
import com.thf.dabplayer.R;
import com.thf.dabplayer.utils.Logger;
import com.thf.dabplayer.utils.SharedPreferencesHelper;
import com.thf.dabplayer.utils.UsbDeviceHandling;

/* renamed from: com.ex.dabplayer.pad.activity.MainActivity */
/* loaded from: classes.dex */
public class MainActivity extends Activity {

  /* renamed from: a */
  private Context context;

  /* renamed from: e */
  private SimpleDialog progressDialog;
  private UsbDevice usbDevice;

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

    if (usbDevice != null) {
      this.usbDevice = usbDevice;
    }

    boolean startOnUsbAttached = SharedPreferencesHelper.getInstance().getBoolean("startUsb");
    if ("android.hardware.usb.action.USB_DEVICE_ATTACHED".equals(this.startedByIntent.getAction())
        && !startOnUsbAttached) {
      Logger.d("start on USB device attached NOT allowed by settings");
      toastAndFinish(null);
      return;
    }

    if (!Settings.canDrawOverlays(this) && !permissionOverlayRequested) {
      Intent intent =
          new Intent(
              Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
      startActivityForResult(intent, PERMISSION_OVERLAY);
      return;
    }
        
        
        
        

    Intent intent = new Intent();
    intent.setClass(this, PlayerActivity.class);
    intent.putExtra("UsbDevice", this.usbDevice);
    intent.putExtra("StartedByIntent", this.startedByIntent);
    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
    startActivity(intent);
    this.progressDialog.dismiss();
    finish();
  }

  private static final int PERMISSION_OVERLAY = 1;
  private boolean permissionOverlayRequested = false;

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == PERMISSION_OVERLAY) {
      /*
      if (resultCode == Activity.RESULT_OK) {
        String result = data.getStringExtra("result");
      }
      if (resultCode == Activity.RESULT_CANCELED) {
        // Write your code if there's no result
      }
      */
      if (!Settings.canDrawOverlays(this)) {
        SimpleDialog sd =
            new SimpleDialog(
                MainActivity.this,
                context.getString(R.string.Permission),
                new SimpleDialog.SimpleDialogListener() {

                  @Override
                  public void onClick(boolean positive, int selection) {
                    if (positive) {
                      MainActivity.this.permissionOverlayRequested = false;
                    } else {
                      MainActivity.this.permissionOverlayRequested = true;
                    }
                    MainActivity.this.startPlayerWithUsbDevice(null);
                  }
                });

        sd.setMessage(context.getString(R.string.PermissionOverlayHint));
        sd.setPositiveButton(context.getString(R.string.ok));
        sd.setNegativeButton(context.getString(R.string.ignore));
        sd.show();
      } else {
         MainActivity.this.startPlayerWithUsbDevice(null);
      }
    }
  }

  /*
  public boolean startedByUsbAttachedIntent() {
    String action;
    if (this.startedByIntent == null || (action = this.startedByIntent.getAction()) == null) {
      return false;
    }
    return action.equals("android.hardware.usb.action.USB_DEVICE_ATTACHED");
  }
    */
}

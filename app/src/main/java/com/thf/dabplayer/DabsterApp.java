package com.thf.dabplayer;

import android.app.Application;
import com.thf.dabplayer.utils.SharedPreferencesHelper;

/* loaded from: classes.dex */
public class DabsterApp extends Application {

  private static DabsterApp sInstance;
  private boolean isPopupActivityRunning = false;

  public void setRunned() {}

  public static DabsterApp getInstance() {
    return sInstance;
  }

  @Override // android.app.Application
  public void onCreate() {
    super.onCreate();
    sInstance = this;
    SharedPreferencesHelper sharedPreferencesHelper =
        new SharedPreferencesHelper(getApplicationContext());
  }

  @Override // android.app.Application
  public void onTerminate() {
    super.onTerminate();
  }

  public void setPopupActivityRunning(boolean running) {
    this.isPopupActivityRunning = running;
  }

  public boolean isPopupActivityRunning() {
    return this.isPopupActivityRunning;
  }
}

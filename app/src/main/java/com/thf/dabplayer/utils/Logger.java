package com.thf.dabplayer.utils;

import android.util.Log;

public class Logger {

  public static void d(String message) {
    Log.d("Dabster", message);
  }

  public static void e(String message, Throwable throwable) {
    Log.e("Dabster", message, throwable);
  }
}

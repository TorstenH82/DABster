package com.thf.dabplayer.utils;

import android.content.Context;
import static android.content.Context.MODE_PRIVATE;
import android.content.SharedPreferences;

public class SharedPreferencesHelper {
  private static SharedPreferencesHelper sharedPreferencesHelper;
  private Context context;
  private SharedPreferences sharedPreferences;
  private String value;

  public SharedPreferencesHelper(Context context) {
    this.context = context;
    this.sharedPreferences = context.getSharedPreferences("USERDATA", MODE_PRIVATE);
    sharedPreferencesHelper = this;
  }

  public SharedPreferencesHelper(
      Context context, SharedPreferences.OnSharedPreferenceChangeListener listener) {
    this.context = context;
    this.sharedPreferences = context.getSharedPreferences("USERDATA", MODE_PRIVATE);
    this.sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
  }

  public static SharedPreferencesHelper getInstance() {
    return SharedPreferencesHelper.sharedPreferencesHelper;
  }

  public void unregisterOnSharedPreferenceChangeListener(
      SharedPreferences.OnSharedPreferenceChangeListener listener) {
    this.sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
  }

  private String getDefaultString(String key) {
    key = "pref_" + key;
    int resId = this.context.getResources().getIdentifier(key, "string", context.getPackageName());
    if (resId == 0) {
      return "";
    } else {
      return context.getString(resId);
    }
  }

  public String getString(String key) {
    return this.sharedPreferences.getString(key, getDefaultString(key));
  }

  private int getDefaultInt(String key) {
    key = "pref_" + key;
    int resId = context.getResources().getIdentifier(key, "string", context.getPackageName());
    if (resId == 0) {
      return -99;
    } else {
      return Integer.parseInt(context.getString(resId));
    }
  }

  public int getInteger(String key) {
    return sharedPreferences.getInt(key, getDefaultInt(key));
  }

  public void setInteger(String key, int valueInt) {
    sharedPreferences.edit().putInt(key, valueInt).apply();
  }

  private boolean getDefaultBool(String key) {
    key = "pref_" + key;
    int resId = context.getResources().getIdentifier(key, "string", context.getPackageName());
    if (resId == 0) {
      return false;
    } else {
      return Boolean.parseBoolean(context.getString(resId));
    }
  }

  public boolean getBoolean(String key) {
    return sharedPreferences.getBoolean(key, getDefaultBool(key));
  }
}

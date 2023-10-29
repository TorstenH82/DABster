package com.thf.dabplayer.activity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Bundle;

import android.view.MenuItem;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

import androidx.preference.SeekBarPreference;
import com.thf.dabplayer.R;

/* loaded from: classes.dex */
public class SettingsActivity extends AppCompatActivity {
  private static Activity activity;
  private static Context context;
  private static PrefFragment settingsFragment;

  /* loaded from: classes.dex */
  public static class PrefFragment extends PreferenceFragmentCompat
      implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override // android.content.SharedPreferences.OnSharedPreferenceChangeListener
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
      SeekBarPreference seekBarPreference;
      switch (key) {
        case "presetPages":
          seekBarPreference = findPreference(key);
          seekBarPreference.setSummary(seekBarPreference.getValue() + "");
          break;
      }
    }

    @Override // androidx.fragment.app.Fragment
    public void onResume() {
      super.onResume();
      getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override // androidx.fragment.app.Fragment
    public void onPause() {
      super.onPause();
      getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override // androidx.preference.PreferenceFragmentCompat
    public void onCreatePreferences(Bundle bundle, String str) {
      getPreferenceManager().setSharedPreferencesName("USERDATA");
      setPreferencesFromResource(R.xml.preferences, str);

      SeekBarPreference presetPages = findPreference("presetPages");
      presetPages.setMin(1);
      presetPages.setUpdatesContinuously(true);
      presetPages.setSummary(presetPages.getValue() + "");

      try {
        PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        findPreference("prefAbout")
            .setSummary(
                "version "
                    + pInfo.versionName
                    + "\n\nManufacturer: "
                    + android.os.Build.MANUFACTURER
                    + "\nProduct: "
                    + android.os.Build.PRODUCT
                    + "\nDevice: "
                    + android.os.Build.DEVICE
                    + "\nBoard: "
                    + android.os.Build.BOARD);
      } catch (Exception ignore) {
      }
    }
  }

  /* JADX INFO: Access modifiers changed from: protected */
  @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity,
  // androidx.core.app.ComponentActivity, android.app.Activity
  public void onCreate(Bundle bundle) {
    super.onCreate(bundle);
    context = getApplicationContext();
    activity = this;
    setContentView(R.layout.activity_settings);

    settingsFragment = new PrefFragment();
    getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.frmSettings, settingsFragment)
        .commit();

    // calling the action bar
    ActionBar actionBar = getSupportActionBar();

    // showing the back button in action bar
    actionBar.setDisplayHomeAsUpEnabled(true);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        this.finish();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }
}
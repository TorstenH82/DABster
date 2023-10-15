package com.thf.dabplayer.activity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import com.thf.dabplayer.R;
import com.thf.dabplayer.utils.ServiceFollowing;

/* renamed from: com.ex.dabplayer.pad.activity.SettingsGeneralActivity */
/* loaded from: classes.dex */
public class SettingsGeneralActivity extends Activity {
  private Context mContext = null;
  @IdRes private final int R_id_switchStartOnUsbAttached = R.id.switchStartOnUsbAttached;
  @IdRes private final int R_id_textStartOnUsbAttached = R.id.textStartOnUsbAttached;

  @StringRes
  private final int R_string_startOnUsbAttached_Off = R.string.settings_text_startOnUsbAttached_Off;

  @StringRes
  private final int R_string_startOnUsbAttached_On = R.string.settings_text_startOnUsbAttached_On;

  @IdRes private final int R_id_switchServiceFollowing = R.id.switchServiceFollowing;

  @IdRes
  private final int R_id_switchOnStartByUsbGotoBackground = R.id.switchOnStartByUsbGotoBackground;

  @IdRes
  private final int R_id_textSwitchOnStartByUsbGotoBackground =
      R.id.textSwitchOnStartByUsbGotoBackground;

  @IdRes private final int R_id_switchSendBroadcastIntent = R.id.switchSendBroadcastIntent;
  @IdRes private final int R_id_switchSwapPrevNext = R.id.switchSwapPrevNextFromSWC;
  private final CompoundButton.OnCheckedChangeListener switchCheckedListener =
      new CompoundButton.OnCheckedChangeListener() { // from class:
        // com.ex.dabplayer.pad.activity.SettingsGeneralActivity.1
        @Override // android.widget.CompoundButton.OnCheckedChangeListener
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
          if (buttonView.getId() == R.id.switchStartOnUsbAttached) {
            SettingsGeneralActivity.this.setStartOnUsbAttached(isChecked);
            return;
          } else if (buttonView.getId() == R.id.switchServiceFollowing) {
              /* 2131427383 */
            SettingsGeneralActivity.this.setServiceFollowing(isChecked);
            return;
          } else if (buttonView.getId() == R.id.switchOnStartByUsbGotoBackground) {
              /* 2131427418 */
            SettingsGeneralActivity.this.setOnStartByUsbGotoBackground(isChecked);
            return;
          } else if (buttonView.getId() == R.id.switchSendBroadcastIntent) {
              /* 2131427433 */
            SettingsGeneralActivity.this.setSendBroadcastIntent(isChecked);
            return;
          } else if (buttonView.getId() == R.id.switchSwapPrevNextFromSWC) {
              /* 2131427472 */
            SettingsGeneralActivity.this.setSwapPrevNext(isChecked);
            return;
          } else {
            return;
          }
        }
      };

  @Override // android.app.Activity
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    View layout;
    super.onCreate(savedInstanceState);
    setContentView(R.layout.settings_general);
    this.mContext = getApplicationContext();
    SharedPreferences pref_playing = this.mContext.getSharedPreferences("playing", 0);
    SharedPreferences pref_settings =
        this.mContext.getSharedPreferences(SettingsActivity.prefname_settings, 0);
    Switch switchStartOnUsbAttached = (Switch) findViewById(R.id.switchStartOnUsbAttached);
    boolean stateStartOnUsbAttached =
        pref_settings.getBoolean(SettingsActivity.pref_key_startOnUsbAttached, true);
    if (switchStartOnUsbAttached != null) {
      switchStartOnUsbAttached.setChecked(stateStartOnUsbAttached);
      switchStartOnUsbAttached.setOnCheckedChangeListener(this.switchCheckedListener);
    }
    TextView textStartOnUsbAttached = (TextView) findViewById(R.id.textStartOnUsbAttached);
    if (textStartOnUsbAttached != null) {
      if (stateStartOnUsbAttached) {
        textStartOnUsbAttached.setText(R.string.settings_text_startOnUsbAttached_On);
      } else {
        textStartOnUsbAttached.setText(R.string.settings_text_startOnUsbAttached_Off);
      }
    }
    Switch switchOnStartByUsbGotoBackground =
        (Switch) findViewById(R.id.switchOnStartByUsbGotoBackground);
    if (switchOnStartByUsbGotoBackground != null) {
      switchOnStartByUsbGotoBackground.setChecked(
          pref_settings.getBoolean(SettingsActivity.pref_key_onstartbyusb_gotobackground, false));
      switchOnStartByUsbGotoBackground.setOnCheckedChangeListener(this.switchCheckedListener);
    }
    TextView textSwitchOnStartByUsbGotoBackground =
        (TextView) findViewById(R.id.textSwitchOnStartByUsbGotoBackground);
    /*
        if (Build.VERSION.SDK_INT < 21 && (layout = findViewById(R.id.layout_usagestats)) != null) {
      layout.setVisibility(8);
    }
        */
        
    Switch sw = (Switch) findViewById(R.id.switchServiceFollowing);
    if (sw != null) {
      String ServiceFollowing = getResources().getString(R.string.settings_ServiceFollowing);
      String Experimental = getResources().getString(R.string.Experimental);
      String switchText = ServiceFollowing;
      if (!Experimental.isEmpty()) {
        switchText = switchText + " (" + Experimental + ")";
      }
      sw.setText(switchText);
      sw.setChecked(pref_playing.getBoolean(SettingsActivity.pref_key_service_link_switch, true));
      sw.setOnCheckedChangeListener(this.switchCheckedListener);
    }
    Switch sw2 = (Switch) findViewById(R.id.switchSendBroadcastIntent);
    if (sw2 != null) {
      sw2.setChecked(
          pref_settings.getBoolean(SettingsActivity.pref_key_sendBroadcastIntent, false));
      sw2.setOnCheckedChangeListener(this.switchCheckedListener);
    }
    Switch sw3 = (Switch) findViewById(R.id.switchSwapPrevNextFromSWC);
    if (sw3 != null) {
      sw3.setChecked(pref_settings.getBoolean(SettingsActivity.pref_key_swapPrevNext, false));
      sw3.setOnCheckedChangeListener(this.switchCheckedListener);
    }
    try {
      PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
      if (pInfo.versionName.toLowerCase().contains("nousbdf")) {
        if (switchStartOnUsbAttached != null) {
          switchStartOnUsbAttached.setEnabled(false);
        }
        if (textStartOnUsbAttached != null) {
          textStartOnUsbAttached.setEnabled(false);
        }
        if (switchOnStartByUsbGotoBackground != null) {
          switchOnStartByUsbGotoBackground.setEnabled(false);
        }
        if (textSwitchOnStartByUsbGotoBackground != null) {
          textSwitchOnStartByUsbGotoBackground.setEnabled(false);
        }
        TextView text = (TextView) findViewById(2131427440);
        if (text != null) {
          text.setEnabled(false);
        }
        TextView text2 = (TextView) findViewById(2131427441);
        if (text2 != null) {
          text2.setEnabled(false);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override // android.app.Activity
  protected void onResume() {
    super.onResume();
  }

  /* JADX INFO: Access modifiers changed from: private */
  public void setStartOnUsbAttached(boolean isEnabled) {
    SharedPreferences.Editor preferences =
        this.mContext.getSharedPreferences(SettingsActivity.prefname_settings, 0).edit();
    preferences.putBoolean(SettingsActivity.pref_key_startOnUsbAttached, isEnabled);
    preferences.apply();
    TextView textStartOnUsbAttached = (TextView) findViewById(R.id.textStartOnUsbAttached);
    if (textStartOnUsbAttached != null) {
      if (isEnabled) {
        textStartOnUsbAttached.setText(R.string.settings_text_startOnUsbAttached_On);
      } else {
        textStartOnUsbAttached.setText(R.string.settings_text_startOnUsbAttached_Off);
      }
    }
  }

  /* JADX INFO: Access modifiers changed from: private */
  public void setServiceFollowing(boolean isEnabled) {
    SharedPreferences.Editor preferences = this.mContext.getSharedPreferences("playing", 0).edit();
    preferences.putBoolean(SettingsActivity.pref_key_service_link_switch, isEnabled);
    preferences.apply();
    ServiceFollowing.update_enabled_status(this.mContext);
  }

  /* JADX INFO: Access modifiers changed from: private */
  public void setOnStartByUsbGotoBackground(boolean isEnabled) {
    SharedPreferences.Editor preferences =
        this.mContext.getSharedPreferences(SettingsActivity.prefname_settings, 0).edit();
    preferences.putBoolean(SettingsActivity.pref_key_onstartbyusb_gotobackground, isEnabled);
    preferences.apply();
  }

  /* JADX INFO: Access modifiers changed from: private */
  public void setSendBroadcastIntent(boolean isEnabled) {
    SharedPreferences.Editor preferences =
        this.mContext.getSharedPreferences(SettingsActivity.prefname_settings, 0).edit();
    preferences.putBoolean(SettingsActivity.pref_key_sendBroadcastIntent, isEnabled);
    preferences.apply();
  }

  /* JADX INFO: Access modifiers changed from: private */
  public void setSwapPrevNext(boolean isEnabled) {
    SharedPreferences.Editor preferences =
        this.mContext.getSharedPreferences(SettingsActivity.prefname_settings, 0).edit();
    preferences.putBoolean(SettingsActivity.pref_key_swapPrevNext, isEnabled);
    preferences.apply();
  }
}

package com.thf.dabplayer.activity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import com.thf.dabplayer.R;
import com.thf.dabplayer.service.DabService;
import com.thf.dabplayer.utils.C0162a;
import java.util.Locale;

/* renamed from: com.ex.dabplayer.pad.activity.SettingsAudioActivity */
/* loaded from: classes.dex */
public class SettingsAudioActivity extends Activity {
  private final float AUDIOLEVEL_MIN = 0.0f;
  private final float AUDIOLEVEL_MAX = 1.0f;
  private final float AUDIOLEVEL_STEP = 0.1f;
  private Context mContext = null;
  private SeekBar mSeekBarAudioLevelWhenDucked = null;
  @IdRes private final int R_id_switchRecordButton = R.id.switchRecordButton;
  @IdRes private final int R_id_switchDeclip = R.id.switchDeclip;
  @IdRes private final int R_id_switchDeclipNotification = R.id.switchDeclipNotification;
  @IdRes private final int R_id_switchAudiolossSupport = R.id.switchAudiolossSupport;
  @IdRes private final int R_id_btnDecrAudioLevel = R.id.btnDecrAudioLevel;
  @IdRes private final int R_id_btnIncrAudioLevel = R.id.btnIncrAudioLevel;
  private final CompoundButton.OnCheckedChangeListener switchCheckedListener =
      new CompoundButton.OnCheckedChangeListener() { // from class:
        // com.ex.dabplayer.pad.activity.SettingsAudioActivity.1
        @Override // android.widget.CompoundButton.OnCheckedChangeListener
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
          if (buttonView.getId() == R.id.switchRecordButton) {
            /* 2131427384 */
            SettingsAudioActivity.this.setRecordButton(isChecked);
            return;
          } else if (buttonView.getId() == R.id.switchDeclip) {
            /* 2131427445 */
            SettingsAudioActivity.this.setDeclip(isChecked);
            return;
          } else if (buttonView.getId() == R.id.switchDeclipNotification) {
            /* 2131427446 */
            SettingsAudioActivity.this.setDeclipNotification(isChecked);
            return;
          } else if (buttonView.getId() == R.id.switchAudiolossSupport) {
            /* 2131427447 */
            SettingsAudioActivity.this.setAudioLossSupport(isChecked);
            return;
          } else {
            return;
          }
        }
      };
  private final View.OnClickListener buttonClickedListener =
      new View
          .OnClickListener() { // from class: com.ex.dabplayer.pad.activity.SettingsAudioActivity.2
        @Override // android.view.View.OnClickListener
        public void onClick(View v) {
          if (v.getId() == R.id.btnIncrAudioLevel) {
              /* 2131427468 */
            SettingsAudioActivity.this.setAudioLevelChange(0.1f);
            return;
          } else if (v.getId() == R.id.btnDecrAudioLevel) {
              /* 2131427469 */
            SettingsAudioActivity.this.setAudioLevelChange(-0.1f);
            return;
          } else {
            return;
          }
        }
      };
  private final SeekBar.OnSeekBarChangeListener seekBarAudioLevelWhenDuckedChangeListener =
      new SeekBar.OnSeekBarChangeListener() { // from class:
        // com.ex.dabplayer.pad.activity.SettingsAudioActivity.3
        @Override // android.widget.SeekBar.OnSeekBarChangeListener
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
          if (fromUser) {
            SettingsAudioActivity.this.setAudioLevelWhenDucked(progress);
          }
        }

        @Override // android.widget.SeekBar.OnSeekBarChangeListener
        public void onStartTrackingTouch(SeekBar seekBar) {}

        @Override // android.widget.SeekBar.OnSeekBarChangeListener
        public void onStopTrackingTouch(SeekBar seekBar) {}
      };

  @Override // android.app.Activity
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.settings_audio);
    this.mContext = getApplicationContext();
    Switch[] switches = {
      (Switch) findViewById(R.id.switchRecordButton),
      (Switch) findViewById(R.id.switchDeclip),
      (Switch) findViewById(R.id.switchDeclipNotification),
      (Switch) findViewById(R.id.switchAudiolossSupport)
    };
    for (Switch sw : switches) {
      if (sw != null) {
        sw.setOnCheckedChangeListener(this.switchCheckedListener);
      }
    }
    Button[] buttons = {
      (Button) findViewById(R.id.btnDecrAudioLevel), (Button) findViewById(R.id.btnIncrAudioLevel)
    };
    for (Button btn : buttons) {
      if (btn != null) {
        btn.setOnClickListener(this.buttonClickedListener);
      }
    }
    this.mSeekBarAudioLevelWhenDucked =
        (SeekBar)
            findViewById(
                getResources()
                    .getIdentifier(
                        "seekBarAudioLevelWhenDucked", DabService.EXTRA_ID, getPackageName()));
    if (this.mSeekBarAudioLevelWhenDucked != null) {
      this.mSeekBarAudioLevelWhenDucked.setOnSeekBarChangeListener(
          this.seekBarAudioLevelWhenDuckedChangeListener);
    }
  }

  @Override // android.app.Activity
  protected void onResume() {
    super.onResume();
    SharedPreferences pref_settings =
        this.mContext.getSharedPreferences(SettingsActivity.prefname_settings, 0);
    Switch sw = (Switch) findViewById(R.id.switchRecordButton);
    if (sw != null) {
      sw.setChecked(pref_settings.getBoolean(SettingsActivity.pref_key_record_button, false));
    }
    Switch sw2 = (Switch) findViewById(R.id.switchDeclip);
    if (sw2 != null) {
      sw2.setChecked(pref_settings.getBoolean(SettingsActivity.pref_key_declip, true));
    }
    Switch sw3 = (Switch) findViewById(R.id.switchDeclipNotification);
    if (sw3 != null) {
      sw3.setChecked(pref_settings.getBoolean(SettingsActivity.pref_key_declip_notification, true));
      setDeclipNotificationEnabled(
          pref_settings.getBoolean(SettingsActivity.pref_key_declip, true));
    }
    Switch sw4 = (Switch) findViewById(R.id.switchAudiolossSupport);
    if (sw4 != null) {
      sw4.setChecked(pref_settings.getBoolean(SettingsActivity.pref_key_audioloss_support, true));
    }
    if (this.mSeekBarAudioLevelWhenDucked != null) {
      this.mSeekBarAudioLevelWhenDucked.setEnabled(
          pref_settings.getBoolean(SettingsActivity.pref_key_audioloss_support, true));
      float audioLevelWhenDucked =
          pref_settings.getFloat(SettingsActivity.pref_key_audioLevelWhenDucked, 0.5f);
      int decimalAudioLevelWhenDucked = (int) (10.0f * audioLevelWhenDucked);
      this.mSeekBarAudioLevelWhenDucked.setProgress(decimalAudioLevelWhenDucked);
    }
    setAudioLevelChange(0.0f);
  }

  /* JADX INFO: Access modifiers changed from: private */
  public void setRecordButton(boolean isEnabled) {
    SharedPreferences.Editor preferences =
        this.mContext.getSharedPreferences(SettingsActivity.prefname_settings, 0).edit();
    preferences.putBoolean(SettingsActivity.pref_key_record_button, isEnabled);
    preferences.apply();
  }

  /* JADX INFO: Access modifiers changed from: private */
  public void setDeclip(boolean isEnabled) {
    SharedPreferences.Editor preferences =
        this.mContext.getSharedPreferences(SettingsActivity.prefname_settings, 0).edit();
    preferences.putBoolean(SettingsActivity.pref_key_declip, isEnabled);
    preferences.apply();
    setDeclipNotificationEnabled(isEnabled);
  }

  /* JADX INFO: Access modifiers changed from: private */
  public void setDeclipNotification(boolean isEnabled) {
    SharedPreferences.Editor preferences =
        this.mContext.getSharedPreferences(SettingsActivity.prefname_settings, 0).edit();
    preferences.putBoolean(SettingsActivity.pref_key_declip_notification, isEnabled);
    preferences.apply();
  }

  private void setDeclipNotificationEnabled(boolean isEnabled) {
    Switch sw = (Switch) findViewById(R.id.switchDeclipNotification);
    if (sw != null) {
      sw.setEnabled(isEnabled);
    }
  }

  /* JADX INFO: Access modifiers changed from: private */
  public void setAudioLossSupport(boolean isEnabled) {
    SharedPreferences.Editor preferences =
        this.mContext.getSharedPreferences(SettingsActivity.prefname_settings, 0).edit();
    preferences.putBoolean(SettingsActivity.pref_key_audioloss_support, isEnabled);
    preferences.apply();
    if (this.mSeekBarAudioLevelWhenDucked != null) {
      this.mSeekBarAudioLevelWhenDucked.setEnabled(isEnabled);
    }
  }

  /* JADX INFO: Access modifiers changed from: private */
  public void setAudioLevelWhenDucked(int levelWhenDucked) {
    if (levelWhenDucked >= 0 && levelWhenDucked <= 10) {
      SharedPreferences.Editor preferences =
          this.mContext.getSharedPreferences(SettingsActivity.prefname_settings, 0).edit();
      preferences.putFloat(SettingsActivity.pref_key_audioLevelWhenDucked, levelWhenDucked / 10.0f);
      preferences.apply();
    }
  }

  /* JADX INFO: Access modifiers changed from: private */
  public void setAudioLevelChange(float diff) {
    SharedPreferences prefSettings =
        this.mContext.getSharedPreferences(SettingsActivity.prefname_settings, 0);
    float volume = prefSettings.getFloat(SettingsActivity.pref_key_audioLevel, 1.0f);
    float newVolume = volume + diff;
    if (newVolume > 1.0f) {
      newVolume = 1.0f;
    }
    if (newVolume < 0.0f) {
      newVolume = 0.0f;
    }
    SharedPreferences.Editor editor = prefSettings.edit();
    editor.putFloat(SettingsActivity.pref_key_audioLevel, newVolume);
    editor.apply();
    C0162a.m9a("set audio level (" + volume + ") to " + newVolume);
    TextView textView =
        (TextView)
            findViewById(
                getResources()
                    .getIdentifier("textAudioLevelPercent", DabService.EXTRA_ID, getPackageName()));
    if (textView != null) {
      String audioLevelPercent =
          String.format(Locale.US, "%d%%", Integer.valueOf(Math.round(100.0f * newVolume)));
      textView.setText(audioLevelPercent);
    }
  }
}

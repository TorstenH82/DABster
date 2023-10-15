package com.thf.dabplayer.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import com.thf.dabplayer.R;
import com.thf.dabplayer.activity.ColorPickerView;
import com.thf.dabplayer.activity.MotImageDimSettingsDialog;
import com.thf.dabplayer.activity.Player;
import com.thf.dabplayer.service.DabService;
import com.thf.dabplayer.utils.C0162a;
import java.util.Locale;

/* renamed from: com.ex.dabplayer.pad.activity.SettingsLayoutActivity */
/* loaded from: classes.dex */
public class SettingsLayoutActivity extends Activity
    implements View.OnClickListener,
        CompoundButton.OnCheckedChangeListener,
        AdapterView.OnItemSelectedListener,
        MotImageDimSettingsDialog.ResultListener,
        DialogInterface.OnClickListener {
  private static final String keyAutoMaximizeTimeout = "maximizeTimeout";
  private static final String keyScaleFactor = "scaleFactor";
  @IdRes private int R_id_textDlsSize;
  @IdRes private int R_id_textSettingsDLS;
  @IdRes private int R_id_textSettingsStationName;
  @IdRes private int R_id_textStationNameSize;
  private float mDlsSizeFromStyle;
  private boolean mOldSettingAdditionalInfos;
  private boolean mOldSettingStationLogoInList;
  private float mStationNameSizeFromStyle;
  private Context mContext = null;
  @IdRes private final int R_id_switchAdditionalInfos = R.id.switchAdditionalInfos;
  @IdRes private final int R_id_switchStationLogoInList = R.id.switchStationLogoInList;
  @IdRes private final int R_id_switchLogoAsMot = R.id.switchLogoAsMot;
  @IdRes private final int R_id_switchBackgroundBoxes = R.id.switchBackgroundBoxes;
  @IdRes private final int R_id_switchAutoMaximize = R.id.switchAutoMaximize;
  @IdRes private final int R_id_switchMotSlideshowEnabled = R.id.switchMotSlideshowEnabled;
  @IdRes private final int R_id_spinner_automaximize_timeout = R.id.spinner_automaximize_timeout;
  @IdRes private final int R_id_spinner_max_scale_factor = R.id.spinner_max_scale_factor;
  @IdRes private final int R_id_brightnessValueTextBtn = R.id.brightnessValueTextBtn;
  @IdRes private final int R_id_btnDecrStationNameSize = R.id.btnDecrStationNameSize;
  @IdRes private final int R_id_btnIncrStationNameSize = R.id.btnIncrStationNameSize;
  @IdRes private final int R_id_btnDecrDlsSize = R.id.btnDecrDlsSize;
  @IdRes private final int R_id_btnIncrDlsSize = R.id.btnIncrDlsSize;
  @IdRes private final int R_id_btnColorPickerStationName = R.id.btnColorPickerStationName;

  @IdRes
  private final int R_id_radioBtnSignalBarSameColorStationName =
      R.id.radioBtnSignalBarSameColorStationName;

  @IdRes private final int R_id_radioBtnSignalBarFancy = R.id.radioBtnSignalBarFancy;

  private void init() {
    this.R_id_textStationNameSize =
        getResources().getIdentifier("textStationNameSize", DabService.EXTRA_ID, getPackageName());
    this.R_id_textSettingsStationName =
        getResources()
            .getIdentifier("textSettingsStationName", DabService.EXTRA_ID, getPackageName());
    this.R_id_textDlsSize =
        getResources().getIdentifier("textDlsSize", DabService.EXTRA_ID, getPackageName());
    this.R_id_textSettingsDLS =
        getResources().getIdentifier("textSettingsDLS", DabService.EXTRA_ID, getPackageName());
  }

  @Override // android.app.Activity
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.settings_layout);
    C0162a.m9a("SettingsLayoutActivity:onCreate");
    init();
    this.mContext = getApplicationContext();
    SharedPreferences pref_settings =
        this.mContext.getSharedPreferences(SettingsActivity.prefname_settings, 0);
    this.mOldSettingAdditionalInfos =
        pref_settings.getBoolean(SettingsActivity.pref_key_showAdditionalInfos, true);
    this.mOldSettingStationLogoInList =
        pref_settings.getBoolean(SettingsActivity.pref_key_showStationLogoInList, true);
    Switch[] switches = {
      (Switch) findViewById(R.id.switchAdditionalInfos),
      (Switch) findViewById(R.id.switchStationLogoInList),
      (Switch) findViewById(R.id.switchBackgroundBoxes),
      (Switch) findViewById(R.id.switchLogoAsMot),
      (Switch) findViewById(R.id.switchAutoMaximize),
      (Switch) findViewById(R.id.switchMotSlideshowEnabled)
    };
    for (Switch sw : switches) {
      if (sw != null) {
        sw.setOnCheckedChangeListener(this);
      }
    }
    Spinner[] spinners = {
      (Spinner) findViewById(R.id.spinner_automaximize_timeout),
      (Spinner) findViewById(R.id.spinner_max_scale_factor)
    };
    for (Spinner sp : spinners) {
      if (sp != null) {
        sp.setOnItemSelectedListener(this);
      }
    }
    Button[] buttons = {
      (Button) findViewById(R.id.brightnessValueTextBtn),
      (Button) findViewById(R.id.btnDecrStationNameSize),
      (Button) findViewById(R.id.btnIncrStationNameSize),
      (Button) findViewById(R.id.btnDecrDlsSize),
      (Button) findViewById(R.id.btnIncrDlsSize)
    };
    for (Button btn : buttons) {
      if (btn != null) {
        btn.setOnClickListener(this);
      }
    }
    ImageButton imageButton = (ImageButton) findViewById(R.id.btnColorPickerStationName);
    if (imageButton != null) {
      imageButton.setOnClickListener(this);
    }
    RadioButton[] radioButtons = {
      (RadioButton) findViewById(R.id.radioBtnSignalBarSameColorStationName),
      (RadioButton) findViewById(R.id.radioBtnSignalBarFancy)
    };
    for (RadioButton radioButton : radioButtons) {
      if (radioButton != null) {
        radioButton.setOnClickListener(this);
      }
    }
    if (radioButtons[0] != null) {
      String format =
          getResources()
              .getString(getResources().getIdentifier("color_like", "string", getPackageName()));
      String text =
          String.format(
              format,
              getResources()
                  .getString(
                      getResources().getIdentifier("StationName", "string", getPackageName())));
      radioButtons[0].setText(text);
    }
    TextView textView = (TextView) findViewById(this.R_id_textStationNameSize);
    if (textView != null) {
      int sizeIncrement =
          pref_settings.getInt(SettingsActivity.pref_key_stationNameSizeIncrement, 0);
      textView.setText(String.valueOf(sizeIncrement));
      TextView textView2 = (TextView) findViewById(this.R_id_textSettingsStationName);
      if (textView2 != null) {
        this.mStationNameSizeFromStyle = textView2.getTextSize();
        float newSize = this.mStationNameSizeFromStyle + sizeIncrement;
        textView2.setTextSize(newSize);
      }
    }
    TextView textView3 = (TextView) findViewById(this.R_id_textDlsSize);
    if (textView3 != null) {
      int sizeIncrement2 = pref_settings.getInt(SettingsActivity.pref_key_dlsSizeIncrement, 0);
      textView3.setText(String.valueOf(sizeIncrement2));
      TextView textView4 = (TextView) findViewById(this.R_id_textSettingsDLS);
      if (textView4 != null) {
        this.mDlsSizeFromStyle = textView4.getTextSize();
        float newSize2 = this.mDlsSizeFromStyle + sizeIncrement2;
        textView4.setTextSize(newSize2);
      }
    }
  }

  @Override // android.app.Activity
  protected void onResume() {
    super.onResume();
    C0162a.m9a("SettingsLayoutActivity:onResume");
    SharedPreferences pref_settings =
        this.mContext.getSharedPreferences(SettingsActivity.prefname_settings, 0);
    Switch sw = (Switch) findViewById(R.id.switchAdditionalInfos);
    if (sw != null) {
      sw.setChecked(pref_settings.getBoolean(SettingsActivity.pref_key_showAdditionalInfos, true));
    }
    Switch sw2 = (Switch) findViewById(R.id.switchStationLogoInList);
    if (sw2 != null) {
      sw2.setChecked(
          pref_settings.getBoolean(SettingsActivity.pref_key_showStationLogoInList, true));
    }
    Switch sw3 = (Switch) findViewById(R.id.switchLogoAsMot);
    if (sw3 != null) {
      sw3.setChecked(pref_settings.getBoolean(SettingsActivity.pref_key_logo_as_mot, true));
    }
    Switch sw4 = (Switch) findViewById(R.id.switchBackgroundBoxes);
    if (sw4 != null) {
      sw4.setChecked(pref_settings.getBoolean(SettingsActivity.pref_key_background_boxes, false));
    }
    Switch sw5 = (Switch) findViewById(R.id.switchAutoMaximize);
    Spinner sp = (Spinner) findViewById(R.id.spinner_automaximize_timeout);
    if (sp != null && sw5 != null) {
      SettingsActivity.createSimpleTextSpinnerAdapter(
          sp, keyAutoMaximizeTimeout, this.mContext, R.array.maximizeTimeoutStrings);
      boolean enabled = pref_settings.getBoolean(SettingsActivity.pref_key_auto_maximize, false);
      sw5.setChecked(enabled);
      long timeoutMs =
          pref_settings.getLong(
              SettingsActivity.pref_key_auto_maximize_timeout,
              SettingsActivity.pref_defvalue_auto_maximize_timeout_msec.longValue());
      int i = 0;
      int[] intArray = getResources().getIntArray(R.array.maximizeTimeoutValues);
      int length = intArray.length;
      int i2 = 0;
      while (true) {
        if (i2 >= length) {
          break;
        }
        int val = intArray[i2];
        if (val == timeoutMs) {
          sp.setSelection(i);
          break;
        } else {
          i++;
          i2++;
        }
      }
      sp.setEnabled(enabled);
    }
    Switch sw6 = (Switch) findViewById(R.id.switchMotSlideshowEnabled);
    if (sw6 != null) {
      sw6.setChecked(pref_settings.getBoolean(SettingsActivity.pref_key_motSlideshowEnabled, true));
    }
    Spinner sp2 = (Spinner) findViewById(R.id.spinner_max_scale_factor);
    if (sp2 != null) {
      SettingsActivity.createSimpleTextSpinnerAdapter(
          sp2, keyScaleFactor, this.mContext, R.array.scaleFactorStrings);
      float factor = pref_settings.getFloat(SettingsActivity.pref_key_maxScaleFactor, 2.0f);
      int i3 = 0;
      int[] intArray2 = getResources().getIntArray(R.array.scaleFactorValues);
      int length2 = intArray2.length;
      int i4 = 0;
      while (true) {
        if (i4 >= length2) {
          break;
        }
        int val2 = intArray2[i4];
        float valf = val2 / 1000.0f;
        if (valf == factor) {
          sp2.setSelection(i3);
          break;
        } else {
          i3++;
          i4++;
        }
      }
    }
    setBrightnessBtnTextFromPreferences();
    int stationNameColor = pref_settings.getInt(SettingsActivity.pref_key_stationNameColor, -1);
    TextView textStationName = (TextView) findViewById(this.R_id_textSettingsStationName);
    if (textStationName != null) {
      textStationName.setTextColor(stationNameColor);
    }
    ImageButton imageButton = (ImageButton) findViewById(R.id.btnColorPickerStationName);
    if (imageButton != null) {
      imageButton.setBackgroundColor(stationNameColor);
    }
    RadioButton signalBarColorOptionRadioButton = null;
    int signalBarColorOption =
        pref_settings.getInt(SettingsActivity.pref_key_signalBarColorOption, 1);
    switch (signalBarColorOption) {
      case 0:
        signalBarColorOptionRadioButton =
            (RadioButton) findViewById(R.id.radioBtnSignalBarSameColorStationName);
        break;
      case 1:
        signalBarColorOptionRadioButton = (RadioButton) findViewById(R.id.radioBtnSignalBarFancy);
        break;
    }
    if (signalBarColorOptionRadioButton != null) {
      signalBarColorOptionRadioButton.setChecked(true);
    }
    setSignalBarColorOption(signalBarColorOption);
  }

  @Override // android.app.Activity
  protected void onDestroy() {
    super.onDestroy();
    C0162a.m9a("SettingsLayoutActivity:onDestroy");
    boolean restartPlayerNeeded = false;
    SharedPreferences pref_settings =
        this.mContext.getSharedPreferences(SettingsActivity.prefname_settings, 0);
    Switch sw = (Switch) findViewById(R.id.switchAdditionalInfos);
    if (sw != null) {
      boolean currentSettingAdditionalInfos =
          pref_settings.getBoolean(SettingsActivity.pref_key_showAdditionalInfos, true);
      restartPlayerNeeded =
          (0 == 0 && currentSettingAdditionalInfos == this.mOldSettingAdditionalInfos)
              ? false
              : true;
    }
    Switch sw2 = (Switch) findViewById(R.id.switchStationLogoInList);
    if (sw2 != null) {
      boolean currentSettingStationLogoInList =
          pref_settings.getBoolean(SettingsActivity.pref_key_showStationLogoInList, true);
      restartPlayerNeeded =
          restartPlayerNeeded
              || currentSettingStationLogoInList != this.mOldSettingStationLogoInList;
    }
    if (restartPlayerNeeded) {
      C0162a.m9a("layout changes require recreation");
      sendBroadcast(new Intent(Player.HomeKeyReceiver.ACTION_RECREATE));
    }
  }

  @Override // android.widget.CompoundButton.OnCheckedChangeListener
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

    if (buttonView.getId() == R.id.switchAdditionalInfos) {
      /* 2131427378 */
      setAdditionalInfos(isChecked);
      return;
    } else if (buttonView.getId() == R.id.switchBackgroundBoxes) {
      /* 2131427393 */
      setBackgroundBoxes(isChecked);
      return;
    } else if (buttonView.getId() == R.id.switchStationLogoInList) {
      /* 2131427410 */
      setStationLogoInList(isChecked);
      return;
    } else if (buttonView.getId() == R.id.switchLogoAsMot) {
      /* 2131427412 */
      setStationLogoAsMot(isChecked);
      return;
    } else if (buttonView.getId() == R.id.switchAutoMaximize) {
      /* 2131427424 */
      setAutoMaximize(isChecked);
      return;
    } else if (buttonView.getId() == R.id.switchMotSlideshowEnabled) {
      /* 2131427467 */
      setSlideshowAndMotEnabled(isChecked);
      return;
    } else {
      return;
    }
  }

  @Override // android.widget.AdapterView.OnItemSelectedListener
  public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    if (parent.getId() == R.id.spinner_automaximize_timeout) {
      /* 2131427426 */
      int[] maximizeTimeoutValues = getResources().getIntArray(R.array.maximizeTimeoutValues);
      setAutoMaximizeTimeoutMs(maximizeTimeoutValues[position]);
      return;
    }
    // case R.id.textMaxScaleFactor /* 2131427427 */:
    // case R.id.textTextMaxScaleFactor /* 2131427428 */:
    // default:
    //  return;
    else if (parent.getId() == R.id.spinner_max_scale_factor) {
      /* 2131427429 */
      int[] scaleFactorValues = getResources().getIntArray(R.array.scaleFactorValues);
      setScaleFactor(scaleFactorValues[position] / 1000.0f);
      return;
    } else {
      return;
    }
  }

  @Override // android.widget.AdapterView.OnItemSelectedListener
  public void onNothingSelected(AdapterView<?> arg0) {}

  @Override // android.view.View.OnClickListener
  public void onClick(View v) {
    if (v.getId() == R.id.brightnessValueTextBtn) {
        /* 2131427438 */
      new MotImageDimSettingsDialog(v.getContext(), this);
      return;
    } else if (v.getId() == R.id.btnDecrStationNameSize) {
        /* 2131427448 */
      setTextSize(
          false,
          this.R_id_textStationNameSize,
          this.R_id_textSettingsStationName,
          SettingsActivity.pref_key_stationNameSizeIncrement,
          this.mStationNameSizeFromStyle);
      return;
    } else if (v.getId() == R.id.btnIncrStationNameSize) {
        /* 2131427449 */
      setTextSize(
          true,
          this.R_id_textStationNameSize,
          this.R_id_textSettingsStationName,
          SettingsActivity.pref_key_stationNameSizeIncrement,
          this.mStationNameSizeFromStyle);
      return;
    } else if (v.getId() == R.id.btnDecrDlsSize) {
        /* 2131427452 */
      setTextSize(
          false,
          this.R_id_textDlsSize,
          this.R_id_textSettingsDLS,
          SettingsActivity.pref_key_dlsSizeIncrement,
          this.mDlsSizeFromStyle);
      return;
    } else if (v.getId() == R.id.btnIncrDlsSize) {
        /* 2131427453 */
      setTextSize(
          true,
          this.R_id_textDlsSize,
          this.R_id_textSettingsDLS,
          SettingsActivity.pref_key_dlsSizeIncrement,
          this.mDlsSizeFromStyle);
      return;
    } else if (v.getId() == R.id.btnColorPickerStationName) {
        /* 2131427456 */
      AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext(), 2);
      View view =
          LayoutInflater.from(v.getContext())
              .inflate(
                  v.getContext()
                      .getResources()
                      .getIdentifier("colorpicker_dlg", "layout", getPackageName()),
                  (ViewGroup) null);
      if (view != null) {
        AlertDialog dlg =
            builder
                .setView(view)
                .setPositiveButton(17039370, this)
                .setNegativeButton("Default", this)
                .show();
        ColorPickerView colorPicker =
            (ColorPickerView)
                dlg.findViewById(
                    this.mContext
                        .getResources()
                        .getIdentifier("dlgColorPicker", DabService.EXTRA_ID, getPackageName()));
        if (colorPicker != null) {
          SharedPreferences pref_settings =
              this.mContext.getSharedPreferences(SettingsActivity.prefname_settings, 0);
          colorPicker.setColor(
              pref_settings.getInt(SettingsActivity.pref_key_stationNameColor, -1));
          colorPicker.setOnColorChangedListener(
              new ColorPickerView.OnColorChangedListener() { // from class:
                // com.ex.dabplayer.pad.activity.SettingsLayoutActivity.1
                @Override // com.thf.dabplayer.activity.ColorPickerView.OnColorChangedListener
                public void onColorChanged(int color) {
                  SettingsLayoutActivity.this.setStationNameColor(color);
                }
              });
          return;
        }
        return;
      }
      return;
    } else if (v.getId() == R.id.radioBtnSignalBarSameColorStationName) {
        /* 2131427464 */
      setSignalBarColorOption(0);
      return;
    } else if (v.getId() == R.id.radioBtnSignalBarFancy) {
        /* 2131427465 */
      setSignalBarColorOption(1);
      return;
    } else {
      return;
    }
  }

  @Override // com.thf.dabplayer.activity.MotImageDimSettingsDialog.ResultListener
  public void onDialogResult(MotImageDimSettingsDialog dlg, int which) {
    if (which == -1) {
      setBrightnessBtnTextFromPreferences();
    }
  }

  @Override // android.content.DialogInterface.OnClickListener
  public void onClick(DialogInterface dialog, int which) {
    switch (which) {
      case -2:
        setStationNameColor(-1);
        return;
      case -1:
      default:
        return;
    }
  }

  private void setAdditionalInfos(boolean isEnabled) {
    SharedPreferences.Editor preferences =
        this.mContext.getSharedPreferences(SettingsActivity.prefname_settings, 0).edit();
    preferences.putBoolean(SettingsActivity.pref_key_showAdditionalInfos, isEnabled);
    preferences.apply();
  }

  private void setStationLogoInList(boolean isEnabled) {
    SharedPreferences.Editor preferences =
        this.mContext.getSharedPreferences(SettingsActivity.prefname_settings, 0).edit();
    preferences.putBoolean(SettingsActivity.pref_key_showStationLogoInList, isEnabled);
    preferences.apply();
  }

  private void setStationLogoAsMot(boolean isEnabled) {
    SharedPreferences.Editor preferences =
        this.mContext.getSharedPreferences(SettingsActivity.prefname_settings, 0).edit();
    preferences.putBoolean(SettingsActivity.pref_key_logo_as_mot, isEnabled);
    preferences.apply();
  }

  private void setBackgroundBoxes(boolean isEnabled) {
    SharedPreferences.Editor preferences =
        this.mContext.getSharedPreferences(SettingsActivity.prefname_settings, 0).edit();
    preferences.putBoolean(SettingsActivity.pref_key_background_boxes, isEnabled);
    preferences.apply();
  }

  private void setAutoMaximize(boolean isEnabled) {
    SharedPreferences.Editor preferences =
        this.mContext.getSharedPreferences(SettingsActivity.prefname_settings, 0).edit();
    preferences.putBoolean(SettingsActivity.pref_key_auto_maximize, isEnabled);
    preferences.apply();
    Spinner sp = (Spinner) findViewById(R.id.spinner_automaximize_timeout);
    if (sp != null) {
      sp.setEnabled(isEnabled);
    }
  }

  private void setAutoMaximizeTimeoutMs(int timeoutMs) {
    SharedPreferences.Editor preferences =
        this.mContext.getSharedPreferences(SettingsActivity.prefname_settings, 0).edit();
    preferences.putLong(SettingsActivity.pref_key_auto_maximize_timeout, timeoutMs);
    preferences.apply();
  }

  private void setScaleFactor(float factor) {
    SharedPreferences.Editor preferences =
        this.mContext.getSharedPreferences(SettingsActivity.prefname_settings, 0).edit();
    preferences.putFloat(SettingsActivity.pref_key_maxScaleFactor, factor);
    preferences.apply();
  }

  private void setBrightnessBtnTextFromPreferences() {
    Button btn = (Button) findViewById(R.id.brightnessValueTextBtn);
    if (btn != null) {
      int dimVal =
          getSharedPreferences(SettingsActivity.prefname_settings, 0)
              .getInt(SettingsActivity.pref_key_dim_percent, 50);
      String dimValPercent = String.format(Locale.US, "%d%%", Integer.valueOf(dimVal));
      btn.setText(dimValPercent);
    }
  }

  private void setTextSize(
      boolean increase,
      @IdRes int idTextViewWithValue,
      @IdRes int idTextViewExample,
      String preference_key,
      float textSizeFromStyle) {
    int size;
    TextView text = (TextView) findViewById(idTextViewWithValue);
    if (text != null) {
      try {
        int size2 = Integer.parseInt(text.getText().toString());
        if (increase) {
          size = size2 + 1;
        } else {
          size = size2 - 1;
        }
        text.setText(String.valueOf(size));
        TextView text2 = (TextView) findViewById(idTextViewExample);
        if (text2 != null) {
          float newSize = textSizeFromStyle + size;
          text2.setTextSize(newSize);
          SharedPreferences.Editor preferences =
              this.mContext.getSharedPreferences(SettingsActivity.prefname_settings, 0).edit();
          preferences.putInt(preference_key, size);
          preferences.apply();
        }
      } catch (NumberFormatException e) {
        e.printStackTrace();
      }
    }
  }

  /* JADX INFO: Access modifiers changed from: private */
  public void setStationNameColor(int color) {
    SharedPreferences preferences =
        this.mContext.getSharedPreferences(SettingsActivity.prefname_settings, 0);
    preferences.edit().putInt(SettingsActivity.pref_key_stationNameColor, color).apply();
    TextView textStationName = (TextView) findViewById(this.R_id_textSettingsStationName);
    if (textStationName != null) {
      textStationName.setTextColor(color);
    }
    ImageButton colorPickerBtn = (ImageButton) findViewById(R.id.btnColorPickerStationName);
    if (colorPickerBtn != null) {
      colorPickerBtn.setBackgroundColor(color);
    }
    setSignalBarColorOption(preferences.getInt(SettingsActivity.pref_key_signalBarColorOption, 1));
  }

  private void setSignalBarColorOption(int colorOption) {
    SharedPreferences preferences =
        this.mContext.getSharedPreferences(SettingsActivity.prefname_settings, 0);
    switch (colorOption) {
      case 0:
      case 1:
        preferences
            .edit()
            .putInt(SettingsActivity.pref_key_signalBarColorOption, colorOption)
            .apply();
        for (int i = 1; i < 6; i++) {
          int color = -1;
          switch (colorOption) {
            case 0:
              color = preferences.getInt(SettingsActivity.pref_key_stationNameColor, -1);
              break;
            case 1:
              color =
                  getResources()
                      .getColor(
                          getResources().getIdentifier("signal_" + i, "color", getPackageName()));
              break;
          }
          ImageView imgView =
              (ImageView)
                  findViewById(
                      getResources()
                          .getIdentifier(
                              "imgSettingSignal_" + i, DabService.EXTRA_ID, getPackageName()));
          if (imgView != null) {
            imgView.setColorFilter(color);
          }
        }
        return;
      default:
        return;
    }
  }

  private void setSlideshowAndMotEnabled(boolean isEnabled) {
    SharedPreferences.Editor preferences =
        this.mContext.getSharedPreferences(SettingsActivity.prefname_settings, 0).edit();
    preferences.putBoolean(SettingsActivity.pref_key_motSlideshowEnabled, isEnabled);
    preferences.apply();
  }
}

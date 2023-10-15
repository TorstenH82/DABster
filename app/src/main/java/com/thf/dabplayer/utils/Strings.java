package com.thf.dabplayer.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.annotation.NonNull;
// import android.support.p000v4.view.ViewCompat;
import androidx.core.view.ViewCompat;
import com.thf.dabplayer.R;
import com.thf.dabplayer.activity.SettingsActivity;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/* renamed from: com.ex.dabplayer.pad.utils.Strings */
/* loaded from: classes.dex */
public class Strings {
  private static Map<String, Integer> channel2freq;
  private static String[] pty_names;
  private static boolean show_additional_infos;

  @NonNull
  public static String scanning(@NonNull Context ct, int percent, int freq_kHz) {
    if (percent == 0) {
      SharedPreferences sp = ct.getSharedPreferences(SettingsActivity.prefname_settings, 0);
      show_additional_infos = sp.getBoolean(SettingsActivity.pref_key_showAdditionalInfos, true);
    }
    StringBuilder sb = new StringBuilder(ct.getResources().getString(R.string.scanning));
    sb.append(" ").append(percent).append("%");
    if (show_additional_infos) {
      sb.append("\n                                        \n");
      if (freq_kHz != 0) {
        String channel_name = C0162a.dabFreqToChannelMap.get(freq_kHz, "");
        if (channel_name.length() == 0) {
          C0162a.m9a("no channel name for " + freq_kHz + "kHz");
        }
        sb.append(channel_name)
            .append(
                String.format(
                    " - %d,%03d MHz",
                    Integer.valueOf(freq_kHz / 1000), Integer.valueOf(freq_kHz % 1000)));
      }
    }
    return sb.toString();
  }

  public static String PTYname(@NonNull Context ct, int number) {
    if (pty_names == null) {
      pty_names = ct.getResources().getStringArray(R.array.pty_array);
      C0162a.m9a("PTYnames length=" + pty_names.length);
    }
    if (number < 0 || number >= pty_names.length) {
      number = pty_names.length - 1;
    }
    return pty_names[number];
  }

  public static int PTYnumber(@NonNull String name) {
    if (pty_names == null) {
      return 0;
    }
    int number = 0;
    while (number < pty_names.length - 1 && !name.equals(pty_names[number])) {
      number++;
    }
    return number;
  }

  public static String freq2channelname(int frequency) {
    int frequency2 = frequency & ViewCompat.MEASURED_SIZE_MASK;
    String ch = C0162a.dabFreqToChannelMap.get(frequency2, null);
    return ch != null ? ch : String.format("%d", Integer.valueOf(frequency2));
  }

  public static int channelname2freq(@NonNull String channel) {
    if (channel2freq == null) {
      int entries = C0162a.dabFreqToChannelMap.size();
      channel2freq = new HashMap(entries);
      for (int i = 0; i < entries; i++) {
        channel2freq.put(
            C0162a.dabFreqToChannelMap.valueAt(i),
            Integer.valueOf(C0162a.dabFreqToChannelMap.keyAt(i)));
      }
    }
    Integer freq = channel2freq.get(channel);
    if (freq == null) {
      return 0;
    }
    return freq.intValue();
  }

  public static String DAB_path() {
    return Environment.getExternalStorageDirectory() + File.separator + "DAB";
  }
}
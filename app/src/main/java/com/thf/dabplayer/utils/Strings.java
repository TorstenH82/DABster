package com.thf.dabplayer.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.annotation.NonNull;
// import android.support.p000v4.view.ViewCompat;
import android.util.SparseArray;
import androidx.core.view.ViewCompat;
import com.thf.dabplayer.R;

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
    String sb = "";

    // if (percent == 0) {
    // SharedPreferences sp = ct.getSharedPreferences(SettingsActivity.prefname_settings, 0);
    //  show_additional_infos =
    //     true; // sp.getBoolean(SettingsActivity.pref_key_showAdditionalInfos, true);
    // }
    // StringBuilder sb = new StringBuilder(ct.getResources().getString(R.string.scanning));
    // sb.append(" ").append(percent).append("%");
    // if (show_additional_infos) {
    // sb.append("\n                                        \n");
    if (freq_kHz != 0) {
      String channel_name = dabFreqToChannelMap.get(freq_kHz, "");
      if (channel_name.length() == 0) {
        Logger.d("no channel name for " + freq_kHz + "kHz");
      }
      sb =
          channel_name
              + String.format(
                  " - %d,%03d MHz",
                  Integer.valueOf(freq_kHz / 1000), Integer.valueOf(freq_kHz % 1000));
      // }
    }
    return sb;
  }

  public static String PTYname(@NonNull Context ct, int number) {
    if (pty_names == null) {
      pty_names = ct.getResources().getStringArray(R.array.pty_array);
      Logger.d("PTYnames length=" + pty_names.length);
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
    String ch = dabFreqToChannelMap.get(frequency2, null);
    return ch != null ? ch : String.format("%d", Integer.valueOf(frequency2));
  }

  public static int channelname2freq(@NonNull String channel) {
    if (channel2freq == null) {
      int entries = dabFreqToChannelMap.size();
      channel2freq = new HashMap(entries);
      for (int i = 0; i < entries; i++) {
        channel2freq.put(
            dabFreqToChannelMap.valueAt(i),
            Integer.valueOf(dabFreqToChannelMap.keyAt(i)));
      }
    }
    Integer freq = channel2freq.get(channel);
    if (freq == null) {
      return 0;
    }
    return freq.intValue();
  }

  public static String DAB_path() {
    return Environment.getExternalStorageDirectory() + File.separator + "DABster";
  }

  public static final SparseArray<String> dabFreqToChannelMap =
      new SparseArray<String>() { // from class: com.ex.dabplayer.pad.utils.a.1
        {
          append(174928, "5A");
          append(176640, "5B");
          append(178352, "5C");
          append(180064, "5D");
          append(181936, "6A");
          append(183648, "6B");
          append(185360, "6C");
          append(187072, "6D");
          append(188928, "7A");
          append(190640, "7B");
          append(192352, "7C");
          append(194064, "7D");
          append(195936, "8A");
          append(197648, "8B");
          append(199360, "8C");
          append(201072, "8D");
          append(202928, "9A");
          append(204640, "9B");
          append(206352, "9C");
          append(208064, "9D");
          append(209936, "10A");
          append(210096, "10N");
          append(211648, "10B");
          append(213360, "10C");
          append(215072, "10D");
          append(216928, "11A");
          append(217088, "11N");
          append(218640, "11B");
          append(220352, "11C");
          append(222064, "11D");
          append(223936, "12A");
          append(224096, "12N");
          append(225648, "12B");
          append(227360, "12C");
          append(229072, "12D");
          append(230784, "13A");
          append(232496, "13B");
          append(234208, "13C");
          append(235776, "13D");
          append(237488, "13E");
          append(239200, "13F");
        }
      };

  private static final String LOGO_PATH =
      (Strings.DAB_path() + File.separator + "logos" + File.separator);
  public static final String LOGO_PATH_TMP = (LOGO_PATH + "tmp" + File.separator);
  public static final String LOGO_PATH_USER = (LOGO_PATH + "user" + File.separator);
}
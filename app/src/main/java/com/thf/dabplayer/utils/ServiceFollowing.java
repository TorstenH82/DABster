package com.thf.dabplayer.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
// import android.support.p000v4.view.ViewCompat;
import androidx.core.view.ViewCompat;
import com.thf.dabplayer.R;
import com.thf.dabplayer.activity.Player;
import com.thf.dabplayer.activity.SettingsActivity;
import com.thf.dabplayer.dab.DabSubChannelInfo;
import com.thf.dabplayer.service.DabService;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

/* renamed from: com.ex.dabplayer.pad.utils.ServiceFollowing */
/* loaded from: classes.dex */
public class ServiceFollowing {
  private static final int EVENT_HORIZON = 10;
  private static Cluster builtin_freq;
  private static DabSubChannelInfo follow_from;
  private static DabSubChannelInfo man_tune;
  private static String text_disabled;
  private int[] table_frequencies;
  private int[] table_sids;
  private static boolean is_enabled = false;
  private static int report_freq = 0;
  private static boolean is_supplied = false;
  private static boolean use_network_following = false;
  private static LinkedList<logEntry> eventlog = new LinkedList<>();
  private static String network_info = "";
  private int table_freq_index = 0;
  private boolean new_table = true;

  private static Cluster need_builtin_freq() {
    return need_builtin_freq(null);
  }

  private static boolean write_cluster_file(@NonNull File file, @NonNull String[] lines) {
    try {
      if ((!file.exists() || file.delete()) && file.createNewFile()) {
        C0162a.m5a("cluster file created: ", file.getName());
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        for (String line : lines) {
          builtin_freq.add(line);
          bw.append((CharSequence) line);
          bw.newLine();
        }
        bw.close();
        C0162a.m8a("cluster lines written=", lines.length);
        return true;
      }
      return false;
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }

  private static int get_version(String line) {
    if (line != null) {
      String[] parts = line.trim().split("\\.version");
      if (parts.length == 2 && parts[0].length() == 0) {
        String s = parts[1].trim();
        if (s.matches("[0-9]+")) {
          return Integer.parseInt(s);
        }
      }
    }
    return -1;
  }

  private static Cluster need_builtin_freq(Context ct) {
    int file_version;
    if (builtin_freq == null && ct != null) {
      boolean ok = false;
      builtin_freq = new Cluster();
      String[] lines = ct.getResources().getStringArray(R.array.servicefollow_array);
      File file = new File(Strings.DAB_path(), "cluster.conf");
      if (file.canRead()) {
        try {
          BufferedReader br = new BufferedReader(new FileReader(file));
          int our_version = get_version(lines[0]);
          C0162a.m5a("cluster read file: ", "cluster.conf");
          use_network_following = false;
          String line = br.readLine();
          if (line != null
              && ((file_version = get_version(line)) < 0 || file_version >= our_version)) {
            while (line != null) {
              if (line.equals("\\.network")) {
                use_network_following = true;
              } else {
                builtin_freq.add(line);
              }
              line = br.readLine();
              ok = true;
            }
          }
          br.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      if (!ok) {
        File dir = new File(Strings.DAB_path());
        try {
          if (!dir.exists()) {
            dir.mkdirs();
          }
        } catch (SecurityException e2) {
          e2.printStackTrace();
        }
        if (!write_cluster_file(file, lines)) {
          C0162a.m9a("can't use cluster file");
          for (String line2 : lines) {
            builtin_freq.add(line2);
          }
        }
      }
    }
    return builtin_freq;
  }

  public ServiceFollowing(int[] freqs, int[] sids) {
    this.table_frequencies = freqs;
    this.table_sids = sids;
    follow_from = man_tune;
    if (need_builtin_freq() != null && man_tune != null) {
      builtin_freq.initiate_frequency(man_tune.mFreq, man_tune.mEID);
    }
  }

  public synchronized int next_frequency() {
    int freq;
    do {
      if (this.new_table) {
        this.new_table = false;
      } else if (use_network_following && this.table_freq_index < this.table_frequencies.length) {
        this.table_freq_index++;
      } else if (builtin_freq == null || !builtin_freq.goto_next_frequency()) {
        freq = 0;
        break;
      }
      freq = frequency();
    } while (freq == 0);
    return freq;
  }

  private int frequency() {
    int freq = 0;
    if (use_network_following && this.table_freq_index < this.table_frequencies.length) {
      freq = this.table_frequencies[this.table_freq_index] & ViewCompat.MEASURED_SIZE_MASK;
    } else if (builtin_freq != null) {
      freq = builtin_freq.frequency();
    }
    C0162a.m8a("servicefollow frequency:", freq);
    return freq;
  }

  public synchronized boolean change_frequency() {
    boolean z = true;
    synchronized (this) {
      if (use_network_following && this.table_freq_index < this.table_frequencies.length) {
        if ((this.table_frequencies[this.table_freq_index] & Integer.MIN_VALUE) == 0) {
          z = false;
        }
      }
    }
    return z;
  }

  private boolean sid_is_ok(int sid) {
    C0162a.m9a(String.format("servicefollow found sid: %04X", Integer.valueOf(sid)));
    return true;
  }

  public synchronized boolean find_sid(int sid) {
    int[] iArr;
    boolean z = false;
    synchronized (this) {
      if (use_network_following && this.table_freq_index < this.table_frequencies.length) {
        for (int s : this.table_sids) {
          if (sid == s) {
            z = sid_is_ok(sid);
            break;
          }
        }
        C0162a.m9a(String.format("servicefollow did not find sid: %04X", Integer.valueOf(sid)));
      } else {
        if (man_tune != null && sid == man_tune.mSID) {
          z = sid_is_ok(sid);
        }
        C0162a.m9a(String.format("servicefollow did not find sid: %04X", Integer.valueOf(sid)));
      }
    }
    return z;
  }

  public static synchronized void update_enabled_status(Context ct) {
    synchronized (ServiceFollowing.class) {
      boolean was_enabled = is_enabled;

      is_enabled = SharedPreferencesHelper.getInstance().getBoolean("serviceFollowing");
      text_disabled = ct.getResources().getString(R.string.Disabled);
      need_builtin_freq(ct);
      if (was_enabled != is_enabled) {
        update_info();
      }
    }
  }

  public static boolean is_enabled() {
    return is_enabled;
  }

  public static synchronized boolean is_possible() {
    boolean z = false;
    synchronized (ServiceFollowing.class) {
      if (is_enabled) {
        if (is_supplied) {
          z = true;
        } else if (need_builtin_freq() != null
            && man_tune != null
            && builtin_freq.find_ensemble(man_tune.mFreq, man_tune.mEID)) {
          C0162a.m9a("servicefollow possible");
          z = true;
        } else if (man_tune == null || report_freq != man_tune.mFreq) {
          report_freq = man_tune.mFreq;
          C0162a.m8a("servicefollow not possible for ", report_freq);
        }
      }
    }
    return z;
  }

  /* JADX INFO: Access modifiers changed from: package-private */
  /* renamed from: com.ex.dabplayer.pad.utils.ServiceFollowing$logEntry */
  /* loaded from: classes.dex */
  public static class logEntry {
    private long begin;
    private String desc;
    private long end;

    public logEntry(String info) {
      this.desc = info;
      long currentTimeMillis = System.currentTimeMillis();
      this.end = currentTimeMillis;
      this.begin = currentTimeMillis;
    }

    public boolean is_same(String info) {
      if (this.desc == null || !this.desc.equals(info)) {
        return false;
      }
      this.end = System.currentTimeMillis();
      return true;
    }

    public StringBuilder append_info(StringBuilder sb) {
      if (this.desc != null) {
        sb.append(new SimpleDateFormat("HH:mm").format(new Date(this.begin)));
        if (this.begin != this.end) {
          sb.append(new SimpleDateFormat("-HH:mm").format(new Date(this.end)));
        }
        sb.append(' ').append(this.desc);
      }
      return sb;
    }
  }

  private static String follow_info(DabSubChannelInfo follow_to) {
    StringBuilder sb = new StringBuilder();
    if (follow_from != null) {
      sb.append(make_channel_info(follow_from));
      sb.append("→");
      if (follow_to != null) {
        sb.append(Strings.freq2channelname(follow_to.mFreq));
      } else {
        sb.append("?");
      }
    }
    return sb.toString();
  }

  private static String network_string(int[] freqs, int[] sids) {
    StringBuilder sb = new StringBuilder();
    int tally = 0;
    if (freqs != null) {
      for (int i = 0; i < freqs.length; i++) {
        if (freqs[i] != 0) {
          int tally2 = tally + 1;
          sb.append(tally == 0 ? '(' : ',');
          if ((freqs[i] & Integer.MIN_VALUE) != 0) {
            sb.append('*');
          }
          sb.append(Strings.freq2channelname(freqs[i]));
          tally = tally2;
        }
      }
      int i2 = 0;
      while (i2 < sids.length) {
        if (sids[i2] != 0) {
          sb.append(i2 == 0 ? '/' : ',');
          sb.append(String.format("%04X", Integer.valueOf(sids[i2])));
        }
        i2++;
      }
      if (tally > 0) {
        sb.append(") ");
      }
    }
    if (man_tune != null && need_builtin_freq() != null) {
      sb.append(builtin_freq.list_of_members(man_tune.mFreq, man_tune.mEID));
    }
    return sb.toString();
  }

  private static String info_status() {
    if (is_enabled) {
      return network_info;
    }
    return text_disabled == null ? "-" : text_disabled;
  }

  private static String info_log() {
    StringBuilder sb = new StringBuilder();
    String separator = "";
    Iterator<logEntry> it = eventlog.iterator();
    while (it.hasNext()) {
      logEntry e = it.next();
      sb.append(separator);
      e.append_info(sb);
      separator = " 🔸 ";
    }
    return sb.toString();
  }

  private static void update_info() {
    Handler handler;
    WeakReference<Handler> playerHandler = Player.getPlayerHandler();
    if (playerHandler != null && (handler = playerHandler.get()) != null) {
      Intent intent = new Intent(DabService.META_CHANGED);
      intent.putExtra(DabService.EXTRA_SENDER, DabService.SENDER_DAB);
      intent.putExtra(DabService.EXTRA_SERVICEFOLLOWING, info_status());
      intent.putExtra(DabService.EXTRA_SERVICELOG, info_log());
      Message intentMessage = handler.obtainMessage();
      intentMessage.what = 100;
      intentMessage.obj = intent;
      handler.sendMessage(intentMessage);
    }
  }

  private static String make_channel_info(@NonNull DabSubChannelInfo info) {
    return String.format(
        "%s/%04X", Strings.freq2channelname(info.mFreq), Integer.valueOf(info.mEID));
  }

  public static void manTune(DabSubChannelInfo info) {
    manTune(info, null, null);
  }

  public static synchronized void manTune(DabSubChannelInfo info, int[] freqs, int[] sids) {
    int i = 0;
    synchronized (ServiceFollowing.class) {
      man_tune = info;
      network_info = network_string(freqs, sids);
      is_supplied = false;
      if (freqs != null) {
        int length = freqs.length;
        while (true) {
          if (i >= length) {
            break;
          }
          int f = freqs[i];
          if (f == 0) {
            i++;
          } else {
            is_supplied = true;
            break;
          }
        }
      }
      follow_from = null;
      eventlog.clear();
      update_info();
    }
  }

  public static synchronized void autoTune(DabSubChannelInfo info) {
    synchronized (ServiceFollowing.class) {
      record_latest(follow_info(null));
      man_tune = info;
      update_info();
    }
  }

  public static synchronized void autoTune(DabSubChannelInfo info, int[] freqs, int[] sids) {
    synchronized (ServiceFollowing.class) {
      record_latest(follow_info(info));
      man_tune = info;
      network_info = network_string(freqs, sids);
      update_info();
    }
  }

  private static void record_latest(@NonNull String new_info) {
    if (eventlog.isEmpty() || !eventlog.getLast().is_same(new_info)) {
      if (eventlog.size() >= 10) {
        eventlog.remove();
      }
      eventlog.add(new logEntry(new_info));
    }
  }
}

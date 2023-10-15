package com.thf.dabplayer.utils;

import android.support.annotation.NonNull;
import android.util.SparseArray;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* renamed from: com.ex.dabplayer.pad.utils.Cluster */
/* loaded from: classes.dex */
public class Cluster {
  private List<SparseArray> clusters = new ArrayList();
  SparseArray<List<Integer>> iterate_cluster = null;
  int iterate_from = 0;
  int iterator = 0;
  boolean iterate_last = true;

  private void add1freq(SparseArray<List<Integer>> cluster, int freq, int eid) {
    List<Integer> eids = cluster.get(freq);
    if (eids == null) {
      List<Integer> eids2 = new ArrayList<>(2);
      eids2.add(new Integer(eid));
      cluster.append(freq, eids2);
      return;
    }
    for (Integer e : eids) {
      if (e.intValue() == eid) {
        C0162a.m6a("cluster dup freq=", freq, " eid=", eid);
        return;
      }
    }
    eids.add(new Integer(eid));
  }

  private boolean contains_eid(List<Integer> eids, int eid) {
    if (eids != null) {
      for (Integer e : eids) {
        if (e.intValue() == eid) {
          return true;
        }
      }
    }
    return false;
  }

  public synchronized String list_of_members(int i, int i2) {
    String members = null;;
    for (SparseArray sparseArray : this.clusters) {
      int indexOfKey = sparseArray.indexOfKey(i);
      if (indexOfKey >= 0 && contains_eid((List) sparseArray.valueAt(indexOfKey), i2)) {
        StringBuilder stringBuilder2 = null;
        int idx = indexOfKey;
        while (true) {
          idx++;
          if (idx >= sparseArray.size()) {
            idx = 0;
          }
          if (idx == indexOfKey) {
            break;
          }
          if (members != null) {
            members += ",";
          }
          members+=Strings.freq2channelname(sparseArray.keyAt(idx));
        }
        if (members != null) {
          break;
        }
      }
    }
    return (members == null ? "" : members);
  }

  public synchronized boolean initiate_frequency(int freq, int eid) {
    boolean z;
    int i;
    Iterator<SparseArray> it = this.clusters.iterator();
    while (true) {
      if (!it.hasNext()) {
        z = false;
        break;
      }
      SparseArray<List<Integer>> cluster = it.next();
      if (contains_eid(cluster.get(freq), eid) && (i = cluster.indexOfKey(freq)) >= 0) {
        this.iterate_cluster = cluster;
        this.iterator = i;
        this.iterate_from = i;
        this.iterate_last = false;
        if (next_iterator()) {
          z = true;
          break;
        }
        this.iterate_cluster = null;
      }
    }
    return z;
  }

  public synchronized boolean find_ensemble(int freq, int eid) {
    boolean z;
    Iterator<SparseArray> it = this.clusters.iterator();
    while (true) {
      if (!it.hasNext()) {
        z = false;
        break;
      }
      SparseArray<List<Integer>> cluster = it.next();
      if (contains_eid(cluster.get(freq), eid)) {
        z = true;
        break;
      }
    }
    return z;
  }

  public synchronized boolean goto_next_frequency() {
    return next_iterator();
  }

  private boolean next_iterator() {
    if (this.iterate_cluster == null) {
      return false;
    }
    if (this.iterate_last) {
      this.iterate_cluster = null;
      return false;
    }
    int i = this.iterator + 1;
    this.iterator = i;
    if (i >= this.iterate_cluster.size()) {
      this.iterator = 0;
    }
    if (this.iterator == this.iterate_from) {
      this.iterate_last = true;
      if (this.iterate_cluster.valueAt(this.iterator).size() <= 1) {
        this.iterate_cluster = null;
        return false;
      }
    }
    return true;
  }

  public synchronized int frequency() {
    return this.iterate_cluster == null ? 0 : this.iterate_cluster.keyAt(this.iterator);
  }

  public synchronized void add(@NonNull String cl) {
    int eid;
    String f;
    int freq;
    SparseArray<List<Integer>> cluster = new SparseArray<>();
    int where = 0;
    int hash = cl.indexOf(35, 0);
    if (hash >= 0) {
      cl = cl.substring(0, hash);
    }
    String cl2 = cl.trim();
    if (cl2.length() <= 0 || cl2.charAt(0) != '.') {
      while (true) {
        int slash = cl2.indexOf(47, where);
        if (slash < 0) {
          break;
        }
        int end_eid = cl2.indexOf(44, slash + 1);
        if (end_eid < 0) {
          end_eid = cl2.length();
        }
        String frequencies_s = cl2.substring(where, slash).trim();
        try {
          eid = Integer.parseInt(cl2.substring(slash + 1, end_eid).trim(), 16);
        } catch (NumberFormatException e) {
          C0162a.m5a("cluster eid bad: ", cl2.substring(slash + 1, end_eid));
          eid = -1;
        }
        if (eid >= 0) {
          do {
            int comma = frequencies_s.indexOf(44, where);
            if (comma < 0) {
              f = frequencies_s.trim();
              frequencies_s = null;
            } else {
              f = frequencies_s.substring(0, comma).trim();
              frequencies_s = frequencies_s.substring(comma + 1);
            }
            if (f.length() == 6 && f.matches("[0-9]+")) {
              freq = Integer.parseInt(f);
            } else {
              freq = Strings.channelname2freq(f);
            }
            if (freq > 0) {
              add1freq(cluster, freq, eid);
              continue;
            } else {
              C0162a.m5a("cluster freq bad: ", cl2.substring(where, slash));
              continue;
            }
          } while (frequencies_s != null);
        }
        where = end_eid + 1;
      }
      if (where < cl2.length()) {
        C0162a.m5a("cluster ignore: ", cl2.substring(where));
      }
      if (cluster.size() > 1) {
        this.clusters.add(cluster);
      }
    }
  }
}

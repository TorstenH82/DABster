package com.thf.dabplayer.dab;

import com.thf.dabplayer.utils.Logger;
import com.thf.dabplayer.utils.Strings;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/* renamed from: com.ex.dabplayer.pad.dab.e */
/* loaded from: classes.dex */
public class DabRecorder extends Thread {

  /* renamed from: a */
  private boolean f64a;

  /* renamed from: b */
  private String f65b;

  /* renamed from: c */
  private RingBuffer f66c;

  /* renamed from: d */
  private String f67d;

  public DabRecorder(int i, RingBuffer rVar, String str) {
    this.f66c = rVar;
    this.f67d = str.trim();
    if (i == 0) {
      this.f65b = ".aac";
    } else {
      this.f65b = ".mp3";
    }
    Logger.d("start dab recorder");
  }

  /* renamed from: b */
  private String m58b() {
    File file;
    int i = 0;
    String rec_path = Strings.DAB_path() + File.separator + "rec";
    File file2 = new File(rec_path);
    if (!file2.exists()) {
      try {
        file2.mkdirs();
      } catch (SecurityException e) {
        e.printStackTrace();
      }
    }
    while (true) {
      file = new File(String.valueOf(rec_path) + File.separator + this.f67d + "_" + i + this.f65b);
      if (file.exists()) {
        i++;
      } else {
        try {
          file.createNewFile();

          break;
        } catch (IOException e2) {
          e2.printStackTrace();
        }
      }
    }
    Logger.d("record: " + file.getAbsolutePath());
    return file.getAbsolutePath();
  }

  /* renamed from: a */
  public void m59a() {
    this.f64a = true;
  }

  @Override // java.lang.Thread, java.lang.Runnable
  public void run() {
    FileOutputStream fileOutputStream;
    byte[] bArr = new byte[10240];
    try {
      fileOutputStream = new FileOutputStream(new File(m58b()));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      fileOutputStream = null;
    }
    while (!this.f64a && fileOutputStream != null) {
      try {
        sleep(5L);
      } catch (InterruptedException e2) {
        e2.printStackTrace();
      }
      synchronized (this.f66c) {
        if (this.f66c.getNumSamplesAvailable() >= 10240) {
          this.f66c.readBuffer(bArr, 10240);
          try {
            fileOutputStream.write(bArr);
            fileOutputStream.flush();
          } catch (IOException e3) {
            e3.printStackTrace();
          }
        }
      }
    }
    try {
      fileOutputStream.flush();
      fileOutputStream.close();
    } catch (IOException e4) {
      e4.printStackTrace();
    }
  }
}
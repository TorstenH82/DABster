package com.thf.dabplayer.dab;

import com.thf.dabplayer.utils.Logger;
import com.thf.dabplayer.utils.Strings;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/* renamed from: com.ex.dabplayer.pad.dab.n */
/* loaded from: classes.dex */
public class FicRecorder extends Thread {

  /* renamed from: a */
  private boolean exit;

  /* renamed from: b */
  private String fic_extension = ".fic";

  /* renamed from: c */
  private RingBuffer ficRecorderRingBuffer;

  public FicRecorder(RingBuffer ficRecorderRingBuffer) {
    this.ficRecorderRingBuffer = ficRecorderRingBuffer;
    Logger.d("start fic recorder");
  }

  /* renamed from: b */
  private String m28b() {
    int i = 0;
    String rec_path = Strings.DAB_path() + File.separator + "rec";
    File file = new File(rec_path);
    if (!file.exists()) {
      file.mkdir();
    }
    while (true) {
      File file2 = new File(String.valueOf(rec_path) + File.separator + i + this.fic_extension);
      if (file2.exists()) {
        i++;
      } else {
        try {
          file2.createNewFile();
          Logger.d("record: " + file2.getAbsolutePath());
          return file2.getAbsolutePath();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  /* renamed from: a */
  public void exit() {
    this.exit = true;
  }

  @Override // java.lang.Thread, java.lang.Runnable
  public void run() {
    FileOutputStream fileOutputStream;
    byte[] bArr = new byte[30720];
    try {
      fileOutputStream = new FileOutputStream(new File(m28b()));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      fileOutputStream = null;
    }
    while (!this.exit) {
      try {
        sleep(5L);
      } catch (InterruptedException e2) {
        e2.printStackTrace();
      }
      synchronized (this.ficRecorderRingBuffer) {
        if (this.ficRecorderRingBuffer.getNumSamplesAvailable() >= 10240) {
          int a = this.ficRecorderRingBuffer.readBuffer(bArr, bArr.length);
          try {
            fileOutputStream.write(bArr, 0, a);
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

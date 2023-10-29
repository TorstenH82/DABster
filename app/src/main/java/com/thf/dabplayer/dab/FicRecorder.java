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
    private String f118b = ".fic";

    /* renamed from: c */
    private RingBuffer f119c;

    public FicRecorder(RingBuffer rVar) {
        this.f119c = rVar;
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
            File file2 = new File(String.valueOf(rec_path) + File.separator + i + this.f118b);
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
            synchronized (this.f119c) {
                if (this.f119c.getNumSamplesAvailable() >= 10240) {
                    int a = this.f119c.readBuffer(bArr, bArr.length);
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
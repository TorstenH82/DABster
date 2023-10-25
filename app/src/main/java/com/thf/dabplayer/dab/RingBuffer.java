package com.thf.dabplayer.dab;

import android.widget.Toast;
import com.thf.dabplayer.activity.PlayerActivity;

/* renamed from: com.ex.dabplayer.pad.dab.r */
/* loaded from: classes.dex */
public class RingBuffer {
    //private static final String LOGTAG = "r.class";
    public static final int P_INITIALBUFFERING_THRESHOLD = 0;
    public static final int P_LEVEL_THRESHOLD = 0;
    private static final boolean useInitialBuffering = false;
    private static final boolean useOldCode = false;
    private static final boolean useTracing = false;
    private byte[] byteArrSamples;
    PlayerActivity player;
    private int iSamplesSize;
    private int lastLevel = 0;
    private boolean inInitialBuffering = false;
    private int storePos = 0;
    private int readPos = 0;
    private int iNumSamplesAvailable = 0;

    public RingBuffer(int i) {
        this.byteArrSamples = new byte[i];
        this.iSamplesSize = i;
    }

    /* renamed from: a */
    public int readBuffer(byte[] bArr, int i) {
        if (this.iNumSamplesAvailable < i) {
            i = this.iNumSamplesAvailable;
        }
        if (this.readPos + i <= this.iSamplesSize) {
            //copy to bArr
            System.arraycopy(this.byteArrSamples, this.readPos, bArr, 0, i);
            this.readPos = (this.readPos + i) % this.iSamplesSize;
        } else {
            int i1 = this.iSamplesSize - this.readPos;
            int i2 = i - i1;
            System.arraycopy(this.byteArrSamples, this.readPos, bArr, 0, i1);
            System.arraycopy(this.byteArrSamples, 0, bArr, i1, i2);
            this.readPos = i2 % this.iSamplesSize;
        }
        this.iNumSamplesAvailable -= i;
        return i;
    }

    /* renamed from: a */
    public void reset() {
        this.storePos = 0;
        this.readPos = 0;
        this.iNumSamplesAvailable = 0;
    }

    /* renamed from: b */
    public int getNumSamplesAvailable() {
        return this.iNumSamplesAvailable;
    }

    /* renamed from: b */
    public int writeBuffer(byte[] bArr, int i) {
        int i3 = this.iSamplesSize - this.iNumSamplesAvailable;
        if (i3 <= i) {
            i = i3;
        }
        if (this.storePos + i <= this.iSamplesSize) {
            System.arraycopy(bArr, 0, this.byteArrSamples, this.storePos, i);
            this.storePos = (this.storePos + i) % this.iSamplesSize;
        } else {
            int i1 = this.iSamplesSize - this.storePos;
            int i2 = i - i1;
            System.arraycopy(bArr, 0, this.byteArrSamples, this.storePos, i1);
            System.arraycopy(bArr, i1, this.byteArrSamples, 0, i2);
            this.storePos = i2 % this.iSamplesSize;
        }
        this.iNumSamplesAvailable += i;
        return i;
    }

    /* renamed from: c */
    public int getRemainingCapacity() {
        return this.iSamplesSize - this.iNumSamplesAvailable;
    }

    public void checkLevel(String why) {
        showToast();
    }

    public int getReadPos() {
        return this.readPos;
    }

    public int getStorePos() {
        return this.storePos;
    }

    public int getLastLevelReported() {
        return this.lastLevel;
    }

    public void setPlayer(PlayerActivity p) {
        this.player = p;
    }

    public void showToast() {
        this.player.runOnUiThread(new Runnable() { // from class: com.ex.dabplayer.pad.dab.r.1
            @Override // java.lang.Runnable
            public void run() {
                Toast.makeText(RingBuffer.this.player, "AUDIO_FOCUS_LOST", 0).show();
            }
        });
    }
}
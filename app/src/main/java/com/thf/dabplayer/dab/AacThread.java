package com.thf.dabplayer.dab;

import android.content.Context;
import android.os.Process;
import com.thf.dabplayer.utils.C0162a;

/* renamed from: com.ex.dabplayer.pad.dab.a */
/* loaded from: classes.dex */
public class AacThread extends Thread {
  private Context mContext;
  private final RingBuffer mInputAacRingBuffer;
  private PcmThread mPcmThread = null;
  private final RingBuffer mOutputPcmRingBuffer = new RingBuffer(204800);
  private DabDec mDabDecoder = new DabDec();
  private boolean exit = false;
  private int mTick = 0;

  public AacThread(Context context, RingBuffer ringBuffer) {
    this.mInputAacRingBuffer = ringBuffer;
    this.mContext = context;
  }

  /* renamed from: a */
  public void exit() {
    this.exit = true;
    C0162a.m9a("aac thread about to exit");
  }

  @Override // java.lang.Thread, java.lang.Runnable
  public void run() {
    boolean isAudioTrackInitial = false;
    byte[] array = new byte[25600];
    C0162a.m9a("aac thread run");
    try {
      Process.setThreadPriority(-16);
      setName("aac");
    } catch (IllegalArgumentException | SecurityException e) {
      e.printStackTrace();
    }
    if (this.mDabDecoder.decoder_init(0) < 0) {
      C0162a.m9a("aac player init fail");
      return;
    }
    while (!this.exit) {
      try {
        sleep(1L);
      } catch (InterruptedException e2) {
        e2.printStackTrace();
      }

      C0162a.m9a("available data " + this.mInputAacRingBuffer.getNumSamplesAvailable());

      int decoder_feed_data = -1;
      int isRead = this.mDabDecoder.decoder_is_feed_data(0);
      C0162a.m9a("thf: isRead is " + isRead);
      if (isRead == 1) {
        synchronized (this.mInputAacRingBuffer) {
          if (this.mInputAacRingBuffer.getNumSamplesAvailable() >= 1024) {
            int len = this.mInputAacRingBuffer.readBuffer(array, 1024);
            C0162a.m9a("length read from buffer " + len);
            if (len == 1024) {
              decoder_feed_data = this.mDabDecoder.decoder_feed_data(0, array, len);
              C0162a.m9a("decoder feed data is " + decoder_feed_data);
              if (decoder_feed_data < 0) {
                C0162a.m9a("feed data fail, " + decoder_feed_data + " bytes");
                continue;
              }
            } else {
              C0162a.m9a("nothing to feed, only " + len);
              continue;
            }
          } else {
            C0162a.m9a("not enough data in buffer");
            continue;
          }
        }
      }

      String strLen = array == null ? "null" : array.length + "";

      C0162a.m9a("length of array to decode: " + strLen);

      int decoder_decode = 0;
      // if (array != null && array.length > 0) {

      decoder_decode = this.mDabDecoder.decoder_decode(0, array);
      C0162a.m9a("decoder return code is " + decoder_decode);
      // }

      if (decoder_decode < 0) {
        C0162a.m9a("aac decode fail exit");
        break;
      } else if (decoder_decode == 0) {
        C0162a.m9a("decode continue");
        continue;
      } else {
        if (!isAudioTrackInitial) {
          C0162a.m9a("audio track not initial");
          int samplerate = this.mDabDecoder.decoder_get_samplerate(0);
          int channels = this.mDabDecoder.decoder_get_channels(0);
          this.mPcmThread =
              new PcmThread(this.mContext, this.mOutputPcmRingBuffer, samplerate, channels);
          this.mPcmThread.start();
          isAudioTrackInitial = true;
        }
        if (this.mTick < 5) {
          this.mTick++;
        } else {
          synchronized (this.mOutputPcmRingBuffer) {
            this.mOutputPcmRingBuffer.writeBuffer(array, decoder_decode);
          }
        }
      }
    } // end while

    if (this.mPcmThread != null) {
      this.mPcmThread.m25a();
      try {
        this.mPcmThread.join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    this.mDabDecoder.decoder_close(0);
    C0162a.m9a("aac thread exit");
  }

  public void setAudioState(int audioState) {
    if (this.mPcmThread != null) {
      this.mPcmThread.setAudioState(audioState);
    }
  }
}

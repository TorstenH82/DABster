package com.thf.dabplayer.dab;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import com.thf.dabplayer.activity.PlayerActivity;

import com.thf.dabplayer.service.DabService;
import com.thf.dabplayer.utils.AudioTools;
import com.thf.dabplayer.utils.C0162a;
import com.thf.dabplayer.utils.ClippingAreaDetection;
import com.thf.dabplayer.utils.SharedPreferencesHelper;
import java.lang.ref.WeakReference;

/* renamed from: com.ex.dabplayer.pad.dab.o */
/* loaded from: classes.dex */
public class Mp2Thread extends Thread {

  /* renamed from: b */
  private final RingBuffer f121b;

  /* renamed from: d */
  private int f123d;

  /* renamed from: e */
  private AudioTrack f124e;

  /* renamed from: g */
  private Context f126g;
  private boolean mIsClippedSampleDetectionEnabled;
  private boolean mIsClippedSampleNotificationEnabled;
  private SharedPrefListener mPrefListener;

  /* renamed from: a */
  private boolean f120a = false;

  /* renamed from: c */
  private DabDec f122c = new DabDec();

  /* renamed from: f */
  private boolean f125f = false;
  private int mSampleRateInHz = 0;
  private int mAudioState = DabThread.AUDIOSTATE_PLAY;

  /* renamed from: com.ex.dabplayer.pad.dab.o$SharedPrefListener */
  /* loaded from: classes.dex */
  private class SharedPrefListener implements SharedPreferences.OnSharedPreferenceChangeListener {
    SharedPrefListener() {}

    @Override // android.content.SharedPreferences.OnSharedPreferenceChangeListener
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
      if (key.equals("suppressNoise")) {
        Mp2Thread.this.mIsClippedSampleDetectionEnabled =
            SharedPreferencesHelper.getInstance().getBoolean("suppressNoise");

        Mp2Thread.this.mIsClippedSampleNotificationEnabled =
            SharedPreferencesHelper.getInstance().getBoolean("suppressNoise");
      } else if (key.equals("volume")) {
        // float volume = sharedPreferences.getFloat(SettingsActivity.pref_key_audioLevel, 1.0f);
        // if (Mp2Thread.this.f124e != null) {
        //   AudioTools.setVolume(Mp2Thread.this.f124e, volume);
        // }
      }
    }
  }

  public Mp2Thread(Context context, RingBuffer rVar) {
    this.mIsClippedSampleDetectionEnabled = false;
    this.mIsClippedSampleNotificationEnabled = false;
    this.f121b = rVar;
    this.f126g = context;

    this.mIsClippedSampleDetectionEnabled =
        SharedPreferencesHelper.getInstance().getBoolean("suppressNoise");
    this.mIsClippedSampleNotificationEnabled =
        SharedPreferencesHelper.getInstance().getBoolean("suppressNoise");
    this.mPrefListener = new SharedPrefListener();
    SharedPreferencesHelper.getInstance()
        .getSharedPreferences()
        .registerOnSharedPreferenceChangeListener(this.mPrefListener);
  }

  /* renamed from: a */
  private void m26a(int sampleRateInHz, int channels) {
    this.mSampleRateInHz = sampleRateInHz;
    if (sampleRateInHz > 0) {
      int channelConfig = channels == 1 ? 2 : 3;
      this.f123d = AudioTrack.getMinBufferSize(sampleRateInHz, channelConfig, 2);
      C0162a.m9a("pcm min buffer size: " + this.f123d);
      this.f124e = new AudioTrack(3, sampleRateInHz, channelConfig, 2, this.f123d, 1);

      float volume = 1.0f; // pref_settings.getFloat(SettingsActivity.pref_key_audioLevel, 1.0f);
      AudioTools.setVolume(this.f124e, volume);
      this.f124e.play();
    }
  }

  /* renamed from: a */
  public void exit() {
    this.f120a = true;
    C0162a.m9a("mp2 player about to exit");
  }

  private void notifyIntent(int samplerate, boolean isPlaying) {
    Handler handler;
    WeakReference<Handler> playerHandler = PlayerActivity.getPlayerHandler();
    if (playerHandler != null && (handler = playerHandler.get()) != null) {
      Intent intent = new Intent(DabService.META_CHANGED);
      intent.putExtra(DabService.EXTRA_SENDER, DabService.SENDER_DAB);
      intent.putExtra(DabService.EXTRA_AUDIOSAMPLERATE, samplerate);
      intent.putExtra("playing", isPlaying);
      Message intentMessage = handler.obtainMessage();
      intentMessage.what = 100;
      intentMessage.obj = intent;
      handler.sendMessage(intentMessage);
    }
  }

  @Override // java.lang.Thread, java.lang.Runnable
  public void run() {
    int a;
    byte[] bArr = new byte[6144];
    byte[] bArr2 = new byte[61440];
    try {
      Process.setThreadPriority(-16);
      setName("mp2");
    } catch (IllegalArgumentException | SecurityException e) {
      e.printStackTrace();
    }
    if (this.f122c.decoder_init(1) < 0) {
      C0162a.m9a("mp2 player init fail");
      return;
    }
    C0162a.m9a("mp2 player run");
    ClippingAreaDetection cad = null;
    int i = 0;
    int i2 = 0;
    boolean z = true;
    while (!this.f120a) {
      try {
        sleep(1L);
      } catch (InterruptedException e2) {
        e2.printStackTrace();
      }
      if (z) {
        synchronized (this.f121b) {
          if (this.f121b.getNumSamplesAvailable() >= 1024
              && (a = this.f121b.readBuffer(bArr, 1024)) == 1024) {
            int decoder_feed_data = this.f122c.decoder_feed_data(1, bArr, a);
            if (decoder_feed_data < 0) {
              C0162a.m9a("feed data fail, " + decoder_feed_data + " bytes");
            }
            z = false;
          }
        }
      }
      int a2 = this.f122c.decoder_decode(1, bArr);
      if (a2 < 0) {
        z = true;
      } else {
        if (i2 == 0) {
          int decoder_get_samplerate = this.f122c.decoder_get_samplerate(1);
          int decoder_get_channels = this.f122c.decoder_get_channels(1);
          C0162a.m9a("samplerate:" + decoder_get_samplerate + ", channels:" + decoder_get_channels);
          if (decoder_get_samplerate > 0) {
            m26a(decoder_get_samplerate, decoder_get_channels);
            i2 = 1;
            if (decoder_get_channels > 0 && decoder_get_channels < 3) {
              cad = new ClippingAreaDetection(decoder_get_channels);
            }
          }
        }
        if (i + a2 < bArr2.length) {
          for (int decoder_get_samplerate2 = 0;
              decoder_get_samplerate2 < a2;
              decoder_get_samplerate2++) {
            bArr2[i + decoder_get_samplerate2] = bArr[decoder_get_samplerate2];
          }
          i += a2;
        }
        if (i >= this.f123d && this.f124e != null) {
          switch (this.mAudioState) {
            case DabThread.AUDIOSTATE_PLAY /* 200 */:
            case DabThread.AUDIOSTATE_DUCK /* 202 */:
              if (this.f124e.getPlayState() == 2) {
                C0162a.m9a("paused -> playing");
                this.f124e.play();
                notifyIntent(this.mSampleRateInHz, true);
              }
              if (this.f124e.getPlayState() == 3) {
                if (this.mIsClippedSampleDetectionEnabled && cad != null) {
                  if (!cad.areSamplesClipped(bArr2, i)) {
                    this.f124e.write(bArr2, 0, i);
                  } else {
                    notifyClippedSamplesDetected();
                  }
                } else {
                  this.f124e.write(bArr2, 0, i);
                }
                if (!this.f125f) {
                  this.f125f = true;
                  Intent intent = new Intent("com.microntek.app");
                  intent.putExtra("app", DabService.SENDER_DAB);
                  intent.putExtra("audio", "play");
                  this.f126g.sendBroadcast(intent);
                  notifyIntent(this.mSampleRateInHz, true);
                  break;
                }
              }
              break;
            case DabThread.AUDIOSTATE_PAUSE /* 201 */:
              if (this.f124e.getPlayState() == 3) {
                C0162a.m9a("playing -> paused");
                this.f124e.pause();
                notifyIntent(0, false);
                break;
              }
              break;
          }
          i = 0;
        }
      }
    }
    if (this.f124e != null) {
      this.f124e.stop();
      this.f124e.release();
    }
    notifyIntent(0, true);
    this.f122c.decoder_close(1);
    C0162a.m9a("mp2 player exit");
  }

  private void notifyClippedSamplesDetected() {
    WeakReference<Handler> playerHandlerWeak;
    Handler playerHandler;
    C0162a.m9a("Clipping detected");
    if (this.mIsClippedSampleNotificationEnabled
        && (playerHandlerWeak = PlayerActivity.getPlayerHandler()) != null
        && (playerHandler = playerHandlerWeak.get()) != null) {
      playerHandler.removeMessages(PlayerActivity.PLAYERMSG_AUDIO_DISTORTION);
      Message m = playerHandler.obtainMessage(PlayerActivity.PLAYERMSG_AUDIO_DISTORTION);
      playerHandler.sendMessage(m);
    }
  }

  public void setAudioState(int audioState) {

    if (this.f124e != null) {
      if (this.mAudioState == 200 && audioState == 202) {
        float volume =
            1.0f; // = pref_settings.getFloat(SettingsActivity.pref_key_audioLevel, 1.0f);
        float volumeDucked =
            0.5f; // pref_settings.getFloat(SettingsActivity.pref_key_audioLevelWhenDucked, 0.5f);
        AudioTools.setVolume(this.f124e, volume * volumeDucked);
        C0162a.m9a("playing -> duck");
      } else if (this.mAudioState == 202 && audioState != 202) {
        float volume2 = 1.0f; // pref_settings.getFloat(SettingsActivity.pref_key_audioLevel, 1.0f);
        AudioTools.setVolume(this.f124e, volume2);
        if (audioState == 200) {
          C0162a.m9a("duck -> playing");
        } else {
          C0162a.m9a("duck -> pause");
        }
      }
    }
    this.mAudioState = audioState;
  }
}

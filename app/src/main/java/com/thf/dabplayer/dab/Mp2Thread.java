package com.thf.dabplayer.dab;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import com.thf.dabplayer.activity.PlayerActivity;

import com.thf.dabplayer.service.DabService;
import com.thf.dabplayer.utils.AudioTools;
import com.thf.dabplayer.utils.Logger;
import com.thf.dabplayer.utils.ClippingAreaDetection;
import com.thf.dabplayer.utils.SharedPreferencesHelper;
import java.lang.ref.WeakReference;

/* renamed from: com.ex.dabplayer.pad.dab.o */
/* loaded from: classes.dex */
public class Mp2Thread extends Thread {

  /* renamed from: b */
  private final RingBuffer f121b;

  /* renamed from: d */
  private int bufferSize;

  /* renamed from: e */
  private AudioTrack audioTrack;

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
        // if (Mp2Thread.this.audioTrack != null) {
        //   AudioTools.setVolume(Mp2Thread.this.audioTrack, volume);
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
      int channelConfig =
          channels == 1
              ? AudioFormat.CHANNEL_CONFIGURATION_MONO
              : AudioFormat.CHANNEL_CONFIGURATION_STEREO;
      this.bufferSize = AudioTrack.getMinBufferSize(sampleRateInHz, channelConfig, 2);
      Logger.d("pcm min buffer size: " + this.bufferSize);
      this.audioTrack =
          new AudioTrack(
              AudioManager.STREAM_MUSIC,
              sampleRateInHz,
              channelConfig,
              AudioFormat.ENCODING_PCM_16BIT,
              this.bufferSize,
              AudioTrack.MODE_STREAM);

      float volume = 1.0f; // pref_settings.getFloat(SettingsActivity.pref_key_audioLevel, 1.0f);
      AudioTools.setVolume(this.audioTrack, volume);
      this.audioTrack.play();
    }
  }

  /* renamed from: a */
  public void exit() {
    this.f120a = true;
    Logger.d("mp2 player about to exit");
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
      Logger.d("mp2 player init fail");
      return;
    }
    Logger.d("mp2 player run");
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
              Logger.d("feed data fail, " + decoder_feed_data + " bytes");
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
          Logger.d("samplerate:" + decoder_get_samplerate + ", channels:" + decoder_get_channels);
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
        if (i >= this.bufferSize && this.audioTrack != null) {
          switch (this.mAudioState) {
            case DabThread.AUDIOSTATE_PLAY /* 200 */:
            case DabThread.AUDIOSTATE_DUCK /* 202 */:
              if (this.audioTrack.getPlayState() == 2) {
                Logger.d("paused -> playing");
                this.audioTrack.play();
                notifyIntent(this.mSampleRateInHz, true);
              }
              if (this.audioTrack.getPlayState() == 3) {
                if (this.mIsClippedSampleDetectionEnabled && cad != null) {
                  if (!cad.areSamplesClipped(bArr2, i)) {
                    this.audioTrack.write(bArr2, 0, i);
                  } else {
                    notifyClippedSamplesDetected();
                  }
                } else {
                  this.audioTrack.write(bArr2, 0, i);
                }
                if (!this.f125f) {
                  this.f125f = true;
                  /*
                  Intent intent = new Intent("com.microntek.app");
                  intent.putExtra("app", DabService.SENDER_DAB);
                  intent.putExtra("audio", "play");
                  this.f126g.sendBroadcast(intent);
                  */
                  notifyIntent(this.mSampleRateInHz, true);
                  break;
                }
              }
              break;
            case DabThread.AUDIOSTATE_PAUSE /* 201 */:
              if (this.audioTrack.getPlayState() == 3) {
                Logger.d("playing -> paused");
                this.audioTrack.pause();
                notifyIntent(0, false);
                break;
              }
              break;
          }
          i = 0;
        }
      }
    }
    if (this.audioTrack != null) {
      this.audioTrack.stop();
      this.audioTrack.release();
    }
    notifyIntent(0, true);
    this.f122c.decoder_close(1);
    Logger.d("mp2 player exit");
  }

  private void notifyClippedSamplesDetected() {
    WeakReference<Handler> playerHandlerWeak;
    Handler playerHandler;
    Logger.d("Clipping detected");
    if (this.mIsClippedSampleNotificationEnabled
        && (playerHandlerWeak = PlayerActivity.getPlayerHandler()) != null
        && (playerHandler = playerHandlerWeak.get()) != null) {
      playerHandler.removeMessages(PlayerActivity.PLAYERMSG_AUDIO_DISTORTION);
      Message m = playerHandler.obtainMessage(PlayerActivity.PLAYERMSG_AUDIO_DISTORTION);
      playerHandler.sendMessage(m);
    }
  }

  public void setAudioState(int audioState) {

    if (this.audioTrack != null) {
      if (this.mAudioState == 200 && audioState == 202) {
        float volume =
            1.0f; // = pref_settings.getFloat(SettingsActivity.pref_key_audioLevel, 1.0f);
        float volumeDucked =
            0.5f; // pref_settings.getFloat(SettingsActivity.pref_key_audioLevelWhenDucked, 0.5f);
        AudioTools.setVolume(this.audioTrack, volume * volumeDucked);
        Logger.d("playing -> duck");
      } else if (this.mAudioState == 202 && audioState != 202) {
        float volume2 = 1.0f; // pref_settings.getFloat(SettingsActivity.pref_key_audioLevel, 1.0f);
        AudioTools.setVolume(this.audioTrack, volume2);
        if (audioState == 200) {
          Logger.d("duck -> playing");
        } else {
          Logger.d("duck -> pause");
        }
      }
    }
    this.mAudioState = audioState;
  }
}

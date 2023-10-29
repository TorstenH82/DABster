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
import com.thf.dabplayer.utils.Logger;
import com.thf.dabplayer.utils.ClippingAreaDetection;
import com.thf.dabplayer.utils.SharedPreferencesHelper;
import java.lang.ref.WeakReference;

/* renamed from: com.ex.dabplayer.pad.dab.p */
/* loaded from: classes.dex */
public class PcmThread extends Thread {

  /* renamed from: b */
  private final RingBuffer ringBuffer;

  /* renamed from: c */
  private AudioTrack audioTrack;

  /* renamed from: d */
  private int minBufferSize;

  /* renamed from: f */
  private Context context;
  private int mChannels;
  private boolean isClippedSampleDetectionEnabled;
  private boolean isClippedSampleNotificationEnabled;
  private SharedPrefListener mPrefListener;
  private int mSampleRateInHz;

  /* renamed from: a */
  private boolean f127a = false;

  /* renamed from: e */
  private boolean f131e = false;
  private int mAudioState = DabThread.AUDIOSTATE_PLAY;

  /* renamed from: com.ex.dabplayer.pad.dab.p$SharedPrefListener */
  /* loaded from: classes.dex */
  private class SharedPrefListener implements SharedPreferences.OnSharedPreferenceChangeListener {
    SharedPrefListener() {}

    @Override // android.content.SharedPreferences.OnSharedPreferenceChangeListener
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
      if (key.equals("suppressNoise")) {
        PcmThread.this.isClippedSampleDetectionEnabled =
            SharedPreferencesHelper.getInstance().getBoolean("suppressNoise");

        PcmThread.this.isClippedSampleNotificationEnabled =
            SharedPreferencesHelper.getInstance().getBoolean("suppressNoise");

      } else if (key.equals("volume")) {
        // float volume = sharedPreferences.getFloat(SettingsActivity.pref_key_audioLevel, 1.0f);
        // if (PcmThread.this.audioTrack != null) {
        //  AudioTools.setVolume(PcmThread.this.audioTrack, volume);
        // }
      }
    }
  }

  public PcmThread(Context context, RingBuffer ringBuffer, int i, int i2) {
    this.isClippedSampleDetectionEnabled = false;
    this.isClippedSampleNotificationEnabled = false;
    this.ringBuffer = ringBuffer;
    this.context = context;
    this.mSampleRateInHz = i;
    this.mChannels = i2;

    this.isClippedSampleDetectionEnabled =
        SharedPreferencesHelper.getInstance().getBoolean("suppressNoise");
    this.isClippedSampleNotificationEnabled =
        SharedPreferencesHelper.getInstance().getBoolean("suppressNoise");
    this.mPrefListener = new SharedPrefListener();

    SharedPreferencesHelper.getInstance()
        .getSharedPreferences()
        .registerOnSharedPreferenceChangeListener(this.mPrefListener);
  }

  /* renamed from: a */
  private void m24a(int sampleRateInHz, int channels) {
    if (sampleRateInHz > 0) {
      int channelConfig = channels == 1 ? 2 : 3;
      this.minBufferSize = AudioTrack.getMinBufferSize(sampleRateInHz, channelConfig, 2);
      Logger.d("pcm min buffer size: " + this.minBufferSize);
      this.audioTrack = new AudioTrack(3, sampleRateInHz, channelConfig, 2, this.minBufferSize, 1);

      // float volume = pref_settings.getFloat(SettingsActivity.pref_key_audioLevel, 1.0f);
      float volume = 1.0f;
      AudioTools.setVolume(this.audioTrack, volume);
      this.audioTrack.play();
    }
  }

  /* renamed from: a */
  public void m25a() {
    this.f127a = true;
    Logger.d("pcm thread about to exit");
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

   int numRemainUnclippedSamples = 0; 
    
    
  @Override // java.lang.Thread, java.lang.Runnable
  public void run() {
    byte[] bArr = new byte[184320];
    Logger.d("pcm thread run");
    try {
      Process.setThreadPriority(-16);
      setName("pcm");
    } catch (IllegalArgumentException | SecurityException e) {
      e.printStackTrace();
    }
    ClippingAreaDetection cad = new ClippingAreaDetection(this.mChannels);
    while (!this.f127a) {
      try {
        Thread.sleep(1L);
      } catch (InterruptedException e2) {
        e2.printStackTrace();
      }
      synchronized (this.ringBuffer) {
        if (this.ringBuffer.getRemainingCapacity() >= this.minBufferSize) {
          int a = this.ringBuffer.readBuffer(bArr, this.minBufferSize);
          if (!this.f131e) {
            this.f131e = true;
            m24a(this.mSampleRateInHz, this.mChannels);
            /*            
            Intent intent = new Intent("com.microntek.app");
            intent.putExtra("app", DabService.SENDER_DAB);
            intent.putExtra("audio", "play");
            this.context.sendBroadcast(intent);
            */            
            notifyIntent(this.audioTrack.getSampleRate(), true);
          }
          switch (this.mAudioState) {
            case DabThread.AUDIOSTATE_PLAY /* 200 */:
            case DabThread.AUDIOSTATE_DUCK /* 202 */:
              if (this.audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PAUSED) {
                Logger.d("paused -> playing");
                this.audioTrack.play();
                notifyIntent(this.mSampleRateInHz, true);
              }
              if (this.audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                if (this.isClippedSampleDetectionEnabled) {
                  if (!cad.areSamplesClipped(bArr, a)) {
                    this.audioTrack.write(bArr, 0, a);
                    break;
                  } else {
                    notifyClippedSamplesDetected();
                    break;
                  }
                } else {
                  this.audioTrack.write(bArr, 0, a);
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
        }
      }
    }
    if (this.audioTrack != null) {
      this.audioTrack.stop();
      this.audioTrack.release();
    }
    notifyIntent(0, true);
    Logger.d("pcm thread exit");
  }

  private void notifyClippedSamplesDetected() {
    WeakReference<Handler> playerHandlerWeak;
    Handler playerHandler;
    Logger.d("Clipping detected");
    if (this.isClippedSampleNotificationEnabled
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
        // float volume = pref_settings.getFloat(SettingsActivity.pref_key_audioLevel, 1.0f);
        float volume = 1.0f;
        float volumeDucked = 0.5f;
        // float volumeDucked =
        //    pref_settings.getFloat(SettingsActivity.pref_key_audioLevelWhenDucked, 0.5f);
        AudioTools.setVolume(this.audioTrack, volume * volumeDucked);
        Logger.d("playing -> duck");
      } else if (this.mAudioState == 202 && audioState != 202) {
        // float volume2 = pref_settings.getFloat(SettingsActivity.pref_key_audioLevel, 1.0f);
        float volume2 = 1.0f;
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
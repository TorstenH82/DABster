package com.thf.dabplayer.dab;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
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
  private int unclippedSamplesRequired = 1;
  private int remainingUnclippedSamples = 1;
  private SharedPrefListener mPrefListener;
  private int mSampleRateInHz;

  /* renamed from: a */
  private boolean exit = false;

  /* renamed from: e */
  private boolean f131e = false;
  private int mAudioState = DabThread.AUDIOSTATE_PLAY;

  /* renamed from: com.ex.dabplayer.pad.dab.p$SharedPrefListener */
  /* loaded from: classes.dex */
  private class SharedPrefListener implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override // android.content.SharedPreferences.OnSharedPreferenceChangeListener
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
      switch (key) {
        case "suppressNoise":
          PcmThread.this.isClippedSampleDetectionEnabled =
              SharedPreferencesHelper.getInstance().getBoolean(key);

          PcmThread.this.isClippedSampleNotificationEnabled =
              SharedPreferencesHelper.getInstance().getBoolean(key);
          break;
        case "volume":
          // float volume =
          // sharedPreferences.getFloat(SettingsActivity.pref_key_audioLevel, 1.0f);
          // if (PcmThread.this.audioTrack != null) {
          //  AudioTools.setVolume(PcmThread.this.audioTrack, volume);
          break;
        case "unclippedSamples":
          PcmThread.this.unclippedSamplesRequired =
              SharedPreferencesHelper.getInstance().getInteger(key);
          break;
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

    this.unclippedSamplesRequired =
        SharedPreferencesHelper.getInstance().getInteger("unclippedSamples");

    this.mPrefListener = new SharedPrefListener();

    SharedPreferencesHelper.getInstance()
        .getSharedPreferences()
        .registerOnSharedPreferenceChangeListener(this.mPrefListener);
  }

  /* renamed from: a */
  private void prepareAudioTrack(int sampleRateInHz, int channels) {
    if (sampleRateInHz > 0) {

      Logger.d("pcm min buffer size: " + this.minBufferSize);

      this.minBufferSize =
          AudioTrack.getMinBufferSize(
              sampleRateInHz,
              channels == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO,
              AudioFormat.ENCODING_PCM_16BIT);

      this.audioTrack =
          new AudioTrack.Builder()
              .setAudioAttributes(
                  new AudioAttributes.Builder()
                      .setUsage(AudioAttributes.USAGE_MEDIA)
                      .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                      .build())
              .setAudioFormat(
                  new AudioFormat.Builder()
                      .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                      .setSampleRate(sampleRateInHz)
                      .setChannelMask(
                          channels == 1
                              ? AudioFormat.CHANNEL_OUT_MONO
                              : AudioFormat.CHANNEL_OUT_STEREO)
                      .build())
              .setBufferSizeInBytes(minBufferSize)
              .build();

      // float volume = pref_settings.getFloat(SettingsActivity.pref_key_audioLevel, 1.0f);
      float volume = 1.0f;
      AudioTools.setVolume(this.audioTrack, volume);
      this.audioTrack.play();
    }
  }

  /* renamed from: a */
  public void exit() {
    this.exit = true;
    Logger.d("pcm thread about to exit");
  }

  private void notifyIntent(int samplerate, boolean isPlaying) {
    Handler handler;
    WeakReference<Handler> playerHandler = PlayerActivity.getPlayerHandler();
    if (playerHandler != null && (handler = playerHandler.get()) != null) {
      StationInfo stationInfo = StationInfo.getInstance();
      stationInfo.setSamplerate(samplerate);
      stationInfo.setPlaying(true);

      Message intentMessage = handler.obtainMessage();
      intentMessage.what = PlayerActivity.PLAYERMSG_STATIONINFO;
      intentMessage.obj = stationInfo;
      handler.sendMessage(intentMessage);
    }
  }

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
    while (!this.exit) {
      try {
        Thread.sleep(1L);
      } catch (InterruptedException e2) {
        e2.printStackTrace();
      }
      synchronized (this.ringBuffer) {
        if (this.ringBuffer.getNumSamplesAvailable() >= this.minBufferSize) {
          // if (this.ringBuffer.getRemainingCapacity() >= this.minBufferSize) {
          int a = this.ringBuffer.readBuffer(bArr, this.minBufferSize);
          if (!this.f131e) {
            this.f131e = true;
            prepareAudioTrack(this.mSampleRateInHz, this.mChannels);
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
                    remainingUnclippedSamples -= 1;
                    if (remainingUnclippedSamples <= 0) {
                      this.audioTrack.write(bArr, 0, a);
                      remainingUnclippedSamples = 1;
                    }
                    break;
                  } else {
                    notifyClippedSamplesDetected();
                    remainingUnclippedSamples = unclippedSamplesRequired;
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
        // float volume = pref_settings.getFloat(SettingsActivity.pref_key_audioLevel,
        // 1.0f);
        float volume = 1.0f;
        float volumeDucked = 0.5f;
        // float volumeDucked =
        //    pref_settings.getFloat(SettingsActivity.pref_key_audioLevelWhenDucked, 0.5f);
        AudioTools.setVolume(this.audioTrack, volume * volumeDucked);
        Logger.d("playing -> duck");
      } else if (this.mAudioState == 202 && audioState != 202) {
        // float volume2 = pref_settings.getFloat(SettingsActivity.pref_key_audioLevel,
        // 1.0f);
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

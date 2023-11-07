package com.thf.dabplayer.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import com.thf.dabplayer.utils.SharedPreferencesHelper;
import com.thf.dabplayer.R;

public class DonateActivity extends Activity {

  private String source;
  private Context context;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.context = getApplicationContext();
    Intent intent = getIntent();
    this.source = intent.getStringExtra("source");
  }

  @Override
  protected void onResume() {
    super.onResume();
    //    prepareAudioTrack(44000, 2);
    showDonateDialog();
  }

  private void showDonateDialog() {

    SimpleDialog.SimpleDialogListener simpleDialogListener =
        new SimpleDialog.SimpleDialogListener() {
          @Override
          public void onClick(boolean positive, int selection) {
            if (!positive) {
              SharedPreferencesHelper.getInstance().setBoolean("showDonate", false);
            }
            finish();
          }
        };
    SimpleDialog donateDialog =
        new SimpleDialog(this, this.context.getString(R.string.SupportMe), simpleDialogListener);
    if ("PlayerActivity".equals(source)) {
      donateDialog.setPositiveButton(this.context.getString(R.string.maybe_later));
      donateDialog.setNegativeButton(this.context.getString(R.string.already_donated));
    } else {
      donateDialog.setPositiveButton(this.context.getString(R.string.ok));
    }

    donateDialog.setMessage(this.context.getString(R.string.DonateText));
    donateDialog.setImage(this.context.getDrawable(R.drawable.qrcode));
    donateDialog.setUrl(this.context.getString(R.string.DonateUrl));
    donateDialog.show();
  }
  /*
  int minBufferSize;
  AudioTrack audioTrack;


  private void prepareAudioTrack(int sampleRateInHz, int channels) {
    if (sampleRateInHz > 0) {

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
    */
}

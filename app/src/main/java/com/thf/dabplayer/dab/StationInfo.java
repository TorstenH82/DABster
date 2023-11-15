package com.thf.dabplayer.dab;

import android.content.Context;
import android.content.Intent;
import android.content.pm.SigningInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import com.thf.dabplayer.service.DabService;
import com.thf.dabplayer.R;
import com.thf.dabplayer.utils.Logger;
import java.io.File;

public class StationInfo {
  public static final String AUDIOFORMAT_AAC = "AAC";
  public static final String AUDIOFORMAT_MP2 = "MP2";
  public static final String EXTRA_AFFECTS_ANDROID_METADATA = "affectsAndroidMetaData";
  public static final String EXTRA_ARTIST = "artist";
  public static final String EXTRA_ALBUM = "album";
  public static final String EXTRA_TRACK = "track";
  public static final String EXTRA_AUDIOFORMAT = "audio_format";
  public static final String EXTRA_AUDIOSAMPLERATE = "samplerate";
  public static final String EXTRA_BITRATE = "bitrate";
  public static final String EXTRA_DLS = "dls";
  public static final String EXTRA_ENSEMBLE_ID = "ensemble_id";
  public static final String EXTRA_ENSEMBLE_NAME = "ensemble_name";
  public static final String EXTRA_FREQUENCY_KHZ = "frequency_khz";
  public static final String EXTRA_ID = "id";
  public static final String EXTRA_NUMSTATIONS = "num_stations";
  public static final String EXTRA_PLAYING = "playing";
  public static final String EXTRA_PTY = "pty";
  public static final String EXTRA_SENDER = "sender";
  public static final String EXTRA_SERVICEFOLLOWING = "service_following";
  public static final String EXTRA_SERVICEID = "serviceid";
  public static final String EXTRA_SERVICELOG = "service_log";
  public static final String EXTRA_SIGNALQUALITY = "signal";
  public static final String EXTRA_SLS = "sls";
  public static final String EXTRA_SLSBITMAP = "slsBitmap";
  public static final String EXTRA_STATION = "station";
  public static final String META_CHANGED = "com.android.music.metachanged";
  private static final String MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id";
  private static final String MY_MEDIA_ROOT_ID = "media_root_id";
  private static final int ONGOING_NOTIFICATION = 21;
  public static final String SENDER_DAB = "com.thf.dabplayer";
  public static final int SIGNALQUALITY_NONE = 8000;

  private static StationInfo stationInfo;

  private int stationIdx = -1;
  private String serviceFollowing;
  private String serviceLog;
  private int numStations;
  private String sender;
  private String artist;
  private String track;
  private String station;
  private int serviceId;
  private int frequencyKhz;
  private String pty;
  private int bitrate;
  private String ensembleName;
  private int ensembleId;
  private boolean affectMetadata;
  private BitmapDrawable motImage;
  private String dls;
  private String audiocodec;
  private int samplerate;
  private String sls;
  private boolean playing;

  private void initVars() {
    this.serviceFollowing = "";
    this.serviceLog = "";
    this.numStations = 0;
    this.sender = SENDER_DAB;
    this.artist = "";
    this.track = "";
    this.station = "";
    this.serviceId = -1;
    this.frequencyKhz = 0;
    this.pty = "";
    this.bitrate = 0;
    this.ensembleName = "";
    this.ensembleId = 0;
    this.affectMetadata = false;
    this.motImage = null;
    this.dls = "";
    this.audiocodec = "";
    this.samplerate = 0;
    this.sls = "";
    this.playing = false;
  }

  public void setPlaying(boolean playing) {
    this.playing = playing;
  }

  public boolean getPlaying() {
    return this.playing;
  }

  // EXTRA_SLS
  public void setSls(String sls) {
    this.sls = sls;
  }

  public String getSls() {
    return this.sls == null ? "" : this.sls;
  }

  // EXTRA_AUDIOSAMPLERATE
  public void setSamplerate(int samplerate) {
    this.samplerate = samplerate;
  }

  public int getSamplerate() {
    return this.samplerate;
  }

  // EXTRA_AUDIOFORMAT
  public void setAudiocodec(String audiocodec) {
    this.audiocodec = audiocodec;
  }

  public String getAudiocodec() {
    return this.audiocodec == null ? "" : this.audiocodec;
  }

  // EXTRA_SERVICEFOLLOWING
  public void setServiceFollowing(String serviceFollowing) {
    this.serviceFollowing = serviceFollowing;
  }

  public String getServiceFollowing() {
    return this.serviceFollowing == null ? "" : this.serviceFollowing;
  }

  // EXTRA_SERVICELOG
  public void setServiceLog(String serviceLog) {
    this.serviceLog = serviceLog;
  }

  // EXTRA_NUMSTATIONS
  public void setNumStations(int numStations) {
    this.numStations = numStations;
  }

  public int getNumStations() {
    return this.numStations;
  }

  // EXTRA_ARTIST
  public void setArtist(String artist) {
    this.artist = artist;
  }

  public String getArtist() {
    return this.artist == null ? "" : this.artist;
  }

  // EXTRA_TRACK
  public void setTrack(String track) {
    this.track = track;
  }

  public String getTrack() {
    return this.track == null ? "" : this.track;
  }

  // EXTRA_DLS
  public void setDls(String dls) {
    this.dls = dls;
  }

  public String getDls() {
    return this.dls == null ? "" : this.dls;
  }

  // EXTRA_STATION
  public void setStation(String station) {
    this.station = station;
  }

  public String getStation() {
    return this.station == null ? "" : this.station;
  }

  // EXTRA_SERVICEID
  public void setServiceId(int serviceId) {
    this.serviceId = serviceId;
  }

  public int getServiceId() {
    return this.serviceId;
  }

  // EXTRA_FREQUENCY_KHZ
  public void setFrequencyKhz(int frequencyKhz) {
    this.frequencyKhz = frequencyKhz;
  }

  public int getFrequencyKhz() {
    return this.frequencyKhz;
  }

  // EXTRA_PTY
  public void setPty(String pty) {
    this.pty = pty;
  }

  public String getPty() {
    return this.pty == null ? "" : this.pty;
  }

  // EXTRA_BITRATE
  public void setBitrate(int bitrate) {
    this.bitrate = bitrate;
  }

  public int getBitrate() {
    return this.serviceId;
  }

  // EXTRA_ENSEMBLE_NAME
  public void setEnsembleName(String ensembleName) {
    this.ensembleName = ensembleName;
  }

  public String getEnsembleName() {
    return this.ensembleName == null ? "" : this.ensembleName;
  }

  // EXTRA_ENSEMBLE_ID
  public void setEnsembleId(int ensembleId) {
    this.ensembleId = ensembleId;
  }

  public int getEnsembleId() {
    return this.ensembleId;
  }

  // EXTRA_AFFECTS_ANDROID_METADATA
  public void setAffectMetadata(boolean affectMetadata) {
    this.affectMetadata = affectMetadata;
  }

  private Intent intent;

  public static StationInfo getInstance() {
    if (StationInfo.stationInfo == null) {
      StationInfo.stationInfo = new StationInfo();
    }
    return StationInfo.stationInfo;
  }

  private StationInfo() {
    initVars();
  }

  // EXTRA_ID
  public void setStationIdxAndReset(int idx) {
    if (this.stationIdx != idx) {
      this.stationIdx = idx;
      initVars();
    }
  }

  public int getStationNum() {
    if (this.stationIdx != -1) {
      return this.stationIdx + 1;
    } else {
      return -1;
    }
  }

  public BitmapDrawable getLogo(Context context) {
    File file = new File(getSls());
    if (file.exists()) {
      Bitmap decodeFile = BitmapFactory.decodeFile(file.getAbsolutePath());
      if (decodeFile != null) {
        this.motImage = new BitmapDrawable(context.getResources(), decodeFile);
      }
    } else {
      Logger.d("no mot file for sls '" + getSls() + "'");
    }

    BitmapDrawable logoDrawable = this.motImage;
    if (logoDrawable == null) {

      LogoDb logoDb = LogoDbHelper.getInstance(context);
      logoDrawable = logoDb.getLogo(getStation(), getServiceId());
    }
    if (logoDrawable == null) {
      logoDrawable = (BitmapDrawable) context.getDrawable(R.drawable.radio);
    }
    return logoDrawable;
  }

  public void sentMetadataBroadcast(Context context) {
    Intent intent = new Intent(META_CHANGED);
    intent.putExtra(EXTRA_SENDER, SENDER_DAB);
    intent.putExtra(EXTRA_ID, getStationNum());
    intent.putExtra(EXTRA_NUMSTATIONS, getNumStations());
    intent.putExtra(EXTRA_ARTIST, getArtist());
    intent.putExtra(EXTRA_TRACK, "".equals(getTrack()) ? getDls() : getTrack());
    intent.putExtra(EXTRA_ALBUM, getStation());
    intent.putExtra(EXTRA_STATION, getStation());
    intent.putExtra(EXTRA_DLS, getDls());
    intent.putExtra("playing", getPlaying());
    intent.putExtra(EXTRA_SERVICEID, getServiceId());
    intent.putExtra(EXTRA_FREQUENCY_KHZ, getFrequencyKhz());
    intent.putExtra(EXTRA_PTY, getPty());
    intent.putExtra(EXTRA_BITRATE, getBitrate());
    intent.putExtra(EXTRA_ENSEMBLE_NAME, getEnsembleName());
    intent.putExtra(EXTRA_ENSEMBLE_ID, getEnsembleId());
    intent.putExtra(EXTRA_SLS, getSls());
    intent.putExtra(EXTRA_AUDIOFORMAT, getAudiocodec());
    intent.putExtra(EXTRA_AUDIOSAMPLERATE, getSamplerate());
    intent.putExtra(EXTRA_SLSBITMAP, getLogo(context).getBitmap());
    context.sendBroadcast(intent);
  }
}

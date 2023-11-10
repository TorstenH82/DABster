package com.thf.dabplayer.dab;

import com.thf.dabplayer.utils.Logger;

/* renamed from: com.ex.dabplayer.pad.dab.StationLogo */
/* loaded from: classes.dex */
public class StationLogo {
  public static final int SERVICE_ID_UNDEF = 0;
  public static final int SERVICE_ID_ASSET = -99;
  public String mLogoPathFilename;
  public String mStationNameNormalized;
  public int mStationServiceId = 0;

  public static StationLogo create(String path, String name) {
    return create(path, name, null);
  }

  public static StationLogo createFromAsset(String path, String name) {
    return create(path, name, SERVICE_ID_ASSET);
  }

  public static StationLogo create(String path, String name, Integer sid) {
    if (name.endsWith(".jpg") || name.endsWith(".png")) {
      StationLogo stationLogo = new StationLogo();
      stationLogo.mLogoPathFilename = path;
      String[] station = name.split("\\.(?=[^\\.]+$)");
      stationLogo.mStationNameNormalized = getNormalizedStationName(station[0]);
      if (sid != null) {
        stationLogo.mStationServiceId = sid;
      }

      //Logger.d(stationLogo.mLogoPathFilename);
      //Logger.d(stationLogo.mStationNameNormalized);
      //Logger.d(stationLogo.mStationServiceId + "");

      return stationLogo;
    }
    Logger.d("ignore stationlogo " + name);
    return null;
  }

  public static String getNormalizedStationName(String stationName) {
    String norm = new String();
    if (stationName != null) {
      String stationName2 = stationName.toLowerCase();
      for (int i = 0; i < stationName2.length(); i++) {
        int c = stationName2.codePointAt(i);
        if ((c >= 97 && c <= 122) || (c >= 48 && c <= 57)) {
          norm = norm + String.format("%c", Integer.valueOf(c));
        } else if (c == 43) {
          norm = norm + "plus";
        }
      }
    }
    return norm;
  }
}
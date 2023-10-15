package com.thf.dabplayer.activity;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import com.thf.dabplayer.dab.StationLogo;
import com.thf.dabplayer.utils.C0162a;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

/* renamed from: com.ex.dabplayer.pad.activity.LogoAssets */
/* loaded from: classes.dex */
public class LogoAssets {
  static final String AssetLogosPath = "logos";
  static HashMap<String, String> mLogos = null;
  private Context mContext;
  private Handler mHandler;

  /* JADX INFO: Access modifiers changed from: package-private */
  public LogoAssets(Context ct, Handler hm) {
    this.mContext = ct;
    this.mHandler = hm;
    if (mLogos == null) {
      mLogos = new HashMap<>();
      new SyncLogoAssets(this).execute(new Void[0]);
    }
  }

  public static synchronized BitmapDrawable getBitmapForStation(Context ct, @NonNull String label) {
    BitmapDrawable logoDrawable;
    Bitmap bitmap;
    synchronized (LogoAssets.class) {
      logoDrawable = null;
      String assetpath = mLogos.get(StationLogo.getNormalizedStationName(label));
      AssetManager mAssetMgr = ct.getAssets();
      if (assetpath != null) {
        try {
          InputStream stream = mAssetMgr.open("logos/" + assetpath);
          if (stream != null && (bitmap = BitmapFactory.decodeStream(stream)) != null) {
            logoDrawable = new BitmapDrawable(ct.getResources(), bitmap);
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return logoDrawable;
  }

  public static synchronized boolean isLogoStation(@NonNull String label) {
    boolean z;
    synchronized (LogoAssets.class) {
      z = mLogos.get(StationLogo.getNormalizedStationName(label)) != null;
    }
    return z;
  }

  /* renamed from: com.ex.dabplayer.pad.activity.LogoAssets$SyncLogoAssets */
  /* loaded from: classes.dex */
  private class SyncLogoAssets extends AsyncTask<Void, Void, Void> {
    final /* synthetic */ LogoAssets logoAssets;
    private int total;

    private SyncLogoAssets(LogoAssets logoAssets) {
      this.logoAssets = logoAssets;
      this.total = 0;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.os.AsyncTask
    public Void doInBackground(Void... params) {
      long start = System.currentTimeMillis();
      C0162a.m9a("assetlogos: sync");
      this.total = LogoAssets.this.sync_path(null);
      C0162a.m8a("assetlogos: time=", (int) (System.currentTimeMillis() - start));
      return null;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.os.AsyncTask
    public void onPostExecute(Void param) {
      C0162a.m8a("assetlogos: count=", this.total);
      if (this.total > 0) {
        Message obtainMessage = LogoAssets.this.mHandler.obtainMessage();
        obtainMessage.what = 98;
        LogoAssets.this.mHandler.sendMessage(obtainMessage);
      }
    }
  }

  /* JADX INFO: Access modifiers changed from: private */
  public int sync_path(String logos_path) {
    String[] assets;
    int total = 0;
    AssetManager mAssetMgr = this.mContext.getAssets();
    try {
      assets = mAssetMgr.list(AssetLogosPath + (logos_path == null ? "" : "/" + logos_path));
    } catch (IOException e) {
      e.printStackTrace();
      assets = null;
    }
    if (assets != null) {
      for (String a : assets) {
        if (logos_path == null || !logos_path.equals(a)) {
          if (a.contains(".")) {
            StationLogo stationLogo =
                StationLogo.create((logos_path == null ? "" : logos_path + "/") + a, a);
            if (stationLogo != null) {
              add(stationLogo);
              total++;
            }
          } else if (logos_path == null) {
            total += sync_path(a);
          } else {
            C0162a.m5a("assetlogos: ignore ", a);
          }
        }
      }
    } else {
      C0162a.m9a("assetlogos: none");
    }
    return total;
  }

  private synchronized void add(StationLogo stationLogo) {
    if (mLogos.get(stationLogo.mStationNameNormalized) == null) {
      mLogos.put(stationLogo.mStationNameNormalized, stationLogo.mLogoPathFilename);
    } else {
      C0162a.m5a("assetlogos: duplicate ", stationLogo.mLogoPathFilename);
    }
  }
}

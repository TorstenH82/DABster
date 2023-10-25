package com.thf.dabplayer.dab;

import android.content.Context;
import android.content.res.AssetManager;
import android.widget.Toast;
import com.thf.dabplayer.utils.C0162a;
import java.io.IOException;

public class LogoDbAssets implements Runnable {
  Context context;
  static final String AssetLogosPath = "logos";

  public LogoDbAssets(Context context) {
    this.context = context;
  }

  public int sync_path(String logos_path) {
    String[] assets;
    int total = 0;
    LogoDb logoDb = LogoDbHelper.getInstance(this.context);

    AssetManager mAssetMgr = this.context.getAssets();
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
            String logoPath = (logos_path == null ? "" : logos_path + "/") + a;

            StationLogo stationLogo =
                StationLogo.createFromAsset((logos_path == null ? "" : logos_path + "/") + a, a);
            if (stationLogo != null) {
              logoDb.updateOrInsertStationLogo(stationLogo);
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

  @Override
  public void run() {
    int counter = sync_path(null);
    C0162a.m9a(counter + " assetlogos added to database");
  }

  public void execute() {
    new Thread(this).start();
  }
}

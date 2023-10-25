package com.thf.dabplayer.dab;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

import com.thf.dabplayer.service.DabService;
import com.thf.dabplayer.utils.C0162a;
import com.thf.dabplayer.utils.Strings;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/* renamed from: com.ex.dabplayer.pad.dab.LogoDb */
/* loaded from: classes.dex */
public class LogoDb {
  private Context mContext;
  private final String dbName = "logos";
  private final String colName_id = "_id";
  private final String colNameStation = DabService.EXTRA_STATION;
  private final String colNamePath = "path";
  private final String colNameSid = "sid";
  private SQLiteDatabase mDb = null;

  /* renamed from: com.ex.dabplayer.pad.dab.LogoDb$LookupIssueType */
  /* loaded from: classes.dex */
  public enum LookupIssueType {
    NONE,
    AMBIGUOUS,
    NOMATCH
  }

  public LogoDb(Context context) {
    this.mContext = context;
    openOrCreateDb();
  }

  private SQLiteDatabase getDb() {
    if (this.mDb == null) {
      String dbPathFile =
          new StringBuilder(String.valueOf(this.mContext.getFilesDir().getAbsoluteFile()))
                  .toString()
              + "/logos.db";
      this.mDb =
          SQLiteDatabase.openOrCreateDatabase(dbPathFile, (SQLiteDatabase.CursorFactory) null);
    }
    return this.mDb;
  }

  public int getLogosCount() {
    int count;
    synchronized (this) {
      if (!getDb().isOpen()) {
        openOrCreateDb();
      }
      Cursor rawQuery = getDb().rawQuery("SELECT * FROM logos", null);
      count = rawQuery.getCount();
    }
    return count;
  }

  private void openOrCreateDb() {
    synchronized (this) {
      SQLiteDatabase db = getDb();
      if (db.rawQuery("SELECT * FROM sqlite_master WHERE type='table'", null).getCount() == 1) {
        db.execSQL(
            "CREATE TABLE logos (_id INTEGER PRIMARY KEY AUTOINCREMENT, station TEXT, path TEXT, sid INTEGER )");
        C0162a.m9a("logos db created");
      }
      if (db.isOpen()) {
        C0162a.m9a("logos db opened");
      } else {
        C0162a.m9a("logos db NOT open !!");
      }
    }
  }

  public void clearDb() {
    synchronized (this) {
      if (!getDb().isOpen()) {
        openOrCreateDb();
      }
      getDb().execSQL("DELETE FROM logos");
      C0162a.m9a("logos db cleared");
    }
  }

  public void closeDb() {
    synchronized (this) {
      if (getDb().isOpen()) {
        C0162a.m9a("logos db closed");
        getDb().close();
        this.mDb = null;
      }
    }
  }

  public void insertStationLogo(StationLogo stationLogo) {
    synchronized (this) {
      if (!getDb().isOpen()) {
        openOrCreateDb();
      }
      ContentValues contentValues = new ContentValues();
      contentValues.put(DabService.EXTRA_STATION, stationLogo.mStationNameNormalized);
      contentValues.put("path", stationLogo.mLogoPathFilename);
      contentValues.put("sid", Integer.valueOf(stationLogo.mStationServiceId));
      getDb().insert("logos", null, contentValues);
    }
  }

  public boolean updateOrInsertStationLogo(StationLogo stationLogo) {
    boolean result = true;
    synchronized (this) {
      if (!getDb().isOpen()) {
        openOrCreateDb();
      }
      ContentValues contentValues = new ContentValues();
      contentValues.put(DabService.EXTRA_STATION, stationLogo.mStationNameNormalized);
      contentValues.put("path", stationLogo.mLogoPathFilename);
      contentValues.put("sid", Integer.valueOf(stationLogo.mStationServiceId));
      String where =
          "station='"
              + stationLogo.mStationNameNormalized
              + "' AND sid="
              + stationLogo.mStationServiceId;
      int rowsUpdated = getDb().update("logos", contentValues, where, null);
      C0162a.m9a(
          "logo db updated "
              + rowsUpdated
              + " rows for "
              + stationLogo.mStationNameNormalized
              + " sid "
              + stationLogo.mStationServiceId);
      if (rowsUpdated == 0) {
        if (getDb().insert("logos", null, contentValues) == -1) {
          C0162a.m9a("failed to insert logo to db");
          result = false;
        } else {
          C0162a.m9a("new logo inserted");
        }
      }
    }
    return result;
  }

  public ArrayList<StationLogo> getStationLogoList() {
    ArrayList<StationLogo> arrayList;
    synchronized (this) {
      arrayList = new ArrayList<>();
      if (!getDb().isOpen()) {
        openOrCreateDb();
      }
      Cursor rawQuery = getDb().rawQuery("SELECT * FROM logos", null);
      while (rawQuery.moveToNext()) {
        StationLogo stationLogo = new StationLogo();
        stationLogo.mStationNameNormalized =
            rawQuery.getString(rawQuery.getColumnIndex(DabService.EXTRA_STATION));
        String name = rawQuery.getString(rawQuery.getColumnIndex("path"));
        stationLogo.mLogoPathFilename = name;
        stationLogo.mStationServiceId = rawQuery.getInt(rawQuery.getColumnIndex("sid"));
        arrayList.add(stationLogo);
      }
      rawQuery.close();
    }
    return arrayList;
  }

  public String getLogoFilenameForStation(String name, int sid) {
    String path = getLogoFilenameForStationAndIssue(name, sid, null);

    if (sid == -99) {
      path = "logos/" + path;
    }

    return path;
  }

  public BitmapDrawable getLogo(String name, int sid) {

    BitmapDrawable logo = null;

    String path = getLogoFilenameForStationAndIssue(name, sid, null);
    if (path != null) {
      logo = getBitmapForStation(mContext, path);
    }
    if (logo == null) {
      path = getLogoFilenameForStationAndIssue(name, -99, null);
      if (path != null) {
        AssetManager mAssetMgr = mContext.getAssets();
        Bitmap bitmap;
        try {
          InputStream stream = mAssetMgr.open("logos/" + path);
          if (stream != null && (bitmap = BitmapFactory.decodeStream(stream)) != null) {
            logo = new BitmapDrawable(mContext.getResources(), bitmap);
          }
        } catch (IOException e) {
          C0162a.m9a("error getting asset '" + "logos/" + path + "':" + e.toString());
        }
      }
    }

    return logo;
  }

  public static BitmapDrawable getBitmapForStation(Context ct, String logopath) {
    File f = new File(logopath);
    if (f.exists()) {
      BitmapDrawable logoDrawable = new BitmapDrawable(ct.getResources(), logopath);
      return logoDrawable;
    }
    C0162a.m9a("not exist: " + logopath);
    return null;
  }

  public String getLogoFilenameForStationAndIssue(
      String name, int sid, @Nullable LookupIssue issue) {
    String normalized;
    String result = null;
    if (name != null
        && (normalized = StationLogo.getNormalizedStationName(name)) != null
        && !normalized.isEmpty()) {
      synchronized (this) {
        if (!getDb().isOpen()) {
          openOrCreateDb();
        }
        result = getExactSid(normalized, name, sid);
        if (result == null) {
          result = getExactMatch(normalized, name, sid);
        }
        if (result == null) {
          result = getSubstring(normalized, name, sid, issue);
        }
      }
    }
    return result;
  }

  private String getExactSid(String normalizedStationName, String realStationName, int serviceId) {
    String result = null;
    String queryStr = "SELECT * FROM 'logos' where sid=" + serviceId;
    String queryStrUser = queryStr + " AND path like '%" + Strings.LOGO_PATH_USER + "%'";
    Cursor rawQuery = getDb().rawQuery(queryStrUser, null);
    if (rawQuery.getCount() != 1) {
      rawQuery = getDb().rawQuery(queryStr, null);
    }
    if (rawQuery.getCount() > 1) {
      C0162a.m9a("ambiguous sid results for '" + realStationName + "':");
      while (rawQuery.moveToNext()) {
        C0162a.m9a(" " + rawQuery.getInt(rawQuery.getColumnIndex("sid")));
      }
      result = null;
    } else if (rawQuery.getCount() < 1) {
      result = null;
    } else if (rawQuery.moveToNext()) {
      result = rawQuery.getString(rawQuery.getColumnIndex("path"));
    }
    rawQuery.close();
    return result;
  }

  private String getExactMatch(
      String normalizedStationName, String realStationName, int serviceId) {
    String result = null;
    String queryStr = "SELECT * FROM 'logos' where station='" + normalizedStationName + "'";
    String queryStrUser = queryStr + " AND path like '%" + Strings.LOGO_PATH_USER + "%'";
    Cursor rawQuery = getDb().rawQuery(queryStrUser, null);
    if (rawQuery.getCount() != 1) {
      rawQuery = getDb().rawQuery(queryStr, null);
    }
    if (rawQuery.getCount() > 1) {
      C0162a.m9a("ambiguous exact match results for '" + realStationName + "':");
      while (rawQuery.moveToNext()) {
        C0162a.m9a(" " + rawQuery.getString(rawQuery.getColumnIndex("path")));
      }
      result = null;
    } else if (rawQuery.getCount() < 1) {
      result = null;
    } else if (rawQuery.moveToNext()) {
      result = rawQuery.getString(rawQuery.getColumnIndex("path"));
      int sid = rawQuery.getInt(rawQuery.getColumnIndex("sid"));
      if (sid == 0) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("sid", Integer.valueOf(serviceId));
        contentValues.put(
            DabService.EXTRA_STATION,
            rawQuery.getString(rawQuery.getColumnIndex(DabService.EXTRA_STATION)));
        contentValues.put("path", result);
        int uniqueRowId = rawQuery.getInt(rawQuery.getColumnIndex("_id"));
        contentValues.put("_id", Integer.valueOf(uniqueRowId));
        String where = "_id=" + uniqueRowId;
        if (getDb().update("logos", contentValues, where, null) != 1) {
          C0162a.m9a(" exact match: OUCH: !=1 modified rows for _id=" + uniqueRowId);
        }
      }
    }
    rawQuery.close();
    return result;
  }

  private String getSubstring(
      String normalizedStationName,
      String realStationName,
      int serviceId,
      @Nullable LookupIssue issue) {
    String result;
    String queryStr = "SELECT * FROM 'logos' where station like '%" + normalizedStationName + "%'";
    String queryStrUser = queryStr + " AND path like '%" + Strings.LOGO_PATH_USER + "%'";
    Cursor rawQuery = getDb().rawQuery(queryStrUser, null);
    if (rawQuery.getCount() != 1) {
      rawQuery = getDb().rawQuery(queryStr, null);
    }
    if (rawQuery.getCount() > 1) {
      if (issue != null) {
        issue.setStationName(realStationName);
        issue.setType(LookupIssueType.AMBIGUOUS);
      }
      C0162a.m9a("ambiguous substring search results for '" + realStationName + "':");
      while (rawQuery.moveToNext()) {
        C0162a.m9a(" " + rawQuery.getString(rawQuery.getColumnIndex("path")));
        if (issue != null) {
          issue.addDetail(rawQuery.getString(rawQuery.getColumnIndex("path")));
        }
      }
      result = null;
    } else if (rawQuery.getCount() < 1) {
      if (issue != null) {
        issue.setStationName(realStationName);
        issue.setType(LookupIssueType.NOMATCH);
      }
      result = null;
    } else {
      result = null;
      if (rawQuery.moveToNext()) {
        result = rawQuery.getString(rawQuery.getColumnIndex("path"));
        int sid = rawQuery.getInt(rawQuery.getColumnIndex("sid"));
        if (sid == 0) {
          ContentValues contentValues = new ContentValues();
          contentValues.put("sid", Integer.valueOf(serviceId));
          contentValues.put(
              DabService.EXTRA_STATION,
              rawQuery.getString(rawQuery.getColumnIndex(DabService.EXTRA_STATION)));
          contentValues.put("path", result);
          int uniqueRowId = rawQuery.getInt(rawQuery.getColumnIndex("_id"));
          contentValues.put("_id", Integer.valueOf(uniqueRowId));
          String where = "_id=" + uniqueRowId;
          if (getDb().update("logos", contentValues, where, null) != 1) {
            C0162a.m9a(" substring: OUCH: !=1 modified rows for _id=" + uniqueRowId);
          }
        }
      }
    }
    rawQuery.close();
    return result;
  }

  /* renamed from: com.ex.dabplayer.pad.dab.LogoDb$LookupIssue */
  /* loaded from: classes.dex */
  public class LookupIssue {
    private LookupIssueType mType = LookupIssueType.NONE;
    private String mStationName = "";
    private ArrayList<String> details = new ArrayList<>();

    public LookupIssue() {}

    public void addDetail(String strDetail) {
      this.details.add(strDetail);
    }

    public String getStationName() {
      return this.mStationName;
    }

    public void setStationName(String name) {
      this.mStationName = name;
    }

    public ArrayList<String> getDetails() {
      return this.details;
    }

    public void setType(LookupIssueType type) {
      this.mType = type;
    }

    public LookupIssueType getType() {
      return this.mType;
    }
  }

  private static final String LOGO_PATH =
      Strings.DAB_path() + File.separator + "logos" + File.separator;
  public static final String LOGO_PATH_USER = LOGO_PATH + "user" + File.separator;

  /* added to store mot as station logo */
  public boolean storeUserStationLogo(Drawable drawable, String stationName, int serviceId) {
    FileOutputStream fos;
    boolean result = false;
    if (drawable == null || stationName == null) {
      return false;
    }
    File logoUserDir = new File(LOGO_PATH_USER);
    boolean dirExists = logoUserDir.exists();
    if (!dirExists) {
      dirExists = logoUserDir.mkdirs();
    }
    if (!dirExists) {
      C0162a.m9a("does not exist and cannot be created:" + LOGO_PATH_USER);
    } else {
      Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
      File imageFile = new File(logoUserDir, stationName + ".png");
      try {
        fos = new FileOutputStream(imageFile);
        result = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        fos.close();
        if (result) {
          StationLogo logo = new StationLogo();
          logo.mLogoPathFilename = imageFile.getAbsolutePath();
          logo.mStationServiceId = serviceId;
          logo.mStationNameNormalized = StationLogo.getNormalizedStationName(stationName);
          result = updateOrInsertStationLogo(logo);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return result;
  }
}

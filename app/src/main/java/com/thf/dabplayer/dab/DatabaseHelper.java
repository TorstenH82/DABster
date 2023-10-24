package com.thf.dabplayer.dab;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.thf.dabplayer.service.DabService;
import com.thf.dabplayer.utils.C0162a;
import java.util.ArrayList;
import java.util.List;

/* renamed from: com.ex.dabplayer.pad.dab.d */
/* loaded from: classes.dex */
public class DatabaseHelper {
  //public static final int STATIONLIST_FILTER_ALL = 0;
  //public static final int STATIONLIST_FILTER_FAV = 2;
  //public static final int STATIONLIST_FILTER_FAV_AND_PTY = 3;
  //public static final int STATIONLIST_FILTER_PTY = 1;
  //public static final int STATIONLIST_RANGE_ALL = 32;
  //public static final int STATIONLIST_RANGE_FAV = -33;
  private static int favCount = 0;
  private Context mContext;
 // private int mCurrentStation;
  private SQLiteDatabase mDatabase;
 //private int mFilter = 0;
 // private final String table_preset = "preset";
  private final String table_service = "service";
 // private final String pref_name = "playing";
  //private final String pref_key_playing = "current_playing";
 // private final String pref_key_filter = "current_filter";

  public DatabaseHelper(Context context) {
    this.mContext = context;
    OpenDatabase();
    updateFavCount();
  }

  /* renamed from: a */
  private void insertStation(DabSubChannelInfo subChannelInfo) {
    ContentValues contentValues = new ContentValues();
    contentValues.put("label", subChannelInfo.mLabel.trim());
    contentValues.put("subid", Byte.valueOf(subChannelInfo.mSubChannelId));
    contentValues.put(DabService.EXTRA_BITRATE, Integer.valueOf(subChannelInfo.mBitrate));
    contentValues.put("sid", Integer.valueOf(subChannelInfo.mSID));
    contentValues.put("freq", Integer.valueOf(subChannelInfo.mFreq));
    contentValues.put(DabService.EXTRA_PTY, Byte.valueOf(subChannelInfo.mPty));
    contentValues.put("type", Byte.valueOf(subChannelInfo.mType));
    contentValues.put("abbreviated", Byte.valueOf(subChannelInfo.mAbbreviatedFlag));
    contentValues.put("eid", Integer.valueOf(subChannelInfo.mEID));
    contentValues.put("elabel", subChannelInfo.mEnsembleLabel.trim());
    contentValues.put("scid", Integer.valueOf(subChannelInfo.mSCID));
    contentValues.put("ps", Integer.valueOf(subChannelInfo.mPS));
    contentValues.put("fav", Integer.valueOf(subChannelInfo.mFavorite));
    SQLiteDatabase sQLiteDatabase = this.mDatabase;
    getClass();
    sQLiteDatabase.insert("service", null, contentValues);
    updateFavCount();
  }

  /* renamed from: a */
  private boolean isInList(List list, DabSubChannelInfo subChannelInfo) {
    for (int i = 0; i < list.size(); i++) {
      DabSubChannelInfo subChannelInfo2 = (DabSubChannelInfo) list.get(i);
      if (subChannelInfo2.mBitrate == subChannelInfo.mBitrate
          && subChannelInfo2.mSubChannelId == subChannelInfo.mSubChannelId
          && subChannelInfo2.mEID == subChannelInfo.mEID
          && subChannelInfo2.mSID == subChannelInfo.mSID) {
        C0162a.m5a(subChannelInfo2.mLabel, " already exists");
        return true;
      }
    }
    return false;
  }

  private void OpenDatabase() {
    C0162a.m9a("open dab.db");
    this.mDatabase =
        SQLiteDatabase.openOrCreateDatabase(
            String.valueOf(this.mContext.getFilesDir().getAbsolutePath()) + "/dab.db",
            (SQLiteDatabase.CursorFactory) null);
    if (this.mDatabase.rawQuery("select * from sqlite_master where type='table'", null).getCount()
        == 1) {
      C0162a.m9a("create dab.db");
      SQLiteDatabase sQLiteDatabase = this.mDatabase;
      this.mDatabase.execSQL(
          "CREATE TABLE service (_id INTEGER PRIMARY KEY AUTOINCREMENT, label TEXT, subid INTEGER, bitrate INTEGER, sid INTEGER, freq INTEGER, pty INTEGER, type INTEGER, abbreviated INTEGER, eid INTEGER, elabel TEXT, scid INTEGER, ps INTEGER, fav INTEGER)");
    }
  }
  
  /* renamed from: a */
  public int insertNewStations(List list) {
    int new_stations = 0;
    List c = getServiceSubchannelInfo();
    for (int i = 0; i < list.size(); i++) {
      DabSubChannelInfo subChannelInfo = (DabSubChannelInfo) list.get(i);
      if (!isInList(c, subChannelInfo)) {
        c.add(subChannelInfo);
        insertStation(subChannelInfo);
        new_stations++;
      }
    }
    return new_stations;
  }
  
  /* renamed from: c */
  public List getStationList() {
    return getServiceSubchannelInfo();
  }

  public DabSubChannelInfo getFavouriteService(int pos) {
    if (this.mDatabase.isOpen()) {
      String query = "SELECT * FROM service where fav=" + pos;
      Cursor rawQuery = this.mDatabase.rawQuery(query.toString(), null);
      while (rawQuery.moveToNext()) {
        DabSubChannelInfo subchannelinfo = new DabSubChannelInfo();
        subchannelinfo.mLabel = rawQuery.getString(rawQuery.getColumnIndex("label"));
        subchannelinfo.mSubChannelId = (byte) rawQuery.getInt(rawQuery.getColumnIndex("subid"));
        subchannelinfo.mBitrate =
            rawQuery.getInt(rawQuery.getColumnIndex(DabService.EXTRA_BITRATE));
        subchannelinfo.mSID = rawQuery.getInt(rawQuery.getColumnIndex("sid"));
        subchannelinfo.mFreq = rawQuery.getInt(rawQuery.getColumnIndex("freq"));
        subchannelinfo.mPty = (byte) rawQuery.getInt(rawQuery.getColumnIndex(DabService.EXTRA_PTY));
        subchannelinfo.mType = (byte) rawQuery.getInt(rawQuery.getColumnIndex("type"));
        subchannelinfo.mAbbreviatedFlag =
            (byte) rawQuery.getInt(rawQuery.getColumnIndex("abbreviated"));
        subchannelinfo.mEID = rawQuery.getInt(rawQuery.getColumnIndex("eid"));
        subchannelinfo.mEnsembleLabel = rawQuery.getString(rawQuery.getColumnIndex("elabel"));
        subchannelinfo.mSCID = rawQuery.getInt(rawQuery.getColumnIndex("scid"));
        subchannelinfo.mPS = rawQuery.getInt(rawQuery.getColumnIndex("ps"));
        subchannelinfo.mFavorite = rawQuery.getInt(rawQuery.getColumnIndex("fav"));
        return subchannelinfo;
      }
    }
    return null;
  }

  /* renamed from: c */
  public List getServiceSubchannelInfo() {
    List<DabSubChannelInfo> arrayList = new ArrayList<>();
    List<DabSubChannelInfo> dummies = null;
    if (!this.mDatabase.isOpen()) {
      OpenDatabase();
    }
    String query = "SELECT * FROM service";
    // getClass();
    // StringBuilder query = new StringBuilder(append.append("service").toString());
    
    query += " ORDER BY label COLLATE NOCASE ASC";
    C0162a.m9a(query.toString());
    Cursor rawQuery = this.mDatabase.rawQuery(query.toString(), null);
    while (rawQuery.moveToNext()) {
      DabSubChannelInfo subchannelinfo = new DabSubChannelInfo();
      subchannelinfo.mLabel = rawQuery.getString(rawQuery.getColumnIndex("label"));
      subchannelinfo.mSubChannelId = (byte) rawQuery.getInt(rawQuery.getColumnIndex("subid"));
      subchannelinfo.mBitrate = rawQuery.getInt(rawQuery.getColumnIndex(DabService.EXTRA_BITRATE));
      subchannelinfo.mSID = rawQuery.getInt(rawQuery.getColumnIndex("sid"));
      subchannelinfo.mFreq = rawQuery.getInt(rawQuery.getColumnIndex("freq"));
      subchannelinfo.mPty = (byte) rawQuery.getInt(rawQuery.getColumnIndex(DabService.EXTRA_PTY));
      subchannelinfo.mType = (byte) rawQuery.getInt(rawQuery.getColumnIndex("type"));
      subchannelinfo.mAbbreviatedFlag =
          (byte) rawQuery.getInt(rawQuery.getColumnIndex("abbreviated"));
      subchannelinfo.mEID = rawQuery.getInt(rawQuery.getColumnIndex("eid"));
      subchannelinfo.mEnsembleLabel = rawQuery.getString(rawQuery.getColumnIndex("elabel"));
      subchannelinfo.mSCID = rawQuery.getInt(rawQuery.getColumnIndex("scid"));
      subchannelinfo.mPS = rawQuery.getInt(rawQuery.getColumnIndex("ps"));
      subchannelinfo.mFavorite = rawQuery.getInt(rawQuery.getColumnIndex("fav"));
      arrayList.add(subchannelinfo);
    }
    if (0 != 0) {
      for (DabSubChannelInfo s : dummies) {
        arrayList.add(s);
      }
    }
    updateFavCount();
    return arrayList;
  }
  
  public int getStationCount() {
    String query = "SELECT * FROM service";
    Cursor rawQuery = this.mDatabase.rawQuery(query.toString(), null);
    return rawQuery.getCount();
  }
    
  /* renamed from: e */
  public void deleteAllFromServiceTbl() {
    C0162a.m9a("delete all from SQL table service");
    SQLiteDatabase sQLiteDatabase = this.mDatabase;
    StringBuilder append = new StringBuilder().append("DELETE FROM ");
    getClass();
    sQLiteDatabase.execSQL(append.append("service").toString());
  }

  public void deleteNonFavs() {
    C0162a.m9a("delete non-favorites from SQL table service");
    SQLiteDatabase sQLiteDatabase = this.mDatabase;
    StringBuilder append = new StringBuilder().append("DELETE FROM ");
    getClass();
    sQLiteDatabase.execSQL(append.append("service").append(" WHERE fav=0").toString());
  }

  /* renamed from: f */
  public void closeDb() {
    this.mDatabase.close();
    C0162a.m9a("close db");
  }

  public void updateFav(DabSubChannelInfo subChannelInfo, int favoPos) {
    synchronized (this) {
      if (this.mDatabase.isOpen()) {
        // remove existing station from fav position
        String query = "SELECT * FROM service WHERE fav>0 AND fav=" + favoPos;
        Cursor rawQuery = this.mDatabase.rawQuery(query, null);
        while (rawQuery.moveToNext()) {
          int id = rawQuery.getInt(rawQuery.getColumnIndex("_id"));
          ContentValues contentValues = new ContentValues();
          contentValues.put("label", rawQuery.getString(rawQuery.getColumnIndex("label")));
          contentValues.put("subid", rawQuery.getInt(rawQuery.getColumnIndex("subid")));
          contentValues.put("bitrate", rawQuery.getInt(rawQuery.getColumnIndex("bitrate")));
          contentValues.put("sid", rawQuery.getInt(rawQuery.getColumnIndex("sid")));
          contentValues.put("freq", rawQuery.getInt(rawQuery.getColumnIndex("freq")));
          contentValues.put("pty", rawQuery.getInt(rawQuery.getColumnIndex("pty")));
          contentValues.put("type", rawQuery.getInt(rawQuery.getColumnIndex("type")));
          contentValues.put("abbreviated", rawQuery.getInt(rawQuery.getColumnIndex("abbreviated")));
          contentValues.put("eid", rawQuery.getInt(rawQuery.getColumnIndex("eid")));
          contentValues.put("elabel", rawQuery.getString(rawQuery.getColumnIndex("elabel")));
          contentValues.put("scid", rawQuery.getInt(rawQuery.getColumnIndex("scid")));
          contentValues.put("ps", rawQuery.getInt(rawQuery.getColumnIndex("ps")));
          contentValues.put("fav", 0);
          String where = "_id=" + id;
          SQLiteDatabase sQLiteDatabase = this.mDatabase;
          sQLiteDatabase.update("service", contentValues, where, null);
        }

        C0162a.m9a("updateFav " + subChannelInfo.mLabel + " to pos " + favoPos);
        ContentValues contentValues = new ContentValues();
        contentValues.put("label", subChannelInfo.mLabel);
        contentValues.put("subid", Byte.valueOf(subChannelInfo.mSubChannelId));
        contentValues.put("bitrate", Integer.valueOf(subChannelInfo.mBitrate));
        contentValues.put("sid", Integer.valueOf(subChannelInfo.mSID));
        contentValues.put("freq", Integer.valueOf(subChannelInfo.mFreq));
        contentValues.put("pty", Byte.valueOf(subChannelInfo.mPty));
        contentValues.put("type", Byte.valueOf(subChannelInfo.mType));
        contentValues.put("abbreviated", Byte.valueOf(subChannelInfo.mAbbreviatedFlag));
        contentValues.put("eid", Integer.valueOf(subChannelInfo.mEID));
        contentValues.put("elabel", subChannelInfo.mEnsembleLabel);
        contentValues.put("scid", Integer.valueOf(subChannelInfo.mSCID));
        contentValues.put("ps", Integer.valueOf(subChannelInfo.mPS));
        contentValues.put("fav", Integer.valueOf(favoPos));
        String where =
            "freq="
                + subChannelInfo.mFreq
                + " AND sid="
                + subChannelInfo.mSID
                + " AND subid="
                + ((int) subChannelInfo.mSubChannelId)
                + " AND bitrate="
                + subChannelInfo.mBitrate;
        SQLiteDatabase sQLiteDatabase = this.mDatabase;

        int response = sQLiteDatabase.update("service", contentValues, where, null);

        C0162a.m9a(
            "updateFav " + subChannelInfo.mLabel + " to pos " + favoPos + " returned " + response);
        updateFavCount();
      }
    }
  }

  private void updateFavCount() {
    synchronized (this) {
      if (this.mDatabase.isOpen()) {
        String query = "SELECT * FROM where fav>0";
        Cursor rawQuery = this.mDatabase.rawQuery(query, null);
        this.favCount = rawQuery.getCount();
      }
    }
  }

  public static int getFavCount() {
    return favCount;
  }

  public void delete(DabSubChannelInfo subChannelInfo) {
    synchronized (this) {
      if (this.mDatabase.isOpen()) {
        C0162a.m9a("remove " + subChannelInfo.mLabel);
        String where =
            "freq="
                + subChannelInfo.mFreq
                + " AND sid="
                + subChannelInfo.mSID
                + " AND subid="
                + ((int) subChannelInfo.mSubChannelId)
                + " AND bitrate="
                + subChannelInfo.mBitrate;
        SQLiteDatabase sQLiteDatabase = this.mDatabase;
        getClass();
        int deleted = sQLiteDatabase.delete("service", where, null);
        if (deleted != 1) {
          C0162a.m9a("delete OUCH: " + deleted + " rows");
        }
        updateFavCount();
      }
    }
  }

  public ArrayList<DabSubChannelInfo> getFavorites() {
    ArrayList<DabSubChannelInfo> results = new ArrayList<>(getFavCount());
    synchronized (this) {
      String query = "SELECT * FROM service where fav>0";
      Cursor rawQuery = this.mDatabase.rawQuery(query, null);
      while (rawQuery.moveToNext()) {
        DabSubChannelInfo subChannelInfo = new DabSubChannelInfo();
        subChannelInfo.mLabel = rawQuery.getString(rawQuery.getColumnIndex("label"));
        subChannelInfo.mSubChannelId = (byte) rawQuery.getInt(rawQuery.getColumnIndex("subid"));
        subChannelInfo.mBitrate =
            rawQuery.getInt(rawQuery.getColumnIndex(DabService.EXTRA_BITRATE));
        subChannelInfo.mSID = rawQuery.getInt(rawQuery.getColumnIndex("sid"));
        subChannelInfo.mFreq = rawQuery.getInt(rawQuery.getColumnIndex("freq"));
        subChannelInfo.mPty = (byte) rawQuery.getInt(rawQuery.getColumnIndex(DabService.EXTRA_PTY));
        subChannelInfo.mType = (byte) rawQuery.getInt(rawQuery.getColumnIndex("type"));
        subChannelInfo.mAbbreviatedFlag =
            (byte) rawQuery.getInt(rawQuery.getColumnIndex("abbreviated"));
        subChannelInfo.mEID = rawQuery.getInt(rawQuery.getColumnIndex("eid"));
        subChannelInfo.mEnsembleLabel = rawQuery.getString(rawQuery.getColumnIndex("elabel"));
        subChannelInfo.mSCID = rawQuery.getInt(rawQuery.getColumnIndex("scid"));
        subChannelInfo.mPS = rawQuery.getInt(rawQuery.getColumnIndex("ps"));
        subChannelInfo.mFavorite = rawQuery.getInt(rawQuery.getColumnIndex("fav"));
        results.add(subChannelInfo);
      }
    }
    return results;
  }
}

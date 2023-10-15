package com.thf.dabplayer.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.thf.dabplayer.R;
import com.thf.dabplayer.activity.Player;
import com.thf.dabplayer.dab.DabThread;
import com.thf.dabplayer.dab.LogoDb;
import com.thf.dabplayer.dab.LogoDbHelper;
import com.thf.dabplayer.dab.StationLogo;
import com.thf.dabplayer.dab.DabSubChannelInfo;
import com.thf.dabplayer.utils.C0162a;
import com.thf.dabplayer.utils.Strings;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/* renamed from: com.ex.dabplayer.pad.activity.SettingsStationLogoActivity */
/* loaded from: classes.dex */
public class SettingsStationLogoActivity extends Activity {
  private static final String LOGO_PATH =
      Strings.DAB_path() + File.separator + "logos" + File.separator;
  public static final String LOGO_PATH_TMP = LOGO_PATH + "tmp" + File.separator;
  public static final String LOGO_PATH_USER = LOGO_PATH + "user" + File.separator;
  private ProgressDialog mProgressDlg;
  @IdRes private final int R_id_btn_stationlogo_sync = R.id.btn_stationlogo_sync;
  private Button mBtnLogoSync = null;
  @IdRes private final int R_id_expandlist_nologos = R.id.expandlist_nologos;
  private ExpandableListView mExpListViewNoLogos = null;
  private final String HashMapKeyNoLogo = "NoLogo";
  private Context mContext = null;
  private Spinner mCountrySpinner = null;
  private ArrayAdapter mCountrySpinnerAdapter = null;
  private TextView mSyncInfoText = null;
  private boolean mPlayerRecreateNeeded = false;
  private final View.OnClickListener clickListener = new View.OnClickListener() { // from class:
        // com.ex.dabplayer.pad.activity.SettingsStationLogoActivity.1
        @Override // android.view.View.OnClickListener
        public void onClick(View buttonView) {
          if (buttonView.getId() == R.id.btn_stationlogo_sync) {
            /* 2131427411 */
            SettingsStationLogoActivity.this.onClickLogoSync();
            return;
          } else {
            return;
          }
        }
      };

  @Override // android.app.Activity
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    C0162a.m9a("SettingsStationLogoActivity:onCreate");
    setContentView(R.layout.settings_stationlogo);
    this.mContext = getApplicationContext();
    this.mProgressDlg = new ProgressDialog(this);
    this.mProgressDlg.setTitle("");
    this.mProgressDlg.setIndeterminate(true);
    this.mProgressDlg.setCancelable(false);
    this.mProgressDlg.setIndeterminateDrawable(
        getResources().getDrawable(R.anim.progress_dialog_anim));
    this.mBtnLogoSync = (Button) findViewById(R.id.btn_stationlogo_sync);
    if (this.mBtnLogoSync != null) {
      this.mBtnLogoSync.setOnClickListener(this.clickListener);
    }
    this.mExpListViewNoLogos = (ExpandableListView) findViewById(R.id.expandlist_nologos);
    if (this.mExpListViewNoLogos != null) {
      this.mExpListViewNoLogos.setOnGroupClickListener(
          new ExpandableListView.OnGroupClickListener() { // from class:
            // com.ex.dabplayer.pad.activity.SettingsStationLogoActivity.2
            @Override // android.widget.ExpandableListView.OnGroupClickListener
            public boolean onGroupClick(
                ExpandableListView parent, View v, int groupPosition, long id) {
              SettingsStationLogoActivity.this.setListViewHeight(parent, groupPosition);
              return false;
            }
          });
    }
    this.mSyncInfoText = (TextView) findViewById(R.id.sync_info);
    if (this.mSyncInfoText != null) {
      this.mSyncInfoText.setClickable(true);
      this.mSyncInfoText.setOnClickListener(
          new View.OnClickListener() { // from class:
            // com.ex.dabplayer.pad.activity.SettingsStationLogoActivity.3
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
              File f = new File(SettingsStationLogoActivity.LOGO_PATH);
              Uri uri = Uri.fromFile(f);
              Intent intent = new Intent("android.intent.action.GET_CONTENT");
              intent.setDataAndType(uri, "file/*");
              intent.addFlags(268435456);
              if (intent.resolveActivityInfo(
                      SettingsStationLogoActivity.this.getPackageManager(), 0)
                  != null) {
                C0162a.m9a("start intent  on " + uri.toString());
                SettingsStationLogoActivity.this.startActivity(intent);
                return;
              }
              C0162a.m9a("no file browser activity");
              Toast.makeText(
                      SettingsStationLogoActivity.this.mContext,
                      (int) R.string.FileExplorerAppNotInstalled,
                      1)
                  .show();
            }
          });
    }
  }

  @Override // android.app.Activity
  protected void onResume() {
    super.onResume();
    C0162a.m9a("SettingsStationLogoActivity:onResume");
    updateSyncInfo();
    File[] predefinedDirs = {
      new File(LOGO_PATH), new File(LOGO_PATH_USER), new File(LOGO_PATH_TMP)
    };
    for (File f : predefinedDirs) {
      if (!f.exists()) {
        try {
          f.mkdirs();
        } catch (SecurityException e) {
          e.printStackTrace();
        }
      }
      if (!f.isDirectory()) {
        try {
          f.delete();
          f.mkdirs();
        } catch (SecurityException e2) {
          e2.printStackTrace();
        }
      }
      File nomedia = new File(f, ".nomedia");
      if (!nomedia.exists()) {
        try {
          nomedia.createNewFile();
        } catch (IOException | SecurityException e3) {
          e3.printStackTrace();
        }
      }
    }
    updateLookupIssues();
  }

  @Override // android.app.Activity
  protected void onDestroy() {
    super.onDestroy();
    C0162a.m9a("SettingsStationLogoActivity:onDestroy");
    if (this.mPlayerRecreateNeeded) {
      sendBroadcast(new Intent(Player.HomeKeyReceiver.ACTION_RECREATE));
    }
  }

  /* JADX INFO: Access modifiers changed from: private */
  public void onClickLogoSync() {
    new SyncFromFiles().execute(LOGO_PATH);
  }

  /* JADX INFO: Access modifiers changed from: private */
  public void setListViewHeight(ExpandableListView listView, int group) {
    ExpandableListAdapter listAdapter = listView.getExpandableListAdapter();
    if (listAdapter != null) {
      int totalHeight = 0;
      int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), 1073741824);
      for (int i = 0; i < listAdapter.getGroupCount(); i++) {
        View groupItem = listAdapter.getGroupView(i, false, null, listView);
        groupItem.measure(desiredWidth, 0);
        totalHeight += groupItem.getMeasuredHeight();
        if ((listView.isGroupExpanded(i) && i != group)
            || (!listView.isGroupExpanded(i) && i == group)) {
          for (int j = 0; j < listAdapter.getChildrenCount(i); j++) {
            View listItem = listAdapter.getChildView(i, j, false, null, listView);
            listItem.measure(desiredWidth, 0);
            totalHeight += listItem.getMeasuredHeight();
          }
        }
      }
      ViewGroup.LayoutParams params = listView.getLayoutParams();
      int height = totalHeight + (listView.getDividerHeight() * (listAdapter.getGroupCount() - 1));
      if (height < 10) {
        height = DabThread.AUDIOSTATE_PLAY;
      }
      params.height = height;
      listView.setLayoutParams(params);
      listView.requestLayout();
    }
  }

  public static boolean storeUserStationLogo(
      Context context, Drawable drawable, String stationName, int serviceId) {
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
          LogoDb logoDb = LogoDbHelper.getInstance(context);
          StationLogo logo = new StationLogo();
          logo.mLogoPathFilename = imageFile.getAbsolutePath();
          logo.mStationServiceId = serviceId;
          logo.mStationNameNormalized = StationLogo.getNormalizedStationName(stationName);
          result = logoDb.updateOrInsertStationLogo(logo);
        }

      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return result;
  }

  /* JADX INFO: Access modifiers changed from: private */
  public void updateSyncInfo() {
    File f = new File(LOGO_PATH);
    this.mBtnLogoSync.setEnabled(f.exists());
    TextView syncInfoText = (TextView) findViewById(R.id.sync_info);
    if (syncInfoText != null) {
      LogoDb db = LogoDbHelper.getInstance(this.mContext);
      CharSequence text =
          "" + db.getLogosCount() + " " + getResources().getString(R.string.N_logos_in) + " ";
      SpannableString span = new SpannableString(LOGO_PATH);
      span.setSpan(new UnderlineSpan(), 0, LOGO_PATH.length(), 0);
      syncInfoText.setText(TextUtils.concat(text, span));
    }
  }

  /* JADX INFO: Access modifiers changed from: private */
  public void updateLookupIssues() {
    ArrayList<DabSubChannelInfo> stationListShadow;
    ArrayList<LogoDb.LookupIssue> list = new ArrayList<>();
    LogoDb logoDb = LogoDbHelper.getInstance(this.mContext);
    WeakReference<ArrayList<DabSubChannelInfo>> weakStationListShadow = Player.getStationListShadow();
    if (weakStationListShadow != null
        && (stationListShadow = weakStationListShadow.get()) != null
        && stationListShadow.size() > 0) {
      logoDb.getClass();
      LogoDb.LookupIssue lookupIssue = logoDb.new LookupIssue(); // new LogoDb.LookupIssue();
      Iterator<DabSubChannelInfo> it = stationListShadow.iterator();
      while (it.hasNext()) {
        DabSubChannelInfo info = it.next();
        if (logoDb.getLogoFilenameForStationAndIssue(info.mLabel, info.mSID, lookupIssue) == null) {
          logoDb.getClass();
          LogoDb.LookupIssue lookupIssueWithoutPathPrefix = logoDb.new LookupIssue();
          lookupIssueWithoutPathPrefix.setType(lookupIssue.getType());
          lookupIssueWithoutPathPrefix.setStationName(lookupIssue.getStationName());
          Iterator<String> it2 = lookupIssue.getDetails().iterator();
          while (it2.hasNext()) {
            String path = it2.next();
            lookupIssueWithoutPathPrefix.addDetail(path.replaceFirst(LOGO_PATH, ""));
          }
          list.add(lookupIssueWithoutPathPrefix);
          logoDb.getClass();
          lookupIssue = logoDb.new LookupIssue(); // .new LogoDb.LookupIssue();
        }
      }
    }
    ArrayList<String> listHeaders = new ArrayList<>();
    ArrayList<String> listOfStations = new ArrayList<>();
    ArrayList<String> listNoLogos = new ArrayList<>();
    ArrayList<String> listAssetLogos = new ArrayList<>();
    HashMap<String, ArrayList<String>> hashMapIssues = new HashMap<>();
    Iterator<LogoDb.LookupIssue> it3 = list.iterator();
    while (it3.hasNext()) {
      LogoDb.LookupIssue issue = it3.next();
      if (issue.getType() == LogoDb.LookupIssueType.NOMATCH) {
        if (LogoAssets.isLogoStation(issue.getStationName())) {
          listAssetLogos.add(issue.getStationName());
        } else {
          listNoLogos.add(issue.getStationName());
        }
      } else if (issue.getType() == LogoDb.LookupIssueType.AMBIGUOUS) {
        listHeaders.add(
            getString(R.string.AmbiguousLogosForX, new Object[] {issue.getStationName()}));
        listOfStations.add(issue.getStationName());
        hashMapIssues.put(issue.getStationName(), issue.getDetails());
      }
    }
    if (listAssetLogos.size() > 0) {}
    if (listNoLogos.size() > 0) {
      listHeaders.add(
          getString(
              R.string.NoLogoFoundForXstations,
              new Object[] {Integer.valueOf(listNoLogos.size())}));
      listOfStations.add("NoLogo");
      hashMapIssues.put("NoLogo", listNoLogos);
    }
    try {
      LookupIssuesExpandableListViewAdapter expandableListViewAdapter =
          new LookupIssuesExpandableListViewAdapter(
              this.mContext, listHeaders, listOfStations, hashMapIssues);
      if (this.mExpListViewNoLogos != null && expandableListViewAdapter != null) {
        this.mExpListViewNoLogos.setAdapter(expandableListViewAdapter);
        for (int i = 0; i < expandableListViewAdapter.getGroupCount(); i++) {
          setListViewHeight(this.mExpListViewNoLogos, i);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /* JADX INFO: Access modifiers changed from: private */
  /* renamed from: com.ex.dabplayer.pad.activity.SettingsStationLogoActivity$LookupIssuesExpandableListViewAdapter */
  /* loaded from: classes.dex */
  public class LookupIssuesExpandableListViewAdapter extends BaseExpandableListAdapter {
    private Context mContext;
    private HashMap<String, ArrayList<String>> mIssuePerStationName;
    private ArrayList<String> mListHeaders;
    private ArrayList<String> mStationsWithIssues;
    @LayoutRes private final int R_layout_lookup_listgroup = R.layout.lookup_listgroup;
    @IdRes private final int R_id_issueText = R.id.issueText;
    @LayoutRes private final int R_layout_lookup_listitem = R.layout.lookup_listitem;
    @IdRes private final int R_id_stationOrFile = R.id.stationOrFile;

    public LookupIssuesExpandableListViewAdapter(
        Context context,
        ArrayList<String> headers,
        ArrayList<String> stationsWithIssues,
        HashMap<String, ArrayList<String>> issuePerStationName)
        throws Exception {
      this.mContext = context;
      this.mListHeaders = headers;
      this.mStationsWithIssues = stationsWithIssues;
      this.mIssuePerStationName = issuePerStationName;
      if (headers.size() != stationsWithIssues.size()) {
        throw new Exception(
            "headers.size() (="
                + headers.size()
                + ") != stationsWithIssues.size() (="
                + stationsWithIssues.size());
      }
    }

    @Override // android.widget.ExpandableListAdapter
    public Object getChild(int groupPosition, int childPosititon) {
      return this.mIssuePerStationName
          .get(this.mStationsWithIssues.get(groupPosition))
          .get(childPosititon);
    }

    @Override // android.widget.ExpandableListAdapter
    public long getChildId(int groupPosition, int childPosition) {
      return childPosition;
    }

    @Override // android.widget.ExpandableListAdapter
    public View getChildView(
        int groupPosition,
        int childPosition,
        boolean isLastChild,
        View convertView,
        ViewGroup parent) {
      String childText = (String) getChild(groupPosition, childPosition);
      if (convertView == null) {
        LayoutInflater layoutInflater =
            (LayoutInflater) this.mContext.getSystemService("layout_inflater");
        convertView = layoutInflater.inflate(R.layout.lookup_listitem, (ViewGroup) null);
      }
      TextView stationOrFile = (TextView) convertView.findViewById(R.id.stationOrFile);
      stationOrFile.setText(childText);
      return convertView;
    }

    @Override // android.widget.ExpandableListAdapter
    public int getChildrenCount(int groupPosition) {
      return this.mIssuePerStationName.get(this.mStationsWithIssues.get(groupPosition)).size();
    }

    @Override // android.widget.ExpandableListAdapter
    public Object getGroup(int groupPosition) {
      return this.mStationsWithIssues.get(groupPosition);
    }

    @Override // android.widget.ExpandableListAdapter
    public int getGroupCount() {
      return this.mStationsWithIssues.size();
    }

    @Override // android.widget.ExpandableListAdapter
    public long getGroupId(int groupPosition) {
      return groupPosition;
    }

    @Override // android.widget.ExpandableListAdapter
    public View getGroupView(
        int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
      String headerTitle = this.mListHeaders.get(groupPosition);
      if (convertView == null) {
        LayoutInflater layoutInflater =
            (LayoutInflater) this.mContext.getSystemService("layout_inflater");
        convertView = layoutInflater.inflate(R.layout.lookup_listgroup, (ViewGroup) null);
      }
      TextView lblListHeader = (TextView) convertView.findViewById(R.id.issueText);
      lblListHeader.setText(headerTitle);
      return convertView;
    }

    @Override // android.widget.ExpandableListAdapter
    public boolean hasStableIds() {
      return false;
    }

    @Override // android.widget.ExpandableListAdapter
    public boolean isChildSelectable(int groupPosition, int childPosition) {
      return false;
    }
  }

  /* JADX INFO: Access modifiers changed from: private */
  @SuppressLint({"StaticFieldLeak"})
  /* renamed from: com.ex.dabplayer.pad.activity.SettingsStationLogoActivity$SyncFromFiles */
  /* loaded from: classes.dex */
  public class SyncFromFiles extends AsyncTask<String, String, Long> {
    private int mOldLogoCount;

    private SyncFromFiles() {}

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.os.AsyncTask
    public Long doInBackground(String... path) {
      String filepath;
      LogoDb db = LogoDbHelper.getInstance(SettingsStationLogoActivity.this.mContext);
      db.clearDb();
      C0162a.m9a("logo sync from " + path[0]);
      Long totalFileCount = 0L;
      File directory = new File(path[0]);
      if (!directory.isDirectory()) {
        C0162a.m9a("not exist or no dir:" + directory.getAbsolutePath());
        return 0L;
      }
      File[] files = directory.listFiles();
      if (files != null) {
        for (File file : files) {
          if (isCancelled()) {
            break;
          }
          if (file.isDirectory()) {
            if (SettingsStationLogoActivity.LOGO_PATH_TMP.contains(file.getAbsolutePath())) {
              C0162a.m9a("skip dir " + file.getAbsolutePath());
            } else {
              C0162a.m9a("enter dir " + file.getAbsolutePath());
              File[] filesInSubdir = file.listFiles();
              if (filesInSubdir != null) {
                for (File fileInSubdir : filesInSubdir) {
                  if (!isCancelled()) {
                    if (fileInSubdir.isFile()) {
                      try {
                        filepath = fileInSubdir.getCanonicalPath();
                      } catch (IOException e) {
                        e.printStackTrace();
                        filepath = "";
                      }
                      StationLogo stationLogo =
                          StationLogo.create(filepath, fileInSubdir.getName());
                      if (stationLogo != null) {
                        publishProgress(fileInSubdir.getName());
                        totalFileCount = Long.valueOf(totalFileCount.longValue() + 1);
                        db.insertStationLogo(stationLogo);
                      }
                    } else {
                      C0162a.m9a("ignore " + fileInSubdir.getName());
                    }
                  }
                }
              } else {
                C0162a.m9a("empty dir:" + file.getAbsolutePath());
              }
            }
          } else {
            C0162a.m9a("no dir:" + file.getName());
          }
        }
      } else {
        C0162a.m9a("empty dir:" + directory.getAbsolutePath());
      }
      return totalFileCount;
    }

    @Override // android.os.AsyncTask
    protected void onPreExecute() {
      LogoDb db = LogoDbHelper.getInstance(SettingsStationLogoActivity.this.mContext);
      this.mOldLogoCount = db.getLogosCount();
      SettingsStationLogoActivity.this.mProgressDlg.setTitle(R.string.Synchronisation);
      SettingsStationLogoActivity.this.mProgressDlg.show();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.os.AsyncTask
    public void onProgressUpdate(String... fileName) {
      SettingsStationLogoActivity.this.mProgressDlg.setMessage(fileName[0]);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.os.AsyncTask
    public void onPostExecute(Long result) {
      C0162a.m9a("sync result:" + result);
      SettingsStationLogoActivity.this.updateSyncInfo();
      SettingsStationLogoActivity.this.updateLookupIssues();
      SettingsStationLogoActivity.this.mProgressDlg.dismiss();
      LogoDb db = LogoDbHelper.getInstance(SettingsStationLogoActivity.this.mContext);
      if (this.mOldLogoCount != db.getLogosCount()) {
        C0162a.m9a("logo count changed: require layout recreation");
        SettingsStationLogoActivity.this.mPlayerRecreateNeeded = true;
      }
    }
  }

  /* renamed from: com.ex.dabplayer.pad.activity.SettingsStationLogoActivity$StationLogosByCountry */
  /* loaded from: classes.dex */
  private class StationLogosByCountry {
    public String mLabel;
    public String mPathCountryLogo;
    public String mRelativeURLCountryLogo;
    public String mURLPackage;

    private StationLogosByCountry() {}
  }
}
package com.thf.dabplayer.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
// import android.support.p000v4.media.MediaMetadataCompat;
// import android.support.p000v4.media.session.MediaSessionCompat;
// import android.support.p000v4.view.MotionEventCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.KeyEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.thf.dabplayer.R;
// import com.thf.dabplayer.activity.StationBaseAdapter;
import com.thf.dabplayer.dab.DabThread;
import com.thf.dabplayer.dab.ChannelInfo;
import com.thf.dabplayer.dab.LogoDb;
import com.thf.dabplayer.dab.LogoDbHelper;
import com.thf.dabplayer.dab.StationLogo;
import com.thf.dabplayer.dab.DabSubChannelInfo;
import com.thf.dabplayer.service.DabServiceBinder;
import com.thf.dabplayer.service.DabService;
import com.thf.dabplayer.utils.C0162a;
import com.thf.dabplayer.utils.DirCleaner;
import com.thf.dabplayer.utils.ServiceFollowing;
import com.thf.dabplayer.utils.SharedPreferencesHelper;
import com.thf.dabplayer.utils.Strings;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import android.app.Activity;

/* renamed from: com.ex.dabplayer.pad.activity.Player */
/* loaded from: classes.dex */
public class Player extends Activity implements ServiceConnection, View.OnClickListener {
  public static final int PLAYERMSG_ASSET_FOUND_LOGOS = 98;
  public static final int PLAYERMSG_AUDIO_DISTORTION = 102;
  public static final int PLAYERMSG_DISMISS_SERVICE_FOLLOWING = 24;
  public static final int PLAYERMSG_DLS = 9;
  public static final int PLAYERMSG_HW_FAILURE = 101;
  public static final int PLAYERMSG_MOT = 10;
  public static final int PLAYERMSG_NEW_LIST_OF_STATIONS = 1;
  public static final int PLAYERMSG_NEW_STATION_LIST = 18;
  public static final int PLAYERMSG_NEXT_STATION = 103;
  public static final int PLAYERMSG_PREV_STATION = 104;
  public static final int PLAYERMSG_SCAN_FINISHED = 99;
  public static final int PLAYERMSG_SCAN_PROGRESS_UPDATE = 0;
  public static final int PLAYERMSG_SHOW_SERVICE_FOLLOWING = 23;
  public static final int PLAYERMSG_HIDE_SERVICE_FOLLOWING = 24;
  public static final int PLAYERMSG_SIGNAL_QUALITY = 11;
  public static final int PLAYERMSG_STATIONINFO_INTENT = 100;
  public static final int PLAYERMSG_SET_STATIONMEMORY = 200;
  public static final int PLAYERMSG_PLAYING_STATION = 300;

  /* renamed from: a */
  public static String[] arrPty;

  /* renamed from: A */
  private List<DabSubChannelInfo> stationList;

  /* renamed from: B */
  private List<ChannelInfo> channelInfoList;

  /* renamed from: C */
  private AudioManager audioManager;

  /* renamed from: E */
  private ProgressDialog progressDialog;

  /* renamed from: G */
  private boolean f19G;

  /* renamed from: I */
  // private SharedPreferences f20I;

  /* renamed from: d */
  public Context context;

  /* renamed from: e */
  public boolean f26e;

  /* renamed from: f */
  public boolean f27f;
  public Button favorBtn;

  /* renamed from: g */
  public boolean f28g;

  /* renamed from: h */
  private int playIndex;

  /* renamed from: j */
  private Button btnPrev;

  /* renamed from: k */
  private Button btnNext;

  /* renamed from: l */
  private Button layScan;
  private Button layExit;

  /* renamed from: m */
  // public Button f34m;
  private float mDefaultLeftAreaLayoutWeight;
  Intent mServiceIntent;
  private boolean mShowLogosInList;
  // private ListView mStationListView;

  /* renamed from: n */
  // private Button f35n;

  /* renamed from: o */
  private TextView txtServiceName;

  /* renamed from: p */
  // private TextView f37p;

  /* renamed from: q */
  // private TextView f38q;

  /* renamed from: r */
  // private MotImage motImage;

  /* renamed from: s */
  private ImageView imgSignalLevel;

  private TextClock textClock;

  /* renamed from: t */
  private TextView txtDls;
  private Toast toast_service_following;

  /* renamed from: v */
  // private Spinner f42v;

  private SwitchStationsAdapter stationsAdapter;
  private LinearLayoutManager linearLayoutManager;
  private RecyclerView recyclerView;
  private SwitchStationsAdapter.Listener switchStationsAdapterListener =
      new SwitchStationsAdapter.Listener() {
        @Override
        public void onItemClick(int position) {
          onStationClicked(position);
        }

        @Override
        public void onLongPress(int position) {
          Toast.makeText(context, "long clicked at " + position, Toast.LENGTH_SHORT).show();
          if (Player.this.motImage != null) {
            if (Player.this.stationList != null && Player.this.stationList.size() > position) {
              DabSubChannelInfo sci = Player.this.stationList.get(position);
              Player.this.mLogoDb.storeUserStationLogo(Player.this.motImage, sci.mLabel, sci.mSID);
            }
          }
        }
      };

  private ViewPagerAdapter viewPagerAdapter;
  private LinearLayoutManager linearLayoutManagerPageViewer;
  private ViewPager2 viewPager;
  private ViewPagerAdapter.Listener viewPagerAdapterListener =
      new ViewPagerAdapter.Listener() {
        @Override
        public void onItemClick(int memoryPos) {
          if (!Player.this.isInitialized) {
            String text = getResources().getString(R.string.waitfewseconds);
            Toast.makeText(Player.this.context, text, Toast.LENGTH_LONG).show();
            return;
          }
          playFavourite(memoryPos);
        }

        @Override
        public void onLongPress(int memoryPos) {
          if (!Player.this.isInitialized) {
            String text = getResources().getString(R.string.waitfewseconds);
            Toast.makeText(Player.this.context, text, Toast.LENGTH_LONG).show();
            return;
          }
          setMemory(memoryPos);
          // this comes back with PLAYERMSG_SET_STATIONMEMORY
        }
      };
  private TabLayout tabLayout;

  // private LinearLayout memory1, memory2, memory3, memory4, memory5, memory6;

  /* renamed from: w */
  public Handler dabHandler;

  /* renamed from: x */
  private DabService dabService;

  /* renamed from: H */
  // private static HomeKeyReceiver homeKeyReceiver = null;
  private static WeakReference<Intent> sMainActivityStartIntent = null;
  private static WeakReference<Handler> sPlayerHandler = null;
  private static ArrayList<DabSubChannelInfo> s_stationListShadow = null;

  /* renamed from: D */
  public boolean isInitialized = false;

  /* renamed from: J */
  private String strDls = "";

  /* renamed from: M */
  private Handler f22M = new DabFHandler(this);

  /* renamed from: N */
  private final BroadcastReceiver f23N = new hBroadcastReceiver();
  // @IdRes private final int R_id_left_area = R.id.left_area;

  /* renamed from: c */
  AudioManager.OnAudioFocusChangeListener f24c =
      new AudioManager
          .OnAudioFocusChangeListener() { // from class: com.ex.dabplayer.pad.activity.Player.1
        @Override // android.media.AudioManager.OnAudioFocusChangeListener
        public void onAudioFocusChange(int i) {
          int arg = -1;
          boolean isAudiolossSupportEnabled =
              Player.this
                  .context
                  .getSharedPreferences(SettingsActivity.prefname_settings, 0)
                  .getBoolean(SettingsActivity.pref_key_audioloss_support, true);
          switch (i) {
            case -3:
              C0162a.m9a("AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
              if (isAudiolossSupportEnabled) {
                arg = DabThread.AUDIOSTATE_DUCK;
                break;
              } else {
                C0162a.m9a("no complete audioloss support enabled");
                break;
              }
            case -2:
              C0162a.m9a("AUDIOFOCUS_LOSS_TRANSIENT");
              if (isAudiolossSupportEnabled) {
                arg = DabThread.AUDIOSTATE_PAUSE;
                break;
              } else {
                C0162a.m9a("no complete audioloss support enabled");
                break;
              }
            case -1:
              C0162a.m9a("AUDIOFOCUS_LOSS");
              Player.this.finishTheApp();
              break;
            case 1:
              C0162a.m9a("AUDIOFOCUS_GAIN");
              arg = DabThread.AUDIOSTATE_PLAY;
              break;
            case 2:
              C0162a.m9a("AUDIOFOCUS_GAIN_TRANSIENT");
              break;
            case 3:
              C0162a.m9a("AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK");
              break;
          }
          if (arg != -1 && Player.this.dabHandler != null) {
            Player.this.dabHandler.removeMessages(34);
            Message msg = Player.this.dabHandler.obtainMessage(34);
            msg.arg1 = arg;
            Player.this.dabHandler.sendMessage(msg);
          }
        }
      };

  /* renamed from: i */
  public int stationListSize = 0;
  private boolean isFavoriteListActive = false;
  private boolean isInForeground = false;
  private DelayedRunnableHandler keyDownHandler = new DelayedRunnableHandler();
  private Toast mAudioDistortionToast = null;
  private Toast mChannelToast = null;
  private float mDlsSizeFromStyle = 0.0f;
  private boolean mIsLeftAreaMaximized = false;
  private LogoAssets mLogoAssets = null;
  private LogoDb mLogoDb = null;
  private MediaMetadataCompat mMetaData = new MediaMetadataCompat.Builder().build();
  private boolean mProperShutdown = false;
  private boolean mSendBroadcastIntent = true;
  private boolean mShowAdditionalInfos = true;
  // private StationDetails mStationDetails = new StationDetails();
  private float mStationNameSizeFromStyle = 0.0f;
  private TouchListener mTouchListener = null;
  private ViewFlipper mViewFlipper = null;
  private DelayedRunnableHandler maximizeLeftAreaHandler = new DelayedRunnableHandler();
  private BitmapDrawable motImage;

  /* renamed from: y */
  private UsbManager usbManager = null;

  /* renamed from: z */
  private UsbDevice usbDevice = null;

  /* renamed from: com.ex.dabplayer.pad.activity.Player$DabFHandler */
  /* loaded from: classes.dex */
  public class DabFHandler extends Handler {
    private final WeakReference<Player> mPlayer;

    public DabFHandler(Player player) {
      this.mPlayer = new WeakReference<>(player);
    }

    @Override // android.os.Handler
    public void handleMessage(Message message) {
      Player player = this.mPlayer.get();
      if (player != null) {
        super.handleMessage(message);
        switch (message.what) {
          case Player.PLAYERMSG_SCAN_PROGRESS_UPDATE: // 0:
            player.progressDialog.setProgress(message.arg1);
            player.progressDialog.setMessage(
                Strings.scanning(Player.this.getApplicationContext(), message.arg1, message.arg2));
            player.progressDialog.show();
            break;
          case PLAYERMSG_NEW_LIST_OF_STATIONS: // 1:
            player.stationList = (List) message.obj;
            player.stationListSize = player.stationList.size();
            if (player.stationListSize > 0) {
              player.updateStationList();
            }
            break;
          case PLAYERMSG_DLS: // 9:
            String dls = (String) message.obj;
            if (!player.strDls.equals(dls)) {
              player.strDls = dls;
              player.txtDls.setText(dls);
            }
            break;
          case PLAYERMSG_MOT: // 10:
            player.showMotImage((String) message.obj);
            break;
          case PLAYERMSG_SIGNAL_QUALITY: // 11
            if (!player.f27f && !player.f26e && player.stationListSize == 0) {
              message.arg1 = 0;
            }
            player.setSignalLevel(message.arg1);
            break;
          case 13:
            // Player.access$802(player, (List) message.obj);
            player.channelInfoList = (List) message.obj;
            // Player.access$900(player);
            player.m78f();
            break;
          case PLAYERMSG_NEW_STATION_LIST: // 18
            Toast.makeText(
                    context, "Attention: reveived PLAYERMSG_NEW_STATION_LIST", Toast.LENGTH_LONG)
                .show();
            player.fillStationRecycler((List) message.obj);
            break;
          case 19:
            player.isInitialized = true;
            if (player.stationListSize > 0) {
              player.playStation(player.playIndex);
            }
            break;
          case 23:
            player.showServiceFollowing(true, (String) message.obj, message.arg1);
            break;
          case PLAYERMSG_HIDE_SERVICE_FOLLOWING: // 24
            player.showServiceFollowing(false, (String) message.obj, message.arg1);
            break;
          case Player.PLAYERMSG_ASSET_FOUND_LOGOS /* 98 */:
            if (player.stationList != null) {
              C0162a.m9a("assetlogos refresh display");
              player.updateStationList();
            }
            break;
          case Player.PLAYERMSG_SCAN_FINISHED /* 99 */:
            int i = message.arg1;
            if (player.progressDialog.isShowing()) {
              player.progressDialog.dismiss();
            }
            player.progressDialog.setMessage("");
            player.updateStationList();
            if (player.stationListSize > 0) {
              player.playStation(0);
            }
            break;
          case Player.PLAYERMSG_STATIONINFO_INTENT: // 100
            Intent intent = (Intent) message.obj;
            boolean affectsAndroidMetaData =
                intent.hasExtra(DabService.EXTRA_AFFECTS_ANDROID_METADATA);
            if (affectsAndroidMetaData) {
              affectsAndroidMetaData =
                  intent.getBooleanExtra(DabService.EXTRA_AFFECTS_ANDROID_METADATA, false);
            }
            player.notifyStationInfo(intent, affectsAndroidMetaData);
            break;
          case Player.PLAYERMSG_HW_FAILURE /* 101 */:
            player.toastAndFinish((String) message.obj);
            break;
          case Player.PLAYERMSG_AUDIO_DISTORTION /* 102 */:
            player.notifyAudioDistortion();
            break;
          case Player.PLAYERMSG_NEXT_STATION /* 103 */:
            player.onStationChange_nextWrapper();
            break;
          case Player.PLAYERMSG_PREV_STATION /* 104 */:
            player.onStationChange_prevWrapper();
            break;

          case Player.PLAYERMSG_PLAYING_STATION:
            player.playIndex = message.arg1;
            player.scrollToPositionRecycler(player.playIndex);
            break;

          case PLAYERMSG_SET_STATIONMEMORY:
            viewPagerAdapter.setItems((List) message.obj);

            /*
            List<DabSubChannelInfo> memoryList = (List) message.obj;
            // here we need to set the memory buttons
            for (DabSubChannelInfo sci : memoryList) {
              int memIdx = sci.mFavorite;
              ImageView imageViewMemory = null;
              TextView textViewMemory = null;
              switch (memIdx) {
                case 1:
                  imageViewMemory = findViewById(R.id.memory1Img);
                  textViewMemory = findViewById(R.id.memory1Tv);
                  break;
                case 2:
                  imageViewMemory = findViewById(R.id.memory2Img);
                  textViewMemory = findViewById(R.id.memory2Tv);
                  break;
                case 3:
                  imageViewMemory = findViewById(R.id.memory3Img);
                  textViewMemory = findViewById(R.id.memory3Tv);
                  break;
                case 4:
                  imageViewMemory = findViewById(R.id.memory4Img);
                  textViewMemory = findViewById(R.id.memory4Tv);
                  break;
                case 5:
                  imageViewMemory = findViewById(R.id.memory5Img);
                  textViewMemory = findViewById(R.id.memory5Tv);
                  break;
                case 6:
                  imageViewMemory = findViewById(R.id.memory6Img);
                  textViewMemory = findViewById(R.id.memory6Tv);
                  break;
              }
              if (imageViewMemory != null) {
                textViewMemory.setText(sci.mLabel);
                String pathToLogo = mLogoDb.getLogoFilenameForStation(sci.mLabel, sci.mSID);
                BitmapDrawable logoDrawable = null;
                if (pathToLogo != null) {
                  logoDrawable = mLogoDb.getBitmapForStation(player.context, pathToLogo);
                }
                if (logoDrawable == null) {
                  logoDrawable = LogoAssets.getBitmapForStation(player.context, sci.mLabel);
                }
                if (logoDrawable != null) {
                  imageViewMemory.setImageDrawable(logoDrawable);
                } else {
                  imageViewMemory.setImageResource(R.drawable.radio);
                }
              }
            }
                    */
            break;
          default:
            Toast.makeText(player.context, "msg.what" + message.what, 0).show();
            break;
        }
      }
    }
  }

  /* renamed from: com.ex.dabplayer.pad.activity.Player$FlipViewAnimatorListener */
  /* loaded from: classes.dex */
  public class FlipViewAnimatorListener extends AnimatorListenerAdapter {
    final int mFlipToViewIdx;
    final ObjectAnimator mInvisToVis;

    public FlipViewAnimatorListener(ObjectAnimator invisToVis, int flipToViewIdx) {
      this.mInvisToVis = invisToVis;
      this.mFlipToViewIdx = flipToViewIdx;
    }

    @Override // android.animation.AnimatorListenerAdapter,
    // android.animation.Animator.AnimatorListener
    public void onAnimationEnd(Animator anim) {
      this.mInvisToVis.start();
      Player.this.mViewFlipper.setDisplayedChild(this.mFlipToViewIdx);
    }
  }

  /* renamed from: com.ex.dabplayer.pad.activity.Player$hBroadcastReceiver */
  /* loaded from: classes.dex */
  public class hBroadcastReceiver extends BroadcastReceiver {
    public hBroadcastReceiver() {}

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
      C0162a.m9a("h: " + intent.toString());
      C0162a.m9a("h: " + intent.getExtras().toString());
      String action = intent.getAction();

      if (action.equals("android.hardware.usb.action.USB_DEVICE_DETACHED")) {
        UsbDevice device = (UsbDevice) intent.getParcelableExtra("device");
        C0162a.m9a("USB device detached: " + device.getDeviceName());
        if (device.equals(Player.this.usbDevice)) {
          C0162a.m9a("USB device gone -> finish");
          Player.this.finishTheApp();
        }
      }
    }
  }

  /* renamed from: com.ex.dabplayer.pad.activity.Player$HomeKeyReceiver */
  /* loaded from: classes.dex */
  /*
  public class HomeKeyReceiver extends BroadcastReceiver {
    public static final String ACTION_RECREATE = "com.ex.dabplayer.pad.RECREATE";
    private final WeakReference<Player> mPlayer;

    public HomeKeyReceiver(Player player) {
      this.mPlayer = new WeakReference<>(player);
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
      Player player = this.mPlayer.get();
      if (player != null) {
        if (intent.getAction().equals("android.intent.action.CLOSE_SYSTEM_DIALOGS")) {
          if ("homekey".equals(intent.getStringExtra("reason"))) {
            C0162a.m9a("home key press");
          }
        } else if (intent.getAction().equals(ACTION_RECREATE)) {
          C0162a.m9a("recreate Player");
          Player.this.m79e();
          if (Player.this.dabHandler != null) {
            Player.this.dabHandler.removeMessages(5);
            Message obtainMessage = Player.this.dabHandler.obtainMessage();
            obtainMessage.what = 5;
            obtainMessage.arg1 = 0;
            Player.this.dabHandler.sendMessage(obtainMessage);
          }
          player.recreate();
        }
      }
    }
  }

  /* renamed from: com.ex.dabplayer.pad.activity.Player$MaximizeListener */
  /* loaded from: classes.dex */
  /*
      /*
    public class MaximizeListener extends AnimatorListenerAdapter
        implements ValueAnimator.AnimatorUpdateListener {
      private View mView;

      public MaximizeListener(View view) {
        if (view.getLayoutParams() instanceof LinearLayout.LayoutParams) {
          this.mView = view;
          return;
        }
        throw new IllegalArgumentException("view must be instanceof LinearLayout");
      }

      @Override // android.animation.ValueAnimator.AnimatorUpdateListener
      public void onAnimationUpdate(ValueAnimator animation) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) this.mView.getLayoutParams();
        params.weight = ((Float) animation.getAnimatedValue()).floatValue();
        this.mView.setLayoutParams(params);
        this.mView.getParent().requestLayout();
      }

      @Override // android.animation.AnimatorListenerAdapter,
      // android.animation.Animator.AnimatorListener
      public void onAnimationCancel(Animator animation) {
        super.onAnimationCancel(animation);
        C0162a.m9a("animation cancelled");
      }
    }
  */
  private class SearchDabHandler implements Runnable {
    public void run() {
      int i = 0;
      C0162a.m9a("searching mDabHandler");
      while (Player.this.dabHandler == null && i < 10) {
        i++;
        DabService dabService = Player.this.getDabService();
        if (dabService != null) {
          Player.this.dabHandler = Player.this.getDabService().getDabHandlerFromDabThread();
          C0162a.m9a("mDabHandler:" + Player.this.dabHandler);
        }
        try {
          Thread.sleep(100L);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      if (i >= 10 && Player.this.dabHandler == null) {
        C0162a.m9a("failed searching mDabHandler");
      } else {
        Message obtainMessage = Player.this.dabHandler.obtainMessage();
        obtainMessage.what = DabThread.MSGTYPE_DAB_INIT; // 2;
        Player.this.dabHandler.sendMessage(obtainMessage);
        C0162a.m9a("searching mDabHandler done");
      }
    }
  }

  // @SuppressLint({"StaticFieldLeak"})
  /* renamed from: com.ex.dabplayer.pad.activity.Player$n */
  /* loaded from: classes.dex */

  // private class AsyncTaskC0118n extends AsyncTask<Integer, Long, Long> {
  // private AsyncTaskC0118n() {}

  /* JADX INFO: Access modifiers changed from: protected
      @Override // android.os.AsyncTask
      public Long doInBackground(Integer... objects) {
        int i = 0;
        C0162a.m9a("searching mDabHandler");
        while (Player.this.dabHandler == null && i < 10) {
          i++;
          DabService dabService = Player.this.getDabService();
          if (dabService != null) {
            Player.this.dabHandler = Player.this.getDabService().getDabHandlerFromDabThread();
            C0162a.m9a("mDabHandler:" + Player.this.dabHandler);
          }
          try {
            Thread.sleep(100L);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
        if (i >= 10 && Player.this.dabHandler == null) {
          C0162a.m9a("failed searching mDabHandler");
          return 0L;
        }
        Message obtainMessage = Player.this.dabHandler.obtainMessage();
        obtainMessage.what = DabThread.MSGTYPE_DAB_INIT; // 2;
        Player.this.dabHandler.sendMessage(obtainMessage);
        C0162a.m9a("searching mDabHandler done");
        return 1L;
      }
    }
  */

  /* renamed from: com.ex.dabplayer.pad.activity.Player$RunnableBWrapperNext */
  /* loaded from: classes.dex */
  public class RunnableBWrapperNext implements Runnable {
    public RunnableBWrapperNext() {}

    @Override // java.lang.Runnable
    public void run() {
      Player.this.selectNextStation();
    }
  }

  /* renamed from: com.ex.dabplayer.pad.activity.Player$RunnableCWrapperPrev */
  /* loaded from: classes.dex */
  public class RunnableCWrapperPrev implements Runnable {
    public RunnableCWrapperPrev() {}

    @Override // java.lang.Runnable
    public void run() {
      Player.this.selectPreviousStation();
    }
  }

  /* renamed from: com.ex.dabplayer.pad.activity.Player$RunnableMaximizeLeftArea */
  /* loaded from: classes.dex */
  /*
  public class RunnableMaximizeLeftArea implements Runnable {
    private final boolean mAnimate;
    private final boolean mMaximize;

    public RunnableMaximizeLeftArea(boolean maximize, boolean animate) {
      this.mMaximize = maximize;
      this.mAnimate = animate;
    }

    @Override // java.lang.Runnable
    public void run() {
      Player.this.maximizeLeftArea(this.mMaximize, this.mAnimate);
    }
  }
  */
  /* renamed from: com.ex.dabplayer.pad.activity.Player$StationDetails */
  /* loaded from: classes.dex */
  /*
    public class StationDetails {
      private int mCurrStation = 0;
      private int mTotalStations = 0;
      private String mStationName = "";
      private boolean mIsPlaying = false;

      public StationDetails() {}

      public void updateAllDetailsViewFromIntent(Intent intent) {
        String filename;
        String str;
        String str2;
        int eid;
        if (intent != null) {
                  viewPagerAdapter.setDetails(intent);


          if (intent.hasExtra(DabService.EXTRA_ID)
              && (eid = intent.getIntExtra(DabService.EXTRA_ID, 0)) > 0) {
            C0162a.m8a("* id : ", eid);
          }
          if (intent.hasExtra(DabService.EXTRA_ARTIST)
              && (str2 = intent.getStringExtra(DabService.EXTRA_ARTIST)) != null
              && !str2.isEmpty()) {
            C0162a.m5a("* artist : ", str2);
          }
          if (intent.hasExtra(DabService.EXTRA_TRACK)
              && (str = intent.getStringExtra(DabService.EXTRA_TRACK)) != null
              && !str.isEmpty()) {
            C0162a.m5a("* track : ", str);
          }
          if (intent.hasExtra(DabService.EXTRA_STATION)) {
            this.mStationName = intent.getStringExtra(DabService.EXTRA_STATION);
            if (this.mStationName == null) {
              this.mStationName = "";
            }
            if (!this.mStationName.isEmpty()) {
              C0162a.m5a("* station : ", this.mStationName);
            }
            //updateStationName();
          }
          if (intent.hasExtra(DabService.EXTRA_DLS)) {
            C0162a.m5a("* dls : ", intent.getStringExtra(DabService.EXTRA_DLS));
            updateDls(intent.getStringExtra(DabService.EXTRA_DLS));
          }
          if (intent.hasExtra("playing")) {
            this.mIsPlaying = intent.getBooleanExtra("playing", false);
            C0162a.m9a("* playing : " + this.mIsPlaying);
          }
          if (intent.hasExtra(DabService.EXTRA_SERVICEID)) {
            int sid = intent.getIntExtra(DabService.EXTRA_SERVICEID, 0);
            if (sid > 0) {
              C0162a.m5a("* serviceid : ", String.format("0x%04X", Integer.valueOf(sid)));
            }
            //updateServiceId(sid);
          }
          if (intent.hasExtra(DabService.EXTRA_FREQUENCY_KHZ)) {
            int freq = intent.getIntExtra(DabService.EXTRA_FREQUENCY_KHZ, 0);
            if (freq != 0) {
              C0162a.m8a("* frequency_khz : ", freq);
            }
            updateFrequency(freq);
          }
          if (intent.hasExtra(DabService.EXTRA_PTY)) {
            String str3 = intent.getStringExtra(DabService.EXTRA_PTY);
            if (str3 != null && !str3.isEmpty()) {
              C0162a.m5a("* pty : ", str3);
            }
            updatePty(str3);
          }
          if (intent.hasExtra(DabService.EXTRA_ENSEMBLE_NAME)
              && intent.hasExtra(DabService.EXTRA_ENSEMBLE_ID)) {
            String str4 = intent.getStringExtra(DabService.EXTRA_ENSEMBLE_NAME);
            int id = intent.getIntExtra(DabService.EXTRA_ENSEMBLE_ID, 0);
            if (id > 0) {
              C0162a.m5a("* ensemble_name : ", str4);
              C0162a.m5a("* ensemble_id : ", String.format("0x%04X", Integer.valueOf(id)));
            }
            updateEnsemble(str4, id);
          }
          if (intent.hasExtra(DabService.EXTRA_BITRATE)) {
            int bitrate = intent.getIntExtra(DabService.EXTRA_BITRATE, 0);
            if (bitrate > 0) {
              C0162a.m8a("* bitrate : ", bitrate);
            }
            updateBitrate(bitrate);
          }
          if (intent.hasExtra(DabService.EXTRA_SIGNALQUALITY)) {
            int signal = intent.getIntExtra(DabService.EXTRA_SIGNALQUALITY, -1);
            if (signal >= 0) {
              C0162a.m8a("* signal : ", signal);
            }
            updateSignalQuality(signal);
          }
          if (intent.hasExtra(DabService.EXTRA_SERVICEFOLLOWING)) {
            String info = intent.getStringExtra(DabService.EXTRA_SERVICEFOLLOWING);
            if (!info.isEmpty()) {
              C0162a.m5a("* service_following : ", info);
            }
            updateServiceFollowing(info);
          }
          if (intent.hasExtra(DabService.EXTRA_SERVICELOG)) {
            updateServiceLog(intent.getStringExtra(DabService.EXTRA_SERVICELOG));
          }
          if (intent.hasExtra(DabService.EXTRA_SLS)
              && (filename = intent.getStringExtra(DabService.EXTRA_SLS)) != null) {
            C0162a.m5a("* sls : ", filename);
            updateSls(filename);
          }
          if (intent.hasExtra(DabService.EXTRA_ID)) {
            this.mCurrStation = intent.getIntExtra(DabService.EXTRA_ID, 0);
            if (this.mCurrStation > 0) {
              C0162a.m8a("* id : ", this.mCurrStation);
            }
            updateStationName();
          }
          if (intent.hasExtra(DabService.EXTRA_NUMSTATIONS)) {
            this.mTotalStations = intent.getIntExtra(DabService.EXTRA_NUMSTATIONS, 0);
            C0162a.m8a("* num_stations : ", this.mTotalStations);
            updateStationName();
          }
          if (intent.hasExtra(DabService.EXTRA_AUDIOFORMAT)) {
            String str5 = intent.getStringExtra(DabService.EXTRA_AUDIOFORMAT);
            if (str5 != null && !str5.isEmpty()) {
              C0162a.m5a("* audio_format : ", str5);
            }
            updateAudioformat(str5);
          }
          if (intent.hasExtra(DabService.EXTRA_AUDIOSAMPLERATE)) {
            int samplerate = intent.getIntExtra(DabService.EXTRA_AUDIOSAMPLERATE, 0);
            if (samplerate != 0) {
              C0162a.m8a("* samplerate : ", samplerate);
            }
            updateAudioSamplerate(samplerate);
          }
        }
      }

      public boolean isPlaying() {
        return this.mIsPlaying;
      }

      private void updateStationName() {
        int[] idArray = {R.id.details_station_name, 2131427406};
        for (int id : idArray) {
          TextView textView = (TextView) Player.this.findViewById(id);
          if (textView != null) {
            if (id == R.id.details_station_name
                && this.mTotalStations != 0
                && this.mCurrStation == 0) {
              String details = this.mStationName + " /" + this.mTotalStations;
              textView.setText(details);
            } else if (id == 2131427406
                || (id == R.id.details_station_name
                    && (this.mTotalStations == 0 || this.mCurrStation == 0))) {
              textView.setText(this.mStationName);
            } else if (id == R.id.details_station_name
                && this.mTotalStations != 0
                && this.mCurrStation != 0) {
              String details2 =
                  this.mStationName + " (" + this.mCurrStation + "/" + this.mTotalStations + ")";
              textView.setText(details2);
            }
          }
        }
      }

      private void updateServiceId(int sid) {
        TextView textView = (TextView) Player.this.findViewById(R.id.details_service_id);
        if (textView != null) {
          String text = Integer.toHexString(sid).toUpperCase();
          if (sid == 0) {
            text = "";
          }
          textView.setText(text);
        }
      }

      private void updateFrequency(int freq) {
        TextView textView = (TextView) Player.this.findViewById(R.id.details_frequency);
        if (textView != null) {
          if (freq != 0) {
            float fFreq = freq / 1000.0f;
            String text =
                Strings.freq2channelname(freq) + String.format(" - %,.3f MHz", Float.valueOf(fFreq));
            textView.setText(text);
            return;
          }
          textView.setText("");
        }
      }

      private void updatePty(String pty) {
        TextView textView = (TextView) Player.this.findViewById(R.id.details_pty);
        if (textView != null) {
          textView.setText(pty);
        }
      }

      private void updateBitrate(int bitrate) {
        TextView textView = (TextView) Player.this.findViewById(R.id.details_bitrate);
        if (textView != null) {
          textView.setText(bitrate != 0 ? String.format("%d kbits/s", Integer.valueOf(bitrate)) : "");
        }
      }

      private void updateDls(String dlsText) {}

      private void updateEnsemble(String name, int id) {
        TextView textView = (TextView) Player.this.findViewById(R.id.details_ensemble);
        if (textView != null) {
          if (id != 0) {
            name = String.format("%s (%04X)", name, Integer.valueOf(id));
          }
          textView.setText(name);
        }
      }

      private void updateSignalQuality(int qual) {
        TextView textView = (TextView) Player.this.findViewById(R.id.details_signalquality);
        if (textView != null) {
          if (qual < 0) {
            textView.setText("");
          } else {
            textView.setText(String.format("%d", Integer.valueOf(qual)));
          }
        }
      }

      private void updateServiceFollowing(@NonNull String info) {
        TextView textView = (TextView) Player.this.findViewById(R.id.details_servicefollowing);
        if (textView != null) {
          textView.setText(info);
        }
      }

      private void updateServiceLog(@NonNull String info) {
        TextView textView = (TextView) Player.this.findViewById(R.id.details_servicelog);
        if (textView != null) {
          textView.setText(info);
          textView.setSelected(true);
        }
      }

      private void updateSls(String motFilename) {}

      private void updateAudioformat(String audioformat) {
        TextView textView = (TextView) Player.this.findViewById(R.id.details_audiocodec);
        if (textView != null && audioformat != null) {
          textView.setText(audioformat);
        }
      }

      private void updateAudioSamplerate(int sampleRate) {
        TextView textView = (TextView) Player.this.findViewById(R.id.details_audiobitrate);
        if (textView != null) {
          if (sampleRate > 0) {
            if (sampleRate % 1000 == 0) {
              textView.setText(String.format("%d kHz", Integer.valueOf(sampleRate / 1000)));
              return;
            } else {
              textView.setText(String.format("%,.1f kHz", Float.valueOf(sampleRate / 1000.0f)));
              return;
            }
          }
          textView.setText("");
        }
      }
    }
  */
  /* renamed from: com.ex.dabplayer.pad.activity.Player$TouchDelegateRunnable */
  /* loaded from: classes.dex */
  public class TouchDelegateRunnable implements Runnable {
    final View mDelegateView;
    final View mParentView;

    TouchDelegateRunnable(View delegateView, View parentView) {
      this.mDelegateView = delegateView;
      this.mParentView = parentView;
    }

    @Override // java.lang.Runnable
    public void run() {
      Rect delegateArea = new Rect();
      this.mDelegateView.getHitRect(delegateArea);
      delegateArea.top = 0;
      delegateArea.left = 0;
      delegateArea.right = this.mParentView.getWidth();
      delegateArea.bottom = this.mParentView.getHeight();
      TouchDelegate touchDelegate = new TouchDelegate(delegateArea, this.mDelegateView);
      if (View.class.isInstance(this.mParentView)) {
        this.mParentView.setTouchDelegate(touchDelegate);
      }
    }
  }

  /* renamed from: com.ex.dabplayer.pad.activity.Player$VTOLayoutListener */
  /* loaded from: classes.dex */

  public class VTOLayoutListener implements ViewTreeObserver.OnGlobalLayoutListener {
    private final LinearLayout mLeftBackgroundBox;
    private final WeakReference<Player> mPlayer;

    public VTOLayoutListener(Player player, LinearLayout leftBackgroundBox) {
      this.mPlayer = new WeakReference<>(player);
      this.mLeftBackgroundBox = leftBackgroundBox;
    }

    @Override // android.view.ViewTreeObserver.OnGlobalLayoutListener
    public void onGlobalLayout() {
      Player player = this.mPlayer.get();
      if (player != null) {
        this.mLeftBackgroundBox.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        /*
                MotImage motImage = player.getMotImage();
        if (motImage != null) {
          motImage.setMaxDimensions(
              this.mLeftBackgroundBox.getMeasuredWidth(),
              this.mLeftBackgroundBox.getMeasuredHeight());
        }
                */
      }
    }
  }

  /* JADX INFO: Access modifiers changed from: private */
  /* renamed from: a */
  public void updateStationList() {
    List<String> arrayList = new ArrayList<>();
    List<StationItem> arrayList2 = new ArrayList<>();
    for (int i = 0; i < this.stationList.size(); i++) {
      DabSubChannelInfo info = this.stationList.get(i);
      StationItem item = new StationItem();
      item.Index = i + 1;
      item.ItemTitle = info.mLabel;
      item.ItemInfos = Strings.freq2channelname(info.mFreq) + " - " + info.mEnsembleLabel;
      item.ItemFavorite = info.mFavorite;
      String logoFilename = null;
      String normalizedStationName = null;
      if (this.mShowLogosInList) {
        logoFilename = mLogoDb.getLogoFilenameForStation(info.mLabel, info.mSID);
        normalizedStationName = StationLogo.getNormalizedStationName(info.mLabel);
      }
      item.ItemLogo = logoFilename;
      arrayList2.add(item);
      String c = PTYname(info.mPty);
      C0162a.m9a(
          "------:'"
              + info.mLabel
              + "',pty:"
              + c
              + ",fav:"
              + info.mFavorite
              + ",logo:'"
              + logoFilename
              + "',norm:'"
              + normalizedStationName
              + "'");
      if (!arrayList.contains(c)) {
        arrayList.add(c);
      }
    }
    /*
        StationBaseAdapter stationAdapter =
            new StationBaseAdapter(
                this.context,
                arrayList2,
                this.mShowAdditionalInfos,
                this.mTouchListener,
                this.mShowLogosInList);
        stationAdapter.setSelectedIndex(this.playIndex);
        this.mStationListView.setAdapter((ListAdapter) stationAdapter);
        this.mStationListView.setTranscriptMode(0);
        this.mStationListView.setChoiceMode(1);
    */
    this.stationsAdapter =
        new SwitchStationsAdapter(this.context, switchStationsAdapterListener, arrayList2, false);
    this.recyclerView.setAdapter(stationsAdapter);
    scrollToPositionRecycler(this.playIndex);

    if (arrPty == null && arrayList.size() > 0) {
      arrPty = new String[arrayList.size() + 1];
      for (int i2 = 0; i2 < arrayList.size(); i2++) {
        arrPty[i2 + 1] = arrayList.get(i2);
      }
      arrPty[0] = PTYname(32); // ALL
      ArrayAdapter lVar =
          new ArrayAdapter<String>(
              this.context,
              R.layout.spinner_list_item,
              arrPty) { // from class: com.ex.dabplayer.pad.activity.Player.2
            @Override // android.widget.ArrayAdapter, android.widget.BaseAdapter,
            // android.widget.SpinnerAdapter
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
              View inflate =
                  Player.this
                      .getLayoutInflater()
                      .inflate(R.layout.spinner_drop_down, parent, false);
              ((TextView) inflate.findViewById(R.id.label)).setText(getItem(position));
              return inflate;
            }
          };
      lVar.notifyDataSetInvalidated();
      // this.f42v.setAdapter((SpinnerAdapter) lVar);
    }
  }

  /* JADX INFO: Access modifiers changed from: private */
  /* renamed from: a */
  public void showServiceFollowing(boolean z, String str, int i) {
    if (!z) {
      if (this.toast_service_following != null) {
        this.toast_service_following.cancel();
      }
      if (str != null && !str.isEmpty()) {
        // this.f38q.setText(str);
        return;
      }
      return;
    }
    String channel = "";
    if (str != null && str.length() > 0) {
      try {
        int frequency = Integer.parseInt(str);
        channel = Strings.freq2channelname(frequency);
      } catch (NumberFormatException e) {
      }
    }
    String msg =
        getResources().getString(R.string.ServiceFollowing)
            + " "
            + getResources().getString(R.string.scanningz)
            + " "
            + channel;
    if (this.toast_service_following == null) {
      this.toast_service_following = Toast.makeText(getApplicationContext(), msg, 1);
      this.toast_service_following.setGravity(
          17, -(getResources().getDisplayMetrics().widthPixels / 4), 0);
    } else {
      this.toast_service_following.setText(msg);
    }
    if (this.isInForeground) {
      this.toast_service_following.show();
    } else {
      this.toast_service_following.cancel();
    }
  }

  /* JADX INFO: Access modifiers changed from: private */
  /* renamed from: a */
  public void playStation(int i) {
    if (i >= 0) {
      if (this.dabHandler == null) {
        this.dabHandler = this.dabService.getDabHandlerFromDabThread();
      }
      // LogoDb logoDb = LogoDbHelper.getInstance(this.context);
      if (s_stationListShadow == null) {
        s_stationListShadow = new ArrayList<>();
      } else {
        s_stationListShadow.clear();
      }
      if (this.stationList == null) {
        C0162a.m9a("station list is null");
      } else {
        s_stationListShadow.addAll(this.stationList);
        C0162a.m9a("a(I): station list: " + s_stationListShadow.size());
        // maximizeLeftArea(false, true);

        if (i < this.stationList.size()) {
          DabSubChannelInfo subChannelInfo = this.stationList.get(i);
          updateSelectedStatus(i);
          this.txtServiceName.setText(subChannelInfo.mLabel);
          // this.f38q.setText("" + subChannelInfo.mFreq);
          // this.f37p.setText(PTYname(subChannelInfo.mPty));
          this.f26e = true;

          /*
          boolean showLogoAsMot =
              this.context
                  .getSharedPreferences(SettingsActivity.prefname_settings, 0)
                  .getBoolean(SettingsActivity.pref_key_logo_as_mot, true);
          BitmapDrawable logoDrawable = null;
          if (showLogoAsMot) {
            String pathToLogo =
                mLogoDb.getLogoFilenameForStation(subChannelInfo.mLabel, subChannelInfo.mSID);
            if (pathToLogo != null) {
              logoDrawable = mLogoDb.getBitmapForStation(this, pathToLogo);
            }
            if (logoDrawable == null) {
              logoDrawable = LogoAssets.getBitmapForStation(this.context, subChannelInfo.mLabel);
            }
          }
          */
          // if (logoDrawable != null) {
          //  this.motImage.setImage(logoDrawable, 1);
          // } else {
          //  this.motImage.setDefaultImage();
          // }
          this.txtDls.setText("");
          this.dabHandler.removeMessages(DabThread.MSGTYPE_START_PLAY_STATION);
          Message obtainMessage = this.dabHandler.obtainMessage();
          obtainMessage.what = DabThread.MSGTYPE_START_PLAY_STATION; // 6;
          obtainMessage.arg1 = i;
          this.dabHandler.sendMessage(obtainMessage);

          this.playIndex = i;
          C0162a.m9a("dab play index:" + this.playIndex);
          // DeterminedScrollTo(this.mStationListView, this.playIndex);
          scrollToPositionRecycler(this.playIndex);

          // SharedPreferences.Editor edit = this.f20I.edit();
          // edit.putInt("current_playing", this.playIndex);
          // edit.apply();
          SharedPreferencesHelper.getInstance().setInteger("current_playing", this.playIndex);
          m78f();
          displayPrevCurrNextStation(this.playIndex);
          notifyStationChangesTo(subChannelInfo, this.playIndex, this.stationList.size());
        }
      }
    }
  }

  private void scrollToPositionRecycler(int idx) {
    Toast.makeText(context, "scroll to pos " + idx, Toast.LENGTH_LONG).show();
    this.linearLayoutManager.scrollToPosition(idx);
    if (this.stationsAdapter != null) {
      this.stationsAdapter.setMot(null, -1);
    }
    this.motImage = null;
  }

  /* JADX INFO: Access modifiers changed from: private */
  /* renamed from: a */
  public void fillStationRecycler(List list) {
    if (this.stationList == null) {
      this.stationList = new ArrayList<>();
    } else {
      this.stationList.clear();
    }
    if (list != null) {
      this.stationList.addAll(list);
    }
    if (s_stationListShadow == null) {
      s_stationListShadow = new ArrayList<>();
    } else {
      s_stationListShadow.clear();
    }
    if (list != null) {
      s_stationListShadow.addAll(list);
    }
    C0162a.m9a("a(list): station list : " + s_stationListShadow.size());
    // maximizeLeftArea(false, true);
    this.stationListSize = this.stationList.size();
    if (this.stationListSize != 0) {
      String str = (String) this.txtServiceName.getText();
      List<StationItem> arrayList2 = new ArrayList<>();
      int i = 0;
      int i2 = -1;
      // LogoDb logoDb = LogoDbHelper.getInstance(this.context);
      while (i < this.stationList.size()) {
        DabSubChannelInfo info = this.stationList.get(i);
        StationItem item = new StationItem();
        item.Index = i + 1;
        item.ItemTitle = info.mLabel;
        item.ItemInfos = Strings.freq2channelname(info.mFreq) + " - " + info.mEnsembleLabel;
        item.ItemFavorite = info.mFavorite;
        if (this.mShowLogosInList) {
          item.ItemLogo = mLogoDb.getLogoFilenameForStation(info.mLabel, info.mSID);
        } else {
          item.ItemLogo = null;
        }
        arrayList2.add(item);
        int i3 = str.equals(info.mLabel) ? i : i2;
        i++;
        i2 = i3;
      }

      /*
      StationBaseAdapter aVar =
          new StationBaseAdapter(
              this.context,
              arrayList2,
              this.mShowAdditionalInfos,
              this.mTouchListener,
              this.mShowLogosInList);
      if (i2 > -1) {
        aVar.setSelectedIndex(i2);
      } else {
        playStation(0);
      }
      this.mStationListView.setAdapter((ListAdapter) aVar);
      */

      stationsAdapter =
          new SwitchStationsAdapter(this.context, switchStationsAdapterListener, arrayList2, false);
      this.recyclerView.setAdapter(stationsAdapter);
    }
  }

  /* renamed from: b */
  private void playPreset(int i) {
    ChannelInfo qVar = new ChannelInfo();
    C0162a.m9a("----dabPresetPlay:" + i);
    if (this.dabHandler == null) {
      this.dabHandler = this.dabService.getDabHandlerFromDabThread();
    }
    if (this.channelInfoList != null) {
      this.stationListSize = this.channelInfoList.size();
      if (i < this.stationListSize) {
        int i2 = 0;
        Iterator<ChannelInfo> it = this.channelInfoList.iterator();
        while (true) {
          if (!it.hasNext()) {
            break;
          }
          ChannelInfo qVar2 = it.next();
          if (i2 == i) {
            qVar.freq = qVar2.freq;
            qVar.subChannelId = qVar2.subChannelId;
            qVar.label = qVar2.label;
            qVar.bitrate = qVar2.bitrate;
            qVar.type = qVar2.type;
            break;
          }
          i2++;
        }
        this.txtServiceName.setText(qVar.label);
        this.f27f = true;
        this.playIndex = i;
        this.f26e = true;
        // this.motImage.setImageResource(R.drawable.radio);
        this.txtDls.setText("");
        this.dabHandler.removeMessages(6);
        Message obtainMessage = this.dabHandler.obtainMessage();
        obtainMessage.what = 12;
        obtainMessage.obj = qVar;
        this.dabHandler.sendMessage(obtainMessage);
        C0162a.m9a("dab play preset index:" + this.playIndex);
        // SharedPreferences.Editor edit = this.f20I.edit();
        // edit.putInt("current_playing", this.playIndex);
        // edit.apply();

        SharedPreferencesHelper.getInstance().setInteger("current_playing", this.playIndex);

        m78f();
      }
    }
  }

  /* JADX INFO: Access modifiers changed from: private */
  public void selectNextStation() {
    if (!this.progressDialog.isShowing() && this.stationListSize > 0) {
      int i = ((this.playIndex + this.stationListSize) + 1) % this.stationListSize;
      // if (this.f27f) {
      //  playPreset(i);
      // } else {
      playStation(i);
      // }
    }
  }

  /* JADX INFO: Access modifiers changed from: private */
  /* renamed from: b */
  public void showMotImage(String str) {
    if (str.isEmpty()) {
      // this.motImage.setDefaultImage();
      this.stationsAdapter.setMot(null, -1);
      this.motImage = null;
      return;
    }
    SharedPreferences pref_settings =
        this.context.getSharedPreferences(SettingsActivity.prefname_settings, 0);
    // if (pref_settings.getBoolean(SettingsActivity.pref_key_motSlideshowEnabled, true)) {
    File file = new File(this.context.getFilesDir(), str);
    if (file.exists()) {
      Bitmap decodeFile = BitmapFactory.decodeFile(file.getAbsolutePath());
      if (decodeFile != null) {
        // this.motImage.setImage(new BitmapDrawable(getResources(), decodeFile), 2);
        Toast.makeText(context, "set mot to pos " + this.playIndex, Toast.LENGTH_LONG).show();
        this.motImage = new BitmapDrawable(getResources(), decodeFile);
        this.stationsAdapter.setMot(this.motImage, playIndex);
        return;
      }
      return;
    }
    C0162a.m9a("file '" + file.getAbsolutePath() + "' not found");
    // }
  }

  /* JADX INFO: Access modifiers changed from: private */
  public void onStationChange_nextWrapper() {
    SharedPreferences prefSettings =
        this.context.getSharedPreferences(SettingsActivity.prefname_settings, 0);
    boolean swapPrevNext = prefSettings.getBoolean(SettingsActivity.pref_key_swapPrevNext, false);
    if (this.keyDownHandler != null) {
      this.keyDownHandler.removeMessages(1);
      Message msg = this.keyDownHandler.obtainMessage();
      msg.what = DelayedRunnableHandler.MSG_DELAYED_RUN; // 1
      if (!swapPrevNext) {
        msg.obj = new RunnableBWrapperNext();
      } else {
        C0162a.m9a("NEXT->PREV");
        msg.obj = new RunnableCWrapperPrev();
      }
      this.keyDownHandler.sendMessageDelayed(msg, getKeyDownDebounceTimeMs());
      return;
    }
    selectNextStation();
  }

  /* JADX INFO: Access modifiers changed from: private */
  public void selectPreviousStation() {
    if (!this.progressDialog.isShowing() && this.stationListSize > 0) {
      int i = ((this.playIndex + this.stationListSize) - 1) % this.stationListSize;
      if (this.f27f) {
        playPreset(i);
      } else {
        playStation(i);
      }
    }
  }

  /* JADX INFO: Access modifiers changed from: private */
  public void onStationChange_prevWrapper() {
    SharedPreferences prefSettings =
        this.context.getSharedPreferences(SettingsActivity.prefname_settings, 0);
    boolean swapPrevNext = prefSettings.getBoolean(SettingsActivity.pref_key_swapPrevNext, false);
    if (this.keyDownHandler != null) {
      this.keyDownHandler.removeMessages(1);
      Message msg = this.keyDownHandler.obtainMessage();
      msg.what = 1;
      if (!swapPrevNext) {
        msg.obj = new RunnableCWrapperPrev();
      } else {
        C0162a.m9a("PREV->NEXT");
        msg.obj = new RunnableBWrapperNext();
      }
      this.keyDownHandler.sendMessageDelayed(msg, getKeyDownDebounceTimeMs());
      return;
    }
    selectPreviousStation();
  }

  public void deleteStationAtPosition(int pos) {
    DabSubChannelInfo subChannelInfo;
    if (pos >= 0
        && pos < this.stationList.size()
        && (subChannelInfo = this.stationList.get(pos)) != null
        && this.dabHandler != null) {
      Message obtainMessage = this.dabHandler.obtainMessage();
      obtainMessage.what = 33;
      obtainMessage.obj = subChannelInfo;
      this.dabHandler.sendMessage(obtainMessage);
    }
  }

  private void updateSelectedStatus(int index) {
    /*
    StationBaseAdapter stationAdapter = (StationBaseAdapter) this.mStationListView.getAdapter();
    if (stationAdapter != null) {
      stationAdapter.setSelectedIndex(index);
    }
    this.mStationListView.setItemChecked(index, true);
    */
    Toast.makeText(context, "updateSelectedStatus is not implements", Toast.LENGTH_LONG).show();
    scrollToPositionRecycler(index);
  }

  private void DeterminedScrollTo(AbsListView listView, int index) {
    if (listView != null && index >= 0) {
      int firstPos = listView.getFirstVisiblePosition();
      int lastPos = listView.getLastVisiblePosition();
      int target = index;
      if (firstPos >= 0 && index <= firstPos) {
        if (index > 0) {
          target = index - 1;
        }
      } else if (lastPos > 0 && index >= lastPos) {
        target = index + 1;
      }
      C0162a.m9a(
          "scroll to "
              + index
              + ": firstVis="
              + firstPos
              + " lastVis="
              + lastPos
              + " target="
              + target);
      if (index > firstPos) {
        // listView.smoothScrollToPosition(target);
        scrollToPositionRecycler(target);

      } else {
        // listView.smoothScrollToPositionFromTop(target, 0);
        scrollToPositionRecycler(target);
      }
    }
  }

  private void QuickScrollTo(AbsListView listView, int index) {
    if (listView != null && index >= 0) {
      listView.setSelection(index);
    }
  }

  private int FirstPosition(AbsListView listView) {
    if (listView == null) {
      return -1;
    }
    return listView.getFirstVisiblePosition();
  }

  public void displayPrevCurrNextStation(int currIndex) {
    DabSubChannelInfo currChannnel;
    if (currIndex >= 0 && currIndex < this.stationList.size()) {
      int prevIndex = currIndex > 0 ? currIndex - 1 : this.stationList.size() - 1;
      DabSubChannelInfo prevChannel = this.stationList.get(prevIndex);
      if (prevChannel != null) {
        int nextIndex = currIndex < this.stationList.size() + (-2) ? currIndex + 1 : 0;
        DabSubChannelInfo nextChannel = this.stationList.get(nextIndex);
        if (nextChannel != null
            && (currChannnel = this.stationList.get(currIndex)) != null
            && !this.isInForeground) {
          String text = currChannnel.mLabel;
          if (this.mChannelToast != null) {
            this.mChannelToast.cancel();
          }
          this.mChannelToast = Toast.makeText(this.context, text, 1);
          this.mChannelToast.show();
        }
      }
    }
  }

  /* JADX INFO: Access modifiers changed from: private */
  /* renamed from: e */
  public void m79e() {
    this.txtServiceName.setText("");
    // this.f37p.setText("");
    // this.f38q.setText("");
    this.f26e = false;
    if (this.dabHandler != null) {
      this.dabHandler.removeMessages(7);
      Message obtainMessage = this.dabHandler.obtainMessage();
      obtainMessage.what = DabThread.MSGTYPE_DAB_HANDLER_STOP; // 7;
      this.dabHandler.sendMessage(obtainMessage);
    }
  }

  /* JADX INFO: Access modifiers changed from: private */
  /* renamed from: f */
  public void m78f() {
    ChannelInfo channelInfo = new ChannelInfo();
    if (this.channelInfoList != null
        && this.channelInfoList.size() != 0
        && this.stationList != null
        && this.stationList.size() != 0
        && this.playIndex >= 0
        && this.playIndex < this.stationList.size()) {
      channelInfo.label = this.stationList.get(this.playIndex).mLabel;
      channelInfo.freq = this.stationList.get(this.playIndex).mFreq;
      channelInfo.subChannelId = this.stationList.get(this.playIndex).mSubChannelId;
      channelInfo.bitrate = this.stationList.get(this.playIndex).mBitrate;
      for (ChannelInfo qVar2 : this.channelInfoList) {
        if (qVar2.freq == channelInfo.freq
            && qVar2.subChannelId == channelInfo.subChannelId
            && qVar2.label.equals(channelInfo.label)) {
          return;
        }
      }
    }
  }

  /* JADX INFO: Access modifiers changed from: private */
  public void finishTheApp() {
    C0162a.m9a("finishTheApp");
    m79e();
    if (this.dabHandler != null) {
      this.dabHandler.removeMessages(5);
      Message obtainMessage = this.dabHandler.obtainMessage();
      obtainMessage.what = DabThread.MSGTYPE_DAB_DEINIT; // 5;
      obtainMessage.arg1 = 0;
      this.dabHandler.sendMessage(obtainMessage);
    }
    boolean isDestroyed = false;
    if (Build.VERSION.SDK_INT >= 17) {
      isDestroyed = isDestroyed();
    }
    if (!isDestroyed) {
      C0162a.m9a("finish()");
      finishAffinity();
      // finish();
    }
    this.mProperShutdown = true;
  }

  public void flipViews(boolean leftToRight) {
    float visToInvisStartDeg;
    float visToInvisStopDeg;
    float invisToVisStartDeg;
    float invisToVisStopDeg;
    int flipToViewIdx;
    int currentViewIdx = this.mViewFlipper.getDisplayedChild();
    if (leftToRight) {
      visToInvisStartDeg = 0.0f;
      visToInvisStopDeg = 90.0f;
      invisToVisStartDeg = -90.0f;
      invisToVisStopDeg = 0.0f;
      flipToViewIdx =
          ((this.mViewFlipper.getChildCount() + currentViewIdx) - 1)
              % this.mViewFlipper.getChildCount();
    } else {
      visToInvisStartDeg = 0.0f;
      visToInvisStopDeg = -90.0f;
      invisToVisStartDeg = 90.0f;
      invisToVisStopDeg = 0.0f;
      flipToViewIdx = (currentViewIdx + 1) % this.mViewFlipper.getChildCount();
    }
    View currentVis = this.mViewFlipper.getChildAt(currentViewIdx);
    View currentInvis = this.mViewFlipper.getChildAt(flipToViewIdx);
    ObjectAnimator visToInvis =
        ObjectAnimator.ofFloat(currentVis, "rotationY", visToInvisStartDeg, visToInvisStopDeg);
    visToInvis.setDuration(250L).setInterpolator(new AccelerateInterpolator());
    ObjectAnimator invisToVis =
        ObjectAnimator.ofFloat(currentInvis, "rotationY", invisToVisStartDeg, invisToVisStopDeg);
    invisToVis.setDuration(250L).setInterpolator(new DecelerateInterpolator());
    visToInvis.addListener(new FlipViewAnimatorListener(invisToVis, flipToViewIdx));
    visToInvis.start();
  }

  /* renamed from: g */
  private void setUsbDeviceFromDeviceList() {
    this.usbManager = (UsbManager) this.context.getSystemService("usb");
    for (UsbDevice usbDevice : this.usbManager.getDeviceList().values()) {
      if (usbDevice.getVendorId() == 5824 && usbDevice.getProductId() == 1500) {
        if (this.usbManager.hasPermission(usbDevice)) {
          this.usbDevice = usbDevice;
          return;
        }
        return;
      }
    }
  }

  public final Context getContext() {
    return this.context;
  }

  public final DabService getDabService() {
    return this.dabService;
  }

  public final long getKeyDownDebounceTimeMs() {
    return 150L;
  }

  public static WeakReference<Intent> getMainActivityStartIntentWeakRef() {
    return sMainActivityStartIntent;
  }

  public BitmapDrawable getMotLogoOrIcon() {

    if (this.motImage != null) {
      return this.motImage;
    } else {

      if (this.stationList != null && this.stationList.size() > this.playIndex) {
        String label = this.stationList.get(this.playIndex).mLabel;
        // LogoDb logoDb = LogoDbHelper.getInstance(this.context);
        int sid = this.stationList.get(this.playIndex).mSID;

        String logoFilename = mLogoDb.getLogoFilenameForStation(label, sid);
        BitmapDrawable logoDrawable = null;

        if (logoFilename != null) {
          logoDrawable = mLogoDb.getBitmapForStation(this.context, logoFilename);
        }
        if (logoFilename == null) {
          logoDrawable = LogoAssets.getBitmapForStation(this.context, label);
        }
        if (logoDrawable != null) {
          return logoDrawable;
        }
      }
      return (BitmapDrawable) context.getDrawable(R.drawable.radio);
    }
  }

  public static WeakReference<Handler> getPlayerHandler() {
    return sPlayerHandler;
  }

  public static WeakReference<ArrayList<DabSubChannelInfo>> getStationListShadow() {
    if (s_stationListShadow == null) {
      return null;
    }
    WeakReference<ArrayList<DabSubChannelInfo>> retVal = new WeakReference<>(s_stationListShadow);
    return retVal;
  }

  public boolean isFavoriteListActive() {
    return this.isFavoriteListActive;
  }

  /*
    public final boolean isLeftAreaMaximized() {
      return this.mIsLeftAreaMaximized;
    }
  */
  /*
  public void maximizeLeftArea(boolean maximize, boolean animate) {
    float start;
    float stop;
    C0162a.m9a("maximize=" + maximize + " animate=" + animate);
    if (this.mIsLeftAreaMaximized == maximize) {
      C0162a.m9a("maximize no state change");
    } else {
      this.mIsLeftAreaMaximized = maximize;
      View leftArea = findViewById(R.id.left_area);
      if (leftArea != null) {
        if (animate) {
          if (maximize) {
            start = this.mDefaultLeftAreaLayoutWeight;
            stop = 0.0f;
          } else {
            start = 0.0f;
            stop = this.mDefaultLeftAreaLayoutWeight;
          }
          ValueAnimator animator = ValueAnimator.ofFloat(start, stop);
          if (maximize) {
            animator.setDuration(1000L);
            animator.setInterpolator(new AccelerateInterpolator());
          } else {
            animator.setDuration(200L);
            animator.setInterpolator(new DecelerateInterpolator());
          }
          MaximizeListener listener = new MaximizeListener(leftArea);
          animator.addListener(listener);
          animator.addUpdateListener(listener);
          animator.start();
        } else {
          LinearLayout.LayoutParams layoutParams =
              (LinearLayout.LayoutParams) leftArea.getLayoutParams();
          if (maximize) {
            layoutParams.weight = 0.0f;
          } else {
            layoutParams.weight = this.mDefaultLeftAreaLayoutWeight;
          }
          leftArea.setLayoutParams(layoutParams);
          leftArea.getParent().requestLayout();
        }
      }
    }
    SharedPreferences settingsPreferences =
        getSharedPreferences(SettingsActivity.prefname_settings, 0);
    if (!maximize
        && settingsPreferences.getBoolean(SettingsActivity.pref_key_auto_maximize, false)) {
      long delayMs =
          settingsPreferences.getLong(
              SettingsActivity.pref_key_auto_maximize_timeout,
              SettingsActivity.pref_defvalue_auto_maximize_timeout_msec.longValue());
      C0162a.m9a("maximize again in " + delayMs + " ms");
      this.maximizeLeftAreaHandler.removeMessages(1);
      Message msg = this.maximizeLeftAreaHandler.obtainMessage();
      msg.what = 1;
      msg.obj = new RunnableMaximizeLeftArea(true, true);
      this.maximizeLeftAreaHandler.sendMessageDelayed(msg, delayMs);
    }
  }
  */

  public void notifyAudioDistortion() {
    if (this.mAudioDistortionToast != null) {
      this.mAudioDistortionToast.cancel();
    }
    this.mAudioDistortionToast =
        Toast.makeText(this.context, (int) R.string.audio_distortion_notification, 0);
    this.mAudioDistortionToast.show();
  }

  private void notifyStationChangesTo(DabSubChannelInfo info, int newPos, int numStations) {
    Intent intent = new Intent(DabService.META_CHANGED);
    intent.putExtra(DabService.EXTRA_SENDER, DabService.SENDER_DAB);
    intent.putExtra(DabService.EXTRA_ID, newPos + 1);
    intent.putExtra(DabService.EXTRA_NUMSTATIONS, numStations);
    intent.putExtra(DabService.EXTRA_ARTIST, info.mLabel);
    intent.putExtra(DabService.EXTRA_TRACK, "");
    intent.putExtra(DabService.EXTRA_STATION, info.mLabel);
    intent.putExtra(DabService.EXTRA_DLS, "");
    intent.putExtra("playing", true);
    intent.putExtra(DabService.EXTRA_SERVICEID, info.mSID);
    intent.putExtra(DabService.EXTRA_FREQUENCY_KHZ, info.mFreq);
    intent.putExtra(DabService.EXTRA_PTY, PTYname(info.mPty));
    intent.putExtra(DabService.EXTRA_BITRATE, info.mBitrate);
    intent.putExtra(DabService.EXTRA_ENSEMBLE_NAME, info.mEnsembleLabel);
    intent.putExtra(DabService.EXTRA_ENSEMBLE_ID, info.mEID);
    intent.putExtra(DabService.EXTRA_SIGNALQUALITY, -1);
    intent.putExtra(DabService.EXTRA_SLS, "");
    switch (info.mType) {
      case 0:
      case 1:
      case 2:
        intent.putExtra(DabService.EXTRA_AUDIOFORMAT, DabService.AUDIOFORMAT_MP2);
        break;
      case DabSubChannelInfo.AUDIOCODEC_HEAAC /* 63 */:
        intent.putExtra(DabService.EXTRA_AUDIOFORMAT, DabService.AUDIOFORMAT_AAC);
        break;
    }
    intent.putExtra(DabService.EXTRA_AUDIOSAMPLERATE, 0);
    notifyStationInfo(intent, true);
  }

  private void notifyPlayerStopped() {
    Intent intent = new Intent(DabService.META_CHANGED);
    intent.putExtra(DabService.EXTRA_SENDER, DabService.SENDER_DAB);
    intent.putExtra(DabService.EXTRA_ID, 0);
    intent.putExtra(DabService.EXTRA_NUMSTATIONS, 0);
    intent.putExtra(DabService.EXTRA_ARTIST, "");
    intent.putExtra(DabService.EXTRA_TRACK, "");
    intent.putExtra(DabService.EXTRA_STATION, "");
    intent.putExtra(DabService.EXTRA_DLS, "");
    intent.putExtra("playing", false);
    intent.putExtra(DabService.EXTRA_SERVICEID, 0);
    intent.putExtra(DabService.EXTRA_FREQUENCY_KHZ, 0);
    intent.putExtra(DabService.EXTRA_PTY, "");
    intent.putExtra(DabService.EXTRA_BITRATE, 0);
    intent.putExtra(DabService.EXTRA_ENSEMBLE_NAME, "");
    intent.putExtra(DabService.EXTRA_ENSEMBLE_ID, 0);
    intent.putExtra(DabService.EXTRA_SIGNALQUALITY, -1);
    intent.putExtra(DabService.EXTRA_SLS, "");
    intent.putExtra(DabService.EXTRA_AUDIOFORMAT, "");
    intent.putExtra(DabService.EXTRA_AUDIOSAMPLERATE, 0);
    notifyStationInfo(intent, true);
  }

  /* JADX INFO: Access modifiers changed from: private */

  public void notifyStationInfo(Intent intent, boolean affectsAndroidMetaData) {
    String sender;
    DabService dabService;
    MediaSessionCompat mediaSession;
    Bitmap motImage = getMotLogoOrIcon().getBitmap();
    intent.putExtra(DabService.EXTRA_SLSBITMAP, motImage);
    // here we update the station details
    // this.mStationDetails.updateAllDetailsViewFromIntent(intent);

    viewPagerAdapter.setDetails(intent);

    if (this.mSendBroadcastIntent
        && affectsAndroidMetaData
        && (dabService = getDabService()) != null
        && (mediaSession = dabService.getMediaSession()) != null) {
      MediaMetadataCompat.Builder metaDataBuilder = new MediaMetadataCompat.Builder(this.mMetaData);
      if (intent.hasExtra(DabService.EXTRA_ENSEMBLE_NAME)) {
        metaDataBuilder.putString(
            MediaMetadataCompat.METADATA_KEY_ALBUM,
            intent.getStringExtra(DabService.EXTRA_ENSEMBLE_NAME));
      }
      if (intent.hasExtra(DabService.EXTRA_STATION)) {
        metaDataBuilder.putString(
            MediaMetadataCompat.METADATA_KEY_ARTIST,
            intent.getStringExtra(DabService.EXTRA_STATION));
      }
      if (intent.hasExtra(DabService.EXTRA_DLS)) {
        metaDataBuilder.putString(
            MediaMetadataCompat.METADATA_KEY_TITLE, intent.getStringExtra(DabService.EXTRA_DLS));
      }
      if (intent.hasExtra(DabService.EXTRA_ID)) {
        metaDataBuilder.putLong(
            MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER,
            intent.getIntExtra(DabService.EXTRA_ID, 0));
      }
      if (intent.hasExtra(DabService.EXTRA_NUMSTATIONS)) {
        metaDataBuilder.putLong(
            MediaMetadataCompat.METADATA_KEY_NUM_TRACKS,
            intent.getIntExtra(DabService.EXTRA_NUMSTATIONS, 0));
      }
      if (intent.hasExtra(DabService.EXTRA_PTY)) {
        metaDataBuilder.putString(
            MediaMetadataCompat.METADATA_KEY_GENRE, intent.getStringExtra(DabService.EXTRA_PTY));
      }
      metaDataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, motImage);
      this.mMetaData = metaDataBuilder.build();
      mediaSession.setMetadata(this.mMetaData);
    }
    if (this.mSendBroadcastIntent
        && intent.hasExtra(DabService.EXTRA_SENDER)
        && (sender = intent.getStringExtra(DabService.EXTRA_SENDER)) != null
        && sender.equals(DabService.SENDER_DAB)) {
      if (!intent.hasExtra("playing")) {

        // Toast.makeText(context, "no playing info", Toast.LENGTH_LONG).show();
        // intent.putExtra("playing", this.mStationDetails.isPlaying());
      }
      if (intent.hasExtra(DabService.EXTRA_AFFECTS_ANDROID_METADATA)) {
        intent.removeExtra(DabService.EXTRA_AFFECTS_ANDROID_METADATA);
      }
      if (intent.hasExtra(DabService.EXTRA_SERVICEFOLLOWING)) {
        intent.removeExtra(DabService.EXTRA_SERVICEFOLLOWING);
      }
      if (intent.hasExtra(DabService.EXTRA_SERVICELOG)) {
        intent.removeExtra(DabService.EXTRA_SERVICELOG);
      }
      sendBroadcast(intent);
    }
    if (this.dabService != null && affectsAndroidMetaData) {
      this.dabService.updateNotification(intent);
    }
  }

  @Override // android.app.Activity
  protected void onActivityResult(int i, int i2, Intent intent) {
    super.onActivityResult(i, i2, intent);
    String stringExtra = intent.getStringExtra("title");
    if (stringExtra != null) {
      if (stringExtra.contains("Preset")) {
        playPreset(i2);
      } else if (!stringExtra.equals("pty dialog")) {
        this.stationListSize = this.stationList.size();
        if (i > 0) {
          playStation(i2);
        }
      } else if (i2 >= 0 && arrPty != null && this.dabHandler != null) {
        String stringExtra2 = arrPty[i2];
        Message obtainMessage = this.dabHandler.obtainMessage();
        obtainMessage.what = 20;
        obtainMessage.arg1 = Strings.PTYnumber(stringExtra2);
        this.dabHandler.sendMessage(obtainMessage);
      }
    }
  }

  @Override // android.view.View.OnClickListener
  public void onClick(View view) {
    C0162a.m9a("onClick 0x" + Integer.toHexString(view.getId()));
    if (view.getId() == R.id.bt_settings) {
      onSettingsButtonClicked();
    } else if (view.getId() == R.id.layExit) {
      finishTheApp();
    } else if (!this.isInitialized) {
      String text = getResources().getString(R.string.waitfewseconds);
      Toast.makeText(this.context, text, Toast.LENGTH_LONG).show();
    } else if (this.stationListSize != 0 || view.getId() == R.id.layScan) {
      if (view.getId() == R.id.layScan) {
        /* 2131427335 */
        Toast.makeText(context, "scsn", Toast.LENGTH_LONG).show();
        onScanButtonClicked();
        return;
      } else if (view.getId() == R.id.signal_level) {
        /* 2131427346 */
        if (ServiceFollowing.is_possible()) {
          C0162a.m9a("manually activated service following");
          onServiceFollowRequested();
          return;
        }
        C0162a.m9a("manually activation ignored");
        return;
        /*
        } else if (view.getId() == R.id.bt_record) {
          // 2131427347
          if (this.f27f || this.f26e) {
            Message obtainMessage = this.dabHandler.obtainMessage();
            if (this.f28g) {
              this.f28g = false;
              this.f34m.setBackgroundResource(R.drawable.record_start_selector);
              obtainMessage.what = 22;
              this.dabHandler.sendMessage(obtainMessage);
            } else {
              this.f28g = true;
              this.f34m.setBackgroundResource(R.drawable.record_stop_selector);
              obtainMessage.what = 21;
              this.dabHandler.sendMessage(obtainMessage);
            }
            C0162a.m9a("recorder:" + this.f28g);
            return;
          }
          return;
        */
      } else if (view.getId() == R.id.bt_prev) {
        /* 2131427353 */
        selectPreviousStation();
        return;
      } else if (view.getId() == R.id.bt_next) {
        /* 2131427354 */
        selectNextStation();
        return;
        /*
        } else if (view.getId() == R.id.bt_pty) {

          onPtyButtonClicked();
          return;
        } else if (view.getId() == R.id.bt_favor) {

          boolean active = isFavoriteListActive();
          if (!active && DatabaseHelper.getFavCount() < 1) {
            String text2 = getResources().getString(R.string.selectfavoritesfirst);
            Toast.makeText(this.context, text2, 0).show();
            return;
          }

          setFavoriteListActive(active ? false : true);
          boolean active2 = isFavoriteListActive();
          C0162a.m9a("favorite:" + active2);
          if (active2) {
            this.favorBtn.setBackgroundResource(R.drawable.favor_list_selector_on);
            return;
          } else {
            this.favorBtn.setBackgroundResource(R.drawable.favor_list_selector_off);
            return;
          }
          */
      } else {
        return;
      }
    } else {
      String text3 = getResources().getString(R.string.scanfirst);
      Toast.makeText(this.context, text3, 0).show();
    }
  }

  private void setMemory(int storagePos) {
    DabSubChannelInfo subChannelInfo = this.stationList.get(this.playIndex);
    subChannelInfo.mFavorite = storagePos;
    if (this.dabHandler == null) return;
    Message obtainMessage = this.dabHandler.obtainMessage();
    obtainMessage.what = DabThread.UPDATE_FAVOURITE;
    obtainMessage.obj = subChannelInfo;
    obtainMessage.arg1 = storagePos;
    this.dabHandler.sendMessage(obtainMessage);
    // this comes back with PLAYERMSG_SET_STATIONMEMORY
  }

  private void playFavourite(int storagePos) {
    Toast.makeText(
            context,
            "request DabThread to play favourite from storage pos  " + storagePos,
            Toast.LENGTH_LONG)
        .show();
    this.dabHandler.removeMessages(DabThread.PLAY_FAVOURITE);
    Message obtainMessage = this.dabHandler.obtainMessage();
    obtainMessage.what = DabThread.PLAY_FAVOURITE;
    obtainMessage.arg1 = storagePos;
    Player.this.dabHandler.sendMessage(obtainMessage);
    // this comes back with PLAYERMSG_PLAYING_STATION
  }

  /*
    @Override // android.app.Activity, android.content.ComponentCallbacks
    public void onConfigurationChanged(Configuration newConfig) {
      super.onConfigurationChanged(newConfig);
      C0162a.m9a("config change ignored");
    }
  */
  @Override // android.app.Activity
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    C0162a.m9a("Player:onCreate");
    setContentView(R.layout.activity_player);
    this.playIndex = 0;
    this.context = getApplicationContext();
    // this.motImage = (MotImage) findViewById(R.id.mot);
    this.imgSignalLevel = (ImageView) findViewById(R.id.signal_level);
    this.imgSignalLevel.setOnClickListener(this);
    this.btnNext = (Button) findViewById(R.id.bt_next);
    this.btnNext.setOnClickListener(this);
    this.btnPrev = (Button) findViewById(R.id.bt_prev);
    this.btnPrev.setOnClickListener(this);
    this.layScan = (Button) findViewById(R.id.layScan);
    this.layScan.setOnClickListener(this);
    this.layExit = (Button) findViewById(R.id.layExit);
    this.layExit.setOnClickListener(this);
    Button btnSettings2 = (Button) findViewById(R.id.bt_settings);
    btnSettings2.setOnClickListener(this);
    // this.f35n = (Button) findViewById(R.id.bt_pty);
    // this.f35n.setOnClickListener(this);

    // this.f28g = false;
    // this.f34m = (Button) findViewById(R.id.bt_record);
    // this.f34m.setOnClickListener(this);
    // this.f38q = (TextView) findViewById(R.id.service_freq);
    // this.f38q.setText("");
    this.txtServiceName = (TextView) findViewById(R.id.service_name);
    this.txtServiceName.setText("");
    // this.f37p = (TextView) findViewById(R.id.service_pty);
    // this.f37p.setText("");
    /*
    this.f42v = (Spinner) findViewById(R.id.pty_spinner);
    this.f42v.setOnItemSelectedListener(
        new AdapterView
            .OnItemSelectedListener() { // from class: com.ex.dabplayer.pad.activity.Player.3
          @Override // android.widget.AdapterView.OnItemSelectedListener
          public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (Player.arrPty != null && Player.this.dabHandler != null) {
              String str = Player.arrPty[Player.this.stationListSize];
              Message obtainMessage = Player.this.dabHandler.obtainMessage();
              obtainMessage.what = 20;
              obtainMessage.arg1 = Strings.PTYnumber(str);
              Player.this.dabHandler.sendMessage(obtainMessage);
            }
          }

          @Override // android.widget.AdapterView.OnItemSelectedListener
          public void onNothingSelected(AdapterView<?> parent) {}
        });
        */
    this.txtDls = (TextView) findViewById(R.id.dls_scroll);
    this.txtDls.setText("");
    // this.mStationListView = getListView();

    // recycler

    this.textClock = (TextClock) findViewById(R.id.textClock);
    if (!SharedPreferencesHelper.getInstance().getBoolean("showClock")) {
      this.textClock.setVisibility(View.GONE);
    }

    recyclerView = this.findViewById(R.id.recycler);
    recyclerView.setHasFixedSize(true);
    recyclerView.setItemAnimator(new DefaultItemAnimator());
    linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
    recyclerView.addOnScrollListener(
        new RecyclerView.OnScrollListener() {
          @Override
          public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
              int position =
                  ((LinearLayoutManager) recyclerView.getLayoutManager())
                      .findFirstVisibleItemPosition();
              Toast.makeText(context, position + " OnScrollListener", Toast.LENGTH_LONG).show();
              onStationClicked(position);
            }
          }
        });

    recyclerView.setLayoutManager(linearLayoutManager);
    PagerSnapHelper pagerSnapHelper = new PagerSnapHelper();
    pagerSnapHelper.attachToRecyclerView(recyclerView);

    if ("RMX3301EEA".equals(Build.PRODUCT)) {
      stationsAdapter =
          new SwitchStationsAdapter(
              this.context, switchStationsAdapterListener, new ArrayList<>(), false);
      this.recyclerView.setAdapter(stationsAdapter);
    }

    this.viewPager = this.findViewById(R.id.viewPager);
    int numberPresetPages = SharedPreferencesHelper.getInstance().getInteger("presetPages");
    this.viewPagerAdapter =
        new ViewPagerAdapter(context, this.viewPagerAdapterListener, numberPresetPages);
    this.viewPager.setAdapter(this.viewPagerAdapter);
    this.tabLayout = this.findViewById(R.id.tabLayout);
    new TabLayoutMediator(this.tabLayout, this.viewPager, (tab, position) -> {}).attach();

    this.audioManager = (AudioManager) getSystemService("audio");
    this.progressDialog = new ProgressDialog(this);
    this.progressDialog.setProgressStyle(0);
    this.progressDialog.setTitle("");
    this.progressDialog.setMessage("");
    this.progressDialog.setCancelable(false);
    this.progressDialog.setIndeterminate(false);
    this.progressDialog.setIndeterminateDrawable(
        getResources().getDrawable(R.anim.progress_dialog_anim));
    // new IntentFilter("com.microntek.app");
    // Intent intent = new Intent("com.microntek.app");
    // intent.putExtra("app", DabService.SENDER_DAB);
    // intent.putExtra("state", "ENTER");
    // this.context.sendBroadcast(intent);

    this.audioManager.requestAudioFocus(this.f24c, 3, 1);

    this.mServiceIntent = new Intent(this, DabService.class);
    startService(this.mServiceIntent);
    bindService(this.mServiceIntent, this, 1);

    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction("android.hardware.usb.action.USB_DEVICE_DETACHED");
    this.context.registerReceiver(this.f23N, intentFilter);

    this.mLogoDb = LogoDbHelper.getInstance(this);
    this.mLogoAssets = new LogoAssets(this, this.f22M);
    onCreateAdditions(savedInstanceState);
  }

  @SuppressLint({"ClickableViewAccessibility"})
  private void onCreateAdditions(Bundle savedInstanceState) {
    LinearLayout.LayoutParams params;
    // homeKeyReceiver = new HomeKeyReceiver(this);
    sPlayerHandler = new WeakReference<>(this.f22M);

    /*
    if (this.f20I == null) {
      this.f20I = this.context.getSharedPreferences("playing", 0);
    }
    int filter = this.f20I.getInt("current_filter", 0);
    this.isFavoriteListActive = filter == 2 || filter == 3;
     */
    /*
        this.favorBtn = (Button) findViewById(R.id.bt_favor);
    this.favorBtn.setOnClickListener(this);
    if (this.isFavoriteListActive) {
      this.favorBtn.setBackgroundResource(R.drawable.favor_list_selector_on);
    } else {
      this.favorBtn.setBackgroundResource(R.drawable.favor_list_selector_off);
    }
        */
    this.mTouchListener = new TouchListener(this);
    // this.mStationListView.setOnTouchListener(
    //    new StationViewTouchHelper(this.context, this.mTouchListener));

    /*LinearLayout leftBackgroundBox = (LinearLayout) findViewById(R.id.left_background_box);
    if (leftBackgroundBox != null) {
      ViewTreeObserver vto = leftBackgroundBox.getViewTreeObserver();
      vto.addOnGlobalLayoutListener(new VTOLayoutListener(this, leftBackgroundBox));
    }
    */
    Intent startedByIntent = getIntent();
    if (startedByIntent != null) {
      C0162a.m9a("Player startedByIntent=" + startedByIntent.toString());
      Intent mainWasStartedByIntent =
          (Intent) startedByIntent.getParcelableExtra("StartedByIntent");
      if (mainWasStartedByIntent != null) {
        sMainActivityStartIntent = new WeakReference<>(mainWasStartedByIntent);
        String action = mainWasStartedByIntent.getAction();
        if (action != null && action.equals("android.hardware.usb.action.USB_DEVICE_ATTACHED")) {
          SharedPreferences settingsPreferences =
              getSharedPreferences(SettingsActivity.prefname_settings, 0);
          if (settingsPreferences.getBoolean(
              SettingsActivity.pref_key_onstartbyusb_gotobackground, false)) {
            C0162a.m9a("started by USB_DEVICE_ATTACHED -> background");
            // moveTaskToBack(true);
          }
        }
      }
    }

    this.mDefaultLeftAreaLayoutWeight = 5.0f;
    /*
        LinearLayout leftArea = (LinearLayout) findViewById(R.id.left_area);
    if (leftArea != null
        && (params = (LinearLayout.LayoutParams) leftArea.getLayoutParams()) != null) {
      this.mDefaultLeftAreaLayoutWeight = params.weight;
    }

    this.mViewFlipper = (ViewFlipper) findViewById(R.id.viewflipper);
    if (this.mViewFlipper != null) {
      LargeSlsTouchListener touchListener = new LargeSlsTouchListener(this);
      for (int i = 0; i < this.mViewFlipper.getChildCount(); i++) {
        this.mViewFlipper.getChildAt(i).setClickable(true);
        this.mViewFlipper.getChildAt(i).setOnTouchListener(touchListener);
      }
    }
        */
    TextView textView =
        (TextView)
            findViewById(
                getResources()
                    .getIdentifier("service_name", DabService.EXTRA_ID, getPackageName()));
    if (textView != null) {
      this.mStationNameSizeFromStyle = textView.getTextSize();
    }
    TextView textView2 =
        (TextView)
            findViewById(
                getResources().getIdentifier("dls_scroll", DabService.EXTRA_ID, getPackageName()));
    if (textView2 != null) {
      this.mDlsSizeFromStyle = textView2.getTextSize();
    }

    /*
    String[] buttonNames = {"bt_prev", "bt_favor", "bt_next", "bt_pty", "bt_settings2"};
    int length = buttonNames.length;
    int i2 = 0;
    while (true) {
      int i3 = i2;
      if (i3 < length) {
        String buttonName = buttonNames[i3];
        Button btn =
            (Button)
                findViewById(
                    getResources()
                        .getIdentifier(buttonName, DabService.EXTRA_ID, getPackageName()));
        if (btn != null) {
          ViewParent parent = btn.getParent();
          if (parent == null) {
            C0162a.m9a(buttonName + " has no parent");
          } else if (View.class.isInstance(parent)) {
            ((View) parent).post(new TouchDelegateRunnable(btn, (View) parent));
          } else {
            C0162a.m9a("not a View instance: " + parent.toString());
          }
        } else {
          C0162a.m9a("findViewById null for " + buttonName);
        }
        i2 = i3 + 1;
      } else {
        return;
      }
    }
    */
  }

  public void onDeleteButtonClicked(int posInList) {
    C0162a.m9a("Player:onDeleteButtonClicked pos " + posInList);
    if (posInList >= 0 && posInList < this.stationList.size()) {
      deleteStationAtPosition(posInList);
    }
  }

  @Override // android.app.ListActivity, android.app.Activity
  protected void onDestroy() {
    super.onDestroy();
    C0162a.m9a("Player:onDestroy");
    notifyPlayerStopped();
    if (!this.mProperShutdown) {
      C0162a.m9a("finishTheApp!");
      finishTheApp();
    }
    unbindService(this);
    stopService(this.mServiceIntent);
    this.audioManager.abandonAudioFocus(this.f24c);
    this.context.unregisterReceiver(this.f23N);
    this.mLogoDb.closeDb();
    File f = new File(SettingsStationLogoActivity.LOGO_PATH_TMP);
    if (f.exists()) {
      new DirCleaner(f).clean();
      f.delete();
      C0162a.m9a("deleted " + f.getAbsoluteFile());
    } else {
      C0162a.m9a("not exist " + f.getAbsoluteFile());
    }
    sPlayerHandler = null;
    sMainActivityStartIntent = null;
  }

  @Override // android.app.Activity, android.view.KeyEvent.Callback
  public boolean onKeyDown(int i, KeyEvent keyEvent) {
    boolean handled = false;
    C0162a.m9a("onKeyDown " + i);
    switch (i) {
      case 87:
        onStationChange_nextWrapper();
        handled = true;
        break;
      case 88:
        onStationChange_prevWrapper();
        handled = true;
        break;
    }
    /*
    if (i == 4) {
      this.f19G = true;
    }
    */
    if (handled) {
      return true;
    }
    return super.onKeyDown(i, keyEvent);
  }

  /*
  public void onMotClicked() {
    if (isLeftAreaMaximized()) {
      maximizeLeftArea(false, true);
    } else {
      maximizeLeftArea(true, true);
    }
  }
  */

  public void onMotLongClicked() {
    /*
    DabSubChannelInfo currentStation;
    if (this.motImage.getSource() == 2
        && this.stationList != null
        && this.stationList.size() > 0
        && this.playIndex >= 0
        && this.playIndex < this.stationList.size()
        && (currentStation = this.stationList.get(this.playIndex)) != null) {
      int first_pos = FirstPosition(this.mStationListView);
      if (SettingsStationLogoActivity.storeUserStationLogo(
          this.context, this.motImage.getDrawable(), currentStation.mLabel, currentStation.mSID)) {
        updateStationList();
        QuickScrollTo(this.mStationListView, first_pos);
      }
    }
    */
    Toast.makeText(context, "onMotLongClicked is not implements", Toast.LENGTH_LONG).show();
  }

  @Override // android.app.Activity
  protected void onPause() {
    C0162a.m9a("Player:onPause");
    super.onPause();
    this.isInForeground = false;
    if (this.progressDialog.isShowing()) {
      this.progressDialog.dismiss();
    }
    // if (homeKeyReceiver != null) {
    // this.context.unregisterReceiver(homeKeyReceiver);
    // }

    // stop on back key?
    if (this.f19G) {
      m79e();
      if (this.dabHandler != null) {
        this.dabHandler.removeMessages(DabThread.MSGTYPE_DAB_DEINIT);
        Message obtainMessage = this.dabHandler.obtainMessage();
        obtainMessage.what = DabThread.MSGTYPE_DAB_DEINIT; // 5
        this.dabHandler.sendMessage(obtainMessage);
      }
      /*
      Intent intent = new Intent("com.microntek.app");
      intent.putExtra("app", DabService.SENDER_DAB);
      intent.putExtra("state", "EXIT");
      this.context.sendBroadcast(intent);
      */
    }
    this.mProperShutdown = isFinishing();
  }

  private void onPtyButtonClicked() {
    if (arrPty == null) {
      Toast.makeText(this.context, "PTY list is empty", 0).show();
    } else {
      startActivityForResult(new Intent(getApplicationContext(), PtyActivity.class), 1);
    }
  }

  @Override // android.app.Activity
  protected void onRestart() {
    C0162a.m9a("Player:onRestart");
    super.onRestart();
  }

  @Override // android.app.Activity
  protected void onResume() {
    C0162a.m9a("Player:onResume");
    super.onResume();
    this.isInForeground = true;
    this.f19G = false;

    // this.f20I = this.context.getSharedPreferences("playing", 0);
    // this.playIndex = this.f20I.getInt("current_playing", 0);
    this.playIndex = SharedPreferencesHelper.getInstance().getInteger("current_playing", 0);
    // if (this.mStationListView != null) {
    //  DeterminedScrollTo(this.mStationListView, this.playIndex);
    // }
    scrollToPositionRecycler(this.playIndex);
    /*
    SharedPreferences sharedPreferences =
        getSharedPreferences(SettingsActivity.prefname_settings, 0);
    Button btnRecord = (Button) findViewById(R.id.bt_record);
    if (btnRecord != null) {
      if (sharedPreferences.getBoolean(SettingsActivity.pref_key_record_button, false)) {
        btnRecord.setVisibility(0);
      } else {
        btnRecord.setVisibility(8);
      }
    */
    if (this.dabService != null) {
      this.dabService.m16a(this.f22M);
    }
    setUsbDeviceFromDeviceList();
    onResumeAdditions();
  }

  public void onResumeAdditions() {}

  public void onScanButtonClicked() {
    C0162a.m9a("scan button clicked");
    new ScanDialog(this, this.stationList.size());
  }

  @Override // android.content.ServiceConnection
  public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
    this.dabService = ((DabServiceBinder) iBinder).getService();
    C0162a.m9a("DAB service connected");
    this.dabService.m16a(this.f22M);
    this.dabService.setUsbDevice(this.usbManager, this.usbDevice);
    this.dabService.startDabThread();
    this.dabHandler = this.dabService.getDabHandlerFromDabThread();
    // new AsyncTaskC0118n().executeOnExecutor(Executors.newCachedThreadPool(), new Integer[0]);
    new Thread(new SearchDabHandler()).start();
  }

  @Override // android.content.ServiceConnection
  public void onServiceDisconnected(ComponentName componentName) {
    C0162a.m9a("DAB service disconnected");
    this.dabService = null;
  }

  public void onServiceFollowRequested() {
    Message obtainMessage = this.dabHandler.obtainMessage();
    obtainMessage.what = DabThread.MSGTYPE_START_SERVICE_FOLLOWING; // 23
    obtainMessage.obj = "";
    obtainMessage.arg1 = 0;
    this.dabHandler.sendMessage(obtainMessage);
  }

  public void onSettingsButtonClicked() {
    startActivity(new Intent(this.context, SettingsActivity.class));
  }

  public void onStationClicked(int posInList) {
    C0162a.m9a("Player:onStationClicked pos " + posInList);
    playStation(posInList);
  }

  private String PTYname(int i) {
    return Strings.PTYname(getApplicationContext(), i);
  }

  /*
  public void recreateLayout() {
    View v;
    View v2 = findViewById(R.id.left_area);
    if (v2 != null && (v = v2.getRootView()) != null) {
      v.invalidate();
      v.requestLayout();
    }
  }
     */

  public void setFavoriteListActive(boolean enabled) {
    int what;
    if (enabled != this.isFavoriteListActive) {
      this.isFavoriteListActive = enabled;
      if (this.isFavoriteListActive) {
        what = 31;
      } else {
        what = 32;
      }
      if (this.dabHandler != null) {
        this.dabHandler.removeMessages(what);
        Message message = this.dabHandler.obtainMessage(what);
        message.what = what;
        this.dabHandler.sendMessage(message);
      }
    }
  }

  public void setSignalLevel(int level) {
    switch (level) {
      case -1:
        this.imgSignalLevel.setImageDrawable(getResources().getDrawable(R.drawable.signal_scan));
        break;
      case 0:
        this.imgSignalLevel.setImageDrawable(getResources().getDrawable(R.drawable.signal_0));
        break;
      case 1:
        this.imgSignalLevel.setImageDrawable(getResources().getDrawable(R.drawable.signal_1));
        break;
      case 2:
        this.imgSignalLevel.setImageDrawable(getResources().getDrawable(R.drawable.signal_2));
        break;
      case 3:
        this.imgSignalLevel.setImageDrawable(getResources().getDrawable(R.drawable.signal_3));
        break;
      case 4:
        this.imgSignalLevel.setImageDrawable(getResources().getDrawable(R.drawable.signal_4));
        break;
      case 5:
        this.imgSignalLevel.setImageDrawable(getResources().getDrawable(R.drawable.signal_5));
        break;
    }
  }

  public void startScan(int scanType) {
    if (this.dabHandler == null) {
      this.dabHandler = this.dabService.getDabHandlerFromDabThread();
    }
    this.txtServiceName.setText("");
    // this.f38q.setText("");
    // this.f37p.setText("");
    this.txtDls.setText("");
    // this.motImage.setDefaultImage();
    arrPty = null;
    // this.f42v.setAdapter((SpinnerAdapter) null);
    Message obtainMessage = this.dabHandler.obtainMessage();
    obtainMessage.what = DabThread.MSGTYPE_START_STATION_SCAN; // 3
    obtainMessage.arg1 = 0;
    obtainMessage.arg2 = scanType;
    if (scanType != 1) {
      this.stationListSize = 0;
    }
    this.dabHandler.sendMessage(obtainMessage);
  }

  public void toastAndFinish(String toastText) {
    if (toastText != null) {
      C0162a.m9a("toastAndFinish: " + toastText);
      Toast.makeText(this.context, toastText, 1).show();
    }
    finishTheApp();
  }

  /*
  public void toggleFavoriteAtPosition(View view, int pos) {
    StationBaseAdapter.C0137b bVar;
    if (pos >= 0
        && pos < this.stationList.size()
        && view != null
        && (bVar = (StationBaseAdapter.C0137b) view.getTag()) != null) {
      DabSubChannelInfo subChannelInfo = this.stationList.get(pos);
      subChannelInfo.mIsFavorite = !subChannelInfo.mIsFavorite;
      C0162a.m9a("toggle fav @pos " + pos + " to " + subChannelInfo.mIsFavorite);
      try {
        this.stationList.set(pos, subChannelInfo);
        StationBaseAdapter.updateFavoriteUI(bVar, subChannelInfo.mIsFavorite);
        // StationBaseAdapter adapter = (StationBaseAdapter) this.mStationListView.getAdapter();
        // if (adapter != null) {
        //  adapter.updateFavorite(pos, subChannelInfo.mIsFavorite);
        // }
        if (this.dabHandler != null) {
          Message obtainMessage = this.dabHandler.obtainMessage();
          obtainMessage.what = 30;
          obtainMessage.obj = subChannelInfo;
          this.dabHandler.sendMessage(obtainMessage);
          return;
        }
        C0162a.m9a("no handler created yet, fav setting not saved");
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  */
}

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
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.KeyEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
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
import com.thf.dabplayer.dab.DabThread;
// import com.thf.dabplayer.dab.ChannelInfo;
import com.thf.dabplayer.dab.LogoDb;
import com.thf.dabplayer.dab.LogoDbHelper;
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
import java.util.List;
import android.app.Activity;

/* renamed from: com.ex.dabplayer.pad.activity.Player */
/* loaded from: classes.dex */
public class PlayerActivity extends Activity
    implements ServiceConnection,
        View.OnClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener {
  public static final int PLAYERMSG_ASSET_FOUND_LOGOS = 98;
  public static final int PLAYERMSG_AUDIO_DISTORTION = 102;
  public static final int PLAYERMSG_DISMISS_SERVICE_FOLLOWING = 24;
  public static final int PLAYERMSG_DLS = 9;
  public static final int PLAYERMSG_HW_FAILURE = 101;
  public static final int PLAYERMSG_MOT = 10;
  public static final int PLAYERMSG_NEW_LIST_OF_STATIONS = 1;
  public static final int PLAYERMSG_NEW_STATION_LIST = 18;
  public static final int PLAYERMSG_DAB_THREAD_INITIALIZED = 19;
  public static final int PLAYERMSG_NEXT_STATION = 103;
  public static final int PLAYERMSG_PREV_STATION = 104;
  public static final int PLAYERMSG_SCAN_FINISHED = 99;
  public static final int PLAYERMSG_SCAN_PROGRESS_UPDATE = 0;
  public static final int PLAYERMSG_SHOW_SERVICE_FOLLOWING = 23;
  public static final int PLAYERMSG_HIDE_SERVICE_FOLLOWING = 24;
  public static final int PLAYERMSG_SIGNAL_QUALITY = 11;
  public static final int PLAYERMSG_STATIONINFO_INTENT = 100;
  public static final int PLAYERMSG_SET_STATIONMEMORY = 200;
  public static final int PLAYERMSG_PLAY_STATION = 300;

  public static String[] arrPty;
  private List<DabSubChannelInfo> stationList;
  // private List<ChannelInfo> channelInfoList;
  private AudioManager audioManager;
  private ProgressDialog progressDialog;
  private boolean f19G;
  public Context context;
  public boolean f26e;
  // public boolean f27f;
  public Button favorBtn;
  public boolean f28g;
  private int playIndex = -1;
  private Button btnPrev;
  private Button btnNext;
  private Button layScan;
  private Button layExit;

  // private float mDefaultLeftAreaLayoutWeight;
  Intent mServiceIntent;
  private boolean mShowLogosInList;
  private TextView txtServiceName;
  private ImageView imgSignalLevel;
  private TextClock textClock;
  private TextView txtDls;
  private Toast toast_service_following;

  private SwitchStationsAdapter stationsAdapter;
  //private LinearLayoutManager linearLayoutManager;
  private ViewPager2 recyclerView;
  private SwitchStationsAdapter.Listener switchStationsAdapterListener =
      new SwitchStationsAdapter.Listener() {
        @Override
        public void onItemClick(int position) {
          onStationClicked(position);
        }

        @Override
        public void onLongPress(int position) {
          Toast.makeText(context, "long clicked at " + position, Toast.LENGTH_SHORT).show();
          if (PlayerActivity.this.motImage != null) {
            if (PlayerActivity.this.stationList != null
                && PlayerActivity.this.stationList.size() > position) {
              DabSubChannelInfo sci = PlayerActivity.this.stationList.get(position);
              PlayerActivity.this.mLogoDb.storeUserStationLogo(
                  PlayerActivity.this.motImage, sci.mLabel, sci.mSID);
            }
            // refresh presets
            viewPagerAdapter.refreshWithoutNewData();
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
          if (!PlayerActivity.this.isInitialized) {
            String text = getResources().getString(R.string.waitfewseconds);
            Toast.makeText(PlayerActivity.this.context, text, Toast.LENGTH_LONG).show();
            return;
          }
          playFavourite(memoryPos);
        }

        @Override
        public void onLongPress(int memoryPos) {
          if (!PlayerActivity.this.isInitialized) {
            String text = getResources().getString(R.string.waitfewseconds);
            Toast.makeText(PlayerActivity.this.context, text, Toast.LENGTH_LONG).show();
            return;
          }
          setMemory(memoryPos);
          // this comes back with PLAYERMSG_SET_STATIONMEMORY
        }
      };
  private TabLayout tabLayout;

  public Handler dabHandler;
  private DabService dabService;

  private static WeakReference<Intent> sMainActivityStartIntent = null;
  private static WeakReference<Handler> sPlayerHandler = null;

  public boolean isInitialized = false;
  private String strDls = "";

  private Handler dabFHandler = new DabFHandler(this);

  private final BroadcastReceiver f23N = new hBroadcastReceiver();

  AudioManager.OnAudioFocusChangeListener f24c =
      new AudioManager
          .OnAudioFocusChangeListener() { // from class: com.ex.dabplayer.pad.activity.Player.1
        @Override // android.media.AudioManager.OnAudioFocusChangeListener
        public void onAudioFocusChange(int i) {
          int arg = -1;
          boolean isAudiolossSupportEnabled = true;

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
              PlayerActivity.this.finishTheApp();
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
          if (arg != -1 && PlayerActivity.this.dabHandler != null) {
            PlayerActivity.this.dabHandler.removeMessages(34);
            Message msg = PlayerActivity.this.dabHandler.obtainMessage(34);
            msg.arg1 = arg;
            PlayerActivity.this.dabHandler.sendMessage(msg);
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
  private LogoDb mLogoDb = null;
  private MediaMetadataCompat mMetaData = new MediaMetadataCompat.Builder().build();
  private boolean mProperShutdown = false;
  private boolean mSendBroadcastIntent = true;
  private boolean mShowAdditionalInfos = true;
  // private StationDetails mStationDetails = new StationDetails();
  private float mStationNameSizeFromStyle = 0.0f;
  // private TouchListener mTouchListener = null;
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
    private final WeakReference<PlayerActivity> mPlayer;

    public DabFHandler(PlayerActivity player) {
      this.mPlayer = new WeakReference<>(player);
    }

    @Override // android.os.Handler
    public void handleMessage(Message message) {
      PlayerActivity player = this.mPlayer.get();
      if (player != null) {
        super.handleMessage(message);
        switch (message.what) {
          case PlayerActivity.PLAYERMSG_SCAN_PROGRESS_UPDATE: // 0:
            player.progressDialog.setProgress(message.arg1);
            player.progressDialog.setMessage(
                Strings.scanning(
                    PlayerActivity.this.getApplicationContext(), message.arg1, message.arg2));
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
            // if (!player.f27f && !player.f26e && player.stationListSize == 0) {
            //  message.arg1 = 0;
            // }
            player.setSignalLevel(message.arg1);
            break;
          case 13:
            // Player.access$802(player, (List) message.obj);
            // player.channelInfoList = (List) message.obj;
            // Player.access$900(player);
            // player.m78f();
            break;
          case PLAYERMSG_NEW_STATION_LIST: // 18
            Toast.makeText(
                    context, "Attention: reveived PLAYERMSG_NEW_STATION_LIST", Toast.LENGTH_LONG)
                .show();
            player.fillStationRecycler((List) message.obj);
            break;
          case PLAYERMSG_DAB_THREAD_INITIALIZED:
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
          case PlayerActivity.PLAYERMSG_ASSET_FOUND_LOGOS /* 98 */:
            if (player.stationList != null) {
              C0162a.m9a("assetlogos refresh display");
              player.updateStationList();
            }
            break;
          case PlayerActivity.PLAYERMSG_SCAN_FINISHED /* 99 */:
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
          case PlayerActivity.PLAYERMSG_STATIONINFO_INTENT: // 100
            Intent intent = (Intent) message.obj;
            boolean affectsAndroidMetaData =
                intent.hasExtra(DabService.EXTRA_AFFECTS_ANDROID_METADATA);
            if (affectsAndroidMetaData) {
              affectsAndroidMetaData =
                  intent.getBooleanExtra(DabService.EXTRA_AFFECTS_ANDROID_METADATA, false);
            }
            player.notifyStationInfo(intent, affectsAndroidMetaData);
            break;
          case PlayerActivity.PLAYERMSG_HW_FAILURE /* 101 */:
            player.toastAndFinish((String) message.obj);
            break;
          case PlayerActivity.PLAYERMSG_AUDIO_DISTORTION /* 102 */:
            player.notifyAudioDistortion();
            break;
          case PlayerActivity.PLAYERMSG_NEXT_STATION /* 103 */:
            player.onStationChange_nextWrapper();
            break;
          case PlayerActivity.PLAYERMSG_PREV_STATION /* 104 */:
            player.onStationChange_prevWrapper();
            break;

          case PlayerActivity.PLAYERMSG_PLAY_STATION:
            // player.playIndex = message.arg1;
            player.playStation(message.arg1);
            // SharedPreferencesHelper.getInstance().setInteger("current_playing",
            // this.player.playIndex);
            // player.scrollToPositionRecycler(player.playIndex);
            break;

          case PLAYERMSG_SET_STATIONMEMORY:
            viewPagerAdapter.setItems((List) message.obj);
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
      PlayerActivity.this.mViewFlipper.setDisplayedChild(this.mFlipToViewIdx);
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
        if (device.equals(PlayerActivity.this.usbDevice)) {
          C0162a.m9a("USB device gone -> finish");
          PlayerActivity.this.finishTheApp();
        }
      }
    }
  }

  private class SearchDabHandler implements Runnable {
    public void run() {
      int i = 0;
      C0162a.m9a("searching mDabHandler");
      while (PlayerActivity.this.dabHandler == null && i < 10) {
        i++;
        DabService dabService = PlayerActivity.this.getDabService();
        if (dabService != null) {
          PlayerActivity.this.dabHandler =
              PlayerActivity.this.getDabService().getDabHandlerFromDabThread();
          C0162a.m9a("mDabHandler:" + PlayerActivity.this.dabHandler);
        }
        try {
          Thread.sleep(100L);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      if (i >= 10 && PlayerActivity.this.dabHandler == null) {
        C0162a.m9a("failed searching mDabHandler");
      } else {
        Message obtainMessage = PlayerActivity.this.dabHandler.obtainMessage();
        obtainMessage.what = DabThread.MSGTYPE_DAB_INIT; // 2;
        PlayerActivity.this.dabHandler.sendMessage(obtainMessage);
        C0162a.m9a("searching mDabHandler done");
      }
    }
  }

  /* renamed from: com.ex.dabplayer.pad.activity.Player$RunnableBWrapperNext */
  /* loaded from: classes.dex */
  public class RunnableBWrapperNext implements Runnable {
    public RunnableBWrapperNext() {}

    @Override // java.lang.Runnable
    public void run() {
      PlayerActivity.this.selectNextStation();
    }
  }

  /* renamed from: com.ex.dabplayer.pad.activity.Player$RunnableCWrapperPrev */
  /* loaded from: classes.dex */
  public class RunnableCWrapperPrev implements Runnable {
    public RunnableCWrapperPrev() {}

    @Override // java.lang.Runnable
    public void run() {
      PlayerActivity.this.selectPreviousStation();
    }
  }

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
    private final WeakReference<PlayerActivity> mPlayer;

    public VTOLayoutListener(PlayerActivity player, LinearLayout leftBackgroundBox) {
      this.mPlayer = new WeakReference<>(player);
      this.mLeftBackgroundBox = leftBackgroundBox;
    }

    @Override // android.view.ViewTreeObserver.OnGlobalLayoutListener
    public void onGlobalLayout() {
      PlayerActivity player = this.mPlayer.get();
      if (player != null) {
        this.mLeftBackgroundBox.getViewTreeObserver().removeOnGlobalLayoutListener(this);
      }
    }
  }

  /* JADX INFO: Access modifiers changed from: private */
  /* renamed from: a */
  public void updateStationList() {
    this.stationsAdapter =
        new SwitchStationsAdapter(
            this.context, switchStationsAdapterListener, this.stationList, false);
    this.recyclerView.setAdapter(stationsAdapter);
    scrollToPositionRecycler(this.playIndex);
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

      if (this.stationList == null) {
        C0162a.m9a("station list is null");
      } else {

        C0162a.m9a("a(I): station list: " + this.stationList.size());
        // maximizeLeftArea(false, true);

        if (i < this.stationList.size()) {
          DabSubChannelInfo subChannelInfo = this.stationList.get(i);
          updateSelectedStatus(i);
          this.txtServiceName.setText(subChannelInfo.mLabel);
          // this.f38q.setText("" + subChannelInfo.mFreq);
          // this.f37p.setText(PTYname(subChannelInfo.mPty));
          this.f26e = true;

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

          SharedPreferencesHelper.getInstance().setInteger("current_playing", this.playIndex);
          // m78f();
          displayPrevCurrNextStation(this.playIndex);
          notifyStationChangesTo(subChannelInfo, this.playIndex, this.stationList.size());
        }
      }
    }
  }

  private void scrollToPositionRecycler(int idx) {
    // Toast.makeText(context, "scroll to pos " + idx, Toast.LENGTH_LONG).show();
    //this.linearLayoutManager.scrollToPosition(idx);
        this.recyclerView.setCurrentItem(idx);
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

    // maximizeLeftArea(false, true);
    this.stationListSize = this.stationList.size();
    C0162a.m9a("a(list): station list : " + this.stationListSize);
    if (this.stationListSize != 0) {

      stationsAdapter =
          new SwitchStationsAdapter(
              this.context, switchStationsAdapterListener, this.stationList, false);
      this.recyclerView.setAdapter(stationsAdapter);
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
    // SharedPreferences pref_settings =
    //  this.context.getSharedPreferences(SettingsActivity.prefname_settings, 0);
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

    if (this.keyDownHandler != null) {
      this.keyDownHandler.removeMessages(1);
      Message msg = this.keyDownHandler.obtainMessage();
      msg.what = DelayedRunnableHandler.MSG_DELAYED_RUN; // 1
      msg.obj = new RunnableBWrapperNext();

      this.keyDownHandler.sendMessageDelayed(msg, getKeyDownDebounceTimeMs());
      return;
    }
    selectNextStation();
  }

  /* JADX INFO: Access modifiers changed from: private */
  public void selectPreviousStation() {
    if (!this.progressDialog.isShowing() && this.stationListSize > 0) {
      int i = ((this.playIndex + this.stationListSize) - 1) % this.stationListSize;
      // if (this.f27f) {
      //  playPreset(i);
      // } else {
      playStation(i);
      // }
    }
  }

  /* JADX INFO: Access modifiers changed from: private */
  public void onStationChange_prevWrapper() {

    if (this.keyDownHandler != null) {
      this.keyDownHandler.removeMessages(1);
      Message msg = this.keyDownHandler.obtainMessage();
      msg.what = DelayedRunnableHandler.MSG_DELAYED_RUN;
      msg.obj = new RunnableCWrapperPrev();

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
    // Toast.makeText(context, "updateSelectedStatus is not implements", Toast.LENGTH_LONG).show();
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
        BitmapDrawable logoDrawable = mLogoDb.getLogo(label, sid);
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

  /*
  public static WeakReference<ArrayList<DabSubChannelInfo>> getStationListShadow() {
    if (s_stationListShadow == null) {
      return null;
    }
    WeakReference<ArrayList<DabSubChannelInfo>> retVal = new WeakReference<>(s_stationListShadow);
    return retVal;
  }
  */

  public boolean isFavoriteListActive() {
    return this.isFavoriteListActive;
  }

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
      if (!stringExtra.equals("pty dialog")) {
        this.stationListSize = this.stationList.size();
        if (i > 0) {
          playStation(i2);
        }
      } else if (i2 >= 0 && arrPty != null && this.dabHandler != null) {
        String stringExtra2 = arrPty[i2];
        Message obtainMessage = this.dabHandler.obtainMessage();
        obtainMessage.what = DabThread.MSGTYPE_SELECT_PTY; //  20;
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
        onScanButtonClicked();
        return;
      } else if (view.getId() == R.id.signal_level) {
        if (ServiceFollowing.is_possible()) {
          C0162a.m9a("manually activated service following");
          onServiceFollowRequested();
          return;
        }
        C0162a.m9a("manually activation ignored");
        return;
      } else if (view.getId() == R.id.bt_prev) {
        selectPreviousStation();
        return;
      } else if (view.getId() == R.id.bt_next) {
        selectNextStation();
        return;
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

  private void playFavourite(int memoryPos) {

    this.dabHandler.removeMessages(DabThread.PLAY_FAVOURITE);
    Message obtainMessage = this.dabHandler.obtainMessage();
    obtainMessage.what = DabThread.PLAY_FAVOURITE;
    obtainMessage.arg1 = memoryPos;
    PlayerActivity.this.dabHandler.sendMessage(obtainMessage);
    // this comes back with PLAYERMSG_PLAY_STATION
  }

  @Override // android.app.Activity
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    C0162a.m9a("Player:onCreate");
    setContentView(R.layout.activity_player);
    // this.playIndex = 0;
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
    this.txtServiceName = (TextView) findViewById(R.id.service_name);
    this.txtServiceName.setText("");

    this.txtDls = (TextView) findViewById(R.id.dls_scroll);
    this.txtDls.setText("");

    this.textClock = (TextClock) findViewById(R.id.textClock);
    if (!SharedPreferencesHelper.getInstance().getBoolean("showClock")) {
      this.textClock.setVisibility(View.GONE);
    }

    recyclerView = this.findViewById(R.id.recycler2);
    /*
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
    */

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

    this.audioManager.requestAudioFocus(this.f24c, 3, 1);

    this.mServiceIntent = new Intent(this, DabService.class);
    startService(this.mServiceIntent);
    bindService(this.mServiceIntent, this, 1);

    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction("android.hardware.usb.action.USB_DEVICE_DETACHED");
    this.context.registerReceiver(this.f23N, intentFilter);

    this.mLogoDb = LogoDbHelper.getInstance(this);
    onCreateAdditions(savedInstanceState);
  }

  @SuppressLint({"ClickableViewAccessibility"})
  private void onCreateAdditions(Bundle savedInstanceState) {
    LinearLayout.LayoutParams params;
    // homeKeyReceiver = new HomeKeyReceiver(this);
    sPlayerHandler = new WeakReference<>(this.dabFHandler);

    Intent startedByIntent = getIntent();
    if (startedByIntent != null) {
      C0162a.m9a("Player startedByIntent=" + startedByIntent.toString());
      Intent mainWasStartedByIntent =
          (Intent) startedByIntent.getParcelableExtra("StartedByIntent");
      if (mainWasStartedByIntent != null) {
        sMainActivityStartIntent = new WeakReference<>(mainWasStartedByIntent);
        String action = mainWasStartedByIntent.getAction();
      }
    }

    // this.mDefaultLeftAreaLayoutWeight = 5.0f;
    /*
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
        */
  }

  /*
  public void onDeleteButtonClicked(int posInList) {
    C0162a.m9a("Player:onDeleteButtonClicked pos " + posInList);
    if (posInList >= 0 && posInList < this.stationList.size()) {
      deleteStationAtPosition(posInList);
    }
  }
   */

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
    File f = new File(Strings.LOGO_PATH_TMP);

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

  public void onMotLongClicked() {
    Toast.makeText(context, "onMotLongClicked is not implements", Toast.LENGTH_LONG).show();
  }

  @Override // android.app.Activity
  protected void onPause() {
    C0162a.m9a("Player:onPause");
    super.onPause();

    /*
        SharedPreferencesHelper.getInstance()
            .getSharedPreferences()
            .unregisterOnSharedPreferenceChangeListener(this);
    */
    this.isInForeground = false;
    if (this.progressDialog.isShowing()) {
      this.progressDialog.dismiss();
    }

    // stop on back key?
    if (this.f19G) {
      m79e();
      if (this.dabHandler != null) {
        this.dabHandler.removeMessages(DabThread.MSGTYPE_DAB_DEINIT);
        Message obtainMessage = this.dabHandler.obtainMessage();
        obtainMessage.what = DabThread.MSGTYPE_DAB_DEINIT; // 5
        this.dabHandler.sendMessage(obtainMessage);
      }
    }
    this.mProperShutdown = isFinishing();
  }

  /*
  private void onPtyButtonClicked() {
    if (arrPty == null) {
      Toast.makeText(this.context, "PTY list is empty", 0).show();
    } else {
      startActivityForResult(new Intent(getApplicationContext(), PtyActivity.class), 1);
    }
  }
    */

  @Override // android.app.Activity
  protected void onRestart() {
    C0162a.m9a("Player:onRestart");
    super.onRestart();
  }

  @Override // android.app.Activity
  protected void onResume() {
    C0162a.m9a("Player:onResume");
    super.onResume();
    SharedPreferencesHelper.getInstance()
        .getSharedPreferences()
        .registerOnSharedPreferenceChangeListener(this);

    this.isInForeground = true;
    this.f19G = false;

    if (this.playIndex == -1) {
      this.playIndex = SharedPreferencesHelper.getInstance().getInteger("current_playing", 0);
      scrollToPositionRecycler(this.playIndex);
    }

    if (this.dabService != null) {
      this.dabService.setPlayerHandler(this.dabFHandler);
    }
    setUsbDeviceFromDeviceList();
  }

  @Override // android.content.SharedPreferences.OnSharedPreferenceChangeListener
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    switch (key) {
      case "presetPages":
        viewPagerAdapter.setNumPages(SharedPreferencesHelper.getInstance().getInteger(key));
        break;
      case "showClock":
        boolean showClock = SharedPreferencesHelper.getInstance().getBoolean(key);
        if (showClock) {
          this.textClock.setVisibility(View.VISIBLE);
        } else {
          this.textClock.setVisibility(View.GONE);
        }
        break;
    }
  }

  public void onScanButtonClicked() {
    C0162a.m9a("scan button clicked");

    SimpleDialog.SimpleDialogListener simpleDialogListener =
        new SimpleDialog.SimpleDialogListener() {
          @Override
          public void onClick(boolean positive, int selection) {
            // 0 full
            // 1 keep favourites
            // 2 update
            if (positive) {
              startScan(selection);
            }
          }
        };
    SimpleDialog sd = new SimpleDialog(this, "Scan mode", true, simpleDialogListener);
    sd.addRadio(context.getResources().getString(R.string.text_full_scan));
    sd.addRadio(context.getResources().getString(R.string.text_favo_scan));
    // sd.addRadio(context.getResources().getString(R.string.text_incr_scan));
    sd.show();

    // new ScanDialog(this, this.stationList.size());
  }

  @Override // android.content.ServiceConnection
  public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
    this.dabService = ((DabServiceBinder) iBinder).getService();
    C0162a.m9a("DAB service connected");
    this.dabService.setPlayerHandler(this.dabFHandler);
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
  */

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
}

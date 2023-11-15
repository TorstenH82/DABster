package com.thf.dabplayer.activity;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.thf.dabplayer.DabsterApp;
import com.thf.dabplayer.R;
import com.thf.dabplayer.dab.DabThread;
// import com.thf.dabplayer.dab.ChannelInfo;
import com.thf.dabplayer.dab.LogoDb;
import com.thf.dabplayer.dab.LogoDbHelper;
import com.thf.dabplayer.dab.DabSubChannelInfo;
import com.thf.dabplayer.dab.StationInfo;
import com.thf.dabplayer.service.DabServiceBinder;
import com.thf.dabplayer.service.DabService;
import com.thf.dabplayer.utils.Logger;
import com.thf.dabplayer.utils.DirCleaner;
import com.thf.dabplayer.utils.ServiceFollowing;
import com.thf.dabplayer.utils.SharedPreferencesHelper;
import com.thf.dabplayer.utils.Strings;
import java.io.File;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import java.util.stream.Collectors;

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
  public static final int PLAYERMSG_STATIONINFO = 100;
  public static final int PLAYERMSG_SET_STATIONMEMORY = 200;
  // public static final int PLAYERMSG_PLAY_PRESET = 300;

  // public static String[] arrPty;
  private List<DabSubChannelInfo> stationList;
  private List<DabSubChannelInfo> presetList;
  // private List<ChannelInfo> channelInfoList;
  private AudioManager audioManager;
  private SimpleDialog progressDialog;
  // private boolean f19G;
  private DabsterApp mApplication;
  public Context context;
  public boolean f26e;
  private int isPlayingPreset = -1; // only set to true if user clicked on memory button
  // public boolean f27f;
  public Button favorBtn;
  public boolean f28g;
  private int playIndex = -1;
  private Button btnPrev;
  private Button btnNext;
  private Button btnScan;
  private Button btnExit;
  private Button btnSettings;

  // private float mDefaultLeftAreaLayoutWeight;
  Intent mServiceIntent;
  private boolean mShowLogosInList;
  private ImageView imgSignalLevel;
  private TextClock textClock;
  private TextView txtDls;
  private Toast toast_service_following;

  private SwitchStationsAdapter stationsAdapter;
  // private LinearLayoutManager linearLayoutManager;
  private ViewPager2 viewPagerStations;
  private SwitchStationsAdapter.Listener switchStationsAdapterListener =
      new SwitchStationsAdapter.Listener() {
        private int lastLongClickPosition = -1;

        @Override
        public void onItemClick(int position) {
          onStationClicked(position);
        }

        // long click on station item
        @Override
        public void onLongPress(int position) {
          // check here if custom image set
          if (PlayerActivity.this.stationList != null
              && PlayerActivity.this.stationList.size() > position) {
            DabSubChannelInfo sci = PlayerActivity.this.stationList.get(position);

            if (PlayerActivity.this.motImage != null) {
              PlayerActivity.this.mLogoDb.storeUserStationLogo(
                  PlayerActivity.this.motImage, sci.mLabel, sci.mSID);
              // refresh presets
              // viewPagerAdapter.refreshWithoutNewData();
            } else {
              if (PlayerActivity.this.mLogoDb.getLogoFilenameForStation(sci.mLabel, sci.mSID)
                  != null) {
                if (position == this.lastLongClickPosition) {
                  PlayerActivity.this.mLogoDb.deleteUserStationLogo(sci.mLabel, sci.mSID);
                  // PlayerActivity.this.stationsAdapter.setMot(null, -1);
                  PlayerActivity.this.stationsAdapter.refreshWithoutNewData(
                      PlayerActivity.this.viewPagerPresets.getCurrentItem());

                } else {
                  this.lastLongClickPosition = position;
                  Toast.makeText(
                          PlayerActivity.this.context,
                          "Again to reset station logo",
                          Toast.LENGTH_LONG)
                      .show();
                  return;
                }
              }
            }
            // refresh presets
            viewPagerAdapter.refreshWithoutNewData();
            this.lastLongClickPosition = -1;
          }
        }
      };

  private ViewPagerAdapter viewPagerAdapter;
  // private LinearLayoutManager linearLayoutManagerPageViewer;
  private ViewPager2 viewPagerPresets;
  private ViewPagerAdapter.Listener viewPagerAdapterListener =
      new ViewPagerAdapter.Listener() {
        @Override
        public void onItemClick(int memoryPos) {
          if (!PlayerActivity.this.isInitialized) {
            String text = getResources().getString(R.string.waitfewseconds);
            Toast.makeText(PlayerActivity.this.context, text, Toast.LENGTH_LONG).show();
            return;
          }
          playPreset(memoryPos);
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

  // private static WeakReference<Intent> sMainActivityStartIntent = null;
  private static WeakReference<Handler> sPlayerHandler = null;

  public boolean isInitialized = false;
  private String strDls = "";

  private Handler dabFHandler = new DabFHandler(this);

  private final BroadcastReceiver usbDetachedBroadcastReceiver = new UsbDetachedBroadcastReceiver();

  AudioManager.OnAudioFocusChangeListener audioFocusChangeListener =
      new AudioManager.OnAudioFocusChangeListener() { // from class:
        // com.ex.dabplayer.pad.activity.Player.1
        @Override // android.media.AudioManager.OnAudioFocusChangeListener
        public void onAudioFocusChange(int i) {
          int arg = -1;
          boolean isAudiolossSupportEnabled = true;

          switch (i) {
            case -3:
              Logger.d("AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
              if (isAudiolossSupportEnabled) {
                arg = DabThread.AUDIOSTATE_DUCK;
                break;
              } else {
                Logger.d("no complete audioloss support enabled");
                break;
              }
            case -2:
              Logger.d("AUDIOFOCUS_LOSS_TRANSIENT");
              if (isAudiolossSupportEnabled) {
                arg = DabThread.AUDIOSTATE_PAUSE;
                break;
              } else {
                Logger.d("no complete audioloss support enabled");
                break;
              }
            case -1:
              Logger.d("AUDIOFOCUS_LOSS");
              PlayerActivity.this.finishTheApp();
              break;
            case 1:
              Logger.d("AUDIOFOCUS_GAIN");
              arg = DabThread.AUDIOSTATE_PLAY;
              break;
            case 2:
              Logger.d("AUDIOFOCUS_GAIN_TRANSIENT");
              break;
            case 3:
              Logger.d("AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK");
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
  // private boolean isFavoriteListActive = false;
  private boolean isInForeground = false;
  private DelayedRunnableHandler keyDownHandler = new DelayedRunnableHandler();
  private Toast mAudioDistortionToast = null;
  private Toast mChannelToast = null;
  private LogoDb mLogoDb = null;
  private MediaMetadataCompat mMetaData = new MediaMetadataCompat.Builder().build();
  private boolean mProperShutdown = false;
  private boolean mSendBroadcastIntent = true;
  private boolean mShowAdditionalInfos = true;
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
            // player.progressDialog.setProgress(message.arg1);

            String title =
                context.getResources().getString(R.string.scanning) + " " + message.arg1 + "%";
            player.progressDialog.setTitle(title);

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
            player.setSignalLevel(message.arg1);
            break;

          case PLAYERMSG_NEW_STATION_LIST: // 18
            Toast.makeText(
                    context, "Attention: reveived PLAYERMSG_NEW_STATION_LIST", Toast.LENGTH_LONG)
                .show();
            player.fillStationViewPager((List) message.obj);
            break;
          case PLAYERMSG_DAB_THREAD_INITIALIZED:
            player.isInitialized = true;
            if (player.stationListSize > 0) {
              if (message.arg1 == 0) {
                Logger.d("play station after PLAYERMSG_DAB_THREAD_INITIALIZED");
                player.playStation(player.playIndex);
              }
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
              Logger.d("assetlogos refresh display");
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
          case PlayerActivity.PLAYERMSG_STATIONINFO: // 100
            
            StationInfo stationInfo = (StationInfo) message.obj;
            player.notifyStationInfo(stationInfo);
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
          case PlayerActivity.PLAYERMSG_SET_STATIONMEMORY:
            player.isPlayingPreset = 1;
            player.presetList = (List) message.obj;
            viewPagerAdapter.setItems(player.presetList);
            break;
          default:
            Toast.makeText(player.context, "msg.what" + message.what, 0).show();
            break;
        }
      }
    }
  }

  /* renamed from: com.ex.dabplayer.pad.activity.Player$hBroadcastReceiver */
  /* loaded from: classes.dex */
  public class UsbDetachedBroadcastReceiver extends BroadcastReceiver {
    public UsbDetachedBroadcastReceiver() {}

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
      Logger.d("h: " + intent.toString());
      Logger.d("h: " + intent.getExtras().toString());
      String action = intent.getAction();

      if (action.equals("android.hardware.usb.action.USB_DEVICE_DETACHED")) {
        UsbDevice device = (UsbDevice) intent.getParcelableExtra("device");
        Logger.d("USB device detached: " + device.getDeviceName());
        if (device.equals(PlayerActivity.this.usbDevice)) {
          Logger.d("USB device gone -> finish");
          PlayerActivity.this.finishTheApp();
        }
      }
    }
  }

  private class SearchDabHandler implements Runnable {
    public void run() {
      int i = 0;
      Logger.d("searching mDabHandler");
      while (PlayerActivity.this.dabHandler == null && i < 10) {
        i++;
        DabService dabService = PlayerActivity.this.getDabService();
        if (dabService != null) {
          PlayerActivity.this.dabHandler =
              PlayerActivity.this.getDabService().getDabHandlerFromDabThread();
          Logger.d("mDabHandler:" + PlayerActivity.this.dabHandler);
        }
        try {
          Thread.sleep(100L);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      if (i >= 10 && PlayerActivity.this.dabHandler == null) {
        Logger.d("failed searching mDabHandler");
      } else {
        Message obtainMessage = PlayerActivity.this.dabHandler.obtainMessage();
        obtainMessage.what = DabThread.MSGTYPE_DAB_INIT; // 2;
        PlayerActivity.this.dabHandler.sendMessage(obtainMessage);
        Logger.d("searching mDabHandler done");
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
  /*
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
  */
  /* renamed from: com.ex.dabplayer.pad.activity.Player$VTOLayoutListener */
  /* loaded from: classes.dex */
  /*
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
  */

  /* JADX INFO: Access modifiers changed from: private */
  /* renamed from: a */
  public void updateStationList() {
    this.stationsAdapter =
        new SwitchStationsAdapter(this.context, switchStationsAdapterListener, this.stationList);
    this.viewPagerStations.setAdapter(stationsAdapter);
    scrollToPositionViewPagerStations(this.playIndex);
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
      // this.toast_service_following.setGravity(
      //        17, -(getResources().getDisplayMetrics().widthPixels / 4), 0);
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
        Logger.d("station list is null");
      } else {

        Logger.d("a(I): station list: " + this.stationList.size());
        // maximizeLeftArea(false, true);

        if (i < this.stationList.size()) {
          DabSubChannelInfo subChannelInfo = this.stationList.get(i);
          scrollToPositionViewPagerStations(i);

          this.f26e = true;
          this.playIndex = i;

          this.txtDls.setText("");
          this.dabHandler.removeMessages(DabThread.MSGTYPE_START_PLAY_STATION);
          Message obtainMessage = this.dabHandler.obtainMessage();
          obtainMessage.what = DabThread.MSGTYPE_START_PLAY_STATION; // 6;
          obtainMessage.arg1 = i;
          this.dabHandler.sendMessage(obtainMessage);

          Logger.d("dab play index:" + this.playIndex);
          // DeterminedScrollTo(this.mStationListView, this.playIndex);
          scrollToPositionViewPagerStations(this.playIndex);

          SharedPreferencesHelper.getInstance().setInteger("current_playing", this.playIndex);
          // m78f();
          showStationPopup(this.playIndex);
          // notifyStationChangesTo(subChannelInfo, this.playIndex, this.stationList.size());
        }
      }
    }
  }

  // switch to another station in stations view pager and null the mot image
  private void scrollToPositionViewPagerStations(int idx) {
    // Toast.makeText(context, "scroll to pos " + idx, Toast.LENGTH_LONG).show();
    // this.linearLayoutManager.scrollToPosition(idx);
    this.viewPagerStations.setCurrentItem(idx, false);
    if (this.stationsAdapter != null) {
      this.stationsAdapter.setMot(null, -1);
    }
    this.motImage = null;
  }

  /* JADX INFO: Access modifiers changed from: private */
  /* renamed from: a */
  public void fillStationViewPager(List list) {
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
    Logger.d("a(list): station list : " + this.stationListSize);
    if (this.stationListSize != 0) {

      stationsAdapter =
          new SwitchStationsAdapter(this.context, switchStationsAdapterListener, this.stationList);
      this.viewPagerStations.setAdapter(stationsAdapter);
    }
  }

  /* JADX INFO: Access modifiers changed from: private */
  public void selectPreviousStation() {
    /*
    if (!this.progressDialog.isShowing() && this.stationListSize > 0) {
      int i = ((this.playIndex + this.stationListSize) - 1) % this.stationListSize;
      playStation(i);
    }
    */
    if (!this.progressDialog.isShowing() && this.stationListSize > 0) {
      if ((this.isPlayingPreset == -1 || this.isPlayingPreset == 1)
          && this.presetList != null
          && this.presetList.size() > 1) {

        DabSubChannelInfo info = this.stationList.get(this.playIndex);
        int presetIndex = this.presetList.indexOf(info);
        if (presetIndex != -1) {
          presetIndex = ((presetIndex + this.presetList.size()) - 1) % this.presetList.size();
          info = this.presetList.get(presetIndex);
          playPreset(info.mFavorite);
          return;
        }
      }
      int i = ((this.playIndex + this.stationListSize) - 1) % this.stationListSize;
      playStation(i);
    }
  }

  /* JADX INFO: Access modifiers changed from: private */
  public void selectNextStation() {
    if (!this.progressDialog.isShowing() && this.stationListSize > 0) {
      if ((this.isPlayingPreset == -1 || this.isPlayingPreset == 1)
          && this.presetList != null
          && this.presetList.size() > 1) {
        DabSubChannelInfo info = this.stationList.get(this.playIndex);
        int presetIndex = this.presetList.indexOf(info);
        if (presetIndex != -1) {
          presetIndex = ((presetIndex + this.presetList.size()) + 1) % this.presetList.size();
          info = this.presetList.get(presetIndex);
          playPreset(info.mFavorite);
          return;
        }
      }
      int i = ((this.playIndex + this.stationListSize) + 1) % this.stationListSize;
      playStation(i);
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
        this.motImage = new BitmapDrawable(getResources(), decodeFile);
        this.stationsAdapter.setMot(this.motImage, playIndex);
        return;
      }
      return;
    }
    Logger.d("file '" + file.getAbsolutePath() + "' not found");
    // }
  }

  /* JADX INFO: Access modifiers changed from: private */
  public void onStationChange_nextWrapper() {
    if (this.keyDownHandler != null) {
      this.keyDownHandler.removeMessages(DelayedRunnableHandler.MSG_DELAYED_RUN);
      Message msg = this.keyDownHandler.obtainMessage();
      msg.what = DelayedRunnableHandler.MSG_DELAYED_RUN; // 1
      msg.obj = new RunnableBWrapperNext();

      this.keyDownHandler.sendMessageDelayed(msg, getKeyDownDebounceTimeMs());
      return;
    }
    selectNextStation();
  }

  /* JADX INFO: Access modifiers changed from: private */
  public void onStationChange_prevWrapper() {
    if (this.keyDownHandler != null) {
      this.keyDownHandler.removeMessages(DelayedRunnableHandler.MSG_DELAYED_RUN);
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

  /*
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
  */
  public void showStationPopup(int currIndex) {

    if (this.isInForeground) return;

    DabSubChannelInfo sciCurrent = this.stationList.get(currIndex);
    DabSubChannelInfo sciPrevious = null;
    DabSubChannelInfo sciNext = null;

    // get next an previous station from presets
    if ((this.isPlayingPreset == -1 || this.isPlayingPreset == 1)
        && this.presetList != null
        && this.presetList.size() > 1) {

      int presetIndexCurrent = this.presetList.indexOf(sciCurrent);
      if (presetIndexCurrent != -1) {
        int presetIndexNext =
            ((presetIndexCurrent + this.presetList.size()) + 1) % this.presetList.size();
        sciNext = this.presetList.get(presetIndexNext);
        int presetIndexPrevious =
            ((presetIndexCurrent + this.presetList.size()) - 1) % this.presetList.size();
        sciPrevious = this.presetList.get(presetIndexPrevious);
      }
    }

    // get next an previous station from station list
    if (sciNext == null) {
      int presetIndexNext = ((currIndex + this.stationListSize) + 1) % this.stationListSize;
      sciNext = this.stationList.get(presetIndexNext);
      int presetIndexPrevious = ((currIndex + this.stationListSize) - 1) % this.stationListSize;
      sciPrevious = this.stationList.get(presetIndexPrevious);
    }

    if (sciPrevious != null && sciNext != null) {
      List<DabSubChannelInfo> list = new ArrayList<>();
      list.add(sciPrevious);
      list.add(sciCurrent);
      list.add(sciNext);

      if (mApplication.isPopupActivityRunning()) {
        Intent intentBrdc = new Intent("popup-message");
        intentBrdc.putExtra("stationList", (Serializable) list);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intentBrdc);
      } else {
        Logger.d("show rich station switch popup");
        Intent intentSrv = new Intent(context, PopupActivity.class);
        intentSrv.setAction("ACTION_POPUP");
        intentSrv.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intentSrv.putExtra("stationList", (Serializable) list);
        startActivity(intentSrv);
      }
    }
  }

  /* JADX INFO: Access modifiers changed from: private */
  /* renamed from: e */
  public void stopDabHandler() {
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
    Logger.d("finishTheApp");

    Intent intentSrv = new Intent(getBaseContext(), DabService.class);
    intentSrv.setAction(DabService.ACTION_STOP_SERVICE);
    startForegroundService(intentSrv);

    // ???

    stopDabHandler();
    /*    ???
    if (this.dabHandler != null) {
      this.dabHandler.removeMessages(5);
      Message obtainMessage = this.dabHandler.obtainMessage();
      obtainMessage.what = DabThread.MSGTYPE_DAB_DEINIT; // 5;
      obtainMessage.arg1 = 0;
      this.dabHandler.sendMessage(obtainMessage);
    }
    */
    boolean isDestroyed = false;
    if (Build.VERSION.SDK_INT >= 17) {
      isDestroyed = isDestroyed();
    }
    if (!isDestroyed) {
      Logger.d("finish()");
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

  /*
  public static WeakReference<Intent> getMainActivityStartIntentWeakRef() {
    return sMainActivityStartIntent;
  }
  */

  public BitmapDrawable getMotLogoOrIcon() {
    if (this.motImage != null) {
      return this.motImage;
    } else {
      if (this.stationList != null && this.stationList.size() > this.playIndex) {
        String label = this.stationList.get(this.playIndex).mLabel;
        // LogoDb logoDb = LogoDbHelper.getInstance(this.context);
        int sid = this.stationList.get(this.playIndex).mSID;
        // String logoFilename = mLogoDb.getLogoFilenameForStation(label, sid);
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

  public void notifyAudioDistortion() {
    if (this.mAudioDistortionToast != null) {
      this.mAudioDistortionToast.cancel();
    }
    this.mAudioDistortionToast =
        Toast.makeText(this.context, (int) R.string.audio_distortion_notification, 0);
    this.mAudioDistortionToast.show();
  }

  public void notifyStationInfo(StationInfo stationInfo) {
    viewPagerAdapter.setDetails(stationInfo);

    MediaSessionCompat mediaSession = mediaSession = dabService.getMediaSession();

    if (mediaSession != null) {
      Logger.d("set mediaSession metadata and set artist to " + stationInfo.getStation());

      MediaMetadataCompat.Builder metaDataBuilder = new MediaMetadataCompat.Builder(this.mMetaData);
      metaDataBuilder.putString(
          MediaMetadataCompat.METADATA_KEY_ALBUM, stationInfo.getEnsembleName());
      metaDataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, stationInfo.getStation());
      metaDataBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, stationInfo.getDls());
      metaDataBuilder.putLong(
          MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, stationInfo.getStationNum());
      metaDataBuilder.putLong(
          MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, stationInfo.getNumStations());
      metaDataBuilder.putString(MediaMetadataCompat.METADATA_KEY_GENRE, stationInfo.getPty());
      metaDataBuilder.putBitmap(
          MediaMetadataCompat.METADATA_KEY_ALBUM_ART, stationInfo.getLogo(context).getBitmap());
      this.mMetaData = metaDataBuilder.build();
      mediaSession.setMetadata(this.mMetaData);
      this.dabService.updateNotification(stationInfo);
    }
  }

  @Override // android.view.View.OnClickListener
  public void onClick(View view) {
    Logger.d("onClick 0x" + Integer.toHexString(view.getId()));

    if (view.getId() == R.id.bt_settings) {
      onSettingsButtonClicked();
    } else if (view.getId() == R.id.layExit) {
      finishTheApp();
    } else if (!this.isInitialized) {
      String waitfewseconds = getResources().getString(R.string.waitfewseconds);
      Toast.makeText(this.context, waitfewseconds, Toast.LENGTH_LONG).show();
    } else if (this.stationListSize != 0 || view.getId() == R.id.layScan) {
      if (view.getId() == R.id.layScan) {
        onScanButtonClicked();
        return;
      } else if (view.getId() == R.id.signal_level) {
        if (ServiceFollowing.is_possible()) {
          Logger.d("manually activated service following");
          onServiceFollowRequested();
          return;
        }
        Logger.d("manually activation ignored");
        return;
      } else if (view.getId() == R.id.bt_prev) {
        this.isPlayingPreset = 0;
        selectPreviousStation();
        return;
      } else if (view.getId() == R.id.bt_next) {
        this.isPlayingPreset = 0;
        selectNextStation();
        return;
      } else {
        return;
      }
    } else {
      String scanfirst = getResources().getString(R.string.scanfirst);
      Toast.makeText(this.context, scanfirst, Toast.LENGTH_SHORT).show();
    }
  }

  private void setMemory(int storagePos) {
    if (this.stationList != null) {
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
  }

  private void playPreset(int memoryPos) {
    if (this.presetList != null) {
      List<DabSubChannelInfo> presetStation =
          this.presetList.stream()
              .filter(preset -> preset.mFavorite == memoryPos)
              .collect(Collectors.toList());

      if (presetStation != null && presetStation.size() == 1) {
        this.isPlayingPreset = 1;
        int idx = this.stationList.indexOf(presetStation.get(0));
        this.playStation(idx);
        return;
      }
    }
    Toast.makeText(
            this.context, this.context.getString(R.string.PresetLongPressHint), Toast.LENGTH_LONG)
        .show();
  }

  private void prepareMenuBar(boolean menuOnTop) {
    LinearLayout topBottomBar;
    if (menuOnTop) {
      topBottomBar = (LinearLayout) findViewById(R.id.topbar);
      ((LinearLayout) findViewById(R.id.bottombar)).setVisibility(View.GONE);
    } else {
      topBottomBar = (LinearLayout) findViewById(R.id.bottombar);
      ((LinearLayout) findViewById(R.id.topbar)).setVisibility(View.GONE);
    }
    topBottomBar.setVisibility(View.VISIBLE);
    this.btnScan = (Button) topBottomBar.findViewById(R.id.layScan);
    this.btnScan.setOnClickListener(this);
    this.btnExit = (Button) topBottomBar.findViewById(R.id.layExit);
    this.btnExit.setOnClickListener(this);
    this.btnSettings = (Button) topBottomBar.findViewById(R.id.bt_settings);
    btnSettings.setOnClickListener(this);
    this.imgSignalLevel = (ImageView) topBottomBar.findViewById(R.id.signal_level);
    this.imgSignalLevel.setOnClickListener(this);
    this.textClock = (TextClock) topBottomBar.findViewById(R.id.textClock);
    if (!SharedPreferencesHelper.getInstance().getBoolean("showClock")) {
      this.textClock.setVisibility(View.GONE);
    }
  }

  @Override // android.app.Activity
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Logger.d("Player:onCreate");
    setContentView(R.layout.activity_player);

    this.context = getApplicationContext();
    this.mApplication = (DabsterApp) getApplicationContext();

    prepareMenuBar(SharedPreferencesHelper.getInstance().getBoolean("menuTop"));

    this.btnNext = (Button) findViewById(R.id.bt_next);
    this.btnNext.setOnClickListener(this);
    this.btnPrev = (Button) findViewById(R.id.bt_prev);
    this.btnPrev.setOnClickListener(this);

    this.txtDls = (TextView) findViewById(R.id.dls_scroll);
    this.txtDls.setText("");
    this.txtDls.setSelected(true);

    this.viewPagerStations = this.findViewById(R.id.station_vp);
    this.viewPagerStations.registerOnPageChangeCallback(
        new ViewPager2.OnPageChangeCallback() {
          @Override
          public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels);
          }

          @Override
          public void onPageSelected(int position) {
            super.onPageSelected(position);

            if (userScrollChange) {
              PlayerActivity.this.isPlayingPreset = 0;
              onStationClicked(position);
            }
          }

          int previousState;
          boolean userScrollChange;

          @Override
          public void onPageScrollStateChanged(int state) {
            super.onPageScrollStateChanged(state);
            if (previousState == ViewPager.SCROLL_STATE_DRAGGING
                && state == ViewPager.SCROLL_STATE_SETTLING) userScrollChange = true;
            else if (previousState == ViewPager.SCROLL_STATE_SETTLING
                && state == ViewPager.SCROLL_STATE_IDLE) userScrollChange = false;

            previousState = state;
          }
        });

    this.viewPagerPresets = this.findViewById(R.id.viewPager);
    int numberPresetPages = SharedPreferencesHelper.getInstance().getInteger("presetPages");
    this.viewPagerAdapter =
        new ViewPagerAdapter(context, this.viewPagerAdapterListener, numberPresetPages);
    this.viewPagerPresets.setAdapter(this.viewPagerAdapter);
    this.tabLayout = this.findViewById(R.id.tabLayout);
    new TabLayoutMediator(this.tabLayout, this.viewPagerPresets, (tab, position) -> {}).attach();

    this.audioManager = (AudioManager) getSystemService("audio");
    this.progressDialog = new SimpleDialog(this, context.getString(R.string.Connecting));
    this.progressDialog.showProgress(true);

    this.audioManager.requestAudioFocus(this.audioFocusChangeListener, 3, 1);

    this.mServiceIntent = new Intent(this, DabService.class);
    startService(this.mServiceIntent);
    bindService(this.mServiceIntent, this, 1);

    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction("android.hardware.usb.action.USB_DEVICE_DETACHED");
    this.context.registerReceiver(this.usbDetachedBroadcastReceiver, intentFilter);

    this.mLogoDb = LogoDbHelper.getInstance(this);

    onCreateAdditions(savedInstanceState);
  }

  @SuppressLint({"ClickableViewAccessibility"})
  private void onCreateAdditions(Bundle savedInstanceState) {
    // LinearLayout.LayoutParams params;
    // homeKeyReceiver = new HomeKeyReceiver(this);
    sPlayerHandler = new WeakReference<>(this.dabFHandler);

    /*
    Intent startedByIntent = getIntent();
    if (startedByIntent != null) {
      Logger.d("Player startedByIntent=" + startedByIntent.toString());
      Intent mainWasStartedByIntent =
          (Intent) startedByIntent.getParcelableExtra("StartedByIntent");
      if (mainWasStartedByIntent != null) {
        sMainActivityStartIntent = new WeakReference<>(mainWasStartedByIntent);
        String action = mainWasStartedByIntent.getAction();
      }
    }
    */
    showDonateDialog();
  }

  @Override // android.app.ListActivity, android.app.Activity
  protected void onDestroy() {
    super.onDestroy();
    Logger.d("Player:onDestroy");
    // notifyPlayerStopped();
    /*
    if (!this.mProperShutdown) {
      Logger.d("finishTheApp!");
      // finishTheApp();
    }
    */
    unbindService(this);
    // ???
    // stopService(this.mServiceIntent);
    this.audioManager.abandonAudioFocus(this.audioFocusChangeListener);
    this.context.unregisterReceiver(this.usbDetachedBroadcastReceiver);
    this.mLogoDb.closeDb();
    File f = new File(Strings.LOGO_PATH_TMP);

    if (f.exists()) {
      new DirCleaner(f).clean();
      f.delete();
      Logger.d("deleted " + f.getAbsoluteFile());
    } else {
      Logger.d("not exist " + f.getAbsoluteFile());
    }
    sPlayerHandler = null;
    // sMainActivityStartIntent = null;
  }

  @Override // android.app.Activity, android.view.KeyEvent.Callback
  public boolean onKeyDown(int i, KeyEvent keyEvent) {
    Logger.d("onKeyDown " + i);
    switch (i) {
      case KeyEvent.KEYCODE_MEDIA_NEXT: // 87:
        onStationChange_nextWrapper();
        return true;
      case KeyEvent.KEYCODE_MEDIA_PREVIOUS: // 88:
        onStationChange_prevWrapper();
        return true;
    }

    return false;
  }

  @Override // android.app.Activity
  protected void onPause() {
    Logger.d("PlayerActivity:onPause");
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

    this.mProperShutdown = isFinishing();
  }

  @Override // android.app.Activity
  protected void onRestart() {
    Logger.d("PlayerActivity:onRestart");
    super.onRestart();
  }

  @Override // android.app.Activity
  protected void onResume() {
    Logger.d("PlayerActivity:onResume");
    super.onResume();

    SharedPreferencesHelper.getInstance()
        .getSharedPreferences()
        .registerOnSharedPreferenceChangeListener(this);

    this.isInForeground = true;
    // this.f19G = false;

    if (this.playIndex == -1) {
      this.playIndex = SharedPreferencesHelper.getInstance().getInteger("current_playing", 0);
      scrollToPositionViewPagerStations(this.playIndex);
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
      case "menuTop":
        prepareMenuBar(SharedPreferencesHelper.getInstance().getBoolean(key));
        break;
    }
  }

  public void onScanButtonClicked() {
    Logger.d("scan button clicked");

    SimpleDialog.SimpleDialogListener simpleDialogListener =
        new SimpleDialog.SimpleDialogListener() {
          @Override
          public void onClick(boolean positive, int selection) {
            // 0 keep favourites
            // 1 full
            // 2 update
            if (positive) {
              startScan(selection);
            }
          }
        };
    SimpleDialog scanDialog =
        new SimpleDialog(this, context.getString(R.string.scan_mode), simpleDialogListener);
    scanDialog.setPositiveButton(context.getString(R.string.next));
    scanDialog.setNegativeButton(context.getString(R.string.cancel));

    scanDialog.addRadio(context.getResources().getString(R.string.text_favo_scan));
    scanDialog.addRadio(context.getResources().getString(R.string.text_full_scan));
    scanDialog.show();
  }

  public void showDonateDialog() {

    if (!SharedPreferencesHelper.getInstance().getBoolean("showDonate")) {
      return;
    }
    int startCounter = SharedPreferencesHelper.getInstance().getInteger("startCounter") + 1;
    SharedPreferencesHelper.getInstance().setInteger("startCounter", startCounter);
    int donateAfterStarts = SharedPreferencesHelper.getInstance().getInteger("donateAfterStarts");
    if (startCounter < donateAfterStarts) {
      return;
    }

    Intent intent = new Intent();
    intent.setClass(this, DonateActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
    intent.putExtra("source", "PlayerActivity");
    try {
      startActivity(intent);
    } catch (ActivityNotFoundException e) {
      Logger.d(e.toString());
    }
  }

  @Override // android.content.ServiceConnection
  public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
    this.dabService = ((DabServiceBinder) iBinder).getService();
    Logger.d("DAB service connected");
    this.dabService.setPlayerHandler(this.dabFHandler);
    this.dabService.setUsbDevice(this.usbManager, this.usbDevice);
    this.dabService.startDabThread();
    this.dabHandler = this.dabService.getDabHandlerFromDabThread();
    // new AsyncTaskC0118n().executeOnExecutor(Executors.newCachedThreadPool(), new Integer[0]);
    new Thread(new SearchDabHandler()).start();
  }

  @Override // android.content.ServiceConnection
  public void onServiceDisconnected(ComponentName componentName) {
    Logger.d("DAB service disconnected");
    this.dabService = null;
  }

  public void onServiceFollowRequested() {
    Message obtainMessage = this.dabHandler.obtainMessage();
    obtainMessage.what = DabThread.MSGTYPE_START_SERVICE_FOLLOWING; // 23
    // obtainMessage.obj = "";
    // obtainMessage.arg1 = 0;
    obtainMessage.arg1 = this.playIndex;
    this.dabHandler.sendMessage(obtainMessage);
  }

  public void onSettingsButtonClicked() {
    startActivity(new Intent(this.context, SettingsActivity.class));
  }

  public void onStationClicked(int posInList) {
    Logger.d("PlayerActivity:onStationClicked pos " + posInList);
    playStation(posInList);
  }

  private String PTYname(int i) {
    return Strings.PTYname(getApplicationContext(), i);
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

    this.txtDls.setText("");
    // this.motImage.setDefaultImage();
    // arrPty = null;
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
      Logger.d("toastAndFinish: " + toastText);
      Toast.makeText(this.context, toastText, 1).show();
    }
    finishTheApp();
  }
}
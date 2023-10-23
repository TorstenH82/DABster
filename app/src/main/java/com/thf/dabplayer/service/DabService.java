package com.thf.dabplayer.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.media.session.MediaSession;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat.MediaItem;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import androidx.media.MediaBrowserServiceCompat.Result;
import android.view.ViewGroup;
import androidx.media.session.MediaButtonReceiver;
//import com.thf.dabplayer.activity.AlarmReceiver;
import com.thf.dabplayer.activity.MainActivity;
import com.thf.dabplayer.activity.Player;
import com.thf.dabplayer.dab.DabThread;
import com.thf.dabplayer.dab.UsbDeviceConnector;
import com.thf.dabplayer.utils.C0162a;
import com.thf.dabplayer.utils.Credits;
import com.thf.dabplayer.activity.SettingsActivity;
import java.util.ArrayList;
import java.util.List;
import androidx.media.MediaBrowserServiceCompat;
import com.thf.dabplayer.R;

/* renamed from: com.ex.dabplayer.pad.service.DabService */
/* loaded from: classes.dex */
public class DabService extends MediaBrowserServiceCompat {
  public static final String AUDIOFORMAT_AAC = "AAC";
  public static final String AUDIOFORMAT_MP2 = "MP2";
  public static final String EXTRA_AFFECTS_ANDROID_METADATA = "affectsAndroidMetaData";
  public static final String EXTRA_ARTIST = "artist";
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
  public static final String EXTRA_TRACK = "track";
  public static final String META_CHANGED = "com.android.music.metachanged";
  private static final String MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id";
  private static final String MY_MEDIA_ROOT_ID = "media_root_id";
  private static final int ONGOING_NOTIFICATION = 21;
  public static final String SENDER_DAB = "com.thf.dabplayer";
  public static final int SIGNALQUALITY_NONE = 8000;

  private static final int NOTIFCATION_ID = 1447;
  public static String id1 = "dabster_channel_01";

  /* renamed from: b */
  private Handler playerHandler;

  /* renamed from: a */
  private IBinder dabServiceBinder = new DabServiceBinder(this);

  /* renamed from: c */
  private DabThread dabThread = null;

  /* renamed from: d */
  private UsbDevice usbDevice = null;

  /* renamed from: e */
  private UsbManager usbManager = null;
  private Notification.Builder mNotificationBuilder = null;
  private MediaSessionCompat mMediaSession = null;
  private PlaybackStateCompat.Builder mPlaybackStateBuilder = null;
  //private final AlarmReceiver alarm = new AlarmReceiver();

  /* renamed from: com.ex.dabplayer.pad.service.DabService$MediaSessionCallback */
  /* loaded from: classes.dex */
  public class MediaSessionCallback extends MediaSessionCompat.Callback {
    public MediaSessionCallback() {}

    @Override // android.support.p000v4.media.session.MediaSessionCompat.Callback
    public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
      KeyEvent keyEv;
      boolean handled = false;
      if (mediaButtonEvent != null) {
        String action = mediaButtonEvent.getAction();
        if (action != null
            && mediaButtonEvent.getAction().equals("android.intent.action.MEDIA_BUTTON")
            && mediaButtonEvent.hasExtra("android.intent.extra.KEY_EVENT")
            && (keyEv =
                    (KeyEvent)
                        mediaButtonEvent.getParcelableExtra("android.intent.extra.KEY_EVENT"))
                != null) {
          C0162a.m9a("MediaSessionCallback:onMediaButtonEvent: " + keyEv.toString());
          int keyCode = keyEv.getKeyCode();
          int keyAction = keyEv.getAction();
          if (keyAction == 0) {
            switch (keyCode) {
              case 87:
                if (DabService.this.playerHandler != null) {
                  DabService.this.playerHandler.removeMessages(Player.PLAYERMSG_NEXT_STATION);
                  Message m = DabService.this.playerHandler.obtainMessage(Player.PLAYERMSG_NEXT_STATION);
                  DabService.this.playerHandler.sendMessage(m);
                } else {
                  C0162a.m9a("no handler to handle NEXT");
                }
                handled = true;
                break;
              case 88:
                if (DabService.this.playerHandler != null) {
                  DabService.this.playerHandler.removeMessages(Player.PLAYERMSG_PREV_STATION);
                  Message m2 = DabService.this.playerHandler.obtainMessage(Player.PLAYERMSG_PREV_STATION);
                  DabService.this.playerHandler.sendMessage(m2);
                } else {
                  C0162a.m9a("no handler to handle PREV");
                }
                handled = true;
                break;
            }
          }
        }
      }
      return handled || super.onMediaButtonEvent(mediaButtonEvent);
    }

    @Override // android.support.p000v4.media.session.MediaSessionCompat.Callback
    public void onPlay() {
      C0162a.m9a("MediaSessionCallback:onPlay");
      super.onPlay();
    }

    @Override // android.support.p000v4.media.session.MediaSessionCompat.Callback
    public void onPause() {
      C0162a.m9a("MediaSessionCallback:onPause");
      super.onPause();
    }

    @Override // android.support.p000v4.media.session.MediaSessionCompat.Callback
    public void onStop() {
      C0162a.m9a("MediaSessionCallback:onStop");
      super.onStop();
    }

    @Override // android.support.p000v4.media.session.MediaSessionCompat.Callback
    public void onSetRating(RatingCompat rating) {
      C0162a.m9a("MediaSessionCallback:onSetRating");
      super.onSetRating(rating);
    }

    @Override // android.support.p000v4.media.session.MediaSessionCompat.Callback
    public void onSkipToNext() {
      C0162a.m9a("MediaSessionCallback:onSkipToNext");
      if (DabService.this.playerHandler != null) {
        DabService.this.playerHandler.removeMessages(Player.PLAYERMSG_NEXT_STATION);
        Message m = DabService.this.playerHandler.obtainMessage(Player.PLAYERMSG_NEXT_STATION);
        DabService.this.playerHandler.sendMessage(m);
        return;
      }
      C0162a.m9a("no handler to handle NEXT");
    }

    @Override // android.support.p000v4.media.session.MediaSessionCompat.Callback
    public void onSkipToPrevious() {
      C0162a.m9a("MediaSessionCallback:onSkipToPrevious");
      if (DabService.this.playerHandler != null) {
        DabService.this.playerHandler.removeMessages(Player.PLAYERMSG_PREV_STATION);
        Message m = DabService.this.playerHandler.obtainMessage(Player.PLAYERMSG_PREV_STATION);
        DabService.this.playerHandler.sendMessage(m);
        return;
      }
      C0162a.m9a("no handler to handle NEXT");
    }
  }

  /* renamed from: a */
  public Handler getDabHandlerFromDabThread() {
    if (this.dabThread != null) {
      C0162a.m9a("get dab handler");
      return this.dabThread.getDabHandler();
    }
    C0162a.m9a("get dab handler: c=null");
    return null;
  }

  /* renamed from: a */
  public void m16a(Handler playerHandler) {
    C0162a.m9a("service set handler");
    this.playerHandler = playerHandler;
    if (this.dabThread != null) {
      this.dabThread.setPlayerHandler(playerHandler);
    }
  }

  /* renamed from: a */
  public void setUsbDevice(UsbManager usbManager, UsbDevice usbDevice) {
    C0162a.m9a("service set usb device");
    this.usbDevice = usbDevice;
    this.usbManager = usbManager;
  }

  /* renamed from: b */
  public void startDabThread() {
    C0162a.m9a("start dab service");
    if (this.usbDevice != null) {
      this.dabThread =
          new DabThread(
              getApplicationContext(),
              this.playerHandler,
              new UsbDeviceConnector(this.usbManager, this.usbDevice, getApplicationContext()));
      this.dabThread.start();
      return;
    }
    C0162a.m9a("mUsbDevice is null");
    gracefullyStop();
  }

  @Override // android.support.p000v4.media.MediaBrowserServiceCompat, android.app.Service
  public IBinder onBind(Intent intent) {
    C0162a.m9a("service bind");
    if (intent != null) {
      C0162a.m9a(intent.toString());
    }
    return this.dabServiceBinder;
  }

  @Override // android.app.Service
  public boolean onUnbind(Intent intent) {
    C0162a.m9a("service unbind");
    if (intent != null) {
      C0162a.m9a(intent.toString());
      return false;
    }
    return false;
  }

  @Override // android.app.Service
  public int onStartCommand(Intent intent, int flags, int startId) {
    C0162a.m9a("onStartCommand");
    KeyEvent keyEvent = MediaButtonReceiver.handleIntent(this.mMediaSession, intent);
    if (keyEvent == null && this.mNotificationBuilder == null) {
      showNotification();
    }
    return START_STICKY;
  }

  @Override // android.support.p000v4.media.MediaBrowserServiceCompat, android.app.Service
  public void onCreate() {
    C0162a.m9a("DabService:onCreate");
    super.onCreate();
    //this.alarm.setAlarm(this);
    //C0162a.m9a("ALARM IN DABSERVICE LAUNCHED");
    this.mMediaSession = new MediaSessionCompat(getApplicationContext(), "dab");
    this.mMediaSession.setFlags(3);
    this.mPlaybackStateBuilder =
        new PlaybackStateCompat.Builder()
            .setActions(512L)
            .setState(3, 0L, 1.0f, SystemClock.elapsedRealtime());
    this.mMediaSession.setPlaybackState(this.mPlaybackStateBuilder.build());
    this.mMediaSession.setCallback(new MediaSessionCallback());
    setSessionToken(this.mMediaSession.getSessionToken());
    PendingIntent mainIntent =
        PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
    this.mMediaSession.setSessionActivity(mainIntent);
    this.mMediaSession.setActive(true);
  }

  @Override // android.app.Service
  public void onDestroy() {
    C0162a.m9a("DabService:onDestroy");
    super.onDestroy();
    this.mPlaybackStateBuilder =
        new PlaybackStateCompat.Builder(this.mPlaybackStateBuilder.build())
            .setState(0, 0L, 1.0f, SystemClock.elapsedRealtime());
    this.mMediaSession.setActive(false);
    this.mMediaSession.release();
    this.mMediaSession = null;
    gracefullyStop();
  }

  private boolean allowBrowsing(@NonNull String clientPackageName, int clientUid) {
    return false;
  }

  @Override // android.support.p000v4.media.MediaBrowserServiceCompat
  public MediaBrowserServiceCompat.BrowserRoot onGetRoot(
      @NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
    if (allowBrowsing(clientPackageName, clientUid)) {
      C0162a.m9a("DabService:onGetRoot media_root_id");
      return new MediaBrowserServiceCompat.BrowserRoot(MY_MEDIA_ROOT_ID, null);
    }
    C0162a.m9a("DabService:onGetRoot empty_root_id");
    return new MediaBrowserServiceCompat.BrowserRoot(MY_EMPTY_MEDIA_ROOT_ID, null);
  }

  @Override
  public void onLoadChildren(String parentMediaId, Result<List<MediaItem>> result) {

    C0162a.m9a("DabService:onLoadChildren " + parentMediaId);
    if (TextUtils.equals(MY_EMPTY_MEDIA_ROOT_ID, parentMediaId)) {
      result.sendResult(null);
      return;
    }
    List<MediaItem> mediaItems = new ArrayList<>();
    if (MY_MEDIA_ROOT_ID.equals(parentMediaId)) {}
    result.sendResult(mediaItems);
  }

  /*
  @Override // android.support.p000v4.media.MediaBrowserServiceCompat
  public void onLoadChildren(
      String parentMediaId,
      MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>> result) {
    C0162a.m9a("DabService:onLoadChildren " + parentMediaId);
    if (TextUtils.equals(MY_EMPTY_MEDIA_ROOT_ID, parentMediaId)) {
      result.sendResult(null);
      return;
    }
    List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();
    if (MY_MEDIA_ROOT_ID.equals(parentMediaId)) {}
    result.sendResult(mediaItems);
  }
    */

  private void showNotification() {
    PendingIntent mainIntent =
        PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
    PendingIntent settingsIntent =
        PendingIntent.getActivity(this, 0, new Intent(this, SettingsActivity.class), 0);
    PendingIntent nextIntent = MediaButtonReceiver.buildMediaButtonPendingIntent(this, 32L);
    PendingIntent prevIntent = MediaButtonReceiver.buildMediaButtonPendingIntent(this, 16L);
    int iconID = getResourceId("ic_notification", "drawable", getPackageName());
    CharSequence settingsStr =
        getText(getResourceId("btn_text_Settings", "string", getPackageName()));
    if (this.mNotificationBuilder == null) {
      this.mNotificationBuilder = new Notification.Builder(this);
    }
    this.mNotificationBuilder
        .setSmallIcon(iconID)
        .setContentTitle(getText(getResourceId("app_name", "string", getPackageName())))
        .setContentText("")
        .setContentIntent(mainIntent)
        .setChannelId(id1);
    if (Build.VERSION.SDK_INT < 20) {
      this.mNotificationBuilder.addAction(android.R.drawable.ic_media_previous, "", prevIntent);
      this.mNotificationBuilder.addAction(android.R.drawable.ic_media_next, "", nextIntent);
      //this.mNotificationBuilder.addAction(
      //    android.R.drawable.ic_menu_preferences, settingsStr, settingsIntent);
    } else {
      this.mNotificationBuilder.addAction(
          new Notification.Action.Builder(android.R.drawable.ic_media_previous, "", prevIntent)
              .build());
      this.mNotificationBuilder.addAction(
          new Notification.Action.Builder(android.R.drawable.ic_media_next, "", nextIntent)
              .build());
     // this.mNotificationBuilder.addAction(
          //new Notification.Action.Builder(
            //      android.R.drawable.ic_menu_preferences, settingsStr, settingsIntent)
           //   .build());
    }
    if (Build.VERSION.SDK_INT >= 21) {
      this.mNotificationBuilder.setCategory("service");
      this.mNotificationBuilder.setStyle(
          new Notification.MediaStyle()
              .setMediaSession((MediaSession.Token) getSessionToken().getToken())
              .setShowActionsInCompactView(0, 1));
    }
    Notification notification = this.mNotificationBuilder.build();
        
        
    /*    
    LayoutInflater layoutInflater = (LayoutInflater) getSystemService("layout_inflater");
    View settingsInfoView = null;
    if (layoutInflater != null) {
      settingsInfoView =
          layoutInflater.inflate(
              getResourceId("settings_about", "layout", getPackageName()), (ViewGroup) null, false);
    }
*/
    createChannel();
    startForeground(NOTIFCATION_ID, notification);

    // Credits credits = new Credits();
    // credits.credits_show_notification(this, 21, notification, settingsInfoView);
  }

  private void createChannel() {
    NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    NotificationChannel mChannel =
        new NotificationChannel(
            id1,
            getString(R.string.channel_name), // name of the channel
            NotificationManager.IMPORTANCE_LOW); // importance level
    // important level: default is is high on the phone.  high is urgent on the phone.  low is
    // medium, so none is low?
    // Configure the notification channel.
    mChannel.setDescription(getString(R.string.channel_description));
    mChannel.enableLights(true);
    // Sets the notification light color for notifications posted to this channel, if the device
    // supports this feature.
    mChannel.setShowBadge(true);
    nm.createNotificationChannel(mChannel);
  }

  private void clearNotification() {
    NotificationManager nm = (NotificationManager) getSystemService("notification");
    if (nm != null) {
      nm.cancel(NOTIFCATION_ID);
    }
    stopForeground(true);
    this.mNotificationBuilder = null;
  }

  public void updateNotification(@NonNull Intent intent) {
    if (this.mNotificationBuilder != null) {
      String stationName = intent.getStringExtra(EXTRA_STATION);
      if (stationName != null) {
        this.mNotificationBuilder.setContentText(stationName);
      }
      Bitmap logoOrSlsBitmap = (Bitmap) intent.getParcelableExtra(EXTRA_SLSBITMAP);
      if (logoOrSlsBitmap != null) {
        this.mNotificationBuilder.setLargeIcon(logoOrSlsBitmap);
      }
      NotificationManager nm = (NotificationManager) getSystemService("notification");
      if (nm != null) {
        try {
          nm.notify(NOTIFCATION_ID, this.mNotificationBuilder.build());
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }

  private int getResourceId(String pVariableName, String pResourcename, String pPackageName) {
    try {
      return getResources().getIdentifier(pVariableName, pResourcename, pPackageName);
    } catch (Exception e) {
      e.printStackTrace();
      return -1;
    }
  }

  private void gracefullyStop() {
    C0162a.m9a("service stopself");
    clearNotification();
    stopSelf();
  }

  public final MediaSessionCompat getMediaSession() {
    return this.mMediaSession;
  }
}

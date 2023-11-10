/*todo:
- test other fifo buffer

- replace
preferences.getBoolean(SettingsActivity.pref_key_service_link_switch, true);
with
SharedPreferencesHelper.getInstance().getBoolean("serviceFollowing");
*/

package com.thf.dabplayer.dab;

import android.content.Context;
import android.content.Intent;
import android.nfc.NdefRecord;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;
import com.thf.dabplayer.activity.PlayerActivity;
import com.thf.dabplayer.dab.Decode;
import com.thf.dabplayer.service.DabService;
import com.thf.dabplayer.utils.Logger;
import com.thf.dabplayer.utils.RepairEBU;
import com.thf.dabplayer.utils.ServiceFollowing;
import com.thf.dabplayer.utils.SharedPreferencesHelper;
import com.thf.dabplayer.utils.Strings;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/* renamed from: com.ex.dabplayer.pad.dab.f */
/* loaded from: classes.dex */
public class DabThread extends Thread {
  public static final int AUDIOSTATE_DUCK = 202;
  public static final int AUDIOSTATE_PAUSE = 201;
  public static final int AUDIOSTATE_PLAY = 200;
  public static final int MSGTYPE_DAB_DEINIT = 5;
  public static final int MSGTYPE_DAB_HANDLER_STOP = 7;
  public static final int MSGTYPE_DAB_INIT = 2;
  // public static final int MSGTYPE_SELECT_PTY = 20;
  public static final int MSGTYPE_START_PLAY_STATION = 6;
  public static final int MSGTYPE_START_SERVICE_FOLLOWING = 23;
  public static final int MSGTYPE_START_STATION_SCAN = 3;
  // public static final int SCANTYPE_FAVOURITE = 2;
  // public static final int SCANTYPE_FULL = 0;
  // public static final int SCANTYPE_INCREMENTAL = 1;
  public static final int UPDATE_FAVOURITE = 30;
  // public static final int PLAY_FAVOURITE = 300;

  /* renamed from: H */
  // private String f74H;

  /* renamed from: c */
  private Context context;

  /* renamed from: d */
  private Handler playerHandler;

  /* renamed from: e */
  private Handler dabHandler;

  /* renamed from: g */
  // private RingBuffer ringBuffer;

  /* renamed from: i */
  private UsbDeviceConnector usbDeviceConnector;

  /* renamed from: j */
  private DatabaseHelper dbHelper;

  /* renamed from: k */
  private FicThread ficThread;

  /* renamed from: l */
  private MscThread mscThread;

  /* renamed from: r */
  private int freq;

  /* renamed from: w */
  private boolean isOnExit;

  /* renamed from: y */
  private Looper looper;

  /* renamed from: A */
  private int[] arrFreqs = new int[16];

  /* renamed from: B */
  private int[] arrSids = new int[16];

  /* renamed from: C */
  private RingBuffer ficRecorderRingBuffer = null;

  /* renamed from: D */
  private FicRecorder ficRecorder = null;

  /* renamed from: E */
  private boolean isExecutingServiceFollowing = false;

  /* renamed from: G */
  private DabSubChannelInfo dabSubChannelInfo = new DabSubChannelInfo();

  /* renamed from: I */
  private boolean ficRecording = false;

  /* renamed from: a */
  private List stationList = new ArrayList();

  /* renamed from: b */
  // private List<ChannelInfo> channelInfoList = new ArrayList();

  /* renamed from: m */
  private SignalMotDlsMgr mSignalMotDlsMgr = null;

  /* renamed from: n */
  private AacThread aacThread = null;

  /* renamed from: o */
  private Mp2Thread mp2Thread = null;

  /* renamed from: p */
  private DabRecorder dabRecorder = null;

  public static final int AUDIOTYPE_AAC = 0;
  public static final int AUDIOTYPE_MP2 = 1;
  /* renamed from: s */
  private int audioType = 2;

  /* renamed from: u */
  private int f94u = 0;

  /* renamed from: v */
  private boolean tuneOk = false;

  /* renamed from: x */
  private boolean f97x = false;

  /* renamed from: z */
  private boolean f99z = false;
  private int scan_service_count = 0;
  private int total_known_services = 0;

  /* renamed from: h */
  private DabDec dabDec = new DabDec();

  /* renamed from: f */
  private RingBuffer inputRingBuffer = new RingBuffer(20480);

  public DabThread(Context context, Handler handler, UsbDeviceConnector kVar) {
    this.usbDeviceConnector = null;
    this.isOnExit = false;
    this.context = context;
    this.playerHandler = handler;
    this.usbDeviceConnector = kVar;
    this.isOnExit = false;
    this.dbHelper = new DatabaseHelper(this.context);
  }

  /* renamed from: a */
  public Handler getDabHandler() {
    return this.dabHandler;
  }

  public final Context getContext() {
    return this.context;
  }

  public final Handler getPlayerHandler() {
    return this.playerHandler;
  }

  /* renamed from: a */
  private int tune(int frequency) {
    if (frequency == 0) {
      return -1;
    }
    this.freq = frequency;
    this.dabDec.decoder_reset_ensemble_info(0);
    Logger.d("tune frequency:" + frequency);
    this.dabDec.dab_api_set_subid(65);
    int dab_api_tune = this.dabDec.dab_api_tune(frequency);
    if (dab_api_tune != 1) {
      this.tuneOk = false;
      return dab_api_tune;
    }
    Logger.d("tune ok");
    this.tuneOk = true;
    this.dabDec.dab_api_set_subid(64);
    return 1;
  }

  /* JADX INFO: Access modifiers changed from: private */
  /* renamed from: a */
  public void m54a(DabSubChannelInfo subChannelInfo) {
    Toast.makeText(context, "m54a is not implemented", Toast.LENGTH_LONG).show();
    /*
    for (ChannelInfo qVar : this.channelInfoList) {
      if (qVar.freq == subChannelInfo.mFreq
          && qVar.subChannelId == subChannelInfo.mSubChannelId
          && qVar.label.equals(qVar.label)) {
        this.dbHelper.m70a(qVar);
        this.channelInfoList = this.dbHelper.getPresetChannelInfo();
        Message obtainMessage = this.playerHandler.obtainMessage();
        obtainMessage.what = 15;
        obtainMessage.obj = subChannelInfo;
        this.playerHandler.sendMessage(obtainMessage);
        Message obtainMessage2 = this.playerHandler.obtainMessage();
        obtainMessage2.what = 13;
        obtainMessage2.arg1 = this.channelInfoList.size();
        obtainMessage2.obj = this.channelInfoList;
        this.playerHandler.sendMessage(obtainMessage2);
        return;
      }
    }
    ChannelInfo qVar2 = new ChannelInfo();
    qVar2.label = subChannelInfo.mLabel;
    qVar2.freq = subChannelInfo.mFreq;
    qVar2.subChannelId = subChannelInfo.mSubChannelId;
    qVar2.bitrate = subChannelInfo.mBitrate;
    qVar2.type = subChannelInfo.mType;
    this.dbHelper.insertPreset(qVar2);
    Message obtainMessage3 = this.playerHandler.obtainMessage();
    obtainMessage3.what = 17;
    obtainMessage3.obj = subChannelInfo;
    this.playerHandler.sendMessage(obtainMessage3);
    this.channelInfoList = this.dbHelper.getPresetChannelInfo();
    Message obtainMessage4 = this.playerHandler.obtainMessage();
    obtainMessage4.what = 13;
    obtainMessage4.arg1 = this.channelInfoList.size();
    obtainMessage4.obj = this.channelInfoList;
    this.playerHandler.sendMessage(obtainMessage4);
        */
  }

  /* JADX INFO: Access modifiers changed from: private */
  /* renamed from: a */
  /*
    public void m53a(ChannelInfo qVar) {
      if (this.mp2Thread != null) {
        this.mp2Thread.exit();
        this.mp2Thread = null;
      }
      if (this.mSignalMotDlsMgr != null) {
        this.mSignalMotDlsMgr.exit();
        this.mSignalMotDlsMgr = null;
      }
      if (this.aacThread != null) {
        this.aacThread.exit();
        this.aacThread = null;
      }
      if (this.ficThread != null) {
        this.ficThread.exit();
        this.ficThread = null;
      }
      if (this.mscThread != null) {
        this.mscThread.m39a();
      }
      try {
        sleep(500L);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      synchronized (this.dabDec) {
        DabSubChannelInfo subChannelInfo = new DabSubChannelInfo();
        subChannelInfo.mBitrate = qVar.bitrate;
        subChannelInfo.mFreq = qVar.freq;
        subChannelInfo.mSubChannelId = (byte) qVar.subChannelId;
        subChannelInfo.mType = (byte) qVar.type;
        subChannelInfo.mLabel = qVar.label;
        Logger.d("current label:" + subChannelInfo.mLabel);
        Logger.d("current service type:" + ((int) subChannelInfo.mType));
        this.dabDec.dab_api_set_subid(65);
        this.dabDec.dab_api_set_msc_size((short) (subChannelInfo.mBitrate * 3));
        this.dabDec.dab_api_tune(subChannelInfo.mFreq);
        Logger.d("current frequency[a]: " + subChannelInfo.mFreq);
        this.dabDec.dab_api_set_subid(subChannelInfo.mSubChannelId);
        ServiceFollowing.manTune(subChannelInfo);
        this.mscThread = new MscThread(subChannelInfo.mBitrate);
        this.mscThread.start();
        this.audioType = subChannelInfo.mType == 63 ? AUDIOTYPE_AAC : AUDIOTYPE_MP2;
        if (this.audioType == AUDIOTYPE_AAC) {
          Logger.d("play aac audio");
          this.aacThread = new AacThread(this.context, this.inputRingBuffer);
          this.aacThread.start();
        } else if (this.audioType == AUDIOTYPE_MP2) {
          Logger.d("play mp2 audio");
          this.mp2Thread = new Mp2Thread(this.context, this.inputRingBuffer);
          this.mp2Thread.start();
        } else {
          Logger.d("unknown audio type");
        }
        Logger.d("bitrate: " + subChannelInfo.mBitrate);
      }
      if (this.mSignalMotDlsMgr == null) {
        this.mSignalMotDlsMgr = new SignalMotDlsMgr();
        this.mSignalMotDlsMgr.start();
      }
    }
  */
  /* renamed from: a */
  /*
  private void getStationLogoFromDabBin(String str) {
    int dab_get_image;
    String dabBinFile = this.context.getFilesDir().getAbsolutePath() + "/dab.bin";
    String imageFile = this.context.getFilesDir().getAbsolutePath() + ("/" + str + ".png");
    if (new File(dabBinFile).exists()) {
      File file = new File(imageFile);
      if (file.exists()) {
        file.delete();
      }
      Logger.d("dab_get_image '" + str + "'");
      Logger.d(" -> " + file.getAbsolutePath());
      synchronized (this.dabDec) {
        dab_get_image =
            this.dabDec.dab_get_image(dabBinFile.getBytes(), str.getBytes(), imageFile.getBytes());
      }
      Message obtainMessage = this.playerHandler.obtainMessage();
      obtainMessage.what = 10;
      if (dab_get_image == 0) {
        Logger.d("get service logo");
        obtainMessage.obj = TextUtils.concat(str, ".png");
        this.playerHandler.sendMessage(obtainMessage);
        return;
      }
      Logger.d("no service logo '" + str + "' in dab.bin, dab_get_image=" + dab_get_image);
    }
  }
  */
  /* JADX INFO: Access modifiers changed from: private */
  /* renamed from: b */
  public void scan(int scan_which_region, int scan_type) {

    Logger.d("add assets to logo database");
    new LogoDbAssets(context).execute();

    Logger.d("scan location: " + scan_which_region + ", type: " + scan_type);
    int priority = getPriority();
    setPriority(1);
    synchronized (this.dabDec) {
      this.dabDec.decoder_reset_ensemble_info(0);
    }
    if (scan_type == 0) {
      this.dbHelper.deleteNonFavs();
    } else if (scan_type == 1) {
      this.dbHelper.deleteAllFromServiceTbl();
    }
    this.stationList.clear();
    // this.dbHelper.m72a(32);
    this.dabDec.decoder_fic_reset(1);
    if (this.mscThread != null) {
      this.mscThread.exit();
      this.mscThread = null;
    }
    if (this.ficThread == null) {
      this.ficThread = new DabThread.FicThread();
      this.ficRecording = SharedPreferencesHelper.getInstance().getBoolean("ficRecording");
      this.ficThread.start();
    }
    if (this.mSignalMotDlsMgr != null) {
      this.mSignalMotDlsMgr.exit();
      this.mSignalMotDlsMgr = null;
    }
    this.dabDec.dab_api_set_subid(64);
    switch (scan_which_region) {
      case 0:
        Logger.d("scan euro");
        scanFrequencies(new ScanFrequencies().euro);
        break;
      case 1:
        Logger.d("scan china");
        scanFrequencies(new ScanFrequencies().china);
        break;
      case 2:
        Logger.d("scan korea");
        scanFrequencies(new ScanFrequencies().korea);
        break;
      default:
        ScanFrequencies cv = new ScanFrequencies();
        int[] all = new int[cv.euro.length + cv.china.length + cv.korea.length];
        System.arraycopy(cv.euro, 0, all, 0, cv.euro.length);
        System.arraycopy(cv.china, 0, all, cv.euro.length, cv.china.length);
        System.arraycopy(cv.korea, 0, all, cv.euro.length + cv.china.length, cv.korea.length);
        Logger.d("scan all");
        scanFrequencies(all);
        break;
    }
    setPriority(priority);
    if (this.ficThread != null) {
      this.ficThread.exit();
      this.ficThread = null;
    }
  }

  private void sendHardwareFailure(String text) {
    Message obtainMessage = this.playerHandler.obtainMessage();
    obtainMessage.what = PlayerActivity.PLAYERMSG_HW_FAILURE;
    obtainMessage.obj = text;
    this.playerHandler.sendMessage(obtainMessage);
  }

  private String fic_db_filename() {
    return this.context.getFilesDir().getAbsolutePath() + File.separatorChar + "fic.db";
  }

  /* JADX INFO: Access modifiers changed from: private */
  public void erase_fic_db() {
    Logger.d("erase fic.db");
    File file = new File(fic_db_filename());
    if (file.exists() && file.delete()) {
      Logger.d("erase fic.db happened");
    }
  }

  /* JADX INFO: Access modifiers changed from: private */
  /* renamed from: c */
  public void dabInit() {
    Logger.d("dab init");
    String filename = fic_db_filename();
    String usb_devicename = this.usbDeviceConnector.getUsbDeviceName();
    File file = new File(filename);
    try {
      if (file.exists() && !file.delete()) {
        Logger.d("delete failed: " + filename);
      }
      if (!file.createNewFile()) {
        Logger.d("createNewFile failed: " + filename);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    Logger.d("reset mode:1");
    this.dabDec.dab_api_init(
        this.usbDeviceConnector.getUsbConFileDescriptor(),
        usb_devicename.getBytes(),
        usb_devicename.getBytes().length);
    this.dabDec.decoder_fic_init(filename.getBytes());
    this.dabDec.decoder_fic_reset(1);
    if (this.dabDec.dab_api_power_on(0) != 1) {
      Logger.d("power on fail");
      sendHardwareFailure("Failed to power on DAB hardware");
    } else if (this.dabDec.dab_api_echo(0) != 1) {
      Logger.d("echo fail");
      sendHardwareFailure("Failed echo test with DAB hardware");
    } else if (this.dabDec.dab_api_version(0) != 1) {
      Logger.d("get version fail");
      sendHardwareFailure("Failed to get version from DAB hardware");
    } else {
      // send stationlist to player
      this.stationList = this.dbHelper.getStationList();
      Message obtainMessage = this.playerHandler.obtainMessage();
      obtainMessage.what = PlayerActivity.PLAYERMSG_NEW_LIST_OF_STATIONS; // 1;
      obtainMessage.arg1 = this.stationList.size();
      obtainMessage.obj = this.stationList;
      this.playerHandler.sendMessage(obtainMessage);

      Message obtainMessage3 = this.playerHandler.obtainMessage();
      obtainMessage3.what = PlayerActivity.PLAYERMSG_DAB_THREAD_INITIALIZED;
      this.playerHandler.sendMessage(obtainMessage3);

      // select favourites and send to player
      List<DabSubChannelInfo> memoryList = this.dbHelper.getFavorites();
      if (memoryList.size() > 0) {
        Message obtainMessage4 = this.playerHandler.obtainMessage();
        obtainMessage4.what = PlayerActivity.PLAYERMSG_SET_STATIONMEMORY;
        obtainMessage4.obj = memoryList;
        this.playerHandler.sendMessage(obtainMessage4);
      }
    }
  }

  /* JADX INFO: Access modifiers changed from: private */
  /* renamed from: d */
  public void deInit() {
    if (this.f94u == 1 && this.dabDec.dab_api_power_off(0) != 1) {
      Logger.d("power off fail");
    }
    this.dabDec.decoder_fic_deinit();
    this.dabDec.dab_api_close(0);
    this.dbHelper.closeDb();
    this.usbDeviceConnector.closeUsbDeviceConnection();
  }

  /* JADX INFO: Access modifiers changed from: private */
  /* renamed from: d */
  public void playStation(int i) {
    int dab_get_new_pgm_bitrate;
    List<DabSubChannelInfo> stationList = this.dbHelper.getStationList();
    int size = stationList.size() - 1;
    Logger.d("play audio:" + i + "/" + size);
    if (i <= size) {
      if (this.mSignalMotDlsMgr != null) {
        this.mSignalMotDlsMgr.exit();
        this.mSignalMotDlsMgr = null;
      }
      if (this.mp2Thread != null) {
        this.mp2Thread.exit();
        this.mp2Thread = null;
      }
      if (this.aacThread != null) {
        this.aacThread.exit();
        this.aacThread = null;
      }
      if (this.ficThread != null) {
        this.ficThread.exit();
        this.ficThread = null;
      }
      if (this.mscThread != null) {
        this.mscThread.exit();
        this.mscThread = null;
      }
      this.isExecutingServiceFollowing = false;
      synchronized (this.dabDec) {
        DabSubChannelInfo subChannelInfo = stationList.get(i);
        Logger.d("current label:" + subChannelInfo.mLabel);
        Logger.d("bitrate:" + subChannelInfo.mBitrate);
        Logger.d("subchid:" + ((int) subChannelInfo.mSubChannelId));
        // this.f74H = subChannelInfo.mLabel;
        // String temp = C0165c.m1a(subChannelInfo.mLabel.trim());
        // getStationLogoFromDabBin(temp);
        this.dabSubChannelInfo.mAbbreviatedFlag = subChannelInfo.mAbbreviatedFlag;
        this.dabSubChannelInfo.mBitrate = subChannelInfo.mBitrate;
        this.dabSubChannelInfo.mEID = subChannelInfo.mEID;
        this.dabSubChannelInfo.mEnsembleLabel = subChannelInfo.mEnsembleLabel;
        this.dabSubChannelInfo.mFreq = subChannelInfo.mFreq;
        this.dabSubChannelInfo.mLabel = subChannelInfo.mLabel;
        this.dabSubChannelInfo.mPS = subChannelInfo.mPS;
        this.dabSubChannelInfo.mPty = subChannelInfo.mPty;
        this.dabSubChannelInfo.mSCID = subChannelInfo.mSCID;
        this.dabSubChannelInfo.mSID = subChannelInfo.mSID;
        this.dabSubChannelInfo.mSubChannelId = subChannelInfo.mSubChannelId;
        this.dabSubChannelInfo.mType = subChannelInfo.mType;
        this.f97x = true;
        this.freq = subChannelInfo.mFreq;
        int size2 =
            this.dabDec.dab_get_pgm_index(
                this.freq,
                subChannelInfo.mSID,
                (byte) subChannelInfo.mSCID,
                subChannelInfo.mSubChannelId);
        Logger.d("pgm index: " + size2);
        if (size2 >= 0 && tune(this.freq) == 1) {
          long currentTimeMillis = System.currentTimeMillis();
          byte[] bArr = new byte[384];
          while (true) {
            if (this.dabDec.dab_api_get_fic_data(bArr) > 0
                && (dab_get_new_pgm_bitrate =
                        this.dabDec.dab_get_new_pgm_bitrate(size2, this.freq, bArr))
                    > 0) {
              subChannelInfo.mBitrate = dab_get_new_pgm_bitrate;
              Logger.d("new bitrate: " + subChannelInfo.mBitrate);
              break;
            } else if (System.currentTimeMillis() - currentTimeMillis >= 1500) {
              break;
            }
          }
        }
        this.dabDec.dab_api_set_subid(65);
        if (this.f94u == 1) {
          this.dabDec.dab_api_set_msc_size((short) (subChannelInfo.mBitrate * 3));
          this.dabDec.dab_api_tune(subChannelInfo.mFreq);
          this.dabDec.dab_api_set_subid(subChannelInfo.mSubChannelId);
        } else {
          this.dabDec.dab_api_set_subid(subChannelInfo.mSubChannelId);
          this.dabDec.dab_api_tune(subChannelInfo.mFreq);
          this.dabDec.dab_api_set_msc_size((short) (subChannelInfo.mBitrate * 3));
        }
        Logger.d("current frequency[d]: " + subChannelInfo.mFreq);
        this.mscThread = new MscThread(subChannelInfo.mBitrate);
        this.mscThread.start();
        this.audioType = subChannelInfo.mType == 63 ? AUDIOTYPE_AAC : AUDIOTYPE_MP2;
        if (this.audioType == AUDIOTYPE_AAC) {
          Logger.d("play aac audio");
          this.aacThread = new AacThread(this.context, this.inputRingBuffer);
          this.aacThread.start();
        } else if (this.audioType == AUDIOTYPE_MP2) {
          Logger.d("play mp2 audio");
          this.mp2Thread = new Mp2Thread(this.context, this.inputRingBuffer);
          this.mp2Thread.start();
        } else {
          Logger.d("unknown audio type");
        }
        Logger.d("bitrate: " + subChannelInfo.mBitrate);
        if (ServiceFollowing.is_enabled()) {
          ServiceLink sf = new ServiceLink(this.dabDec);
          sf.read(subChannelInfo, this.arrFreqs, this.arrSids);
          ServiceFollowing.manTune(subChannelInfo, this.arrFreqs, this.arrSids);
          notifyNewStationPlaying(subChannelInfo, i, stationList.size());
        }
      }
      if (this.mSignalMotDlsMgr == null) {
        this.mSignalMotDlsMgr = new SignalMotDlsMgr();
        this.mSignalMotDlsMgr.start();
      }
    }
  }

  private void scanFrequencies(int[] freqs) {
    this.total_known_services = this.dbHelper.getStationCount();
    this.scan_service_count = 0;
    showSearchIcon();
    notifyScanning(this.total_known_services);
    for (int i = 0; i < freqs.length; i++) {
      int progress = (i * 100) / freqs.length;
      Message obtainMessage = this.playerHandler.obtainMessage();
      obtainMessage.what = PlayerActivity.PLAYERMSG_SCAN_PROGRESS_UPDATE; // 0;
      obtainMessage.arg1 = progress;
      obtainMessage.arg2 = freqs[i];
      this.playerHandler.sendMessage(obtainMessage);
      notifyScanning(freqs[i], this.total_known_services);
      if (tune(freqs[i]) == 1) {
        m45i();
      } else {
        Logger.d("scanning: tune failed");
        try {
          sleep(300L);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      if (this.isOnExit) {
        return;
      }
    }
    notifyScanningDone(this.total_known_services);
    refreshStationList();
    Message obtainMessage2 = this.playerHandler.obtainMessage();
    obtainMessage2.what = PlayerActivity.PLAYERMSG_SCAN_FINISHED; // 99;
    obtainMessage2.arg1 = this.scan_service_count;
    this.playerHandler.sendMessage(obtainMessage2);

    // select favourites and send to player
    List<DabSubChannelInfo> memoryList = this.dbHelper.getFavorites();
    if (memoryList.size() > 0) {
      Message obtainMessage4 = this.playerHandler.obtainMessage();
      obtainMessage4.what = PlayerActivity.PLAYERMSG_SET_STATIONMEMORY;
      obtainMessage4.obj = memoryList;
      this.playerHandler.sendMessage(obtainMessage4);
    }
  }

  /* renamed from: i */
  private void m45i() {
    this.f99z = false;
    for (int i = 0; i < 500; i++) {
      synchronized (this.dabDec) {
        this.dabDec.decoder_fic_get_usage();
      }
      if (!this.f99z) {
        try {
          Thread.sleep(50L, 0);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      } else {
        return;
      }
    }
  }

  private void startServiceFollowing(int stationIndex) {
    if (this.isExecutingServiceFollowing) {
      Logger.d("already service following");
      return;
    }
    this.isExecutingServiceFollowing = true;
    Logger.d("activated service following");
    boolean was_success = false;
    byte[] fic_data = new byte[384];
    int n_usage_stable = 0;
    DabSubChannelInfo subChannelInfo = new DabSubChannelInfo();
    if (this.mp2Thread != null) {
      this.mp2Thread.exit();
      this.mp2Thread = null;
    }
    if (this.aacThread != null) {
      this.aacThread.exit();
      this.aacThread = null;
    }
    if (this.ficThread != null) {
      this.ficThread.exit();
      this.ficThread = null;
    }
    if (this.mscThread != null) {
      this.mscThread.exit();
      this.mscThread = null;
    }
    showSearchIcon();
    Logger.d("service linking");
    synchronized (this.dabDec) {
      int i2 = this.freq;
      ServiceFollowing serviceFollowing = new ServiceFollowing(this.arrFreqs, this.arrSids);
      while (!was_success) {
        int try_freq = serviceFollowing.next_frequency();
        if (try_freq <= 0) {
          break;
        }
        subChannelInfo.mFreq = try_freq;
        Logger.d("service follow freq:" + try_freq);
        this.dabDec.dab_api_set_subid(64);
        int i;
        if (serviceFollowing.change_frequency()) {
          Message obtainMessage = this.playerHandler.obtainMessage();
          obtainMessage.what = MSGTYPE_START_SERVICE_FOLLOWING;
          obtainMessage.arg1 = 0;
          obtainMessage.obj = String.valueOf(try_freq);
          this.playerHandler.sendMessage(obtainMessage);
          int a = tune(try_freq);

          if (a == -1) {
            try {
              sleep(100);
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
            a = tune(try_freq);
          }
          Logger.d("service follow tune " + try_freq + ":" + a);
          if (a == 1) {
            Integer decoder_fic_get_usage = null;
            this.dabDec.decoder_fic_reset(1);

            long currentTimeMillis = System.currentTimeMillis();
            boolean stationsFound = false;
            n_usage_stable = 0;
            do {

              try {
                sleep(10);
              } catch (InterruptedException e2) {
                e2.printStackTrace();
              }
              int dab_api_get_fic_data = this.dabDec.dab_api_get_fic_data(fic_data);
              if (dab_api_get_fic_data > 0) {
                this.dabDec.decoder_fic_parse(fic_data, dab_api_get_fic_data, try_freq);
                Integer new_decoder_fic_get_usage =
                    this.dabDec.decoder_fic_get_usage() != 0 ? 1 : null;
                if (new_decoder_fic_get_usage != decoder_fic_get_usage) {
                  decoder_fic_get_usage = new_decoder_fic_get_usage;
                  n_usage_stable = 0;
                } else {
                  n_usage_stable++;
                }

                if (n_usage_stable >= 10) {
                  int decoder_fic_get_service_count = this.dabDec.decoder_fic_get_service_count();
                  Logger.d("servicefollow stations: " + decoder_fic_get_service_count);

                  for (i = 0; i < decoder_fic_get_service_count; i++) {
                    this.dabDec.decoder_fic_get_subch_info(subChannelInfo, (char) i);
                    stationsFound = true;
                    if (serviceFollowing.find_sid(subChannelInfo.mSID)) {
                      was_success = true;
                      break;
                    }
                  }
                }
              }
              if (stationsFound) {
                break;
              }
            } while (System.currentTimeMillis() - currentTimeMillis < 1500);

            Logger.d("servicefollow usage stable: " + n_usage_stable);
            this.dabDec.decoder_fic_reset(0);
            continue;
          } else {
            // tune failed
            continue;
          }
        } else {
          int decoder_fic_get_service_count = this.dabDec.decoder_fic_get_service_count();
          for (i = 0; i < decoder_fic_get_service_count; i++) {
            this.dabDec.decoder_fic_get_subch_info(subChannelInfo, (char) i);
            if (serviceFollowing.find_sid(subChannelInfo.mSID)) {
              was_success = true;
              // continue;
              break;
            }
          }
          // continue;
        }
      }

      if (was_success) {
        RepairEBU.fixLabels(subChannelInfo);
        Logger.d("servicefollow label:" + subChannelInfo.mLabel);
        Logger.d("bitrate:" + subChannelInfo.mBitrate);
        Logger.d("subchid:" + subChannelInfo.mSubChannelId);
        this.dabSubChannelInfo.mFreq = subChannelInfo.mFreq;
        this.dabSubChannelInfo.mAbbreviatedFlag = subChannelInfo.mAbbreviatedFlag;
        this.dabSubChannelInfo.mBitrate = subChannelInfo.mBitrate;
        this.dabSubChannelInfo.mEID = subChannelInfo.mEID;
        this.dabSubChannelInfo.mPS = subChannelInfo.mPS;
        this.dabSubChannelInfo.mEnsembleLabel = subChannelInfo.mEnsembleLabel;
        this.dabSubChannelInfo.mLabel = subChannelInfo.mLabel;
        this.dabSubChannelInfo.mPty = subChannelInfo.mPty;
        this.dabSubChannelInfo.mSCID = subChannelInfo.mSCID;
        this.dabSubChannelInfo.mSID = subChannelInfo.mSID;
        this.dabSubChannelInfo.mSubChannelId = subChannelInfo.mSubChannelId;
        this.dabSubChannelInfo.mType = subChannelInfo.mType;
      } else {
        this.freq = i2;
        this.dabSubChannelInfo.mFreq = this.freq;
      }
      this.f97x = true;
      this.dabDec.dab_api_set_subid(65);
      if (this.f94u == 1) {
        this.dabDec.dab_api_set_msc_size((short) (this.dabSubChannelInfo.mBitrate * 3));
        this.dabDec.dab_api_tune(this.dabSubChannelInfo.mFreq);
        this.dabDec.dab_api_set_subid(this.dabSubChannelInfo.mSubChannelId);
      } else {
        this.dabDec.dab_api_set_subid(this.dabSubChannelInfo.mSubChannelId);
        this.dabDec.dab_api_tune(this.dabSubChannelInfo.mFreq);
        this.dabDec.dab_api_set_msc_size((short) (this.dabSubChannelInfo.mBitrate * 3));
      }
      Logger.d("current frequency[j]: " + this.dabSubChannelInfo.mFreq);
      this.mscThread = new MscThread(this.dabSubChannelInfo.mBitrate);
      this.mscThread.start();
      this.audioType = this.dabSubChannelInfo.mType == (byte) 63 ? AUDIOTYPE_AAC : AUDIOTYPE_MP2;
      if (this.audioType == AUDIOTYPE_AAC) {
        Logger.d("play aac audio");
        this.aacThread = new AacThread(this.context, this.inputRingBuffer);
        this.aacThread.start();
      } else if (this.audioType == AUDIOTYPE_MP2) {
        Logger.d("play mp2 audio");
        this.mp2Thread = new Mp2Thread(this.context, this.inputRingBuffer);
        this.mp2Thread.start();
      } else {
        Logger.d("unknown audio type");
      }
      Logger.d("bitrate: " + this.dabSubChannelInfo.mBitrate);
      if (was_success) {
        new ServiceLink(this.dabDec).read(this.dabSubChannelInfo, this.arrFreqs, this.arrSids);
        ServiceFollowing.autoTune(this.dabSubChannelInfo, this.arrFreqs, this.arrSids);
      } else {
        ServiceFollowing.autoTune(this.dabSubChannelInfo);
      }
    }
    if (this.mSignalMotDlsMgr == null) {
      this.mSignalMotDlsMgr = new SignalMotDlsMgr();
      this.mSignalMotDlsMgr.start();
    }
    Message obtainMessage2 = this.playerHandler.obtainMessage();
    obtainMessage2.what = PlayerActivity.PLAYERMSG_HIDE_SERVICE_FOLLOWING;
    obtainMessage2.arg1 = this.dabSubChannelInfo.mFreq;
    obtainMessage2.obj = "";
    this.playerHandler.sendMessage(obtainMessage2);
    if (was_success) {
      updateStationPlaying(this.dabSubChannelInfo);
      Logger.d("service following done");
      return;
    }
    Logger.d("service following fail, wait 5 secs");
    try {
      sleep(5000);
    } catch (InterruptedException e3) {
      e3.printStackTrace();
    }
    this.isExecutingServiceFollowing = false;
  }

  /* renamed from: a */
  public void setPlayerHandler(Handler handler) {
    this.playerHandler = handler;
  }

  /* renamed from: b */
  public void exit() {
    Logger.d("dab thread exit");
    this.isOnExit = true;
    if (this.mSignalMotDlsMgr != null) {
      this.mSignalMotDlsMgr.exit();
      this.mSignalMotDlsMgr = null;
    }
    if (this.mp2Thread != null) {
      this.mp2Thread.exit();
      this.mp2Thread = null;
    }
    if (this.aacThread != null) {
      this.aacThread.exit();
      this.aacThread = null;
    }
    if (this.ficThread != null) {
      this.ficThread.exit();
      this.ficThread = null;
    }
    if (this.mscThread != null) {
      this.mscThread.exit();
      this.mscThread = null;
    }
  }

  @Override // java.lang.Thread, java.lang.Runnable
  public void run() {
    Logger.d("dab thread run");
    this.ficRecording = SharedPreferencesHelper.getInstance().getBoolean("ficRecording");
    if (this.ficRecording) {
      Logger.d("FIC recording enabled");
    }
    Looper.prepare();
    this.looper = Looper.myLooper();
    this.dabHandler = new DabThread.DabHandler();
    Looper.loop();
    if (this.mSignalMotDlsMgr != null) {
      this.mSignalMotDlsMgr.exit();
      this.mSignalMotDlsMgr = null;
    }
    if (this.ficThread != null) {
      this.ficThread.exit();
      this.ficThread = null;
    }
    if (this.mscThread != null) {
      this.mscThread.exit();
      this.mscThread = null;
    }
  }

  public void deleteStationAndUpdateList(DabSubChannelInfo subChannelInfo) {
    this.dbHelper.delete(subChannelInfo);
    refreshStationList();
  }

  public void refreshStationList() {
    Logger.d("refreshStationList");
    this.stationList = this.dbHelper.getStationList();
    Message obtainMessage = this.playerHandler.obtainMessage();
    obtainMessage.what = PlayerActivity.PLAYERMSG_NEW_STATION_LIST; // 18
    obtainMessage.arg1 = this.stationList.size();
    obtainMessage.obj = this.stationList;
    this.playerHandler.sendMessage(obtainMessage);
  }

  private void notifyNewStationPlaying(
      DabSubChannelInfo info, int currentPlayingPos, int numStations) {
    Intent intent = new Intent(DabService.META_CHANGED);
    intent.putExtra(DabService.EXTRA_SENDER, DabService.SENDER_DAB);
    intent.putExtra(DabService.EXTRA_ID, currentPlayingPos + 1);
    intent.putExtra(DabService.EXTRA_NUMSTATIONS, numStations);
    intent.putExtra(DabService.EXTRA_ARTIST, info.mLabel);
    intent.putExtra(DabService.EXTRA_TRACK, "");
    intent.putExtra(DabService.EXTRA_STATION, info.mLabel);
    intent.putExtra(DabService.EXTRA_SERVICEID, info.mSID);
    intent.putExtra(DabService.EXTRA_FREQUENCY_KHZ, info.mFreq);
    intent.putExtra(DabService.EXTRA_PTY, Strings.PTYname(this.context, info.mPty));
    intent.putExtra(DabService.EXTRA_BITRATE, info.mBitrate);
    intent.putExtra(DabService.EXTRA_ENSEMBLE_NAME, info.mEnsembleLabel);
    intent.putExtra(DabService.EXTRA_ENSEMBLE_ID, info.mEID);
    intent.putExtra(DabService.EXTRA_AFFECTS_ANDROID_METADATA, true);
    Message obtainMessage = this.playerHandler.obtainMessage();
    obtainMessage.what = PlayerActivity.PLAYERMSG_STATIONINFO_INTENT;
    obtainMessage.obj = intent;
    this.playerHandler.sendMessage(obtainMessage);
  }

  private void updateStationPlaying(DabSubChannelInfo info) {
    Intent intent = new Intent(DabService.META_CHANGED);
    intent.putExtra(DabService.EXTRA_SENDER, DabService.SENDER_DAB);
    intent.putExtra(DabService.EXTRA_STATION, info.mLabel);
    intent.putExtra(DabService.EXTRA_SERVICEID, info.mSID);
    intent.putExtra(DabService.EXTRA_FREQUENCY_KHZ, info.mFreq);
    intent.putExtra(DabService.EXTRA_PTY, Strings.PTYname(this.context, info.mPty));
    intent.putExtra(DabService.EXTRA_BITRATE, info.mBitrate);
    intent.putExtra(DabService.EXTRA_ENSEMBLE_NAME, info.mEnsembleLabel);
    intent.putExtra(DabService.EXTRA_ENSEMBLE_ID, info.mEID);
    intent.putExtra(DabService.EXTRA_AFFECTS_ANDROID_METADATA, true);
    Message obtainMessage = this.playerHandler.obtainMessage();
    obtainMessage.what = PlayerActivity.PLAYERMSG_STATIONINFO_INTENT;
    obtainMessage.obj = intent;
    this.playerHandler.sendMessage(obtainMessage);
  }

  private void notifyScanning(int numStations) {
    Intent intent = new Intent(DabService.META_CHANGED);
    intent.putExtra(DabService.EXTRA_SENDER, "");
    intent.putExtra(DabService.EXTRA_ID, 0);
    intent.putExtra(DabService.EXTRA_NUMSTATIONS, numStations);
    intent.putExtra(DabService.EXTRA_ARTIST, "");
    intent.putExtra(DabService.EXTRA_TRACK, "");
    intent.putExtra(DabService.EXTRA_STATION, "");
    intent.putExtra(DabService.EXTRA_SERVICEID, 0);
    intent.putExtra(DabService.EXTRA_PTY, "");
    intent.putExtra(DabService.EXTRA_BITRATE, 0);
    intent.putExtra(DabService.EXTRA_ENSEMBLE_NAME, "");
    intent.putExtra(DabService.EXTRA_ENSEMBLE_ID, 0);
    intent.putExtra(DabService.EXTRA_SIGNALQUALITY, -1);
    intent.putExtra(DabService.EXTRA_SERVICEFOLLOWING, "");
    intent.putExtra(DabService.EXTRA_SERVICELOG, "");
    intent.putExtra(DabService.EXTRA_AUDIOFORMAT, "");
    Message obtainMessage = this.playerHandler.obtainMessage();
    obtainMessage.what = PlayerActivity.PLAYERMSG_STATIONINFO_INTENT;
    obtainMessage.obj = intent;
    this.playerHandler.sendMessage(obtainMessage);
  }

  private void notifyScanning(int frequency, int numStations) {
    Intent intent = new Intent(DabService.META_CHANGED);
    intent.putExtra(DabService.EXTRA_SENDER, "");
    intent.putExtra(DabService.EXTRA_NUMSTATIONS, numStations);
    intent.putExtra(DabService.EXTRA_FREQUENCY_KHZ, frequency);
    Message obtainMessage = this.playerHandler.obtainMessage();
    obtainMessage.what = PlayerActivity.PLAYERMSG_STATIONINFO_INTENT;
    obtainMessage.obj = intent;
    this.playerHandler.sendMessage(obtainMessage);
  }

  private void notifyScanningDone(int numStations) {
    Intent intent = new Intent(DabService.META_CHANGED);
    intent.putExtra(DabService.EXTRA_SENDER, "");
    intent.putExtra(DabService.EXTRA_NUMSTATIONS, numStations);
    intent.putExtra(DabService.EXTRA_FREQUENCY_KHZ, 0);
    Message obtainMessage = this.playerHandler.obtainMessage();
    obtainMessage.what = PlayerActivity.PLAYERMSG_STATIONINFO_INTENT;
    obtainMessage.obj = intent;
    this.playerHandler.sendMessage(obtainMessage);
  }

  private void showSearchIcon() {
    Message obtainMessage = this.playerHandler.obtainMessage();
    obtainMessage.what = 11;
    obtainMessage.arg1 = -1;
    this.playerHandler.sendMessage(obtainMessage);
  }

  void setAudioState(int state) {
    if (this.aacThread != null) {
      this.aacThread.setAudioState(state);
    }
    if (this.mp2Thread != null) {
      this.mp2Thread.setAudioState(state);
    }
  }

  /* renamed from: com.ex.dabplayer.pad.dab.f$g */
  /* loaded from: classes.dex */
  class DabHandler extends Handler {
    DabHandler() {}

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    @Override // android.os.Handler
    public void handleMessage(Message message) {
      switch (message.what) {
          // here!
        case MSGTYPE_DAB_INIT: // 2:
          DabThread.this.dabInit();
          return;
        case MSGTYPE_START_STATION_SCAN:
          DabThread.this.scan(message.arg1, message.arg2);
          return;
        case MSGTYPE_DAB_DEINIT: // 5
          Logger.d("deinit");
          DabThread.this.isOnExit = true;
          DabThread.this.deInit();
          DabThread.this.playerHandler = null;
          if (message.arg1 == 1) {
            DabThread.this.erase_fic_db();
          }
          break;
        case MSGTYPE_START_PLAY_STATION:
          if (!DabThread.this.isOnExit) {
            ServiceFollowing.update_enabled_status(DabThread.this.getContext());
            DabThread.this.playStation(message.arg1);
            return;
          }
          return;
        case MSGTYPE_DAB_HANDLER_STOP:
          Logger.d("DAB handler stop");
          if (DabThread.this.mSignalMotDlsMgr != null) {
            DabThread.this.mSignalMotDlsMgr.exit();
            DabThread.this.mSignalMotDlsMgr = null;
          }
          if (DabThread.this.ficThread != null) {
            DabThread.this.ficThread.exit();
            DabThread.this.ficThread = null;
          }
          if (DabThread.this.mscThread != null) {
            DabThread.this.mscThread.exit();
            DabThread.this.mscThread = null;
          }
          if (DabThread.this.dabRecorder != null && DabThread.this.dabRecorder.isAlive()) {
            DabThread.this.dabRecorder.m59a();
            return;
          }
          return;
        case 8:
          break;
        case 16:
          Toast.makeText(context, "case 16!", Toast.LENGTH_SHORT).show();
          DabThread.this.m54a((DabSubChannelInfo) message.obj);
          return;
        case 20:
          // DabThread.this.m48c(message.arg1);
          return;
        case MSGTYPE_START_SERVICE_FOLLOWING:
          DabThread.this.startServiceFollowing(message.arg1);
          return;
        case UPDATE_FAVOURITE:
          // update the favourite
          DabThread.this.dbHelper.updateFav((DabSubChannelInfo) message.obj, message.arg1);
          // request player to update the memory buttons
          Message obtainMessage = DabThread.this.playerHandler.obtainMessage();
          obtainMessage.what = PlayerActivity.PLAYERMSG_SET_STATIONMEMORY;
          obtainMessage.obj = DabThread.this.dbHelper.getFavorites();
          DabThread.this.playerHandler.sendMessage(obtainMessage);
          return;
          /*
          case PLAY_FAVOURITE:
            DabSubChannelInfo sciFavourite =
                DabThread.this.dbHelper.getFavouriteService(message.arg1);
            if (sciFavourite != null) {
              int idx = DabThread.this.stationList.indexOf(sciFavourite);
              if (idx != -1) {
                if (!DabThread.this.isOnExit) {

                  // inform player which index gets played
                  Message obtainMessage2 = DabThread.this.playerHandler.obtainMessage();
                  obtainMessage2.what = PlayerActivity.PLAYERMSG_PLAY_PRESET;
                  obtainMessage2.arg1 = idx;
                  DabThread.this.playerHandler.sendMessage(obtainMessage2);

                  ServiceFollowing.update_enabled_status(DabThread.this.getContext());
                  DabThread.this.playStation(idx);
                }
              }
            }
            return;
            */
        default:
          return;
      }
      DabThread.this.looper.quit();
      DabThread.this.looper = null;
      Logger.d("looper quit");
    }
  }

  /* renamed from: com.ex.dabplayer.pad.dab.f$h */
  /* loaded from: classes.dex */
  public class FicThread extends Thread {

    /* renamed from: b */
    private volatile boolean exit = false;

    public FicThread() {}

    /* renamed from: a */
    private void writeExtraDataToBuffer(byte[] bArr, int i) {
      if (DabThread.this.ficRecorderRingBuffer != null && DabThread.this.ficRecorder != null) {
        synchronized (DabThread.this.ficRecorderRingBuffer) {
          DabThread.this.ficRecorderRingBuffer.writeBuffer(bArr, i);
        }
      }
    }

    /* renamed from: b */
    private void startRecording() {
      if (DabThread.this.ficRecorder != null) {
        DabThread.this.ficRecorder.exit();
      }
      DabThread.this.ficRecorderRingBuffer = new RingBuffer(81920);
      DabThread.this.ficRecorder = new FicRecorder(DabThread.this.ficRecorderRingBuffer);
      DabThread.this.ficRecorder.start();
    }

    /* renamed from: c */
    private void stopRecording() {
      if (DabThread.this.ficRecorder != null) {
        DabThread.this.ficRecorder.exit();
        DabThread.this.ficRecorder = null;
      }
    }

    /* renamed from: a */
    public void exit() {
      this.exit = true;
    }

    @Override // java.lang.Thread, java.lang.Runnable
    public void run() {
      int i;
      boolean usage_was_nonzero;
      int n_usage_stable;
      int fic_bytes_len;
      byte[] fic_data = new byte[384];
      this.exit = false;
      if (DabThread.this.ficRecording) {
        startRecording();
        i = 0;
        usage_was_nonzero = false;
        n_usage_stable = 0;
      } else {
        i = 0;
        usage_was_nonzero = false;
        n_usage_stable = 0;
      }
      while (!this.exit) {
        try {
          sleep(10L);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        if (DabThread.this.tuneOk) {
          synchronized (DabThread.this.dabDec) {
            fic_bytes_len = DabThread.this.dabDec.dab_api_get_fic_data(fic_data);
          }
          if (fic_bytes_len > 0) {
            if (DabThread.this.ficRecording) {
              if (i != DabThread.this.freq) {
                i = DabThread.this.freq;
                String str = "FREQ" + DabThread.this.freq;
                writeExtraDataToBuffer(str.getBytes(), str.getBytes().length);
              }
              writeExtraDataToBuffer(fic_data, fic_bytes_len);
            }
            DabThread.this.dabDec.decoder_fic_parse(fic_data, fic_bytes_len, DabThread.this.freq);
            boolean usage_is_nonzero = DabThread.this.dabDec.decoder_fic_get_usage() != 0;
            if (usage_is_nonzero != usage_was_nonzero) {
              usage_was_nonzero = usage_is_nonzero;
              n_usage_stable = 0;
            } else {
              n_usage_stable++;
            }
            if (n_usage_stable >= 100) {
              Logger.d("fic decode finish");
              int service_count = DabThread.this.dabDec.decoder_fic_get_service_count();
              if (service_count > 0) {
                Logger.d("found " + service_count + " service");
                List arrayList = new ArrayList();
                for (int n = 0; n < service_count; n++) {
                  DabSubChannelInfo info = new DabSubChannelInfo(true);
                  DabThread.this.dabDec.decoder_fic_get_subch_info(info, (char) n);
                  // here we could add extra data
                  arrayList.add(RepairEBU.fixLabels(info));
                }
                DabThread.this.total_known_services +=
                    DabThread.this.dbHelper.insertNewStations(arrayList);
                DabThread.this.scan_service_count += service_count;
                DabThread.this.stationList = DabThread.this.dbHelper.getStationList();
                Message obtainMessage = DabThread.this.playerHandler.obtainMessage();
                obtainMessage.what = PlayerActivity.PLAYERMSG_NEW_LIST_OF_STATIONS; // 1;
                obtainMessage.arg1 = DabThread.this.stationList.size();
                obtainMessage.obj = DabThread.this.stationList;
                DabThread.this.playerHandler.sendMessage(obtainMessage);
                DabThread.this.dabDec.decoder_reset_ensemble_info(0);
                DabThread.this.f99z = true;
                n_usage_stable = 0;
              }
            }
          }
        }
      }
      if (DabThread.this.ficRecording) {
        stopRecording();
      }
      Logger.d("fic thread exit");
    }
  }

  /* renamed from: com.ex.dabplayer.pad.dab.f$i */
  /* loaded from: classes.dex */
  public class MscThread extends Thread {

    /* renamed from: b */
    private volatile boolean exit = false;

    public MscThread(int i) {
      DabThread.this.mp2Thread = null;
      DabThread.this.aacThread = null;
      DabThread.this.inputRingBuffer.reset();
    }

    /* renamed from: a */
    public void exit() {
      this.exit = true;
      if (DabThread.this.mp2Thread != null) {
        DabThread.this.mp2Thread.exit();
        DabThread.this.mp2Thread = null;
      }
      if (DabThread.this.aacThread != null) {
        DabThread.this.aacThread.exit();
        DabThread.this.aacThread = null;
      }
    }

    @Override // java.lang.Thread, java.lang.Runnable
    public void run() {
      int bufferSize;
      int msc_data;
      byte[] bArr = null; // = new byte[4096];
      this.exit = false;
      Logger.d("msc thread start");
      while (!this.exit) {
        try {
          sleep(1L);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        synchronized (DabThread.this.inputRingBuffer) {
          bufferSize = DabThread.this.inputRingBuffer.getRemainingCapacity();
        }

        if (bArr == null || bufferSize >= bArr.length) {
          msc_data = 0;
          if (DabThread.this.audioType == AUDIOTYPE_AAC) {
            bArr = new byte[2048];
            msc_data = DabThread.this.dabDec.dab_api_get_msc_data(bArr);
            if (msc_data != -1) {
              msc_data = DabThread.this.dabDec.decoder_msc2aac(bArr, msc_data, bArr);
            }
          } else if (DabThread.this.audioType == AUDIOTYPE_MP2) {
            bArr = new byte[4096];
            msc_data = DabThread.this.dabDec.dab_api_get_msc_data(bArr);
          }
          if (msc_data > 0) {
            synchronized (DabThread.this.inputRingBuffer) {
              DabThread.this.inputRingBuffer.writeBuffer(bArr, msc_data);
            }
          }
          // }
        } else {
          Logger.d("no free buffer space");
        }
      }
      Logger.d("msc thread exit");
    }
  }

  /* JADX INFO: Access modifiers changed from: package-private */
  /* renamed from: com.ex.dabplayer.pad.dab.f$j */
  /* loaded from: classes.dex */
  public class SignalMotDlsMgr extends Thread {

    /* renamed from: b */
    private volatile boolean exit;
    private byte[] get_dls_buff;
    private byte[] get_mot_data_buff;
    private byte[] get_mot_type_buff;
    private String prevDlsString;
    private int prevSignal;
    private int times_little_signal;

    private SignalMotDlsMgr() {
      this.exit = false;
      this.times_little_signal = 0;
      this.get_dls_buff = new byte[102400];
      this.get_mot_type_buff = new byte[2];
      this.get_mot_data_buff = new byte[102400];
      this.prevDlsString = new String("");
      this.prevSignal = -1;
    }

    private void sendSignalQuality(int dab_api_get_signal) {
      int dab_api_get_signal2;
      if (this.prevSignal != dab_api_get_signal) {
        this.prevSignal = dab_api_get_signal;

        Handler playerHandler = DabThread.this.getPlayerHandler();
        if (playerHandler != null && 1 == 2) {
          Intent intent = new Intent(DabService.META_CHANGED);
          intent.putExtra(DabService.EXTRA_SENDER, DabService.SENDER_DAB);
          intent.putExtra(DabService.EXTRA_SIGNALQUALITY, dab_api_get_signal);
          Message intentMessage = playerHandler.obtainMessage();
          intentMessage.what = PlayerActivity.PLAYERMSG_STATIONINFO_INTENT;
          intentMessage.obj = intent;
          playerHandler.sendMessage(intentMessage);
        }

        if (dab_api_get_signal > 900) {
          dab_api_get_signal2 = 0;
        } else {
          dab_api_get_signal2 =
              dab_api_get_signal > 400
                  ? 1
                  : dab_api_get_signal > 300
                      ? 2
                      : dab_api_get_signal > 200 ? 3 : dab_api_get_signal > 100 ? 4 : 5;
        }
        if (DabThread.this.playerHandler != null) {
          Message obtainMessage = DabThread.this.playerHandler.obtainMessage();
          obtainMessage.what = PlayerActivity.PLAYERMSG_SIGNAL_QUALITY; // 11;
          obtainMessage.arg1 = dab_api_get_signal2;
          DabThread.this.playerHandler.removeMessages(PlayerActivity.PLAYERMSG_SIGNAL_QUALITY);
          DabThread.this.playerHandler.sendMessage(obtainMessage);
        }
      }
    }

    private void sendDls(String dls) {
      Handler handler;
      if (DabThread.this.playerHandler != null) {
        Message obtainMessage = DabThread.this.playerHandler.obtainMessage();
        obtainMessage.what = PlayerActivity.PLAYERMSG_DLS; // 9;
        obtainMessage.obj = dls;
        DabThread.this.playerHandler.sendMessage(obtainMessage);
      }
      WeakReference<Handler> playerHandler = PlayerActivity.getPlayerHandler();
      if (playerHandler != null && (handler = playerHandler.get()) != null) {
        Intent intent = new Intent(DabService.META_CHANGED);
        intent.putExtra(DabService.EXTRA_SENDER, DabService.SENDER_DAB);
        intent.putExtra(DabService.EXTRA_TRACK, dls);
        intent.putExtra(DabService.EXTRA_DLS, dls);
        intent.putExtra(DabService.EXTRA_AFFECTS_ANDROID_METADATA, true);
        Message intentMessage = handler.obtainMessage();
        intentMessage.what = PlayerActivity.PLAYERMSG_STATIONINFO_INTENT;
        intentMessage.obj = intent;
        handler.sendMessage(intentMessage);
      }
    }

    private void poll_signallevel() {
      int dab_api_get_signal = DabThread.this.dabDec.dab_api_get_signal(0);
      if (dab_api_get_signal >= 0) {
        if (dab_api_get_signal > 900) {
          this.times_little_signal++;
        } else {
          this.times_little_signal = 0;
        }
        sendSignalQuality(dab_api_get_signal);
        if (this.times_little_signal <= 4) {
          DabThread.this.isExecutingServiceFollowing = false;
        } else if (!DabThread.this.isExecutingServiceFollowing && ServiceFollowing.is_possible()) {
          Message obtainMessage2 = DabThread.this.dabHandler.obtainMessage();
          obtainMessage2.what = MSGTYPE_START_SERVICE_FOLLOWING;
          obtainMessage2.obj = "";
          obtainMessage2.arg1 = 0;
          DabThread.this.dabHandler.sendMessage(obtainMessage2);
        }
      }
    }

    private void poll_dls() {
      int decoder_get_dls;
      synchronized (DabThread.this.dabDec) {
        decoder_get_dls =
            DabThread.this.dabDec.decoder_get_dls(DabThread.this.audioType, this.get_dls_buff);
      }
      if (decoder_get_dls > 0) {
        byte[] obj2 = new byte[decoder_get_dls];
        System.arraycopy(this.get_dls_buff, 0, obj2, 0, decoder_get_dls);
        byte[] a = Decode.decodeToCharacter(obj2);
        String newDlsString = new String(a);
        if (!newDlsString.equals(this.prevDlsString)) {
          Logger.d("decoder_get_dls: " + newDlsString);
          this.prevDlsString = newDlsString;
          sendDls(newDlsString);
        }
      }
    }

    private void poll_mot() {
      int decoder_get_mot_data;
      synchronized (DabThread.this.dabDec) {
        decoder_get_mot_data =
            DabThread.this.dabDec.decoder_get_mot_data(
                this.get_mot_data_buff, this.get_mot_type_buff);
      }
      if (decoder_get_mot_data > 0) {
        Logger.d("received mot type " + this.get_mot_type_buff[0]);
        String fileName = this.get_mot_type_buff[0] == 0 ? "mot.png" : "mot.jpg";
        File file = new File(DabThread.this.context.getFilesDir(), fileName);
        if (file.exists()) {
          file.delete();
        }
        try {
          FileOutputStream fileOutputStream = new FileOutputStream(file);
          fileOutputStream.write(this.get_mot_data_buff, 0, decoder_get_mot_data);
          fileOutputStream.flush();
          fileOutputStream.close();
        } catch (FileNotFoundException e) {
          e.printStackTrace();
        } catch (IOException e2) {
          e2.printStackTrace();
        }
        Message obtainMessage = DabThread.this.playerHandler.obtainMessage();
        obtainMessage.what = PlayerActivity.PLAYERMSG_MOT; // 10
        obtainMessage.obj = fileName;
        DabThread.this.playerHandler.sendMessage(obtainMessage);
        try {
          String canonicalPath = file.getCanonicalPath();
          Intent intent = new Intent(DabService.META_CHANGED);
          intent.putExtra(DabService.EXTRA_SENDER, DabService.SENDER_DAB);
          intent.putExtra(DabService.EXTRA_SLS, canonicalPath);
          Logger.d("decoder_get_mot_data: " + canonicalPath);
          DabThread.this.getContext().sendBroadcast(intent);
        } catch (IOException io) {
          io.printStackTrace();
        } catch (SecurityException s) {
          s.printStackTrace();
        }
      }
    }

    /* renamed from: a */
    public void exit() {
      this.exit = true;
      sendSignalQuality(DabService.SIGNALQUALITY_NONE);
      sendDls("");
    }

    @Override // java.lang.Thread, java.lang.Runnable
    public void run() {

      // setPriority(1);
      int i = 0;
      while (!this.exit) {
        poll_dls();
        poll_signallevel();
        poll_mot();

        try {
          sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

        /*
        poll_dls();
        if (i > 1) {
          poll_signallevel();
        }
        poll_mot();
        try {
          sleep(500L);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        i++;
        if (DabThread.this.f97x) {
          DabThread.this.f97x = false;
          i = 0;
        }
                */

      }
    }
  }
}

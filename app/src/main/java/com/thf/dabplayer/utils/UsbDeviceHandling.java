package com.thf.dabplayer.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/* renamed from: com.ex.dabplayer.pad.utils.UsbDeviceHandling */
/* loaded from: classes.dex */
public class UsbDeviceHandling {
    public static final String ACTION_USB_PERMISSION = UsbDeviceHandling.class.getName() + ".USB_PERMISSION";
    private final Context mContext;
    private final OnUsbDeviceHandlingResultListener mListener;
    private final int mUSB_PID;
    private final int mUSB_VID;
    private UsbFsmHandler mUsbFsmHandler;

    /* renamed from: com.ex.dabplayer.pad.utils.UsbDeviceHandling$OnUsbDeviceHandlingResultListener */
    /* loaded from: classes.dex */
    public interface OnUsbDeviceHandlingResultListener {
        void onNoUsbDevice();

        void onNoUsbPermissionGranted();

        void onUsbConnectAttemptStarted(int i, int i2);

        void onUsbDeviceFound(UsbDevice usbDevice);

        void onUsbPermissionRequestAttemptStarted(int i, int i2);
    }

    public UsbDeviceHandling(@NonNull Context context, int vid, int pid, @Nullable OnUsbDeviceHandlingResultListener listener) {
        this.mUSB_VID = vid;
        this.mUSB_PID = pid;
        this.mContext = context;
        this.mListener = listener;
    }

    public void start() {
        this.mUsbFsmHandler = new UsbFsmHandler(this.mContext, this.mUSB_VID, this.mUSB_PID, this.mListener);
        this.mUsbFsmHandler.sendFsmEvent(6);
    }

    public void pause() {
        if (this.mUsbFsmHandler == null) {
            return;
        }
        this.mUsbFsmHandler.sendFsmEvent(1);
    }

    public void resume() {
        if (this.mUsbFsmHandler == null) {
            return;
        }
        this.mUsbFsmHandler.sendFsmEvent(0);
    }

    public void stop() {
        if (this.mUsbFsmHandler == null) {
            return;
        }
        this.mUsbFsmHandler.sendFsmEvent(7);
    }

    public void reset() {
        if (this.mUsbFsmHandler == null) {
            return;
        }
        this.mUsbFsmHandler.resetFsm();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.ex.dabplayer.pad.utils.UsbDeviceHandling$LogMe */
    /* loaded from: classes.dex */
    public static class LogMe {
        LogMe() {
        }

        /* renamed from: i */
        static void m10i(String str) {
            Logger.d("UsbDeviceHandling: " + str);
        }

        /* renamed from: e */
        static void m11e(String str) {
            Logger.d("UsbDeviceHandling ERROR: " + str);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.ex.dabplayer.pad.utils.UsbDeviceHandling$UsbFsmHandler */
    /* loaded from: classes.dex */
    public static class UsbFsmHandler extends Handler {
        private static final int EVENT_BASE = 0;
        private static final int EVENT_FINALLY_NO_USBDEVICE = 2;
        private static final int EVENT_FINALLY_NO_USBPERMISSION = 5;
        private static final int EVENT_FOUND_USBDEVICE = 3;
        private static final int EVENT_HAS_PERMISSION = 4;
        private static final int EVENT_PAUSE = 1;
        private static final int EVENT_RESUME = 0;
        private static final int EVENT_START = 6;
        private static final int EVENT_STOP = 7;
        private static final int STATE_CONNECTING = 10;
        private static final int STATE_DELAY_AFTER_RESUME = 40;
        private static final int STATE_PAUSED = 30;
        private static final int STATE_REQUESTING_PERMISSION = 20;
        private static final int STATE_START = 0;
        private static final int STATE_STOP = -5;
        private static final int TIMER_BASE = 1000;
        private static final int TIMER_CONNECT_RETRY = 1000;
        private static final long TIMER_CONNECT_RETRY_MS = 1000;
        private static final int TIMER_PERMISSION_RETRY = 1001;
        private static final long TIMER_PERMISSION_RETRY_MS = 2000;
        private static final int TIMER_RESUME_DELAY = 1002;
        private static final long TIMER_RESUME_DELAY_MS = 1000;
        private final Context mContext;
        private final OnUsbDeviceHandlingResultListener mListener;
        private final UsbManager mUsbManager;
        private final int mUsbPid;
        private final int mUsbVid;
        private int mState = STATE_STOP;
        private int mNumConnectingRetries = 0;
        final int CONNECTING_MAX_RETRIES = 5;
        private int mNumPermissionRetries = 0;
        final int PERMISSION_MAX_RETRIES = 3;
        private UsbDevice mFoundUsbDevice = null;

        UsbFsmHandler(@NonNull Context context, int vid, int pid, @Nullable OnUsbDeviceHandlingResultListener listener) {
            this.mContext = context;
            this.mUsbVid = vid;
            this.mUsbPid = pid;
            this.mListener = listener;
            this.mUsbManager = (UsbManager) context.getSystemService("usb");
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    if (this.mState == 0) {
                        setState(10);
                        searchUsbDevice();
                        return;
                    } else if (STATE_PAUSED == this.mState) {
                        setState(40);
                        scheduleTimer(1002, 1000L);
                        return;
                    } else {
                        return;
                    }
                case 1:
                    if (STATE_STOP != this.mState) {
                        setState(STATE_PAUSED);
                        return;
                    }
                    return;
                case 2:
                    resetFsm();
                    if (this.mListener != null) {
                        this.mListener.onNoUsbDevice();
                        return;
                    }
                    return;
                case 3:
                    if (10 == this.mState) {
                        setState(20);
                        requestUsbPermission();
                        return;
                    }
                    return;
                case 4:
                    resetFsm();
                    if (this.mListener != null) {
                        this.mListener.onUsbDeviceFound(this.mFoundUsbDevice);
                        return;
                    }
                    return;
                case 5:
                    resetFsm();
                    if (this.mListener != null) {
                        this.mListener.onNoUsbPermissionGranted();
                        return;
                    }
                    return;
                case 6:
                    resetFsm();
                    return;
                case 7:
                    setState(STATE_STOP);
                    return;
                case 1000:
                    if (10 == this.mState) {
                        searchUsbDevice();
                        return;
                    }
                    return;
                case 1001:
                    if (20 == this.mState) {
                        requestUsbPermission();
                        return;
                    }
                    return;
                case 1002:
                    if (40 == this.mState) {
                        setState(10);
                        searchUsbDevice();
                        return;
                    }
                    return;
                default:
                    return;
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void sendFsmEvent(int eventID) {
            Message msg = obtainMessage(eventID);
            removeMessages(eventID);
            sendMessage(msg);
        }

        private void scheduleTimer(final int timerID, long timeout) {
            postDelayed(new Runnable() { // from class: com.ex.dabplayer.pad.utils.UsbDeviceHandling.UsbFsmHandler.1
                @Override // java.lang.Runnable
                public void run() {
                    UsbFsmHandler.this.sendFsmEvent(timerID);
                }
            }, timeout);
        }

        private void setState(int state) {
            this.mState = state;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void resetFsm() {
            this.mNumPermissionRetries = 0;
            this.mNumConnectingRetries = 0;
            setState(0);
        }

        /* JADX WARN: Code restructure failed: missing block: B:18:0x0096, code lost:
            r6.mFoundUsbDevice = r1;
            com.thf.dabplayer.utils.UsbDeviceHandling.LogMe.m10i("USB device found " + r1.getDeviceName());
         */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct code enable 'Show inconsistent code' option in preferences
        */
       private void searchUsbDevice() {
            this.mNumConnectingRetries += EVENT_PAUSE;
            this.mNumPermissionRetries = 0;
            if (this.mNumConnectingRetries > EVENT_FINALLY_NO_USBPERMISSION) {
                sendFsmEvent(EVENT_FINALLY_NO_USBDEVICE);
                return;
            }
            Logger.d("USB connect attempt " + this.mNumConnectingRetries + "/" + EVENT_FINALLY_NO_USBPERMISSION);
            if (this.mListener != null) {
                this.mListener.onUsbConnectAttemptStarted(this.mNumConnectingRetries, EVENT_FINALLY_NO_USBPERMISSION);
            }
            if (this.mUsbManager != null) {
                try {
                    for (UsbDevice usbDevice : this.mUsbManager.getDeviceList().values()) {
                       
                        Logger.d("USB device " + usbDevice.getProductId() + "/" + usbDevice.getVendorId());
                        if (usbDevice.getProductId() == this.mUsbPid && usbDevice.getVendorId() == this.mUsbVid) {
                            this.mFoundUsbDevice = usbDevice;
                            Logger.d("USB device found " + usbDevice.getDeviceName());
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Logger.d("mUsbManager null");
            }
            if (this.mFoundUsbDevice != null) {
                sendFsmEvent(EVENT_FOUND_USBDEVICE);
            } else {
                scheduleTimer(1000, 1000);
            }
        }
        
        
        private void searchUsbDeviceX() {
            /*
                r6 = this;
                r4 = 5
                int r2 = r6.mNumConnectingRetries
                int r2 = r2 + 1
                r6.mNumConnectingRetries = r2
                r2 = 0
                r6.mNumPermissionRetries = r2
                int r2 = r6.mNumConnectingRetries
                if (r2 <= r4) goto L13
                r2 = 2
                r6.sendFsmEvent(r2)
            L12:
                return
            L13:
                java.lang.StringBuilder r2 = new java.lang.StringBuilder
                r2.<init>()
                java.lang.String r3 = "USB connect attempt "
                java.lang.StringBuilder r2 = r2.append(r3)
                int r3 = r6.mNumConnectingRetries
                java.lang.StringBuilder r2 = r2.append(r3)
                java.lang.String r3 = "/"
                java.lang.StringBuilder r2 = r2.append(r3)
                java.lang.StringBuilder r2 = r2.append(r4)
                java.lang.String r2 = r2.toString()
                com.thf.dabplayer.utils.UsbDeviceHandling.LogMe.m10i(r2)
                com.ex.dabplayer.pad.utils.UsbDeviceHandling$OnUsbDeviceHandlingResultListener r2 = r6.mListener
                if (r2 == 0) goto L40
                com.ex.dabplayer.pad.utils.UsbDeviceHandling$OnUsbDeviceHandlingResultListener r2 = r6.mListener
                int r3 = r6.mNumConnectingRetries
                r2.onUsbConnectAttemptStarted(r3, r4)
            L40:
                android.hardware.usb.UsbManager r2 = r6.mUsbManager
                if (r2 == 0) goto Lc1
                android.hardware.usb.UsbManager r2 = r6.mUsbManager     // Catch: java.lang.Exception -> Lbc
                java.util.HashMap r2 = r2.getDeviceList()     // Catch: java.lang.Exception -> Lbc
                java.util.Collection r2 = r2.values()     // Catch: java.lang.Exception -> Lbc
                java.util.Iterator r2 = r2.iterator()     // Catch: java.lang.Exception -> Lbc
            L52:
                boolean r3 = r2.hasNext()     // Catch: java.lang.Exception -> Lbc
                if (r3 == 0) goto Lb2
                java.lang.Object r1 = r2.next()     // Catch: java.lang.Exception -> Lbc
                android.hardware.usb.UsbDevice r1 = (android.hardware.usb.UsbDevice) r1     // Catch: java.lang.Exception -> Lbc
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch: java.lang.Exception -> Lbc
                r3.<init>()     // Catch: java.lang.Exception -> Lbc
                java.lang.String r4 = "USB device "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch: java.lang.Exception -> Lbc
                int r4 = r1.getProductId()     // Catch: java.lang.Exception -> Lbc
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch: java.lang.Exception -> Lbc
                java.lang.String r4 = "/"
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch: java.lang.Exception -> Lbc
                int r4 = r1.getVendorId()     // Catch: java.lang.Exception -> Lbc
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch: java.lang.Exception -> Lbc
                java.lang.String r3 = r3.toString()     // Catch: java.lang.Exception -> Lbc
                com.thf.dabplayer.utils.UsbDeviceHandling.LogMe.m10i(r3)     // Catch: java.lang.Exception -> Lbc
                int r3 = r1.getProductId()     // Catch: java.lang.Exception -> Lbc
                int r4 = r6.mUsbPid     // Catch: java.lang.Exception -> Lbc
                if (r3 != r4) goto L52
                int r3 = r1.getVendorId()     // Catch: java.lang.Exception -> Lbc
                int r4 = r6.mUsbVid     // Catch: java.lang.Exception -> Lbc
                if (r3 != r4) goto L52
                r6.mFoundUsbDevice = r1     // Catch: java.lang.Exception -> Lbc
                java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch: java.lang.Exception -> Lbc
                r2.<init>()     // Catch: java.lang.Exception -> Lbc
                java.lang.String r3 = "USB device found "
                java.lang.StringBuilder r2 = r2.append(r3)     // Catch: java.lang.Exception -> Lbc
                java.lang.String r3 = r1.getDeviceName()     // Catch: java.lang.Exception -> Lbc
                java.lang.StringBuilder r2 = r2.append(r3)     // Catch: java.lang.Exception -> Lbc
                java.lang.String r2 = r2.toString()     // Catch: java.lang.Exception -> Lbc
                com.thf.dabplayer.utils.UsbDeviceHandling.LogMe.m10i(r2)     // Catch: java.lang.Exception -> Lbc
            Lb2:
                android.hardware.usb.UsbDevice r2 = r6.mFoundUsbDevice
                if (r2 == 0) goto Lc7
                r2 = 3
                r6.sendFsmEvent(r2)
                goto L12
            Lbc:
                r0 = move-exception
                r0.printStackTrace()
                goto Lb2
            Lc1:
                java.lang.String r2 = "mUsbManager null"
                com.thf.dabplayer.utils.UsbDeviceHandling.LogMe.m11e(r2)
                goto Lb2
            Lc7:
                r2 = 1000(0x3e8, float:1.401E-42)
                r4 = 1000(0x3e8, double:4.94E-321)
                r6.scheduleTimer(r2, r4)
                goto L12
            */
            throw new UnsupportedOperationException("Method not decompiled: com.thf.dabplayer.utils.UsbDeviceHandling.UsbFsmHandler.searchUsbDevice():void");
        }

        private void requestUsbPermission() {
            this.mNumPermissionRetries++;
            this.mNumConnectingRetries = 0;
            if (this.mNumPermissionRetries > 3) {
                sendFsmEvent(5);
                return;
            }
            LogMe.m10i("USB permission attempt " + this.mNumPermissionRetries + "/3");
            if (this.mListener != null) {
                this.mListener.onUsbPermissionRequestAttemptStarted(this.mNumPermissionRetries, 3);
            }
            if (this.mFoundUsbDevice != null && this.mUsbManager != null) {
                if (this.mUsbManager.hasPermission(this.mFoundUsbDevice)) {
                    LogMe.m10i("hasPermission " + this.mFoundUsbDevice.getDeviceName());
                    sendFsmEvent(4);
                    return;
                }
                PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(UsbDeviceHandling.ACTION_USB_PERMISSION), 0);
                this.mUsbManager.requestPermission(this.mFoundUsbDevice, usbPermissionIntent);
                LogMe.m10i("requested USB permission " + this.mFoundUsbDevice.getDeviceName());
                scheduleTimer(1001, TIMER_PERMISSION_RETRY_MS);
                return;
            }
            LogMe.m11e("mFoundUsbDevice:" + this.mFoundUsbDevice + ", mUsbManager:" + this.mUsbManager);
            sendFsmEvent(5);
        }
    }
}
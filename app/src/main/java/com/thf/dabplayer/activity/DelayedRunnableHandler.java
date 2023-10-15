package com.thf.dabplayer.activity;

import android.os.Handler;
import android.os.Message;

/* renamed from: com.ex.dabplayer.pad.activity.DelayedRunnableHandler */
/* loaded from: classes.dex */
public class DelayedRunnableHandler extends Handler {
    public static final int MSG_DELAYED_RUN = 1;

    @Override // android.os.Handler
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case 1:
                if (msg.obj != null) {
                    ((Runnable) msg.obj).run();
                    return;
                }
                return;
            default:
                return;
        }
    }
}

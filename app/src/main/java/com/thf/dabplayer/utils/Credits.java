package com.thf.dabplayer.utils;

import android.app.Notification;
import android.view.View;
import com.thf.dabplayer.activity.MainActivity;
import com.thf.dabplayer.service.DabService;

/* renamed from: com.ex.dabplayer.pad.utils.Credits */
/* loaded from: classes.dex */
public class Credits {
    public native boolean credits_show_notification(DabService dabService, int i, Notification notification, View view);

    public native boolean credits_show_notification(MainActivity dabService, int i, Notification notification, View view);
    
    static {
        System.loadLibrary("credits");
        C0162a.m9a("credits loaded");
    }
}
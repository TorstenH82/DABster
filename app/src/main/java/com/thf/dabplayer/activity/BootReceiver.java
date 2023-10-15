package com.thf.dabplayer.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.thf.dabplayer.utils.C0162a;

/* renamed from: com.ex.dabplayer.pad.activity.BootReceiver */
/* loaded from: classes.dex */
public class BootReceiver extends BroadcastReceiver {
    private final AlarmReceiver alarm = new AlarmReceiver();

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            this.alarm.setAlarm(context);
            C0162a.m9a("===== BOOT RECEIVED ! =====");
        }
    }
}

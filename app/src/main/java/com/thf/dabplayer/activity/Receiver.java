package com.thf.dabplayer.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.thf.dabplayer.service.DabService;

/* renamed from: com.ex.dabplayer.pad.activity.Receiver */
/* loaded from: classes.dex */
public class Receiver extends BroadcastReceiver {
    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("com.microntek.app")) {
            String upperCase = intent.getStringExtra("app").toUpperCase();
            String stringExtra = intent.getStringExtra("state");
            if (upperCase.contains("FM") && stringExtra.equals("ENTER")) {
                Intent intent2 = new Intent(DabService.SENDER_DAB);
                intent2.putExtra("stop", "1");
                context.sendBroadcast(intent2);
            }
        }
    }
}

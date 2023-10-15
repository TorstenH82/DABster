package com.thf.dabplayer.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
// import android.content.pm.PackageManager;
// import android.support.p000v4.content.WakefulBroadcastReceiver;
import android.content.pm.PackageManager;
import androidx.legacy.content.WakefulBroadcastReceiver;
import com.thf.dabplayer.service.SchedulingService;
import java.util.Calendar;

/* renamed from: com.ex.dabplayer.pad.activity.AlarmReceiver */
/* loaded from: classes.dex */
public class AlarmReceiver extends WakefulBroadcastReceiver {
  @Override // android.content.BroadcastReceiver
  public void onReceive(Context context, Intent intent) {
    Intent service = new Intent(context, SchedulingService.class);
    // startWakefulService(context, service);
    context.startService(service);
  }

  public void setAlarm(Context context) {
    AlarmManager alarmMgr = (AlarmManager) context.getSystemService("alarm");
    Intent intent = new Intent(context, AlarmReceiver.class);
    PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
    Calendar calendar = Calendar.getInstance();
    alarmMgr.setRepeating(0, calendar.getTimeInMillis(), 1800000L, alarmIntent);
    ComponentName receiver = new ComponentName(context, BootReceiver.class);
    PackageManager pm = context.getPackageManager();
    pm.setComponentEnabledSetting(receiver, 1, 1);
  }
}

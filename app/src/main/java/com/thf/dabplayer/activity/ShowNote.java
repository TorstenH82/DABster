package com.thf.dabplayer.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;

/* renamed from: com.ex.dabplayer.pad.activity.ShowNote */
/* loaded from: classes.dex */
public class ShowNote extends Activity {

    /* renamed from: dm */
    private DownloadManager f53dm;
    private long enqueue;
    boolean isDeleted;
    private BroadcastReceiver receiver;

    static /* synthetic */ boolean access$400() {
        return isRooted();
    }

    @Override // android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView title = new TextView(this);
        title.setText("Update available for DAB+");
        title.setPadding(10, 10, 10, 10);
        title.setGravity(17);
        title.setTextSize(20.0f);
        TextView msg = new TextView(this);
        msg.setText("Version: 1.0 (05.10.18)\n\nUpdate Now?");
        msg.setPadding(10, 10, 10, 10);
        msg.setGravity(17);
        msg.setTextSize(16.0f);
        AlertDialog.Builder builder = new AlertDialog.Builder(this, 2);
        builder.setCustomTitle(title);
        builder.setIcon(2131099733);
        builder.setView(msg);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { // from class: com.ex.dabplayer.pad.activity.ShowNote.1
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(ShowNote.this.getApplicationContext(), "Laster ned oppdatering", 1).show();
                ShowNote.this.f53dm = (DownloadManager) ShowNote.this.getSystemService("download");
                File file = new File(Environment.getExternalStorageDirectory() + "/Download/dab.apk");
                if (!file.exists()) {
                    ShowNote.this.firstTimeInstall();
                    return;
                }
                ShowNote.this.isDeleted = file.delete();
                ShowNote.this.deleteAndInstall();
            }
        });
        builder.setNegativeButton("Later", new DialogInterface.OnClickListener() { // from class: com.ex.dabplayer.pad.activity.ShowNote.2
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int which) {
                ShowNote.this.finish();
            }
        });
        builder.show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void firstTimeInstall() {
        Log.d("May be 1st Update:", "OR deleted from folder");
        downloadAndInstall();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void deleteAndInstall() {
        if (this.isDeleted) {
            Log.d("Deleted Existance file:", String.valueOf(this.isDeleted));
            downloadAndInstall();
            return;
        }
        Log.d("NOT DELETED:", String.valueOf(this.isDeleted));
        Toast.makeText(getApplicationContext(), "Something went wrong.. please try again", 1).show();
    }

    private void downloadAndInstall() {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse("http://androidautoshop.com/dab/aas/dab.apk"));
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "dab.apk");
        this.enqueue = this.f53dm.enqueue(request);
        this.receiver = new BroadcastReceiver() { // from class: com.ex.dabplayer.pad.activity.ShowNote.3
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("android.intent.action.DOWNLOAD_COMPLETE".equals(action)) {
                    Toast.makeText(ShowNote.this.getApplicationContext(), "Download Complete", 1).show();
                    long downloadId = intent.getLongExtra("extra_download_id", 0L);
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(ShowNote.this.enqueue);
                    Cursor c = ShowNote.this.f53dm.query(query);
                    if (c.moveToFirst()) {
                        int columnIndex = c.getColumnIndex("status");
                        if (8 == c.getInt(columnIndex)) {
                            String uriString = c.getString(c.getColumnIndex("local_uri"));
                            Log.d("ainfo", uriString);
                            if (downloadId == c.getInt(0)) {
                                Log.d("DOWNLOAD PATH:", c.getString(c.getColumnIndex("local_uri")));
                                Log.d("isRooted:", String.valueOf(ShowNote.access$400()));
                                if (!ShowNote.access$400()) {
                                    Intent intent_install = new Intent("android.intent.action.VIEW");
                                    intent_install.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/Download/dab.apk")), "application/vnd.android.package-archive");
                                    Log.d("phone path", Environment.getExternalStorageDirectory() + "/Download/dab.apk");
                                    ShowNote.this.startActivity(intent_install);
                                } else {
                                    Toast.makeText(ShowNote.this.getApplicationContext(), "App Installing...Please Wait", 1).show();
                                    File file = new File(Environment.getExternalStorageDirectory() + "/Download/dab.apk");
                                    Log.d("IN INSTALLER:", Environment.getExternalStorageDirectory() + "/Download/dab.apk");
                                    if (file.exists()) {
                                        try {
                                            Log.d("IN File exists:", Environment.getExternalStorageDirectory() + "/Download/dab.apk");
                                            String command = "pm install -r " + Environment.getExternalStorageDirectory() + "/Download/dab.apk";
                                            Log.d("COMMAND:", command);
                                            Process proc = Runtime.getRuntime().exec(new String[]{"su", "-c", command});
                                            proc.waitFor();
                                            Toast.makeText(ShowNote.this.getApplicationContext(), "App Installed Successfully", 1).show();
                                            ShowNote.this.finish();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    }
                    c.close();
                }
            }
        };
        registerReceiver(this.receiver, new IntentFilter("android.intent.action.DOWNLOAD_COMPLETE"));
    }

    private static boolean isRooted() {
        return findBinary("su");
    }

    public static boolean findBinary(String binaryName) {
        if (0 != 0) {
            return false;
        }
        String[] places = {"/sbin/", "/system/bin/", "/system/xbin/", "/data/local/xbin/", "/data/local/bin/", "/system/sd/xbin/", "/system/bin/failsafe/", "/data/local/"};
        for (String where : places) {
            if (new File(where + binaryName).exists()) {
                return false;
            }
        }
        return false;
    }

    @Override // android.app.Activity, android.view.KeyEvent.Callback
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 4) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}

package com.thf.dabplayer.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import com.thf.dabplayer.R;
import com.thf.dabplayer.utils.C0162a;

/* renamed from: com.ex.dabplayer.pad.activity.ScanDialog */
/* loaded from: classes.dex */
public class ScanDialog {
  /* JADX INFO: Access modifiers changed from: package-private */
  public ScanDialog(Player player, int num_stations) {
    int default_choice;
    if (num_stations > 0) {
      int default_scan_type =
          player
              .getSharedPreferences(SettingsActivity.prefname_settings, 0)
              .getInt(SettingsActivity.pref_default_scan_type, 2);
      PlayerScanTypeDialogClickListener dialogClickListener =
          new PlayerScanTypeDialogClickListener(player, default_scan_type);
      CharSequence[] items = {
        player.getResources().getString(R.string.text_full_scan),
        player.getResources().getString(R.string.text_favo_scan),
        player.getResources().getString(R.string.text_incr_scan)
      };
      switch (default_scan_type) {
        case 1:
          default_choice = 2;
          break;
        case 2:
          default_choice = 1;
          break;
        default:
          default_choice = 0;
          break;
      }
      AlertDialog.Builder builder = new AlertDialog.Builder(player, 2);
      builder
          .setTitle(R.string.dialog_select_scantype_caption)
          .setIcon(R.drawable.radio)
          .setSingleChoiceItems(items, default_choice, dialogClickListener)
          .setPositiveButton(player.getResources().getString(17039379), dialogClickListener)
          .setNegativeButton(player.getResources().getString(17039369), dialogClickListener)
          .show();
      return;
    }
    player.startScan(0);
  }

  /* renamed from: com.ex.dabplayer.pad.activity.ScanDialog$PlayerScanTypeDialogClickListener */
  /* loaded from: classes.dex */
  public class PlayerScanTypeDialogClickListener implements DialogInterface.OnClickListener {
    private int mDefaultScanType;
    private Player mPlayer;
    private int mScanType;

    PlayerScanTypeDialogClickListener(Player player, int default_scan_type) {
      this.mPlayer = player;
      this.mDefaultScanType = default_scan_type;
      this.mScanType = default_scan_type;
      C0162a.m9a("scan type dialog default=" + this.mScanType);
    }

    @Override // android.content.DialogInterface.OnClickListener
    public void onClick(DialogInterface dialog, int which) {
      C0162a.m9a("scan type dialog: which=" + which);
      switch (which) {
        case -2:
        default:
          return;
        case -1:
          this.mPlayer.startScan(this.mScanType);
          if (this.mScanType != this.mDefaultScanType) {
            SharedPreferences.Editor preferences =
                this.mPlayer.getSharedPreferences(SettingsActivity.prefname_settings, 0).edit();
            preferences.putInt(SettingsActivity.pref_default_scan_type, this.mScanType);
            preferences.apply();
            return;
          }
          return;
        case 0:
          this.mScanType = 0;
          return;
        case 1:
          this.mScanType = 2;
          return;
        case 2:
          this.mScanType = 1;
          return;
      }
    }
  }
}

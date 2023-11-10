package com.thf.dabplayer.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.app.Activity;
import android.widget.Toast;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.thf.dabplayer.DabsterApp;
import com.thf.dabplayer.dab.DabSubChannelInfo;
import com.thf.dabplayer.utils.Logger;
import com.thf.dabplayer.utils.SharedPreferencesHelper;
import com.thf.dabplayer.R;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PopupActivity extends Activity {

  private String source;
  private Context context;
  private DabsterApp mApplication;
  private PopupDialog popupDialog;
  private List<DabSubChannelInfo> list;

  private PopupDialog.PopupStationsListener listener =
      new PopupDialog.PopupStationsListener() {
        @Override
        public void requestClose() {
          popupDialog.dismiss();
          PopupActivity.this.finish();
        }

        @Override
        public void showPlayer() {

          Intent intent = new Intent();
          intent.setClass(context, PlayerActivity.class);
          intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
          // intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT |
          // Intent.FLAG_ACTIVITY_SINGLE_TOP); //    (536870912);
          startActivity(intent);
          // PopupActivity.this.finish();
          popupDialog.dismiss();
            PopupActivity.this.finish();
        }
      };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mApplication = (DabsterApp) getApplicationContext();
    this.context = getApplicationContext();
    Intent intent = getIntent();

    List<DabSubChannelInfo> list = new ArrayList<>();
    this.list = (List<DabSubChannelInfo>) intent.getSerializableExtra("stationList");
    // this.source = intent.getStringExtra("source");
    popupDialog = new PopupDialog(this, listener);
  }

  @Override
  protected void onResume() {
    super.onResume();

    mApplication.setPopupActivityRunning(true);

    if (!popupDialog.isShowing()) {
      LocalBroadcastManager.getInstance(this)
          .registerReceiver(messageReceiver, new IntentFilter("popup-message"));
      this.popupDialog.setItemsAndShow(this.list);
    }
  }

  protected void onStop() {
    super.onStop();
    Logger.d("stop PopupActivity");
    LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
    mApplication.setPopupActivityRunning(false);
  }

  private BroadcastReceiver messageReceiver =
      new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
          // Extract data included in the Intent
          Logger.d("broadcast message received");
          PopupActivity.this.list =
              (List<DabSubChannelInfo>) intent.getSerializableExtra("stationList");
          PopupActivity.this.popupDialog.setItemsAndShow(PopupActivity.this.list);
        }
      };
}

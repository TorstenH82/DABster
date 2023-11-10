package com.thf.dabplayer.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.thf.dabplayer.R;
import com.thf.dabplayer.dab.DabSubChannelInfo;
import java.util.List;
import android.os.Handler;
import android.os.Looper;

public class PopupDialog {
  AlertDialog.Builder builder;

  private Activity activity;
  private AlertDialog dialog;
  private LinearLayoutManager linearLayoutManager;
  private boolean layoutComplete = false;
  private boolean isShowing = false;

  private PopupStationsAdapter adapter;
  private PopupStationsListener listener;

  private  Bomb bomb;

  public interface PopupStationsListener {
    public void requestClose();

    public void showPlayer();
  }

  public PopupDialog(Activity activity, PopupStationsListener listener) {
    this.activity = activity;
    this.listener = listener;

    this.builder =
        new AlertDialog.Builder(activity, R.style.MaterialAlertDialog_rounded)
            // new ContextThemeWrapper(this.context, (int) R.style.AlertDialogCustom))
            // .setTitle(title)
            // .setMessage(initMessage)
            .setCancelable(true);
    // .setIcon(R.drawable.radio);

    LayoutInflater layoutInflater = LayoutInflater.from(activity);
    final View alertView = layoutInflater.inflate(R.layout.dialog_popup, null);
    this.builder.setView(alertView);

    this.dialog = builder.create();
    this.dialog
        .getWindow()
        .setBackgroundDrawableResource(R.drawable.dialog_rounded_background_trans);
    // this.dialog.getWindow().getDecorView().setBackgroundDrawable(activity.getDrawable(R.drawable.dialogbackground));
    this.dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

    RecyclerView recyclerView = alertView.findViewById(R.id.stationRecycler);
    linearLayoutManager =
        new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false) {
          /*
            @Override
          public boolean canScrollHorizontally() {
            return false;
          }
            */
          @Override
          public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
            // force width of viewHolder here, this will override layout_width from xml
            lp.width = getWidth() / 3;
            return true;
          }

          // after the layout has finished drawing on the screen
          @Override
          public void onLayoutCompleted(RecyclerView.State state) {
            super.onLayoutCompleted(state);
            layoutComplete = true;
          }
        };
    recyclerView.setLayoutManager(linearLayoutManager);
    PagerSnapHelper pagerSnapHelper = new PagerSnapHelper();
    pagerSnapHelper.attachToRecyclerView(recyclerView);
    adapter = new PopupStationsAdapter(activity);
    recyclerView.setAdapter(adapter);

    recyclerView.addOnItemTouchListener(
        new RecyclerView.SimpleOnItemTouchListener() {
          @Override
          public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent me) {
            if (bomb != null) {
              bomb.disarm();
            }
            listener.showPlayer();
            return true;
          }
        });

    this.dialog.create();
  }

  public void show() {
    dialog.show();
    this.isShowing = true;
  }

  public void setItemsAndShow(List<DabSubChannelInfo> stationList) {
    if (bomb == null) {
      bomb = new Bomb(3000);
      // layoutComplete = false;
    } else {
      bomb.disarm();
    }
    layoutComplete = false; // ?
    this.adapter.setStations(stationList);
    if (!this.isShowing) show();
    bomb.start();
  }

  public void dismiss() {
    if (this.bomb != null) {
      bomb.disarm();
    }
    dialog.dismiss();
    this.isShowing = false;
  }

  public boolean isShowing() {
    return this.isShowing;
  }

  private class Bomb {
    private Thread bombThread;
    private Handler handler;
    private int wait = 3000;

    public Bomb(int wait) {
      this.handler = new Handler(Looper.getMainLooper());
      this.wait = wait;
    }

    public void disarm() {
      if (bombThread != null && bombThread.isAlive()) {
        bombThread.interrupt();
        bombThread = null;
      }
    }

    public void start() {
      // do something long
      Runnable runnable =
          new Runnable() {
            @Override
            public void run() {
              while (!layoutComplete) {
                // wait for inflator
              }
              try {
                Thread.sleep(wait);
              } catch (InterruptedException ex) {
                return;
              }

              handler.post(
                  new Runnable() {
                    @Override
                    public void run() {
                      listener.requestClose();
                    }
                  });
            }
          };

      if (bombThread != null && bombThread.isAlive()) {
        bombThread.interrupt();
        bombThread = null;
      }
      bombThread = new Thread(runnable);
      bombThread.start();
    }
  }
}

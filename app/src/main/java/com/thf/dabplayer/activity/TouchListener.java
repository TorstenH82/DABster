package com.thf.dabplayer.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import com.thf.dabplayer.R;
import com.thf.dabplayer.activity.StationBaseAdapter;
import com.thf.dabplayer.utils.C0162a;

/* JADX INFO: Access modifiers changed from: package-private */
/* renamed from: com.ex.dabplayer.pad.activity.TouchListener */
/* loaded from: classes.dex */
public class TouchListener implements View.OnTouchListener {
  public static final int MOVEDIRECTION_HORIZONTAL = 1;
  public static final int MOVEDIRECTION_NONE = 0;
  public static final int MOVEDIRECTION_VERTICAL = 2;
  private Player activity;
  private View currentView;
  private GestureDetector gestureDetector;
  private final int touchSlop;
  private int moveDirection = 0;
  private float downX = 0.0f;
  private float downY = 0.0f;

  public TouchListener(Player parentActivity) {
    this.activity = parentActivity;
    this.gestureDetector = new GestureDetector(parentActivity, new GestureListener());
    this.touchSlop = ViewConfiguration.get(parentActivity.getContext()).getScaledTouchSlop();
  }

  @Override // android.view.View.OnTouchListener
  @SuppressLint({"ClickableViewAccessibility"})
  public boolean onTouch(View v, MotionEvent event) {
    this.currentView = v;
    this.activity.maximizeLeftArea(false, true);
    switch (event.getActionMasked()) {
      case 0:
        this.downX = event.getX();
        this.downY = event.getY();
      case 1:
        this.moveDirection = 0;
        break;
      case 2:
        if (Math.abs(this.downX - event.getX()) > this.touchSlop) {
          this.moveDirection = 1;
          break;
        } else if (Math.abs(this.downY - event.getY()) > this.touchSlop) {
          this.moveDirection = 2;
          break;
        }
        break;
    }
    return this.gestureDetector.onTouchEvent(event);
  }

  public final int getCurrentMoveDirection() {
    return this.moveDirection;
  }

  /* renamed from: com.ex.dabplayer.pad.activity.TouchListener$GestureListener */
  /* loaded from: classes.dex */
  private final class GestureListener extends GestureDetector.SimpleOnGestureListener {
    private GestureListener() {}

    @Override // android.view.GestureDetector.SimpleOnGestureListener,
              // android.view.GestureDetector.OnGestureListener
    public boolean onDown(MotionEvent e) {
      return true;
    }

    @Override // android.view.GestureDetector.SimpleOnGestureListener,
              // android.view.GestureDetector.OnGestureListener
    public boolean onSingleTapUp(MotionEvent e) {
      onClick();
      return super.onSingleTapUp(e);
    }

    @Override // android.view.GestureDetector.SimpleOnGestureListener,
              // android.view.GestureDetector.OnDoubleTapListener
    public boolean onDoubleTap(MotionEvent e) {
      onDoubleClick();
      return super.onDoubleTap(e);
    }

    @Override // android.view.GestureDetector.SimpleOnGestureListener,
              // android.view.GestureDetector.OnGestureListener
    public void onLongPress(MotionEvent e) {
      onLongClick();
      super.onLongPress(e);
    }

    @Override // android.view.GestureDetector.SimpleOnGestureListener,
              // android.view.GestureDetector.OnGestureListener
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
      try {
        float diffY = e2.getY() - e1.getY();
        float diffX = e2.getX() - e1.getX();
        if (Math.abs(diffX) > Math.abs(diffY)) {
          if (Math.abs(diffX) > TouchListener.this.touchSlop) {
            if (diffX > 0.0f) {
              onSwipeRight();
            } else {
              onSwipeLeft();
            }
          }
        } else if (Math.abs(diffY) > TouchListener.this.touchSlop) {
          if (diffY > 0.0f) {
            onSwipeDown();
          } else {
            onSwipeUp();
          }
        }
      } catch (Exception exception) {
        exception.printStackTrace();
      }
      return false;
    }

    public void onSwipeRight() {
      StationBaseAdapter.C0137b bVar = (StationBaseAdapter.C0137b) TouchListener.this.currentView.getTag();
      C0162a.m9a("onSwipeRight pos " + bVar.posInList);
      changeDeleteButton(bVar.deleteBtn, 0);
    }

    public void onSwipeLeft() {
      StationBaseAdapter.C0137b bVar = (StationBaseAdapter.C0137b) TouchListener.this.currentView.getTag();
      C0162a.m9a("onSwipeLeft pos " + bVar.posInList);
      changeDeleteButton(bVar.deleteBtn, 8);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void changeDeleteButton(final View v, final int visibility) {
      switch (visibility) {
        case 0:
          v.setClickable(true);
          v.setOnClickListener(
              new View
                  .OnClickListener() { // from class:
                                       // com.ex.dabplayer.pad.activity.TouchListener.GestureListener.1
                @Override // android.view.View.OnClickListener
                public void onClick(final View v2) {
                  final int pos = ((Integer) v2.getTag()).intValue();
                  DialogInterface.OnClickListener dialogClickListener =
                      new DialogInterface
                          .OnClickListener() { // from class:
                                               // com.ex.dabplayer.pad.activity.TouchListener.GestureListener.1.1
                        @Override // android.content.DialogInterface.OnClickListener
                        public void onClick(DialogInterface dialog, int which) {
                          switch (which) {
                            case -1:
                              TouchListener.this.activity.onDeleteButtonClicked(pos);
                              break;
                          }
                          GestureListener.this.changeDeleteButton(v2, 8);
                        }
                      };
                  StationBaseAdapter aVar = (StationBaseAdapter) TouchListener.this.activity.getListView().getAdapter();
                  String label = (String) aVar.getItem(pos);
                  String msg =
                      TouchListener.this.activity.getResources().getString(R.string.Delete)
                          + " '"
                          + label
                          + "' ?";
                  AlertDialog.Builder builder =
                      new AlertDialog.Builder(TouchListener.this.activity);
                  builder
                      .setTitle(R.string.dialog_heading_confirmation)
                      .setIcon(R.drawable.btn_delete)
                      .setMessage(msg)
                      .setPositiveButton(
                          TouchListener.this.activity.getResources().getString(17039379),
                          dialogClickListener)
                      .setNegativeButton(
                          TouchListener.this.activity.getResources().getString(17039369),
                          dialogClickListener)
                      .show();
                }
              });
          break;
        case 8:
          v.setClickable(false);
          v.setOnClickListener(null);
          break;
        default:
          return;
      }
      TouchListener.this.activity.runOnUiThread(
          new Runnable() { // from class:
                           // com.ex.dabplayer.pad.activity.TouchListener.GestureListener.2
            @Override // java.lang.Runnable
            public void run() {
              v.setVisibility(visibility);
            }
          });
    }

    public void onSwipeUp() {}

    public void onSwipeDown() {}

    public void onClick() {
      StationBaseAdapter.C0137b bVar = (StationBaseAdapter.C0137b) TouchListener.this.currentView.getTag();
      C0162a.m9a("onClick pos " + bVar.posInList);
      TouchListener.this.activity.onStationClicked(bVar.posInList);
    }

    public void onDoubleClick() {}

    public void onLongClick() {
      StationBaseAdapter.C0137b bVar = (StationBaseAdapter.C0137b) TouchListener.this.currentView.getTag();
      C0162a.m9a("onLongClick pos " + bVar.posInList);
      TouchListener.this.activity.toggleFavoriteAtPosition(
          TouchListener.this.currentView, bVar.posInList);
    }
  }
}
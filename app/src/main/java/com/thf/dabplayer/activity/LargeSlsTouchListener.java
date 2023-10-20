package com.thf.dabplayer.activity;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import com.thf.dabplayer.utils.C0162a;

/* renamed from: com.ex.dabplayer.pad.activity.LargeSlsTouchListener */
/* loaded from: classes.dex */
public class LargeSlsTouchListener implements View.OnTouchListener {
  private GestureDetector gestureDetector;
  private Player player;
  private final int touchSlop;

  public LargeSlsTouchListener(Player parentActivity) {
    this.player = parentActivity;
    this.gestureDetector = new GestureDetector(parentActivity, new GestureListener());
    this.touchSlop = ViewConfiguration.get(parentActivity.getContext()).getScaledTouchSlop();
  }

  @Override // android.view.View.OnTouchListener
  public boolean onTouch(View v, MotionEvent event) {
    return this.gestureDetector.onTouchEvent(event);
  }

  /* renamed from: com.ex.dabplayer.pad.activity.LargeSlsTouchListener$GestureListener */
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
          if (Math.abs(diffX) > LargeSlsTouchListener.this.touchSlop) {
            if (diffX > 0.0f) {
              onSwipeRight();
            } else {
              onSwipeLeft();
            }
          }
        } else if (Math.abs(diffY) > LargeSlsTouchListener.this.touchSlop) {
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
      C0162a.m9a("LargeSlsTL::onSwipeRight");
      LargeSlsTouchListener.this.player.flipViews(true);
    }

    public void onSwipeLeft() {
      C0162a.m9a("LargeSlsTL::onSwipeLeft");
      LargeSlsTouchListener.this.player.flipViews(false);
    }

    public void onSwipeUp() {
      C0162a.m9a("LargeSlsTL::onSwipeUp");
      /*MotImage image = LargeSlsTouchListener.this.player.getMotImage();
      if (image != null) {
        image.setBrightness(100);
      }
            */
    }

    public void onSwipeDown() {
      C0162a.m9a("LargeSlsTL::onSwipeDown");
      /*
            MotImage image = LargeSlsTouchListener.this.player.getMotImage();
      if (image != null) {
        image.setBrightness(
            LargeSlsTouchListener.this
                .player
                .context
                .getSharedPreferences(SettingsActivity.prefname_settings, 0)
                .getInt(SettingsActivity.pref_key_dim_percent, 50));
      }
            */
    }

    public void onClick() {
      C0162a.m9a("LargeSlsTL::onClick");
      // LargeSlsTouchListener.this.player.onMotClicked();
    }

    public void onDoubleClick() {}

    public void onLongClick() {
      C0162a.m9a("LargeSlsTL::onLongClick");
      LargeSlsTouchListener.this.player.onMotLongClicked();
    }
  }
}

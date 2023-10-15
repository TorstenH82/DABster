package com.thf.dabplayer.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;

/* renamed from: com.ex.dabplayer.pad.activity.StationViewTouchHelper */
/* loaded from: classes.dex */
public class StationViewTouchHelper implements View.OnTouchListener {
    private float lastY = 0.0f;
    private final TouchListener mTL;

    /* JADX INFO: Access modifiers changed from: package-private */
    public StationViewTouchHelper(Context context, TouchListener touchListener) {
        this.mTL = touchListener;
    }

    @Override // android.view.View.OnTouchListener
    @SuppressLint({"ClickableViewAccessibility"})
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getActionMasked()) {
            case 0:
                this.lastY = event.getY();
                break;
            case 2:
                if (this.mTL != null && this.mTL.getCurrentMoveDirection() == 1) {
                    boolean retval = false;
                    if (Math.abs(this.lastY - event.getY()) > 0.0f) {
                        retval = true;
                    }
                    this.lastY = event.getY();
                    return retval;
                }
                break;
        }
        return false;
    }
}

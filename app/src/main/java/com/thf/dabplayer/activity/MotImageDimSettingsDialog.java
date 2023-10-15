package com.thf.dabplayer.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import com.thf.dabplayer.R;
import com.thf.dabplayer.utils.C0162a;

/* renamed from: com.ex.dabplayer.pad.activity.MotImageDimSettingsDialog */
/* loaded from: classes.dex */
public class MotImageDimSettingsDialog implements SeekBar.OnSeekBarChangeListener, DialogInterface.OnClickListener {
    private TextView mBrightnessValueText;
    private final Context mContext;
    private int mDimValPercent;
    private ResultListener mListener;
    private MotImage mMotImage;

    /* renamed from: com.ex.dabplayer.pad.activity.MotImageDimSettingsDialog$ResultListener */
    /* loaded from: classes.dex */
    public interface ResultListener {
        void onDialogResult(MotImageDimSettingsDialog motImageDimSettingsDialog, int i);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public MotImageDimSettingsDialog(Context context, @Nullable ResultListener resultListener) {
        this.mListener = null;
        this.mContext = context;
        this.mListener = resultListener;
        this.mDimValPercent = context.getSharedPreferences(SettingsActivity.prefname_settings, 0).getInt(SettingsActivity.pref_key_dim_percent, 50);
        if (this.mDimValPercent < 0 || this.mDimValPercent > 100) {
            this.mDimValPercent = 50;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context, 2);
        View v = LayoutInflater.from(context).inflate(R.layout.brightness_dlg, (ViewGroup) null);
        AlertDialog dlg = builder.setView(v).setPositiveButton(17039370, this).setNegativeButton(17039360, this).show();
        this.mBrightnessValueText = (TextView) dlg.findViewById(R.id.brightnessValueTextDlg);
        setBrightnessValueText(this.mDimValPercent);
        this.mMotImage = (MotImage) dlg.findViewById(R.id.motImageDlg);
        if (this.mMotImage != null) {
            Drawable motImageLogo = context.getResources().getDrawable(R.drawable.radio_white);
            if (motImageLogo != null) {
                this.mMotImage.setImage(motImageLogo, 1);
                this.mMotImage.setMaxScaleFactor(1.0f);
            } else {
                C0162a.m9a("radio_white null");
            }
        }
        SeekBar seekBar = (SeekBar) dlg.findViewById(R.id.brightnessSeekBarDlg);
        if (seekBar != null) {
            seekBar.setOnSeekBarChangeListener(this);
            seekBar.setProgress(this.mDimValPercent);
        }
    }

    @Override // android.widget.SeekBar.OnSeekBarChangeListener
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (progress >= 0 && progress <= 100) {
            this.mDimValPercent = progress;
            setBrightnessValueText(progress);
            setMotImageBrightness(progress);
        }
    }

    @Override // android.widget.SeekBar.OnSeekBarChangeListener
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override // android.widget.SeekBar.OnSeekBarChangeListener
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    @Override // android.content.DialogInterface.OnClickListener
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case -2:
                if (this.mListener != null) {
                    this.mListener.onDialogResult(this, -2);
                    return;
                }
                return;
            case -1:
                this.mContext.getSharedPreferences(SettingsActivity.prefname_settings, 0).edit().putInt(SettingsActivity.pref_key_dim_percent, this.mDimValPercent).apply();
                if (this.mListener != null) {
                    this.mListener.onDialogResult(this, -1);
                    return;
                }
                return;
            default:
                return;
        }
    }

    private void setBrightnessValueText(int brightnessPercent) {
        if (this.mBrightnessValueText != null) {
            String dimValPercent = brightnessPercent + " %";
            this.mBrightnessValueText.setText(dimValPercent);
        }
    }

    private void setMotImageBrightness(int brightnessPercent) {
        if (this.mMotImage != null) {
            this.mMotImage.setBrightness(brightnessPercent);
        }
    }
}

package com.thf.dabplayer.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.thf.dabplayer.R;
import java.util.ArrayList;
import java.util.List;


public class SimpleDialog {
  AlertDialog.Builder builder;

  private Activity activity;
  private ProgressBar progressBar;
  private TextView textView;
  private TextView dialogLink;
  private ImageView imageView;

  private AlertDialog dialog;
  private SimpleDialogListener listener;

  private List<String> radioList = new ArrayList<>();
  private RadioGroup radioGroup;

  private int radioIdx = -1;
  private int selectedIdx = 0;

  public interface SimpleDialogListener {
    public void onClick(boolean positive, int selection);
  }

    
    
    
  public SimpleDialog(Activity activity, String title, SimpleDialogListener listener) {
    this.activity = activity;
    this.listener = listener;

    this.builder =
        new AlertDialog.Builder(activity,R.style.MaterialAlertDialog_rounded )
            // new ContextThemeWrapper(this.context, (int) R.style.AlertDialogCustom))
            .setTitle(title)
            // .setMessage(initMessage)
            .setCancelable(false)
            .setIcon(R.drawable.radio);

    this.builder.setPositiveButton(
        android.R.string.ok,
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            if (SimpleDialog.this.listener != null) {
              SimpleDialog.this.listener.onClick(true, SimpleDialog.this.selectedIdx);
            }
          }
        });

    this.builder.setNegativeButton(
        android.R.string.cancel,
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            if (SimpleDialog.this.listener != null) {
              SimpleDialog.this.listener.onClick(false, -1);
            }
          }
        });

    LayoutInflater layoutInflater = LayoutInflater.from(activity);
    final View alertView = layoutInflater.inflate(R.layout.dialog_progress, null);
    this.builder.setView(alertView);

    this.dialog = builder.create();
   this.dialog.getWindow().setBackgroundDrawableResource( R.drawable.dialog_rounded_background);
   // this.dialog.getWindow().getDecorView().setBackgroundDrawable(activity.getDrawable(R.drawable.dialogbackground));
    this.dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

    this.progressBar = alertView.findViewById(R.id.dialog_progress);
    this.progressBar.setVisibility(View.GONE);

    this.imageView = alertView.findViewById(R.id.dialog_image);
    this.imageView.setVisibility(View.GONE);

    this.textView = (TextView) alertView.findViewById(R.id.dialog_message);
    this.textView.setVisibility(View.GONE);

    this.radioGroup = alertView.findViewById(R.id.dialog_radiogroup);
    this.radioGroup.setVisibility(View.GONE);

    this.dialog.create();

    this.dialogLink = (TextView) alertView.findViewById(R.id.dialog_link);
    this.dialogLink.setVisibility(View.GONE);

    this.dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setVisibility(View.GONE);
    this.dialog.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.GONE);
  }

  public SimpleDialog(Activity activity, String title) {
    this(activity, title, null);
  }

  public SimpleDialog(Activity activity) {
    this(activity, null, null);
  }

  public void setPositiveButton(String text) {
    this.dialog.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.VISIBLE);
    this.dialog.getButton(AlertDialog.BUTTON_POSITIVE).setText(text);
  }

  public void setNegativeButton(String text) {
    this.dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setVisibility(View.VISIBLE);
    this.dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setText(text);
  }

  public void setTitle(String title) {
    this.dialog.setTitle(title);
  }

  public void setMessage(String message) {
    this.textView.setVisibility(View.VISIBLE);
    this.textView.setText(message);
  }

  public void setUrl(String url) {
    this.dialogLink.setVisibility(View.VISIBLE);
    this.dialogLink.setText(url);
  }

  public void setImage(Drawable drawable) {
    this.imageView.setVisibility(View.VISIBLE);
    this.imageView.setImageDrawable(drawable);
  }

  public void addRadio(String entry) {
    this.radioGroup.setVisibility(View.VISIBLE);
    RadioButton rdbtn = new RadioButton(this.activity);
    rdbtn.setId(View.generateViewId());
    rdbtn.setText(entry);
    radioIdx++;
    final int radioIdxFin = radioIdx;
    rdbtn.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            SimpleDialog.this.selectedIdx = radioIdxFin;
          }
        });
    this.radioGroup.addView(rdbtn);
    ((RadioButton) this.radioGroup.getChildAt(0)).setChecked(true);
  }

  public void setChecked(int idx) {
    ((RadioButton) this.radioGroup.getChildAt(idx)).setChecked(true);
    this.selectedIdx = idx;
  }

  public void showProgress(boolean showProgress) {
    if (showProgress) {
      this.progressBar.setVisibility(View.VISIBLE);
    } else {
      this.progressBar.setVisibility(View.GONE);
    }
  }

  private boolean isShowing = false;

  public void show() {
    dialog.show();
    this.isShowing = true;
  }

  public void dismiss() {
    dialog.dismiss();
    this.isShowing = false;
  }

  public boolean isShowing() {
    return this.isShowing;
  }
}

package com.thf.dabplayer.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.thf.dabplayer.R;
import java.util.ArrayList;
import java.util.List;

public class SimpleDialog {
  private Activity activity;
  private ProgressBar progressBar;
  private TextView textView;

  private AlertDialog dialog;
  private SimpleDialogListener listener;

  private List<String> radioList = new ArrayList<>();
  private RadioGroup radioGroup;

  private int radioIdx = -1;
  private int selectedIdx = 0;

  public interface SimpleDialogListener {
    public void onClick(boolean positive, int selection);
  }

  public SimpleDialog(
      Activity activity, String title, boolean showButtons, SimpleDialogListener listener) {
    this.activity = activity;
    this.listener = listener;

    // this.setCancelable(false);
    // this.requestWindowFeature(Window.FEATURE_NO_TITLE);
    // this.setContentView(R.layout.dialog_progress);

    AlertDialog.Builder builder =
        new AlertDialog.Builder(activity)
            // new ContextThemeWrapper(this.context, (int) R.style.AlertDialogCustom))
            .setTitle(title)
            // .setMessage(initMessage)
            .setCancelable(false)
            .setIcon(R.drawable.radio);

    if (showButtons) {
      builder.setPositiveButton(
          R.string.next,
          new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
              if (SimpleDialog.this.listener != null) {
                SimpleDialog.this.listener.onClick(true, SimpleDialog.this.selectedIdx);
              }
            }
          });

      builder.setNegativeButton(
          R.string.cancel,
          new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
              if (SimpleDialog.this.listener != null) {
                SimpleDialog.this.listener.onClick(false, -1);
              }
            }
          });
    }

    LayoutInflater layoutInflater = LayoutInflater.from(activity);
    final View alertView = layoutInflater.inflate(R.layout.dialog_progress, null);
    builder.setView(alertView);

    dialog = builder.create();
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

    this.progressBar = alertView.findViewById(R.id.loader);
    this.progressBar.setVisibility(View.GONE);

    this.textView = (TextView) alertView.findViewById(R.id.loading_msg);
    this.textView.setVisibility(View.GONE);

    this.radioGroup = alertView.findViewById(R.id.radiogroup);
    this.radioGroup.setVisibility(View.GONE);
  }

  public SimpleDialog(Activity activity, String title) {
    this(activity, title, false, null);
  }

  public void setMessage(String message) {
    this.textView.setVisibility(View.VISIBLE);
    this.textView.setText(message);
  }

  public void addRadio(String entry) {
    this.radioGroup.setVisibility(View.VISIBLE);
    RadioButton rdbtn = new RadioButton(this.activity);
    rdbtn.setId(View.generateViewId());
    rdbtn.setText(entry);
    radioIdx++;
    final int radioIdxFin =radioIdx;
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

  public void showProgress() {
    this.progressBar.setVisibility(View.VISIBLE);
    this.progressBar.setProgress(50);
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

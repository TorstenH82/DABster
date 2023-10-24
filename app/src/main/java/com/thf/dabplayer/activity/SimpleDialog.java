package com.thf.dabplayer.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.thf.dabplayer.R;

public class SimpleDialog {
  private Activity activity;
  private ProgressBar progressBar;
  private TextView textView;

  private AlertDialog dialog;
  private SimpleDialogListener listener;

  private String[] arrRadio = null;
  private RadioGroup radioGroup;

  public interface SimpleDialogListener {
    public void onClick(boolean positive);
  }

  public SimpleDialog(Activity activity, String title, String[] arrRadio) {
    this.arrRadio = arrRadio;
    this.activity = activity;

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

    if (arrRadio != null) {
      builder.setPositiveButton(
          R.string.next,
          new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
              if (SimpleDialog.this.listener != null) {
                SimpleDialog.this.listener.onClick(true);
              }
            }
          });
    }
    // .setIcon(R.drawable.radio);
    // .setView(R.layout.dialog_progress);

    LayoutInflater layoutInflater = LayoutInflater.from(activity);
    final View alertView = layoutInflater.inflate(R.layout.dialog_progress, null);
    builder.setView(alertView);

    dialog = builder.create();
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

    this.progressBar = alertView.findViewById(R.id.loader);
    this.textView = (TextView) alertView.findViewById(R.id.loading_msg);
    // this.textView.setText(initMessage);
    this.radioGroup = alertView.findViewById(R.id.radiogroup);
    // addRadioButtons(5);
  }

  public SimpleDialog(Activity activity, String title) {
    this(activity, title, null);
  }

  public void addRadioButtons(int number) {
    // this.radioGroup.setOrientation(LinearLayout.HORIZONTAL);
    this.radioGroup.setVisibility(View.VISIBLE);
    //
    for (int i = 1; i <= number; i++) {
      final int a = i;
      RadioButton rdbtn = new RadioButton(this.activity);
      rdbtn.setId(View.generateViewId());
      rdbtn.setText("Radio " + rdbtn.getId());
      rdbtn.setOnClickListener(
          new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              Toast.makeText(activity, a + "", 1).show();
            }
          });
      this.radioGroup.addView(rdbtn);
    }
  }

  public void setMessage(String message) {
    this.textView.setVisibility(View.VISIBLE);
    this.textView.setText(message);
  }

  public void show() {
    dialog.show();
  }

  public void dismiss() {
    dialog.dismiss();
  }
}

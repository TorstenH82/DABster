package com.thf.dabplayer.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.thf.dabplayer.R;

public class SimpleDialog extends Dialog {
  private ProgressBar progressBar;
  private TextView textView;
  private RadioGroup mRgAllButtons;

  private SimpleDialogListener listener;

  public interface SimpleDialogListener {
    public void onClick(boolean positive);
  }

  public SimpleDialog(Activity activity, String initMessage) {
    super(activity);

    this.setCancelable(false);
    this.requestWindowFeature(Window.FEATURE_NO_TITLE);
    this.setContentView(R.layout.dialog_progress);

    this.setPositiveButton(
        R.string.next,
        new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            if (listener != null) listener.onClick(true);
          }
        });

    this.progressBar = findViewById(R.id.loader);
    this.textView = findViewById(R.id.loading_msg);
    this.textView.setText(initMessage);

    mRgAllButtons = findViewById(R.id.radiogroup);
  }

  public void addRadioButtons(int number) {
    mRgAllButtons.setOrientation(LinearLayout.HORIZONTAL);
    //
    for (int i = 1; i <= number; i++) {
      RadioButton rdbtn = new RadioButton(this);
      rdbtn.setId(View.generateViewId());
      rdbtn.setText("Radio " + rdbtn.getId());
      rdbtn.setOnClickListener(this);
      mRgAllButtons.addView(rdbtn);
    }
  }

  public void setMessage(String message) {
    this.textView.setText(message);
  }
}

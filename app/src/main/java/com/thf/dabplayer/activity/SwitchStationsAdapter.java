package com.thf.dabplayer.activity;

// import SwitchStationsAdapter.MyViewHolder;
import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import androidx.recyclerview.widget.RecyclerView;
import com.thf.dabplayer.dab.DabSubChannelInfo;
import com.thf.dabplayer.dab.LogoDb;
import com.thf.dabplayer.dab.LogoDbHelper;
import java.util.ArrayList;
import java.util.List;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import com.thf.dabplayer.R;

public class SwitchStationsAdapter
    extends RecyclerView.Adapter<SwitchStationsAdapter.MyViewHolder> {

  private List<DabSubChannelInfo> stationList;
  private static int selectedPosition = 0;
  private Context context;
  private int motImagePosition = -1;
  private BitmapDrawable motImage;
  private LogoDb logoDb = null;
  private float brightness = 1f;

  public interface Listener {
    void onItemClick(int position);

    void onLongPress(int position);
  }

  private final Listener listener;

  public SwitchStationsAdapter(Context context, Listener listener) {
    this.listener = listener;
    this.context = context;
    this.stationList = new ArrayList<>();
    this.logoDb = LogoDbHelper.getInstance(context);
  }

  public SwitchStationsAdapter(Context context, Listener listener, List<DabSubChannelInfo> list) {
    this.listener = listener;
    this.context = context;
    this.stationList = list;
    this.logoDb = LogoDbHelper.getInstance(context);
  }

  class MyViewHolder extends RecyclerView.ViewHolder implements View.OnTouchListener {

    TextView txtIndex, txtTitle;
    ImageView imgLogo, imgFavor, imgDelete;

    MyViewHolder(View v) {
      super(v);

      v.setOnTouchListener(this);

      txtIndex = v.findViewById(R.id.index);
      txtTitle = v.findViewById(R.id.title);
      imgLogo = v.findViewById(R.id.logo);
    }

    final GestureDetector gestureDetector =
        new GestureDetector(
            context,
            new GestureDetector.SimpleOnGestureListener() {
              public void onLongPress(MotionEvent event) {
                Log.d("dabster", "Longpress detected");
                listener.onLongPress(getAdapterPosition());
                super.onLongPress(event);
              }

              @Override
              public boolean onSingleTapUp(MotionEvent event) {
                // triggers after onDown only for single tap
                Log.d("dabster", "Click detected");
                listener.onItemClick(getAdapterPosition());
                return true;
              }

              @Override
              public boolean onDown(MotionEvent event) {
                // triggers first for both single tap and long press
                return true;
              }
            });

    @Override
    public boolean onTouch(View v, MotionEvent event) {
      selectedPosition = getAdapterPosition();
      return gestureDetector.onTouchEvent(event);
    }
  }

  @Override
  public SwitchStationsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View itemView =
        LayoutInflater.from(parent.getContext()).inflate(R.layout.item_switch, parent, false);
    return new MyViewHolder(itemView);
  }

  @Override
  public void onBindViewHolder(MyViewHolder holder, int position) {
    DabSubChannelInfo sci = stationList.get(position);

    holder.txtIndex.setText(
        String.format("%0" + (getItemCount() + "").length() + "d", position + 1));
    holder.txtTitle.setText(sci.mLabel);

    if (position != this.motImagePosition) {
      BitmapDrawable logoDrawable = logoDb.getLogo(sci.mLabel, sci.mSID);
      if (logoDrawable != null) {
        holder.imgLogo.setImageDrawable(logoDrawable);
      } else {
        holder.imgLogo.setImageResource(R.drawable.radio);
      }
    } else {
      holder.imgLogo.setImageDrawable(this.motImage);
    }

    holder.imgLogo.setAlpha(this.brightness);
  }

  @Override
  public int getItemCount() {
    if (stationList == null) return 0;
    return stationList.size();
  }

  public void setList(List<DabSubChannelInfo> list) {
    if (list != null) {
      this.stationList = list;
      this.notifyDataSetChanged();
    }
  }

  public void setMot(BitmapDrawable motImage, int position) {
    int oldPosition = this.motImagePosition;
    this.motImagePosition = position;
    if (oldPosition != -1 && oldPosition != position) {
      this.notifyItemChanged(oldPosition);
    }
    this.motImage = motImage;
    if (position != -1) this.notifyItemChanged(position);
  }

  public void refreshWithoutNewData(int position) {
    this.notifyItemChanged(position);
  }

  public void dimLogo(float brightness, int position) {
    this.brightness = brightness;
    if (position != -1) this.notifyItemChanged(position);
  }
}

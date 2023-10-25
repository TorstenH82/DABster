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
import java.util.List;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import com.thf.dabplayer.R;

public class SwitchStationsAdapter
    extends RecyclerView.Adapter<SwitchStationsAdapter.MyViewHolder> {
  private static final String TAG = "Dabster";

  private List<DabSubChannelInfo> stationList;
  private static int selectedPosition = 0;
  private Context context;
  private boolean showAdditionalInfos = false;
  private int motImagePosition = -1;
  private BitmapDrawable motImage;
  private LogoDb logoDb = null;

  public interface Listener {
    void onItemClick(int position);

    void onLongPress(int position);
  }

  private final Listener listener;

  public SwitchStationsAdapter(
      Context context,
      Listener listener,
      List<DabSubChannelInfo> list,
      boolean showAdditionalInfos) {
    this.listener = listener;
    this.context = context;
    this.showAdditionalInfos = showAdditionalInfos;
    this.stationList = list;
    this.logoDb = LogoDbHelper.getInstance(context);

    if ("RMX3301EEA".equals(Build.PRODUCT)) {
      DabSubChannelInfo dummy = new DabSubChannelInfo();
      dummy.mLabel = "This is a very long station name";
      list.add(dummy);
      dummy = new DabSubChannelInfo();
      dummy.mLabel = "Radio 456";
      list.add(dummy);
    }
  }

  class MyViewHolder extends RecyclerView.ViewHolder implements View.OnTouchListener {

    TextView txtIndex, txtTitle, txtInfos;
    ImageView imgLogo, imgFavor, imgDelete;

    MyViewHolder(View v) {
      super(v);

      // v.setOnClickListener(this);
      v.setOnTouchListener(this);

      txtIndex = v.findViewById(R.id.index);
      txtTitle = v.findViewById(R.id.title);
      txtInfos = v.findViewById(R.id.infos);
      imgLogo = v.findViewById(R.id.logo);
      // imgFavor = v.findViewById(R.id.favor);
      // imgDelete = v.findViewById(R.id.bt_delete);

      if (!showAdditionalInfos) {
        txtInfos.setVisibility(View.GONE);
      }
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
      /*if (event.getAction() == MotionEvent.ACTION_DOWN) {
        clearPosition();
        v.setBackgroundColor(Color.TRANSPARENT);
        listener.onTouch();
      }
            */
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
    // holder.txtInfos.setText(station.ItemInfos);

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
  }

  @Override
  public int getItemCount() {
    if (stationList == null) return 0;
    return stationList.size();
  }

  public void setMot(BitmapDrawable motImage, int position) {
    int oldPosition = this.motImagePosition;
    this.motImagePosition = position;
    if (oldPosition != -1 && oldPosition != position) {
      this.notifyItemChanged(oldPosition);
    }
    this.motImage = motImage;
    this.notifyItemChanged(position);
  }
}

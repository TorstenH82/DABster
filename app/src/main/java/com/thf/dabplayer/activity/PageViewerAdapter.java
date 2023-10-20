package com.thf.dabplayer.activity;

// import PageViewerAdapter.MyViewHolder;
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
import com.thf.dabplayer.dab.LogoDb;
import java.util.List;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import com.thf.dabplayer.R;

public class PageViewerAdapter
    extends RecyclerView.Adapter<PageViewerAdapter.MyViewHolder> {
  private static final String TAG = "Dabster";

  private List<StationItem> stationList;
  private static int selectedPosition = 0;
  private Context context;
  private boolean showAdditionalInfos = false;
  private int motImagePosition = -1;
  private BitmapDrawable motImage;

  public interface Listener {
    void onItemClick(int position);

    void onLongPress(int position);
  }

  private final Listener listener;

  public PageViewerAdapter(
      Context context, Listener listener, List<StationItem> list, boolean showAdditionalInfos) {
    this.listener = listener;
    this.context = context;
    this.showAdditionalInfos = showAdditionalInfos;
    this.stationList = list;
        
    if ("RMX3301EEA".equals(Build.PRODUCT)) {
      StationItem dummy = new StationItem();
      dummy.Index = 1;
      dummy.ItemFavorite = 0;
      dummy.ItemTitle = "This is a very long station name";
      list.add(dummy);
      dummy = new StationItem();
      dummy.Index = 2;
      dummy.ItemFavorite = 0;
      dummy.ItemTitle = "Radio 456";
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
  public PageViewerAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View itemView =
        LayoutInflater.from(parent.getContext()).inflate(R.layout.item_switch, parent, false);
    return new MyViewHolder(itemView);
  }

  @Override
  public void onBindViewHolder(MyViewHolder holder, int position) {
    StationItem station = stationList.get(position);

    holder.txtIndex.setText(
        String.format("%0" + (getItemCount() + "").length() + "d", station.Index));
    holder.txtTitle.setText(station.ItemTitle);
    holder.txtInfos.setText(station.ItemInfos);

    String pathToLogo = station.ItemLogo;
    BitmapDrawable logoDrawable = null;

    if (position != this.motImagePosition) {
      if (pathToLogo != null) {
        logoDrawable = LogoDb.getBitmapForStation(this.context, pathToLogo);
      }
      if (logoDrawable == null) {
        logoDrawable = LogoAssets.getBitmapForStation(this.context, station.ItemTitle);
      }
      if (logoDrawable != null) {
        holder.imgLogo.setImageDrawable(logoDrawable);
      } else {
        holder.imgLogo.setImageResource(R.drawable.radio);
      }
    } else {
      holder.imgLogo.setImageDrawable(this.motImage);
    }

    // if (position == selectedPosition || selectedPosition == -99) {

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

  /*

  public void setItems(List<StationItem> newList) {
    stationList = newList;
    selectedPosition = 0;
    this.notifyDataSetChanged();
  }

  public void clearPosition() {
    if (stationList == null) return;
    if (selectedPosition != -99) {
      Integer oldPosition = selectedPosition;
      selectedPosition = -99;

      this.notifyItemRangeChanged(0, stationList.size());
    }
  }

  public Integer setPosition() {

    if (stationList == null) return null;

    // this.notifyItemRangeChanged(0, appDataList0.size());

    if (selectedPosition == -99) {
      selectedPosition = 0;
      this.notifyItemRangeChanged(0, stationList.size());
      // this.notifyItemChanged(selectedPosition);
    } else {
      Integer oldPosition = selectedPosition;
      selectedPosition++;
      if (selectedPosition > getItemCount() - 1) {
        selectedPosition = 0;
      }
      this.notifyItemChanged(selectedPosition);
      this.notifyItemChanged(oldPosition);
    }
    return selectedPosition;
  }

  public StationItem getCurrentApp() {
    try {
      return stationList.get(selectedPosition);
    } catch (ArrayIndexOutOfBoundsException ex) {
      Log.e(TAG, "current application cannot be provided based on index " + selectedPosition);
      return null;
    }
  }
    */

}

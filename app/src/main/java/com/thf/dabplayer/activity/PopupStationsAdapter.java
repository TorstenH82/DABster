package com.thf.dabplayer.activity;

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

public class PopupStationsAdapter extends RecyclerView.Adapter<PopupStationsAdapter.MyViewHolder> {

  private List<DabSubChannelInfo> stationList;
  private static int selectedPosition = 0;
  private Context context;
  private LogoDb logoDb = null;

  public interface Listener {
    void onItemClick(int position);

    void onLongPress(int position);
  }

  public PopupStationsAdapter(Context context) {
    this.context = context;
    this.logoDb = LogoDbHelper.getInstance(context);
  }

  class MyViewHolder extends RecyclerView.ViewHolder {

    TextView txtTitle;
    ImageView imgLogo, imgFavor, imgDelete;

    MyViewHolder(View v) {
      super(v);
      txtTitle = v.findViewById(R.id.title);
      imgLogo = v.findViewById(R.id.logo);
    }
  }

  @Override
  public PopupStationsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View itemView =
        LayoutInflater.from(parent.getContext()).inflate(R.layout.popup_item_switch, parent, false);
    return new MyViewHolder(itemView);
  }

  @Override
  public void onBindViewHolder(MyViewHolder holder, int position) {
    DabSubChannelInfo sci = stationList.get(position);

    holder.txtTitle.setText(sci.mLabel);

    BitmapDrawable logoDrawable = logoDb.getLogo(sci.mLabel, sci.mSID);

    if (logoDrawable != null) {
      holder.imgLogo.setImageDrawable(logoDrawable.mutate());
    } else {
      holder.imgLogo.setImageDrawable(context.getDrawable(R.drawable.radio).mutate());
    }

    ColorMatrix matrix = new ColorMatrix();
    matrix.setSaturation(1f);

    if (position == 1) {
      holder.txtTitle.setAlpha(1f);
      holder.imgLogo.setAlpha(1f);
    } else {
      holder.txtTitle.setAlpha(0.5f);
      holder.imgLogo.setAlpha(0.5f);
      matrix.setSaturation(0);
    }

    ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
    holder.imgLogo.setColorFilter(filter);
  }

  @Override
  public int getItemCount() {
    if (stationList == null) return 0;
    return stationList.size();
  }

  public void setStations(List<DabSubChannelInfo> stationList) {
    this.stationList = stationList;
    if (stationList != null) {
      notifyDataSetChanged();
    }
  }
}

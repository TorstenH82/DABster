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
  private String[] arrPages;

  public interface Listener {
    void onItemClick(int position);

    void onLongPress(int position);
  }

  private final Listener listener;

  public PageViewerAdapter(Context context, Listener listener) {
    this.listener = listener;
    this.context = context;
    this.arrPages = {"m", "m", "i"};  
  }

  class MyViewHolder extends RecyclerView.ViewHolder implements View.OnTouchListener, View.OnClickListener {

    TableLayout layMemory, layInfo;
    LinearLayout layMemory1, layMemory2, layMemory3, layMemory4, layMemory5, layMemory6;
    ImageView imageViewMemory1, imageViewMemory2, imageViewMemory3, imageViewMemory4, imageViewMemory5, imageViewMemory6;
    TextView textViewMemory1, textViewMemory2, textViewMemory3, textViewMemory4, textViewMemory5, textViewMemory6:
      
    MyViewHolder(View v) {
      super(v);

      // v.setOnClickListener(this);
      v.setOnTouchListener(this);

    layMemory = v.findViewById(R.id.memoryTable);
    layMemory1 = findViewById(R.id.memory1);
    layMemory1.setOnLongClickListener(this);
    layMemory1.setOnClickListener(this);
    imageViewMemory1 = findViewById(R.id.memory1Img);
    textViewMemory1 = findViewById(R.id.memory1Tv);       
    layMemory2 = findViewById(R.id.memory2);
    layMemory2.setOnLongClickListener(this);
    layMemory2.setOnClickListener(this);
    imageViewMemory2 = findViewById(R.id.memory2Img);
    textViewMemory2 = findViewById(R.id.memory2Tv);  
    layMemory3 = findViewById(R.id.memory3);
    layMemory3.setOnLongClickListener(this);
    layMemory3.setOnClickListener(this);
    imageViewMemory3 = findViewById(R.id.memory3Img);
    textViewMemory3 = findViewById(R.id.memory3Tv);
    layMemory4 = findViewById(R.id.memory4);
    layMemory4.setOnLongClickListener(this);
    layMemory4.setOnClickListener(this);
    imageViewMemory4 = findViewById(R.id.memory4Img);
    textViewMemory4 = findViewById(R.id.memory4Tv); 
    layMemory5 = findViewById(R.id.memory5);
    layMemory5.setOnLongClickListener(this);
    layMemory5.setOnClickListener(this);
    imageViewMemory5 = findViewById(R.id.memory5Img);
    textViewMemory5 = findViewById(R.id.memory5Tv);
    layMemory6 = findViewById(R.id.memory6);
    layMemory6.setOnLongClickListener(this);
    layMemory6.setOnClickListener(this);
    imageViewMemory6 = findViewById(R.id.memory6Img);
    textViewMemory6 = findViewById(R.id.memory6Tv);
        
    layInfo = v.findViewById(R.id.infoTable);


    }

    
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
    @Override
    public void onClick(View view) {
    }
  }

  @Override
  public PageViewerAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View itemView =
        LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_memory, parent, false);
    return new MyViewHolder(itemView);
  }

  @Override
  public void onBindViewHolder(MyViewHolder holder, int position) {
    String page = arrPages[position];
    switch (page) {
        case "m":
            
            break:
        case "i":

            break;
    }
    //holder.txtTitle.setText(station.ItemTitle);
  }

  @Override
  public int getItemCount() {
    if (stationList == null) return 0;
    return stationList.size();
  }

  public void setItems(List<StationItem> newList) {
    stationList = newList;
    selectedPosition = 0;
    this.notifyDataSetChanged();
  }

}

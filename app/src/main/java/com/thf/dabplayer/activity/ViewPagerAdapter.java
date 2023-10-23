package com.thf.dabplayer.activity;

// import ViewPagerAdapter.MyViewHolder;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;
import com.thf.dabplayer.dab.DabSubChannelInfo;
import com.thf.dabplayer.dab.LogoDb;
import com.thf.dabplayer.dab.LogoDbHelper;
import com.thf.dabplayer.service.DabService;
import com.thf.dabplayer.utils.Strings;
import java.util.ArrayList;
import java.util.List;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import com.thf.dabplayer.R;

public class ViewPagerAdapter extends RecyclerView.Adapter<ViewPagerAdapter.MyViewHolder> {
  private static final String TAG = "Dabster";

  private static int selectedPageIdx = 0;
  private Context context;
  private LogoDb logoDb = null;
  private boolean showAdditionalInfos = false;
  private int motImagePosition = -1;
  private BitmapDrawable motImage;
  private List<String> listPages;
  private List<DabSubChannelInfo> listSci = new ArrayList<>();
  private int numPages;

  String serviceName = "";
  String pty = "";
  String ensemble = "";
  String serviceId = "";
  String frequency = "";
  String bitrate = "";
  String audiocodec = "";
  String audiobitrate = "";
  String signalquality = "";
  String servicefollowing = "";
  String servicelog = "";

  public interface Listener {
    void onItemClick(int memoryPos);

    void onLongPress(int memoryPos);
  }

  private final Listener listener;

  public ViewPagerAdapter(Context context, Listener listener, int numPages) {
    this.listener = listener;
    this.context = context;
    this.logoDb = LogoDbHelper.getInstance(context);

    this.listPages = new ArrayList<>();
    this.numPages = numPages;

    for (int i = 0; i < numPages; i++) {
      this.listPages.add("m");
    }
    this.listPages.add("i");
  }

  class MyViewHolder extends RecyclerView.ViewHolder
      implements View.OnLongClickListener, View.OnClickListener {

    TableLayout layMemory; // , layInfo;
    ScrollView layInfo;
    LinearLayout layMemory1, layMemory2, layMemory3, layMemory4, layMemory5, layMemory6;
    ImageView imageViewMemory1,
        imageViewMemory2,
        imageViewMemory3,
        imageViewMemory4,
        imageViewMemory5,
        imageViewMemory6;
    TextView textViewMemory1,
        textViewMemory2,
        textViewMemory3,
        textViewMemory4,
        textViewMemory5,
        textViewMemory6;
    TextView txtServiceName,
        txtPty,
        txtEnsemble,
        txtServiceId,
        txtFrequency,
        txtBitrate,
        txtAudiocodec,
        txtAudiobitrate,
        txtSignalquality,
        txtServicefollowing,
        txtServicelog;

    MyViewHolder(View v) {
      super(v);
      layMemory = v.findViewById(R.id.memoryTable);
      layMemory1 = v.findViewById(R.id.memory1);
      layMemory1.setOnLongClickListener(this);
      layMemory1.setOnClickListener(this);
      imageViewMemory1 = v.findViewById(R.id.memory1Img);
      textViewMemory1 = v.findViewById(R.id.memory1Tv);
      layMemory2 = v.findViewById(R.id.memory2);
      layMemory2.setOnLongClickListener(this);
      layMemory2.setOnClickListener(this);
      imageViewMemory2 = v.findViewById(R.id.memory2Img);
      textViewMemory2 = v.findViewById(R.id.memory2Tv);
      layMemory3 = v.findViewById(R.id.memory3);
      layMemory3.setOnLongClickListener(this);
      layMemory3.setOnClickListener(this);
      imageViewMemory3 = v.findViewById(R.id.memory3Img);
      textViewMemory3 = v.findViewById(R.id.memory3Tv);
      layMemory4 = v.findViewById(R.id.memory4);
      layMemory4.setOnLongClickListener(this);
      layMemory4.setOnClickListener(this);
      imageViewMemory4 = v.findViewById(R.id.memory4Img);
      textViewMemory4 = v.findViewById(R.id.memory4Tv);
      layMemory5 = v.findViewById(R.id.memory5);
      layMemory5.setOnLongClickListener(this);
      layMemory5.setOnClickListener(this);
      imageViewMemory5 = v.findViewById(R.id.memory5Img);
      textViewMemory5 = v.findViewById(R.id.memory5Tv);
      layMemory6 = v.findViewById(R.id.memory6);
      layMemory6.setOnLongClickListener(this);
      layMemory6.setOnClickListener(this);
      imageViewMemory6 = v.findViewById(R.id.memory6Img);
      textViewMemory6 = v.findViewById(R.id.memory6Tv);

      layInfo = v.findViewById(R.id.infoTable);
      txtServiceName = (TextView) v.findViewById(R.id.details_station_name);
      txtPty = (TextView) v.findViewById(R.id.details_pty);
      txtEnsemble = (TextView) v.findViewById(R.id.details_ensemble);
      txtServiceId = (TextView) v.findViewById(R.id.details_service_id);
      txtFrequency = (TextView) v.findViewById(R.id.details_frequency);
      txtBitrate = (TextView) v.findViewById(R.id.details_bitrate);
      txtAudiocodec = (TextView) v.findViewById(R.id.details_audiocodec);
      txtAudiobitrate = (TextView) v.findViewById(R.id.details_audiobitrate);
      txtSignalquality = (TextView) v.findViewById(R.id.details_signalquality);
      txtServicefollowing = (TextView) v.findViewById(R.id.details_servicefollowing);
      txtServicelog = (TextView) v.findViewById(R.id.details_servicelog);
    }

    @Override
    public void onClick(View view) {
      ViewPagerAdapter.selectedPageIdx = getAdapterPosition();
      int memoryNum = getMemoryNum(view.getId(), getAdapterPosition());
      Toast.makeText(context, "clicked on memory position " + memoryNum, Toast.LENGTH_LONG).show();
      listener.onItemClick(memoryNum);
    }

    @Override
    public boolean onLongClick(View view) {
      ViewPagerAdapter.selectedPageIdx = getAdapterPosition();
      listener.onLongPress(getMemoryNum(view.getId(), getAdapterPosition()));
      return true;
    }

    public int getMemoryNum(int buttonId, int pageIdx) {
      int offset = pageIdx * 6;
      int buttonNo = 0;
      if (buttonId == R.id.memory1) {
        buttonNo = 1;
      } else if (buttonId == R.id.memory2) {
        buttonNo = 2;
      } else if (buttonId == R.id.memory3) {
        buttonNo = 3;
      } else if (buttonId == R.id.memory4) {
        buttonNo = 4;
      } else if (buttonId == R.id.memory5) {
        buttonNo = 5;
      } else if (buttonId == R.id.memory6) {
        buttonNo = 6;
      } else {
        return -1;
      }
      return buttonNo + offset;
    }
  }

  @Override
  public ViewPagerAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View itemView =
        LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_memory, parent, false);
    return new MyViewHolder(itemView);
  }

  @Override
  public void onBindViewHolder(MyViewHolder holder, int pageIdx) {
    String pageType = listPages.get(pageIdx);
    switch (pageType) {
      case "m":
        holder.layMemory.setVisibility(View.VISIBLE);
        holder.layInfo.setVisibility(View.GONE);

        for (DabSubChannelInfo sci : listSci) {
          ImageView imageViewMemory = null;
          TextView textViewMemory = null;

          if (sci.mFavorite == holder.getMemoryNum(R.id.memory1, pageIdx)) {
            imageViewMemory = holder.imageViewMemory1;
            textViewMemory = holder.textViewMemory1;
          } else if (sci.mFavorite == holder.getMemoryNum(R.id.memory2, pageIdx)) {
            imageViewMemory = holder.imageViewMemory2;
            textViewMemory = holder.textViewMemory2;
          } else if (sci.mFavorite == holder.getMemoryNum(R.id.memory3, pageIdx)) {
            imageViewMemory = holder.imageViewMemory3;
            textViewMemory = holder.textViewMemory3;
          } else if (sci.mFavorite == holder.getMemoryNum(R.id.memory4, pageIdx)) {
            imageViewMemory = holder.imageViewMemory4;
            textViewMemory = holder.textViewMemory4;
          } else if (sci.mFavorite == holder.getMemoryNum(R.id.memory5, pageIdx)) {
            imageViewMemory = holder.imageViewMemory5;
            textViewMemory = holder.textViewMemory5;
          } else if (sci.mFavorite == holder.getMemoryNum(R.id.memory6, pageIdx)) {
            imageViewMemory = holder.imageViewMemory6;
            textViewMemory = holder.textViewMemory6;
          }

          if (imageViewMemory != null) {
            textViewMemory.setText(sci.mLabel);
            String pathToLogo = logoDb.getLogoFilenameForStation(sci.mLabel, sci.mSID);
            BitmapDrawable logoDrawable = null;
            if (pathToLogo != null) {
              logoDrawable = LogoDb.getBitmapForStation(this.context, pathToLogo);
            }
            if (logoDrawable == null) {
              logoDrawable = LogoAssets.getBitmapForStation(this.context, sci.mLabel);
            }
            if (logoDrawable != null) {
              imageViewMemory.setImageDrawable(logoDrawable);
            } else {
              imageViewMemory.setImageResource(R.drawable.radio);
            }
          }
        }

        break;
      case "i":
        holder.layMemory.setVisibility(View.GONE);
        holder.layInfo.setVisibility(View.VISIBLE);

        holder.txtServiceName.setText(this.serviceName);
        holder.txtPty.setText(this.pty);
        holder.txtEnsemble.setText(this.ensemble);
        holder.txtServiceId.setText(this.serviceId);
        holder.txtFrequency.setText(this.frequency);
        holder.txtBitrate.setText(this.bitrate);
        holder.txtAudiocodec.setText(this.audiocodec);
        holder.txtAudiobitrate.setText(this.audiobitrate);
        holder.txtSignalquality.setText(this.signalquality);
        holder.txtServicefollowing.setText(this.servicefollowing);
        holder.txtServicelog.setText(this.servicelog);
        break;
    }
  }

  @Override
  public int getItemCount() {
    if (listPages == null) return 0;
    return listPages.size();
  }

  public void setItems(List<DabSubChannelInfo> listSci) {
    this.listSci = listSci;
    //selectedPageIdx = 0;
    this.notifyDataSetChanged();
  }

  public void setDetails(Intent intent) {
    // update details
    if (intent == null) {
      return;
    }
    //this.serviceName = "";
    if (intent.hasExtra(DabService.EXTRA_STATION)) {
      int totalStations = 0;
      if (intent.hasExtra(DabService.EXTRA_NUMSTATIONS)) {
        totalStations = intent.getIntExtra(DabService.EXTRA_NUMSTATIONS, 0);
      }
      if (totalStations != 0) {
        this.serviceName =
            intent.getStringExtra(DabService.EXTRA_STATION) + " (" + totalStations + ")";
      } else {
        this.serviceName = intent.getStringExtra(DabService.EXTRA_STATION);
      }
    }

    //this.servicefollowing = "";
    if (intent.hasExtra(DabService.EXTRA_SERVICEFOLLOWING)) {
      this.servicefollowing = intent.getStringExtra(DabService.EXTRA_SERVICEFOLLOWING);
    }

    //this.serviceId = "";
    if (intent.hasExtra(DabService.EXTRA_SERVICEID)) {
      int sid = intent.getIntExtra(DabService.EXTRA_SERVICEID, 0);
      this.serviceId = Integer.toHexString(sid);
    }

    //this.frequency = "";
    if (intent.hasExtra(DabService.EXTRA_FREQUENCY_KHZ)) {
      int freq = intent.getIntExtra(DabService.EXTRA_FREQUENCY_KHZ, 0);
      if (freq != 0) {
        float fFreq = freq / 1000.0f;
        this.frequency =
            Strings.freq2channelname(freq) + String.format(" - %,.3f MHz", Float.valueOf(fFreq));
      }
    }

    //this.audiocodec = "";
    if (intent.hasExtra(DabService.EXTRA_AUDIOFORMAT)) {
      this.audiocodec = intent.getStringExtra(DabService.EXTRA_AUDIOFORMAT);
    }

    //this.pty = "";
    if (intent.hasExtra(DabService.EXTRA_PTY)) {
      this.pty = intent.getStringExtra(DabService.EXTRA_PTY);
    }

    //this.ensemble = "";
    if (intent.hasExtra(DabService.EXTRA_ENSEMBLE_NAME)) {
      String name = intent.getStringExtra(DabService.EXTRA_ENSEMBLE_NAME);
      int id = 0;
      if (intent.hasExtra(DabService.EXTRA_ENSEMBLE_ID)) {
        id = intent.getIntExtra(DabService.EXTRA_ENSEMBLE_ID, 0);
      }
      if (id != 0) {
        this.ensemble = String.format("%s (%04X)", name, Integer.valueOf(id));
      } else {
        this.ensemble = name;
      }
    }

    //this.bitrate = "";
    if (intent.hasExtra(DabService.EXTRA_BITRATE)) {
      int bitrate = intent.getIntExtra(DabService.EXTRA_BITRATE, 0);
      if (bitrate != 0) {
        this.bitrate = String.format("%d kbits/s", Integer.valueOf(bitrate));
      }
    }

    //this.audiobitrate = "";
    if (intent.hasExtra(DabService.EXTRA_AUDIOSAMPLERATE)) {
      int samplerate = intent.getIntExtra(DabService.EXTRA_AUDIOSAMPLERATE, 0);
      if (samplerate > 0) {
        if (samplerate % 1000 == 0) {
          this.audiobitrate = String.format("%d kHz", Integer.valueOf(samplerate / 1000));
        } else {
          this.audiobitrate = String.format("%,.1f kHz", Float.valueOf(samplerate / 1000.0f));
        }
      }
    }

    //this.signalquality = "";
    if (intent.hasExtra(DabService.EXTRA_SIGNALQUALITY)) {
      int qual = intent.getIntExtra(DabService.EXTRA_SIGNALQUALITY, -1);
      if (qual >= 0) {
        this.signalquality = String.format("%d", Integer.valueOf(qual));
      }
    }

    //this.servicelog = "";
    if (intent.hasExtra(DabService.EXTRA_SERVICELOG)) {
      this.servicelog = intent.getStringExtra(DabService.EXTRA_SERVICELOG);
    }

    // we only need to update info page
    this.notifyItemChanged(this.numPages);
  }
}

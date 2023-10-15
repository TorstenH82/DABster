package com.thf.dabplayer.activity;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.thf.dabplayer.R;
import com.thf.dabplayer.dab.LogoDb;
import java.util.List;

/* renamed from: com.ex.dabplayer.pad.activity.a */
/* loaded from: classes.dex */
public class StationBaseAdapter extends BaseAdapter {

  /* renamed from: a */
  private List<StationItem> stationList;

  /* renamed from: b */
  private Context context;

  /* renamed from: c */
  private LayoutInflater layoutInflater;

  /* renamed from: d */
  private int selectedIdx = 0;
  private boolean mShowAdditionalInfos;
  private boolean mShowLogosInList;
  private TouchListener mTouchListener;

  public StationBaseAdapter(
      Context context,
      List<StationItem> list,
      boolean showAdditionalInfos,
      TouchListener touchListener,
      boolean showLogosInList) {
    this.context = context;
    this.layoutInflater = LayoutInflater.from(context);
    this.stationList = list;
    this.mShowAdditionalInfos = showAdditionalInfos;
    this.mTouchListener = touchListener;
    this.mShowLogosInList = showLogosInList;
  }

  /* renamed from: a */
  public void setSelectedIndex(int i) {
    this.selectedIdx = i;
  }

  public int getSelectedIndex() {
    return this.selectedIdx;
  }

  @Override // android.widget.Adapter
  public int getCount() {
    return this.stationList.size();
  }

  @Override // android.widget.Adapter
  public Object getItem(int i) {
    return this.stationList.get(i).ItemTitle;
  }

  @Override // android.widget.Adapter
  public long getItemId(int i) {
    return i;
  }

  @Override // android.widget.Adapter
  public View getView(int i, View view, ViewGroup viewGroup) {
    C0137b bVar;
    StationItem item = this.stationList.get(i);
    if (view == null) {
      view = this.layoutInflater.inflate(R.layout.list, viewGroup, false);
      C0137b bVar2 = new C0137b();
      bVar2.f58a = (TextView) view.findViewById(R.id.index);
      bVar2.f59b = (TextView) view.findViewById(R.id.title);
      bVar2.logo = (ImageView) view.findViewById(R.id.logo);
      if (!this.mShowLogosInList) {
        bVar2.logo.setVisibility(8);
      }
      bVar2.infos = (TextView) view.findViewById(R.id.infos);
      if (!this.mShowAdditionalInfos) {
        bVar2.infos.setVisibility(8);
      }
      bVar2.favorBtn = (ImageView) view.findViewById(R.id.favor);
      bVar2.deleteBtn = (ImageView) view.findViewById(R.id.bt_delete);
      if (this.mTouchListener != null) {
        view.setOnTouchListener(this.mTouchListener);
      }
      view.setTag(bVar2);
      bVar = bVar2;
    } else {
      bVar = (C0137b) view.getTag();
    }
    StringBuilder index = new StringBuilder();
    index.append((i + 1) / 10);
    index.append((i + 1) % 10);
    bVar.f58a.setText(index);
    bVar.f59b.setText(item.ItemTitle);
    bVar.infos.setText(item.ItemInfos);
    if (i == this.selectedIdx) {
      view.setBackgroundResource(R.drawable.bg_bar);
    } else {
      view.setBackgroundColor(0);
    }
    updateFavoriteUI(bVar, item.ItemFavorite);
    BitmapDrawable logoDrawable = null;
    String pathToLogo = item.ItemLogo;
    if (pathToLogo != null) {
      logoDrawable = LogoDb.getBitmapForStation(this.context, pathToLogo);
    }
    if (logoDrawable == null) {
      logoDrawable = LogoAssets.getBitmapForStation(this.context, item.ItemTitle);
    }
    if (logoDrawable != null) {
      bVar.logo.setImageDrawable(logoDrawable);
    } else {
      bVar.logo.setImageResource(R.drawable.radio);
    }
    bVar.posInList = i;
    bVar.deleteBtn.setTag(Integer.valueOf(i));
    return view;
  }

  public static void updateFavoriteUI(C0137b bVar, boolean isFavorite) {
    if (isFavorite) {
      bVar.favorBtn.setBackgroundResource(R.drawable.btn_show_p);
    } else {
      bVar.favorBtn.setBackgroundColor(0);
    }
  }

  public void updateFavorite(int i, boolean isFavorite) {
    this.stationList.get(i).ItemFavorite = isFavorite;
  }

  public void changeData(List<StationItem> changed) {
    this.stationList.clear();
    this.stationList.addAll(changed);
    notifyDataSetChanged();
  }

  /* renamed from: com.ex.dabplayer.pad.activity.a$b */
  /* loaded from: classes.dex */
  public class C0137b {

    /* renamed from: a */
    TextView f58a;

    /* renamed from: b */
    TextView f59b;
    ImageView deleteBtn;
    ImageView favorBtn;
    TextView infos;
    ImageView logo;
    int posInList = -1;

    C0137b() {}
  }
}

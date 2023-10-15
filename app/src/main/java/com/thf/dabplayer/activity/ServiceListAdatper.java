package com.thf.dabplayer.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import com.thf.dabplayer.R;
import com.thf.dabplayer.utils.ShareData;
import java.util.ArrayList;
import java.util.HashMap;

/* renamed from: com.ex.dabplayer.pad.activity.ServiceListAdatper */
/* loaded from: classes.dex */
public class ServiceListAdatper extends ListActivity {

    /* renamed from: a */
    private ListView f50a;

    /* renamed from: b */
    public TextView f51b;

    /* renamed from: c */
    private ArrayList f52c = new ArrayList();

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.layout_service_list);
        this.f51b = (TextView) findViewById(R.id.listview_title);
        Intent intent = new Intent();
        intent.putExtra("title", this.f51b.getText());
        setResult(-1, intent);
        this.f52c = (ArrayList) ((ShareData) getIntent().getParcelableExtra("sharedata")).m13a();
        this.f51b.setText(getIntent().getStringExtra("title"));
        this.f50a = getListView();
        this.f50a.setOnItemClickListener(new AdapterViewClickListener(this));
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < this.f52c.size(); i++) {
            HashMap hashMap = new HashMap();
            hashMap.put("Index", new StringBuilder().append(i + 1).toString());
            hashMap.put("ItemTitle", (String) this.f52c.get(i));
            arrayList.add(hashMap);
        }
        this.f50a.setAdapter((ListAdapter) new SimpleAdapter(this, arrayList, R.layout.list, new String[]{"Index", "ItemTitle"}, new int[]{R.id.index, R.id.title}));
    }
}
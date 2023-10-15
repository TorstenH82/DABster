package com.thf.dabplayer.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import com.thf.dabplayer.R;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/* renamed from: com.ex.dabplayer.pad.activity.PtyActivity */
/* loaded from: classes.dex */
public class PtyActivity extends ListActivity implements AdapterView.OnItemClickListener {

    /* renamed from: b */
    private ListView f48b;

    /* renamed from: c */
    private Intent intent;

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        requestWindowFeature(1);
        setContentView(R.layout.pty_dialog);
        this.intent = new Intent();
        this.intent.putExtra("title", "pty dialog");
        setResult(-1, this.intent);
        this.f48b = getListView();
        this.f48b.setOnItemClickListener(this);
        List arrayList = new ArrayList();
        for (int i = 0; i < Player.arrPty.length; i++) {
            HashMap hashMap = new HashMap();
            hashMap.put("Index", Integer.valueOf(i + 1));
            hashMap.put("ItemTitle", Player.arrPty[i]);
            arrayList.add(hashMap);
        }
        this.f48b.setAdapter((ListAdapter) new SimpleAdapter(this, arrayList, R.layout.list_pty, new String[]{"Index", "ItemTitle"}, new int[]{R.id.index, R.id.title}));
    }

    @Override // android.widget.AdapterView.OnItemClickListener
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        setResult((int) id, this.intent);
        finish();
    }
}

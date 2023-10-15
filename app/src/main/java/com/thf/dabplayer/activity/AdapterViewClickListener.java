package com.thf.dabplayer.activity;

import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

/* renamed from: com.ex.dabplayer.pad.activity.p */
/* loaded from: classes.dex */
class AdapterViewClickListener implements AdapterView.OnItemClickListener {

    /* renamed from: a */
    final /* synthetic */ ServiceListAdatper f60a;

    /* JADX INFO: Access modifiers changed from: package-private */
    public AdapterViewClickListener(ServiceListAdatper serviceListAdatper) {
        this.f60a = serviceListAdatper;
    }

    @Override // android.widget.AdapterView.OnItemClickListener
    public void onItemClick(AdapterView adapterView, View view, int i, long j) {
        TextView textView;
        Intent intent = new Intent();
        textView = this.f60a.f51b;
        intent.putExtra("title", textView.getText());
        this.f60a.setResult((int) j, intent);
        this.f60a.finish();
    }
}

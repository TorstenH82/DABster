package com.thf.dabplayer.service;

import android.os.Binder;
import com.thf.dabplayer.utils.Logger;

/* renamed from: com.ex.dabplayer.pad.service.a */
/* loaded from: classes.dex */
public class DabServiceBinder extends Binder {

    /* renamed from: a */
    final DabService dabService;

    public DabServiceBinder(DabService dabService) {
        this.dabService = dabService;
    }

    /* renamed from: a */
    public DabService getService() {
        Logger.d("server getservice");
        return this.dabService;
    }
}
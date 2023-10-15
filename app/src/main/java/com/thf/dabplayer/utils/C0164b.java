package com.thf.dabplayer.utils;

import android.os.Parcel;
import android.os.Parcelable;

/* renamed from: com.ex.dabplayer.pad.utils.b */
/* loaded from: classes.dex */
class C0164b implements Parcelable.Creator {
    @Override // android.os.Parcelable.Creator
    /* renamed from: a */
    public ShareData createFromParcel(Parcel parcel) {
        ShareData shareData = new ShareData();
        shareData.f144a = parcel.readValue(ShareData.class.getClassLoader());
        return shareData;
    }

    @Override // android.os.Parcelable.Creator
    /* renamed from: a */
    public ShareData[] newArray(int i) {
        return new ShareData[i];
    }
}
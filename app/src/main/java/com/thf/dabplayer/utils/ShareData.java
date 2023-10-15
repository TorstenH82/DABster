package com.thf.dabplayer.utils;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;

/* renamed from: com.ex.dabplayer.pad.utils.ShareData */
/* loaded from: classes.dex */
public class ShareData implements Parcelable {
    public static final Parcelable.Creator CREATOR = new C0164b();

    /* renamed from: a and made public*/
    public  Object f144a;

    /* renamed from: b */
    private ArrayList f145b = new ArrayList();

    /* renamed from: a */
    public Object m13a() {
        return this.f144a;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeValue(this.f144a);
    }
}
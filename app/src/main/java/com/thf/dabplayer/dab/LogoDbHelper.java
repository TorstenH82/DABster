package com.thf.dabplayer.dab;

import android.content.Context;

/* renamed from: com.ex.dabplayer.pad.dab.LogoDbHelper */
/* loaded from: classes.dex */
public class LogoDbHelper {
    private static LogoDb mLogoDbInst = null;

    public static LogoDb getInstance(Context context) {
        if (mLogoDbInst == null) {
            mLogoDbInst = new LogoDb(context);
        }
        return mLogoDbInst;
    }
}
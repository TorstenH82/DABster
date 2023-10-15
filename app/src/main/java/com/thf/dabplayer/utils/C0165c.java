package com.thf.dabplayer.utils;

/* renamed from: com.ex.dabplayer.pad.utils.c */
/* loaded from: classes.dex */
public class C0165c {

    /* renamed from: a */
    protected static char[] f147a = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    /* renamed from: a */
    public static String m1a(String str) {
        return "";
    }

    /* renamed from: a */
    public static String m0a(byte[] bArr) {
        StringBuilder stringBuilder = new StringBuilder(bArr.length * 2);
        for (int i = 0; i < bArr.length; i++) {
            stringBuilder.append(f147a[(bArr[i] & 240) >>> 4]);
            stringBuilder.append(f147a[bArr[i] & 15]);
        }
        return stringBuilder.toString();
    }
}
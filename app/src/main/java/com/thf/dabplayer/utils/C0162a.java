package com.thf.dabplayer.utils;

import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.SparseArray;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Calendar;

/* renamed from: com.ex.dabplayer.pad.utils.a */
/* loaded from: classes.dex */
public class C0162a {
    private static final long LogOverflowDetectionThreshold = 2;

    /* renamed from: a */
    private static char[] f146a = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private static BufferedWriter bufWriter = null;
    private static boolean testedForLogDir = false;
    private static long lastLogTimeMs = 0;
    private static String lastOverflowingLogStr = "";
    private static int lastOverflowingLogStrCount = 0;
    private static final CharSequence MyDateTimeFormat = "yyyy-MM-dd_HH-mm-ss";
    public static final SparseArray<String> dabFreqToChannelMap = new SparseArray<String>() { // from class: com.ex.dabplayer.pad.utils.a.1
        {
            append(174928, "5A");
            append(176640, "5B");
            append(178352, "5C");
            append(180064, "5D");
            append(181936, "6A");
            append(183648, "6B");
            append(185360, "6C");
            append(187072, "6D");
            append(188928, "7A");
            append(190640, "7B");
            append(192352, "7C");
            append(194064, "7D");
            append(195936, "8A");
            append(197648, "8B");
            append(199360, "8C");
            append(201072, "8D");
            append(202928, "9A");
            append(204640, "9B");
            append(206352, "9C");
            append(208064, "9D");
            append(209936, "10A");
            append(210096, "10N");
            append(211648, "10B");
            append(213360, "10C");
            append(215072, "10D");
            append(216928, "11A");
            append(217088, "11N");
            append(218640, "11B");
            append(220352, "11C");
            append(222064, "11D");
            append(223936, "12A");
            append(224096, "12N");
            append(225648, "12B");
            append(227360, "12C");
            append(229072, "12D");
            append(230784, "13A");
            append(232496, "13B");
            append(234208, "13C");
            append(235776, "13D");
            append(237488, "13E");
            append(239200, "13F");
        }
    };

    /* renamed from: a_ */
    public static Object m4a_(String str) {
        return null;
    }

    /* renamed from: a */
    public static void m9a(String str) {
        Calendar calendar = Calendar.getInstance();
        long nowMs = calendar.getTimeInMillis();
        if (nowMs - lastLogTimeMs > 2) {
            Log.d("dabster", str);
            lastLogTimeMs = nowMs;
        } else {
            if (lastOverflowingLogStr.equals(str)) {
                lastOverflowingLogStrCount++;
                if (lastOverflowingLogStrCount % 100 == 1) {
                    Log.d("dabster", TextUtils.concat(str, " (repeated " + lastOverflowingLogStrCount + ")").toString());
                }
            } else {
                lastOverflowingLogStrCount = 0;
                lastOverflowingLogStr = str;
                Log.d("dabster", str);
            }
            lastLogTimeMs = nowMs;
        }
        if (!testedForLogDir) {
            testedForLogDir = true;
            try {
                File logpath = new File(Strings.DAB_path() + File.separator + "logs");
                if (logpath.isDirectory()) {
                    String filename = "dab+" + ((Object) DateFormat.format(MyDateTimeFormat, calendar)) + ".log";
                    File logfile = new File(logpath.getAbsolutePath(), File.separator + filename);
                    if (logfile.createNewFile()) {
                        bufWriter = new BufferedWriter(new FileWriter(logfile, true));
                        bufWriter.append((CharSequence) (calendar.getTimeInMillis() + ": DAB+ log started " + ((Object) DateFormat.format(MyDateTimeFormat, calendar))));
                        bufWriter.newLine();
                        bufWriter.flush();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (bufWriter != null) {
            try {
                Calendar cal = Calendar.getInstance();
                String line = cal.getTimeInMillis() + ": " + str;
                bufWriter.append((CharSequence) line);
                bufWriter.newLine();
                bufWriter.flush();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
    }

    public static void copyFile(File src, File dst) throws IOException {
        FileInputStream inStream = new FileInputStream(src);
        FileOutputStream outStream = new FileOutputStream(dst);
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        inChannel.transferTo(0L, inChannel.size(), outChannel);
        inStream.close();
        outStream.close();
    }

    /* renamed from: a */
    public static void m8a(String str, int a) {
        m9a(str + a);
    }

    /* renamed from: a */
    public static void m7a(String str, int a, String str2) {
        m9a(str + a + str2);
    }

    /* renamed from: a */
    public static void m6a(String str, int a, String str2, int a2) {
        m9a(str + a + str2 + a2);
    }

    /* renamed from: a */
    public static void m5a(String str, String str2) {
        m9a(str + str2);
    }
}
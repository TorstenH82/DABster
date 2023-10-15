package com.thf.dabplayer.utils;

import android.support.annotation.NonNull;
import android.util.SparseArray;
import com.thf.dabplayer.dab.DabSubChannelInfo;

/* renamed from: com.ex.dabplayer.pad.utils.RepairEBU */
/* loaded from: classes.dex */
public class RepairEBU {
    public static final char U_ERROR = 65533;
    private static boolean beenHere = false;
    private static boolean needRepair = false;
    private static SparseArray<Byte> reverseEBU = null;
    private static final char[] ebu2unichar = {65533, 280, 302, 370, 258, 278, 270, 536, 538, 266, 65533, 65533, 288, 313, 379, 323, 261, 281, 303, 371, 259, 279, 271, 537, 539, 267, 327, 282, 289, 314, 380, 65533, ' ', '!', '\"', '#', 322, '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ':', ';', '<', '=', '>', '?', '@', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '[', 366, ']', 321, '_', 260, 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 171, 367, 187, 317, 294, 225, 224, 233, 232, 237, 236, 243, 242, 250, 249, 209, 199, 350, 223, 161, 376, 226, 228, 234, 235, 238, 239, 244, 246, 251, 252, 241, 231, 351, 287, 305, 255, 310, 325, 169, 290, 286, 283, 328, 337, 336, 8364, 163, '$', 256, 274, 298, 362, 311, 326, 315, 291, 316, 304, 324, 369, 368, 191, 318, 176, 257, 275, 299, 363, 193, 192, 201, 200, 205, 204, 211, 210, 218, 217, 344, 268, 352, 381, 208, 319, 194, 196, 202, 203, 206, 207, 212, 214, 219, 220, 345, 269, 353, 382, 273, 320, 195, 197, 198, 338, 375, 221, 213, 216, 222, 330, 340, 262, 346, 377, 356, 240, 227, 229, 230, 339, 373, 253, 245, 248, 254, 331, 341, 263, 347, 378, 357, 295};

    private static void needReverseEBU() {
        if (reverseEBU == null) {
            reverseEBU = new SparseArray<>(256);
            for (int i = 0; i < ebu2unichar.length; i++) {
                char c = ebu2unichar[i];
                if (c != 65533) {
                    reverseEBU.put(c, Byte.valueOf((byte) i));
                }
            }
        }
    }

    private static Boolean cpuIsIntel() {
        String arch = System.getProperty("os.arch");
        C0162a.m9a("EBURepair os.arch " + arch);
        return Boolean.valueOf(System.getProperty("os.arch").toLowerCase().contains("x86"));
    }

    private static final boolean isPrintAscii(char c) {
        return c >= ' ' && c < 127;
    }

    private static void append16(@NonNull StringBuilder sb, int c) {
        if (c > 65535) {
            int top = c >> 10;
            sb.append((char) ((55296 | top) - 64));
            c = 56320 | (c & 1023);
        }
        sb.append((char) c);
    }

    private static String fromUTF8(@NonNull byte[] utf) {
        StringBuilder sb = new StringBuilder();
        int utf_sequence = 0;
        int build_char = 0;
        for (byte b : utf) {
            int c = b & 255;
            if (utf_sequence > 0) {
                build_char = (build_char << 6) | (c & 63);
                utf_sequence--;
                if (utf_sequence <= 0) {
                    append16(sb, build_char);
                }
            } else if ((c & 128) != 0) {
                if ((c & 64) == 0) {
                    sb.append(ebu2unichar[c]);
                } else if ((c & 32) == 0) {
                    build_char = c & 31;
                    utf_sequence = 1;
                } else if ((c & 16) == 0) {
                    build_char = (c & 31) - 16;
                    utf_sequence = 2;
                } else {
                    build_char = (c & 7) | 16;
                    utf_sequence = 3;
                }
            } else {
                sb.append((char) c);
            }
        }
        if (utf_sequence > 0) {
            int nc = utf.length;
            while (utf_sequence > 0) {
                build_char <<= 6;
                if (nc < 16) {
                    build_char |= 32;
                }
                utf_sequence--;
            }
            append16(sb, build_char);
        }
        return sb.toString();
    }

    public static String genRepair(@NonNull String in) {
        byte[] ba = new byte[in.length()];
        boolean need_repair = false;
        needReverseEBU();
        for (int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            if (c == '_') {
                c = ' ';
            }
            Byte ebu = reverseEBU.get(new Character(c).charValue());
            if (ebu == null) {
                log("Sample " + quoteString(in) + " contains non-EBU characters");
                ebu = (byte) 63;
            }
            char ebu_char = (char) (ebu.intValue() & 255);
            ba[i] = (byte) ebu_char;
            if (ebu_char != c) {
                need_repair = true;
            }
        }
        if (need_repair) {
            return fromUTF8(ba);
        }
        return null;
    }

    @NonNull
    public static String quoteString(@NonNull String str) {
        StringBuilder sb = new StringBuilder();
        sb.append('\"');
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (!isPrintAscii(c)) {
                if (c < ' ') {
                    sb.append(String.format("\\%03o", Integer.valueOf(c)));
                } else {
                    sb.append(String.format("\\u%04x", Integer.valueOf(c)));
                }
            } else {
                sb.append(c);
            }
        }
        sb.append('\"');
        return sb.toString();
    }

    private static void log(@NonNull String str) {
        C0162a.m9a("EBURepair:" + str);
    }

    private static double likely5(StringBuilder sb, int b5) {
        char c;
        double v;
        int b52 = b5 & 63;
        if (b52 == 32) {
            c = ' ';
            v = 0.55d;
        } else {
            c = ebu2unichar[(b52 + 16) | 64];
            v = 0.4d;
        }
        sb.append(c);
        return v;
    }

    private static double likely6(StringBuilder sb, int b6, boolean is_last) {
        String so_far = sb.toString();
        char c_prev = so_far.charAt(so_far.length() - 1);
        int b62 = b6 & 63;
        if (b62 == 32) {
            if (!is_last) {
                sb.append(' ');
            }
            return 0.95d;
        } else if (b62 == 0 && is_last) {
            return 0.9995d;
        } else {
            if (c_prev == 214 && b62 == 45) {
                sb.append(ebu2unichar[b62]);
                return 0.75d;
            }
            sb.append(ebu2unichar[b62 | 64]);
            return 0.75d;
        }
    }

    public static String doRepair(@NonNull String str) {
        String str2 = str.trim();
        StringBuilder sb = new StringBuilder();
        double likelyhood = 1.0d;
        int utf16_seq = 0;
        boolean is_last = false;
        for (int i = 0; i < str2.length(); i++) {
            char c = str2.charAt(i);
            if (i == str2.length() - 1) {
                is_last = true;
            }
            if (utf16_seq != 0) {
                if (c < 56320 || c > 57343) {
                    log(String.format("low surrogate expected after \\u%04x, got \\u%04x", Integer.valueOf(utf16_seq), Integer.valueOf(c)));
                }
                int utf16_seq2 = (utf16_seq << 10) | (c & 1023);
                sb.append(ebu2unichar[(utf16_seq2 >> 18) | 240]);
                likelyhood = likelyhood * likely5(sb, utf16_seq2 >> 12) * likely6(sb, utf16_seq2 >> 6, is_last && (c & '?') == 32) * likely6(sb, utf16_seq2, is_last);
                utf16_seq = 0;
            } else if (c >= ' ' && c < 192) {
                if (ebu2unichar[c] == c) {
                    sb.append(c);
                    likelyhood *= 0.9995d;
                } else {
                    sb.append(ebu2unichar[c]);
                    likelyhood *= 0.9d;
                }
            } else if (c <= 2047) {
                switch (c) {
                    case 228:
                    case 252:
                        sb.append(c);
                        likelyhood *= 0.8d;
                        continue;
                    default:
                        sb.append(ebu2unichar[(c >> 6) | 192]);
                        likelyhood *= likely6(sb, c, is_last);
                        continue;
                }
            } else if (c >= 55296 && c <= 57343) {
                utf16_seq = c & 2047;
            } else {
                sb.append(ebu2unichar[(c >> '\f') | 224]);
                likelyhood = likelyhood * likely6(sb, c >> 6, is_last && (c & 31) == 0) * likely6(sb, c, is_last);
            }
        }
        if (utf16_seq != 0) {
            sb.append(U_ERROR);
            likelyhood = 0.0d;
        }
        if (likelyhood < 0.99d) {
            log(String.format("repair for %s is %s (%s) [%.1f%%]", quoteString(str2), sb.toString(), quoteString(sb.toString()), Double.valueOf(100.0d * likelyhood)));
        }
        return sb.toString();
    }

    private static String repair(@NonNull String str) {
        if (beenHere) {
            if (!needRepair) {
                return str;
            }
        } else {
            beenHere = true;
            needRepair = cpuIsIntel().booleanValue();
            if (!needRepair) {
                log("not needed on this platform");
                return str;
            }
        }
        return doRepair(str);
    }

    public static DabSubChannelInfo fixLabels(@NonNull DabSubChannelInfo info) {
        info.mLabel = repair(info.mLabel);
        info.mEnsembleLabel = repair(info.mEnsembleLabel);
        return info;
    }
}
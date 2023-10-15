package com.thf.dabplayer.dab;

import com.thf.dabplayer.utils.C0162a;

/* JADX INFO: Access modifiers changed from: package-private */
/* renamed from: com.ex.dabplayer.pad.dab.ServiceLink */
/* loaded from: classes.dex */
public class ServiceLink {
    final DabDec dab;

    public ServiceLink(DabDec dc) {
        this.dab = dc;
    }

    public void read(DabSubChannelInfo sc, int[] freqs, int[] sids) {
        int eid = sc.mEID;
        int sid = sc.mSID;
        C0162a.m9a(String.format("service link eid: %04x sid: %04x", Integer.valueOf(eid), Integer.valueOf(sid)));
        for (int i = 0; i < freqs.length; i++) {
            freqs[i] = 0;
            sids[i] = 0;
        }
        this.dab.decoder_fic_find_service_link(freqs, sids, eid, sid);
        StringBuilder sb = new StringBuilder("service link freq:");
        for (int f : freqs) {
            if (f != 0) {
                sb.append(String.format(" %02x/%d", Integer.valueOf((f >> 24) & 255), Integer.valueOf(16777215 & f)));
            }
        }
        C0162a.m9a(sb.toString());
        StringBuilder sb2 = new StringBuilder("service link id:");
        for (int s : sids) {
            if (s != 0) {
                sb2.append(String.format(" %04x", Integer.valueOf(s)));
            }
        }
        C0162a.m9a(sb2.toString());
    }
}
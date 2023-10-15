package com.thf.dabplayer.dab;

/* renamed from: com.ex.dabplayer.pad.dab.Dab */
/* loaded from: classes.dex */
public class DabDec {
    public native int dab_api_close(int i);

    public native int dab_api_echo(int i);

    public native int dab_api_get_fic_data(byte[] bArr);

    public native int dab_api_get_msc_data(byte[] bArr);

    public native int dab_api_get_signal(int i);

    public native int dab_api_init(int i, byte[] bArr, int i2);

    public native int dab_api_power_off(int i);

    public native int dab_api_power_on(int i);

    public native int dab_api_set_msc_size(int i);

    public native int dab_api_set_subid(int i);

    public native int dab_api_tune(int i);

    public native int dab_api_version(int i);

    public native int dab_get_image(byte[] bArr, byte[] bArr2, byte[] bArr3);

    public native int dab_get_new_pgm_bitrate(int i, int i2, byte[] bArr);

    public native int dab_get_pgm_index(int i, int i2, byte b, byte b2);

    public native void decoder_close(int i);

    public native int decoder_decode(int i, byte[] bArr);

    public native int decoder_feed_data(int i, byte[] bArr, int i2);

    public native int decoder_fic_deinit();

    public native int decoder_fic_find_service_link(int[] iArr, int[] iArr2, int i, int i2);

    public native int decoder_fic_get_service_count();

    public native int decoder_fic_get_subch_info(Object obj, char c);

    public native int decoder_fic_get_usage();

    public native int decoder_fic_init(byte[] bArr);

    public native int decoder_fic_parse(byte[] bArr, int i, int i2);

    public native int decoder_fic_reset(int i);

    public native int decoder_get_channels(int i);

    public native int decoder_get_dls(int i, byte[] bArr);

    public native int decoder_get_mot_data(byte[] bArr, byte[] bArr2);

    public native int decoder_get_samplerate(int i);

    public native int decoder_init(int i);

    public native int decoder_is_feed_data(int i);

    public native int decoder_msc2aac(byte[] bArr, int i, byte[] bArr2);

    public native int decoder_reset_ensemble_info(int i);

    static {
        System.loadLibrary("dab");
    }
}
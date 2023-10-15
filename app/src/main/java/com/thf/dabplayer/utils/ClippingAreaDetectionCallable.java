package com.thf.dabplayer.utils;

import java.util.concurrent.Callable;

/* renamed from: com.ex.dabplayer.pad.utils.ClippingAreaDetectionCallable */
/* loaded from: classes.dex */
public class ClippingAreaDetectionCallable implements Callable<Integer> {
    private boolean m_abortAfterFirstDetection;
    private int m_audioFormat;
    private int m_clipThresholdPercent;
    private int m_numChannels;
    private int m_sampleThreshold;
    private byte[] m_audioBuffer = null;
    private int m_bufferSizeInBytes = 0;
    private int m_fromSample = 0;
    private int m_toSample = 0;
    private int m_channel = 0;

    public ClippingAreaDetectionCallable(int audioFormat, int numChannels, int sampleThreshold, int clipThresholdPercent, boolean abortAfterFirstDetection) {
        this.m_audioFormat = audioFormat;
        this.m_numChannels = numChannels;
        this.m_sampleThreshold = sampleThreshold;
        this.m_clipThresholdPercent = clipThresholdPercent;
        this.m_abortAfterFirstDetection = abortAfterFirstDetection;
    }

    public void setParams(byte[] audioBuffer, int bufferSizeInBytes, int fromSample, int toSample, int channel) {
        this.m_audioBuffer = audioBuffer;
        this.m_bufferSizeInBytes = bufferSizeInBytes;
        this.m_fromSample = fromSample;
        if (toSample > (this.m_bufferSizeInBytes / this.m_numChannels) / 2) {
            toSample = (this.m_bufferSizeInBytes / this.m_numChannels) / 2;
        }
        this.m_toSample = toSample;
        this.m_channel = channel;
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // java.util.concurrent.Callable
    public Integer call() {
        if (this.m_bufferSizeInBytes < this.m_numChannels * 2 || this.m_audioFormat != 2 || this.m_numChannels < 1 || this.m_numChannels > 2 || this.m_sampleThreshold < 1 || this.m_clipThresholdPercent < 50 || this.m_clipThresholdPercent > 100 || this.m_audioBuffer == null || this.m_fromSample < 1 || this.m_toSample < 1 || this.m_toSample < this.m_fromSample) {
            return -1;
        }
        int clippingThresholdValue = ((this.m_clipThresholdPercent * 32767) + 50) / 100;
        return Integer.valueOf(AudioTools.detectClippingOnChannel(this.m_channel, this.m_audioBuffer, this.m_bufferSizeInBytes, this.m_numChannels, this.m_sampleThreshold, clippingThresholdValue, this.m_abortAfterFirstDetection, this.m_fromSample, this.m_toSample));
    }
}
package com.thf.dabplayer.utils;

import android.media.AudioTrack;
import android.os.Build;
import android.support.annotation.NonNull;
//import android.support.p000v4.internal.view.SupportMenu;
import androidx.core.internal.view.SupportMenu;
/* renamed from: com.ex.dabplayer.pad.utils.AudioTools */
/* loaded from: classes.dex */
public class AudioTools {
    public static final int _16BIT = 2;
    static final boolean _DEBUG = false;

    public static int detectClipping(byte[] audioBuffer, int bufferSizeInBytes, int audioFormat, int numChannels, int sampleThreshold, int clipThresholdPercent, boolean abortAfterFirstDetection, int numParallelJobs) {
        int numClippingAreaLeftMono;
        if (bufferSizeInBytes < numChannels * 2 || audioFormat != 2 || numChannels < 1 || numChannels > 2 || sampleThreshold < 1 || clipThresholdPercent < 50 || clipThresholdPercent > 100 || audioBuffer == null || numParallelJobs < 0 || numParallelJobs > 4) {
            return -1;
        }
        int clippingThresholdValue = (clipThresholdPercent * 32767) / 100;
        int samplesPerChannel = (bufferSizeInBytes / 2) / numChannels;
        int endSample1stHalf = (samplesPerChannel + 1) / 2;
        int rest = endSample1stHalf % sampleThreshold;
        int startSample2ndHalf = (endSample1stHalf + sampleThreshold) - rest;
        if ((numChannels == 1 && numParallelJobs == 2) || (numChannels == 2 && numParallelJobs == 4)) {
            int numClippingAreaLeftMono1stHalf = detectClippingOnChannel(1, audioBuffer, bufferSizeInBytes, numChannels, sampleThreshold, clippingThresholdValue, abortAfterFirstDetection, 1, endSample1stHalf);
            int numClippingAreaLeftMono2ndHalf = detectClippingOnChannel(1, audioBuffer, bufferSizeInBytes, numChannels, sampleThreshold, clippingThresholdValue, abortAfterFirstDetection, startSample2ndHalf, samplesPerChannel);
            numClippingAreaLeftMono = numClippingAreaLeftMono1stHalf + numClippingAreaLeftMono2ndHalf;
        } else {
            numClippingAreaLeftMono = detectClippingOnChannel(1, audioBuffer, bufferSizeInBytes, numChannels, sampleThreshold, clippingThresholdValue, abortAfterFirstDetection, 1, samplesPerChannel);
        }
        int numClippingAreaRight = 0;
        if (numChannels == 2 && (!abortAfterFirstDetection || numClippingAreaLeftMono == 0)) {
            if (numParallelJobs == 4) {
                int numClippingAreaRight1stHalf = detectClippingOnChannel(2, audioBuffer, bufferSizeInBytes, numChannels, sampleThreshold, clippingThresholdValue, abortAfterFirstDetection, 1, endSample1stHalf);
                int numClippingAreaRight2ndHalf = detectClippingOnChannel(2, audioBuffer, bufferSizeInBytes, numChannels, sampleThreshold, clippingThresholdValue, abortAfterFirstDetection, startSample2ndHalf, samplesPerChannel);
                numClippingAreaRight = numClippingAreaRight1stHalf + numClippingAreaRight2ndHalf;
            } else {
                numClippingAreaRight = detectClippingOnChannel(2, audioBuffer, bufferSizeInBytes, numChannels, sampleThreshold, clippingThresholdValue, abortAfterFirstDetection, 1, samplesPerChannel);
            }
        }
        return numClippingAreaLeftMono + numClippingAreaRight;
    }

    public static int detectClippingOnChannel(int channel, byte[] audioBuffer, int bufferSizeInBytes, int numChannels, int sampleThreshold, int clippingThresholdValue, boolean abortAfterFirstDetection, int fromSample, int toSample) {
        int numClippingAreas = 0;
        int idx = ((channel - 1) + ((fromSample - 1) * numChannels)) * 2;
        boolean searchStartOfClippedArea = true;
        int toIdx = ((channel - 1) + ((toSample - 1) * numChannels)) * 2;
        while (idx + 1 < bufferSizeInBytes && idx <= toIdx) {
            int sample = getSampleWithOffset(audioBuffer, idx, 0, numChannels);
            boolean sampleClipped = isClippedSample(sample, clippingThresholdValue);
            if (searchStartOfClippedArea == sampleClipped) {
                int numConsecutiveSamplesInArea = 1;
                for (int s = -1; s >= (-(sampleThreshold - 1)); s--) {
                    if (isSampleInBounds(bufferSizeInBytes, idx, s, numChannels)) {
                        int sample2 = getSampleWithOffset(audioBuffer, idx, s, numChannels);
                        if (searchStartOfClippedArea != isClippedSample(sample2, clippingThresholdValue)) {
                            break;
                        }
                        numConsecutiveSamplesInArea++;
                    }
                }
                for (int s2 = 1; s2 <= sampleThreshold - 1; s2++) {
                    if (isSampleInBounds(bufferSizeInBytes, idx, s2, numChannels)) {
                        int sample3 = getSampleWithOffset(audioBuffer, idx, s2, numChannels);
                        if (searchStartOfClippedArea != isClippedSample(sample3, clippingThresholdValue)) {
                            break;
                        }
                        numConsecutiveSamplesInArea++;
                    }
                }
                if (numConsecutiveSamplesInArea >= sampleThreshold) {
                    if (searchStartOfClippedArea) {
                        numClippingAreas++;
                    }
                    searchStartOfClippedArea = !searchStartOfClippedArea;
                }
            }
            if (abortAfterFirstDetection && numClippingAreas > 0) {
                break;
            }
            idx += numChannels * sampleThreshold * 2;
        }
        return numClippingAreas;
    }

    private static boolean isClippedSample(int sample, int clippingThresholdValue) {
        return (sample >= 0 && sample >= clippingThresholdValue) || (sample < 0 && sample <= (-clippingThresholdValue));
    }

    private static boolean isSampleInBounds(int bufferSizeInBytes, int idx, int offset, int numChannels) {
        return ((offset * 2) * numChannels) + idx >= 0 && ((offset * 2) * numChannels) + idx < bufferSizeInBytes;
    }

    private static int getSampleWithOffset(byte[] audioBuffer, int idx, int offset, int numChannels) {
        return (short) ((((audioBuffer[(idx + 1) + ((offset * 2) * numChannels)] & 255) << 8) | (audioBuffer[(offset * 2 * numChannels) + idx] & 255)) & SupportMenu.USER_MASK);
    }

    public static void setVolume(@NonNull AudioTrack track, float volume) {
        if (Build.VERSION.SDK_INT >= 21) {
            track.setVolume(volume);
        } else {
            track.setStereoVolume(volume, volume);
        }
    }
}
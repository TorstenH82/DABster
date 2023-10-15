package com.thf.dabplayer.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/* renamed from: com.ex.dabplayer.pad.utils.ClippingAreaDetection */
/* loaded from: classes.dex */
public class ClippingAreaDetection {
    private int mChannels;
    private List<Long> mClipDetectionDurations = new ArrayList();
    private ThreadPoolExecutor mExecutor;
    private List<ClippingAreaDetectionCallable> mWorkers;
    private List<Future<Integer>> mWorkersResults;

    public ClippingAreaDetection(int channels) {
        this.mExecutor = null;
        this.mWorkers = null;
        this.mWorkersResults = null;
        if (channels < 1 || channels > 2) {
            throw new IllegalArgumentException("channels=" + channels + " not allowed");
        }
        this.mChannels = channels;
        this.mExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
        this.mWorkers = new ArrayList(4);
        this.mWorkersResults = new ArrayList(4);
        for (int i = 0; i < 4; i++) {
            ClippingAreaDetectionCallable w = new ClippingAreaDetectionCallable(2, this.mChannels, 5, 95, true);
            this.mWorkers.add(w);
        }
    }

    public boolean areSamplesClipped(byte[] audioBuffer, int bufferSizeInBytes) {
        int channels = this.mChannels;
        for (int channel = 0; channel < channels; channel++) {
            ClippingAreaDetectionCallable w = this.mWorkers.get(channel * channels);
            w.setParams(audioBuffer, bufferSizeInBytes, 1, ((bufferSizeInBytes / channels) / 2) / 2, channel + 1);
            Future<Integer> workerResult = this.mExecutor.submit(w);
            this.mWorkersResults.add(channel * channels, workerResult);
            ClippingAreaDetectionCallable w2 = this.mWorkers.get((channel * channels) + 1);
            w2.setParams(audioBuffer, bufferSizeInBytes, (((bufferSizeInBytes / channels) / 2) / 2) + 1, (bufferSizeInBytes / channels) / 2, channel + 1);
            Future<Integer> workerResult2 = this.mExecutor.submit(w2);
            this.mWorkersResults.add((channel * channels) + 1, workerResult2);
        }
        int numClippedAreas = 0;
        for (Future<Integer> future : this.mWorkersResults) {
            try {
                if (future.get().intValue() != -1) {
                    numClippedAreas += future.get().intValue();
                }
            } catch (InterruptedException | CancellationException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        this.mWorkersResults.clear();
        return numClippedAreas > 0;
    }
}
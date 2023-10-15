package com.thf.dabplayer.utils;

import java.io.File;

/* renamed from: com.ex.dabplayer.pad.utils.DirCleaner */
/* loaded from: classes.dex */
public class DirCleaner {
    private final File mFile;

    public DirCleaner(File file) {
        this.mFile = file;
    }

    public boolean clean() {
        File[] listFiles;
        if (this.mFile != null && this.mFile.exists() && this.mFile.isDirectory()) {
            for (File file : this.mFile.listFiles()) {
                delete(file);
            }
            return true;
        }
        return false;
    }

    private void delete(File file) {
        File[] listFiles;
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                delete(child);
            }
        }
        file.delete();
    }
}
package com.thf.dabplayer.dab;

import java.io.Serializable;

/* renamed from: com.ex.dabplayer.pad.dab.SubChannelInfo */
/* loaded from: classes.dex */
public class DabSubChannelInfo implements Serializable {
  public static final int AUDIOCODEC_HEAAC = 63;
  public static final int AUDIOCODEC_MP2_BACKGROUND = 1;
  public static final int AUDIOCODEC_MP2_FOREGROUND = 0;
  public static final int AUDIOCODEC_MP2_MULTICHANNEL = 2;
  public byte mAbbreviatedFlag;
  public int mBitrate;
  public int mEID;
  public String mEnsembleLabel;
  public int mFreq;
  public int mFavorite;
  public String mLabel;
  public int mPS;
  public byte mPty;
  public int mSCID;
  public int mSID;
  public byte mSubChannelId;
  public byte mType;
  // ECC='E0' [ Germany ]
  // public String mECC;

  public int compare(DabSubChannelInfo subChannelInfo, DabSubChannelInfo subChannelInfo2) {
    return subChannelInfo.mLabel.compareTo(subChannelInfo2.mLabel);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DabSubChannelInfo other = (DabSubChannelInfo) o;
    return (this.mSID == other.mSID && this.mSubChannelId == other.mSubChannelId);
  }

  public DabSubChannelInfo() {}

  public DabSubChannelInfo(boolean init) {
    if (init) {
      this.mAbbreviatedFlag = (byte) 0;
      this.mBitrate = 0;
      this.mFreq = 0;
      this.mLabel = "";
      this.mPty = (byte) 0;
      this.mSID = 0;
      this.mSubChannelId = (byte) 0;
      this.mType = (byte) 0;
      this.mFavorite = 0;
    }
  }

  public DabSubChannelInfo(DabSubChannelInfo s) {
    this.mAbbreviatedFlag = s.mAbbreviatedFlag;
    this.mBitrate = s.mBitrate;
    this.mEID = s.mEID;
    this.mEnsembleLabel = s.mEnsembleLabel;
    this.mFreq = s.mFreq;
    this.mLabel = s.mLabel;
    this.mPS = s.mPS;
    this.mPty = s.mPty;
    this.mSCID = s.mSCID;
    this.mSID = s.mSID;
    this.mSubChannelId = s.mSubChannelId;
    this.mType = s.mType;
    this.mFavorite = s.mFavorite;
  }
}

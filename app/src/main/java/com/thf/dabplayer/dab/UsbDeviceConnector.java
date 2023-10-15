package com.thf.dabplayer.dab;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbManager;
import com.thf.dabplayer.utils.C0162a;

/* renamed from: com.ex.dabplayer.pad.dab.k */
/* loaded from: classes.dex */
public class UsbDeviceConnector {

  /* renamed from: a */
  private String deviceName;

  /* renamed from: b */
  private Context context;

  /* renamed from: c */
  private UsbManager usbManager;

  /* renamed from: d */
  private UsbDevice usbDevice;

  /* renamed from: e */
  private UsbDeviceConnection usbDeviceConnection;

  /* renamed from: f */
//  private UsbEndpoint f112f = null;

  /* renamed from: g */
//  private UsbEndpoint f113g = null;

  /* renamed from: h */
//  private UsbEndpoint f114h = null;

  public UsbDeviceConnector(UsbManager usbManager, UsbDevice usbDevice, Context context) {
    this.usbManager = usbManager;
    this.usbDevice = usbDevice;
    this.context = context;
    this.deviceName = usbDevice.getDeviceName();
  }

  /* renamed from: a */
  public int getUsbConFileDescriptor() {
    this.usbDeviceConnection = this.usbManager.openDevice(this.usbDevice);
    int fileDescriptor = this.usbDeviceConnection.getFileDescriptor();
    C0162a.m9a("interfaces: " + this.usbDevice.getInterfaceCount());
    return fileDescriptor;
  }

  /* renamed from: b */
  public void closeUsbDeviceConnection() {
    this.usbDeviceConnection.close();
  }
    
    public String getUsbDeviceName() {
        return this.deviceName;
    }
    
}

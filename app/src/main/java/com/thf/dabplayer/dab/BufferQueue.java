package com.thf.dabplayer.dab;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class BufferQueue {

  private BlockingQueue<Byte> bufferQueue;

  public BufferQueue(int i) {
    this.bufferQueue = new ArrayBlockingQueue<>(i);
  }

  public int readBuffer(byte[] bArr, int i) {
    if (this.bufferQueue.size() < i) {
      i = this.bufferQueue.size();
    }
    bArr = new byte[i];
    for (int a = 0; a < i; a++) {
      bArr[a] = this.bufferQueue.poll();
    }
    return i;
  }

  public int writeBuffer(byte[] bArr) {
    for (int a = 0; a < bArr.length - 1; a++) {
      if (!this.bufferQueue.offer(bArr[a])) {
        this.bufferQueue.poll();
        try {
          this.bufferQueue.put(bArr[a]);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
    return 1;
  }

  public void reset() {
    this.bufferQueue.clear();
  }

  public int getNumSamplesAvailable() {
    return this.bufferQueue.size();
  }

  public int getBufferSize() { // free
    return this.bufferQueue.remainingCapacity();
  }
}

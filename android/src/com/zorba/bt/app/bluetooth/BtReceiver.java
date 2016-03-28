package com.zorba.bt.app.bluetooth;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import com.zorba.bt.app.bluetooth.BtHwLayer;
import com.zorba.bt.app.bluetooth.NotificationListener;

public class BtReceiver extends Thread {
   String error = null;
   BtHwLayer hwLayer = null;
   InputStream inStream = null;
   
   
   OutputStream outStream = null;
   Object lock = new Object();
   HashMap responseQueue = new HashMap();
   boolean shouldStop = false;

   public BtReceiver(BtHwLayer var1, InputStream var2) {
      this.hwLayer = var1;
      this.inStream = var2;
      this.start();
   }

   private void addNotification(int param1) {
      // $FF: Couldn't be decompiled
   }

   private void addResponse(int param1, int param2) {
      // $FF: Couldn't be decompiled
   }

   public void close() throws Exception {
      this.shouldStop = true;
      this.responseQueue.clear();
      this.inStream.close();
      this.responseQueue = null;
      this.inStream = null;
   }

   public byte[] getData(int param1) {
      return null;
   }

   public boolean isConnected() {
      boolean var1;
      if(this.shouldStop) {
         var1 = false;
      } else {
         var1 = true;
      }

      return var1;
   }

   public void run() {
      // $FF: Couldn't be decompiled
   }

   
}

package com.zorba.bt.app.bluetooth;

import java.io.OutputStream;

import com.zorba.bt.app.CommonUtils;
import com.zorba.bt.app.bluetooth.BtHwLayer;

public class BtSender {
   String error = null;
   BtHwLayer hwLayer = null;
   OutputStream outStream = null;

   public BtSender(BtHwLayer var1, OutputStream var2) {
      this.hwLayer = var1;
      this.outStream = var2;
   }

   public void close() throws Exception {
      this.outStream.close();
      this.outStream = null;
   }

   public boolean sendCmd(byte[] var1) {
      boolean sent = true;
      try {
    	 CommonUtils.printBytes("Write", var1);
         this.outStream.write(var1);
         this.outStream.flush();
      } catch (Exception var3) {
         this.error = var3.getMessage();
         sent = false;
      }
      return sent;
   }
}

package com.zorba.bt.app.bluetooth;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import com.zorba.bt.app.CommonUtils;
import com.zorba.bt.app.bluetooth.BtHwLayer;
import com.zorba.bt.app.bluetooth.NotificationListener;

public class BtReceiver extends Thread {
   String error = null;
   BtHwLayer hwLayer = null;
   InputStream inStream = null;
   NotificationListener notificationListener = null;
   ConnectionListener connectionListener = null;
   
   
   OutputStream outStream = null;
   Object lock = new Object();
   HashMap<Integer, byte[]> responseQueue = new HashMap<Integer, byte[]>();
   boolean shouldStop = false;

   public BtReceiver(BtHwLayer hwLayer, InputStream is) {
      this.hwLayer = hwLayer;
      this.inStream = is;
      this.start();
   }
   
   public void close() throws Exception {
      this.shouldStop = true;
      this.responseQueue.clear();
      this.inStream.close();
      this.responseQueue = null;
      this.inStream = null;
   }

   public byte[] getData(int reqno) {
	   byte readbytes[] = null;
		synchronized (lock) {
			try {
				lock.wait(1000);
				readbytes = responseQueue.remove(reqno);
				return readbytes;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return readbytes;
   }

   public boolean isConnected() {
      return (!this.shouldStop);
   }

   public void run() {
	   while( true ){
		   if( shouldStop) {
			   return;
		   }
		   try{
			   byte[] recvBytes = new byte[1024];
			   int numRead = inStream.read(recvBytes);
			   if( numRead == -1)
				   continue;
			   if( numRead == 0) {
				   continue;
			   }
			   byte[] readBytes = new byte[numRead];
			   for(int i=0; i<numRead; i++)
				   readBytes[i] = recvBytes[i];
			   
			   int cmd = readBytes[0];
			   int reqno = readBytes[1];
			   hwLayer.printBytes("Read..", readBytes);
			   byte []data = new byte[numRead-2];
				for(int i=0; i<data.length; i++)
					data[i] = readBytes[i+2];
			   if (cmd == 36) {
					byte reqid = readBytes[1];
					byte numdevs = readBytes[2];
					byte alldevs = (byte)0xFF;
					if( numdevs == alldevs) {
						data = new byte[CommonUtils.getMaxNoDevices()*2];
						byte devindex = 1;
						for (int i = 3; i < data.length; i++) {
							byte status = data[i];
							data[devindex*2] = devindex;
							data[devindex*2+1] = status;
						}
					} else {
						data = new byte[readBytes.length - 3];
						for (int i = 0; i < data.length; i++)
							data[i] = readBytes[i + 3];
					}
					notificationListener.notificationReceived(data);
				} else {
				   synchronized (lock) {
						responseQueue.put(reqno, data);
						lock.notifyAll();
					}
			   }
		   }catch(Exception e){
			   e.printStackTrace();
			   shouldStop = true;
			   hwLayer.closeDevice();
			   if( connectionListener != null)
				   connectionListener.connectionLost();
		   }
	   }
   }

   public void setNotificationListener(NotificationListener l) {
	   notificationListener = l;
   }

   public void setConnectionListener(ConnectionListener cl) {
	   this.connectionListener = cl;
}
   
}

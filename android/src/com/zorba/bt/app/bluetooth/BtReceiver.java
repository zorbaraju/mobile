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
				System.out.println("Wating for read lock...."+reqno);
				lock.wait(100);
				System.out.println("Wafter wait "+reqno+".."+responseQueue);
				readbytes = responseQueue.remove(reqno);
				System.out.println("Wreque.... return ..."+readbytes);
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
			   System.out.println("Stopping reciver thread...");
			   return;
		   }
		   try{
			   byte[] recvBytes = new byte[1024];
			   System.out.println("Reading...");
			   int numRead = inStream.read(recvBytes);
			   if( numRead == -1)
				   continue;
			   System.out.println("Read..."+numRead);
			   if( numRead == 0) {
				   System.out.println("Numread is no");
				   continue;
			   }
			   byte[] readBytes = new byte[numRead];
			   for(int i=0; i<numRead; i++)
				   readBytes[i] = recvBytes[i];
			   
			   int cmd = readBytes[0];
			   int reqno = readBytes[1];
			   hwLayer.printBytes("Read..", readBytes);
			   System.out.println("cmd..."+cmd+"req..."+reqno);
			   byte []data = new byte[numRead-2];
				for(int i=0; i<data.length; i++)
					data[i] = readBytes[i+2];
			   if (cmd == 36) {
					System.out.println("notification");
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
				   System.out.println("sync");
				   synchronized (lock) {
						responseQueue.put(reqno, data);
						lock.notifyAll();
					}
				   System.out.println("After nofiy wait "+reqno+".."+responseQueue);
					
			   }
		   }catch(Exception e){
			   e.printStackTrace();
			   System.out.println("Exception in read thread..."+e.getMessage());
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
	   System.out.println("Connection listenrr is set on revivee...");
	   this.connectionListener = cl;
}
   
}

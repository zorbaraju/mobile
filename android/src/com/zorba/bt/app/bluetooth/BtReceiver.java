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
		while (true) {
			if (shouldStop) {
				return;
			}
			try {
				byte[] recvBytes = new byte[1024];
				int numRead = inStream.read(recvBytes);
				if (numRead == -1)
					continue;
				if (numRead == 0) {
					continue;
				}
				byte[] readBytes = new byte[numRead];
				for (int i = 0; i < numRead; i++)
					readBytes[i] = recvBytes[i];

				int cmd = readBytes[0];
				int reqno = readBytes[1];
				CommonUtils.printBytes("Read", readBytes);
				byte[] data = new byte[numRead - 2];
				for (int i = 0; i < data.length; i++)
					data[i] = readBytes[i + 2];
				if (cmd == 36) {
					byte numdevs = readBytes[2];
					byte alldevs = (byte) 0xFF;
					if (numdevs == alldevs) {
						int maxdev = CommonUtils.getMaxNoDevices();
						data = new byte[ maxdev * 2];
						for (int i = 0; i < maxdev; i++) {
							byte status = readBytes[i+3];
							data[i * 2] = (byte)(i+1);
							data[i * 2 + 1] = status;
						}
						notificationListener.notificationReceived(data);
					} else {
						CommonUtils.processMultipleNotification(readBytes, 0, notificationListener, null);
					}
					synchronized (lock) {
						responseQueue.put(reqno, data);
						lock.notifyAll();
					}
				} else {
					synchronized (lock) {
						responseQueue.put(reqno, data);
						lock.notifyAll();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				shouldStop = true;
				hwLayer.closeDevice();
				if (connectionListener != null)
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

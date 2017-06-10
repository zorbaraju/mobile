package com.zorba.bt.app.bluetooth;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.zorba.bt.app.CommonUtils;
import com.zorba.bt.app.bluetooth.BtHwLayer;
import com.zorba.bt.app.bluetooth.NotificationListener;

import android.util.Log;

public class BtIotReceiver implements AWSIotMqttNewMessageCallback {
	String error = null;
	NotificationListener notificationListener = null;
	IOTMessageListener iotListener = null;
	ConnectionListener connectionListener = null;
	String roomName = "";
	Object lock = new Object();
	HashMap<String, byte[]> responseQueue = new HashMap<String, byte[]>();
	boolean shouldStop = false;

	public BtIotReceiver(String rn) {
		roomName = rn;
	}

	public void close() throws Exception {
		this.shouldStop = true;
		this.responseQueue.clear();
		this.responseQueue = null;
	}

	public byte[] getData(String cmdNoAndReqNo) {
		byte readbytes[] = null;
		synchronized (lock) {
			try {
				lock.wait(BtHwLayer.IOT_READ_TIMEOUT);
				readbytes = responseQueue.remove(cmdNoAndReqNo);
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

	

	public void setNotificationListener(NotificationListener l, IOTMessageListener iotl) {
		notificationListener = l;
		iotListener = iotl;
	}

	public void setConnectionListener(ConnectionListener cl) {
		this.connectionListener = cl;
	}

	@Override
	public void onMessageArrived(String topic, byte[] readBytes) {
		String message = new String(readBytes);
		System.out.println("Message arrived:");
		System.out.println("   Topic: " + topic);
		String roomname = topic.split("/")[0];
		System.out.println(" Message: " + message);
		int cmdno = readBytes[0];
		int reqno = readBytes[1];
		String cmdNoAndReqNo = cmdno+""+reqno;
		int numRead = readBytes.length;
		CommonUtils.printBytes("Read", readBytes);
		byte[] data = new byte[numRead - 2];
		for (int i = 0; i < data.length; i++)
			data[i] = readBytes[i + 2];
		if (cmdno == 36) {
			byte numdevs = readBytes[2];
			byte alldevs = (byte) 0xFF;
			if (numdevs == alldevs) {
				int maxdev = CommonUtils.getMaxNoDevices();
				data = new byte[ maxdev * 2];
				byte[] devids = new byte[maxdev];
				byte[] statuses = new byte[maxdev];
				
				for (int i = 0; i < maxdev; i++) {
					byte status = readBytes[i+3];
					data[i * 2] = (byte)(i+1);
					data[i * 2 + 1] = status;
					devids[i] = data[i * 2];
					statuses[i] = data[i * 2 + 1];
					System.out.println("devid...."+data[i * 2]+" status="+data[i * 2 + 1]);
				}
				if( notificationListener == null){
					System.out.println("Notification list is null");
				} else {
					notificationListener.notificationReceived(data);
					/*String switchName = BtLocalDB.getInstance(this).getSwitchName(roomDeviceName, switchId);
    				if( switchName == null){
    					switchName = "No match";
    				}
    				*/
					iotListener.mesgReceveid(roomName, devids, statuses);
				}
			} else {
				int numchanges = readBytes[2];
				for( int i=0; i<numchanges; i++) {
            		byte[] devid = {readBytes[3+2*i]};
            		byte[] status = {readBytes[3+2*i+1]};
            		iotListener.mesgReceveid(roomName, devid, status);
				}
				CommonUtils.processMultipleNotification(readBytes, 0, notificationListener, null);
			}
			synchronized (lock) {
				responseQueue.put(cmdNoAndReqNo, data);
				lock.notifyAll();
			}
		} else {
			synchronized (lock) {
				responseQueue.put(cmdNoAndReqNo, data);
				lock.notifyAll();
			}
		}
	}

}

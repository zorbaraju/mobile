package com.zorba.bt.app.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import com.zorba.bt.app.CommonUtils;
import com.zorba.bt.app.Logger;
import com.zorba.bt.app.MainActivity;
import com.zorba.bt.app.dao.DeviceData;

import java.net.Socket;
import java.util.HashMap;
import java.util.UUID;

public class BtHwLayer {

	// setting ip address
	// request>>> {: reqid ssid32 \0 pass64 \0 ipaddr \0}
	// response>>> {: reqid }

	public static final String NOCONNECTIONERROR 	= null;
	public static final String NOCONNECTION			= "No connection to the device";
	public static final String DEVICE_NOTFOUND		= "Device is not found";
	public static final String NODATA				= "No data from device";
	
	byte[] STARTBYTES = new byte[] { (byte) 35, (byte) 63, (byte) 36, (byte) 64, (byte) 45, (byte) 38, (byte) 126,
			(byte) 40, (byte) 41 };

	int mConnectionState = BluetoothProfile.STATE_DISCONNECTED;

	final int STATE_DISCONNECTED = 0;
	final int STATE_CONNECTING = 1;
	final int STATE_CONNECTED = 2;

	final String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
	final String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
	final String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
	final String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
	final String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA";

	private static BtHwLayer instance = null;
	static byte reqgenerator = 0;
	UUID service_uuid = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
	UUID charr_uuid = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
	MainActivity activity = null;
	BluetoothManager mBluetoothManager;
	BluetoothAdapter mBluetoothAdapter;
	String mBluetoothDeviceAddress;
	BluetoothGattCallback mGattCallback = null;
	BluetoothGatt mBluetoothGatt;
	BluetoothGattCharacteristic charr;

	Socket clientSocket;
	BtReceiver receiver;
	BtSender sender;

	String devAddress = "";
	String ipAddress = "";
	String error = null;
	ConnectionListener connectionListener = null;
	NotificationListener notificationListener = null;
	boolean isBtTurnedOffManually = false;
	Object lock = new Object();
	HashMap<Integer, byte[]> responseQueue = new HashMap<Integer, byte[]>();
	boolean isConnected = false;
	BroadcastReceiver mReceiver = null;

	private BtHwLayer(Activity var1) {
		this.activity = (MainActivity) var1;
		mBluetoothManager = (BluetoothManager) this.activity.getSystemService(Context.BLUETOOTH_SERVICE);
		if (mBluetoothManager == null) {
			System.out.println("Unable to initialize BluetoothManager.");
			return;
		}

		mBluetoothAdapter = mBluetoothManager.getAdapter();
		if (mBluetoothAdapter == null) {
			System.out.println("Unable to obtain a BluetoothAdapter.");
			return;
		}

		mReceiver = new BroadcastReceiver() {
			public void onReceive(Context var1, Intent var2) {
				if ("android.bluetooth.adapter.action.STATE_CHANGED".equals(var2.getAction())) {
					if (var2.getIntExtra("android.bluetooth.adapter.extra.STATE", -1) == 10) {
						BtHwLayer.this.error = "Please turn on the Bluetooth, Its has been turned off manually";
						BtHwLayer.this.isBtTurnedOffManually = true;
					} else if (var2.getIntExtra("android.bluetooth.adapter.extra.STATE", -1) == 12) {
						BtHwLayer.this.isBtTurnedOffManually = false;
						BtHwLayer.this.error = null;
					}

					System.out.println("Bluetooth state is changed to " + BtHwLayer.this.isBtTurnedOffManually + "..."
							+ var2.getIntExtra("android.bluetooth.adapter.extra.STATE", -1));
				}

			}
		};
		System.out.println("Registering receiver.....");
		this.activity.registerReceiver(this.mReceiver,
				new IntentFilter("android.bluetooth.adapter.action.STATE_CHANGED"));
	}

	private void checkConnection() throws Exception {
		
		if (isWifi()) {
			System.out.println("checkConnection for wifi");
			if (getInstance(this.activity).makeWifiEnabled() && this.shouldReconnect(this.ipAddress)) {
				System.out.println("In checkConnection reinit wifi");
				String error = this.initDevice(this.devAddress, this.ipAddress);
				if (error != null) {
					System.out.println("In checkConnection reinit wifi error="+error);
					throw new Exception(error);
				}
			}
		} else {
			System.out.println("checkConnection for bt");
			if (getInstance(this.activity).makeBtEnabled() && this.shouldReconnect(this.devAddress)) {
				System.out.println("In checkConnection reinit bt");
				String var1 = this.initDevice(this.devAddress, this.ipAddress);
				if (var1 != null) {
					System.out.println("In checkConnection reinit bt error="+error);
					throw new Exception(var1);
				}
			}
		}
	}

	public static BtHwLayer getInstance(Activity var0) {
		if (instance == null) {
			instance = new BtHwLayer(var0);
		}
		return instance;
	}

	public String initDevice(String macaddress, String ipaddr) {
		
		System.out.println("In InitDevice Incoming macaddress= "+macaddress + "  ipaddress...." + ipaddr);
		isConnected = false;

		if (ipaddr != null && ipaddr.equals("null")) {
			ipaddr = null;
		}
		
		
		if (!isWifiEnabled()) {
			ipaddr = null;
			System.out.println("Wifi is not on , goiing to use BT");
		}
		System.out.println("The ipaddress going to be used for init device is " + ipaddr);
		ipAddress = ipaddr;
		if (isWifi()) {
			try {
				clientSocket = new Socket(ipAddress, 1336);
				receiver = new BtReceiver(this, clientSocket.getInputStream());
				receiver.setNotificationListener(activity);
				receiver.setConnectionListener(connectionListener);
				sender = new BtSender(this, clientSocket.getOutputStream());
				connectionListener.connectionStarted();
				System.out.println("Device is connected in wifi and waiting for 1 sec");
				Thread.sleep(1 * 1000);
				System.out.println("wait is released for 1 sec");
				isConnected = true;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("Device is not connected in wifi and error is "+e.getMessage());
				isConnected = false;
				return NOCONNECTION;
			}
		} else {
			this.devAddress = macaddress;
			BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(macaddress);
			if (device == null) {
				System.out.println("BT Device not found.  Unable to connect. for "+macaddress);
				return DEVICE_NOTFOUND;
			}

			mGattCallback = new BluetoothGattCallback() {
				@Override
				public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
					if (newState == BluetoothProfile.STATE_CONNECTED) {
						mConnectionState = STATE_CONNECTED;
						System.out.println("Connected to GATT server.");
						gatt.discoverServices();
					} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
						mConnectionState = STATE_DISCONNECTED;
						System.out.println("Disconnected from GATT server.");
						closeDevice();
						connectionListener.connectionLost();
					}
				}

				@Override
				public void onServicesDiscovered(BluetoothGatt gatt, int status) {
					if (status == BluetoothGatt.GATT_SUCCESS) {
						System.out.println("Gatt service discovered");
						BluetoothGattService service = gatt.getService(service_uuid);
						if (service == null) {
							System.out.println("Service is not yet found");
							return;
						} else {
							System.out.println("Service is found");
							charr = service.getCharacteristic(charr_uuid);
							gatt.setCharacteristicNotification(charr, true);
							System.out.println("Notifying for service found ..>"+charr+">");
							synchronized (lock) {
								lock.notifyAll();
								isConnected = true;
								connectionListener.connectionStarted();
							}
							System.out.println("Notified for service found");
						}
					}
				}
				
				@Override
				public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,
						int status) {
					byte[] values = characteristic.getValue();
					printBytes("OnWrite", values);
				}

				@Override
				public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
					byte[] values = characteristic.getValue();
					printBytes("OnRead", values);
					if (values[0] == 36) {
						byte[] data = new byte[values.length - 2];
						for (int i = 0; i < data.length; i++)
							data[i] = values[i + 2];
						activity.notificationReceived(data);
					} else {
						synchronized (lock) {
							byte[] data = new byte[values.length - 2];
							for (int i = 0; i < data.length; i++)
								data[i] = values[i + 2];
							int reqno = values[1];
							responseQueue.put(reqno, data);
							lock.notify();
						}
					}
				}
			};
			mBluetoothGatt = device.connectGatt(this.activity, false, mGattCallback);
			boolean isconnected = mBluetoothGatt.connect();
			if (!isconnected) {
				System.out.println("Gatt is not connected to device");
				return NOCONNECTION;
			}
			mBluetoothGatt.discoverServices();
			System.out.println("Waiting for service found for 5 secs");
			synchronized (lock) {
				try {
					lock.wait(5000);
					System.out.println("Waiting for service released>>>isconnectied...<"+isConnected+">");
				} catch (InterruptedException e) {
					System.out.println("Waiting is interupted and not connected..."+e.getMessage());
					isConnected = false;
				}
			}
			if( !isConnected) {
				return NOCONNECTION;
			}
		}
		return NOCONNECTIONERROR;
	}

	public boolean isWifiEnabled() {
		WifiManager wifiManager = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
		return wifiManager.isWifiEnabled();
	}
	
	private byte getNextReqno() {
		reqgenerator++;
		return reqgenerator;
	}

	public void closeDevice() {
		if (isWifi()) {
			try {
				if (this.sender != null) {
					this.sender.close();
				}
				if (this.receiver != null) {
					this.receiver.close();
				}
				this.sender = null;
				this.receiver = null;
				if (clientSocket != null) {
					clientSocket.close();
				}
				this.clientSocket = null;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		try {
			if (this.mBluetoothGatt != null)
				this.mBluetoothGatt.disconnect();
			this.mBluetoothGatt = null;
			mGattCallback = null;
			

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		isConnected = false;
		System.out.println("Connection to device is closed");
	}
	
	public void register() {
		try{
			this.activity.registerReceiver(this.mReceiver,
					new IntentFilter("android.bluetooth.adapter.action.STATE_CHANGED"));
		}catch(Exception e){
			System.out.println("Error in registering the receiver:"+e.getMessage());
		}
	}
	
	public void unregister() {
		try{
			this.activity.unregisterReceiver(this.mReceiver);
		}catch(Exception e){
			System.out.println("Error in unregistering the receiver:"+e.getMessage());
		}
	}

	public boolean isConnected() {
		return isConnected;
	}

	public long convertBytesToLong(byte[] var1) {
		long var3 = (long) var1[0];

		for (int var2 = 1; var2 < var1.length; ++var2) {
			var3 = var3 << 8 | (long) (var1[var2] & 255);
		}

		Logger.e(this.activity, "BtHwLayer", "bytes(" + var1.length + "):<" + var3 + ">");
		return var3;
	}

	public boolean makeBtEnabled() {
		boolean isEnabled = this.mBluetoothAdapter.isEnabled();
		if (!isEnabled) {
			Intent var2 = new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE");
			this.activity.startActivityForResult(var2, MainActivity.ENABLEBT_CODE);
		}
		return isEnabled;
	}

	public boolean makeWifiEnabled() {
		boolean isEnabled = isWifiEnabled();
		if (!isEnabled) {
			Intent var2 = new Intent(Settings.ACTION_WIFI_SETTINGS);
			this.activity.startActivityForResult(var2, MainActivity.ENABLEWIFI_CODE);
		}
		return isEnabled;
	}

	public void print(String msg) {
		Logger.e(this.activity, "BtHwLayer", msg);
	}

	public void printBytes(String tag, byte[] bytes) {
		String resp = "";
		for (int i = 0; i < bytes.length; i++) {
			resp += " " + Integer.toHexString(bytes[i]);
		}
		System.out.println("Data:(" + tag + " " + bytes.length + "): " + resp);
	}

	private byte[] getData(int reqno) {
		if (isWifi()) {
			return receiver.getData(reqno);
		} else {
			byte readbytes[] = null;
			synchronized (lock) {
				try {
					lock.wait(5000);
					readbytes = responseQueue.remove(reqno);
				} catch (InterruptedException e) {
					System.out.println("Not able to get the readbytes from queue>"+e.getMessage());
				}
			}
			return readbytes;
		}

	}

	private void writeBytes(byte[] wbytes) {
		if (isWifi()) {
			System.out.println("Waiting for 250 ms before writing the data to wifi device");
			try {
				Thread.sleep(250);
			} catch (Exception e) {
			}
			sender.sendCmd(wbytes);
		} else {
			int numBytes = wbytes.length;
			int numSent = 0;
			int remainingBytes = numBytes;
			printBytes("Write Be TO SENT", wbytes);
			while (remainingBytes > 0) {
				int numToBeSent = 20;
				if (remainingBytes < 20)
					numToBeSent = remainingBytes;
				byte sendingBytes[] = new byte[numToBeSent];
				for (int i = 0; i < numToBeSent; i++)
					sendingBytes[i] = wbytes[numSent + i];
				charr.setValue(sendingBytes);
				mBluetoothGatt.writeCharacteristic(charr);
				numSent += numToBeSent;
				remainingBytes -= numToBeSent;
				System.out.println("Numtobesent.." + numToBeSent + " numSent=" + numSent + " remain=" + remainingBytes
						+ " numbtes.." + numBytes);
			}
		}
	}

	private byte[] getDataAndValidate(byte reqno) throws Exception{
		byte[] data = this.getData(reqno);
		if (data == null) {
			throw new Exception("No data from device");
		} else {
			return data;
		}
	}
	public int getNumberOfDevices() throws Exception {
		this.checkConnection();
		byte reqno = this.getNextReqno();
		this.writeBytes(new byte[] { (byte) 41, reqno });
		byte[] data = this.getDataAndValidate(reqno);
		// first byte will be the number of devices
		return data[0];
	}

	public byte[] setIpAddress(String ssid, String passwd, String ipaddress) throws Exception {
		this.checkConnection();
		byte reqno = this.getNextReqno();
		System.out.println("ssid="+ssid+" passwd="+passwd + " ipaddress" + ipaddress);
		byte ipbytes[] = ipaddress.getBytes();
		String ssidinfo = ssid + '\0' + passwd + '\0';
		byte[] ssidinfobytes = ssidinfo.getBytes();
		byte ipsetbytes[] = new byte[1 + 1 + ssidinfobytes.length + ipbytes.length + 1];
		ipsetbytes[0] = ':';
		ipsetbytes[1] = reqno;
		for (int index = 0; index < ssidinfobytes.length; index++)
			ipsetbytes[2 + index] = ssidinfobytes[index];
		for (int index = 0; index < ipbytes.length; index++)
			ipsetbytes[2 + ssidinfobytes.length + index] = ipbytes[index];
		ipsetbytes[2 + ssidinfobytes.length + ipbytes.length] = '\0';
		this.writeBytes(ipsetbytes);
		return this.getDataAndValidate(reqno);
	}

	public byte[] readAllStatus() throws Exception {
		this.checkConnection();
		byte reqno = this.getNextReqno();
		this.writeBytes(new byte[] { (byte) 63, reqno, (byte) -1 });
		return this.getDataAndValidate(reqno);
	}

	public byte[] readRGBToDevice() throws Exception {
		this.checkConnection();
		byte reqno = this.getNextReqno();
		this.writeBytes(new byte[] { (byte) '^', reqno });
		return this.getDataAndValidate(reqno);
	}

	public int readCommandToDevice(int var1) throws Exception {
		this.checkConnection();
		byte reqno = this.getNextReqno();
		this.writeBytes(new byte[] { (byte) 63, reqno, (byte) 1, (byte) var1 });
		byte data[] =  this.getDataAndValidate(reqno);
		return data[2];
	}

	public long readPower() throws Exception {
		this.checkConnection();
		byte reqno = this.getNextReqno();
		this.writeBytes(new byte[] { (byte) 38, reqno });
		byte[] data = this.getDataAndValidate(reqno);
		return convertBytesToLong(data);
	}

	public void sendAlarmCommandToDevice(int mon, int date, int hr, int min, DeviceData[] deviceDataArr) throws Exception {
		this.checkConnection();
		byte reqno = this.getNextReqno();
		byte[] timeBytes = new byte[] { (byte) 64, reqno, (byte) mon, (byte) date, (byte) hr, (byte) min, (byte) 0,
				(byte) deviceDataArr.length };
		byte deviceInfo[] = new byte[deviceDataArr.length];

		for (int index = 0; index < deviceDataArr.length; ++index) {
			byte var8 = (byte) deviceDataArr[index].getDevId();
			deviceInfo[index] = (byte) ((byte) deviceDataArr[index].getStatus() & 15 | (byte) (var8 << 4));
		}
		byte[] both = new byte[timeBytes.length + deviceInfo.length];
		for (int i = 0; i < timeBytes.length; i++)
			both[i] = timeBytes[i];
		for (int i = 0; i < deviceInfo.length; i++)
			both[timeBytes.length + i] = deviceInfo[i];

		this.writeBytes(both);
		this.getDataAndValidate(reqno);
	}

	public void sendRGBToDevice(byte i, byte r, byte g, byte b) throws Exception {
		this.checkConnection();
		byte reqno = this.getNextReqno();
		this.writeBytes(new byte[] { (byte) '+', reqno, i, r, g, b });
		getDataAndValidate(reqno);
	}

	public void sendCommandToDevice(int devid, int status) throws Exception {
		this.checkConnection();
		byte reqno = this.getNextReqno();
		this.writeBytes(new byte[] { (byte) 35, reqno, (byte) 1, (byte)devid, (byte)status });
		this.getDataAndValidate(reqno);
	}

	public void sendCommandToDevices(int[] devidandstatuses) throws Exception {
		this.checkConnection();
		byte reqno = this.getNextReqno();
		byte[] writeBytes = new byte[devidandstatuses.length + 3];
		writeBytes[0] = 35;
		writeBytes[1] = reqno;
		writeBytes[2] = (byte) (devidandstatuses.length / 2);
		for (int i=0; i<devidandstatuses.length; i++) {
			writeBytes[3+i] = (byte)devidandstatuses[i];
		}
		this.writeBytes(writeBytes);
		this.getDataAndValidate(reqno);
	}

	public void sendDeleteAlarmCommandToDevice(int schedid) throws Exception {
		this.checkConnection();
		byte reqno = this.getNextReqno();
		this.writeBytes(new byte[] { (byte) 45, reqno, (byte)schedid });
		this.getDataAndValidate(reqno);
	}

	public void setDateAndTime() throws Exception {
		this.checkConnection();
		byte reqno = this.getNextReqno();
		byte[] timeBytes = CommonUtils.getCurrentTime();
		byte writeBytes[] = new byte[2+timeBytes.length];
		writeBytes[0] = (byte) 126;
		writeBytes[1] = reqno;
		for(int i=0; i<timeBytes.length; i++)
			writeBytes[i+2] = timeBytes[i];
		this.writeBytes(writeBytes);
		this.getDataAndValidate(reqno);
	}

	public void setDeviceType(int devid, boolean isDimmable) throws Exception {
		/*this.checkConnection();
		byte reqno = this.getNextReqno();
		byte type = (byte)(isDimmable?2:1);
		this.writeBytes(new byte[] { (byte) 40, reqno, (byte)devid, type });
		this.getDataAndValidate(reqno);*/
	}

	public boolean shouldReconnect(String ipAddressOrDevAddress) {
		boolean reconnect = false;
		if (isWifi()) {
			reconnect = !(this.ipAddress.equals(ipAddressOrDevAddress) && this.clientSocket != null);
		} else {
			reconnect = ! (this.devAddress.equals(ipAddressOrDevAddress) && this.mBluetoothGatt != null && isConnected());
		}
		if( reconnect ) {
			closeDevice();
		}
		System.out.println("should reconnectet returns ..." + reconnect);
		return reconnect;
	}

	private boolean isWifi() {
		boolean isWifi = ipAddress != null && !ipAddress.isEmpty() && !ipAddress.equals("null");
		System.out.println("ipaddress......." + ipAddress+"  iswifi>>>>"+isWifi);
		return isWifi;
	}

	public void setConnectionListener(ConnectionListener connectionListener) {
		this.connectionListener = connectionListener;
	}
}

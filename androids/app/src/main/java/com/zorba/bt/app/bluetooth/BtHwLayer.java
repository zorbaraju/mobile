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
import android.os.Handler;
import android.provider.Settings;
import com.zorba.bt.app.CommonUtils;
import com.zorba.bt.app.Logger;
import com.zorba.bt.app.NetworkStateReceiver;
import com.zorba.bt.app.RoomsActivity;
import com.zorba.bt.app.dao.DeviceData;
import com.zorba.bt.app.dao.RoomData;
import com.zorba.bt.app.db.BtLocalDB;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class BtHwLayer {
	
	public static final int CONNETIONTYPE_BT = 0;
	public static final int CONNETIONTYPE_WIFI = 1;
	public static final int CONNETIONTYPE_DATA = 2;
	
	int connectionType = -1;
	
	boolean _isOOH = false;
	public static final int READ_TIMEOUT = 3000;
	public static final int IOT_READ_TIMEOUT = 10000;
	
	private int idletimeout = 1000 * 60 * 3;
	private long activedTime = -1;
	
	boolean isDiscovery = false;
	String populateMacAddress = null;
	private final static int NAMELEGTH = 12;
	// setting ip address
	// request>>> {: reqid ssid32 \0 pass64 \0 ipaddr \0}
	// response>>> {: reqid }

	long lastsenttime = System.currentTimeMillis();
	
	public static final String NOCONNECTIONERROR 	= null;
	public static final String NOCONNECTION			= "No connection to the device";
	public static final String DEVICE_NOTFOUND		= "Device is not found";
	public static final String NODATA				= "No data from device";
	public static final String BTNOTENABLED			= "Bluetooth is not Enabled";
	
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
	RoomsActivity activity = null;
	BluetoothManager mBluetoothManager;
	BluetoothAdapter mBluetoothAdapter;
	String mBluetoothDeviceAddress;
	BluetoothGattCallback mGattCallback = null;
	BluetoothGatt mBluetoothGatt;
	BluetoothGattCharacteristic charr;

	Socket clientSocket;
	BtReceiver receiver;
	BtSender sender;

	AwsConnection iotConnection = null;
	
	String roomname = "";
	String devAddress = "";
	String ipAddress = "";
	String pwd = null;
	String ssid = "";
	String error = null;
	ConnectionListener connectionListener = null;
	NotificationListener notificationListener = null;
	boolean isBtTurnedOffManually = false;
	Object lock = new Object();
	HashMap<String, byte[]> responseQueue = new HashMap<String, byte[]>();
	boolean isConnected = false;
	boolean isunregistered = true;
	BroadcastReceiver mReceiver = null;
	NetworkStateReceiver nReceiver = null;

	private BtHwLayer(Activity var1) {
		this.activity = (RoomsActivity) var1;
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

		nReceiver = new NetworkStateReceiver();
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
		register();
	}

	private void checkConnection() throws Exception {
		if(mBluetoothGatt != null) {
			System.out.println("checkConnection for bt");
			if (getInstance(this.activity).makeBtEnabled() && this.shouldReconnect(this.devAddress)) {
				System.out.println("In checkConnection reinit bt macaddress="+this.devAddress+" ipaddress="+ this.ipAddress);
				String var1 = this.initDevice(this.roomname, this.devAddress, this.ssid, this.ipAddress, false);
				if (var1 != null) {
					System.out.println("In checkConnection reinit bt error="+var1);
					throw new Exception(var1);
				}
			}
		} else if (clientSocket != null) {
			System.out.println("checkConnection for wifi");
			if ( this.shouldReconnect(this.ipAddress)) {
				System.out.println("In checkConnection reinit wifi");
				String error = this.initDevice(this.roomname, this.devAddress, this.ssid, this.ipAddress, false);
				if (error != null) {
					System.out.println("In checkConnection reinit wifi error="+error);
					throw new Exception(error);
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

	public boolean isDataEnabled(boolean enable){
		if( CommonUtils.isMobileDataConnection(activity) ){
			_isOOH = enable;
		}
		return _isOOH;
	}
	
	public boolean isOOH() {
		return _isOOH;
	}
	
	public String initDevice(String roomname, String macaddress, String ssid, String ipaddr) {
		return initDevice(roomname, macaddress, ssid, ipaddr, true);
	}
	
	public String initDevice(String roomname, String macaddress, String ssid, String ipaddr, boolean isDiscovery) {
		this.isDiscovery = isDiscovery;
		this.devAddress = macaddress;
		System.out.println("In InitDevice Incoming macaddress= "+macaddress + " ssid = "+ssid+" ipaddress...." + ipaddr+" isdiscovery="+isDiscovery);
		isConnected = false;
		if (ipaddr != null && ipaddr.equals("null")) {
			ipaddr = null;
		}
		
		if (!isWifiEnabled()) {
			ipaddr = null;
			System.out.println("Wifi is not on , goiing to use BT");
		}
		System.out.println("The ipaddress going to be used for init device is " + ipaddr);
		this.roomname = roomname;
		ipAddress = ipaddr;
		this.ssid = ssid;
		
		if( !isDiscovery && _isOOH && CommonUtils.isMobileDataConnection(activity)) {
			System.out.println("Iot connection is to be used");
		} else if (isWifi()) {
			CommonUtils.getInstance().writeLog("Wifi mode...ipaddress is "+ipAddress);
			try {
				clientSocket = new Socket(ipAddress, 1336);
				receiver = new BtReceiver(this, clientSocket.getInputStream());
				receiver.setNotificationListener(activity);
				receiver.setConnectionListener(connectionListener);
				sender = new BtSender(this, clientSocket.getOutputStream());
				if( connectionListener != null)
					connectionListener.connectionStarted(CommonUtils.CONNECTION_WIFI);
				System.out.println("Device is connected in wifi and waiting for 1 sec");
				Thread.sleep(1 * 1000);
				System.out.println("wait is released for 1 sec");
				isConnected = true;
				connectionType = CONNETIONTYPE_WIFI;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("Device is not connected in wifi and error is "+e.getMessage());
				isConnected = false;
				return NOCONNECTION;
			}
		} else {
			CommonUtils.getInstance().writeLog("Bt  mode...macaddress is "+macaddress);
			if(!isBt()) {
				isConnected = false;
				return BTNOTENABLED;
			}
			BluetoothDevice device = null;
			try {
				device = mBluetoothAdapter.getRemoteDevice(macaddress);
			}catch(Exception e){
				device = null;
				return "Error in getting device using mac address>"+e.getMessage();
			}
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
							System.out.println("Notifying for service found ..>"+charr+">"+connectionListener);
							synchronized (lock) {
								lock.notifyAll();
								isConnected = true;
								connectionType = CONNETIONTYPE_BT;
								if(connectionListener != null)
									connectionListener.connectionStarted(CommonUtils.CONNECTION_BT);
							}
							System.out.println("Notified for service found");
						}
					}
				}
				
				@Override
				public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,
						int status) {
					byte[] values = characteristic.getValue();
					CommonUtils.printBytes("OnWrite", values);
				}

				@Override
				public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
					byte[] values = characteristic.getValue();
					CommonUtils.printBytes("OnRead", values);
					if (values[0] == 36) {
						byte numdevs = values[2];
						byte alldevs = (byte)0xFF;
						if( numdevs == alldevs) {
							numdevs = (byte)(values.length-3);
							byte[] data = new byte[numdevs*2];
							for (int i = 0; i < numdevs; i++) {
								byte status = values[i+3];
								data[i*2] = (byte)(i+1);
								data[i*2+1] = status;
							}
							activity.notificationReceived(data);
						} else {
							CommonUtils.processMultipleNotification(values, 0, null, activity);
						}
					} else {
						synchronized (lock) {
							byte[] data = new byte[values.length - 2];
							for (int i = 0; i < data.length; i++)
								data[i] = values[i + 2];
							int cmd = values[0];
							int reqno = values[1];
							responseQueue.put(cmd+""+reqno, data);
							lock.notify();
						}
					}
				}
			};
			mBluetoothGatt = device.connectGatt(this.activity, false, mGattCallback);
			boolean isconnected = false;
			if( mBluetoothGatt != null)
				isconnected = mBluetoothGatt.connect();
			if (!isconnected) {
				closeDevice();
				System.out.println("Gatt is not connected to device");
				return NOCONNECTION;
			}
			mBluetoothGatt.discoverServices();
			System.out.println("Waiting for service found for 5 secs");
			synchronized (lock) {
				try {
					lock.wait(3000);
					System.out.println("Waiting for service released>>>isconnectied...<"+isConnected+">");
				} catch (InterruptedException e) {
					System.out.println("Waiting is interupted and not connected..."+e.getMessage());
					isConnected = false;
				}
			}
			if( !isConnected) {
				closeDevice();
				return NOCONNECTION;
			}
		}
		try {
			String devpwd = "";
			if( roomname != null)
				devpwd = BtLocalDB.getInstance(activity).getDevPwd(roomname);
			if( devpwd.isEmpty()) {
				devpwd = CommonUtils.DEVICEPASSWORD;
				String error = verifyAuth(devpwd);
				if( error == null) {
					// change pwd
				} else {
					devpwd = BtLocalDB.getInstance(activity).getDevicePwd();
					error = verifyAuth(devpwd);
					if( error == null && roomname != null) {
						BtLocalDB.getInstance(activity).setDevPwd(roomname, devpwd);
					} else {
						return error;
					}
				}
			} else {
				error = verifyAuth(devpwd);
				if( error != null) {
					return error;
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			//-spb 060217 for aligning error return "Authentication Error "+e.getMessage();
			return "Authentication Error 1 ";
		}
		if(macaddress != null) {
			CommonUtils.getInstance().writeLog("macaddress used is : "+macaddress);
		}
		return NOCONNECTIONERROR;
	}

	private String verifyAuth(String devpwd) throws Exception{
		String error = null;
		byte[] response = verifyPwd(devpwd);
		if( response[0] == '0') {
			String respstr = new String(response).substring(1);
			CommonUtils.getInstance().writeLog("bt mac, firmware: "+respstr);
			byte[] macbytes = new byte[6];
			for(int i=0; i<6; i++)
				macbytes[i] = response[i+1];
			populateMacAddress = getMacAddressString(macbytes);
			CommonUtils.getInstance().writeLog("bt mac, populated : "+populateMacAddress);
		} else {
			String respstr = new String(response).substring(1);
			error = "Autherization is failed, "+response[0] +", "+respstr;
		}
		return error;
	}
	
	public String getPopulateMacAddress() {
		return populateMacAddress;
	}
	public static String getMacAddressString(byte[] a) {
	   StringBuilder sb = new StringBuilder();
	   for(int i=0; i<6; i++) {
		   byte b = a[i];
	      sb.append(String.format("%02x", b & 0xff));
	      if( i<5) {
	    	  sb.append(":");
	      }
	   }
	   return sb.toString().toUpperCase();
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
			if (this.mBluetoothGatt != null) {
		//		this.mBluetoothGatt.disconnect();
			}
			if (this.mBluetoothGatt != null) {
				this.mBluetoothGatt.close();
			}
			this.mBluetoothGatt = null;
			mGattCallback = null;
			System.out.println("Bt socket is closed");

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if( iotConnection != null){
			iotConnection.closeConnection();
			iotConnection = null;
		}
		isConnected = false;
		connectionType = -1;
		System.out.println("Connection to device is closed");
		if( connectionListener != null) {
			connectionListener.connectionLost();
		}
	}
	
	public void register() {
		try{
			if( isunregistered ) {
				this.activity.registerReceiver(this.mReceiver,
						new IntentFilter("android.bluetooth.adapter.action.STATE_CHANGED"));

				this.activity.registerReceiver(this.nReceiver,
						new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
				isunregistered = false;
			}
		}catch(Exception e){
			System.out.println("Error in registering the receiver:"+e.getMessage());
		}
	}
	
	public void unregister() {
		try{
			if( !isunregistered ) {
				this.activity.unregisterReceiver(this.mReceiver);
				this.activity.unregisterReceiver(this.nReceiver);
				isunregistered = true;
			}
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

	public boolean isBt() {
		return this.mBluetoothAdapter.isEnabled();
	}
	public boolean makeBtEnabled() {
		boolean isEnabled = isBt();
		if (!isEnabled) {
			Intent var2 = new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE");
			this.activity.startActivityForResult(var2, RoomsActivity.ENABLEBT_CODE);
		}
		return isEnabled;
	}

	public boolean makeWifiEnabled() {
		boolean isEnabled = isWifiEnabled();
		if (!isEnabled) {
			Intent var2 = new Intent(Settings.ACTION_WIFI_SETTINGS);
			this.activity.startActivityForResult(var2, RoomsActivity.ENABLEWIFI_CODE);
		}
		return isEnabled;
	}

	public void print(String msg) {
		Logger.e(this.activity, "BtHwLayer", msg);
	}

	private byte[] getData(String cmdNoAndReqNo) {
		if( connectionType == CONNETIONTYPE_BT) {
			byte readbytes[] = null;
			synchronized (lock) {
				try {
					lock.wait(READ_TIMEOUT);
					readbytes = responseQueue.remove(cmdNoAndReqNo);
				} catch (InterruptedException e) {
					System.out.println("Not able to get the readbytes from queue>"+e.getMessage());
				}
			}
			return readbytes;
		} else if (connectionType == CONNETIONTYPE_WIFI) {
			return receiver.getData(cmdNoAndReqNo);
		} else if( connectionType == CONNETIONTYPE_DATA) {
			return iotConnection.getData(cmdNoAndReqNo);
		} else {
			return null;
		}
	}

	public void writeBytes(byte[] wbytes) {
		if( connectionType == CONNETIONTYPE_BT) {
			int numBytes = wbytes.length;
			int numSent = 0;
			int remainingBytes = numBytes;
			CommonUtils.printBytes("Write to be sent", wbytes);
			while (remainingBytes > 0) {
				int numToBeSent = 20;
				if (remainingBytes < 20)
					numToBeSent = remainingBytes;
				byte sendingBytes[] = new byte[numToBeSent];
				for (int i = 0; i < numToBeSent; i++)
					sendingBytes[i] = wbytes[numSent + i];
				charr.setValue(sendingBytes);
				mBluetoothGatt.writeCharacteristic(charr);
				CommonUtils.printBytes("WriteP", sendingBytes);
				numSent += numToBeSent;
				remainingBytes -= numToBeSent;
				System.out.println("Numtobesent.." + numToBeSent + " numSent=" + numSent + " remain=" + remainingBytes
						+ " numbtes.." + numBytes);
			}
		} else if (connectionType == CONNETIONTYPE_WIFI) {
			sender.sendCmd(wbytes);
		} else if( connectionType == CONNETIONTYPE_DATA) {
			iotConnection.sendMessage(wbytes);
		}
		lastsenttime = System.currentTimeMillis();
	}

	private byte[] processReqWithRetries(byte reqno, byte[] writeBytes)  throws Exception {
		byte[] data = null;
		int numRetries = 0;
		while(numRetries<3){
			byte cmdno = writeBytes[0];
			String cmdNoAndReqNo = cmdno+""+reqno;
			numRetries++;
			this.writeBytes(writeBytes);
			data = this.getData(cmdNoAndReqNo);
			if( data != null) {
				break;
			}
		}
		
		if (data == null) {
			CommonUtils.getInstance().writeLog(NODATA);
			throw new Exception(NODATA);
		} else {
			return data;
		}
	}
	
	public void sendRawBytes(byte writeBytes[]) throws Exception {
		this.checkConnection();
		processReqWithRetries(writeBytes[1], writeBytes);
	}
	
	public void setWifiAPMode(boolean isAP) throws Exception {
		this.checkConnection();
		byte reqno = this.getNextReqno();
		byte mode = 'S';
		if( isAP ) {
			mode = 'A';
		}
		byte[] writeBytes = new byte[] { 'E', reqno, mode };
		byte[] data = processReqWithRetries(reqno, writeBytes);
	}
	
	public int getNumberOfDevices() throws Exception {
		this.checkConnection();
		byte reqno = this.getNextReqno();
		byte[] writeBytes = new byte[] { (byte) 41, reqno };
		byte[] data = processReqWithRetries(reqno, writeBytes);
		// first byte will be the number of devices
		return data[0];
	}

	public byte[] changePwd(String pwd) throws Exception {
		/*this.checkConnection();
		byte reqno = this.getNextReqno();
		System.out.println("pwd="+pwd);
		pwd = pwd + "\0\0";
		byte[] pwdbytes = pwd.getBytes();
		byte pwdsetbytes[] = new byte[1 + 1 + pwdbytes.length ];
		pwdsetbytes[0] = 'P';
		pwdsetbytes[1] = reqno;
		for (int index = 0; index < pwdbytes.length; index++)
			pwdsetbytes[2 + index] = pwdbytes[index];
		processReqWithRetries(reqno, pwdsetbytes);
		*/
		return "ok".getBytes();
	}
	
	private byte[] verifyPwd(String pwd) throws Exception {
		byte reqno = this.getNextReqno();
		System.out.println("pwd="+pwd);
		byte[] pwdbytes = pwd.getBytes();
		byte pwdsetbytes[] = new byte[1 + 1 + 1 + pwdbytes.length ];
		pwdsetbytes[0] = 'A';
		pwdsetbytes[1] = reqno;
		pwdsetbytes[2] = (byte)(pwdbytes.length);
		for (int index = 0; index < pwdbytes.length; index++)
			pwdsetbytes[3 + index] = pwdbytes[index];
		return processReqWithRetries(reqno, pwdsetbytes);
	}
	
	public byte[] setIpAddress(String ssid, String passwd, String ipaddress) throws Exception {
		this.checkConnection();
		byte reqno = this.getNextReqno();
		System.out.println("setting ip addresse ssid="+ssid+" passwd="+passwd + " ipaddress" + ipaddress);
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
		return processReqWithRetries(reqno, ipsetbytes);
	}

	public byte[] readAllStatus() throws Exception {
		this.checkConnection();
		byte reqno = this.getNextReqno();
		byte[] writeBytes = new byte[] { (byte) 63, reqno, (byte) -1 };
		return processReqWithRetries(reqno, writeBytes);
	}

	public byte[] readRGBToDevice() throws Exception {
		this.checkConnection();
		byte reqno = this.getNextReqno();
		byte[] writeBytes = new byte[] { (byte) '^', reqno };
		return processReqWithRetries(reqno, writeBytes);
	}

	public int readCommandToDevice(int var1) throws Exception {
		this.checkConnection();
		byte reqno = this.getNextReqno();
		byte[] writeBytes = new byte[] { (byte) 63, reqno, (byte) 1, (byte) var1 };
		byte data[] =   processReqWithRetries(reqno, writeBytes);
		return data[2];
	}

	public long readPower() throws Exception {
		this.checkConnection();
		byte reqno = this.getNextReqno();
		byte[] writeBytes = new byte[] { (byte) 38, reqno };
		byte data[] =   processReqWithRetries(reqno, writeBytes);
		return convertBytesToLong(data);
	}

	public void sendAlarmCommandToDevice(int alarmid, int repeattypeValue, int hr, int min, DeviceData[] deviceDataArr) throws Exception {
		this.checkConnection();
		byte reqno = this.getNextReqno();
		
		byte[] timeBytes = new byte[] { (byte) 64, reqno, (byte)alarmid, (byte)repeattypeValue, (byte) hr, (byte) min, (byte) 0,
				(byte) deviceDataArr.length };
		byte deviceInfo[] = new byte[deviceDataArr.length*2];

		for (int index = 0; index < deviceDataArr.length; ++index) {
			byte devid = (byte) deviceDataArr[index].getDevId();
			byte status = (byte) (deviceDataArr[index].getStatus() & 0xff);
			System.out.println("devid .."+devid+" status.."+status);
			deviceInfo[index*2] = devid;
			deviceInfo[index*2+1] = status;
		}
		byte[] both = new byte[timeBytes.length + deviceInfo.length];
		for (int i = 0; i < timeBytes.length; i++)
			both[i] = timeBytes[i];
		for (int i = 0; i < deviceInfo.length; i++) {
			both[timeBytes.length + i] = deviceInfo[i];
		}
		boolean isjunk = false;
		if( isjunk) {
			byte [] a = {(byte)40, reqno, (byte)alarmid, (byte)0xff, (byte)hr, (byte)min, 0, (byte)0xff, deviceInfo[1]};
			processReqWithRetries(reqno, a);
		} else {
			processReqWithRetries(reqno, both);
		}
	}
	
	public byte[] getAlarm(byte schedulerindex) throws Exception {
		this.checkConnection();
		byte reqno = this.getNextReqno();
		byte[] writeBytes = new byte[] { '!', reqno, schedulerindex };
		return processReqWithRetries(reqno, writeBytes);
	}
	
	public void sendDeleteAlarmCommandToDevice(int schedid) throws Exception {
		this.checkConnection();
		byte reqno = this.getNextReqno();
		byte[] writeBytes = new byte[] { (byte) 45, reqno, (byte)schedid };
		processReqWithRetries(reqno, writeBytes);
	}
	
	public void sendRGBToDevice(byte i, byte r, byte g, byte b) throws Exception {
		this.checkConnection();
		byte reqno = this.getNextReqno();
		byte[] writeBytes = new byte[] { (byte) '+', reqno, i, r, g, b };
		processReqWithRetries(reqno, writeBytes);
		
	}

	public void sendCommandToDevice(int devid, int status) throws Exception {
		this.checkConnection();
		byte reqno = this.getNextReqno();
		byte[] writeBytes = new byte[] { (byte) 35, reqno, (byte) 1, (byte)devid, (byte)status };
		processReqWithRetries(reqno, writeBytes);
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
		processReqWithRetries(reqno, writeBytes);
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
		processReqWithRetries(reqno, writeBytes);
		Thread.sleep(2000);
	}
	
	public byte[] getDateAndTime() throws Exception {
		this.checkConnection();
		byte reqno = this.getNextReqno();
		byte writeBytes[] = new byte[2];
		writeBytes[0] = 'Q';
		writeBytes[1] = reqno;
		
		byte[] data = processReqWithRetries(reqno, writeBytes);
		return data;
	}
	
	public void enableOOHCmd(boolean enable) throws Exception {
		this.checkConnection();
		byte reqno = this.getNextReqno();
		byte writeBytes[] = new byte[3];
		writeBytes[0] = 0x74;
		writeBytes[1] = reqno;
		writeBytes[2] = (byte) (enable?1:0);
		processReqWithRetries(reqno, writeBytes);
		Thread.sleep(10000);
	}
	
	public boolean readOOHStatus() throws Exception {
		this.checkConnection();
		byte reqno = this.getNextReqno();
		byte writeBytes[] = new byte[2];
		writeBytes[0] = 0x76;
		writeBytes[1] = reqno;
		byte[] data = processReqWithRetries(reqno, writeBytes);
		return data[0] == 1;
	}
	
	public void resetESBCmd() throws Exception {
		this.checkConnection();
		byte reqno = this.getNextReqno();
		byte writeBytes[] = new byte[2];
		writeBytes[0] = 0x79;
		writeBytes[1] = reqno;
		processReqWithRetries(reqno, writeBytes);
		Thread.sleep(10000);
	}
	
	public void setGatewayIPCmd(String gatewayip) throws Exception {
		this.checkConnection();
		byte reqno = this.getNextReqno();
		System.out.println("setting ip addresse " + gatewayip);
		String[] ipchars = gatewayip.trim().split("\\.");
		byte[] ipsetbytes = new byte[2 + ipchars.length];
		int index = 0;
		ipsetbytes[0] = 0x73;
		ipsetbytes[1] = reqno;
		for (String b : ipchars) {
			ipsetbytes[2 + index++] = (byte) Integer.parseInt(b);
		}
		processReqWithRetries(reqno, ipsetbytes);
		Thread.sleep(10000);
	}
	
	private boolean shouldReconnect(String ipAddressOrDevAddress) {
		boolean reconnect = false;
		if (clientSocket != null || iotConnection != null) {
			reconnect = !(this.ipAddress.equals(ipAddressOrDevAddress));
		} else if (mBluetoothGatt != null){
			reconnect = ! (this.devAddress.equals(ipAddressOrDevAddress));
			System.out.println(" reconnect..."+reconnect);
		}
		if( reconnect ) {
			closeDevice();
		}
		System.out.println("should reconnectet returns ..." + reconnect);
		return reconnect;
	}

	private boolean isWifi() {
		boolean isWifi = ipAddress != null && !ipAddress.isEmpty() && !ipAddress.equals("null");
		return isWifi;
	}

	public void setConnectionListener(ConnectionListener connectionListener) {
		this.connectionListener = connectionListener;
	}
	
	public void setRoomName(String roomName) throws Exception {
		this.checkConnection();
		byte reqno = this.getNextReqno();
		roomName = roomName.trim();
		while( roomName.length()<NAMELEGTH)
			roomName = roomName + " ";
		
		System.out.println("roomName=<"+roomName+"> length="+roomName.length());
		
		byte[] roomNameBytes = roomName.getBytes();
		byte roomNameSetBytes[] = new byte[1 + 1 + roomNameBytes.length ];
		roomNameSetBytes[0] = 0x53;
		roomNameSetBytes[1] = reqno;
		for (int index = 0; index < roomNameBytes.length; index++)
			roomNameSetBytes[2 + index] = roomNameBytes[index];
		processReqWithRetries(reqno, roomNameSetBytes);
	}
	
	public byte[] getRoomName() throws Exception {
		this.checkConnection();
		byte reqno = this.getNextReqno();
		byte[] writeBytes = new byte[] { (byte) 0x56, reqno };
		return processReqWithRetries(reqno, writeBytes);
	}
	
	public void setSwitchName(byte devid, String switchName) throws Exception {
		this.checkConnection();
		byte reqno = this.getNextReqno();
		switchName = switchName.trim();
		while( switchName.length()<NAMELEGTH)
			switchName = switchName + " ";
		
		System.out.println("switchName=<"+switchName+"> length="+switchName.length());
		
		byte[] switchNameBytes = switchName.getBytes();
		byte switchNameSetBytes[] = new byte[1 + 1 + 1 + switchNameBytes.length ];
		switchNameSetBytes[0] = 0x5D;
		switchNameSetBytes[1] = reqno;
		switchNameSetBytes[2] = devid;
		for (int index = 0; index < switchNameBytes.length; index++)
			switchNameSetBytes[3 + index] = switchNameBytes[index];
		processReqWithRetries(reqno, switchNameSetBytes);
	}
	
	public byte[] getSwitchName(byte devid) throws Exception {
		this.checkConnection();
		byte reqno = this.getNextReqno();
		byte[] writeBytes = new byte[] { (byte) 0x5E, reqno, devid };
		return processReqWithRetries(reqno, writeBytes);
	}
	
	public void setGroupName(byte grpindex, String grpName) throws Exception {
		this.checkConnection();
		byte reqno = this.getNextReqno();
		grpName = grpName.trim();
		while( grpName.length()<NAMELEGTH)
			grpName = grpName + " ";
		
		System.out.println("grpName=<"+grpName+"> length="+grpName.length());
		
		byte[] grpNameBytes = grpName.getBytes();
		byte grpNameSetBytes[] = new byte[1 + 1 + 1 + grpNameBytes.length ];
		grpNameSetBytes[0] = 0x55;
		grpNameSetBytes[1] = reqno;
		grpNameSetBytes[2] = grpindex;
		for (int index = 0; index < grpNameBytes.length; index++)
			grpNameSetBytes[3 + index] = grpNameBytes[index];
		processReqWithRetries(reqno, grpNameSetBytes);
	}
	
	public byte[] getGroupName(byte grpindex) throws Exception {
		this.checkConnection();
		byte reqno = this.getNextReqno();
		byte[] writeBytes = new byte[] { (byte) 0x58, reqno, grpindex };
		return processReqWithRetries(reqno, writeBytes);
	}
	
	public void setSchedulerName(byte schedulerindex, String schedulerName) throws Exception {
		this.checkConnection();
		byte reqno = this.getNextReqno();
		schedulerName = schedulerName.trim();
		while( schedulerName.length()<NAMELEGTH)
			schedulerName = schedulerName + " ";
		
		System.out.println("schedulerName=<"+schedulerName+"> length="+schedulerName.length());
		
		byte[] schedulerNameBytes = schedulerName.getBytes();
		byte schedulerNameSetBytes[] = new byte[1 + 1 + 1 + schedulerNameBytes.length ];
		schedulerNameSetBytes[0] = 0x54;
		schedulerNameSetBytes[1] = reqno;
		schedulerNameSetBytes[2] = schedulerindex;
		for (int index = 0; index < schedulerNameBytes.length; index++)
			schedulerNameSetBytes[3 + index] = schedulerNameBytes[index];
		processReqWithRetries(reqno, schedulerNameSetBytes);
	}
	
	public byte[] getSchedulerName(byte schedulerindex) throws Exception {
		this.checkConnection();
		byte reqno = this.getNextReqno();
		byte[] writeBytes = new byte[] { (byte) 0x57, reqno, schedulerindex };
		return processReqWithRetries(reqno, writeBytes);
	}
	
	public void setSwitchType(byte devindex, boolean isdimmable, boolean isinv, byte devtype) throws Exception {
		this.checkConnection();
		byte reqno = this.getNextReqno();
		byte dimvalue = (byte)(isdimmable?1:0);
		byte invvalue = (byte)(isinv?1:0);
		byte prop = (byte)((dimvalue<<7)| (invvalue<<6) | devtype);
		prop = (byte)(prop&(0xff));
		System.out.println("prop.devindex="+devindex+"  isdimmable="+isdimmable+"  isinv="+isinv+".type="+devtype+" prop..."+prop+" byte value>>"+Byte.valueOf(prop).byteValue()+"bytevalue..."+Integer.toBinaryString(prop));
		byte bytes[] = new byte[]{(byte)0x5f, reqno, 1, devindex, prop };
		processReqWithRetries(reqno, bytes);
	}
	
	public byte[] getSwitchTypes() throws Exception {
		this.checkConnection();
		byte reqno = this.getNextReqno();
		byte[] writeBytes = new byte[] { (byte) 0x60, reqno};
		return processReqWithRetries(reqno, writeBytes);
	}
	
	public static boolean isDimmableByProp(byte prop){
		return ((prop>>7)&(0x01)) == 1;
	}
	
	public static boolean isInvByProp(byte prop){
		return ((prop>>6)&(0x01)) == 1;
	}
	
	public static byte getDevTypeByProp(byte prop){
		return (byte)(prop&0x3f);
	}

	public boolean isZorbaDevice(String hostName) {
		String error = initDevice(null, null, null, hostName);
		closeDevice();
		return (error == null);
	}
	
	public void timeout() {
		activedTime = System.currentTimeMillis();

		System.out.println("closingraju tioeout");
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				if( activedTime == -1){
					System.out.println("closingraju by idletimeout already timedout");
					return;
				}
				long currentTime = System.currentTimeMillis();
				long diffTime = currentTime - activedTime;
				if (diffTime >= idletimeout) {
					System.out.println("closingraju by idletimeout "+diffTime+" "+idletimeout);
					closeDevice();
					activedTime = -1;
				} else {
					System.out.println("closingraju Ignored");
				}

			}
		}, idletimeout);

	}
	public boolean enableNotificationForRooms(IOTMessageListener listener, RoomData currentRoom, ArrayList<RoomData> roomDataList){
		boolean enabled = false;
		if( iotConnection != null && !iotConnection.isConnected()) {
			iotConnection.closeConnection();
			iotConnection = null;
		}
		if( iotConnection == null) {
			iotConnection = new AwsConnection(activity,currentRoom, connectionListener);
		}
		System.out.println("...iotConnection...."+iotConnection.isConnected());
		if( iotConnection.isConnected()) {
			for(RoomData rd: roomDataList){
				if( !rd.getAddress().equals(devAddress)) {
					iotConnection.enableNotificationForRoom(listener, rd);
				}
			}
			iotConnection.setNotificationListener(activity,listener);
			isConnected = true;
			enabled = true;
			connectionType = CONNETIONTYPE_DATA;
		} else {
			iotConnection.closeConnection();
			iotConnection = null;
		}
		return enabled;
	}
}

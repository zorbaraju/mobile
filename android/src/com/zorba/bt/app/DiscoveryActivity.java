package com.zorba.bt.app;

import android.app.AlertDialog.Builder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RadioButton;
import java.util.ArrayList;
import java.util.List;

import com.zorba.bt.app.bluetooth.BtHwLayer;
import com.zorba.bt.app.bluetooth.NetworkInfo;
import com.zorba.bt.app.dao.RoomData;
import com.zorba.bt.app.db.BtLocalDB;
import com.zorba.bt.app.utils.BackgroundTask;
import com.zorba.bt.app.utils.BackgroundTaskDialog;

public class DiscoveryActivity extends ZorbaActivity {
	
	static final int DISCOVERYTYPE_BT = 0;
	static final int DISCOVERYTYPE_WR = DISCOVERYTYPE_BT+1;
	static final int DISCOVERYTYPE_WAP = DISCOVERYTYPE_WR+1;
	static final int ENABLE_BT = 1;
	ImageButton deleteButton = null;
	private String deletedRoomList = "";
	LinearLayout discoveryContent = null;
	GifAnimationDrawable little = null;
	WifiManager wifiManager = null;
	WifiScanReceiver wifiReciever = null;
	List<ScanResult> sr = null;
	private BluetoothManager mBluetoothManager;
	private BluetoothAdapter mBTA;
	private BroadcastReceiver mReceiver;
	private String roomNameAddedNewly = "";
	FlowLayout roomsContent = null;
	ImageTextButton selectedRoomButton = null;
	Button saveButton = null;
	BtHwLayer btHwLayer = null;
	RadioButton btdiscoveryBox = null;
	RadioButton wifirdiscoveryBox = null;
	RadioButton wifiapdiscoveryBox = null;
	String currentWifiSSID = null;
	boolean isChangedToAPMode = false;
	int currentDiscoveryType = 0;
	TextView devPwdView = null;
	
	private void addRoomButton(RoomData var1) {
		final ImageTextButton var2 = new ImageTextButton(this);
		var2.setBackgroundImage(R.drawable.scheduler);
		var2.setText(var1.getName());
		var2.setOnClickListener(new OnClickListener() {
			public void onClick(View var1) {
				DiscoveryActivity.this.deleteButton.setVisibility(View.GONE);
				int var3 = DiscoveryActivity.this.roomsContent.getChildCount();

				for (int var2 = 0; var2 < var3; ++var2) {
					((ImageTextButton) DiscoveryActivity.this.roomsContent.getChildAt(var2)).setBorderSelected(false);
				}

			}
		});
		var2.setOnLongClickListener(new OnLongClickListener() {
			public boolean onLongClick(View var1) {
				DiscoveryActivity.this.deleteButton.setVisibility(View.VISIBLE);
				DiscoveryActivity.this.selectedRoomButton = var2;
				var2.setBorderSelected(true);
				return true;
			}
		});
		LayoutParams var3 = new LayoutParams(-2, -2);
		var3.setMargins(10, 10, 10, 10);
		var2.setLayoutParams(var3);
		this.roomsContent.addView(var2);
	}

	private String createRoom(final DiscoveryRoom droom, final String pwd, final int nthDevice) {
		final String validName = CommonUtils.isValidName(this, droom.getRoomName());
		if (validName == null) {
			return null;
		} else if (BtLocalDB.getInstance(this.getApplication()).isRoomNameExist(validName)) {
			CommonUtils.AlertBox(this, "Already exist", "Name" + "(" + validName + ")" + " is exist already");
			return null;
		} else {
			saveButton.setEnabled(false);
			
			BackgroundTaskDialog task = new BackgroundTaskDialog(this) {
				@Override
				public Object runTask(Object params) {
					String ipaddress = null;
					System.err.println("currentDiscoveryType>>>>>>>>>>>"+currentDiscoveryType);
					if (currentDiscoveryType == DISCOVERYTYPE_WAP) {
						if( !btHwLayer.makeWifiEnabled()) {
							return null;
						}
						String ipaddr = CommonUtils.enableNetwork(DiscoveryActivity.this, droom.getDeviceName(),
								droom.getDeviceName());
						isChangedToAPMode = true;
						if (ipaddr == null) {
							CommonUtils.AlertBox(DiscoveryActivity.this, "Discovery",
									"Ip address is not inited," + droom.getRoomName());
							return null;
						}
						try {
							String error = btHwLayer.initDevice(droom.getDeviceAddress(), null, ipaddr, devPwdView.getText().toString());
							if( error != null ) {
								CommonUtils.AlertBox(DiscoveryActivity.this, "Discovery",
										"Not able to init connection " + droom.getRoomName()+" : "+error);
								return null;
							}
							/*try {
								btHwLayer.setWifiAPMode(true);
							} catch (Exception e1) {
								CommonUtils.AlertBox(DiscoveryActivity.this, "Discovery",
										"Not able to set access point mode " + droom.getRoomName());
								return null;
							}*/
							try {
								byte [] response = btHwLayer.changePwd(devPwdView.getText().toString());
								if( response != null){
									error = new String(response).toLowerCase();
									if( !error.equals("ok")) {
										CommonUtils.AlertBox(DiscoveryActivity.this, "Discovery",
												"Not able to change the password " + droom.getRoomName()+" : "+error);
										return null;
									}
								}
							} catch (Exception e1) {
								e1.printStackTrace();
								CommonUtils.AlertBox(DiscoveryActivity.this, "Discovery",
										"Not able to change the password " + droom.getRoomName());
								return null;
							}
							int numberOfDevices = btHwLayer.getNumberOfDevices();
							CommonUtils.setMaxNoDevices(numberOfDevices);
							//btHwLayer.setDateAndTime();
						} catch (Exception e) {
							e.printStackTrace();
							return null;
						}
					} else if ( currentDiscoveryType == DISCOVERYTYPE_BT){
						System.err.println("Trying for bt........");
						String error = btHwLayer.initDevice(droom.getDeviceAddress(), null, null, devPwdView.getText().toString());
						if( error != null ) {
							CommonUtils.AlertBox(DiscoveryActivity.this, "Discovery",
									"Not able to init connection " + droom.getRoomName()+" : "+error);
							return null;
						}
						try {
							byte [] response = btHwLayer.changePwd(devPwdView.getText().toString());
							if( response != null){
								error = new String(response).toLowerCase();
								if( !error.equals("ok")) {
									CommonUtils.AlertBox(DiscoveryActivity.this, "Discovery",
											"Not able to change the password " + droom.getRoomName()+" : "+error);
									return null;
								}
							}
						} catch (Exception e1) {
							e1.printStackTrace();
							CommonUtils.AlertBox(DiscoveryActivity.this, "Discovery",
									"Not able to change the password " + droom.getRoomName());
							return null;
						}
						try {
							int numberOfDevices = btHwLayer.getNumberOfDevices();
							CommonUtils.setMaxNoDevices(numberOfDevices);
							//btHwLayer.setDateAndTime();
						} catch (Exception e) {
							System.out.println("Not able to get number of devices");
							e.printStackTrace();
							return null;
						}
						try{
							btHwLayer.closeDevice();
						}catch(Exception e){
							System.out.println("Closing device..."+e.getMessage());
						}
					} else {
						System.out.println("currentDiscoveryType.....wr");
						NetworkInfo networkInfo = CommonUtils.getNetworkInfo();
						if (networkInfo != null && nthDevice < networkInfo.unusedIndex.length)
							ipaddress = networkInfo.subnet + "." + networkInfo.unusedIndex[nthDevice];
						else {
							ipaddress = "null";
						}
						System.out.println(currentWifiSSID+"currentDiscoveryType.....wr enabling network...devname..."+droom.getDeviceName()+"...."+ipaddress);
						String ipaddr = CommonUtils.enableNetwork(DiscoveryActivity.this, droom.getDeviceName(),
								droom.getDeviceName());
						System.out.println("Afeter enabling the network ipaddress is "+ipaddr);
						
						String error = btHwLayer.initDevice(droom.getDeviceAddress(), null, ipaddr, devPwdView.getText().toString());
						if( error != null ) {
							CommonUtils.AlertBox(DiscoveryActivity.this, "Discovery",
									"Not able to init connection " + droom.getRoomName()+" : "+error);
							return null;
						}
						try {
							System.err.println("Chaning pwd");
							byte [] response = btHwLayer.changePwd(devPwdView.getText().toString());
							if( response != null){
								error = new String(response).toLowerCase();
								if( !error.equals("ok")) {
									CommonUtils.AlertBox(DiscoveryActivity.this, "Discovery",
											"Not able to change the password " + droom.getRoomName()+" : "+error);
									return null;
								}
							}
						} catch (Exception e1) {
							e1.printStackTrace();
							CommonUtils.AlertBox(DiscoveryActivity.this, "Discovery",
									"Not able to change the password " + droom.getRoomName());
							return null;
						}
						try {
							int numberOfDevices = btHwLayer.getNumberOfDevices();
							CommonUtils.setMaxNoDevices(numberOfDevices);
							//btHwLayer.setDateAndTime();
						} catch (Exception e) {
							System.out.println("Not able to get number of devices");
							e.printStackTrace();
							return null;
						}
						try {
							System.err.println("Chaning ipadress...."+currentWifiSSID+"  pwd="+pwd+" ipaddress..."+ipaddress);
							if( currentWifiSSID != null) {
								byte[] response = btHwLayer.setIpAddress(networkInfo.ssid, pwd, ipaddress);
								if (response != null) {
									String responseStr = new String(response).toLowerCase();
									if (!responseStr.equals("ok")) {
										CommonUtils.AlertBox(DiscoveryActivity.this, "Discovery",
												"Ip address is not set for " + droom.getRoomName());
										return null;
									}
								} else {
									CommonUtils.AlertBox(DiscoveryActivity.this, "Ip set",
											"No response from device for ip set");
									ipaddress = "null";
								}
								Thread.sleep(2000);		
							}
						}catch(Exception e){
							System.out.println("Closing device..."+e.getMessage());
							CommonUtils.AlertBox(DiscoveryActivity.this, "Ip set",
									"No response from device for ip set");
							return null;
						}
						/*try {
							System.err.println("Seting station mode");
							btHwLayer.setWifiAPMode(false);
						} catch (Exception e1) {
							e1.printStackTrace();
							CommonUtils.AlertBox(DiscoveryActivity.this, "Discovery",
									"Not able to set station mode " + droom.getRoomName());
							return null;
						}*/
						try{
							btHwLayer.closeDevice();
						}catch(Exception e){
							System.out.println("Closing device..."+e.getMessage());
						}
					}
					String ssid = null;
					if( currentDiscoveryType == DISCOVERYTYPE_WAP) {
						ssid = droom.getDeviceName();
					}
					RoomData newRoom = new RoomData(droom.getDeviceAddress(), validName, droom.isRGBType(),
							ipaddress, ssid, droom.getDeviceName());
					
					return newRoom;
				}

				@Override
				public void finishedTask(Object result) {
					if( result == null) {
						saveButton.setEnabled(true);
						return;
					}
					DiscoveryActivity.this.roomNameAddedNewly = droom.getRoomName();
					DiscoveryActivity.this.discoveryContent.removeView(droom);
					
					RoomData createdRoom = (RoomData)result;
					BtLocalDB.getInstance(DiscoveryActivity.this).addRoom(createdRoom);
					BtLocalDB.getInstance(DiscoveryActivity.this).setDevicePwd(devPwdView.getText().toString());
					addRoomButton(createdRoom);
					saveButton.setEnabled(false);
				}
			};
			return validName;
		}
	}

	private boolean isBtInView(LinearLayout var1, String var2) {
		int var4 = var1.getChildCount();
		int var3 = 0;

		boolean var5;
		while (true) {
			if (var3 >= var4) {
				var5 = false;
				break;
			}

			if (((DiscoveryRoom) var1.getChildAt(var3)).getDeviceName().equals(var2)) {
				var5 = true;
				break;
			}

			++var3;
		}

		return var5;
	}

	private void startDiscoveryProcess() {
		presettingsForDiscovery();
		
		if (currentDiscoveryType == DISCOVERYTYPE_BT) {
			if (mBTA.isDiscovering()) {
				mBTA.cancelDiscovery();
			}
			mBTA.startDiscovery();
			new Thread(new Runnable() {

				@Override
				public void run() {

					BackgroundTask task = new BackgroundTask() {
						protected void onPreExecute() {
							try {
								Thread.sleep(5000);
							} catch (InterruptedException var2) {
								var2.printStackTrace();
							}
						}

						@Override
						public Object runTask(Object params) {
							mBTA.cancelDiscovery();
							DiscoveryActivity.this.stopDiscoveryProcess();
							return null;
						}

						@Override
						public void finishedTask(Object result) {
							TextView pwd = (TextView) findViewById(R.id.wifiPwdText);
							pwd.setEnabled(currentDiscoveryType == DISCOVERYTYPE_WR);
						}
					};
				}

			}).start();
		} 
		if (currentDiscoveryType != DISCOVERYTYPE_BT) {
			if(currentDiscoveryType == DISCOVERYTYPE_WR)
				populateCurrentSSID();
			isChangedToAPMode = false;
			wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
			wifiManager.setWifiEnabled(true);
			wifiReciever = new WifiScanReceiver();
			registerReceiver(wifiReciever, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		      
			wifiManager.startScan();
			new Thread(new Runnable() {

				@Override
				public void run() {

					BackgroundTask task = new BackgroundTask() {
						protected void onPreExecute() {
							try {
								Thread.sleep(5000);
							} catch (InterruptedException var2) {
								var2.printStackTrace();
							}
						}

						@Override
						public Object runTask(Object params) {
							return null;
						}

						@Override
						public void finishedTask(Object result) {
							if( sr == null) {
								return;
							}
							ArrayList localList = new ArrayList<String>();
							for(ScanResult wc:sr){
								String ssid = wc.SSID;
								if (ssid.startsWith("ZOR")) {
									if (localList.contains(ssid) || BtLocalDB.getInstance(DiscoveryActivity.this).isRoomExists(ssid)) {
										Logger.e(DiscoveryActivity.this, "Discovery",
												"Device with " + ssid + " is already in List");
										continue;
									}
									// adding room here.....
									String deviceName = ssid;
									localList.add(deviceName);
									if(currentDiscoveryType == DISCOVERYTYPE_WR)
										ssid = null;
									DiscoveryRoom var7 = new DiscoveryRoom(DiscoveryActivity.this.getApplication(), null, deviceName,
											ssid);
									DiscoveryActivity.this.discoveryContent.addView(var7);
									Logger.e(DiscoveryActivity.this, "Discovery",
											"Deviece with the name " + ssid + " is added in the panel");
								}
							}
							List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();	
							for(WifiConfiguration wc:list){
								String ssid = wc.SSID;
								ssid = ssid.substring(1, ssid.length()-1);
								if (ssid.startsWith("ZOR")) {
									if (localList.contains(ssid) || BtLocalDB.getInstance(DiscoveryActivity.this).isRoomExists(ssid)) {
										Logger.e(DiscoveryActivity.this, "Discovery",
												"Device with " + ssid + " is already in List");
										continue;
									}
									// adding room here.....
									String deviceName = ssid;
									localList.add(deviceName);
									if(currentDiscoveryType == DISCOVERYTYPE_WR)
										ssid = null;
									DiscoveryRoom var7 = new DiscoveryRoom(DiscoveryActivity.this.getApplication(), null, deviceName,
											ssid);
									DiscoveryActivity.this.discoveryContent.addView(var7);
									Logger.e(DiscoveryActivity.this, "Discovery",
											"Deviece with the name " + ssid + " is added in the panel");
								}
							}
							localList.clear();
							TextView pwd = (TextView) findViewById(R.id.wifiPwdText);
							pwd.setEnabled(currentDiscoveryType == DISCOVERYTYPE_WR);
							DiscoveryActivity.this.stopDiscoveryProcess();
						}
						
					};
				}

			}).start();
			
			
		}
	}

	private void presettingsForDiscovery() {
		this.discoveryContent.removeAllViews();
		TextView pwd = (TextView) findViewById(R.id.wifiPwdText);
		pwd.setEnabled(currentDiscoveryType == DISCOVERYTYPE_WR);
		if (!little.isRunning())
			little.start();
		saveButton.setEnabled(false);
		((ImageView) findViewById(R.id.spinnertriangle)).setVisibility(View.GONE);
		((TextView) findViewById(R.id.controllerValue)).setText(getDiscoveryModeStr()+" is going on");
		btdiscoveryBox.setEnabled(false);
		wifirdiscoveryBox.setEnabled(false);
		wifiapdiscoveryBox.setEnabled(false);
	}

	private String getDiscoveryModeStr() {
		String modeStr = "Bluetooth Discovery";
		if (currentDiscoveryType == DISCOVERYTYPE_WR) {
			modeStr = "Station Discovery";
		} else if (currentDiscoveryType == DISCOVERYTYPE_WAP) {
			modeStr = "Access point Discovery";
		}
		return modeStr;
	}

	private void populateCurrentSSID() {
		if( currentDiscoveryType == DISCOVERYTYPE_WR) {
			WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
			WifiInfo currentWifi = wifiManager.getConnectionInfo();
			if( currentWifi == null || currentWifi.getSSID() == null || currentWifi.getSSID().isEmpty()){
				CommonUtils.AlertBox(this, "Discovery", "Wifi connection is not enabled");
				return;
			}
			currentWifiSSID = currentWifi.getSSID().substring(1, currentWifi.getSSID().length()-1);
		} else {
			currentWifiSSID = null;
		}
		System.out.println("currenct ssid..."+currentWifiSSID);
	}

	private void findFreeIp() {
		try {
			int numberOfDevicesDiscovered = discoveryContent.getChildCount();
			CommonUtils.getUnUsedIpInfo(DiscoveryActivity.this, numberOfDevicesDiscovered);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void stopDiscoveryProcess() {
		this.runOnUiThread(new Runnable() {
			public void run() {
				if( currentDiscoveryType == DISCOVERYTYPE_WR) {
					Thread th = new Thread(new Runnable() {
						public void run() {
							findFreeIp();
						}
					});
					th.start();
				}
				((TextView) DiscoveryActivity.this.findViewById(R.id.controllerValue))
						.setText(getDiscoveryModeStr()+" is done");
				((ImageView) DiscoveryActivity.this.findViewById(R.id.spinnertriangle)).setVisibility(0);
				DiscoveryActivity.this.little.stop();
				saveButton.setEnabled(true);
				btdiscoveryBox.setEnabled(true);
				wifirdiscoveryBox.setEnabled(true);
				wifiapdiscoveryBox.setEnabled(true);
				
			}
		});
	}

	public void onActivityResult(int var1, int var2, Intent var3) {
		this.startDiscoveryProcess();
	}

	@Override
	public void onBackPressed() {
		if( isChangedToAPMode && currentWifiSSID != null){
			TextView pwdview = (TextView) findViewById(R.id.wifiPwdText);
			CommonUtils.enableNetwork(this, currentWifiSSID, pwdview.getText().toString());
		}
		Intent intent = new Intent();
		intent.putExtra("newroomname", this.roomNameAddedNewly);
		intent.putExtra("deletedrooms", this.deletedRoomList);
		this.setResult(1, intent);
		super.onBackPressed();
	}

	protected void onCreate(Bundle var1) {
		super.onCreate(var1);
		this.setContentView(R.layout.discoverylayout);
		btHwLayer = BtHwLayer.getInstance(this);
		btHwLayer.unregister();
		btHwLayer.closeDevice();
		btdiscoveryBox = (RadioButton) findViewById(R.id.btdiscovery);
		wifirdiscoveryBox = (RadioButton) findViewById(R.id.wifirdiscovery);
		wifiapdiscoveryBox = (RadioButton) findViewById(R.id.wifiapdiscovery);
		wifiapdiscoveryBox.setChecked(true);
		currentDiscoveryType = DISCOVERYTYPE_WAP;
		
		devPwdView = (TextView) findViewById(R.id.devPwdText);
		devPwdView.setText(BtLocalDB.getInstance(this).getDevicePwd());
		btdiscoveryBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if( isChecked ) {
					currentDiscoveryType = DISCOVERYTYPE_BT;
					if (!mBTA.isEnabled()) {
						startActivityForResult(new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"), 1);
					}
					
					startDiscoveryProcess();
				}
			}
		});
		wifirdiscoveryBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if( isChecked ) {
					currentDiscoveryType = DISCOVERYTYPE_WR;
					populateCurrentSSID();
					startDiscoveryProcess();
				}
			}
		});
		wifiapdiscoveryBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if( isChecked ) {
					currentDiscoveryType = DISCOVERYTYPE_WAP;
					populateCurrentSSID();
					startDiscoveryProcess();
				}
			}
		});
		saveButton = (Button) findViewById(R.id.savebutton);
		saveButton.setEnabled(false);
		((ImageView) this.findViewById(R.id.spinnertriangle)).setOnClickListener(new OnClickListener() {
			public void onClick(View var1) {
				DiscoveryActivity.this.startDiscoveryProcess();
			}
		});
		if (mBluetoothManager == null) {
			mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			if (mBluetoothManager == null) {
				CommonUtils.AlertBox(this, "Bt Manager", "Unable to initialize BluetoothManager.");
				return;
			}
		}
		this.mBTA = mBluetoothManager.getAdapter();
		if (this.mBTA == null) {
			CommonUtils.AlertBox(this, "Bt Manager", "No Bluetooth on this handset");
			this.finish();
			return;
		}
		this.discoveryContent = (LinearLayout) this.findViewById(R.id.discoveryContent);
		this.mReceiver = new BroadcastReceiver() {
			public void onReceive(Context var1, Intent var2) {
				String var4 = var2.getAction();
				Logger.e(DiscoveryActivity.this, "Discovery", "onReceive " + var4);
				if ("android.bluetooth.device.action.FOUND".equals(var4)) {
					BluetoothDevice var5 = (BluetoothDevice) var2
							.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
					if (var5.getType() != BluetoothDevice.DEVICE_TYPE_LE) {
						Logger.e(DiscoveryActivity.this, "Discovery",
								"Device with " + var5.getName() + " is not our device");
					} else {
						if (!var5.getName().startsWith("ZOR"))
							return;
						String macaddress = var5.getAddress();
						String devname = var5.getName();
						if (BtLocalDB.getInstance(DiscoveryActivity.this).isRoomExists(devname)) {
							Logger.e(DiscoveryActivity.this, "Discovery",
									"Device with " + macaddress + " is already in List");
						} else {
							String var3 = var5.getName() + " - " + var5.getAddress();
							Logger.e(DiscoveryActivity.this, "Discovery", "onReceive found " + var3);
							if (DiscoveryActivity.this.isBtInView(DiscoveryActivity.this.discoveryContent,
									var5.getName())) {
								Logger.e(DiscoveryActivity.this, "Discovery",
										"Device with " + macaddress + " is already in Panel");
							} else {
								DiscoveryRoom var7 = new DiscoveryRoom(DiscoveryActivity.this.getApplication(), macaddress,
										var5.getName());
								DiscoveryActivity.this.discoveryContent.addView(var7);
								Logger.e(DiscoveryActivity.this, "Discovery",
										"Deviece with the name " + var5.getName() + " is added in the panel");
							}
						}
					}
				}
			}
		};
		IntentFilter intent = new IntentFilter("android.bluetooth.device.action.FOUND");
		this.registerReceiver(this.mReceiver, intent);
		saveButton.setOnClickListener(new OnClickListener() {
			public void onClick(View var1) {
				
				String pwd = "null";
				TextView pwdview = (TextView) findViewById(R.id.wifiPwdText);
				
				pwd = pwdview.getText().toString();
				System.out.println("Pwd........"+pwd);
				if (currentDiscoveryType == DISCOVERYTYPE_WR && pwd.isEmpty()) {
					CommonUtils.AlertBox(DiscoveryActivity.this, "Wifi", "Password is empty");
					return;
				}
				int numdiscovered = DiscoveryActivity.this.discoveryContent.getChildCount();
				ArrayList<DiscoveryRoom> nonEmptyChildren = new ArrayList<DiscoveryRoom>();
				for (int nth=0; nth<numdiscovered; nth++) {
					DiscoveryRoom disRoom = (DiscoveryRoom) DiscoveryActivity.this.discoveryContent.getChildAt(nth);
					if(!disRoom.getRoomName().trim().isEmpty()) {
						nonEmptyChildren.add(0,disRoom);
					}
				}
				if( nonEmptyChildren.size() == 0){
					CommonUtils.AlertBox(DiscoveryActivity.this, "Discovery", "Name is empty");
					return;
				}
				numdiscovered = nonEmptyChildren.size();
				for(int nth=0;nth<numdiscovered; nth++) {
					DiscoveryRoom disRoom = nonEmptyChildren.get(nth);
					createRoom(disRoom, pwd, nth);
					
				}
				nonEmptyChildren.clear();
			}
		});
		this.deleteButton = (ImageButton) this.findViewById(R.id.deletebutton);
		this.roomsContent = (FlowLayout) this.findViewById(R.id.roomscontent);
		ArrayList<RoomData> roomArrayList = BtLocalDB.getInstance(this).getRoomList();
		roomArrayList.remove(0);
		for (int var2 = 0; var2 < roomArrayList.size(); ++var2) {
			this.addRoomButton(roomArrayList.get(var2));
		}
		this.deleteButton.setOnClickListener(new OnClickListener() {
			public void onClick(View var1) {
				(new Builder(DiscoveryActivity.this)).setTitle("Delete").setMessage("Do you really want to delete ?")
						.setPositiveButton("Yes", new android.content.DialogInterface.OnClickListener() {
							public void onClick(DialogInterface var1, int var2) {
								BtLocalDB.getInstance(DiscoveryActivity.this)
										.deleteRoom(DiscoveryActivity.this.selectedRoomButton.getText());
								DiscoveryActivity.this.roomsContent
										.removeView(DiscoveryActivity.this.selectedRoomButton);
								DiscoveryActivity.this.deleteButton.setVisibility(8);
								if (DiscoveryActivity.this.deletedRoomList.isEmpty()) {
									DiscoveryActivity.this.deletedRoomList = DiscoveryActivity.this.selectedRoomButton
											.getText();
								} else {
									DiscoveryActivity var3 = DiscoveryActivity.this;
									var3.deletedRoomList = var3.deletedRoomList + "#"
											+ DiscoveryActivity.this.selectedRoomButton.getText();
								}

							}
						}).setNegativeButton("No", new android.content.DialogInterface.OnClickListener() {
							public void onClick(DialogInterface var1, int var2) {
								var1.cancel();
							}
						}).show();
			}
		});
		ImageView var7 = (ImageView) this.findViewById(R.id.loadingImage);
		try {
			GifAnimationDrawable var3 = new GifAnimationDrawable(
					this.getResources().openRawResource(R.drawable.refresh));
			this.little = var3;
			this.little.setOneShot(false);
			var7.setImageDrawable(this.little);
		} catch (Exception var4) {
			var4.printStackTrace();
		}
		Logger.e(this, "Discovery", "Discvoery started");
		this.startDiscoveryProcess();
	}

	public void onDestroy() {
		try {
			if( wifiReciever != null)
				unregisterReceiver(wifiReciever);
		} catch (Exception var2) {
			Logger.e(this, "Discovery", "Not able to unregister wifiReciever" + var2.getMessage());
		}
		
		try {
			if (this.mBTA.isDiscovering()) {
				this.mBTA.cancelDiscovery();
			}
			this.unregisterReceiver(this.mReceiver);
		} catch (Exception var2) {
			Logger.e(this, "Discovery", "Not able to unregister Bt" + var2.getMessage());
		}
		super.onDestroy();
	}
	
	private class WifiScanReceiver extends BroadcastReceiver {
		public void onReceive(Context c, Intent intent) {
			sr = wifiManager.getScanResults();
		}
	}
}

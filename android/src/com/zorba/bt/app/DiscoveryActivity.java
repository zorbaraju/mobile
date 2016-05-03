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
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.ArrayList;
import java.util.List;

import com.zorba.bt.app.bluetooth.BtHwLayer;
import com.zorba.bt.app.bluetooth.NetworkInfo;
import com.zorba.bt.app.dao.RoomData;
import com.zorba.bt.app.db.BtLocalDB;
import com.zorba.bt.app.utils.BackgroundTask;

public class DiscoveryActivity extends ZorbaActivity {
	
	static final int DISCOVERYTYPE_BT = 0;
	static final int DISCOVERYTYPE_WR = DISCOVERYTYPE_BT+1;
	static final int DISCOVERYTYPE_WAP = DISCOVERYTYPE_WR+1;
	static final int ENABLE_BT = 1;
	ImageButton deleteButton = null;
	private String deletedRoomList = "";
	LinearLayout discoveryContent = null;
	GifAnimationDrawable little = null;
	private BluetoothManager mBluetoothManager;
	private BluetoothAdapter mBTA;
	private BroadcastReceiver mReceiver;
	private String roomNameAddedNewly = "";
	FlowLayout roomsContent = null;
	ImageTextButton selectedRoomButton = null;
	Button saveButton = null;
	BtHwLayer btHwLayer = null;
	boolean needip = false;
	RadioButton btdiscoveryBox = null;
	RadioButton wifirdiscoveryBox = null;
	RadioButton wifiapdiscoveryBox = null;
	String currentWifiSSID = null;
	boolean isChangedToAPMode = false;
	int currentDiscoveryType = 0;
	
	private void addRoomButton(RoomData var1) {
		final ImageTextButton var2 = new ImageTextButton(this);
		var2.setBackgroundImage(R.drawable.scheduler);
		var2.setText(var1.getName());
		var2.setOnClickListener(new OnClickListener() {
			public void onClick(View var1) {
				DiscoveryActivity.this.deleteButton.setVisibility(8);
				int var3 = DiscoveryActivity.this.roomsContent.getChildCount();

				for (int var2 = 0; var2 < var3; ++var2) {
					((ImageTextButton) DiscoveryActivity.this.roomsContent.getChildAt(var2)).setBorderSelected(false);
				}

			}
		});
		var2.setOnLongClickListener(new OnLongClickListener() {
			public boolean onLongClick(View var1) {
				DiscoveryActivity.this.deleteButton.setVisibility(0);
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
			
			BackgroundTask task = new BackgroundTask() {
				@Override
				public Object runTask(Object params) {
					String ipaddress = null;
					System.out.println("backend task..."+droom.isWAP);
					if (droom.isWAP) {
						if( !btHwLayer.makeWifiEnabled()) {
							return null;
						}
						System.out.println("backend task...enabling network");
						String ipaddr = CommonUtils.enableNetwork(DiscoveryActivity.this, droom.getDeviceName(),
								droom.getDeviceName());
						isChangedToAPMode = true;
						System.out.println("ipaddr...." + ipaddr);
						if (ipaddr == null)
							return null;
						try {
							NetworkInfo networkInfo = CommonUtils.getNetworkInfo();
							if (networkInfo != null && nthDevice < networkInfo.unusedIndex.length)
								ipaddress = networkInfo.subnet + "." + networkInfo.unusedIndex[nthDevice];
							else {
								ipaddress = "null";
								System.out.println("No ip adress.....");
							}
							System.out.println("Device name......"+droom.getDeviceName());
							String error = btHwLayer.initDevice(droom.getDeviceAddress(), null, ipaddr, CommonUtils.DEVICEPASSWORD);
							if( error != null ) {
								CommonUtils.AlertBox(DiscoveryActivity.this, "Discovery",
										"Not able to init connection " + droom.getRoomName()+" : "+error);
								return null;
							}
							try {
								byte [] response = btHwLayer.changePwd(BtLocalDB.getInstance(DiscoveryActivity.this).getPassword());
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
							if( droom.ssid == null) {
								byte[] response = btHwLayer.setIpAddress(networkInfo.ssid, pwd, ipaddress);
								if (response != null) {
									String responseStr = new String(response).toLowerCase();
									System.out.println("Response for set ip address is.." + responseStr);
									if (!responseStr.equals("ok")) {
										CommonUtils.AlertBox(DiscoveryActivity.this, "Discovery",
												"Ip address is not set for " + droom.getRoomName());
										return null;
									}
								} else {
									CommonUtils.AlertBox(DiscoveryActivity.this, "Ip set",
											"No response from device for ip set");
									System.out.println("Not able to set ip address...");
									ipaddress = "null";
								}
								System.out.println("Changed ipadress ..."+ipaddress);
								ipaddr = ipaddress;
							}
							Thread.sleep(2000);
							try{
								System.out.println("Clissssss");
								btHwLayer.closeDevice();
								System.out.println("Cliss111ssss");
							}catch(Exception e){
								System.out.println("Closing device..."+e.getMessage());
							}
							System.out.println("Clisssssreturn aippp...s"+ipaddr);
							return ipaddr;
						} catch (Exception e) {
							e.printStackTrace();
							return null;
						}
					} else {
						String error = btHwLayer.initDevice(droom.getDeviceAddress(), null, null, CommonUtils.DEVICEPASSWORD);
						if( error != null ) {
							CommonUtils.AlertBox(DiscoveryActivity.this, "Discovery",
									"Not able to init connection " + droom.getRoomName()+" : "+error);
							return null;
						}
						try {
							byte [] response = btHwLayer.changePwd(BtLocalDB.getInstance(DiscoveryActivity.this).getPassword());
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

							if (needip) {
								NetworkInfo networkInfo = CommonUtils.getNetworkInfo();
								if (networkInfo != null && nthDevice < networkInfo.unusedIndex.length)
									ipaddress = networkInfo.subnet + "." + networkInfo.unusedIndex[nthDevice];
								else
									ipaddress = "null";

								byte[] response = btHwLayer.setIpAddress(networkInfo.ssid, pwd, ipaddress);
								Thread.sleep(2000);
								btHwLayer.closeDevice();
								if (response != null) {
									String responseStr = new String(response).toLowerCase();
									System.out.println("Response for set ip address is.." + responseStr);
									if (!responseStr.equals("ok")) {
										CommonUtils.AlertBox(DiscoveryActivity.this, "Discovery",
												"Ip address is not set for " + droom.getRoomName());
										return null;
									}
								} else {
									CommonUtils.AlertBox(DiscoveryActivity.this, "Ip set",
											"No response from device for ip set");
									System.out.println("Not able to set ip address...");
									ipaddress = "null";
								}
							}

						} catch (Exception e) {
							System.out.println("Not able to configure ip address");
							e.printStackTrace();
							return null;
						}
					}

					return ipaddress;
				}

				@Override
				public void finishedTask(Object result) {
					System.out.println("Finished result..." + result);
					if( (currentDiscoveryType != DISCOVERYTYPE_BT) && result == null)
						return;
					String devicename = droom.getDeviceName();
					String ssid = droom.getSSID();
					if( currentDiscoveryType == DISCOVERYTYPE_WR) {
						ssid = null;
					}
					System.out.println("created room.." + droom + "...roomname..." + droom.getRoomName());
					DiscoveryActivity.this.roomNameAddedNewly = droom.getRoomName();
					System.out.println("created room. is removed from discoverycontent.");
					DiscoveryActivity.this.discoveryContent.removeView(droom);
					System.out.println("Nou  discovered devices..after removed....."
							+ DiscoveryActivity.this.discoveryContent.getChildCount());
					RoomData var4 = new RoomData(droom.getDeviceAddress(), validName, droom.isRGBType(),
							(String) result, ssid, devicename);
					BtLocalDB.getInstance(DiscoveryActivity.this).addRoom(var4);
					addRoomButton(var4);
					saveButton.setEnabled(false);
					System.out.println("validaname...." + validName);
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
		this.discoveryContent.removeAllViews();
		if (!btHwLayer.isWifiEnabled()) {
			(new Builder(DiscoveryActivity.this)).setTitle("Assign Ip").setMessage("Do you want to assign ip ?")
					.setPositiveButton("Yes", new android.content.DialogInterface.OnClickListener() {
						public void onClick(DialogInterface var1, int var2) {
							needip = true;
							System.out.println("need ip.make true." + needip);
							runDiscoveryWithYesOrNo();
						}
					}).setNegativeButton("No", new android.content.DialogInterface.OnClickListener() {
						public void onClick(DialogInterface var1, int var2) {
							needip = false;
							System.out.println("need ip.make false." + needip);
							var1.cancel();
							runDiscoveryWithYesOrNo();
						}
					}).show();

		} else {
			runDiscoveryWithYesOrNo();
		}

		/*
		*/
	}

	private void runDiscoveryWithYesOrNo() {
		if (currentDiscoveryType == DISCOVERYTYPE_BT) {
			if (!little.isRunning())
				little.start();
			saveButton.setEnabled(false);
			((ImageView) findViewById(R.id.spinnertriangle)).setVisibility(8);
			((TextView) findViewById(R.id.controllerValue)).setText("Bluetooth Discovery is going on");
			if (mBTA.isDiscovering()) {
				mBTA.cancelDiscovery();
			}
			System.out.println("Starting discovery");
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
							System.out.println("After delay");

						}

						@Override
						public Object runTask(Object params) {
							System.out.println("runtask");
							mBTA.cancelDiscovery();
							if (needip) {
								if (!btHwLayer.makeWifiEnabled())
									return null;
								findFreeIp();
							}
							DiscoveryActivity.this.stopDiscoveryProcess();
							return null;
						}

						@Override
						public void finishedTask(Object result) {
							TextView pwd = (TextView) findViewById(R.id.wifiPwdText);
							pwd.setEnabled(needip);
						}
					};
				}

			}).start();
		} if (currentDiscoveryType != DISCOVERYTYPE_BT) {
			if (!little.isRunning())
				little.start();
			saveButton.setEnabled(false);
			((ImageView) findViewById(R.id.spinnertriangle)).setVisibility(8);
			populateCurrentSSID();
			isChangedToAPMode = false;
			WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
			List<ScanResult> sr = wifiManager.getScanResults();
			for(ScanResult wc:sr){
				System.out.println("sss..scanresult."+wc.SSID);
				System.out.println("ssid...."+wc.SSID);
				String ssid = wc.SSID;
				System.out.println("ssid...."+ssid);
				if (ssid.startsWith("ZOR")) {
					if (BtLocalDB.getInstance(DiscoveryActivity.this).isRoomExists(ssid)) {
						Logger.e(DiscoveryActivity.this, "Discovery",
								"Device with " + ssid + " is already in List");
						continue;
					}
					// adding room here.....
					System.out.println("Found our device..." + ssid);
					String deviceName = ssid;
					if(currentDiscoveryType == DISCOVERYTYPE_WR)
						ssid = null;
					DiscoveryRoom var7 = new DiscoveryRoom(DiscoveryActivity.this.getApplication(), null, deviceName,
							ssid, true);
					DiscoveryActivity.this.discoveryContent.addView(var7);
					Logger.e(DiscoveryActivity.this, "Discovery",
							"Deviece with the name " + ssid + " is added in the panel");
					System.out.println("Dosing 555wfi");
				}
			}
			Thread th = new Thread(new Runnable() {
				public void run() {
					findFreeIp();
				}
			});
			th.start();
			
			stopDiscoveryProcess();
		}
	}

	private void populateCurrentSSID() {
		if( currentDiscoveryType == DISCOVERYTYPE_WR) {
			WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
			WifiInfo currentWifi = wifiManager.getConnectionInfo();
			if( currentWifi == null || currentWifi.getSSID() == null || currentWifi.getSSID().isEmpty()){
				CommonUtils.AlertBox(this, "Discovery", "Wifi connection is not enabled");
				return;
			}
			System.out.println("SSID..."+currentWifi.getSSID());
			currentWifiSSID = currentWifi.getSSID().substring(1, currentWifi.getSSID().length()-1);
		} else {
			currentWifiSSID = null;
		}
		System.out.println("currentWifiSSID....."+currentWifiSSID);
	}

	private void findFreeIp() {
		try {
			int numberOfDevicesDiscovered = discoveryContent.getChildCount();
			System.out.println("Rooms Discovered...." + numberOfDevicesDiscovered);
			CommonUtils.getUnUsedIpInfo(DiscoveryActivity.this, numberOfDevicesDiscovered);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void stopDiscoveryProcess() {
		this.runOnUiThread(new Runnable() {
			public void run() {
				((TextView) DiscoveryActivity.this.findViewById(R.id.controllerValue))
						.setText("Bluetooth Discovery is stopped");
				((ImageView) DiscoveryActivity.this.findViewById(R.id.spinnertriangle)).setVisibility(0);
				DiscoveryActivity.this.little.stop();
				saveButton.setEnabled(true);
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
		System.out.println("onBack roomNameAddedNewly=" + roomNameAddedNewly + " deletedrooms.." + deletedRoomList);
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
		btdiscoveryBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if( isChecked ) {
					currentDiscoveryType = DISCOVERYTYPE_BT;
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
		if (!this.mBTA.isEnabled()) {
			this.startActivityForResult(new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"), 1);
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
						System.out.println("Device name is " + var5.getName());
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
				System.out.println("Nou  discovered devices..."+pwdview.getVisibility()+".."+View.VISIBLE);
				pwd = pwdview.getText().toString();
				if (needip && pwd.isEmpty()) {
					CommonUtils.AlertBox(DiscoveryActivity.this, "Wifi", "Password is empty");
					return;
				}
				System.out.println("Nou  discovered devices..." + DiscoveryActivity.this.discoveryContent.getChildCount());
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
					String roomname = createRoom(disRoom, pwd, nth);
					
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
			if (this.mBTA.isDiscovering()) {
				this.mBTA.cancelDiscovery();
			}
			this.unregisterReceiver(this.mReceiver);
		} catch (Exception var2) {
			Logger.e(this, "Discovery", "Not able to unregister Bt" + var2.getMessage());
		}
		super.onDestroy();
	}
}

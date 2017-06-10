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
import android.graphics.MaskFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RadioButton;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.zorba.bt.app.bluetooth.BtHwLayer;
import com.zorba.bt.app.bluetooth.NetworkInfo;
import com.zorba.bt.app.dao.RoomData;
import com.zorba.bt.app.db.BtLocalDB;
import com.zorba.bt.app.utils.BackgroundTask;
import com.zorba.bt.app.utils.BackgroundTaskDialog;

public class DiscoveryActivity extends ZorbaActivity {

	boolean isLastDiscoveryWifi = false;
	
	static final int DISCOVERYTYPE_BT = 0;
	static final int DISCOVERYTYPE_WR = DISCOVERYTYPE_BT + 1;
	//-spb 270417 remove wifiap static final int	static final int DISCOVERYTYPE_WAP = DISCOVERYTYPE_WR + 1;
	static final int ENABLE_BT = 1;
	SvgView deleteButton = null;
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
	//-spb 270417 remove wifiap 	RadioButton wifiapdiscoveryBox = null;
	String currentWifiSSID = null;
	boolean isChangedToAPMode = false;
	private boolean isMaster = false;

	private void addRoomButton(RoomData var1) {
		final ImageTextButton var2 = new ImageTextButton(this);
		var2.changeDeviceButtonStyle("Unknown", 0);
		var2.setText(var1.getName());
		var2.setOnClickListener(new ZorbaOnClickListener() {
	         public void zonClick(View var1) {
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

	private String createRoom(final DiscoveryRoom droom, final String pwd) {
		final String validName = CommonUtils.isValidName(this, droom.getRoomName());
		if (validName == null) {
			return null;
		} else if (BtLocalDB.getInstance(this.getApplication()).isRoomNameExist(validName)) {
			//-spb 270417 for errors CommonUtils.AlertBox(this, "Already exist", "Name" + "(" + validName + ")" + " is exist already");
			CommonUtils.AlertBox(this,  CommonUtils.getInstance().getErrorString("ERROR26"), "Name" + "(" + validName + ")" +  CommonUtils.getInstance().getErrorString("ERROR27"));
			return null;
		} else {
			saveButton.setEnabled(false);
			
			BackgroundTaskDialog task = new BackgroundTaskDialog(this) {
				@Override
				public Object runTask(Object params) {
					String ipaddress = null;
					int currentDiscoveryType = getCurrentDicoveryMode();
					System.err.println("currentDiscoveryType>>>>>>>>>>>" + currentDiscoveryType);
					//-spb 270417 remove wifiap
					/*
					if (currentDiscoveryType == DISCOVERYTYPE_WAP) {
						if (!btHwLayer.makeWifiEnabled()) {
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
							String error = btHwLayer.initDevice(droom.getRoomName(), droom.getDeviceAddress(), null, ipaddr);
							if (error != null) {
								CommonUtils.AlertBox(DiscoveryActivity.this, "Discovery",
										"Not able to init connection " + droom.getRoomName() + " : " + error);
								return null;
							}
							
							int numberOfDevices = btHwLayer.getNumberOfDevices();
							CommonUtils.setMaxNoDevices(numberOfDevices);
							// btHwLayer.setDateAndTime();
						} catch (Exception e) {
							e.printStackTrace();
							return null;
						}
						//-spb 270417 remove wifiap
					//-spb 270417 remove wifiap } else if (currentDiscoveryType == DISCOVERYTYPE_BT) {
						*/
					if (currentDiscoveryType == DISCOVERYTYPE_BT) {
						System.err.println("Trying for bt........");
						String error = btHwLayer.initDevice(droom.getRoomName(), droom.getDeviceAddress(), null, null);
						if (error != null) {
							//-spb 270417 for errors 
							/*
							CommonUtils.AlertBox(DiscoveryActivity.this, "Discovery",
									"Not able to init connection " + droom.getRoomName() + " : " + error);
							*/
							//-spb 270417 for errors 
							CommonUtils.AlertBox(DiscoveryActivity.this,  CommonUtils.getInstance().getErrorString("ERROR30"),
									 CommonUtils.getInstance().getErrorString("ERROR31") + droom.getRoomName() + " : " + error);
							return null;
						}
						try {
							int numberOfDevices = btHwLayer.getNumberOfDevices();
							CommonUtils.setMaxNoDevices(numberOfDevices);
							// btHwLayer.setDateAndTime();
						} catch (Exception e) {
							System.out.println("Not able to get number of devices");
							e.printStackTrace();
							return null;
						}
						try {
							btHwLayer.closeDevice();
						} catch (Exception e) {
							System.out.println("Closing device..." + e.getMessage());
						}
					} else {
						String ipaddr = null;
						System.out.println("currentDiscoveryType.....wr");
						NetworkInfo networkInfo = null;
						
						if (isStationModeMasterDiscovery()) {
							networkInfo = CommonUtils.getUnUsedIpInfo(DiscoveryActivity.this);
							if (networkInfo != null && networkInfo.unusedIndex != -1) {
								ipaddress = networkInfo.subnet + "." + networkInfo.unusedIndex;
								CommonUtils.getInstance().writeLog("ip address set: " + ipaddress);
							} else {
								ipaddress = "null";
							}
							System.out.println(
									currentWifiSSID + "currentDiscoveryType.....wr enabling network...devname..."
											+ droom.getDeviceName() + "...." + ipaddress);
							ipaddr = CommonUtils.enableNetwork(DiscoveryActivity.this, droom.getDeviceName(),
									droom.getDeviceName());
							System.out.println("Afeter enabling the network ipaddress is " + ipaddr);
						} else {
							ipaddr = droom.getDeviceName();
							ipaddress = droom.getDeviceName();
						}
						String error = btHwLayer.initDevice(droom.getRoomName(), droom.getDeviceAddress(), null, ipaddr);
						if (error != null) {
							//-spb 270417 for errors 
							/*
							CommonUtils.AlertBox(DiscoveryActivity.this, "Discovery",
									"Not able to init connection " + droom.getRoomName() + " : " + error);
							//-spb 270417 for errors 
							*/
							CommonUtils.AlertBox(DiscoveryActivity.this,  CommonUtils.getInstance().getErrorString("ERROR30"),
									 CommonUtils.getInstance().getErrorString("ERROR31") + droom.getRoomName() + " : " + error);
							return null;
						}
						
						try {
							int numberOfDevices = btHwLayer.getNumberOfDevices();
							CommonUtils.setMaxNoDevices(numberOfDevices);
							// btHwLayer.setDateAndTime();
						} catch (Exception e) {
							System.out.println("Not able to get number of devices");
							e.printStackTrace();
							return null;
						}
						try {
							System.err.println("Chaning ipadress...." + currentWifiSSID + "  pwd=" + pwd
									+ " ipaddress..." + ipaddress);
							if (currentWifiSSID != null && isStationModeMasterDiscovery()) {
								byte[] response = btHwLayer.setIpAddress(networkInfo.ssid, pwd, ipaddress);
								if (response != null) {
									if (response[0] != 48) {
										//-spb 270417 for errors 
										/*
										CommonUtils.AlertBox(DiscoveryActivity.this, "Discovery",
												"Ip address is not set for " + droom.getRoomName());
										//-spb 270417 for errors 
										*/
										CommonUtils.AlertBox(DiscoveryActivity.this,  CommonUtils.getInstance().getErrorString("ERROR30"),
												 CommonUtils.getInstance().getErrorString("ERROR32") + droom.getRoomName());
										return null;
									}
									
								} else {
									//-spb 270417 for errors 
									/*
									CommonUtils.AlertBox(DiscoveryActivity.this, "Ip set",
											"No response from device for ip set");
									*/
									//-spb 270417 for errors 
									CommonUtils.AlertBox(DiscoveryActivity.this, CommonUtils.getInstance().getErrorString("ERROR33"),
											CommonUtils.getInstance().getErrorString("ERROR34"));
									ipaddress = "null";
								}
								System.err.println("Seting ipaddress: sleeping for 5 secs for mode");
								Thread.sleep(5000);
								System.err.println("Seting ipaddress: slept for 5 secs");
							}
						} catch (Exception e) {
							System.out.println("Closing device..." + e.getMessage());
							//-spb 270417 for errors 
							/*
							CommonUtils.AlertBox(DiscoveryActivity.this, "Ip set",
									"No response from device for ip set");
							//-spb 270417 for errors 
							*/
							CommonUtils.AlertBox(DiscoveryActivity.this, CommonUtils.getInstance().getErrorString("ERROR33"),
									CommonUtils.getInstance().getErrorString("ERROR34"));
							return null;
						}
						if (isStationModeMasterDiscovery()) {
							try {
								System.err.println("Seting station mode");
								btHwLayer.setWifiAPMode(false);
							} catch (Exception e1) {
								e1.printStackTrace();
								//-spb 270417 for errors 
								/*
								CommonUtils.AlertBox(DiscoveryActivity.this, "Discovery",
										"Not able to set station mode " + droom.getRoomName());
								*/
								//-spb 270417 for errors 
								CommonUtils.AlertBox(DiscoveryActivity.this,  CommonUtils.getInstance().getErrorString("ERROR30"),
										CommonUtils.getInstance().getErrorString("ERROR35") + droom.getRoomName());
								return null;
							}
							try {
								System.err.println("Seting station mode: sleeping for 5 secs for mode");
								Thread.sleep(5000);
								System.err.println("Seting station mode: slept for 5 secs");
							} catch (InterruptedException var2) {
								var2.printStackTrace();
							}
						}
						
						try {
							
							btHwLayer.closeDevice();
							System.out.println("Closed device...successfully" );
						} catch (Exception e) {
							System.out.println("Closing device..." + e.getMessage());
						}
					}
					System.out.println("Closed device...successfully  done" );
					String macaddress = droom.getDeviceAddress();
					String ssid = null;
					//-spb 270417 remove wifiap
					/*
					if (currentDiscoveryType == DISCOVERYTYPE_WAP) {
						ssid = droom.getDeviceName();
					}
					*/
					//-spb 270417 remove wifiap
					if (currentDiscoveryType != DISCOVERYTYPE_BT) {
						macaddress = btHwLayer.getPopulateMacAddress();
					}
					RoomData newRoom = new RoomData(macaddress, validName, droom.isRGBType(), ipaddress, ssid,
							droom.getDeviceName());

					return newRoom;
				}

				@Override
				public void finishedTask(Object result) {
					if (result == null) {
						saveButton.setEnabled(true);
						return;
					}
					DiscoveryActivity.this.roomNameAddedNewly = droom.getRoomName();
					DiscoveryActivity.this.discoveryContent.removeView(droom);

					RoomData createdRoom = (RoomData) result;
					BtLocalDB.getInstance(DiscoveryActivity.this).addRoom(createdRoom);
					System.out.println("Added room in configured panel");
					addRoomButton(createdRoom);
					//+spb 020217 for popup msg after room addition
					//-spb 270417 for errors CommonUtils.AlertBox(DiscoveryActivity.this, "Device added successfully !", "Kindly restart app and add switches using 'Configure Zorba'");
					CommonUtils.AlertBox(DiscoveryActivity.this,  CommonUtils.getInstance().getErrorString("ERROR28"),  CommonUtils.getInstance().getErrorString("ERROR29"));
					//+spb 020217 for popup msg after room addition
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
		final int currentDiscoveryType = getCurrentDicoveryMode();
		System.out.println("Current Discovery Mode is "+currentDiscoveryType);
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
			if (currentDiscoveryType == DISCOVERYTYPE_WR)
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
							if (isStationModeMasterDiscovery()) {
								try {
									Thread.sleep(5000);
								} catch (InterruptedException var2) {
									var2.printStackTrace();
								}
							}
						}

						@Override
						public Object runTask(Object params) {
							ArrayList<String> list = null;
							if (!isStationModeMasterDiscovery()) {
								list = performIpDiscovery();
							}
							return list;
						}

						@Override
						public void finishedTask(Object result) {
							if (!isStationModeMasterDiscovery()) {
								if (result != null) {
									ArrayList<String> list = (ArrayList<String>) result;
									for (String ipaddress : list) {

										String roomname = BtLocalDB.getInstance(DiscoveryActivity.this).findRoomExistsByIp(ipaddress);
										if( roomname != null) {
											Logger.e(DiscoveryActivity.this, "Discovery",
													"Device with " + ipaddress + " is already in List , roomname is "+roomname);
											continue;
										}
										DiscoveryRoom var7 = new DiscoveryRoom(DiscoveryActivity.this.getApplication(),
												null, ipaddress, currentWifiSSID);
										DiscoveryActivity.this.discoveryContent.addView(var7);
									}
									
								}
								DiscoveryActivity.this.stopDiscoveryProcess();
								return;
							}
							if (sr == null) {
								return;
							}
							ArrayList localList = new ArrayList<String>();
							for (ScanResult wc : sr) {
								String ssid = wc.SSID;
								if (ssid.contains("ZOR")) {
									if (localList.contains(ssid)
											|| BtLocalDB.getInstance(DiscoveryActivity.this).isRoomExists(ssid)) {
										Logger.e(DiscoveryActivity.this, "Discovery",
												"Device with " + ssid + " is already in List");
										continue;
									}
									// adding room here.....
									String deviceName = ssid;
									localList.add(deviceName);
									if (currentDiscoveryType == DISCOVERYTYPE_WR)
										ssid = null;
									DiscoveryRoom var7 = new DiscoveryRoom(DiscoveryActivity.this.getApplication(),
											null, deviceName, ssid);
									DiscoveryActivity.this.discoveryContent.addView(var7);
									Logger.e(DiscoveryActivity.this, "Discovery",
											"Deviece with the name " + ssid + " is added in the panel");
								}
							}
							List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
							for (WifiConfiguration wc : list) {
								String ssid = wc.SSID;
								ssid = ssid.substring(1, ssid.length() - 1);
								if (ssid.contains("ZOR")) {
									if (localList.contains(ssid)
											|| BtLocalDB.getInstance(DiscoveryActivity.this).isRoomExists(ssid)) {
										Logger.e(DiscoveryActivity.this, "Discovery",
												"Device with " + ssid + " is already in List");
										continue;
									}
									// adding room here.....
									String deviceName = ssid;
									localList.add(deviceName);
									if (currentDiscoveryType == DISCOVERYTYPE_WR)
										ssid = null;
									DiscoveryRoom var7 = new DiscoveryRoom(DiscoveryActivity.this.getApplication(),
											null, deviceName, ssid);
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
		pwd.setEnabled(getCurrentDicoveryMode() == DISCOVERYTYPE_WR);
		if (!little.isRunning())
			little.start();
		saveButton.setEnabled(false);
		((SvgView) findViewById(R.id.spinnertriangle)).setVisibility(View.GONE);
		((TextView) findViewById(R.id.controllerValue)).setText("Please Wait...");
		btdiscoveryBox.setEnabled(false);
		wifirdiscoveryBox.setEnabled(false);
		//-spb 270417 remove wifiap wifiapdiscoveryBox.setEnabled(false);
	}

	private String getDiscoveryModeStr() {
		int discoveryType = getCurrentDicoveryMode();
		String modeStr = "Bluetooth Discovery";
		if (discoveryType == DISCOVERYTYPE_WR) {
			modeStr = "Station Discovery";
		}
			//-spb 270417 remove wifiap
		/*	
		 else if (discoveryType == DISCOVERYTYPE_WAP) {
			modeStr = "Access point Discovery";
		}
		*/
		//-spb 270417 remove wifiap
		return modeStr;
	}

	private void populateCurrentSSID() {
		if (getCurrentDicoveryMode() == DISCOVERYTYPE_WR) {
			WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
			WifiInfo currentWifi = wifiManager.getConnectionInfo();
			if (currentWifi == null || currentWifi.getSSID() == null || currentWifi.getSSID().isEmpty()) {
				//-spb 010217 for error msg chg CommonUtils.AlertBox(this, "Discovery", "Wifi connection is not enabled");
				//-spb 270417 for errors CommonUtils.AlertBox(this, "Discovery", "Kindly start Wifi on phone and try again");
				CommonUtils.AlertBox(this,  CommonUtils.getInstance().getErrorString("ERROR30"), CommonUtils.getInstance().getErrorString("ERROR36"));
				return;
			}
			currentWifiSSID = currentWifi.getSSID().substring(1, currentWifi.getSSID().length() - 1);
		} else {
			currentWifiSSID = null;
		}
		System.out.println("currenct ssid..." + currentWifiSSID);
	}

	private void stopDiscoveryProcess() {
		this.runOnUiThread(new Runnable() {
			public void run() {
				((TextView) DiscoveryActivity.this.findViewById(R.id.controllerValue))
				.setText("Search Complete         ");
				((SvgView) DiscoveryActivity.this.findViewById(R.id.spinnertriangle)).setVisibility(0);
				DiscoveryActivity.this.little.stop();
				saveButton.setEnabled(true);
				btdiscoveryBox.setEnabled(true);
				wifirdiscoveryBox.setEnabled(true);
				//-spb 270417 remove wifiap  wifiapdiscoveryBox.setEnabled(true);

			}
		});
	}

	public void onActivityResult(int var1, int var2, Intent var3) {
		this.startDiscoveryProcess();
	}

	@Override
	public void onBackPressed() {
		if (isChangedToAPMode && currentWifiSSID != null) {
			TextView pwdview = (TextView) findViewById(R.id.wifiPwdText);
			CommonUtils.enableNetwork(this, currentWifiSSID, pwdview.getText().toString());
			try {
				System.err.println("Enabling network: sleeping for 5 secs for mode");
				Thread.sleep(5000);
				System.err.println("Enabled network: slept for 5 secs");
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		if( isLastDiscoveryWifi && !CommonUtils.isActiveNetwork(this)) {
			//-spb 270417 for errors CommonUtils.AlertBox(this, "Network", "Please enable Wifi network");
			CommonUtils.AlertBox(this,  CommonUtils.getInstance().getErrorString("ERROR37"),  CommonUtils.getInstance().getErrorString("ERROR38"));
			return;
		}
		Intent intent = new Intent();
		intent.putExtra("newroomname", this.roomNameAddedNewly);
		intent.putExtra("deletedrooms", this.deletedRoomList);
		this.setResult(1, intent);
		super.onBackPressed();
	}

	public ArrayList<String> performIpDiscovery() {
		ArrayList<String> ipaddressList = new ArrayList<String>();
		WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wifiManager.getConnectionInfo();
		String ssid = info.getSSID();
		if (ssid.isEmpty()) {
			return null;
		}
		ssid = ssid.substring(1, ssid.length() - 1);
		String ip = Formatter.formatIpAddress(info.getIpAddress());
		String subnet = ip.substring(0, ip.lastIndexOf("."));
		for (int i = 201; i < 255; i++) {
			try {
				InetAddress addr = InetAddress.getByName(subnet + "." + i);
				boolean isused = addr.isReachable(200);
				if (!isused) {
					System.out.println("Address.not .." + addr.getHostAddress() + " " + addr.getHostName());
					continue;
				} else {
					System.out.println("Address..." + addr.getHostAddress() + " " + addr.getHostName());
					if(btHwLayer.isZorbaDevice(addr.getHostName()))
						ipaddressList.add(addr.getHostName());
					else {
						System.out.println("Address..." + addr.getHostAddress() + " " + addr.getHostName()+" is not zorba ip device");
					}
				}
			} catch (UnknownHostException e) {
				System.out.println("Unknown host..." + e.getMessage());
			} catch (IOException e) {
				System.out.println("Unknown ..." + e.getMessage());
			}

		}
		return ipaddressList;
	}

	protected void onCreate(Bundle var1) {
		super.onCreate(var1);
		this.setContentView(R.layout.discoverylayout);
		isMaster = BtLocalDB.getInstance(this).isMasterUser();
		btHwLayer = BtHwLayer.getInstance(this);
		btHwLayer.unregister();
		btHwLayer.closeDevice();
		btdiscoveryBox = (RadioButton) findViewById(R.id.btdiscovery);
		wifirdiscoveryBox = (RadioButton) findViewById(R.id.wifirdiscovery);
		//-spb 270417 remove wifiap  wifiapdiscoveryBox = (RadioButton) findViewById(R.id.wifiapdiscovery);
		//-spb 270417 remove wifiap  wifiapdiscoveryBox.setChecked(true);
		
		TextView pwdview = (TextView) findViewById(R.id.wifiPwdText);
		pwdview.setText("");// "8GE5R3N5J4");//"owyoe82486");
		if( !isMaster) {
			pwdview.setEnabled(false);
			wifirdiscoveryBox.setChecked(true);
		}
		btdiscoveryBox.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				CheckBox firsttimecheck = (CheckBox)findViewById(R.id.isfirstdiscovery);
				firsttimecheck.setVisibility(View.GONE);
				if( btdiscoveryBox.isChecked()) {
					if (!mBTA.isEnabled()) {
						startActivityForResult(new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"), 1);
					}
					startDiscoveryProcess();
				}
			}
		});
		wifirdiscoveryBox.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				CheckBox firsttimecheck = (CheckBox)findViewById(R.id.isfirstdiscovery);
				firsttimecheck.setVisibility(View.GONE);
				if( isMaster) {
					firsttimecheck.setVisibility(View.VISIBLE);
				} else {
					populateCurrentSSID();
					startDiscoveryProcess();
				}
		}
		});
		//-spb 270417 remove wifiap 
		/*
		wifiapdiscoveryBox.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				CheckBox firsttimecheck = (CheckBox)findViewById(R.id.isfirstdiscovery);
				firsttimecheck.setVisibility(View.GONE);
				if (wifiapdiscoveryBox.isChecked()) {
					populateCurrentSSID();
					startDiscoveryProcess();
				}	
			}
		});
		*/
		//-spb 270417 remove wifiap 
					saveButton = (Button) findViewById(R.id.savebutton);
		saveButton.setEnabled(false);
		((SvgView) this.findViewById(R.id.spinnertriangle)).setOnClickListener(new ZorbaOnClickListener() {
	         public void zonClick(View var1) {
				DiscoveryActivity.this.startDiscoveryProcess();
			}
		});
		if (mBluetoothManager == null) {
			mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			if (mBluetoothManager == null) {
				//-spb 010217 for error msg chg  CommonUtils.AlertBox(this, "Bt Manager", "Unable to initialize BluetoothManager.");
				//-spb 270417 for errors CommonUtils.AlertBox(this, "Bluetooth setting", "Unable to initialize Bluetooth Manager.");
				CommonUtils.AlertBox(this,  CommonUtils.getInstance().getErrorString("ERROR39"),  CommonUtils.getInstance().getErrorString("ERROR40"));
				return;
			}
		}
		this.mBTA = mBluetoothManager.getAdapter();
		if (this.mBTA == null) {
			//-spb 270417 for errors CommonUtils.AlertBox(this, "Bt Manager", "No Bluetooth on this handset");
			CommonUtils.AlertBox(this,  CommonUtils.getInstance().getErrorString("ERROR41"),  CommonUtils.getInstance().getErrorString("ERROR42"));
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
								DiscoveryRoom var7 = new DiscoveryRoom(DiscoveryActivity.this.getApplication(),
										macaddress, var5.getName());
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
		saveButton.setOnClickListener(new ZorbaOnClickListener() {
	         public void zonClick(View var1) {

	        	 int currentDiscoveryType = getCurrentDicoveryMode();
				String pwd = "null";
				TextView pwdview = (TextView) findViewById(R.id.wifiPwdText);

				pwd = pwdview.getText().toString();
				System.out.println("Pwd........" + pwd);
				if (currentDiscoveryType == DISCOVERYTYPE_WR && pwd.isEmpty() && isStationModeMasterDiscovery()) {
					//-spb 010217 for error msg chg  CommonUtils.AlertBox(DiscoveryActivity.this, "Wifi", "Password is empty");
					//-spb 270417 for errors CommonUtils.AlertBox(DiscoveryActivity.this, "Wifi", "Kindly enter valid Wifi password for selected network");
					CommonUtils.AlertBox(DiscoveryActivity.this,  CommonUtils.getInstance().getErrorString("ERROR43"),  CommonUtils.getInstance().getErrorString("ERROR44"));
					return;
				}
				int numdiscovered = DiscoveryActivity.this.discoveryContent.getChildCount();
				ArrayList<DiscoveryRoom> nonEmptyChildren = new ArrayList<DiscoveryRoom>();
				for (int nth = 0; nth < numdiscovered; nth++) {
					DiscoveryRoom disRoom = (DiscoveryRoom) DiscoveryActivity.this.discoveryContent.getChildAt(nth);
					if (!disRoom.getRoomName().trim().isEmpty()) {
						nonEmptyChildren.add(0, disRoom);
					}
				}
				if (nonEmptyChildren.size() == 0) {
					//-spb 010217 for error msg chg   CommonUtils.AlertBox(DiscoveryActivity.this, "Discovery", "Name is empty");
					//-spb 270417 for errors CommonUtils.AlertBox(DiscoveryActivity.this, "Discovery", "Kindly enter room name");
					CommonUtils.AlertBox(DiscoveryActivity.this,  CommonUtils.getInstance().getErrorString("ERROR30"),  CommonUtils.getInstance().getErrorString("ERROR45"));
					return;
				}
				numdiscovered = nonEmptyChildren.size();
				for (int nth = 0; nth < numdiscovered; nth++) {
					DiscoveryRoom disRoom = nonEmptyChildren.get(nth);
					createRoom(disRoom, pwd);

				}
				nonEmptyChildren.clear();
				isLastDiscoveryWifi = currentDiscoveryType != DISCOVERYTYPE_BT;
			
			}
		});
		this.deleteButton = (SvgView) this.findViewById(R.id.deletebutton);
		this.roomsContent = (FlowLayout) this.findViewById(R.id.roomscontent);
		ArrayList<RoomData> roomArrayList = BtLocalDB.getInstance(this).getRoomList();
		roomArrayList.remove(0);
		for (int var2 = 0; var2 < roomArrayList.size(); ++var2) {
			this.addRoomButton(roomArrayList.get(var2));
		}
		this.deleteButton.setOnClickListener(new ZorbaOnClickListener() {
	         public void zonClick(View var1) {
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
		if( isMaster) {
			CheckBox firsttimecheck = (CheckBox)findViewById(R.id.isfirstdiscovery);
			if( wifirdiscoveryBox.isChecked())
				firsttimecheck.setVisibility(View.VISIBLE);
			else
				firsttimecheck.setVisibility(View.GONE);
			firsttimecheck.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					populateCurrentSSID();
					startDiscoveryProcess();
				}
			});
		}
		little.stop();
		//-spb 270417 for errors CommonUtils.getInstance().writeLog("Discovery started");
		CommonUtils.getInstance().writeLog( CommonUtils.getInstance().getErrorString("ERROR46"));
		Logger.e(this, "Discovery", "Discvoery started");
//		this.startDiscoveryProcess();
	}

	public void onDestroy() {
		try {
			if (wifiReciever != null)
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

	private boolean isStationModeMasterDiscovery() {
		if( getCurrentDicoveryMode() == DISCOVERYTYPE_WR) {
			CheckBox firsttimecheck = (CheckBox)findViewById(R.id.isfirstdiscovery);
			return (isMaster && firsttimecheck.isChecked());
		} else {
			return isMaster;
		}
	}
	
	private class WifiScanReceiver extends BroadcastReceiver {
		public void onReceive(Context c, Intent intent) {
			sr = wifiManager.getScanResults();
		}
	}
	
	private int getCurrentDicoveryMode() {
		int mode = DISCOVERYTYPE_BT;
		if( btdiscoveryBox.isChecked())
			mode = DISCOVERYTYPE_BT;
		else if( wifirdiscoveryBox.isChecked())
			mode = DISCOVERYTYPE_WR;
		//-spb 270417 remove wifiap 
		/*
		else if( wifiapdiscoveryBox.isChecked())
			mode = DISCOVERYTYPE_WAP;
		*/
		//-spb 270417 remove wifiap 
		return mode;
	}


}

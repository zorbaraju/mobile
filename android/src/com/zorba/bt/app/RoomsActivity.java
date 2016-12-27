package com.zorba.bt.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.zorba.bt.app.bluetooth.BtHwLayer;
import com.zorba.bt.app.bluetooth.ConnectionListener;
import com.zorba.bt.app.bluetooth.NotificationListener;
import com.zorba.bt.app.dao.DeviceData;
import com.zorba.bt.app.dao.GroupData;
import com.zorba.bt.app.dao.RoomData;
import com.zorba.bt.app.dao.SchedulerData;
import com.zorba.bt.app.db.BtLocalDB;
import com.zorba.bt.app.utils.BackgroundTaskDialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListPopupWindow;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

public class RoomsActivity  
extends ZorbaActivity implements NotificationListener, ConnectionListener {

	public static final int MENU_INDEX_DISCOVERY = 0;
	public static final int MENU_INDEX_DEVICE_CONFIG = MENU_INDEX_DISCOVERY + 1;
	public static final int MENU_INDEX_HELP = MENU_INDEX_DEVICE_CONFIG + 1;
	public static final int MENU_INDEX_ABOUT = MENU_INDEX_HELP + 1;
	public static final int MENU_INDEX_INVERTER = MENU_INDEX_ABOUT + 1;
	public static final int MENU_INDEX_CHANGEPWD = MENU_INDEX_INVERTER + 1;
	public static final int MENU_INDEX_TIMESETTINGS = MENU_INDEX_CHANGEPWD + 1;
	public static final int MENU_INDEX_SENDLOG = MENU_INDEX_TIMESETTINGS + 1;
	public static final int MENU_INDEX_SWITCHMODELOG = MENU_INDEX_SENDLOG + 1;
	public static final int MENU_INDEX_MTLOG = MENU_INDEX_SWITCHMODELOG + 1;
	public static final int MENU_INDEX_EXIT = MENU_INDEX_MTLOG + 1;
	
	
	
	public static final int DISCOVERY_CODE = 1;
	public static final int ENABLEBT_CODE = DISCOVERY_CODE + 1;
	public static final int ENABLEWIFI_CODE = ENABLEBT_CODE + 1;
	public static final int ADDDEVICE_CODE = ENABLEWIFI_CODE + 1;
	public static final int ADDGROUP_CODE = ADDDEVICE_CODE + 1;
	public static final int ADDSCHEDULER_CODE = ADDGROUP_CODE + 1;
	public static final int APPINFO_CODE = ADDSCHEDULER_CODE + 1;
	public static final int SENDLOG_CODE = APPINFO_CODE + 1;
	public static final int HELP_CODE = SENDLOG_CODE + 1;
	public static final int INVERTER_CODE = HELP_CODE + 1;
	public static final int CHANGEPWD_CODE = INVERTER_CODE + 1;
	
	public static final int RUSULTCODE_CANCEL = 0;
	public static final int RUSULTCODE_SAVE = 1;
	
	int _color = 0;
	long backPressedTime = 0L;
	MyComp devicePanel = null;
	MyComp groupPanel = null;
	HashMap<String, Boolean> groupStatusMap = new HashMap<String, Boolean>();
	MyComp lightsPanel = null;
	ArrayList<RoomData> roomDataList = null;
	ListPopupWindow roomMenuList = null;
	MyComp schedulePanel = null;
	String selectedDeviceName = null;
	ImageTextButton selectedGroupButton = null;
	String selectedGroupName = null;
	RoomData selectedRoom = null;
	ImageTextButton selectedScheduleButton = null;
	String selectedScheduleName = null;
	int selectedSchedulerId = 0;

	LinearLayout roomContent = null;
	RGBController rgbController = null;
	BtHwLayer btHwLayer = null;
	private boolean isInitial = true;
	private boolean isInConfigDeviceMode = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			setContentView(R.layout.rooms);
			BtLocalDB.getInstance(this).cleanDB();
			rgbController = new RGBController(this);
			final ListPopupWindow homeMenu = prepareHomeMenu();
			SvgView homeButton = (SvgView) findViewById(R.id.homeButton);
			homeButton.setOnClickListener(new ZorbaOnClickListener() {
		         public void zonClick(View v) {
					homeMenu.show();
				}
			});
			this.roomMenuList = new ListPopupWindow(this);
			TextView roomText = (TextView) findViewById(R.id.roomList);
			roomText.setOnClickListener(new ZorbaOnClickListener() {
		         public void zonClick(View paramAnonymousView) {
					RoomsActivity.this.roomMenuList.show();
				}
			});
			/*
			 * ((ImageButton)
			 * findViewById(R.id.aboutButton)).setOnClickListener(new
			 * View.OnClickListener() { public void onClick(View
			 * paramAnonymousView1) { Intent paramAnonymousView = new
			 * Intent(MainActivity.this, AppInfoActivity.class);
			 * MainActivity.this.startActivityForResult(paramAnonymousView,
			 * APPINFO_CODE); } });
			 */
			SvgView db = (SvgView) findViewById(R.id.discoverbutton);
			if (db != null) {
				db.setOnClickListener(new View.OnClickListener() {
					public void onClick(View paramAnonymousView1) {
						btHwLayer.unregister();
						Intent paramAnonymousView = new Intent(RoomsActivity.this, DiscoveryActivity.class);
						RoomsActivity.this.startActivityForResult(paramAnonymousView, DISCOVERY_CODE);
					}
				});
			}
			btHwLayer = BtHwLayer.getInstance(this);
			prepareRoomListMenu("", false);
			if (this.roomDataList.size() == 0) {
				startActivityForResult(new Intent(this, DiscoveryActivity.class), DISCOVERY_CODE);
			}
			setConnectionModeIcon(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onBackPressed() {
		if( isInConfigDeviceMode) {
			enableMainActivityOnDeviceConfig(false);
			return;
		}
		CommonUtils.getInstance().deleteLog();
		new AlertDialog.Builder(this).setTitle("Exit").setMessage("Do you really want to exit ?")
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt) {
						RoomsActivity.this.performExit();
					}
				}).setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt) {
						paramAnonymousDialogInterface.cancel();
					}
				}).show();
	}

	private void performExit() {
		btHwLayer.unregister();
		btHwLayer.closeDevice();
		Logger.e(this, "main", "App is closed.....");
		finish();
		Process.killProcess(Process.myPid());
		super.onDestroy();
		System.exit(1);
	}

	private ListPopupWindow prepareHomeMenu() {
		final ListPopupWindow popupWindow = new ListPopupWindow(this);
		ArrayList<ImageTextData> arrayList = new ArrayList<ImageTextData>();
		arrayList.add(new ImageTextData("Add Room", R.raw.discovery));
		arrayList.add(new ImageTextData("Device Configuration", R.raw.deviceconfig));
		arrayList.add(new ImageTextData("Help", R.raw.help));
		arrayList.add(new ImageTextData("About", R.raw.about));
		arrayList.add(new ImageTextData("Inverter Power", R.raw.inverter));
		arrayList.add(new ImageTextData("Change Pwd", R.raw.changepassword));
		arrayList.add(new ImageTextData("Time Settings", R.raw.timesettings));
		arrayList.add(new ImageTextData("Send Log", R.raw.sendlog));
		arrayList.add(new ImageTextData("Go to Ap mode", R.raw.sendemail));
		arrayList.add(new ImageTextData("Mt Log", R.raw.sendemail));
		arrayList.add(new ImageTextData("Exit", R.raw.exit));
		ImageTextAdapter textAdapter = new ImageTextAdapter(this, arrayList, new ZorbaOnClickListener() {
	         public void zonClick(View popupView) {
				popupWindow.dismiss();
				int i = ((Integer) popupView.getTag()).intValue();
				if (i == MENU_INDEX_DISCOVERY) {
					Intent intent = new Intent(RoomsActivity.this, DiscoveryActivity.class);
					RoomsActivity.this.startActivityForResult(intent, DISCOVERY_CODE);
				} else if (i == MENU_INDEX_DEVICE_CONFIG) {
					System.out.println("Device config menu is clicked");
					enableMainActivityOnDeviceConfig(true);
				} else if (i == MENU_INDEX_HELP) {
					Intent intent = new Intent(RoomsActivity.this, HelpActivity.class);
					RoomsActivity.this.startActivityForResult(intent, HELP_CODE);
				} else if (i == MENU_INDEX_ABOUT) {
					Intent intent = new Intent(RoomsActivity.this, AppInfoActivity.class);
					RoomsActivity.this.startActivityForResult(intent, APPINFO_CODE);
				} else if (i == MENU_INDEX_SENDLOG) {
					Intent intent = new Intent(RoomsActivity.this, SendLogActivity.class);
					RoomsActivity.this.startActivityForResult(intent, SENDLOG_CODE);
				} else if (i == MENU_INDEX_INVERTER) {
					Intent intent = new Intent(RoomsActivity.this, InverterActivity.class);
					RoomsActivity.this.startActivityForResult(intent, INVERTER_CODE);
				} else if (i == MENU_INDEX_CHANGEPWD) {
					Intent intent = new Intent(RoomsActivity.this, ChangepwdActivity.class);
					RoomsActivity.this.startActivityForResult(intent, CHANGEPWD_CODE);
				} else if (i == MENU_INDEX_TIMESETTINGS) {
					Intent intent = new Intent(RoomsActivity.this, TimeSettingsActivity.class);
					RoomsActivity.this.startActivityForResult(intent, CHANGEPWD_CODE);
				} else if (i == MENU_INDEX_SWITCHMODELOG) {
						try {
							btHwLayer.setWifiAPMode(true);
						} catch (Exception e) {
							e.printStackTrace();
							CommonUtils.AlertBox(RoomsActivity.this, "Mode change", "Error:"+e.getMessage());
						}
				} else if (i == MENU_INDEX_MTLOG) {
					Intent intent = new Intent(RoomsActivity.this, AwsIotActivity.class);
					String macaddress = "zorbadummy";
					if( selectedRoom != null && selectedRoom.getAddress() != null) {
						macaddress = selectedRoom.getAddress();
					}
					intent.putExtra("deviceName", macaddress);
					RoomsActivity.this.startActivityForResult(intent, CHANGEPWD_CODE);
				} else if (i == MENU_INDEX_EXIT) {
					RoomsActivity.this.performExit();
				}
			}
		});
		popupWindow.setHorizontalOffset( -200 );
		popupWindow.setVerticalOffset( -100 );
		popupWindow.setAdapter((ListAdapter) textAdapter);
		popupWindow.setAnchorView(findViewById(R.id.homeButton));
		popupWindow
				.setWidth(CommonUtils.measureContentWidth(popupWindow.getListView(), (ListAdapter) textAdapter) + 60);
		popupWindow.setHeight(popupWindow.MATCH_PARENT);
		return popupWindow;
	}

	private void prepareRoomListMenu(String newRoomName, boolean paramBoolean) {
		final TextView roomListText = (TextView) findViewById(R.id.roomList);
		OnClickListener listener = new View.OnClickListener() {
			public void onClick(View view) {
				RoomsActivity.this.roomMenuList.dismiss();
				RoomsActivity.this.roomChanged(false, roomListText, ((Integer) view.getTag()).intValue(), true);
			}
		};
		this.roomDataList = BtLocalDB.getInstance(this).getRoomList();
		for(RoomData rd: roomDataList){
			System.out.println("Rodetailss..."+rd.getAddress()+":"+rd.getDeviceName()+":"+rd.getIpAddress()+":"+rd.getName()+":"+rd.getSSID());
		}
		TextAdapter localTextAdapter = new TextAdapter(this, this.roomDataList, listener);
		this.roomMenuList.setAdapter(localTextAdapter);
		this.roomMenuList.setAnchorView(roomListText);
		try {
			int width = CommonUtils.measureContentWidth(this.roomMenuList.getListView(), localTextAdapter) + 20;
			this.roomMenuList.setWidth(width);
			roomListText.setWidth(width);
			this.roomDataList.remove(0);
			localTextAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			this.roomMenuList.setAdapter(localTextAdapter);
			if (!paramBoolean) {
				int selectedRoomIndex = BtLocalDB.getInstance(this).getLastSelectedRoom();
				int selectIndex = selectedRoomIndex;
				if (!newRoomName.isEmpty()) {
					selectIndex = 0;
					if (selectIndex >= this.roomDataList.size()) {
						selectIndex = selectedRoomIndex;
					}
				} else {
					if (this.roomDataList.size() <= 0) {
						return;
					}
				}
				roomChanged(false, roomListText, selectIndex, true);
			} else {
				if (this.roomDataList.size() == 0) {
					((ScrollView) findViewById(R.id.scrollView)).setVisibility(View.GONE);
					((LinearLayout) findViewById(R.id.rgbPanel)).setVisibility(View.GONE);
					((LinearLayout) findViewById(R.id.emptydevicepanel)).setVisibility(View.VISIBLE);
					roomListText.setText("No rooms");
				}
				return;
			}
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	private MyComp populateDeviceButtons(final String tabName) {
		MyComp local16 = new MyComp(getApplicationContext(), tabName, -1, isInConfigDeviceMode ) {
			public void doAddAction() {
				Intent localIntent = new Intent(getBaseContext(), AddDeviceActivity.class);
				localIntent.putExtra("deviceName", selectedRoom.getDeviceName());
				localIntent.putExtra("tabName", tabName);
				startActivityForResult(localIntent, ADDDEVICE_CODE);
			}
			
			public void doEditAction() {
				Intent localIntent = new Intent(getBaseContext(), AddDeviceActivity.class);
				localIntent.putExtra("deviceName", RoomsActivity.this.selectedRoom.getDeviceName());
				localIntent.putExtra("tabName", tabName);
				localIntent.putExtra("entityName", RoomsActivity.this.selectedDeviceName);
				RoomsActivity.this.startActivityForResult(localIntent, ADDDEVICE_CODE);
			}
			
			public void doDeleteAction() {
				new AlertDialog.Builder(RoomsActivity.this).setTitle("Delete")
						.setMessage("Do you really want to delete ?")
						.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface paramAnonymous2DialogInterface,
									int paramAnonymous2Int) {
								BtLocalDB.getInstance(RoomsActivity.this).deleteDevice(RoomsActivity.this.selectedRoom.getDeviceName(),
										RoomsActivity.this.selectedDeviceName);
								RoomsActivity.this.lightsPanel.removeMyView(RoomsActivity.this.selectedDeviceName);
								RoomsActivity.this.devicePanel.removeMyView(RoomsActivity.this.selectedDeviceName);
								showDeleteButton(false);
								updateDeviceCount();
							}
						}).setNegativeButton("No", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface paramAnonymous2DialogInterface,
									int paramAnonymous2Int) {
								paramAnonymous2DialogInterface.cancel();
							}
						}).show();
			}
		};
		((LinearLayout) findViewById(R.id.roomContent)).addView(local16);
		ArrayList<DeviceData> deviceList = BtLocalDB.getInstance(this).getDevices(this.selectedRoom.getDeviceName(), null);
		local16.expandComp(true);
		if (tabName.equals("Lights"))
			this.lightsPanel = local16;
		else
			this.devicePanel = local16;
		int numdevices =deviceList.size();
		for (int ddindex=0; ddindex<numdevices; ddindex++) {
			DeviceData device = deviceList.get(ddindex);
			if (!device.isUnknownType()) {
				if ((tabName.equals("Lights")) && (DeviceData.isLightType(device.getType()))) {
					addButtonPanel(local16, device, true);
				} else if ((tabName.equals("Devices"))
						&& (!DeviceData.isLightType(device.getType()))) {
					addButtonPanel(local16, device, true);
				}
			}
		}
		return local16;
	}

	private MyComp populateGroups() {
		this.groupPanel = new MyComp(getApplicationContext(), "Groups", 8, isInConfigDeviceMode) {
			public void doAddAction() {
				Intent localIntent = new Intent(RoomsActivity.this.getBaseContext(), AddGroupActivity.class);
				localIntent.putExtra("deviceName", RoomsActivity.this.selectedRoom.getDeviceName());
				RoomsActivity.this.startActivityForResult(localIntent, ADDGROUP_CODE);
			}
			
			public void doEditAction() {
				Intent localIntent = new Intent(RoomsActivity.this.getBaseContext(), AddGroupActivity.class);
				localIntent.putExtra("deviceName", RoomsActivity.this.selectedRoom.getDeviceName());
				localIntent.putExtra("entityName", RoomsActivity.this.selectedGroupName);
				RoomsActivity.this.startActivityForResult(localIntent, ADDGROUP_CODE);
			}

			public void doDeleteAction() {
				new AlertDialog.Builder(RoomsActivity.this).setTitle("Delete")
						.setMessage("Do you really want to delete ?")
						.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface paramAnonymous2DialogInterface,
									int paramAnonymous2Int) {
								BtLocalDB.getInstance(RoomsActivity.this).deleteGroup(
										RoomsActivity.this.selectedRoom.getDeviceName(),
										RoomsActivity.this.selectedGroupName);
								RoomsActivity.this.groupPanel.removeMyView(RoomsActivity.this.selectedGroupName);
								showDeleteButton(false);
							}
						}).setNegativeButton("No", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface paramAnonymous2DialogInterface,
									int paramAnonymous2Int) {
								paramAnonymous2DialogInterface.cancel();
							}
						}).show();
			}
		};
		((LinearLayout) findViewById(R.id.roomContent)).addView(this.groupPanel);
		List<GroupData>groupList = BtLocalDB.getInstance(this).getGroups(this.selectedRoom.getDeviceName(),null);
		for (int gindex=0; gindex<groupList.size(); gindex++) {
			addGroupButton(groupList.get(gindex).getName(), true);
		}
		return this.groupPanel;
	}

	private MyComp populateSchedules() {
		this.schedulePanel = new MyComp(getApplicationContext(), "Schedules", 8, isInConfigDeviceMode) {
			public void doAddAction() {
				Intent localIntent = new Intent(RoomsActivity.this.getBaseContext(), AddSchedulerActivity.class);
				localIntent.putExtra("deviceName", RoomsActivity.this.selectedRoom.getDeviceName());
				RoomsActivity.this.startActivityForResult(localIntent, ADDSCHEDULER_CODE);
			}

			public void doEditAction() {
				Intent localIntent = new Intent(RoomsActivity.this.getBaseContext(), AddSchedulerActivity.class);
				localIntent.putExtra("deviceName", RoomsActivity.this.selectedRoom.getDeviceName());
				localIntent.putExtra("entityName", RoomsActivity.this.selectedScheduleName);
				RoomsActivity.this.startActivityForResult(localIntent, ADDSCHEDULER_CODE);
			}
			
			public void doDeleteAction() {
				new AlertDialog.Builder(RoomsActivity.this).setTitle("Delete")
						.setMessage("Do you really want to delete ?")
						.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface paramAnonymous2DialogInterface,
									int paramAnonymous2Int) {
								try {
									//btHwLayer.sendDeleteAlarmCommandToDevice(MainActivity.this.selectedSchedulerId);
									BtLocalDB.getInstance(RoomsActivity.this).deleteSchedule(
											RoomsActivity.this.selectedRoom.getDeviceName(),
											RoomsActivity.this.selectedSchedulerId);
									RoomsActivity.this.schedulePanel
											.removeMyView(RoomsActivity.this.selectedScheduleName);
									showDeleteButton(false);
									return;
								} catch (Exception paramAnonymous2DialogInterfacee) {
									CommonUtils.AlertBox(RoomsActivity.this, "Delete Scheduler",
											"Not able to send delete schedule(" + RoomsActivity.this.selectedScheduleName
													+ "): " + paramAnonymous2DialogInterfacee.getMessage());
								}
							}
						}).setNegativeButton("No", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface paramAnonymous2DialogInterface,
									int paramAnonymous2Int) {
								paramAnonymous2DialogInterface.cancel();
							}
						}).show();
			}
		};
		((LinearLayout) findViewById(R.id.roomContent)).addView(this.schedulePanel);
		ArrayList<SchedulerData> scheduleList = BtLocalDB.getInstance(this)
				.getSchedules(this.selectedRoom.getDeviceName(), null);

		for (int i = 0; i < scheduleList.size(); i++) {
			SchedulerData localSchedulerData = scheduleList.get(i);
			addScheduleButton(Integer.parseInt(localSchedulerData.getSchedulerId()), localSchedulerData.getName(), true);
		}
		return this.schedulePanel;
	}
	
	

	private void addButtonPanel(final MyComp paramMyComp, DeviceData paramDeviceData, boolean isnew) {
		if( !isnew )
			return;
		final int devid = paramDeviceData.getDevId();
		final String devtype = paramDeviceData.getType();
		final String str2 = paramDeviceData.getName();
		paramDeviceData.setStatus(BtLocalDB.getInstance(this).getDeviceStatus((byte)devid));
		final ImageTextButton deviceButton = new ImageTextButton(this);
		deviceButton.setDevice(paramDeviceData);
		deviceButton.setOnLongClickListener(new View.OnLongClickListener() {
			public boolean onLongClick(View paramAnonymousView) {
				paramMyComp.showDeleteButton(true);
				RoomsActivity.this.selectedDeviceName = str2;
				paramMyComp.selectComp(deviceButton);
				if( !isInConfigDeviceMode) {
					if (DeviceData.isDimmable(devtype)) {
						RoomsActivity.this.controlDevice(deviceButton, devtype, devid);
					}
				}
				return true;
			}
		});
		deviceButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View paramAnonymousView) {
				paramMyComp.deselectAll();
				RoomsActivity.this.singleClickButton(devid, devtype, deviceButton);
			}
		});
		paramMyComp.addMyView(deviceButton);
	}

	private void readAndUpateStatusForRoom(boolean update) {
		System.out.println("isUpdate.inside..readAndUpateStatusForRoom.is false..." + CommonUtils.getMaxNoDevices());
		if (update) {
			try {
				byte allStatus[] = btHwLayer.readAllStatus();
				if (allStatus == null) {
					CommonUtils.AlertBox(this, "Device count", "No data from device");
					return;
				}
				for (byte ind = 1; ind < allStatus.length; ind++) {
					try {
						byte deviceid = (byte) ind;
						byte status = allStatus[ind];
						BtLocalDB.getInstance(this).updateDeviceStatus(deviceid, status);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		}

		runOnUiThread(new Runnable() {
			public void run() {
				if (RoomsActivity.this.lightsPanel == null) {
					System.out.println("Notification is received.....i dont know why its coming here....");
					return;
				}
				RoomsActivity.this.lightsPanel.updateButtonInPanel();
				RoomsActivity.this.devicePanel.updateButtonInPanel();
				//updateDeviceCount();
				try {
					if (RoomsActivity.this.groupPanel.isReset()) {
						RoomsActivity.this.groupPanel.updateLiveButtonInPanel();
					}
					if (!btHwLayer.isConnected())
						groupPanel.resetButtonInPanel(true);
					return;
				} catch (Exception localException) {
					CommonUtils.AlertBox(RoomsActivity.this, "Read Power", "No data from device");
				}
			}
		});
	}

	protected void updateDeviceCount() {
		int i = BtLocalDB.getInstance(RoomsActivity.this).getDevicesOnCount();
		((TextView) findViewById(R.id.onDeviceCount)).setText("" + i);
	}

	private void singleClickButton(final int devid, final String devtype, ImageTextButton paramImageTextButton) {
		if( isInConfigDeviceMode) {
			return;
		}
		if( !btHwLayer.isConnected())
			updateWithRealtime();
		else {
			BackgroundTaskDialog btdialog = new BackgroundTaskDialog(this) {

				@Override
				public Object runTask(Object params) {
					try {
						
						int readValue = btHwLayer.readCommandToDevice(devid);
						if (readValue != 0)
							readValue = 0;
						else {
							readValue = 9;
							if (DeviceData.isDimmable(devtype)) {
								readValue = BtLocalDB.getInstance(RoomsActivity.this).getDevicePrevOnStatus((byte) devid);
								if( readValue == -1)
									readValue = 9;
								System.out.println("Prev status fro  db..."+readValue);
							}
						}
						btHwLayer.sendCommandToDevice(devid, readValue);
						BtLocalDB.getInstance(RoomsActivity.this).updateDeviceStatus((byte) devid, (byte) readValue);
						readAndUpateStatusForRoom(false);
						//testExtras();
						
						return null;
					} catch (Exception paramString1) {
						paramString1.printStackTrace();
						CommonUtils.AlertBox(RoomsActivity.this, "Read Error", paramString1.getMessage());
					}
					return null;
				}

				@Override
				public void finishedTask(Object result) {
					// TODO Auto-generated method stub

				}
			};
		}
		

	}

	private void addGroupButton(final String groupName, boolean isnew) {
		if( !isnew )
			return;
		final ImageTextButton localImageTextButton = new ImageTextButton(this);
		ArrayList<GroupData> grpArr = BtLocalDB.getInstance(this).getGroups(RoomsActivity.this.selectedRoom.getDeviceName(), groupName);
  	  	if( grpArr.size() == 0)
  	  		return;
  	  	final GroupData groupData = grpArr.get(0);
		localImageTextButton.setText(groupName);
		localImageTextButton.changeDeviceButtonStyle(0);
		localImageTextButton.setBackgroundImage(groupData.getImageResId());
		localImageTextButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View paramAnonymousView) {
				RoomsActivity.this.groupPanel.deselectAll();
				int groudIds[] = BtLocalDB.getInstance(RoomsActivity.this)
						.getGroupDevices(RoomsActivity.this.selectedRoom.getDeviceName(), groupName);
				boolean groupClicked = RoomsActivity.this.groupStatusMap.containsKey(groupName);
				if (groupClicked) {
					for (int dindex = 0; dindex < groudIds.length; dindex += 2) {
						groudIds[dindex + 1] = 0;
					}
				}
				try {
					btHwLayer.sendCommandToDevices(groudIds);
					if (!groupClicked) {
						RoomsActivity.this.groupStatusMap.put(groupName, Boolean.valueOf(true));
						localImageTextButton.changeDeviceButtonStyle(1);
						localImageTextButton.setBackgroundImage(groupData.getImageResId());
					} else {
						RoomsActivity.this.groupStatusMap.remove(groupName);
						localImageTextButton.changeDeviceButtonStyle(0);
						localImageTextButton.setBackgroundImage(groupData.getImageResId());
					}
					for (int dindex = 0; dindex < groudIds.length; dindex += 2) {
						BtLocalDB.getInstance(RoomsActivity.this).updateDeviceStatus((byte) groudIds[dindex],
								(byte) groudIds[dindex + 1]);
					}
					RoomsActivity.this.readAndUpateStatusForRoom(false);
				} catch (Exception e) {
					RoomsActivity.this.groupStatusMap.remove(groupName);
					localImageTextButton.changeDeviceButtonStyle(-1);
					localImageTextButton.setBackgroundImage(groupData.getImageResId());
					CommonUtils.AlertBox(RoomsActivity.this, "Read Error", e.getMessage());
				}
			}
		});
		localImageTextButton.setOnLongClickListener(new View.OnLongClickListener() {
			public boolean onLongClick(View paramAnonymousView) {
				RoomsActivity.this.groupPanel.showDeleteButton(true);
				RoomsActivity.this.selectedGroupName = groupName;
				RoomsActivity.this.selectedGroupButton = localImageTextButton;
				RoomsActivity.this.groupPanel.selectComp(localImageTextButton);
				return true;
			}
		});
		this.groupPanel.addMyView(localImageTextButton);
	}

	private void addScheduleButton(final int paramInt, final String scheduleName, boolean isnew) {
		if( !isnew )
			return;
		final ImageTextButton localImageTextButton = new ImageTextButton(this);
		ArrayList<SchedulerData> schArr = BtLocalDB.getInstance(this).getSchedules(RoomsActivity.this.selectedRoom.getDeviceName(), scheduleName);
  	  	if( schArr.size() == 0)
  	  		return;
  	  SchedulerData scheduleData = schArr.get(0);
		localImageTextButton.setBackgroundImage(scheduleData.getImageResId());
		localImageTextButton.setText(scheduleName);
		localImageTextButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View paramAnonymousView) {
				RoomsActivity.this.schedulePanel.deselectAll();
			}
		});
		localImageTextButton.setOnLongClickListener(new View.OnLongClickListener() {
			public boolean onLongClick(View paramAnonymousView) {
				RoomsActivity.this.schedulePanel.showDeleteButton(true);
				RoomsActivity.this.selectedSchedulerId = paramInt;
				RoomsActivity.this.selectedScheduleName = scheduleName;
				RoomsActivity.this.selectedScheduleButton = localImageTextButton;
				RoomsActivity.this.schedulePanel.selectComp(localImageTextButton);
				return true;
			}
		});
		this.schedulePanel.addMyView(localImageTextButton);
	}	

	private void controlDevice(final ImageTextButton paramImageTextButton, final String devtype,
			final int devid) {
		Object localObject = LayoutInflater.from(this).inflate(R.layout.devicecontroller, null);
		AlertDialog.Builder localBuilder = new AlertDialog.Builder(this);
		localBuilder.setView((View) localObject);
		localObject = (SeekBar) ((View) localObject).findViewById(R.id.seekBar1);
		try {
			int i = btHwLayer.readCommandToDevice(devid);
			if (i != -1) {
				((SeekBar) localObject).setProgress(i * 10);
			}
			((SeekBar) localObject).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				int prevvalue = -1;
				public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
					if( fromUser ) {
						
						try {
							progresValue = seekBar.getProgress() / 10;
							if (progresValue == 10) {
								progresValue = 9;
							}
							if( prevvalue != progresValue) {
								btHwLayer.sendCommandToDevice(devid, progresValue);
								paramImageTextButton.changeDeviceButtonStyle(devtype, progresValue);
								if( progresValue != 0) {
									BtLocalDB.getInstance(RoomsActivity.this).updateDevicePrevOnStatus((byte)devid, (byte)progresValue);
									System.err.println("Last on status..."+progresValue);
								}
								BtLocalDB.getInstance(RoomsActivity.this).updateDeviceStatus((byte) devid, (byte) progresValue);
							}
							prevvalue = progresValue;
							return;
						} catch (Exception paramAnonymousSeekBare) {
							CommonUtils.AlertBox(RoomsActivity.this, "Error",
									"Sending cmd to Device:" + paramAnonymousSeekBare.getMessage());
						}
						
					}
				}

				public void onStartTrackingTouch(SeekBar paramAnonymousSeekBar) {
				}

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
					
				}
			});
			localBuilder.setCancelable(false).setNegativeButton("Close", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt) {
					paramAnonymousDialogInterface.cancel();
				}
			});
			localBuilder.create().show();
			return;
		} catch (Exception paramImageTextButtone) {
			CommonUtils.AlertBox(this, "Read Error", "Error in reading command:" + paramImageTextButtone.getMessage());
		}
	}

	private void populatePageForSelectedRoom() {
		if (!btHwLayer.isWifiEnabled() && !btHwLayer.makeBtEnabled()) {
			System.out.println("pbt is not enableds");
			return;
		}
		((LinearLayout) findViewById(R.id.roomContent)).removeAllViews();
		lightsPanel = populateDeviceButtons("Lights");
		devicePanel = populateDeviceButtons("Devices");
		groupPanel = populateGroups();
		schedulePanel = populateSchedules();
		
		this.lightsPanel.setSiblings(new MyComp[] { this.devicePanel, this.groupPanel, this.schedulePanel });
		this.devicePanel.setSiblings(new MyComp[] { this.lightsPanel, this.groupPanel, this.schedulePanel });
		this.groupPanel.setSiblings(new MyComp[] { this.lightsPanel, this.devicePanel, this.schedulePanel });
		this.schedulePanel.setSiblings(new MyComp[] { this.lightsPanel, this.devicePanel, this.groupPanel });
		//isInitial = false;
		System.err.println("IsInitial....."+isInitial);
		if( isInitial  ) {
			 TextView roomListText = (TextView) findViewById(R.id.roomList);
			 roomListText.post(new Runnable() {
				public void run() {
					Dialog dialog = onCreateDialog();
					dialog.show();
					//roomMenuList.show();
				}
			});
		} else {
			updateWithRealtime();
			relayout();
		}
		enableMainActivityOnDeviceConfig(isInConfigDeviceMode);
	}

	private void updateWithRealtime() {
		BackgroundTaskDialog task = new BackgroundTaskDialog(this) {

			@Override
			public Object runTask(Object params) {
				boolean isUpdate = false;
				btHwLayer.setConnectionListener(RoomsActivity.this);
				if( selectedRoom == null)
					return null;
				String incomingssid = selectedRoom.getSSID();
				String ipaddress = selectedRoom.getIpAddress();
				String macaddress = selectedRoom.getAddress();
				if( btHwLayer.isWifiEnabled()) {
					if( incomingssid != null && !incomingssid.isEmpty() && !incomingssid.equals("null")) {
						macaddress = null;
						ipaddress = CommonUtils.enableNetwork(RoomsActivity.this, incomingssid, incomingssid);
						if( ipaddress == null) {
							CommonUtils.AlertBox(RoomsActivity.this, "Connection", "Ipaddress is found for ssid:"+incomingssid);
							return null;
						}
					} else {
						incomingssid = null;
					}
				} else {
					incomingssid = null; ipaddress = null;
				}
				
				btHwLayer.closeDevice();
				String error = btHwLayer.initDevice(selectedRoom.getName(), macaddress, incomingssid, ipaddress,
						false);
				if (error == null) {
					try {
						int numberOfDevices = btHwLayer.getNumberOfDevices();
						CommonUtils.setMaxNoDevices(numberOfDevices);
						//btHwLayer.setDateAndTime();
						isUpdate = true;
					} catch (Exception e) {
						isUpdate = false;
						CommonUtils.AlertBox(RoomsActivity.this, "Connection", e.getMessage());
					}
				} else {
					CommonUtils.AlertBox(RoomsActivity.this, "Connection", error);
					connectionLost();
					return null;
				}
				readAndUpateStatusForRoom(isUpdate);
				return null;
			}

			@Override
			public void cancelTask() {
				btHwLayer.closeDevice();
			}
			@Override
			public void finishedTask(Object result) {
				// TODO Auto-generated method stub

			}
		};
	}

	private void roomChanged(boolean isInitial, TextView paramTextView, int paramInt, boolean isFromSelection) {
		if (!isInitial && (this.selectedRoom != null)
				&& (this.selectedRoom.getName().equals(((RoomData) this.roomDataList.get(paramInt)).getName()))) {
			if( isFromSelection)
				return;
		}
		if( roomDataList.size() == 0)
			return;
		this.selectedRoom = ((RoomData) this.roomDataList.get(paramInt));
		System.out.println("Rodetailss.selectedRoom.."+selectedRoom.getAddress()+":"+selectedRoom.getDeviceName()+":"+selectedRoom.getIpAddress()+":"+selectedRoom.getName()+":"+selectedRoom.getSSID());

		paramTextView.setText(this.selectedRoom.getName());
		BtLocalDB.getInstance(this).setLastSelectedRoom(paramInt);
		BtLocalDB.getInstance(this).clearDeviceStatus();
		if (selectedRoom.isRGBType()) {
			rgbController.setRGBView(selectedRoom);
		} else {
			((ScrollView) findViewById(R.id.scrollView)).setVisibility(View.VISIBLE);
			((LinearLayout) findViewById(R.id.rgbPanel)).setVisibility(View.GONE);
			((LinearLayout) findViewById(R.id.emptydevicepanel)).setVisibility(View.GONE);
			populatePageForSelectedRoom();
		}
	}

	public void putInOffline() {
		runOnUiThread(new Runnable() {
			public void run() {
				if (RoomsActivity.this.lightsPanel == null) {
					// rgbController.putOffLine(true);
				} else {
					RoomsActivity.this.lightsPanel.resetButtonInPanel(false);
					RoomsActivity.this.devicePanel.resetButtonInPanel(false);
					RoomsActivity.this.groupPanel.resetButtonInPanel(true);
				}
			}
		});
	}

	public void relayout() {
		((SvgView) findViewById(R.id.overlay)).post(new Runnable() {
			public void run() {
				RoomsActivity.this.schedulePanel.relayout();
				RoomsActivity.this.groupPanel.relayout();
				RoomsActivity.this.devicePanel.relayout();
				RoomsActivity.this.lightsPanel.relayout();
			}
		});
	}

	private void fromDiscoveryActivity(Intent resultIntent) {
		String newRoom;
		try {
			newRoom = resultIntent.getExtras().getString("newroomname");
			if (!newRoom.isEmpty()) {
				prepareRoomListMenu(newRoom, false);
				return;
			}
			String deletedRoomStr = resultIntent.getExtras().getString("deletedrooms");
			if (!deletedRoomStr.isEmpty()) {
				String deletedRooms[] = deletedRoomStr.split("#");
				int numberOfRoomsDeleted = deletedRooms.length;
				if (numberOfRoomsDeleted > 0) {
					prepareRoomListMenu("", true);
					return;
				}
			} else {
				final TextView roomListText = (TextView) findViewById(R.id.roomList);
				int selectedRoomIndex = BtLocalDB.getInstance(this).getLastSelectedRoom();
				roomChanged(false, roomListText, selectedRoomIndex, false);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return;
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
		Logger.e(this, "onActivityResult", "requestcode=" + requestCode + " resultCode=" + resultCode);
		if (requestCode == DISCOVERY_CODE) {
			btHwLayer.register();
			fromDiscoveryActivity(resultIntent);
		} else if (requestCode == ENABLEBT_CODE) {
			populatePageForSelectedRoom();
		} else if ((requestCode == ADDDEVICE_CODE) && (resultCode == RUSULTCODE_SAVE)) {
			String name = resultIntent.getExtras().getString("name");
			String type = resultIntent.getExtras().getString("type");
			DeviceData deviceData = new DeviceData(resultIntent.getExtras().getInt("index"), name, type,
					resultIntent.getExtras().getString("power"), -1);
			if (DeviceData.isLightType(type)) {
				addButtonPanel(this.lightsPanel, deviceData, resultIntent.getExtras().getBoolean("isnew"));
			} else {
				addButtonPanel(this.devicePanel, deviceData, resultIntent.getExtras().getBoolean("isnew"));
			}
			updateDeviceCount();
			return;
		} else if ((requestCode == ADDGROUP_CODE) && (resultCode == RUSULTCODE_SAVE)) {
			addGroupButton(resultIntent.getExtras().getString("name"), resultIntent.getExtras().getBoolean("isnew"));
			return;
		} else if ((requestCode == ADDSCHEDULER_CODE) && (resultCode == RUSULTCODE_SAVE)) {
			requestCode = resultIntent.getExtras().getInt("scheduleid");
			String name = resultIntent.getExtras().getString("name");
			if (resultIntent.getExtras().containsKey("error")) {
				CommonUtils.AlertBox(this, "Scheduler:" + name, resultIntent.getExtras().getString("error"));
				return;
			}
			addScheduleButton(requestCode, name, resultIntent.getExtras().getBoolean("isnew"));
			updateDeviceCount();
		}
	}

	public Dialog onCreateDialog() {
		CharSequence []menunames = new CharSequence[roomDataList.size()];
		int index=0;
		for(RoomData room:roomDataList) {
			menunames[index++] = room.getName();
		}
		   AlertDialog.Builder builder = new AlertDialog.Builder(this);
		   builder.setTitle("Select a room")
		   .setItems(menunames, new DialogInterface.OnClickListener() {
		      public void onClick(DialogInterface dialog, int which) {
		    	  System.out.println("Which..."+which);
		    	  final TextView roomListText = (TextView) findViewById(R.id.roomList);
		    	  isInitial = false;
		  		  RoomsActivity.this.roomChanged(true, roomListText, which, true);
		      }
		   });
		   return builder.create();
		}
	
	public void notificationReceived(byte[] paramArrayOfByte) {
		for (int index=0; index<paramArrayOfByte.length; index +=2) {
			byte devid = paramArrayOfByte[index];
			byte status = paramArrayOfByte[index+ 1];
			BtLocalDB.getInstance(this).updateDeviceStatus(devid, status);
			readAndUpateStatusForRoom(false);
			CommonUtils.getInstance().addNotification(this, selectedRoom.getName(), ""+devid, status);
		}
	}

	public void connectionStarted(final boolean isWifi) {
		runOnUiThread(new Runnable() {
			public void run() {
				if (RoomsActivity.this.lightsPanel != null) {
					setConnectionModeIcon(isWifi?2:1);
					System.out.println("AJJGJHFHFHGFHJGGGGGGggcdfdfdsf");
				}
			}
		});
	}

	public void connectionLost() {
		runOnUiThread(new Runnable() {
			public void run() {
				if (RoomsActivity.this.lightsPanel != null) {
					RoomsActivity.this.lightsPanel.resetButtonInPanel(false);
					RoomsActivity.this.devicePanel.resetButtonInPanel(false);
					RoomsActivity.this.groupPanel.resetButtonInPanel(true);
					setConnectionModeIcon(0);
				}
			}
		});
	}

	public void setConnectionModeIcon(int connectionType) {
		SvgView aboutButton = (SvgView) findViewById(R.id.aboutButton);
		if (connectionType == 0) 
			aboutButton.setImageResource(0);
		else if (connectionType == 1)
			aboutButton.setImageResource(R.raw.bt);
		else
			aboutButton.setImageResource(R.raw.wifi);
	}
	
	private void testExtras() {
		try {
			System.out.println("Setting room name ");
			btHwLayer.setRoomName("Bed Room");
			System.out.println("Setting room name Done");
		} catch (Exception e) {
			System.out.println("Setting error on roomnamme "+e.getMessage());
		}
		
		try {
			System.out.println("Getting room name ");
			byte[] bytes = btHwLayer.getRoomName();
			System.out.println("My roomname received is "+new String(bytes));
		} catch (Exception e) {
			System.out.println("Getting error on roomnamme "+e.getMessage());
		}
		
		try {
			System.out.println("Setting switch name ");
			btHwLayer.setSwitchName((byte)0x01, "Bed Fan");
			System.out.println("Setting switch name Done");
		} catch (Exception e) {
			System.out.println("Setting error on switch name "+e.getMessage());
		}
		
		try {
			System.out.println("Getting switch name ");
			byte[] bytes = btHwLayer.getSwitchName((byte)0x01);
			System.out.println("Switch name received is "+new String(bytes));
		} catch (Exception e) {
			System.out.println("Getting error on switchname "+e.getMessage());
		}
		
		try {
			System.out.println("Setting devproperty ");
			btHwLayer.setSwitchType((byte)1, true, true, (byte)2);
			System.out.println("Setting devproperty Done");
		} catch (Exception e) {
			System.out.println("Setting error on devproperty "+e.getMessage());
		}
		
		try {
			System.out.println("Getting devproperty name ");
			byte[] bytes = btHwLayer.getSwitchTypes();
			for( int i=0; i<bytes.length; i++) {
				boolean isdimmable = BtHwLayer.isDimmableByProp(bytes[i]);
				boolean isInvType = BtHwLayer.isInvByProp(bytes[i]);
				byte devtype = BtHwLayer.getDevTypeByProp(bytes[i]);
				System.out.println("dev property of ("+i+") is, d= "+isdimmable+ " inv= "+isInvType+ " type= "+devtype+" bytevalue:"+Integer.toBinaryString(devtype));
			}
		} catch (Exception e) {
			System.out.println("Getting error on devproperty "+e.getMessage());
		}
		
		try {
			System.out.println("Setting grp name ");
			btHwLayer.setGroupName((byte)0x01, "EveningGroup");
			System.out.println("Setting grp name Done");
		} catch (Exception e) {
			System.out.println("Setting error on grp name "+e.getMessage());
		}
		
		try {
			System.out.println("Getting grp name ");
			byte[] bytes = btHwLayer.getGroupName((byte)0x01);
			System.out.println("My grp name received is "+new String(bytes));
		} catch (Exception e) {
			System.out.println("Getting error on goup name "+e.getMessage());
		}
		
		try {
			System.out.println("Setting scheduler name ");
			btHwLayer.setSchedulerName((byte)0x01, "Alarm1");
			System.out.println("Setting scheduler name Done");
		} catch (Exception e) {
			System.out.println("Setting error on scheduler name "+e.getMessage());
		}
		
		try {
			System.out.println("Getting scheduler name ");
			byte[] bytes = btHwLayer.getSchedulerName((byte)0x01);
			System.out.println("My scheduler name received is "+new String(bytes));
		} catch (Exception e) {
			System.out.println("Getting error on scheduler "+e.getMessage());
		}
		
	}
	
	private void enableMainActivityOnDeviceConfig(boolean enableConfigMode) {
		System.out.println("enableMainActivityOnDeviceConfig: "+enableConfigMode);
		isInConfigDeviceMode = enableConfigMode;
		lightsPanel.enableEditMode(isInConfigDeviceMode);
		devicePanel.enableEditMode(isInConfigDeviceMode);
		groupPanel.enableEditMode(isInConfigDeviceMode);
		schedulePanel.enableEditMode(isInConfigDeviceMode);
	}

}

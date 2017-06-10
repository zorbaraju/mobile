package com.zorba.bt.app;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;

import com.zorba.bt.app.bluetooth.BtHwLayer;
import com.zorba.bt.app.bluetooth.ConnectionListener;
import com.zorba.bt.app.bluetooth.IOTMessageListener;
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
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Process;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListPopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

public class RoomsActivity extends ZorbaActivity
		implements NotificationListener, ConnectionListener, IOTMessageListener {

	String MENUNAME_ADDROOM = "Add Zorba";
	String MENUNAME_DEVICECONFIG = "Configure Zorba          ";
	String MENUNAME_ABOUT = "About";
	// -spb 250117 for removing inverter from menu String MENUNAME_INVERTOR =
	// "Invertor Settings";
	// -spb 010217 for removing change password from menu String
	// MENUNAME_CHANGEPWD = "Change Pwd";
	String MENUNAME_SETTINGS = "Admin Settings";
	String MENUNAME_OOH = "Connect to OOH";
	String MENUNAME_SENDLOG = "Send Log";
	String MENUNAME_MTLOG = "Mt Log";
	String MENUNAME_HELP = "Help";
	String MENUNAME_EXIT = "Exit";

	public static final int DISCOVERY_CODE = 1;
	public static final int ENABLEBT_CODE = DISCOVERY_CODE + 1;
	public static final int ENABLEWIFI_CODE = ENABLEBT_CODE + 1;
	public static final int ADDDEVICE_CODE = ENABLEWIFI_CODE + 1;
	public static final int ADDGROUP_CODE = ADDDEVICE_CODE + 1;
	public static final int ADDSCHEDULER_CODE = ADDGROUP_CODE + 1;
	public static final int APPINFO_CODE = ADDSCHEDULER_CODE + 1;
	public static final int SENDLOG_CODE = APPINFO_CODE + 1;
	public static final int HELP_CODE = SENDLOG_CODE + 1;
	// -spb 250117 for removing inverter from menu public static final int
	// INVERTER_CODE = HELP_CODE + 1;
	// -spb 250117 for removing inverter from menu public static final int
	// CHANGEPWD_CODE = INVERTER_CODE + 1;
	public static final int CHANGEPWD_CODE = HELP_CODE + 1;
	public static final int AWSIOT_CODE = CHANGEPWD_CODE + 1;
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
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		CommonUtils.getInstance().loadErrors(this);
		showMainScreen(savedInstanceState);
	}

	private void invokeSendLog() {
		Intent intent = new Intent(RoomsActivity.this, SendLogActivity.class);
		RoomsActivity.this.startActivityForResult(intent, SENDLOG_CODE);
	}

	private void invokeMTLOG() {
		Intent intent = new Intent(RoomsActivity.this, AwsIotActivity.class);
		String macaddress = "zorbadummy";
		if (selectedRoom != null && selectedRoom.getAddress() != null) {
			macaddress = selectedRoom.getAddress();
		}
		intent.putExtra("deviceName", macaddress);
		RoomsActivity.this.startActivityForResult(intent, AWSIOT_CODE);
	}

	private void showMainScreen(final Bundle savedInstanceState) {
		System.out.println("email"+ BtLocalDB.getInstance(this).getEmailId());
		BtLocalDB.getInstance(this).cleanDB();
		if (!BtLocalDB.getInstance(this).getEmailId().equals("")) {

			try {
				setContentView(R.layout.rooms);
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
				SvgView enableOOHButton = (SvgView) findViewById(R.id.enableOOH);
				enableOOHButton.setOnClickListener(new ZorbaOnClickListener() {
					public void zonClick(View v) {
						System.out.println("Enabling ooh");
						enableOOH(true);
					}
				});

				SvgView db = (SvgView) findViewById(R.id.discoverbutton);
				if (db != null) {
					db.setOnClickListener(new ZorbaOnClickListener() {
						public void zonClick(View paramAnonymousView1) {
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
				setConnectionModeIcon(CommonUtils.CONNECTION_OFFLINE);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			TextView countview = (TextView) findViewById(R.id.onDeviceCount);
			countview.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					CommonUtils.increateCount(v);
					System.out.println("Tocuhcount..." + CommonUtils.getTouchCount(v));
					if (CommonUtils.getTouchCount(v) > 5) {
						invokeSendLog();
						CommonUtils.resetCount();//+spb 180417
					}
				}
			});

		
			TextView countlabel = (TextView) findViewById(R.id.countlabel);
			countlabel.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					CommonUtils.increateCount(v);
					System.out.println("Tocuhcount..." + CommonUtils.getTouchCount(v));
					if (CommonUtils.getTouchCount(v) > 5) {
						invokeMTLOG();
						CommonUtils.resetCount();//+spb 180417
					}
				}
			});

		} else {
			setContentView(R.layout.activity_welcome);
			// -spb 250117 RadioButton masterbox =
			// (RadioButton)findViewById(R.id.master);
			CheckBox masterbox = (CheckBox) findViewById(R.id.master);
			masterbox.setChecked(true);
			// -spb 300117 for login svg button
			// Button gotoButton = (Button)findViewById(R.id.gotobutton);
			// gotoButton.setOnClickListener(new View.OnClickListener() {
			// -spb 3000117 for login svg button

			((SvgView) this.findViewById(R.id.gotobutton)).setOnClickListener(new View.OnClickListener() {

				public void onClick(View paramAnonymousView1) {

					String emailid = getValidEmailId();
						if (emailid == null || emailid.isEmpty()) {
							//-spb 270417 for errors CommonUtils.AlertBox(RoomsActivity.this, "Error", "Enter valid email id");
							CommonUtils.AlertBox(RoomsActivity.this,  CommonUtils.getInstance().getErrorString("ERROR50"),  CommonUtils.getInstance().getErrorString("ERROR58"));
						return;
					}
					// -spb 250117 RadioButton masterbox =
					// (RadioButton)findViewById(R.id.master);
					CheckBox masterbox = (CheckBox) findViewById(R.id.master);			
					saveEmailIdAndUserType(emailid, masterbox.isChecked());
					showMainScreen(savedInstanceState);
				}
			});
			EditText emailfield = (EditText) findViewById(R.id.emailfield);
			emailfield.requestFocus();
		}
	}

	private String getValidEmailId() {
		EditText emailfield = (EditText) findViewById(R.id.emailfield);
		String emailid = emailfield.getText().toString();
		emailid = emailid.trim();
		if (emailid.isEmpty())
			return null;
		return emailid;
	}

	private void saveEmailIdAndUserType(String emailid, boolean isMaster) {
		BtLocalDB.getInstance(this).setEmailId(emailid);
		BtLocalDB.getInstance(this).setUserType(isMaster);
	}

	@Override
	public void onBackPressed() {
		if (isInConfigDeviceMode) {
			enableMainActivityOnDeviceConfig(false);
			updateDeviceCount();//+spb 060217 for indication of device config mode
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
		if (btHwLayer != null) {
			btHwLayer.unregister();
			btHwLayer.closeDevice();
		}
		Logger.e(this, "main", "App is closed.....");
		finish();
		Process.killProcess(Process.myPid());
		super.onDestroy();
		System.exit(1);
	}

	private ListPopupWindow prepareHomeMenu() {

		// -spb 170117 for adding settings menu
		final ListPopupWindow popupWindow = new ListPopupWindow(this);

		final ArrayList<ImageTextData> arrayList = new ArrayList<ImageTextData>();
		arrayList.add(new ImageTextData(MENUNAME_ADDROOM, R.raw.addroom));
		arrayList.add(new ImageTextData(MENUNAME_DEVICECONFIG, R.raw.deviceconfig));
		// arrayList.add(new ImageTextData(MENUNAME_ABOUT, R.raw.about));
		// -spb 250117 for removing inverter from menu arrayList.add(new
		// ImageTextData(MENUNAME_INVERTOR, R.raw.settings));
		// -spb 010217 for removing change password from menu arrayList.add(new
		// ImageTextData(MENUNAME_CHANGEPWD, R.raw.changepassword));
		System.out.println("�s master user..RoomsActivity.ADDDEVICE_CODE"
				+ BtLocalDB.getInstance(RoomsActivity.this).isMasterUser());
		if (BtLocalDB.getInstance(RoomsActivity.this).isMasterUser()) {
			arrayList.add(new ImageTextData(MENUNAME_SETTINGS, R.raw.settings));
		}
		arrayList.add(new ImageTextData(MENUNAME_OOH, R.raw.oho));
		// arrayList.add(new ImageTextData(MENUNAME_SENDLOG, R.raw.sendlog));
		// arrayList.add(new ImageTextData(MENUNAME_MTLOG, R.raw.mtlog));
		arrayList.add(new ImageTextData(MENUNAME_HELP, R.raw.help));
		arrayList.add(new ImageTextData(MENUNAME_EXIT, R.raw.exit));

		ImageTextAdapter textAdapter = new ImageTextAdapter(this, arrayList, new ZorbaOnClickListener() {
			public void zonClick(View popupView) {
				popupWindow.dismiss();
				int menuIndex = ((Integer) popupView.getTag()).intValue();
				String selectedMenuName = arrayList.get(menuIndex).getText();
				if (selectedMenuName.equals(MENUNAME_ADDROOM)) {
					Intent intent = new Intent(RoomsActivity.this, DiscoveryActivity.class);
					RoomsActivity.this.startActivityForResult(intent, DISCOVERY_CODE);
				} else if (selectedMenuName.equals(MENUNAME_DEVICECONFIG)) {
					System.out.println("Device config menu is clicked");
					enableMainActivityOnDeviceConfig(true);
				} else if (selectedMenuName.equals(MENUNAME_HELP)) {
					Intent intent = new Intent(RoomsActivity.this, HelpActivity.class);
					RoomsActivity.this.startActivityForResult(intent, HELP_CODE);
				} else if (selectedMenuName.equals(MENUNAME_ABOUT)) {
					Intent intent = new Intent(RoomsActivity.this, AppInfoActivity.class);
					RoomsActivity.this.startActivityForResult(intent, APPINFO_CODE);
				} else if (selectedMenuName.equals(MENUNAME_SENDLOG)) {
					Intent intent = new Intent(RoomsActivity.this, SendLogActivity.class);
					RoomsActivity.this.startActivityForResult(intent, SENDLOG_CODE);
					// -spb 250117 for removing inverter from menu
					// } else if (selectedMenuName.equals(MENUNAME_INVERTOR)) {
					// Intent intent = new Intent(RoomsActivity.this,
					// InverterActivity.class);
					// RoomsActivity.this.startActivityForResult(intent,
					// INVERTER_CODE);
					// -spb 250117 for removing inverter from menu

					// -spb 010217 for removing change password from menu
					// } else if (selectedMenuName.equals(MENUNAME_CHANGEPWD)) {
					// Intent intent = new Intent(RoomsActivity.this,
					// ChangepwdActivity.class);
					// RoomsActivity.this.startActivityForResult(intent,
					// CHANGEPWD_CODE);
					// -spb 010217 for removing change password from menu
				} else if (selectedMenuName.equals(MENUNAME_SETTINGS)) {
					Intent intent = new Intent(RoomsActivity.this, SettingsActivity.class);
					RoomsActivity.this.startActivityForResult(intent, CHANGEPWD_CODE);
				} else if (selectedMenuName.equals(MENUNAME_OOH)) {
					enableOOH(true);
				} else if (selectedMenuName.equals(MENUNAME_MTLOG)) {
					Intent intent = new Intent(RoomsActivity.this, AwsIotActivity.class);
					String macaddress = "zorbadummy";
					if (selectedRoom != null && selectedRoom.getAddress() != null) {
						macaddress = selectedRoom.getAddress();
					}
					intent.putExtra("deviceName", macaddress);
					RoomsActivity.this.startActivityForResult(intent, AWSIOT_CODE);
				} else if (selectedMenuName.equals(MENUNAME_EXIT)) {
					RoomsActivity.this.performExit();
				}
			}
		});
		// -spb 110117 for shifting popup menu little right
		// popupWindow.setHorizontalOffset( -200 );
		popupWindow.setHorizontalOffset(0);
		// -spb 110117 for shifting popup menu little down
		// popupWindow.setVerticalOffset( -100 );
		popupWindow.setVerticalOffset(-102);
		popupWindow.setAdapter((ListAdapter) textAdapter);
		popupWindow.setAnchorView(findViewById(R.id.homeButton));
		popupWindow
				.setWidth(CommonUtils.measureContentWidth(popupWindow.getListView(), (ListAdapter) textAdapter) + 60);
		popupWindow.setHeight(popupWindow.WRAP_CONTENT);
		return popupWindow;
	}

	private void setImageDrawable(int alarm) {
		// TODO Auto-generated method stub

	}

	private void prepareRoomListMenu(String newRoomName, boolean isNewRoom) {
		final TextView roomListText = (TextView) findViewById(R.id.roomList);
		ZorbaOnClickListener listener = new ZorbaOnClickListener() {
			public void zonClick(View view) {
				RoomsActivity.this.roomMenuList.dismiss();
				RoomsActivity.this.roomChanged(false, roomListText, ((Integer) view.getTag()).intValue(), true);
			}
		};
		this.roomDataList = BtLocalDB.getInstance(this).getRoomList();
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
			if (!isNewRoom) {
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
				roomDataList = BtLocalDB.getInstance(this).getRoomList();
				roomDataList.remove(0);
				if (this.roomDataList.size() == 0) {
					((ScrollView) findViewById(R.id.scrollView)).setVisibility(View.GONE);
					((LinearLayout) findViewById(R.id.rgbPanel)).setVisibility(View.GONE);
					((LinearLayout) findViewById(R.id.emptydevicepanel)).setVisibility(View.VISIBLE);
					roomListText.setText("No rooms");
				} else {
					int selectIndex = BtLocalDB.getInstance(this).getLastSelectedRoom() - 1;
					if (newRoomName.isEmpty()) {
						if (selectIndex < 0)
							selectIndex = 0;
						roomChanged(false, roomListText, selectIndex, false);
						return;
					}
					for (int index = 0; index < roomDataList.size(); index++) {
						RoomData rd = roomDataList.get(index);
						System.out.println("Number of rooms...." + roomDataList.size() + " newroom:" + newRoomName
								+ " rd.." + rd.getName());
						if (rd.getName().equals(newRoomName)) {
							selectIndex = index;
							System.out.println("Number of rooms...." + roomDataList.size() + " newroom:" + newRoomName
									+ " selectIndex.." + selectIndex);
							roomChanged(false, roomListText, selectIndex, false);
							break;
						}
					}
				}
				return;
			}
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	private MyComp populateDeviceButtons(final String tabName) {
		MyComp local16 = new MyComp(getApplicationContext(), tabName, -1, isInConfigDeviceMode) {
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
								BtLocalDB.getInstance(RoomsActivity.this).deleteDevice(
										RoomsActivity.this.selectedRoom.getDeviceName(),
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
		ArrayList<DeviceData> deviceList = BtLocalDB.getInstance(this).getDevices(this.selectedRoom.getDeviceName(),
				null);
		local16.expandComp(true);
		if (tabName.equals(CommonUtils.TABSWITCH))
			this.lightsPanel = local16;
		else
			this.devicePanel = local16;
		int numdevices = deviceList.size();
		for (int ddindex = 0; ddindex < numdevices; ddindex++) {
			DeviceData device = deviceList.get(ddindex);
			if (!device.isUnknownType()) {
				if ((tabName.equals(CommonUtils.TABSWITCH)) && (!DeviceData.isDimmable(device.getType()))) {
					addButtonPanel(local16, device, true);
				} else if ((tabName.equals(CommonUtils.TABDIMMABLES)) && (DeviceData.isDimmable(device.getType()))) {
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
		List<GroupData> groupList = BtLocalDB.getInstance(this).getGroups(this.selectedRoom.getDeviceName(), null);
		for (int gindex = 0; gindex < groupList.size(); gindex++) {
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
									btHwLayer.sendDeleteAlarmCommandToDevice(RoomsActivity.this.selectedSchedulerId);
									BtLocalDB.getInstance(RoomsActivity.this).deleteSchedule(
											RoomsActivity.this.selectedRoom.getDeviceName(),
											RoomsActivity.this.selectedSchedulerId);
									RoomsActivity.this.schedulePanel
											.removeMyView(RoomsActivity.this.selectedScheduleName);
									showDeleteButton(false);
									return;
								} catch (Exception e) {
									//-spb 270417 for errors 
									/*
									CommonUtils.AlertBox(RoomsActivity.this, "Delete Schedular",
													"Not able to delete schedular");
									//-spb 270417 for errors 
									*/
									CommonUtils.AlertBox(RoomsActivity.this,  CommonUtils.getInstance().getErrorString("ERROR56"),
											 CommonUtils.getInstance().getErrorString("ERROR57"));
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
			addScheduleButton(Integer.parseInt(localSchedulerData.getSchedulerId()), localSchedulerData.getName(),
					true);
		}
		return this.schedulePanel;
	}

	private void addButtonPanel(final MyComp paramMyComp, DeviceData paramDeviceData, boolean isnew) {
		final String deviceName = paramDeviceData.getName();
		if (!isnew) {
			paramMyComp.updateMyView(deviceName, paramDeviceData);
			return;
		}
		final int devid = paramDeviceData.getDevId();
		final String devtype = paramDeviceData.getType();
		paramDeviceData.setStatus(BtLocalDB.getInstance(this).getDeviceStatus((byte) devid));
		final ImageTextButton deviceButton = new ImageTextButton(this);
		deviceButton.setDevice(paramDeviceData);
		deviceButton.setOnLongClickListener(new View.OnLongClickListener() {
			public boolean onLongClick(View paramAnonymousView) {
				paramMyComp.showDeleteButton(true);
				RoomsActivity.this.selectedDeviceName = deviceName;
				paramMyComp.selectComp(deviceButton);
				if (!isInConfigDeviceMode) {
					if (DeviceData.isDimmable(devtype)) {
						RoomsActivity.this.controlDevice(deviceButton, devtype, devid);
					}
				}
				return true;
			}
		});
		deviceButton.setOnClickListener(new ZorbaOnClickListener() {
			public void zonClick(View paramAnonymousView) {
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
					// -spb 010217 for error msg chg CommonUtils.AlertBox(this,
					// "Device count", "No data from device");
					//-spb 270417 for errors CommonUtils.AlertBox(this, "Device count", "No communication with the device");
					CommonUtils.AlertBox(this, CommonUtils.getInstance().getErrorString("ERROR76"), CommonUtils.getInstance().getErrorString("ERROR77"));
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
				CommonUtils.AlertBox(this, "Data error", e1.getMessage());
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
				updateDeviceCount();
				try {
					if (RoomsActivity.this.groupPanel.isReset()) {
						RoomsActivity.this.groupPanel.updateLiveButtonInPanel();
					}
					if (!btHwLayer.isConnected())
						groupPanel.resetButtonInPanel(true);
					return;
				} catch (Exception localException) {
					//-spb 270417 for errors CommonUtils.AlertBox(RoomsActivity.this, "Read Power", "No data from device");
					CommonUtils.AlertBox(RoomsActivity.this, CommonUtils.getInstance().getErrorString("ERROR78"), CommonUtils.getInstance().getErrorString("ERROR79"));
				}
			}
		});
	}

	protected void updateDeviceCount() {
		int i = BtLocalDB.getInstance(RoomsActivity.this).getDevicesOnCount();
		//+spb 060217 for indication of device config mode
		if(isInConfigDeviceMode)
		{
			((TextView) findViewById(R.id.onDeviceCount)).setTextColor(Color.parseColor(CommonUtils.SEEKBAR_COLOR));//+spb 060217 for no connection text col chg
			((TextView) findViewById(R.id.onDeviceCount)).setTextSize(14);			
			((TextView) findViewById(R.id.onDeviceCount)).setText("CONFIG MODE ON : PRESS BACK TO EXIT");
		}
		else
		{
			((TextView) findViewById(R.id.onDeviceCount)).setTextColor(Color.parseColor(CommonUtils.SEEKBAR_COLOR));//+spb 060217 for no connection text col chg
			((TextView) findViewById(R.id.onDeviceCount)).setTextSize(20);
			((TextView) findViewById(R.id.onDeviceCount)).setText("" + i);
		}
		//+spb 060217 for indication of device config mode
	}

	private void singleClickButton(final int devid, final String devtype, ImageTextButton paramImageTextButton) {
		if (!btHwLayer.isConnected())
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
								readValue = BtLocalDB.getInstance(RoomsActivity.this)
										.getDevicePrevOnStatus((byte) devid);
								if (readValue == -1)
									readValue = 9;
								System.out.println("Prev status fro  db..." + readValue);
							}
						}
						btHwLayer.sendCommandToDevice(devid, readValue);
						BtLocalDB.getInstance(RoomsActivity.this).updateDeviceStatus((byte) devid, (byte) readValue);
						readAndUpateStatusForRoom(false);
						// testExtras();

						return null;
					} catch (Exception paramString1) {
						CommonUtils.AlertBox(RoomsActivity.this, CommonUtils.getInstance().getErrorString("ERROR47"), paramString1.getMessage());
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
		final ImageTextButton localImageTextButton = new ImageTextButton(this);
		ArrayList<GroupData> grpArr = BtLocalDB.getInstance(this)
				.getGroups(RoomsActivity.this.selectedRoom.getDeviceName(), groupName);
		if (grpArr.size() == 0)
			return;
		final GroupData groupData = grpArr.get(0);
		if (!isnew) {
			groupPanel.updateMyView(groupName, groupData.getImageResId());
			return;
		}
		localImageTextButton.setText(groupName);
		localImageTextButton.changeDeviceButtonStyle(0);
		localImageTextButton.setBackgroundImage(groupData.getImageResId());
		localImageTextButton.setImageResId(groupData.getImageResId());
		localImageTextButton.setOnClickListener(new ZorbaOnClickListener() {
			public void zonClick(View paramAnonymousView) {
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
					//-spb 060217 for aligning error CommonUtils.AlertBox(RoomsActivity.this, "Read Error", e.getMessage());
					//-spb 270417 for errors  CommonUtils.AlertBox(RoomsActivity.this, "Read Error", "Device is Off ");
					CommonUtils.AlertBox(RoomsActivity.this,  CommonUtils.getInstance().getErrorString("ERROR47"),  CommonUtils.getInstance().getErrorString("ERROR48"));
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
		final ImageTextButton localImageTextButton = new ImageTextButton(this);
		ArrayList<SchedulerData> schArr = BtLocalDB.getInstance(this)
				.getSchedules(RoomsActivity.this.selectedRoom.getDeviceName(), scheduleName);
		if (schArr.size() == 0)
			return;
		SchedulerData scheduleData = schArr.get(0);
		if (!isnew) {
			groupPanel.updateMyView(scheduleName, scheduleData.getImageResId());
			return;
		}
		localImageTextButton.changeDeviceButtonStyle(0);// +spb 310117 for
														// schedular image with
														// black background on
														// room page
		localImageTextButton.setBackgroundImage(scheduleData.getImageResId());
		localImageTextButton.setImageResId(scheduleData.getImageResId());
		localImageTextButton.setText(scheduleName);
		localImageTextButton.setOnClickListener(new ZorbaOnClickListener() {
			public void zonClick(View paramAnonymousView) {
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

	private void controlDevice(final ImageTextButton paramImageTextButton, final String devtype, final int devid) {
		Object obj = LayoutInflater.from(this).inflate(R.layout.devicecontroller, null);
		AlertDialog.Builder localBuilder = new AlertDialog.Builder(this);
		localBuilder.setView((View) obj);
		SeekBar localObject = (SeekBar) ((View) obj).findViewById(R.id.seekBar1);
		localObject.getProgressDrawable().setColorFilter(Color.parseColor(CommonUtils.SEEKBAR_COLOR),
				PorterDuff.Mode.SRC_IN);
		localObject.getThumb().setColorFilter(Color.parseColor(CommonUtils.SEEKBAR_COLOR), PorterDuff.Mode.SRC_IN);
		try {
			int i = btHwLayer.readCommandToDevice(devid);
			if (i != -1) {
				((SeekBar) localObject).setProgress(i * 10);
			}
			((SeekBar) localObject).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				int prevvalue = -1;

				public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
					if (fromUser) {

						try {
							progresValue = seekBar.getProgress() / 10;
							if (progresValue == 10) {
								progresValue = 9;
							}
							if (prevvalue != progresValue) {
								btHwLayer.sendCommandToDevice(devid, progresValue);
								paramImageTextButton.changeDeviceButtonStyle(devtype, progresValue);
								if (progresValue != 0) {
									BtLocalDB.getInstance(RoomsActivity.this).updateDevicePrevOnStatus((byte) devid,
											(byte) progresValue);
									System.err.println("Last on status..." + progresValue);
								}
								BtLocalDB.getInstance(RoomsActivity.this).updateDeviceStatus((byte) devid,
										(byte) progresValue);
							}
							prevvalue = progresValue;
							return;
						} catch (Exception paramAnonymousSeekBare) {
							//-spb 270417 for errors 
							/*
							CommonUtils.AlertBox(RoomsActivity.this, "Error",
									"Sending cmd to Device:" + paramAnonymousSeekBare.getMessage());
							//-spb 270417 for errors 
							*/
							CommonUtils.AlertBox(RoomsActivity.this,  CommonUtils.getInstance().getErrorString("ERROR50"),
									 CommonUtils.getInstance().getErrorString("ERROR51") + paramAnonymousSeekBare.getMessage());
						}

					}
				}

				public void onStartTrackingTouch(SeekBar paramAnonymousSeekBar) {
				}

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
					updateDeviceCount();//+spb 060217 for device count not getting updated on dimmer change from off to on

				}
			});
			// -spb 270117 for room page seekbar close popup
			// localBuilder.setCancelable(false).setNegativeButton("Close", new
			// DialogInterface.OnClickListener() {
			// -spb 270117 for room page seekbar close popup public void
			// onClick(DialogInterface paramAnonymousDialogInterface, int
			// paramAnonymousInt) {
			// -spb 270117 for room page seekbar close popup
			// paramAnonymousDialogInterface.cancel();
			// -spb 270117 for room page seekbar close popup }
			// -spb 270117 for room page seekbar close popup });
			localBuilder.create().show();
			return;
		} catch (Exception paramImageTextButtone) {
			// -spb 010217 for error msg chg CommonUtils.AlertBox(this, "Read
			// Error", "Error in reading command:" +
			// paramImageTextButtone.getMessage());
			//-spb 060217 for aligning error CommonUtils.AlertBox(this, "Read Error", "Error in reading command:" + paramImageTextButtone.getMessage());
			//-spb 270417 for errors CommonUtils.AlertBox(this, "Read Error", "Error in reading command:Communication Error" );
			CommonUtils.AlertBox(this,  CommonUtils.getInstance().getErrorString("ERROR47"),  CommonUtils.getInstance().getErrorString("ERROR49") );
		}
	}

	private void populatePageForSelectedRoom() {
		if (!btHwLayer.isWifiEnabled() && !btHwLayer.makeBtEnabled()) {
			System.out.println("pbt is not enableds");
			return;
		}

		// isInitial = false;
		System.err.println("IsInitial....." + isInitial);
		if (isInitial) {
			TextView roomListText = (TextView) findViewById(R.id.roomList);
			roomListText.post(new Runnable() {
				public void run() {
					Dialog dialog = onCreateDialog();
					dialog.show();
					// roomMenuList.show();
				}
			});
		} else {
			((LinearLayout) findViewById(R.id.roomContent)).removeAllViews();
			lightsPanel = populateDeviceButtons(CommonUtils.TABSWITCH);
			devicePanel = populateDeviceButtons(CommonUtils.TABDIMMABLES);
			groupPanel = populateGroups();
			schedulePanel = populateSchedules();

			this.lightsPanel.setSiblings(new MyComp[] { this.devicePanel, this.groupPanel, this.schedulePanel });
			this.devicePanel.setSiblings(new MyComp[] { this.lightsPanel, this.groupPanel, this.schedulePanel });
			this.groupPanel.setSiblings(new MyComp[] { this.lightsPanel, this.devicePanel, this.schedulePanel });
			this.schedulePanel.setSiblings(new MyComp[] { this.lightsPanel, this.devicePanel, this.groupPanel });
			((RelativeLayout) findViewById(R.id.rootView)).setBackgroundColor(Color.parseColor("#ff403c3a"));// 403c3a
			((ScrollView) findViewById(R.id.scrollView)).setBackgroundColor(Color.parseColor("#ff1e1e1e"));
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
				if (selectedRoom == null)
					return null;
				String incomingssid = selectedRoom.getSSID();
				String ipaddress = selectedRoom.getIpAddress();
				String macaddress = selectedRoom.getAddress();
				if (btHwLayer.isWifiEnabled()) {
					if (incomingssid != null && !incomingssid.isEmpty() && !incomingssid.equals("null")) {
						macaddress = null;
						ipaddress = CommonUtils.enableNetwork(RoomsActivity.this, incomingssid, incomingssid);
						if (ipaddress == null) {
							//-spb 270417 for errors 
							/*
							CommonUtils.AlertBox(RoomsActivity.this, "Connection",
									"Ipaddress is found for ssid:" + incomingssid);
							*/
							//-spb 270417 for errors 
							CommonUtils.AlertBox(RoomsActivity.this,  CommonUtils.getInstance().getErrorString("ERROR59"),
									 CommonUtils.getInstance().getErrorString("ERROR60")+ incomingssid);
							return null;
						}
					} else {
						incomingssid = null;
					}
				} else {
					incomingssid = null;
					ipaddress = null;
				}

				btHwLayer.closeDevice();
				String error = btHwLayer.initDevice(selectedRoom.getName(), macaddress, incomingssid, ipaddress, false);
				if (error == null) {
					try {
						int numberOfDevices = btHwLayer.getNumberOfDevices();
						CommonUtils.setMaxNoDevices(numberOfDevices);
						isUpdate = true;
					} catch (Exception e) {
						isUpdate = false;
						//-spb 270417 for errors  CommonUtils.AlertBox(RoomsActivity.this, "Connection Error",CommonUtils.getInstance().getErrorString("ERROR2"));
						CommonUtils.AlertBox(RoomsActivity.this,  CommonUtils.getInstance().getErrorString("ERROR61"),CommonUtils.getInstance().getErrorString("ERROR62"));
					}
				} else {
					//-spb 270417 for errors  CommonUtils.AlertBox(RoomsActivity.this, "Connection Error",CommonUtils.getInstance().getErrorString("ERROR3"));
					CommonUtils.AlertBox(RoomsActivity.this, CommonUtils.getInstance().getErrorString("ERROR61"),CommonUtils.getInstance().getErrorString("ERROR63"));
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
			if (isFromSelection)
				return;
		}
		if (roomDataList.size() == 0)
			return;
		this.selectedRoom = ((RoomData) this.roomDataList.get(paramInt));
		System.out.println("Rodetailss.selectedRoom.." + selectedRoom.getAddress() + ":" + selectedRoom.getDeviceName()
				+ ":" + selectedRoom.getIpAddress() + ":" + selectedRoom.getName() + ":" + selectedRoom.getSSID());

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
			newRoom = resultIntent.getExtras().getString("newroomname").toUpperCase();
			if (!newRoom.isEmpty()) {
				prepareRoomListMenu(newRoom, true);
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
		Logger.e(this, "onActivityResult",
				"requestcode=" + requestCode + " resultCode=" + resultCode + " " + AWSIOT_CODE);
		if (requestCode == AWSIOT_CODE) {
			return;
		} else if (requestCode == DISCOVERY_CODE) {
			btHwLayer.register();
			fromDiscoveryActivity(resultIntent);
		} else if (requestCode == ENABLEBT_CODE) {
			populatePageForSelectedRoom();
		} else if ((requestCode == ADDDEVICE_CODE) && (resultCode == RUSULTCODE_SAVE)) {
			String name = resultIntent.getExtras().getString("name");
			String type = resultIntent.getExtras().getString("type");
			DeviceData deviceData = new DeviceData(resultIntent.getExtras().getInt("index"), name, type,
					resultIntent.getExtras().getString("power"), -1);
			if (DeviceData.isDimmable(type)) {
				addButtonPanel(this.devicePanel, deviceData, resultIntent.getExtras().getBoolean("isnew"));
			} else {
				addButtonPanel(this.lightsPanel, deviceData, resultIntent.getExtras().getBoolean("isnew"));
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
				//-spb 270417 for errors CommonUtils.AlertBox(this, "Scheduler:" + name, resultIntent.getExtras().getString("error"));
				CommonUtils.AlertBox(this,  CommonUtils.getInstance().getErrorString("ERROR55") + name, resultIntent.getExtras().getString("error"));
				return;
			}
			addScheduleButton(requestCode, name, resultIntent.getExtras().getBoolean("isnew"));
			updateDeviceCount();
		}
		System.out.println("Re..." + requestCode + " awsiot=" + AWSIOT_CODE);
	}

	public Dialog onCreateDialog() {
		CharSequence[] menunames = new CharSequence[roomDataList.size()];
		int index = 0;
		for (RoomData room : roomDataList) {
			menunames[index++] = room.getName();
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Select a room").setItems(menunames, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				System.out.println("Which..." + which);
				final TextView roomListText = (TextView) findViewById(R.id.roomList);
				isInitial = false;
				RoomsActivity.this.roomChanged(true, roomListText, which, true);
			}
		});
		return builder.create();
	}

	public void notificationReceived(byte[] paramArrayOfByte) {
		for (int index = 0; index < paramArrayOfByte.length; index += 2) {
			byte devid = paramArrayOfByte[index];
			byte status = paramArrayOfByte[index + 1];
			BtLocalDB.getInstance(this).updateDeviceStatus(devid, status);
			readAndUpateStatusForRoom(false);
		}
	}

	public void connectionStarted(final int connectionType) {
		runOnUiThread(new Runnable() {
			public void run() {
				if (RoomsActivity.this.lightsPanel != null) {
					setConnectionModeIcon(connectionType);
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
					setConnectionModeIcon(CommonUtils.CONNECTION_OFFLINE);
		
				}
			}
		});
	}

	public void setConnectionModeIcon(int connectionType) {
		SvgView aboutButton = (SvgView) findViewById(R.id.aboutButton);
		if (connectionType == CommonUtils.CONNECTION_OFFLINE)
			aboutButton.setImageResource(R.raw.noconnection);
		else if (connectionType == CommonUtils.CONNECTION_BT)
			aboutButton.setImageResource(R.raw.bt);
		else if (connectionType == CommonUtils.CONNECTION_WIFI)
			aboutButton.setImageResource(R.raw.wifi);
		else if (connectionType == CommonUtils.CONNECTION_DATA)
			aboutButton.setImageResource(R.raw.oho_ind);
		
		//+spb 060217 indication of lost connection and msg
		((TextView) findViewById(R.id.onDeviceCount)).setTextSize(14);		
		((TextView) findViewById(R.id.onDeviceCount)).setTextColor(Color.parseColor(CommonUtils.NO_CONNECTION_TAP_SWITCH_COLOR));//+spb 060217 for no connection text col chg
		((TextView) findViewById(R.id.onDeviceCount)).setText("PRESS ANY SWITCH TO GET CONNECTION BACK");
		//+spb 060217 indication of lost connection and msg
	}

	private void testExtras() {
		try {
			System.out.println("Setting room name ");
			btHwLayer.setRoomName("Bed Room");
			System.out.println("Setting room name Done");
		} catch (Exception e) {
			System.out.println("Setting error on roomnamme " + e.getMessage());
		}

		try {
			System.out.println("Getting room name ");
			byte[] bytes = btHwLayer.getRoomName();
			System.out.println("My roomname received is " + new String(bytes));
		} catch (Exception e) {
			System.out.println("Getting error on roomnamme " + e.getMessage());
		}

		try {
			System.out.println("Setting switch name ");
			btHwLayer.setSwitchName((byte) 0x01, "Bed Fan");
			System.out.println("Setting switch name Done");
		} catch (Exception e) {
			System.out.println("Setting error on switch name " + e.getMessage());
		}

		try {
			System.out.println("Getting switch name ");
			byte[] bytes = btHwLayer.getSwitchName((byte) 0x01);
			System.out.println("Switch name received is " + new String(bytes));
		} catch (Exception e) {
			System.out.println("Getting error on switchname " + e.getMessage());
		}

		try {
			System.out.println("Setting devproperty ");
			btHwLayer.setSwitchType((byte) 1, true, true, (byte) 2);
			System.out.println("Setting devproperty Done");
		} catch (Exception e) {
			System.out.println("Setting error on devproperty " + e.getMessage());
		}

		try {
			System.out.println("Getting devproperty name ");
			byte[] bytes = btHwLayer.getSwitchTypes();
			for (int i = 0; i < bytes.length; i++) {
				boolean isdimmable = BtHwLayer.isDimmableByProp(bytes[i]);
				boolean isInvType = BtHwLayer.isInvByProp(bytes[i]);
				byte devtype = BtHwLayer.getDevTypeByProp(bytes[i]);
				System.out.println("dev property of (" + i + ") is, d= " + isdimmable + " inv= " + isInvType + " type= "
						+ devtype + " bytevalue:" + Integer.toBinaryString(devtype));
			}
		} catch (Exception e) {
			System.out.println("Getting error on devproperty " + e.getMessage());
		}

		try {
			System.out.println("Setting grp name ");
			btHwLayer.setGroupName((byte) 0x01, "EveningGroup");
			System.out.println("Setting grp name Done");
		} catch (Exception e) {
			System.out.println("Setting error on grp name " + e.getMessage());
		}

		try {
			System.out.println("Getting grp name ");
			byte[] bytes = btHwLayer.getGroupName((byte) 0x01);
			System.out.println("My grp name received is " + new String(bytes));
		} catch (Exception e) {
			System.out.println("Getting error on goup name " + e.getMessage());
		}

		try {
			System.out.println("Setting scheduler name ");
			btHwLayer.setSchedulerName((byte) 0x01, "Alarm1");
			System.out.println("Setting scheduler name Done");
		} catch (Exception e) {
			System.out.println("Setting error on scheduler name " + e.getMessage());
		}

		try {
			System.out.println("Getting scheduler name ");
			byte[] bytes = btHwLayer.getSchedulerName((byte) 0x01);
			System.out.println("My scheduler name received is " + new String(bytes));
		} catch (Exception e) {
			System.out.println("Getting error on scheduler " + e.getMessage());
		}

	}

	private void enableMainActivityOnDeviceConfig(boolean enableConfigMode) {
		System.out.println("enableMainActivityOnDeviceConfig: " + enableConfigMode);
		isInConfigDeviceMode = enableConfigMode;
		if( lightsPanel == null)
			return;
		lightsPanel.enableEditMode(isInConfigDeviceMode);
		devicePanel.enableEditMode(isInConfigDeviceMode);
		groupPanel.enableEditMode(isInConfigDeviceMode);
		schedulePanel.enableEditMode(isInConfigDeviceMode);
	}

	private void enableOOH(final boolean enable) {
		BackgroundTaskDialog task = new BackgroundTaskDialog(RoomsActivity.this) {
			
			@Override
			public Object runTask(Object params) {
				boolean enabled = btHwLayer.isDataEnabled(enable);
				if( !enabled) {
					//-spb 270417 for errors CommonUtils.AlertBox(RoomsActivity.this, "OOH", "Enable Data for OOH");
					CommonUtils.AlertBox(RoomsActivity.this, CommonUtils.getInstance().getErrorString("ERROR52"),  CommonUtils.getInstance().getErrorString("ERROR54"));
					return null;
				}
				if (enabled) {
					roomDataList = BtLocalDB.getInstance(RoomsActivity.this).getRoomList();
					roomDataList.remove(0);
					enabled = btHwLayer.enableNotificationForRooms(RoomsActivity.this, RoomsActivity.this.selectedRoom, RoomsActivity.this.roomDataList);
					readAndUpateStatusForRoom(true);
				}
				return enabled;
			}
			
			@Override
			public void finishedTask(Object result) {
				if( result != null) {
					boolean enabled = (Boolean)result;
					if(!enabled){
						//-spb 270417 for errors 	CommonUtils.AlertBox(RoomsActivity.this, "OOH", "No connection to Server");
						CommonUtils.AlertBox(RoomsActivity.this,  CommonUtils.getInstance().getErrorString("ERROR52"),  CommonUtils.getInstance().getErrorString("ERROR53"));
					}
				}
			}
		};
	}

	@Override
	public void mesgReceveid(String roomname, byte devids[], byte statuses[]) {
		CommonUtils.getInstance().addNotification(this, roomname, devids, statuses);
	}

}

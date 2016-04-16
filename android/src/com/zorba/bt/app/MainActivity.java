package com.zorba.bt.app;

import java.util.ArrayList;
import java.util.HashMap;

import com.zorba.bt.app.bluetooth.BtHwLayer;
import com.zorba.bt.app.bluetooth.ConnectionListener;
import com.zorba.bt.app.bluetooth.NotificationListener;
import com.zorba.bt.app.dao.DeviceData;
import com.zorba.bt.app.dao.RoomData;
import com.zorba.bt.app.dao.SchedulerData;
import com.zorba.bt.app.db.BtLocalDB;
import com.zorba.bt.app.utils.BackgroundTask;
import com.zorba.bt.app.utils.BackgroundTaskDialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Process;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListPopupWindow;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends ZorbaActivity implements NotificationListener, ConnectionListener{

	public static final int DISCOVERY_CODE = 1;
	public static final int ENABLEBT_CODE = DISCOVERY_CODE+1;
	public static final int ENABLEWIFI_CODE = ENABLEBT_CODE+1;
	public static final int ADDDEVICE_CODE = ENABLEWIFI_CODE+1;
	public static final int ADDGROUP_CODE = ADDDEVICE_CODE+1;
	public static final int ADDSCHEDULER_CODE = ADDGROUP_CODE+1;
	public static final int APPINFO_CODE = ADDSCHEDULER_CODE+1;
	public static final int SENDLOG_CODE = APPINFO_CODE+1;
	public static final int HELP_CODE = SENDLOG_CODE+1;
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.landinglayout);
		
		rgbController = new RGBController(this);
		final ListPopupWindow homeMenu = prepareHomeMenu();
		ImageButton homeButton = (ImageButton) findViewById(R.id.homeButton);
		homeButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				homeMenu.show();
			}
		});
		this.roomMenuList = new ListPopupWindow(this);
		TextView roomText = (TextView) findViewById(R.id.roomList);
		roomText.setOnClickListener(new View.OnClickListener() {
			public void onClick(View paramAnonymousView) {
				MainActivity.this.roomMenuList.show();
			}
		});
		/*
		((ImageButton) findViewById(R.id.aboutButton)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View paramAnonymousView1) {
				Intent paramAnonymousView = new Intent(MainActivity.this, AppInfoActivity.class);
				MainActivity.this.startActivityForResult(paramAnonymousView, APPINFO_CODE);
			}
		});*/
		ImageButton db = (ImageButton) findViewById(R.id.discoverbutton);
		if (db != null) {
			db.setOnClickListener(new View.OnClickListener() {
				public void onClick(View paramAnonymousView1) {
					btHwLayer.unregister();
					Intent paramAnonymousView = new Intent(MainActivity.this, DiscoveryActivity.class);
					MainActivity.this.startActivityForResult(paramAnonymousView, DISCOVERY_CODE);
				}
			});
		}
		btHwLayer = BtHwLayer.getInstance(this);
		prepareRoomListMenu("", false);
		
		System.out.println("Number>>>>> of rooms...." + roomDataList.size());
		if (this.roomDataList.size() == 0) {
			startActivityForResult(new Intent(this, DiscoveryActivity.class), DISCOVERY_CODE);
		}
		setConnectionModeIcon();
	}

	
	@Override
	public void onBackPressed() {
		new AlertDialog.Builder(this).setTitle("Exit").setMessage("Do you really want to exit ?")
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt) {
						MainActivity.this.performExit();
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
		arrayList.add(new ImageTextData("Add Room", R.drawable.discovery));
		arrayList.add(new ImageTextData("Help", R.drawable.help));
		arrayList.add(new ImageTextData("About", R.drawable.about));
		arrayList.add(new ImageTextData("Exit", R.drawable.exit));
		ImageTextAdapter textAdapter = new ImageTextAdapter(this, arrayList, new OnClickListener() {
			public void onClick(View popupView) {
				popupWindow.dismiss();
				int i = ((Integer) popupView.getTag()).intValue();
				if (i == 0) {
					Intent intent = new Intent(MainActivity.this, DiscoveryActivity.class);
					MainActivity.this.startActivityForResult(intent, DISCOVERY_CODE);
					return;
				}
				if (i == 2) {
					Intent intent = new Intent(MainActivity.this, AppInfoActivity.class);
					MainActivity.this.startActivityForResult(intent, APPINFO_CODE);
					return;
				}

				if (i == 1) {
					Intent intent = new Intent(MainActivity.this, HelpActivity.class);
					MainActivity.this.startActivityForResult(intent, HELP_CODE);
					return;
				}
				MainActivity.this.performExit();
			}
		});
		popupWindow.setAdapter((ListAdapter) textAdapter);
		popupWindow.setAnchorView(findViewById(R.id.homeButton));
		popupWindow
				.setWidth(CommonUtils.measureContentWidth(popupWindow.getListView(), (ListAdapter) textAdapter) + 60);
		return popupWindow;
	}

	private void prepareRoomListMenu(String newRoomName, boolean paramBoolean) {
		final TextView roomListText = (TextView) findViewById(R.id.roomList);
		OnClickListener listener = new View.OnClickListener() {
			public void onClick(View view) {
				MainActivity.this.roomMenuList.dismiss();
				MainActivity.this.roomChanged(roomListText, ((Integer) view.getTag()).intValue());
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
				roomChanged(roomListText, selectIndex);
			} else {
				if (this.roomDataList.size() == 0) {
					((ScrollView) findViewById(R.id.scrollView1)).setVisibility(View.GONE);
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

	private MyComp populateDeviceButtons(final String paramString) {
		MyComp local16 = new MyComp(getApplicationContext(), paramString) {
			public void doAddAction() {
				MainActivity.this.configureDevice(paramString);
			}

			public void doDeleteAction() {
				new AlertDialog.Builder(MainActivity.this).setTitle("Delete")
						.setMessage("Do you really want to delete ?")
						.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface paramAnonymous2DialogInterface,
									int paramAnonymous2Int) {
								BtLocalDB.getInstance(MainActivity.this).deleteDevice(MainActivity.this.selectedRoom,
										MainActivity.this.selectedDeviceName);
								MainActivity.this.lightsPanel.removeMyView(MainActivity.this.selectedDeviceName);
								MainActivity.this.devicePanel.removeMyView(MainActivity.this.selectedDeviceName);
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
		((LinearLayout) findViewById(R.id.roomContent)).addView(local16);
		DeviceData[] arrayOfDeviceData = BtLocalDB.getInstance(this).getDevices(this.selectedRoom.getAddress());
		local16.expandComp(true);
		if (paramString.equals("Lights"))
			this.lightsPanel = local16;
		else
			this.devicePanel = local16;
		int i = 0;
		for (;;) {
			if (i >= arrayOfDeviceData.length) {
				return local16;
			}
			if (!arrayOfDeviceData[i].isUnknownType()) {
				if ((paramString.equals("Lights")) && (DeviceData.isLightType(arrayOfDeviceData[i].getType()))) {
					addButtonPanel(local16, arrayOfDeviceData[i]);
				} else if ((paramString.equals("Devices"))
						&& (!DeviceData.isLightType(arrayOfDeviceData[i].getType()))) {
					addButtonPanel(local16, arrayOfDeviceData[i]);
				}
			}
			i += 1;

		}
	}

	private MyComp populateGroups() {
		this.groupPanel = new MyComp(getApplicationContext(), "Groups") {
			public void doAddAction() {
				Intent localIntent = new Intent(MainActivity.this.getBaseContext(), AddGroupActivity.class);
				localIntent.putExtra("deviceAddress", MainActivity.this.selectedRoom.getAddress());
				MainActivity.this.startActivityForResult(localIntent, ADDGROUP_CODE);
			}

			public void doDeleteAction() {
				new AlertDialog.Builder(MainActivity.this).setTitle("Delete")
						.setMessage("Do you really want to delete ?")
						.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface paramAnonymous2DialogInterface,
									int paramAnonymous2Int) {
								BtLocalDB.getInstance(MainActivity.this).deleteGroup(
										MainActivity.this.selectedRoom.getAddress(),
										MainActivity.this.selectedGroupName);
								MainActivity.this.groupPanel.removeMyView(MainActivity.this.selectedGroupName);
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
		String[] arrayOfString = BtLocalDB.getInstance(this).getGroups(this.selectedRoom.getAddress());
		int i = 0;
		for (;;) {
			if (i >= arrayOfString.length) {
				return this.groupPanel;
			}
			addGroupButton(arrayOfString[i]);
			i += 1;
		}
	}

	private MyComp populateSchedules() {
		this.schedulePanel = new MyComp(getApplicationContext(), "Schedules") {
			public void doAddAction() {
				Intent localIntent = new Intent(MainActivity.this.getBaseContext(), AddSchedulerActivity.class);
				localIntent.putExtra("deviceAddress", MainActivity.this.selectedRoom.getAddress());
				MainActivity.this.startActivityForResult(localIntent, ADDSCHEDULER_CODE);
			}

			public void doDeleteAction() {
				new AlertDialog.Builder(MainActivity.this).setTitle("Delete")
						.setMessage("Do you really want to delete ?")
						.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface paramAnonymous2DialogInterface,
									int paramAnonymous2Int) {
								try {
									btHwLayer.sendDeleteAlarmCommandToDevice(MainActivity.this.selectedSchedulerId);
									BtLocalDB.getInstance(MainActivity.this).deleteSchedule(
											MainActivity.this.selectedRoom.getAddress(),
											MainActivity.this.selectedSchedulerId);
									MainActivity.this.schedulePanel
											.removeMyView(MainActivity.this.selectedScheduleName);
									showDeleteButton(false);
									return;
								} catch (Exception paramAnonymous2DialogInterfacee) {
									CommonUtils.AlertBox(MainActivity.this, "Delete Scheduler",
											"Not able to send delete schedule(" + MainActivity.this.selectedScheduleName
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
		ArrayList<SchedulerData> scheduleList = BtLocalDB.getInstance(this).getSchedules(this.selectedRoom.getAddress());
		
		for (int i=0; i<scheduleList.size(); i++) {
			SchedulerData localSchedulerData = scheduleList.get(i);
			addScheduleButton(Integer.parseInt(localSchedulerData.getSchedulerId()), localSchedulerData.getName());
		}
		return this.schedulePanel;
	}

	private void addButtonPanel(final MyComp paramMyComp, DeviceData paramDeviceData) {
		final int i = paramDeviceData.getDevId();
		System.out.println("addButtonPanel....id>>>"+i);
		final String str1 = paramDeviceData.getType();
		final String str2 = paramDeviceData.getName();
		paramDeviceData.setStatus(BtLocalDB.getInstance(this).getDeviceStatus(str2));
		final ImageTextButton deviceButton = new ImageTextButton(this);
		deviceButton.setDevice(paramDeviceData);
		deviceButton.setOnLongClickListener(new View.OnLongClickListener() {
			public boolean onLongClick(View paramAnonymousView) {
				paramMyComp.showDeleteButton(true);
				MainActivity.this.selectedDeviceName = str2;
				paramMyComp.selectComp(deviceButton);
				if (DeviceData.isDimmable(str1)) {
					MainActivity.this.controlDevice(deviceButton, str1, i);
				}
				return true;
			}
		});
		deviceButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View paramAnonymousView) {
				paramMyComp.deselectAll();
				MainActivity.this.singleClickButton(i, str1, deviceButton);
			}
		});
		paramMyComp.addMyView(deviceButton);
	}

	private void readAndUpateStatusForRoom(boolean update) {
		System.out.println("isUpdate.inside..readAndUpateStatusForRoom.is false..."+CommonUtils.MAX_NO_DEVICES);
		if( update ) {
			try {
				byte allStatus[] = btHwLayer.readAllStatus();
				if( allStatus == null) {
					CommonUtils.AlertBox(this, "Device count", "No data from device");
					return;
				}
				for(byte ind=1; ind<allStatus.length; ind += 2){
					try {
						byte deviceid = allStatus[ind];
						byte status = allStatus[ind+1];
						System.out.println("GGGGGGGGGGG>>>>>devid="+deviceid+" status="+status);
						 BtLocalDB.getInstance(this).updateDeviceStatus(deviceid, status);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						System.out.println("isUpdate.inside..readAndUpateStatusForRoom.is ="+update);
					}
				}
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
		}
		
		runOnUiThread(new Runnable() {
			public void run() {
				if( MainActivity.this.lightsPanel == null) {
					System.out.println("Notification is received.....i dont know why its coming here....");
					return;
				}
				MainActivity.this.lightsPanel.updateButtonInPanel();
				MainActivity.this.devicePanel.updateButtonInPanel();
				int i = BtLocalDB.getInstance(MainActivity.this).getDevicesOnCount();
				((TextView) findViewById(R.id.onDeviceCount)).setText(""+i);
				try {
					if (MainActivity.this.groupPanel.isReset()) {
						MainActivity.this.groupPanel.updateLiveButtonInPanel();
					}
					if(!btHwLayer.isConnected())
						groupPanel.resetButtonInPanel(true);
					return;
				} catch (Exception localException) {
					CommonUtils.AlertBox(MainActivity.this, "Read Power", "No data from device");
				}
			}
		});
	}

	private void singleClickButton(final int paramInt, String paramString, ImageTextButton paramImageTextButton) {
		BackgroundTaskDialog btdialog = new BackgroundTaskDialog(this) {
			
			@Override
			public Object runTask(Object params) {
				try {
					int readValue = btHwLayer.readCommandToDevice(paramInt);
					System.out.println("ReadValue..." + readValue);
					if (readValue != 0)
						readValue = 0;
					else
						readValue = 9;
					System.out.println("Send ReadValue..paramInt."+paramInt+" value..." + readValue);
					btHwLayer.sendCommandToDevice(paramInt, readValue);
					System.out.println("Send ReadValue..paramIntaaaa."+paramInt+"  byte.."+((byte) paramInt)+" value..." + readValue);
					BtLocalDB.getInstance(MainActivity.this).updateDeviceStatus((byte) paramInt, (byte) readValue);
					readAndUpateStatusForRoom(false);
					return null;
				} catch (Exception paramString1) {
					paramString1.printStackTrace();
					CommonUtils.AlertBox(MainActivity.this, "Read Error", paramString1.getMessage());
				}
				return null;
			}
			
			@Override
			public void finishedTask(Object result) {
				// TODO Auto-generated method stub
				
			}
		};
		
	}

	private void addGroupButton(final String groupName)
	  {
	    final ImageTextButton localImageTextButton = new ImageTextButton(this);
	    localImageTextButton.setText(groupName);
	    localImageTextButton.changeDeviceButtonStyle(0);
	    localImageTextButton.setBackgroundImage(R.drawable.group);
	    localImageTextButton.setOnClickListener(new View.OnClickListener()
	    {
	      public void onClick(View paramAnonymousView)
	      {
	        MainActivity.this.groupPanel.deselectAll();
	        int groudIds[] = BtLocalDB.getInstance(MainActivity.this).getGroupDevices(MainActivity.this.selectedRoom.getAddress(), groupName);
	        boolean groupClicked = MainActivity.this.groupStatusMap.containsKey(groupName);
	        if(groupClicked){
	        	for(int dindex = 0; dindex<groudIds.length; dindex += 2) {
	        		groudIds[dindex+1] = 0;
	        	}
	        }
	        try {
	        	btHwLayer.sendCommandToDevices(groudIds);
				if( !groupClicked ) {
		        	MainActivity.this.groupStatusMap.put(groupName, Boolean.valueOf(true));
		        	localImageTextButton.changeDeviceButtonStyle(1);
		            localImageTextButton.setBackgroundImage(R.drawable.group);
		        } else {
		        	MainActivity.this.groupStatusMap.remove(groupName);
		        	localImageTextButton.changeDeviceButtonStyle(0);
		            localImageTextButton.setBackgroundImage(R.drawable.group);
		        }
				for(int dindex = 0; dindex<groudIds.length; dindex += 2) {
		        	BtLocalDB.getInstance(MainActivity.this).updateDeviceStatus((byte) groudIds[dindex], (byte) groudIds[dindex+1]);
	        	}
		        MainActivity.this.readAndUpateStatusForRoom(false);
			} catch (Exception e) {
				MainActivity.this.groupStatusMap.remove(groupName);
	        	localImageTextButton.changeDeviceButtonStyle(-1);
	            localImageTextButton.setBackgroundImage(R.drawable.group);
	            CommonUtils.AlertBox(MainActivity.this, "Read Error", e.getMessage());
			}
	      }
	    });
	    localImageTextButton.setOnLongClickListener(new View.OnLongClickListener()
	    {
	      public boolean onLongClick(View paramAnonymousView)
	      {
	        MainActivity.this.groupPanel.showDeleteButton(true);
	        MainActivity.this.selectedGroupName = groupName;
	        MainActivity.this.selectedGroupButton = localImageTextButton;
	        MainActivity.this.groupPanel.selectComp(localImageTextButton);
	        return true;
	      }
	    });
	    this.groupPanel.addMyView(localImageTextButton);
	  }

	private void addScheduleButton(final int paramInt, final String paramString) {
		final ImageTextButton localImageTextButton = new ImageTextButton(this);
		localImageTextButton.setBackgroundImage(R.drawable.scheduler);
		localImageTextButton.setText(paramString);
		localImageTextButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View paramAnonymousView) {
				MainActivity.this.schedulePanel.deselectAll();
			}
		});
		localImageTextButton.setOnLongClickListener(new View.OnLongClickListener() {
			public boolean onLongClick(View paramAnonymousView) {
				MainActivity.this.schedulePanel.showDeleteButton(true);
				MainActivity.this.selectedSchedulerId = paramInt;
				MainActivity.this.selectedScheduleName = paramString;
				MainActivity.this.selectedScheduleButton = localImageTextButton;
				MainActivity.this.schedulePanel.selectComp(localImageTextButton);
				return true;
			}
		});
		this.schedulePanel.addMyView(localImageTextButton);
	}

	private void configureDevice(String paramString) {
		Intent localIntent = new Intent(getBaseContext(), AddDeviceActivity.class);
		localIntent.putExtra("deviceAddress", this.selectedRoom.getAddress());
		localIntent.putExtra("tabName", paramString);
		startActivityForResult(localIntent, ADDDEVICE_CODE);
	}

	private void controlDevice(final ImageTextButton paramImageTextButton, final String paramString,
			final int paramInt) {
		Object localObject = LayoutInflater.from(this).inflate(R.layout.devicecontroller, null);
		AlertDialog.Builder localBuilder = new AlertDialog.Builder(this);
		localBuilder.setView((View) localObject);
		localObject = (SeekBar) ((View) localObject).findViewById(R.id.seekBar1);
		try {
			int i = btHwLayer.readCommandToDevice(paramInt);
			if (i != -1) {
				((SeekBar) localObject).setProgress(i * 10);
			}
			((SeekBar) localObject).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				public void onProgressChanged(SeekBar paramAnonymousSeekBar, int paramAnonymousInt,
						boolean paramAnonymousBoolean) {
					try {
						int i = paramAnonymousSeekBar.getProgress() / 10;
						paramAnonymousInt = i;
						if (i == 10) {
							paramAnonymousInt = 9;
						}
						btHwLayer.sendCommandToDevice(paramInt, paramAnonymousInt);
						paramImageTextButton.changeDeviceButtonStyle(paramString, paramAnonymousInt);
						return;
					} catch (Exception paramAnonymousSeekBare) {
						CommonUtils.AlertBox(MainActivity.this, "Error",
								"Sending cmd to Device:" + paramAnonymousSeekBare.getMessage());
					}
				}

				public void onStartTrackingTouch(SeekBar paramAnonymousSeekBar) {
				}

				public void onStopTrackingTouch(SeekBar paramAnonymousSeekBar) {
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
		if (!btHwLayer.makeBtEnabled()) {
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
		updateWithRealtime();
		relayout();
	}

	private void updateWithRealtime() {
		BackgroundTaskDialog task = new BackgroundTaskDialog(this) {
			
			@Override
			public Object runTask(Object params) {
				boolean isUpdate = false;
				btHwLayer.setConnectionListener(MainActivity.this);
				String error = btHwLayer.initDevice(selectedRoom.getAddress(), selectedRoom.getIpAddress());
				if( error == null) {
					try {
						int numberOfDevices = btHwLayer.getNumberOfDevices();
						CommonUtils.setNumMaxNoDevices(numberOfDevices);
						isUpdate = true;
					} catch (Exception e) {
						isUpdate = false;
						CommonUtils.AlertBox(MainActivity.this, "Connection", e.getMessage());
					}
				} else {
					CommonUtils.AlertBox(MainActivity.this, "Connection", error);
					connectionLost();
					return null;
				}
				readAndUpateStatusForRoom(isUpdate);
				return null;
			}
			
			@Override
			public void finishedTask(Object result) {
				// TODO Auto-generated method stub
				
			}
		};
	}

	private void roomChanged(TextView paramTextView, int paramInt) {
		if ((this.selectedRoom != null)
				&& (this.selectedRoom.getName().equals(((RoomData) this.roomDataList.get(paramInt)).getName()))) {
			System.out.println("reutrn null");
			return;
		}
		this.selectedRoom = ((RoomData) this.roomDataList.get(paramInt));
		paramTextView.setText(this.selectedRoom.getName());
		System.out.println("processing calling pageforselectedroom " + selectedRoom.getName());
		
		System.out.println("processing rooms isrgb="+selectedRoom.isRGBType());
		BtLocalDB.getInstance(this).setLastSelectedRoom(paramInt);
		BtLocalDB.getInstance(this).clearDeviceStatus();
		if( selectedRoom.isRGBType()){
			rgbController.setRGBView(selectedRoom);
		} else {
			((ScrollView) findViewById(R.id.scrollView1)).setVisibility(View.VISIBLE);
			((LinearLayout) findViewById(R.id.rgbPanel)).setVisibility(View.GONE);
			((LinearLayout) findViewById(R.id.emptydevicepanel)).setVisibility(View.GONE);
			populatePageForSelectedRoom();
		}
		
		
	}

	public void putInOffline() {
		runOnUiThread(new Runnable() {
			public void run() {
				System.out.println("Update Status");
				if( MainActivity.this.lightsPanel == null) {
					//rgbController.putOffLine(true);
				} else {
					MainActivity.this.lightsPanel.resetButtonInPanel(false);
					MainActivity.this.devicePanel.resetButtonInPanel(false);
					MainActivity.this.groupPanel.resetButtonInPanel(true);
				}
			}
		});
	}

	public void relayout() {
		((ImageView) findViewById(R.id.overlay)).post(new Runnable() {
			public void run() {
				MainActivity.this.schedulePanel.relayout();
				MainActivity.this.groupPanel.relayout();
				MainActivity.this.devicePanel.relayout();
				MainActivity.this.lightsPanel.relayout();
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
			if (deletedRoomStr.isEmpty()) {
				return;
			}
			String deletedRooms[] = deletedRoomStr.split("#");
			int numberOfRoomsDeleted = deletedRooms.length;
			if (numberOfRoomsDeleted > 0) {
				prepareRoomListMenu("", true);
				return;
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
				addButtonPanel(this.lightsPanel, deviceData);
			} else {
				addButtonPanel(this.devicePanel, deviceData);
			}
			return;
		} else if ((requestCode == ADDGROUP_CODE) && (resultCode == RUSULTCODE_SAVE)) {
			addGroupButton(resultIntent.getExtras().getString("name"));
			return;
		} else if ((requestCode == ADDSCHEDULER_CODE) && (resultCode == RUSULTCODE_SAVE)) {
			requestCode = resultIntent.getExtras().getInt("scheduleid");
			String name = resultIntent.getExtras().getString("name");
			if (resultIntent.getExtras().containsKey("error")) {
				CommonUtils.AlertBox(this, "Scheduler:" + name, resultIntent.getExtras().getString("error"));
				return;
			}
			addScheduleButton(requestCode, name);
		}
	}
	
	public void notificationReceived(byte[] paramArrayOfByte)
	  {
	    int i = 0;
	    for (;;)
	    {
	      if (i >= paramArrayOfByte.length / 2) {
	        return;
	      }
	      byte b1 = paramArrayOfByte[(i * 2)];
	      byte b2 = paramArrayOfByte[(i * 2 + 1)];
	      BtLocalDB.getInstance(this).updateDeviceStatus(b1, b2);
	      readAndUpateStatusForRoom(false);
	      i += 1;
	    }
	  }

	public void connectionStarted() {
		//putOffLine(false);
		System.out.println("ConnectionStarted.....need to be defined");
	}
	public void connectionLost() {
		runOnUiThread(new Runnable() {
			public void run() {
				if( MainActivity.this.lightsPanel != null) {
					MainActivity.this.lightsPanel.resetButtonInPanel(false);
					MainActivity.this.devicePanel.resetButtonInPanel(false);
					MainActivity.this.groupPanel.resetButtonInPanel(true);
				}
			}
		});
		
	}
	
	public void setConnectionModeIcon() {
		ImageButton aboutButton = (ImageButton)findViewById(R.id.aboutButton);
		if ( btHwLayer.isWifiEnabled() )
			aboutButton.setImageResource(R.drawable.wifi);
		else
			aboutButton.setImageResource(R.drawable.bt);
	}
}

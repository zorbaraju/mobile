package com.zorba.bt.app.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.zorba.bt.app.CommonUtils;
import com.zorba.bt.app.dao.DeviceData;
import com.zorba.bt.app.dao.RoomData;
import com.zorba.bt.app.dao.SchedulerData;

public class BtLocalDB {
	private static BtLocalDB instance = null;
	private static int version = 1;
	private SharedPreferences dbInfo = null;
	private HashMap<Byte, Byte> deviceStatusMap = new HashMap<Byte, Byte>();
	private HashMap<Byte, Byte> devicePrevOnStatusMap = new HashMap<Byte, Byte>();
	boolean isStoreClean = false;
	private String currentDeviceName = "";

	private BtLocalDB(Context var1) {
		this.dbInfo = var1.getSharedPreferences("BtHome", 0);
	}

	public static BtLocalDB getInstance(Context var0) {
		if (instance == null) {
			instance = new BtLocalDB(var0);
		}

		return instance;
	}
	
	public String getConfiguration() {
		StringBuffer buf = new StringBuffer("Rooms configuration<br/>");
		ArrayList<RoomData> roomList = getRoomList();
		roomList.remove(0);
		for(RoomData room: roomList) {
			buf.append("Room : "+room.getName()+"<br/>Device List<br/>");
			ArrayList<DeviceData> deviceList = getDevices(room.getDeviceName(), null);
			for(DeviceData deviceData: deviceList) {
				if( deviceData.getName().startsWith("Unknown "))
					continue;
				buf.append("Id:"+deviceData.getDevId()+" Name:"+deviceData.getName()+"<br/>");
			}
			buf.append("<br/>");
		}
		return buf.toString();
	}

	public void addRoom(RoomData roomData) {
		String roomstr = this.dbInfo.getString("BtList", "");
		String rgb = roomData.isRGBType() ? "1" : "0";
		String room = roomData.getAddress() + "#" + roomData.getName() + "#" + rgb + "#" + roomData.getIpAddress() + "#"
				+ roomData.getSSID() + "#" + roomData.getDeviceName();
		if (!roomstr.equals("")) {
			roomstr = roomstr + "#" + room;
		} else
			roomstr = room;

		String devroomstr = "";

		for (int index = 0; index < CommonUtils.getMaxNoDevices(); ++index) {
			String unknowroom = "Unknown " + (index + 1) + "#" + "Unknown" + "#" + "10";
			if (index == 0) {
				devroomstr = unknowroom;
			} else {
				devroomstr = devroomstr + "#" + unknowroom;
			}
		}

		Editor var6 = this.dbInfo.edit();
		var6.putString("BtList", roomstr);
		var6.putString("Room" + roomData.getDeviceName(), devroomstr);
		var6.commit();
	}

	public void deleteRoom(String var1) {
		String roomstr = this.dbInfo.getString("BtList", "");
		String removedroom = "";
		String newroomstr = "";
		String deviceName = "";
		if (!roomstr.isEmpty()) {
			String[] roomstrarr = roomstr.split("#");
			for (int index = 0; index < roomstrarr.length; index += 6) {
				if (roomstrarr[index + 1].equals(var1)) {
					removedroom = roomstrarr[index];
					deviceName = roomstrarr[index+5];
				} else {
					String room = roomstrarr[index] + "#" + roomstrarr[index + 1] + "#" + roomstrarr[index + 2] + "#" + roomstrarr[index + 3] + "#" + roomstrarr[index + 4] + "#" + roomstrarr[index + 5];
					if (newroomstr.equals("")) {
						newroomstr = room;
					} else {
						newroomstr = newroomstr + "#" + room;
					}
				}
			}
		}

		Editor editor = this.dbInfo.edit();
		editor.putString("BtList", newroomstr);
		editor.remove("Room" + removedroom);
		editor.remove("Group" + deviceName);
		editor.commit();
	}

	public void cleanDB() {
		int var1 = this.dbInfo.getInt("version", -1);
		if (this.isStoreClean || var1 != version) {
			Editor var2 = this.dbInfo.edit();
			var2.clear();
			var2.putInt("version", version);
			var2.commit();
		}

	}

	public void clearDeviceStatus() {
		this.deviceStatusMap.clear();
		this.devicePrevOnStatusMap.clear();
	}

	public void deleteDevice(String deviceName, String var2) {
		String[] var4 = this.dbInfo.getString("Room" + deviceName, "").split("#");
		
		int var3;
		for (var3 = 0; var3 < var4.length; var3 += 3) {
			if (var4[var3].equals(var2)) {
				var4[var3] = "Unknown " + (var3 + 1);
				var4[var3 + 1] = "Unknown";
				var4[var3 + 2] = "10";
				break;
			}
		}

		var2 = "";

		for (var3 = 0; var3 < var4.length; ++var3) {
			if (var3 == 0) {
				var2 = var4[var3];
			} else {
				var2 = var2 + "#" + var4[var3];
			}
		}

		Editor var5 = this.dbInfo.edit();
		var5.putString("Room" + deviceName, var2);
		var5.commit();
	}

	public void deleteGroup(String deviceName, String grpName) {
		String[] grps = this.dbInfo.getString("Group" + deviceName, "").split("#");
		String updatedGroups = "";
		int numGrps = grps.length;
		for (int gindex = 0; gindex < numGrps; gindex++) {
			String grp = grps[gindex];
			if (!grpName.equals(grp)) {
				if (updatedGroups.isEmpty()) {
					updatedGroups = grp;
				} else {
					updatedGroups += "#" + grp;
				}
			}
		}

		Editor editor = this.dbInfo.edit();
		editor.putString("Group" + deviceName, updatedGroups);
		editor.remove("Group" + deviceName + grpName);
		editor.commit();
		System.out.println("Delete Groups..."+deviceName+">>"+updatedGroups);
		System.out.println("Delete Groups.datil.."+"Group" + deviceName + grpName);
	}

	public void deleteSchedule(String var1, int var2) {
		String var8 = this.dbInfo.getString("Schedule" + var1, "");
		String var5 = "";
		String var7 = "";
		String var6 = var5;
		String var4 = var7;
		if (!var8.isEmpty()) {
			String[] var10 = var8.split("#");
			int var3 = 0;

			for (var4 = var7; var3 < var10.length; var3 += 2) {
				if (Integer.parseInt(var10[var3]) == var2) {
					var4 = var10[var3];
				} else {
					var6 = var10[var3] + "#" + var10[var3 + 1];
					if (var5.equals("")) {
						var5 = var6;
					} else {
						var5 = var5 + "#" + var6;
					}
				}
			}

			var6 = var5;
		}

		Editor var9 = this.dbInfo.edit();
		var9.putString("Schedule" + var1, var6);
		var9.remove("Schedule" + var1 + var4 + var1);
		var9.commit();
	}

	public int getDevicePrevOnStatus(Byte devid) {
		int status = -1;
		if (this.devicePrevOnStatusMap.containsKey(devid)) {
			status = this.devicePrevOnStatusMap.get(devid);
		}
		return status;
	}
	
	public int getDeviceStatus(Byte devid) {
		int status = -1;
		if (this.deviceStatusMap.containsKey(devid)) {
			status = this.deviceStatusMap.get(devid);
		}
		return status;
	}

	public ArrayList<DeviceData> getDevices(String deviceName, String switchName) {
		currentDeviceName  = deviceName;
		String[] var3 = this.dbInfo.getString("Room" + deviceName, "").split("#");
		ArrayList<DeviceData> list = new ArrayList<DeviceData>();
		for (int var2 = 0; var2 < var3.length / 3; ++var2) {
			DeviceData deviceData = new DeviceData(var2 + 1, var3[var2 * 3], var3[var2 * 3 + 1], var3[var2 * 3 + 2], -1);
			if( switchName == null || switchName.equals(deviceData.getName()))
			list.add(deviceData);
		}
		return list;
	}

	public int getDevicesOnCount() {
		int count = 0;
		Iterator<?> it = this.deviceStatusMap.keySet().iterator();
		while (it.hasNext()) {
			byte devid = (Byte) it.next();
			if( isDeviceIdExist(currentDeviceName, devid)) {
				if ( this.deviceStatusMap.get(devid) > 0) {
					count++;
				}
			}
		}
		return count;
	}

	public int[] getGroupDevices(String deviceName, String grpname) {
		String deviceList = this.dbInfo.getString("Group" + deviceName + grpname, "").trim();
		System.out.println("grpname..."+grpname+" device.."+deviceList);
		int[] var4 = new int[0];
		if (!deviceList.isEmpty()) {
			String[] var5 = deviceList.split("#");
			var4 = new int[var5.length];

			for (int var3 = 0; var3 < var5.length; ++var3) {
				var4[var3] = Integer.parseInt(var5[var3]);
			}
		}

		return var4;
	}

	public String[] getGroups(String var1) {
		String var2 = this.dbInfo.getString("Group" + var1, "");
		String[] var3 = new String[0];
		if (!var2.isEmpty()) {
			var3 = var2.split("#");
		}
		System.out.println("Groups..."+var1+">>"+var2);
		return var3;
	}

	public int getLastSelectedRoom() {
		return this.dbInfo.getInt("lastVisitedRoom", 0);
	}

	public int getNewSchedulerId(String var1) {
		int var3 = 1;
		byte var4 = 1;
		var1 = this.dbInfo.getString("Schedule" + var1, "");
		if (!var1.isEmpty()) {
			String[] var5 = var1.split("#");
			HashMap<String, String> var6 = new HashMap<String, String>();

			int var2;
			for (var2 = 0; var2 < var5.length; var2 += 2) {
				var6.put(var5[var2], var5[var2 + 1]);
			}

			var2 = var4;

			while (true) {
				var3 = var2;
				if (!var6.containsKey("" + var2)) {
					break;
				}

				++var2;
			}
		}

		return var3;
	}

	public ArrayList<RoomData> getRoomList() {
		String var3 = this.dbInfo.getString("BtList", "");
		ArrayList<RoomData> var2 = new ArrayList<RoomData>();
		var2.add(new RoomData("GGYYGGYYGYY", "GGYYGGYYGYY", false, "", "FFFF", "DDDD"));
		if (!var3.isEmpty()) {
			String[] var4 = var3.split("#");

			for (int var1 = 0; var1 < var4.length; var1 += 6) {
				var2.add(new RoomData(var4[var1], var4[var1 + 1], var4[var1 + 2].equals("1"), var4[var1 + 3], var4[var1 + 4], var4[var1 + 5]));
			}
		}
		return var2;
	}

	public ArrayList<SchedulerData> getSchedules(String deviceName, String schedulerName) {
		String var3 = this.dbInfo.getString("Schedule" + deviceName, "");
		System.out.println("schedule..."+deviceName+"..."+var3 +" schedulerName="+schedulerName);
		ArrayList<SchedulerData> var4 = new ArrayList<SchedulerData>();
		if (!var3.isEmpty()) {
			String[] var5 = var3.split("#");

			for (int index = 0; index < var5.length; index += 2) {
				String schedid = var5[index];
				String name = var5[index+1];
				if( schedulerName == null || schedulerName.equals(name)) {
					String detail = this.dbInfo.getString("Schedule" + deviceName+name, "");
					String scheddetailarr[] = detail.split("#");
					int arrindex = 0;
					System.out.println("detail..."+detail+"<"+scheddetailarr[arrindex]+">");
					if( detail.isEmpty())
						continue;
					int repeatType = Integer.parseInt(scheddetailarr[arrindex++]);
					int repeatValue = Integer.parseInt(scheddetailarr[arrindex++]);
					int hr = Integer.parseInt(scheddetailarr[arrindex++]);
					int min = Integer.parseInt(scheddetailarr[arrindex++]);
					int devidstatus[] = new int[scheddetailarr.length-4];
					System.out.println("scheddetailarr.length.."+scheddetailarr.length);
					System.out.println("....."+devidstatus.length);
					for(int dindex=0; dindex<devidstatus.length; dindex+=2) {
						System.out.println("..1..."+devidstatus.length+" dindex="+dindex+" arrindex="+arrindex);
						devidstatus[dindex] = Integer.parseInt(scheddetailarr[arrindex++]);
						System.out.println("..2..."+devidstatus.length);
						devidstatus[dindex+1] = Integer.parseInt(scheddetailarr[arrindex++]);
					}
					var4.add(new SchedulerData(schedid, name, devidstatus,repeatType, repeatValue, hr, min));
				}
			}
		}

		return var4;
	}

	public boolean isDeviceNameExist(String deviceName, String var2) {
		String[] var5 = this.dbInfo.getString("Room" + deviceName, "").split("#");
		for (int index=0; index < var5.length; index += 3) {
			if (var2.equals(var5[index])) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isDeviceIdExist(String deviceName, int devid) {
		String[] var5 = this.dbInfo.getString("Room" + deviceName, "").split("#");
		return !var5[(devid-1)*3].startsWith("Unknown ");
	}

	public boolean isGroupNameExist(String var1, String var2) {
		boolean var5 = false;
		String[] var6 = this.getGroups(var1);
		int var4 = var6.length;

		for (int var3 = 0; var3 < var4; ++var3) {
			if (var2.equals(var6[var3])) {
				var5 = true;
				break;
			}
		}

		return var5;
	}

	public boolean isRoomExists(String devicename) {
		String roomstr = this.dbInfo.getString("BtList", "");
		if( roomstr.isEmpty())
			return false;
		String[] roomstrarr = roomstr.split("#");
		for(int index=0; index<roomstrarr.length; index+=6) {
			String dname = roomstrarr[index+5];
			if (dname.equals(devicename))
				return true;
		}
		return false;
	}

	public boolean isRoomNameExist(String roomname) {
		String roomstr = this.dbInfo.getString("BtList", "");
		boolean isexist = false;
		if (!roomstr.isEmpty()) {
			String[] roomstrarr = roomstr.split("#");

			for (int index = 0; index < roomstrarr.length; index += 6) {
				if (roomname.equals(roomstrarr[index + 1])) {
					isexist = true;
					break;
				}
			}
		}
		return isexist;
	}

	public boolean isSchedulerNameExist(String var1, String var2) {
		var1 = this.dbInfo.getString("Schedule" + var1, "");
		boolean var4;
		if (!var1.isEmpty()) {
			String[] var5 = var1.split("#");

			for (int var3 = 0; var3 < var5.length; var3 += 2) {
				if (var5[var3 + 1].equals(var2)) {
					var4 = true;
					return var4;
				}
			}
		}

		var4 = false;
		return var4;
	}

	public void saveGroup(String deviceName, String grpName, String grpdetail, boolean isNew) {
		String groups = this.dbInfo.getString("Group" + deviceName, "");
		if (groups.indexOf(grpName) == -1) {
			if (groups.isEmpty()) {
				groups = grpName;
			} else {
				groups += "#" + grpName;
			}
		}
		Editor editor = this.dbInfo.edit();
		editor.putString("Group" + deviceName, groups);
		editor.putString("Group" + deviceName + grpName, grpdetail);
		editor.commit();
		System.out.println("Save Groups..."+deviceName+">>"+groups);
		System.out.println("Save Groups.detail.."+deviceName+grpName+">>"+grpdetail);
	}

	public void saveSchedule(String deviceName, boolean isNew, int schedid, int repeatType, int repeatValue, String schedname, int hr, int min,
			DeviceData[] devData) {
		String schediddetail = this.dbInfo.getString("Schedule" + deviceName, "");
		if (schediddetail.indexOf(schedname) == -1) {
			if (schediddetail.isEmpty()) {
				schediddetail = schedid + "#" + schedname;
			} else {
				schediddetail += "#" + schedid + "#" + schedname;
			}
		}

		String detail = repeatType + "#" + repeatValue + "#" + hr + "#" + min;

		for (int index = 0; index < devData.length; ++index) {
			detail = detail + "#"+ devData[index].getDevId() + "#" + devData[index].getStatus();
		}

		Editor var11 = this.dbInfo.edit();
		var11.putString("Schedule" + deviceName, schediddetail);
		var11.putString("Schedule" + deviceName + schedname, detail);
		System.out.println("save schedule..."+deviceName+"..."+schediddetail);
		System.out.println("save schedule..."+deviceName+schedname+"..."+detail);
		var11.commit();
	}

	public void setLastSelectedRoom(int var1) {
		Editor var2 = this.dbInfo.edit();
		var2.putInt("lastVisitedRoom", var1);
		var2.commit();
	}

	public void updateDevice(String deviceName, DeviceData var2) {
		deleteDevice(deviceName, var2.getName());
		String var4 = this.dbInfo.getString("Room" + deviceName, "");
		String[] var5 = var4.split("#");
		int var3 = var2.getDevId() - 1;
		var5[var3 * 3] = var2.getName();
		var5[var3 * 3 + 1] = var2.getType();
		var5[var3 * 3 + 2] = var2.getPower();
		var3 = 0;

		String var6;
		for (var6 = var4; var3 < var5.length; var3 += 3) {
			if (var3 == 0) {
				var6 = var5[var3] + "#" + var5[var3 + 1] + "#" + var5[var3 + 2];
			} else {
				var6 = var6 + "#" + var5[var3] + "#" + var5[var3 + 1] + "#" + var5[var3 + 2];
			}
		}

		Editor var7 = this.dbInfo.edit();
		var7.putString("Room" + deviceName, var6);
		var7.commit();
	}

	public void updateDevicePrevOnStatus(byte devid, byte status) {
		this.devicePrevOnStatusMap.put(devid, status);
	}
	
	public void updateDeviceStatus(byte devid, byte status) {
		this.deviceStatusMap.put(devid, status);
	}
	
	public boolean isInvEnabled(String devNameplusDevId) {
		boolean isEnabled = this.dbInfo.getBoolean(devNameplusDevId, false);
		return isEnabled;
	}
	public void setInvEnabled(String devNameplusDevId, boolean isEnabled) {
		Editor edit = this.dbInfo.edit();
		edit.putBoolean(devNameplusDevId, isEnabled);
		edit.commit();
	}
	
	public void setDevicePwd(String devPwd) {
		Editor edit = this.dbInfo.edit();
		edit.putString("devicepwd", devPwd);
		edit.commit();
		System.out.println("Devicepwd..."+devPwd);
	}
	
	public String getDevicePwd() {
		return this.dbInfo.getString("devicepwd", "");
	}

	public void setEmailId(String emailid) {
		Editor edit = this.dbInfo.edit();
		edit.putString("emailid", emailid);
		edit.commit();
		System.out.println("emailid..."+emailid);
	}
	
	public void setUserType(boolean isMaster) {
		Editor edit = this.dbInfo.edit();
		edit.putBoolean("ismaster", isMaster);
		edit.commit();
		System.out.println("ismaster..."+isMaster);
	}
	
	public boolean isMasterUser() {
		return this.dbInfo.getBoolean("ismaster", false);
	}
}

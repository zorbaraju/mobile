package com.zorba.bt.app.dao;

public class RoomData {
	private String address = "";
	private String name = "";
	private boolean isRGB = false;
	private String ipaddress = "";
	private String deviceName = "";
	private String ssid = "";

	public RoomData(String macaddress, String name, boolean isRGB, String ipaddress, String ssid, String devname) {
		this.name = name;
		this.address = macaddress;
		this.isRGB = isRGB;
		this.ipaddress = ipaddress;
		this.deviceName = devname;
		this.ssid = ssid;
	}

	public String getAddress() {
		return this.address;
	}

	public String getDeviceName() {
		return this.deviceName;
	}

	public String getSSID() {
		return this.ssid;
	}

	public String getName() {
		return this.name;
	}

	public boolean isRGBType() {
		return isRGB;
	}

	public String getIpAddress() {
		return ipaddress;
	}

	public String toString() {
		return this.name;
	}
}

package com.zorba.bt.app;

import android.content.Context;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DiscoveryRoom extends LinearLayout {
   String devAddress = null;
   TextView nameText = null;
   boolean isRGB = false;
   String ssid = null;

   public DiscoveryRoom(Context context, String deviceAddress, String deviceName) {
	   this(context, deviceAddress, deviceName, null);
   }
   public DiscoveryRoom(Context context, String deviceAddress, String deviceName, String ssid) {
      super(context);
      this.devAddress = deviceAddress;
      this.ssid = ssid;
      ((LayoutInflater)context.getSystemService("layout_inflater")).inflate(R.layout.discoveryroom, this);
      this.nameText = (TextView)this.findViewById(R.id.devicename);
      //nameText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(12)});
      this.nameText.setText(deviceName);
      
      this.isRGB = deviceName.endsWith("_RGB");
   }
  
   public String getDeviceAddress() {
      return this.devAddress;
   }

   public String getDeviceName() {
      return this.nameText.getText().toString();
   }
   
   public String getSSID() {
	   return this.ssid;
   }

   public String getRoomName() {
      return ((TextView)this.findViewById(R.id.roomname)).getText().toString();
   }
   
   public boolean isRGBType() {
	   return this.isRGB;
   }
   
   public String toString() {
	   return this.nameText.getText().toString();
   }
}

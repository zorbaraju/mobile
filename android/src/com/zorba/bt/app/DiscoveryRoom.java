package com.zorba.bt.app;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DiscoveryRoom extends LinearLayout {
   String devAddress = null;
   BluetoothDevice device = null;
   TextView nameText = null;
   boolean isRGB = false;

   public DiscoveryRoom(Context var1, String var2, BluetoothDevice var3) {
      super(var1);
      this.devAddress = var2;
      this.device = var3;
      ((LayoutInflater)var1.getSystemService("layout_inflater")).inflate(R.layout.discoveryroom, this);
      this.nameText = (TextView)this.findViewById(R.id.devicename);
      this.nameText.setText(var3.getName());
      this.isRGB = var3.getName().endsWith("_RGB");
   }

   public BluetoothDevice getDevice() {
      return this.device;
   }

   public String getDeviceAddress() {
      return this.devAddress;
   }

   public String getDeviceName() {
      return this.nameText.getText().toString();
   }

   public String getRoomName() {
      return ((TextView)this.findViewById(R.id.roomname)).getText().toString();
   }
   
   public boolean isRGBType() {
	   return this.isRGB;
   }
   
}

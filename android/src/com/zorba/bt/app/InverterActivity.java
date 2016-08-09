package com.zorba.bt.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.zorba.bt.app.dao.DeviceData;
import com.zorba.bt.app.dao.RoomData;
import com.zorba.bt.app.db.BtLocalDB;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

public class InverterActivity extends ZorbaActivity {
	HashMap<String,SelectComp> maps = new HashMap<String, SelectComp>();
   protected void onCreate(Bundle var1) {
      super.onCreate(var1);
      this.setContentView(R.layout.inverter);
      LinearLayout devicesLayout = (LinearLayout)this.findViewById(R.id.invRoomContent);
      ArrayList<RoomData> roomList = BtLocalDB.getInstance(this).getRoomList();
      roomList.remove(0);
      System.out.println("roomList..."+roomList.size()+":");
      for(RoomData room:roomList) {
    	  System.out.println("roomname..."+room.getName());
          
    	  ArrayList<DeviceData> deviceList = BtLocalDB.getInstance(this).getDevices(room.getDeviceName(), null);
          int numDevices = deviceList.size();
          System.out.println("numDevices..."+numDevices);
          TextView roomNameText = new TextView(getApplicationContext());
          roomNameText.setText("Room: " +room.getName());
          roomNameText.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
          //roomNameText.setGravity(Gravity.CENTER);
          roomNameText.setTextColor(Color.WHITE);
          roomNameText.setTextSize(20);
          devicesLayout.addView(roomNameText);
          roomNameText.setPadding(0, 20, 0, 0);
          if( deviceList.size() == 0) {
        	  TextView emptyDeviceText = new TextView(getApplicationContext());
        	  emptyDeviceText.setText("No devices configured");
        	  emptyDeviceText.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        	  emptyDeviceText.setTextColor(Color.WHITE);
        	  emptyDeviceText.setGravity(Gravity.CENTER);
        	  devicesLayout.addView(emptyDeviceText);
          } else {
        	  for(DeviceData device:deviceList) {
    	    	  if(!device.isUnknownType()) {
    	            SelectComp comp = new SelectComp(this, device);
    	            comp.setVisibleController(false);
    	            comp.setId(device.getDevId());
    	            devicesLayout.addView(comp);
    	            String key = room.getDeviceName()+device.getDevId();
    	            boolean isEnabled = BtLocalDB.getInstance(this).isInvEnabled(key);
    	            comp.setSelected(isEnabled);
    	            maps.put(key, comp);
    	            System.out.println("Device..."+device.getDevId()+":"+device.getName());
    	         }
    	    	 System.out.println("devid..."+device.getDevId());
              }
          }
          
      }
   }
   
   @Override
   public void onBackPressed() {
	   Iterator<String> it = maps.keySet().iterator();
	   while(it.hasNext()) {
		   String key = it.next();
		   SelectComp comp = maps.get(key);
		   BtLocalDB.getInstance(this).setInvEnabled(key, comp.isSelected());
	   }
	   super.onBackPressed();
   }
}

package com.zorba.bt.app;

import java.util.ArrayList;

import com.zorba.bt.app.bluetooth.BtHwLayer;
import com.zorba.bt.app.dao.DeviceData;
import com.zorba.bt.app.db.BtLocalDB;

import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;

public class AddDeviceActivity extends ZorbaActivity {
   String deviceName = null;
   String tabName = "Lights";
   CheckBox isdimmable = null;
   String editDeviceName = null;
   
   private String[] getUnusedDeviceIds() {
      ArrayList<DeviceData> deviceList = BtLocalDB.getInstance(this).getDevices(this.deviceName, null);
      ArrayList<Integer> var2 = new ArrayList<Integer>();

      int var1;
      int numdevices = deviceList.size();
      for(var1 = 0; var1 < numdevices; ++var1) {
    	  DeviceData device = deviceList.get(var1);
         if(device.isUnknownType()) {
            var2.add(Integer.valueOf(device.getDevId()));
         }
      }

      String[] var4 = new String[var2.size()];

      for(var1 = 0; var1 < var4.length; ++var1) {
         var4[var1] = "" + var2.get(var1);
      }

      return var4;
   }

   private void initListeners() {
      ((ImageButton)this.findViewById(R.id.cancel)).setOnClickListener(new OnClickListener() {
         public void onClick(View var1) {
            AddDeviceActivity.this.finish();
         }
      });
      ((ImageButton)this.findViewById(R.id.save)).setOnClickListener(new OnClickListener() {
         public void onClick(View var1) {
            AddDeviceActivity.this.saveDevice();
         }
      });
      isdimmable.setOnCheckedChangeListener(new OnCheckedChangeListener() {
		
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			changeDevicePopup();
		}
	});
   }

   private void saveDevice() {
      EditText var3 = (EditText)this.findViewById(R.id.deviceNameInput);
      MyPopupDialog var2 = (MyPopupDialog)this.findViewById(R.id.deviceTypeList);
      MyListMenu var4 = (MyListMenu)this.findViewById(R.id.deviceid);
      EditText var5 = (EditText)this.findViewById(R.id.powerinwatts);
      String var8 = var2.getText();
      String var9 = CommonUtils.isValidName(this, var3.getText().toString());
      if(var9 != null) {
    	 boolean isNew = editDeviceName==null;
         if(editDeviceName == null && com.zorba.bt.app.db.BtLocalDB.getInstance(this.getApplication()).isDeviceNameExist(this.deviceName, var9)) {
            CommonUtils.AlertBox(this, "Already exist", "Name is exist already");
         } else {
        	 ArrayList<DeviceData> deviceList = com.zorba.bt.app.db.BtLocalDB.getInstance(this).getDevices(this.deviceName, null);
            if(var4.getText().isEmpty()) {
               CommonUtils.AlertBox(this, "Device Limit", "Maximum of " + deviceList.size() + " devices can only be added");
            } else {
               String var12 = var5.getText().toString().trim();
               if(var12.isEmpty()) {
                  CommonUtils.AlertBox(this, "No power mentioned", "You may not be able to see power consumption used by this device");
               }

               int var1 = Integer.parseInt(var4.getText());
               DeviceData var10 = new DeviceData(var1, var9, var8, var12, -1);

               try {
                  BtHwLayer.getInstance(this).setSwitchType((byte)var1, isdimmable.isChecked(), false, (byte)var4.getSelectedItemPosition());
               } catch (Exception var7) {
                  CommonUtils.AlertBox(this, "Setting device type", "Setting device type is failed");
                  return;
               }
               
               try {
                   BtHwLayer.getInstance(this).getSwitchTypes();
                } catch (Exception var7) {
                   CommonUtils.AlertBox(this, "getting device type", "Getting device types is failed");
                }

               com.zorba.bt.app.db.BtLocalDB.getInstance(this).updateDevice(this.deviceName, var10);
               Intent var11 = new Intent();
               var11.putExtra("name", var9);
               var11.putExtra("type", var8);
               var11.putExtra("index", var1);
               var11.putExtra("power", var12);
               var11.putExtra("isnew", isNew);
               this.setResult(1, var11);
               this.finish();
            }
         }
      }

   }

   protected void onCreate(Bundle var1) {
      super.onCreate(var1);
      this.setContentView(R.layout.adddevicelayout);
      var1 = this.getIntent().getExtras();
      this.deviceName = var1.getString("deviceName");
      tabName = var1.getString("tabName");
      EditText deviceNameText = (EditText)this.findViewById(R.id.deviceNameInput);
      deviceNameText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(12)});
      TextView titleText = (TextView)this.findViewById(R.id.title);
      TextView var6 = (TextView)this.findViewById(R.id.devicename);
      isdimmable = (CheckBox)this.findViewById(R.id.isdimmable);
      TextView deviceTypeLabel = (TextView)this.findViewById(R.id.devicetype);
      ((RelativeLayout)findViewById(R.id.powerLayout)).setVisibility(View.GONE);
      MyListMenu deviceIdMenu = (MyListMenu)this.findViewById(R.id.deviceid);
      deviceIdMenu.setMenuItems(this.getUnusedDeviceIds());
      if(tabName.equals("Lights")) {
    	  titleText.setText("New Light");
         var6.setText("Light Name");
         deviceTypeLabel.setText("Light Type");
      } else {
    	  titleText.setText("New Device");
         var6.setText("Device Name");
         deviceTypeLabel.setText("Device Type");
      }
      changeDevicePopup();
      this.initListeners();
      editDeviceName = this.getIntent().getExtras().getString("entityName");
      if( editDeviceName != null) {
    	  String title = "Device "+ editDeviceName;
    	  if(tabName.equals("Lights")) {
    		  title = "Light "+ editDeviceName;
    	  }
    	  titleText.setText(title);
    	  deviceNameText.setText(editDeviceName);
    	  deviceNameText.setEnabled(false);
    	  
    	  DeviceData deviceData = BtLocalDB.getInstance(this).getDevices(deviceName, editDeviceName).get(0);
    	  isdimmable.setChecked(deviceData.isDimmable());
    	  String unusedids[] = this.getUnusedDeviceIds();
    	  String [] ids = new String[unusedids.length+1];
    	  ids[0] = ""+deviceData.getDevId();
    	  for(int index=0; index<unusedids.length; index++)
    		  ids[index+1] = unusedids[index]; 
    	  deviceIdMenu.setMenuItems(ids);
    	  MyPopupDialog deviceTypeText = (MyPopupDialog)this.findViewById(R.id.deviceTypeList);
    	  deviceTypeText.setText(deviceData.getType());
      }
   }

   private void changeDevicePopup() {
	   int deviceTypeWithDimmable = 0;
	   MyPopupDialog var3 = (MyPopupDialog)this.findViewById(R.id.deviceTypeList);
	      
      if (tabName.equals("Lights")) {
    	  if( isdimmable.isChecked()) {
    		  deviceTypeWithDimmable = 1;
    	  }
      } else {
    	  if( isdimmable.isChecked()) {
    		  deviceTypeWithDimmable = 3;
    	  } else
    		  deviceTypeWithDimmable = 2;
      }
      var3.setMenuForLight(deviceTypeWithDimmable);
   }
   
   public void onDestroy() {
      super.onDestroy();
   }
}

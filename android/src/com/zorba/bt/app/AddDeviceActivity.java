package com.zorba.bt.app;

import java.util.ArrayList;

import com.zorba.bt.app.dao.DeviceData;
import com.zorba.bt.app.db.BtLocalDB;

import android.widget.ImageButton;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

public class AddDeviceActivity extends ZorbaActivity {
   String deviceAddress = null;

   private String[] getUnusedDeviceIds() {
      DeviceData[] var3 = BtLocalDB.getInstance(this).getDevices(this.deviceAddress);
      ArrayList<Integer> var2 = new ArrayList<Integer>();

      int var1;
      for(var1 = 0; var1 < var3.length; ++var1) {
         if(var3[var1].isUnknownType()) {
            var2.add(Integer.valueOf(var3[var1].getDevId()));
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
   }

   private void saveDevice() {
      EditText var3 = (EditText)this.findViewById(R.id.deviceNameInput);
      MyPopupDialog var2 = (MyPopupDialog)this.findViewById(R.id.deviceTypeList);
      MyListMenu var4 = (MyListMenu)this.findViewById(R.id.deviceid);
      EditText var5 = (EditText)this.findViewById(R.id.powerinwatts);
      String var8 = var2.getText();
      String var9 = CommonUtils.isValidName(this, var3.getText().toString());
      if(var9 != null) {
         if(com.zorba.bt.app.db.BtLocalDB.getInstance(this.getApplication()).isDeviceNameExist(this.deviceAddress, var9)) {
            CommonUtils.AlertBox(this, "Already exist", "Name is exist already");
         } else {
            DeviceData[] var6 = com.zorba.bt.app.db.BtLocalDB.getInstance(this).getDevices(this.deviceAddress);
            if(var4.getText().isEmpty()) {
               CommonUtils.AlertBox(this, "Device Limit", "Maximum of " + var6.length + " devices can only be added");
            } else {
               String var12 = var5.getText().toString().trim();
               if(var12.isEmpty()) {
                  CommonUtils.AlertBox(this, "No power mentioned", "You may not be able to see power consumption used by this device");
               }

               int var1 = Integer.parseInt(var4.getText());
               DeviceData var10 = new DeviceData(var1, var9, var8, var12, -1);

               try {
                  com.zorba.bt.app.bluetooth.BtHwLayer.getInstance(this).setDeviceType(var1, var10.isDimmable());
               } catch (Exception var7) {
                  CommonUtils.AlertBox(this, "Setting device type", "Setting device type is failed");
                  return;
               }

               com.zorba.bt.app.db.BtLocalDB.getInstance(this).updateDevice(this.deviceAddress, var10);
               Intent var11 = new Intent();
               var11.putExtra("name", var9);
               var11.putExtra("type", var8);
               var11.putExtra("index", var1);
               var11.putExtra("power", var12);
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
      this.deviceAddress = var1.getString("deviceAddress");
      String var4 = var1.getString("tabName");
      TextView var5 = (TextView)this.findViewById(R.id.title);
      TextView var6 = (TextView)this.findViewById(R.id.devicename);
      TextView var2 = (TextView)this.findViewById(R.id.devicetype);
      ((MyListMenu)this.findViewById(R.id.deviceid)).setMenuItems(this.getUnusedDeviceIds());
      MyPopupDialog var3 = (MyPopupDialog)this.findViewById(R.id.deviceTypeList);
      if(var4.equals("Lights")) {
         var5.setText("New Light");
         var6.setText("Light Name");
         var2.setText("Light Type");
      } else {
         var5.setText("New Device");
         var6.setText("Device Name");
         var2.setText("Device Type");
      }

      var3.setMenuForLight(var4.equals("Lights"));
      this.initListeners();
   }

   public void onDestroy() {
      super.onDestroy();
   }
}

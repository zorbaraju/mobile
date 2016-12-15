package com.zorba.bt.app;

import java.util.ArrayList;

import com.zorba.bt.app.dao.DeviceData;
import com.zorba.bt.app.db.BtLocalDB;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AddGroupActivity extends ZorbaActivity {
   String deviceName = null;
   String editGroupName = null;

   private void initListeners() {
      ((ImageButton)this.findViewById(R.id.cancel)).setOnClickListener(new OnClickListener() {
         public void onClick(View var1) {
            AddGroupActivity.this.finish();
         }
      });
      ((ImageButton)this.findViewById(R.id.save)).setOnClickListener(new OnClickListener() {
         public void onClick(View var1) {
            AddGroupActivity.this.saveGroup();
         }
      });
   }

   private void populateDevices() {
      LinearLayout devicesLayout = (LinearLayout)this.findViewById(R.id.groupdevices);
      ArrayList<DeviceData> deviceList = BtLocalDB.getInstance(this).getDevices(this.deviceName, null);
      int numDevices = deviceList.size();
      for(int ddindex = 0; ddindex < numDevices; ++ddindex) {
    	  DeviceData device = deviceList.get(ddindex);
    	  if(!device.isUnknownType()) {
            SelectComp comp = new SelectComp(this, device);
            comp.setId(device.getDevId());
            devicesLayout.addView(comp);
         }
      }
      this.initListeners();

      editGroupName = this.getIntent().getExtras().getString("entityName");
      if( editGroupName != null) {
    	  ((TextView)this.findViewById(R.id.title)).setText("Group "+editGroupName);
    	  EditText gNameText = (EditText)this.findViewById(R.id.groupNameText);
    	  gNameText.setEnabled(false);
    	  gNameText.setText(editGroupName);
    	  int devidAndStatus[] = BtLocalDB.getInstance(this).getGroupDevices(deviceName, editGroupName);
    	  for(int dindex = 0; dindex<devidAndStatus.length/2; dindex++) {
        	  SelectComp comp = getSelectComp(devidAndStatus[dindex*2]);
        	  comp.setSelected(true);
        	  comp.setDeviceValue(devidAndStatus[dindex*2+1]);
          }        
      }
   }

   private SelectComp getSelectComp(int devid){
	   LinearLayout devLayout = (LinearLayout)this.findViewById(R.id.groupdevices);
	   int numComps = devLayout.getChildCount();
	   for(int ni=0; ni<numComps; ni++){
		   SelectComp comp = (SelectComp)devLayout.getChildAt(ni);
		   if( comp.getDeviceIndex() == devid)
			   return comp;
	   }
 	  return null;
   }
   
   private void saveGroup() {
      EditText var3 = (EditText)this.findViewById(R.id.groupNameText);
      LinearLayout var6 = (LinearLayout)this.findViewById(R.id.groupdevices);
      String grpName = CommonUtils.isValidName(this, var3.getText().toString());
      boolean isNew = editGroupName==null;
      if(grpName != null) {
         if(isNew && BtLocalDB.getInstance(this.getApplication()).isGroupNameExist(this.deviceName, grpName)) {
            CommonUtils.AlertBox(this, "Already exist", "Name is exist already");
         } else {
            int deviceCount = var6.getChildCount();
            String devdetails = "";
            boolean isSelectedAny = false;
            for(int dIndex = 0; dIndex < deviceCount; dIndex++) {
               SelectComp comp = (SelectComp)var6.getChildAt(dIndex);
               if(comp.isSelected()) {
            	   isSelectedAny = true;
                  String devdetail = comp.getDeviceIndex() + "#" + comp.getDeviceValue();
                  if(devdetails.isEmpty()) {
                	  devdetails = devdetail;
                  } else {
                	  devdetails += "#" + devdetail;
                  }
               }
            }

            if(!isSelectedAny) {
               CommonUtils.AlertBox(this, "Save group", "No devices are selected");
            } else {
               BtLocalDB.getInstance(this).saveGroup(this.deviceName, grpName, devdetails, isNew);
               Intent var9 = new Intent();
               var9.putExtra("name", grpName);
               var9.putExtra("isnew", isNew);
               this.setResult(1, var9);
               this.finish();
            }
         }
      }

   }

   protected void onCreate(Bundle var1) {
      super.onCreate(var1);
      this.setContentView(R.layout.addgrouplayout);
      EditText var3 = (EditText)this.findViewById(R.id.groupNameText);
      var3.setFilters(new InputFilter[] {new InputFilter.LengthFilter(12)});
      ((TextView)this.findViewById(R.id.title)).setText("New Group");
      this.deviceName = this.getIntent().getExtras().getString("deviceName");
      this.populateDevices();
   }

   public void onDestroy() {
      super.onDestroy();
   }
}

package com.zorba.bt.app;

import java.util.ArrayList;

import com.zorba.bt.app.dao.DeviceData;
import com.zorba.bt.app.dao.GroupData;
import com.zorba.bt.app.dao.SchedulerData;
import com.zorba.bt.app.db.BtLocalDB;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AddGroupActivity extends ZorbaActivity {
   String deviceName = null;
   String editGroupName = null;

   private void initListeners() {
      ((SvgView)this.findViewById(R.id.cancel)).setOnClickListener(new ZorbaOnClickListener() {
          public void zonClick(View var1) {
            AddGroupActivity.this.finish();
         }
      });
      ((SvgView)this.findViewById(R.id.save)).setOnClickListener(new ZorbaOnClickListener() {
          public void zonClick(View var1) {
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
    	  ArrayList<GroupData> grpArr = BtLocalDB.getInstance(this).getGroups(deviceName, editGroupName);
    	  if( grpArr.size() == 0)
    		  return;
    	  GroupData groupData = grpArr.get(0);
    	  gNameText.setEnabled(false);
    	  gNameText.setText(editGroupName);
    	  int devidAndStatus[] = BtLocalDB.getInstance(this).getGroupDevices(deviceName, editGroupName);
    	  for(int dindex = 0; dindex<devidAndStatus.length/2; dindex++) {
        	  SelectComp comp = getSelectComp(devidAndStatus[dindex*2]);
        	  comp.setSelected(true);
        	  comp.setDeviceValue(devidAndStatus[dindex*2+1]);
          }     
    	  MyPopupDialog deviceTypeText = (MyPopupDialog)this.findViewById(R.id.deviceTypeList);
    	  deviceTypeText.setText(groupData.getType());
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
      MyPopupDialog listDialog = (MyPopupDialog)this.findViewById(R.id.deviceTypeList);
      String groupType = listDialog.getText();
      boolean isNew = editGroupName==null;
      if(grpName != null) {
         if(isNew && BtLocalDB.getInstance(this.getApplication()).isGroupNameExist(this.deviceName, grpName)) {
        	 //-spb 010217 for error msg chg CommonUtils.AlertBox(this, "Already exist", "Name is exist already");
        	//-spb 270417 for errors CommonUtils.AlertBox(this, "Same name exists", "Please enter another name");
        	 CommonUtils.AlertBox(this, CommonUtils.getInstance().getErrorString("ERROR1"), CommonUtils.getInstance().getErrorString("ERROR2"));
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
            	//-spb 010217 for error msg chg  CommonUtils.AlertBox(this, "Save group", "No devices are selected");
            	//-spb 270417 for errors CommonUtils.AlertBox(this, "Can't save group", "Kindly select switches and state in group");
            	CommonUtils.AlertBox(this, CommonUtils.getInstance().getErrorString("ERROR11"), CommonUtils.getInstance().getErrorString("ERROR12"));
            } else {
               BtLocalDB.getInstance(this).saveGroup(this.deviceName, grpName, groupType, devdetails, isNew);
               Intent var9 = new Intent();
               var9.putExtra("name", grpName);
               var9.putExtra("isnew", isNew);
               var9.putExtra("type", groupType);
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
      MyPopupDialog listDialog = (MyPopupDialog)this.findViewById(R.id.deviceTypeList);
      listDialog.setMenu(GroupData.groupTypes, GroupData.imageResIds);
      this.populateDevices();
   }

   public void onDestroy() {
      super.onDestroy();
   }
}

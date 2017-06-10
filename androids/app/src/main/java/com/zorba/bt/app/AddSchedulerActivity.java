package com.zorba.bt.app;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Calendar;

import com.zorba.bt.app.bluetooth.BtHwLayer;
import com.zorba.bt.app.dao.DeviceData;
import com.zorba.bt.app.dao.GroupData;
import com.zorba.bt.app.dao.SchedulerData;
import com.zorba.bt.app.db.BtLocalDB;

public class AddSchedulerActivity extends ZorbaActivity {
   String deviceName = null;
   private int mDay;
   private int mHour;
   private int mMinute;
   private int mMonth;
   private int mYear;
   String editSchedulerName = null;

   private void initListeners() {
      ((SvgView)this.findViewById(R.id.cancel)).setOnClickListener(new ZorbaOnClickListener() {
          public void zonClick(View var1) {
            AddSchedulerActivity.this.finish();
         }
      });
      ((SvgView)this.findViewById(R.id.save)).setOnClickListener(new ZorbaOnClickListener() {
          public void zonClick(View var1) {
            AddSchedulerActivity.this.saveScheduler();
         }
      });
      this.repeatTypeChanged();
   }

   private static String pad(int var0) {
      String var1;
      if(var0 >= 10) {
         var1 = String.valueOf(var0);
      } else {
         var1 = "0" + String.valueOf(var0);
      }

      return var1;
   }

   private void populateSchedulers() {
      final Calendar var2 = Calendar.getInstance();
      this.mYear = var2.get(1);
      this.mMonth = var2.get(2);
      this.mDay = var2.get(5);
      this.mHour = var2.get(11);
      this.mMinute = var2.get(12);
      final TextView localObject1 = (TextView)findViewById(R.id.startDate);
      final TextView localObject2 = (TextView)findViewById(R.id.startTime);
      final DatePickerDialog.OnDateSetListener myDateListener = new DatePickerDialog.OnDateSetListener() {
    	   @Override
    	   public void onDateSet(DatePicker paramAnonymousDatePicker, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3)
           {
             AddSchedulerActivity.this.mYear = paramAnonymousInt1;
             AddSchedulerActivity.this.mMonth = paramAnonymousInt2;
             AddSchedulerActivity.this.mDay = paramAnonymousInt3;
             AddSchedulerActivity.this.updateDateDisplay(localObject1);
           }
    	};
    	
      localObject1.setOnClickListener(new ZorbaOnClickListener() {
          public void zonClick(View paramAnonymousView)
          {
            new DatePickerDialog(AddSchedulerActivity.this, myDateListener, AddSchedulerActivity.this.mYear, AddSchedulerActivity.this.mMonth, AddSchedulerActivity.this.mDay).show();
          }
        
      });
      final TimePickerDialog.OnTimeSetListener myTimeListener = new TimePickerDialog.OnTimeSetListener() {
   	   
	@Override
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		AddSchedulerActivity.this.mHour = hourOfDay;
        AddSchedulerActivity.this.mMinute = minute;
        AddSchedulerActivity.this.updateTimeDisplay(localObject2);
	}
   	};
   	
   	localObject2.setOnClickListener(new ZorbaOnClickListener() {
        public void zonClick(View paramAnonymousView)
        {
          new TimePickerDialog(AddSchedulerActivity.this, myTimeListener, AddSchedulerActivity.this.mHour, AddSchedulerActivity.this.mMinute, false).show();
        }
      
    });
      this.updateDateDisplay(localObject1);
      this.updateTimeDisplay(localObject2);
      LinearLayout var6 = (LinearLayout)this.findViewById(R.id.scheduledevices);
      ArrayList<DeviceData> deviceList = BtLocalDB.getInstance(this).getDevices(this.deviceName, null);
      int numdevices = deviceList.size();
      for(int ddindex = 0; ddindex < numdevices; ++ddindex) {
    	  DeviceData device = deviceList.get(ddindex);
    	  if(!device.isUnknownType()) {
            SelectComp var4 = new SelectComp(this, device);
            var4.setId(device.getDevId());
            var6.addView(var4);
         }
      }

      this.initListeners();
      editSchedulerName = this.getIntent().getExtras().getString("entityName");
      if( editSchedulerName != null) {
    	  ((TextView)this.findViewById(R.id.title)).setText("Scheduler "+editSchedulerName);
    	  EditText schedNameText = (EditText)this.findViewById(R.id.schedulerNameText);
    	  schedNameText.setEnabled(false);
    	  schedNameText.setText(editSchedulerName);
    	  ArrayList<SchedulerData> schedArr = BtLocalDB.getInstance(this).getSchedules(deviceName, editSchedulerName);
    	  if( schedArr.size() == 0)
    		  return;
    	  SchedulerData schedData = schedArr.get(0);
    	  mMinute = schedData.getMin();
    	  mHour = schedData.getHr();
    	  updateTimeDisplay(localObject2);
    	  MyListMenu repeatMenu = (MyListMenu)this.findViewById(R.id.repeattype);
    	  int repeatType = schedData.getRepeatType();
    	  repeatMenu.setSelectedItem(repeatType);
    	  repeatTypeChanged();
    	// -spb 270417 remove weekly schedular
    				/*

    	  TableRow weeklyRows = (TableRow)this.findViewById(R.id.weeklyselection);
          int repeatTypeValue = schedData.getRepeatValue();
          if(repeatType == 2) {
             int numdays = weeklyRows.getChildCount();
             
             for(int dayindex = numdays-1; dayindex >-1; --dayindex) {
            	int bitvalue = repeatTypeValue&01;
            	((CustomCheckBox)weeklyRows.getChildAt(dayindex)).setDaySelected(bitvalue>0);
                repeatTypeValue >>= 1;
             }
          }
          */
			// -spb 270417 remove weekly schedular

    	  int devidAndStatus[] = schedData.getDevData();
    	  for(int dindex = 0; dindex<devidAndStatus.length/2; dindex++) {
        	  SelectComp comp = getSelectComp(devidAndStatus[dindex*2]);
        	  comp.setSelected(true);
        	  comp.setDeviceValue(devidAndStatus[dindex*2+1]);
          }
    	  MyPopupDialog deviceTypeText = (MyPopupDialog)this.findViewById(R.id.deviceTypeList);
    	  deviceTypeText.setText(schedData.getType());
      }
   }
   
   private SelectComp getSelectComp(int devid){
	   LinearLayout devLayout = (LinearLayout)this.findViewById(R.id.scheduledevices);
	   int numComps = devLayout.getChildCount();
	   for(int ni=0; ni<numComps; ni++){
		   SelectComp comp = (SelectComp)devLayout.getChildAt(ni);
		   if( comp.getDeviceIndex() == devid)
			   return comp;
	   }
 	  return null;
   }

   private void repeatTypeChanged() {
      MyListMenu var2 = (MyListMenu)this.findViewById(R.id.repeattype);
  	// -spb 270417 remove weekly schedular       TableRow var1 = (TableRow)this.findViewById(R.id.weeklyselection);
  	// -spb 270417 remove weekly schedular 
      /*
      if(var2.getSelectedItemPosition() == 2) {
         var1.setVisibility(View.VISIBLE);
      } else {
         var1.setVisibility(View.GONE);
      }
     */
  	// -spb 270417 remove weekly schedular 
   }

   private void saveScheduler() {
      EditText schedNameText = (EditText)this.findViewById(R.id.schedulerNameText);
      LinearLayout layout = (LinearLayout)this.findViewById(R.id.scheduledevices);
      MyListMenu repeatMenu = (MyListMenu)this.findViewById(R.id.repeattype);
      TableRow weeklyRows = (TableRow)this.findViewById(R.id.weeklyselection);
      MyPopupDialog listDialog = (MyPopupDialog)this.findViewById(R.id.deviceTypeList);
      String schedulerType = listDialog.getText();
      int repeatType = repeatMenu.getSelectedItemPosition();
      int repeatTypeValue = 0;
      int daybits = 0;
      if( repeatType == 0) {
    	  repeatTypeValue = 1;
      } else if( repeatType == 1) {
    	  daybits = 0xfe;
      } else {
         int numdays = weeklyRows.getChildCount();
         
         for(int dayindex = 0; dayindex < numdays; ++dayindex) {
            if(((CustomCheckBox)weeklyRows.getChildAt(dayindex)).isDaySelected()) {
            	daybits |= 1;
            } else {
            	daybits |= 0;
            }
            daybits <<= 1;
         }
         daybits >>= 1;
      }
      repeatTypeValue |= daybits;
      boolean isNew = editSchedulerName==null;
      
      String name = CommonUtils.isValidName(this, schedNameText.getText().toString());
      if(name != null) {
         if(isNew && BtLocalDB.getInstance(this.getApplication()).isSchedulerNameExist(this.deviceName, name)) {
        	 //-spb 010217 for error msg chg CommonUtils.AlertBox(this, "Already exist", "Name is exist already");
        	//-spb 270417 for errors CommonUtils.AlertBox(this, "Same name exists", "Please enter another name");
        	 CommonUtils.AlertBox(this, CommonUtils.getInstance().getErrorString("ERROR1"), CommonUtils.getInstance().getErrorString("ERROR2"));
         } else {
            int chCount = layout.getChildCount();
            int numSelectedDevices = 0;
            for(int chindex = 0; chindex < chCount; chindex++) {
               SelectComp comp = (SelectComp)layout.getChildAt(chindex);
               if(comp.isSelected()) {
                  numSelectedDevices++;
               }
            }

            if(numSelectedDevices ==0) {
            	//-spb 270417 for errors  CommonUtils.AlertBox(this, "Can't save scheduler", "Kindly select switches and state in scheduler");
            	CommonUtils.AlertBox(this, CommonUtils.getInstance().getErrorString("ERROR9"), CommonUtils.getInstance().getErrorString("ERROR10"));
            } else {
               DeviceData[] devData = new DeviceData[numSelectedDevices];
               int dindex = 0;

               for(int chindex = 0; chindex < chCount; chindex++) {
                  SelectComp comp = (SelectComp)layout.getChildAt(chindex);
                  if(comp.isSelected()) {
                	  devData[dindex++] = new DeviceData(comp.getDeviceIndex(), "", "", "10", comp.getDeviceValue());
                  }
               }

               int schedid  = BtLocalDB.getInstance(this).getNewSchedulerId(this.deviceName);
               Intent intent = new Intent();
               intent.putExtra("scheduleid", schedid);
               intent.putExtra("name", name);

               try {
                  BtHwLayer.getInstance(this).sendAlarmCommandToDevice(schedid, repeatTypeValue, this.mHour, this.mMinute, devData);
               } catch (Exception ex) {
                  intent.putExtra("error", "Error in sending alarm:" + ex.getMessage());
                  this.setResult(1, intent);
                  this.finish(); 
                  return;
               }
               BtLocalDB.getInstance(this).saveSchedule(this.deviceName, schedulerType, isNew, schedid, repeatType, repeatTypeValue, name, this.mHour, this.mMinute, devData);
               intent.putExtra("isnew", isNew);
               this.setResult(1, intent);
               this.finish();
            }
         }
      }

   }

   private void updateDateDisplay(TextView var1) {
      var1.setText((new StringBuilder()).append(this.mDay).append("-").append(this.mMonth + 1).append("-").append(this.mYear));
   }

   private void updateTimeDisplay(TextView var1) {
      var1.setText((new StringBuilder()).append(pad(this.mHour)).append(":").append(pad(this.mMinute)));
   }

   protected void onCreate(Bundle var1) {
      super.onCreate(var1);
      this.setContentView(R.layout.addschedulerlayout);
      EditText var3 = (EditText)this.findViewById(R.id.schedulerNameText);
      var3.setFilters(new InputFilter[] {new InputFilter.LengthFilter(12)});
      ((TextView)this.findViewById(R.id.title)).setText("New Scheduler");
      this.deviceName = this.getIntent().getExtras().getString("deviceName");
      MyListMenu var2 = (MyListMenu)this.findViewById(R.id.repeattype);
      var2.addCallback(new Runnable() {
         public void run() {
            AddSchedulerActivity.this.repeatTypeChanged();
         }
      });
  	// -spb 270417 remove weekly schedular       var2.setMenuItems(new String[]{"Once    ", "Daily   ", "Weekly  "});
  	var2.setMenuItems(new String[] { "Once    ", "Daily   " });
      MyPopupDialog listDialog = (MyPopupDialog)this.findViewById(R.id.deviceTypeList);
      listDialog.setMenu(SchedulerData.schedulerTypes, SchedulerData.imageResIds);
      this.populateSchedulers();
   }

   public void onDestroy() {
      super.onDestroy();
   }
}



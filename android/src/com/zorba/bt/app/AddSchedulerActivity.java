package com.zorba.bt.app;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

import com.zorba.bt.app.bluetooth.BtHwLayer;
import com.zorba.bt.app.dao.DeviceData;
import com.zorba.bt.app.db.BtLocalDB;

public class AddSchedulerActivity extends ZorbaActivity {
   String deviceName = null;
   private int mDay;
   private int mHour;
   private int mMinute;
   private int mMonth;
   private int mYear;

   private void initListeners() {
      ((ImageButton)this.findViewById(R.id.cancel)).setOnClickListener(new OnClickListener() {
         public void onClick(View var1) {
            AddSchedulerActivity.this.finish();
         }
      });
      ((ImageButton)this.findViewById(R.id.save)).setOnClickListener(new OnClickListener() {
         public void onClick(View var1) {
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
    	
      localObject1.setOnClickListener(new OnClickListener()
      {

    	  public void onClick(View paramAnonymousView)
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
   	
   	localObject2.setOnClickListener(new OnClickListener()
    {

  	  public void onClick(View paramAnonymousView)
        {
          new TimePickerDialog(AddSchedulerActivity.this, myTimeListener, AddSchedulerActivity.this.mHour, AddSchedulerActivity.this.mMinute, false).show();
        }
      
    });
      this.updateDateDisplay(localObject1);
      this.updateTimeDisplay(localObject2);
      LinearLayout var6 = (LinearLayout)this.findViewById(R.id.scheduledevices);
      DeviceData[] var7 = BtLocalDB.getInstance(this).getDevices(this.deviceName);

      for(int var1 = 0; var1 < var7.length; ++var1) {
         if(!var7[var1].isUnknownType()) {
            SelectComp var4 = new SelectComp(this, var7[var1]);
            var4.setId(var7[var1].getDevId());
            var6.addView(var4);
         }
      }

      this.initListeners();
   }

   private void repeatTypeChanged() {
      MyListMenu var2 = (MyListMenu)this.findViewById(R.id.repeattype);
      TableRow var1 = (TableRow)this.findViewById(R.id.weeklyselection);
      if(var2.getSelectedItemPosition() == 2) {
         var1.setVisibility(0);
      } else {
         var1.setVisibility(8);
      }

   }

   private void saveScheduler() {
      EditText var10 = (EditText)this.findViewById(R.id.schedulerNameText);
      LinearLayout var9 = (LinearLayout)this.findViewById(R.id.scheduledevices);
      MyListMenu var7 = (MyListMenu)this.findViewById(R.id.repeattype);
      TableRow var8 = (TableRow)this.findViewById(R.id.weeklyselection);
      int var5 = var7.getSelectedItemPosition();
      int var2 = 0;
      int var1 = 0;
      int var3;
      if(var5 == 1) {
         var1 = 127;
      } else if(var5 == 2) {
         var3 = var8.getChildCount();

         for(var1 = 0; var1 < var3; ++var1) {
            if(((CustomCheckBox)var8.getChildAt(var1)).isDaySelected()) {
               var2 |= 1;
            } else {
               var2 |= 0;
            }

            var2 <<= 1;
         }

         var1 = var2 >> 1;
      }

      String var18 = CommonUtils.isValidName(this, var10.getText().toString());
      if(var18 != null) {
         if(BtLocalDB.getInstance(this.getApplication()).isSchedulerNameExist(this.deviceName, var18)) {
            CommonUtils.AlertBox(this, "Already exist", "Name is exist already");
         } else {
            int var6 = var9.getChildCount();
            String var14 = "";
            var3 = 0;

            int var4;
            String var13;
            for(var2 = 0; var2 < var6; var14 = var13) {
               SelectComp var11 = (SelectComp)var9.getChildAt(var2);
               var4 = var3;
               var13 = var14;
               if(var11.isSelected()) {
                  var4 = var11.getDeviceIndex();
                  if(var14.isEmpty()) {
                     var13 = var14 + var4;
                  } else {
                     var13 = var14 + "#" + var4;
                  }

                  var4 = var3 + 1;
               }

               ++var2;
               var3 = var4;
            }

            if(var14.isEmpty()) {
               CommonUtils.AlertBox(this, "Save scheduler", "No devices are selected");
            } else {
               var14.trim();
               DeviceData[] var15 = new DeviceData[var3];
               var2 = 0;

               for(var4 = 0; var4 < var6; var2 = var3) {
                  SelectComp var16 = (SelectComp)var9.getChildAt(var4);
                  var3 = var2;
                  if(var16.isSelected()) {
                     var15[var2] = new DeviceData(var16.getDeviceIndex(), "", "", "10", var16.getDeviceValue());
                     var3 = var2 + 1;
                  }

                  ++var4;
               }

               var2 = BtLocalDB.getInstance(this).getNewSchedulerId(this.deviceName);
               Intent var17 = new Intent();
               var17.putExtra("scheduleid", var2);
               var17.putExtra("name", var18);

               try {
                  BtHwLayer.getInstance(this).sendAlarmCommandToDevice(var2, var1, this.mHour, this.mMinute, var15);
               } catch (Exception var12) {
                  var17.putExtra("error", "Error in sending alarm:" + var12.getMessage());
                  this.setResult(1, var17);
                  this.finish();
                  return;
               }

               BtLocalDB.getInstance(this).saveSchedule(this.deviceName, var2, var5, var1, var18, this.mHour, this.mMinute, var15);
               this.setResult(1, var17);
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
      var2.setMenuItems(new String[]{"Once", "Daily", "Weekly"});
      this.populateSchedulers();
   }

   public void onDestroy() {
      super.onDestroy();
   }
}



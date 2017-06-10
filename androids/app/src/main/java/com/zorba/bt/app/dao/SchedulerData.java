package com.zorba.bt.app.dao;

import com.zorba.bt.app.R;

public class SchedulerData {
   private String name = "";
   private String schedid = "";
   int devidandstatus[] = null;
   int repeatType = 0;
   int repeatValue = 0;
   int hr = 0;
   int min = 0;
   String schedulerType = "";
   public static String schedulerTypes[] = {
		   "Master  ", "Schedule  "
   };
   public static int imageResIds[] = {
		   R.raw.sch_master, R.raw.sch_alarm
   };

   public SchedulerData(String schedid, String name, String type, int devData[],
		   int repeatType, int repeatValue, int hr, int min ) {
      this.schedid = schedid;
      this.name = name;
      schedulerType = type;
      devidandstatus = devData;
      this.repeatType = repeatType;
      this.repeatValue = repeatValue;
      this.min = min;
      this.hr = hr;
   }

   public String getName() {
      return this.name;
   }

   public String getSchedulerId() {
      return this.schedid;
   }
   
   public String getType() {
	   return schedulerType;
   }

   public String toString() {
      return this.name;
   }
   
	public int[] getDevData() {
		return devidandstatus;
	}
	
	public int getRepeatType() {
	      return this.repeatType;
	   }
	public int getRepeatValue() {
	      return this.repeatValue;
	   }
	public int getHr() {
	      return this.hr;
	   }
	public int getMin() {
	      return this.min;
	   }
	public int getImageResId() {
		int index = 0;
		for(String t:schedulerTypes) {
			if( t.equals(schedulerType)) {
				return imageResIds[index];
			}
			index++;
		}
		return -1;
	}
	
}

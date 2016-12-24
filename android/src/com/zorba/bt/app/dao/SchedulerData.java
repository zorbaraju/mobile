package com.zorba.bt.app.dao;

public class SchedulerData {
   private String name = "";
   private String schedid = "";
   int devidandstatus[] = null;
   int repeatType = 0;
   int repeatValue = 0;
   int hr = 0;
   int min = 0;

   public SchedulerData(String schedid, String name, int devData[],
		   int repeatType, int repeatValue, int hr, int min ) {
      this.schedid = schedid;
      this.name = name;
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
	
	
}

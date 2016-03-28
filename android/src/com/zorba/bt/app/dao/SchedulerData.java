package com.zorba.bt.app.dao;

public class SchedulerData {
   private String name = "";
   private String sid = "";

   public SchedulerData(String var1, String var2) {
      this.name = var2;
      this.sid = var1;
   }

   public String getName() {
      return this.name;
   }

   public String getSchedulerId() {
      return this.sid;
   }

   public String toString() {
      return this.name;
   }
}

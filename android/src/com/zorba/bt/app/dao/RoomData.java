package com.zorba.bt.app.dao;

public class RoomData {
   private String address = "";
   private String name = "";
   private boolean isRGB = false;
   
   public RoomData(String address, String name, boolean isRGB) {
      this.name = name;
      this.address = address;
      this.isRGB = isRGB;
   }

   public String getAddress() {
      return this.address;
   }

   public String getName() {
      return this.name;
   }

   public boolean isRGBType() {
	   return isRGB;
   }
   
   public String toString() {
      return this.name;
   }
}

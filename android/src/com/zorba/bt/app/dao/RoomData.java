package com.zorba.bt.app.dao;

public class RoomData {
   private String address = "";
   private String name = "";
   private boolean isRGB = false;
   private String ipaddress = "";
   
   public RoomData(String address, String name, boolean isRGB, String ipaddress) {
      this.name = name;
      this.address = address;
      this.isRGB = isRGB;
      this.ipaddress = ipaddress;
      
      if( name.equals("ez_125_RGB")) {
    	  this.ipaddress = "10.0.0.156";
      } else
    	  this.ipaddress = "10.0.0.157";
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
   
   public String getIpAddress(){
	   return ipaddress;
   }
   
   public String toString() {
      return this.name;
   }
}

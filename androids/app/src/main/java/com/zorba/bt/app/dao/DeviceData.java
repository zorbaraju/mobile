package com.zorba.bt.app.dao;

//-spb 160217 for icons rearrangement 

   public class DeviceData {
   public static final String DEVICETYPE_AC = "Ac  ";
   public static final String DEVICETYPE_ALARM = "Alarm  ";
   public static final String DEVICETYPE_CCTV_INDOOR = "CCTV  ";
   public static final String DEVICETYPE_CFL = "Chandlier  ";
   public static final String DEVICETYPE_COVE_LIGHT = "Deco Light  ";
   public static final String DEVICETYPE_CURTAIN = "Curtain Open  ";
   public static final String DEVICETYPE_DOORBELL = "Curtain Close  ";
   public static final String DEVICETYPE_DOORLOCK = "Door Lock  ";
   public static final String DEVICETYPE_FAN = "Fan  ";
   public static final String DEVICETYPE_FRIDGE = "Fridge  ";
   public static final String DEVICETYPE_LIGHT = "Light  ";
   public static final String DEVICETYPE_SETTOPBOX = "Settop Box  ";
   public static final String DEVICETYPE_SOCKET = "Socket  ";
   public static final String DEVICETYPE_SPRINKLER = "Water Pump  ";
   public static final String DEVICETYPE_TABLEFAN = "Table Lamp  ";
   public static final String DEVICETYPE_TUBELIGHT = "Tube Light  ";
   public static final String DEVICETYPE_TV = "TV  ";
   public static final String DEVICETYPE_DLIGHT = "Dim Light  ";
   public static final String DEVICETYPE_DCHANDLIER = "Dim Chandlier  ";
   public static final String DEVICETYPE_DSOCKET = "Dim Socket  ";
   public static final String DEVICETYPE_DFAN = "Dim Fan  ";
   public static final String DEVICETYPE_DTABLE = "Dim TableLight  ";
   public static final String DEVICETYPE_DDECO = "Dim DecoLight  ";
   public static final String DEVICETYPE_UNKNOWN = "Unknown  ";
      
   //-spb 160217 for icons rearrangement 
   /*
   public static final String[] deviceTypes = new String[]{"Light", "Dimmable Light", "Tube Light", "Chandlier", "Deco Light", "Fan", "Table Lamp", "Ac", "Ac Split", "Alarm", "CC tv Indoor", "CC tv Outdoor", "Curtain Open", "Curtain Close", "Door Lock", "Fridge", "Led", "Socket", "Micro Oven", "Settop Box", "Sprinkler", "TV"};
   public static final String[] lightdeviceTypes = new String[]{"Light", "Tube Light", "Is it light Chandlier", "is light Deco Light"};
   public static final String[] dimmablelightdeviceTypes = new String[]{"Dimmable Light"};
   public static final String[] nonlightdeviceTypes = new String[]{"Table Lamp", "Ac", "Ac Split", "Alarm", "CC tv Indoor", "CC tv Outdoor", "Curtain Open", "Curtain Close", "Door Lock", "Fridge", "Led", "Socket", "Micro Oven", "Settop Box", "Water Pump", "TV"};
   public static final String[] dimmablenonlightdeviceTypes = new String[]{"Fan"};
   public static final String[] switchdeviceTypes = new String[]{"Light", "Tube Light", "Chandlier", "Deco Light", "Fan", "Table Lamp", "Ac", "Ac Split", "Alarm", "CC tv Indoor", "CC tv Outdoor", "Curtain Open", "Curtain Close", "Door Lock", "Fridge", "Led", "Socket", "Micro Oven", "Settop Box", "Sprinkler", "TV"};
   public static final String[] dimmabledeviceTypes = new String[]{"Dimmable Light", "Tube Light", "Is it light Chandlier", "is light Deco Light"};
   */
   //-spb 160217 for icons rearrangement 
   
   //+spb 160217: required dimmable devices: Fan, Socket,Chandlier,Table lampm
   //+spb 160217: required dimmable devices:  All Devices : Light Table Lamp Tubelight Chandlier Deco Light Fan AC Alarm CCTV Curtain Open Curtain Close Door Lock Fridge Socket Settopbox Water Pump TV

   //+spb 160217 for icons rearrangement 
   public static final String[] deviceTypes = new String[]{"Light  ", "Table Lamp  ", "Tube Light  ", "Chandlier  ", "Deco Light  ", "Fan  ", "Ac  ", "Alarm  ", "CCTV  ", "Curtain Open  ", "Curtain Close  ", "Door Lock  ", "Fridge  ", "Socket  ", "Settop Box  ", "Water Pump  ", "TV  "};
   public static final String[] lightdeviceTypes = new String[]{"Light  ", "Tube Light  ", "Chandlier  ", "Deco Light  "};
   public static final String[] dimmablelightdeviceTypes = new String[]{"Table Lamp  ", "Chandlier  ", "Deco Light  ","Light  "};
   public static final String[] nonlightdeviceTypes = new String[]{"Table Lamp  ", "Ac  ", "Ac Split  ", "Alarm  ", "CCTV  ", "Curtain Open  ", "Curtain Close  ", "Door Lock  ", "Fridge  ", "Socket  ", "Settop Box  ", "Water Pump  ", "TV  "};
   public static final String[] dimmablenonlightdeviceTypes = new String[]{"Fan  ","Socket  "};
   public static final String[] switchdeviceTypes = new String[]{"Light  ", "Table Lamp  ", "Tube Light  ", "Chandlier  ", "Deco Light  ", "Fan  ", "Ac  ", "Alarm  ", "CCTV  ", "Curtain Open  ", "Curtain Close  ", "Door Lock  ", "Fridge  ", "Socket  ", "Settop Box  ", "Water Pump  ", "TV  "};
   public static final String[] dimmabledeviceTypes = new String[]{"Dim Light  ","Dim Fan  ","Dim Socket  ","Dim Chandlier  ","Dim TableLamp  ","Dim DecoLight  "};
  
   //+spb 160217 for icons rearrangement 
   
   
   
   private int devid = 0;
   private String name = "";
   private String powerinwatts = "10";
   private int status = -1;
   private String type = "Unknown";

   public DeviceData(int devid, String name, String type, String powerinwatts, int status) {
      this.devid = devid;
      this.name = name;
      this.type = type;
      this.powerinwatts = powerinwatts;
      this.status = status;
   }

   public static boolean isDimmable(String type) {
	   for (String lightType : dimmabledeviceTypes) {
			if( type.equals(lightType))
				return true;
	   }
	   return false;
   }

   public static boolean isLightType(String type) {
	   for (String lightType : lightdeviceTypes) {
			if( type.equals(lightType))
				return true;
	   }
	   for (String lightType : dimmablelightdeviceTypes) {
			if( type.equals(lightType))
				return true;
	   }
      return false;
   }

   public int getDevId() {
      return this.devid;
   }

   public String getName() {
      return this.name;
   }

   public String getPower() {
      return this.powerinwatts;
   }

   public int getStatus() {
      return this.status;
   }

   public String getType() {
      return this.type;
   }

   public boolean isDimmable() {
      return isDimmable(this.type);
   }

   public boolean isUnknownType() {
      return this.type.equals("Unknown");
   }

   public void setStatus(int var1) {
      this.status = var1;
   }

   public String toString() {
      return "deviid=" + this.devid + " name=" + this.name + " type=" + this.type + " power=" + this.powerinwatts + " status=" + this.status;
   }

	public static String[] getDeviceNames(int deviceTypeWithDimmable) {
		String[] devicenames = DeviceData.nonlightdeviceTypes;
	    if( deviceTypeWithDimmable == 0)
	  	  devicenames = DeviceData.switchdeviceTypes;
	    else if( deviceTypeWithDimmable == 1)
	  	  devicenames = DeviceData.dimmabledeviceTypes;
	    return devicenames;
	}
}

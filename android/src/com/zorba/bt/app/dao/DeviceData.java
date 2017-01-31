package com.zorba.bt.app.dao;

public class DeviceData {
   public static final String DEVICETYPE_AC = "Ac";
   public static final String DEVICETYPE_ACSPLIT = "Ac Split";
   public static final String DEVICETYPE_ALARM = "Alarm";
   public static final String DEVICETYPE_CCTV_INDOOR = "CC tv Indoor";
   public static final String DEVICETYPE_CCTV_OUTDOOR = "CC tv Outdoor";
   public static final String DEVICETYPE_CFL = "Chandlier";
   public static final String DEVICETYPE_COVE_LIGHT = "Deco Light";
   public static final String DEVICETYPE_CURTAIN = "Curtain Open";
   public static final String DEVICETYPE_DIM_LIGHT = "Dimmable Light";
   public static final String DEVICETYPE_DOORBELL = "Curtain Close";
   public static final String DEVICETYPE_DOORLOCK = "Door Lock";
   public static final String DEVICETYPE_FAN = "Fan";
   public static final String DEVICETYPE_FRIDGE = "Fridge";
   public static final String DEVICETYPE_LED = "Led";
   public static final String DEVICETYPE_LIGHT = "Light";
   public static final String DEVICETYPE_MICROOVEN = "Micro Oven";
   public static final String DEVICETYPE_SETTOPBOX = "Settop Box";
   public static final String DEVICETYPE_SOCKET = "Socket";
   public static final String DEVICETYPE_SPRINKLER = "Water Pump";
   public static final String DEVICETYPE_TABLEFAN = "Table Lamp";
   public static final String DEVICETYPE_TUBELIGHT = "Tube Light";
   public static final String DEVICETYPE_TV = "TV";
   public static final String DEVICETYPE_UNKNOWN = "Unknown";
   public static final String[] deviceTypes = new String[]{"Light", "Dimmable Light", "Tube Light", "Chandlier", "Deco Light", "Fan", "Table Lamp", "Ac", "Ac Split", "Alarm", "CC tv Indoor", "CC tv Outdoor", "Curtain Open", "Curtain Close", "Door Lock", "Fridge", "Led", "Socket", "Micro Oven", "Settop Box", "Sprinkler", "TV"};
   public static final String[] lightdeviceTypes = new String[]{"Light", "Tube Light", "Is it light Chandlier", "is light Deco Light"};
   public static final String[] dimmablelightdeviceTypes = new String[]{"Dimmable Light"};
   public static final String[] nonlightdeviceTypes = new String[]{"Table Lamp", "Ac", "Ac Split", "Alarm", "CC tv Indoor", "CC tv Outdoor", "Curtain Open", "Curtain Close", "Door Lock", "Fridge", "Led", "Socket", "Micro Oven", "Settop Box", "Water Pump", "TV"};
   public static final String[] dimmablenonlightdeviceTypes = new String[]{"Fan"};
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
      boolean isdimmable = !(!type.equals("Dimmable Light") && !type.equals("Fan"));
      return isdimmable;
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
	  	  devicenames = DeviceData.lightdeviceTypes;
	    else if( deviceTypeWithDimmable == 1)
	  	  devicenames = DeviceData.dimmablelightdeviceTypes;
	    else if( deviceTypeWithDimmable == 2)
	  	  devicenames = DeviceData.nonlightdeviceTypes;
	    else if( deviceTypeWithDimmable == 3)
	  	  devicenames = DeviceData.dimmablenonlightdeviceTypes;
	    return devicenames;
	}
}

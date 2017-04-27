package com.zorba.bt.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v4.app.NotificationCompat;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;
import android.widget.ListAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import com.zorba.bt.app.bluetooth.BtHwLayer;
import com.zorba.bt.app.bluetooth.NetworkInfo;
import com.zorba.bt.app.bluetooth.NotificationListener;

public class CommonUtils {
	
	public static final String SWITCHTEXT_COLOR ="#ffffff"; //-spb 010217 try white col "#00ff00";
	public static final String SEEKBAR_COLOR = "#ff9400";
	public static final String DIMMER_DIALOG_COLOR = "#67615e";//+spb 2700117 for dimmer dialog color
	public static final String DIMMER_SEEKBAR_COLOR = "#f7f2ed";//+spb 2700117 for seekbar color in room page
	public static final String NO_CONNECTION_TAP_SWITCH_COLOR = "#1e1e1e";//+spb 060217 for text col chg
	public static final String TABSWITCH = "Switches";
	public static final String TABDIMMABLES = "Dimmables";
	
	public static final int CONNECTION_OFFLINE = 0;
	public static final int CONNECTION_BT = CONNECTION_OFFLINE+1;
	public static final int CONNECTION_WIFI = CONNECTION_BT+1;
	public static final int CONNECTION_DATA = CONNECTION_WIFI+1;
	
	
   private static int max_no_devices = 0;
   public static final float DROP_MENU_TEXT_SIZE= 20.0f;//+spb 200117 for common text size 
   public static final int MENUITEMINDEX_ABOUT = 3;
   public static final int MENUITEMINDEX_DISCOVERY = 0;
   public static final int MENUITEMINDEX_EXIT = 4;
   public static final int MENUITEMINDEX_HELP = 1;
   public static final int MENUITEMINDEX_SENDLOG = 2;
   public static final String MENUITEM_ABOUT = "About";
   public static final String MENUITEM_DISCOVERY = "Add Room";
   public static final String MENUITEM_EXIT = "Exit";
   public static final String MENUITEM_HELP = "Help";
   public static final String MENUITEM_SENDLOG = "Send Log";
   //public static final String APPPASSWORD = "EZORBA1234";
   public static final String DEVICEPASSWORD = "           ";
   
   private static CommonUtils instance = null;
   
   private static StringBuffer logContentBuf = null;
   private static HashMap<View, Integer> hiddenCountMap= new HashMap<View, Integer>();
   private Properties properties = new Properties();

   private CommonUtils() {
	   logContentBuf = new StringBuffer();
   }
   
   public static CommonUtils getInstance() {
	   if( instance == null)
		   instance = new CommonUtils();
	   return instance;
   }
   
   public void loadErrors(Context context) {
		try {
			AssetManager assetManager = context.getAssets();
			InputStream inputStream = assetManager.open("errors.properties");
			properties.load(inputStream);

		} catch (IOException e) {
			e.printStackTrace();
		}
   }
   
   public String getErrorString(String errorCode){
	   if( properties.containsKey(errorCode)) 
		   return properties.getProperty(errorCode);
	   else
		   return errorCode;
   }
   
	public static void AlertBox(final Activity var0, final String var1, final String var2) {
		var0.runOnUiThread(new Runnable() {
			public void run() {
				(new Builder(var0)).setTitle(var1).setMessage(var2).setPositiveButton("Close", new OnClickListener() {
					public void onClick(DialogInterface var1, int var2) {
						var1.dismiss();
					}
				}).show();
			}
		});
	}
	
	public static AlertDialog clearDialog(final Activity var0, final String var1, final String var2) {
		Builder builder = new Builder(var0);
		AlertDialog dialog = builder.create();
		dialog.setTitle(var1);
		dialog.setMessage(var2);
		return dialog;
	}

   public static byte[] getCurrentTime() {
      Calendar var7 = Calendar.getInstance();
      int date = var7.get(var7.DATE);
      int month = var7.get(var7.MONTH);
      int year = var7.get(var7.YEAR);
      int dayofweek = var7.get(var7.DAY_OF_WEEK);
      int hr = var7.get(var7.HOUR_OF_DAY);
      int min = var7.get(var7.MINUTE);
      int sec = var7.get(var7.SECOND);
      return new byte[]{(byte)date, (byte)(month + 1), (byte)(year - 2000), (byte)dayofweek, (byte)hr, (byte)min, (byte)sec};
   }

   public static int getDeviceImage(String var0, int var1) {
      int var2 = R.raw.unknown;
      if(var0.equals("Light  ")) {
         if(var1 <= 0) {
            var2 = R.raw.light_off;
         } else {
            var2 = R.raw.light_on;
         }
      } else if(var0.startsWith("Chandlier  ")) {
         if(var1 <= 0) {
            var2 = R.raw.chandlier_off;
         } else {
            var2 = R.raw.chandlier_on;
         }
      } else if(var0.startsWith("Tube Light  ")) {
         if(var1 <= 0) {
            var2 = R.raw.tubelight_off;
         } else {
            var2 = R.raw.tubelight_on;
         }
         /*
      } else if(var0.startsWith("Chandlier")) {
         if(var1 <= 0) {
            var2 = R.raw.chandlier_off;
         } else {
            var2 = R.raw.chandlier_on;
         }*/
      } else if(var0.startsWith("Deco Light  ")) {
         if(var1 <= 0) {
            var2 = R.raw.table_off;
         } else {
            var2 = R.raw.table_on;
         }
      } else if(var0.startsWith("Fan  ")) {
         if(var1 <= 0) {
            var2 = R.raw.fan_off;
         } else {
            var2 = R.raw.fan_on;
         }
      } else if(var0.startsWith("Table Lamp  ")) {
         if(var1 <= 0) {
            var2 = R.raw.tablelamp_off;
         } else {
            var2 = R.raw.tablelamp_on;
         }
      } else if(var0.startsWith("Ac  ")) {
         if(var1 <= 0) {
            var2 = R.raw.ac_off;
         } else {
            var2 = R.raw.ac_on;
         }
         /*
      } else if(var0.startsWith("Ac Split")) {
         if(var1 <= 0) {
            var2 = R.raw.ac_off;
         } else {
            var2 = R.raw.ac_on;
         }*/
      } else if(var0.startsWith("Alarm  ")) {
         if(var1 <= 0) {
            var2 = R.raw.alarm_off;
         } else {
            var2 = R.raw.alarm_on;
         }
      } else if(var0.startsWith("CCTV  ")) {
         if(var1 <= 0) {
            var2 = R.raw.cctvindoor_off;
         } else {
            var2 = R.raw.cctvindoor_on;
         }
         /*
      } else if(var0.startsWith("CC tv Outdoor")) {
         if(var1 <= 0) {
            var2 = R.raw.cctvindoor_off;
         } else {
            var2 = R.raw.cctvindoor_on;
         }*/
      } else if(var0.startsWith("Curtain Open  ")) {
         if(var1 <= 0) {
            var2 = R.raw.curtianopen_off;
         } else {
            var2 = R.raw.curtianopen_on;
         }
      } else if(var0.startsWith("Curtain Close  ")) {
         if(var1 <= 0) {
            var2 = R.raw.curtianclose_off;
         } else {
            var2 = R.raw.curtianclose_on;
         }
      } else if(var0.startsWith("Door Lock  ")) {
         if(var1 <= 0) {
            var2 = R.raw.doorlock_off;
         } else {
            var2 = R.raw.doorlock_on;
         }
      } else if(var0.startsWith("Fridge  ")) {
         if(var1 <= 0) {
            var2 = R.raw.fridge_off;
         } else {
            var2 = R.raw.fridge_on;
         }
         /*
      } else if(var0.startsWith("Led")) {
         if(var1 <= 0) {
            var2 = R.raw.light_off;
         } else {
            var2 = R.raw.light_on;
         }
         */
      } else if(var0.startsWith("Socket  ")) {
         if(var1 <= 0) {
            var2 = R.raw.socket_off;
         } else {
            var2 = R.raw.socket_on;
         }
         /*
      } else if(var0.startsWith("Micro Oven")) {
         if(var1 <= 0) {
            var2 = R.raw.microwave_off;
         } else {
            var2 = R.raw.microwave_on;
         }
         */
      } else if(var0.startsWith("Settop Box  ")) {
         if(var1 <= 0) {
            var2 = R.raw.settopbox_off;
         } else {
            var2 = R.raw.settopbox_on;
         }
      } else if(var0.startsWith("Water Pump  ")) {
         if(var1 <= 0) {
            var2 = R.raw.water_pump_off;
         } else {
            var2 = R.raw.water_pump_on;
         }
         
      } else if(var0.startsWith("Dim Fan  ")) {
          if(var1 <= 0) {
             var2 = R.raw.dfan_off;
          } else {
             var2 = R.raw.dfan_on;
          }
          
      } else if(var0.startsWith("Dim Socket  ")) {
          if(var1 <= 0) {
             var2 = R.raw.dsocket_off;
          } else {
             var2 = R.raw.dsocket_on;
          }
          
      } else if(var0.startsWith("Dim Chandlier  ")) {
          if(var1 <= 0) {
             var2 = R.raw.dchandelier_off;
          } else {
             var2 = R.raw.dchandelier_on;
          }
          
      } else if(var0.startsWith("Dim Light  ")) {
          if(var1 <= 0) {
             var2 = R.raw.dlight_off;
          } else {
             var2 = R.raw.dlight_on;
          }
      } else if(var0.startsWith("Dim TableLamp  ")) {
          if(var1 <= 0) {
             var2 = R.raw.dtablelamp_off;
          } else {
             var2 = R.raw.dtablelamp_on;
          }
         
      } else if(var0.startsWith("Dim DecoLight  ")) {
          if(var1 <= 0) {
             var2 = R.raw.dtable_off;
          } else {
             var2 = R.raw.dtable_on;
          }
         
      } else if(var0.startsWith("TV  ")) {
         if(var1 <= 0) {
            var2 = R.raw.tv_off;
         } else {
            var2 = R.raw.tv_on;
         }
      }

      return var2;
   }

   public static String isValidName(Activity var0, String name) {
      String validName = null;
      name = name.trim();
      if(name.isEmpty()) {
    	//-spb 010217 for error msg chg  AlertBox(var0, "Name", "Name is empty");
    	//-spb 270417 for errors  AlertBox(var0, "Can't add name ", "Kindly enter name");
    	  AlertBox(var0,  CommonUtils.getInstance().getErrorString("ERROR20"),  CommonUtils.getInstance().getErrorString("ERROR21"));
      } else if(name.length() > 12) {
    	//-spb 010217 for error msg chg AlertBox(var0, "Name", "Name should not be more than 12 chars");
    	//-spb 270417 for errors  AlertBox(var0, "Maximum limit reached !", "Name should not be more than 12 chars");
    	  AlertBox(var0, CommonUtils.getInstance().getErrorString("ERROR22"), CommonUtils.getInstance().getErrorString("ERROR23"));
      } else if(name.contains("#")) {
    	//-spb 010217 for error msg chg AlertBox(var0, "Name", "Name should not contain # letter in the name");
    	//-spb 270417 for errors  AlertBox(var0, "Special characters not allowed", "Name characters should be in A-Z and 0-9");
    	  AlertBox(var0, CommonUtils.getInstance().getErrorString("ERROR24"), CommonUtils.getInstance().getErrorString("ERROR25"));
      } else {
    	  validName = Character.toString(name.charAt(0)).toUpperCase() + name.substring(1);
      }

      return validName;
   }

   public static final int measureContentWidth(ViewGroup var0, ListAdapter var1) {
      ViewGroup var11 = null;
      int var2 = 0;
      View var10 = null;
      int var6 = 0;
      int var7 = MeasureSpec.makeMeasureSpec(0, 0);
      int var8 = MeasureSpec.makeMeasureSpec(0, 0);
      int var9 = var1.getCount();

      int var5;
      for(int var3 = 0; var3 < var9; var2 = var5) {
         var5 = var1.getItemViewType(var3);
         int var4 = var6;
         if(var5 != var6) {
            var4 = var5;
            var10 = null;
         }

         ViewGroup var12 = var11;
         if(var11 == null) {
            var12 = var0;
         }

         var10 = var1.getView(var3, var10, var12);
         var10.measure(var7, var8);
         var6 = var10.getMeasuredWidth();
         var5 = var2;
         if(var6 > var2) {
            var5 = var6;
         }

         ++var3;
         var6 = var4;
         var11 = var12;
      }

      return var2;
   }

   public static void setMaxNoDevices(int no) {
      max_no_devices = no;
   }
   
   public static int getMaxNoDevices() {
		return max_no_devices;
	}
   
   public static NetworkInfo getUnUsedIpInfo(Activity activity) {
	   NetworkInfo networkInfo = new NetworkInfo();
	   	WifiManager wifiManager = (WifiManager) activity.getSystemService (Context.WIFI_SERVICE);
		WifiInfo info = wifiManager.getConnectionInfo ();
		String ssid = info.getSSID ();
		if( ssid.isEmpty()) {
			return null;
		}
		ssid = ssid.substring(1, ssid.length()-1);
		String ip = Formatter.formatIpAddress(info.getIpAddress());
		String subnet = ip.substring(0,ip.lastIndexOf("."));
		networkInfo.unusedIndex = -1;
		for(int i=254; i>200; i--) {
			try {
				boolean isreachable = InetAddress.getByName(subnet+"."+i).isReachable(2000);
				if( !isreachable) {
					System.out.println("Isreachable..."+isreachable+" "+subnet+"."+i);
					networkInfo.unusedIndex = i;
					break;
				}
			} catch (Exception e) {}
		}
		networkInfo.ssid = ssid;
		networkInfo.subnet = subnet;
		return networkInfo;
   }
   
   public static void printStackTrace() {
	   String l = null;
		try{
			l.length();
		}catch(Exception e){
			e.printStackTrace();
		}
   }
   
	public static String enableNetwork(Activity activity, String networkSSID, String networkPass) {
		NetworkStateReceiver.removeDevice(networkSSID);
		String ipaddr = null;
		try {
			WifiManager wifiManager = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
			wifiManager.disconnect();
			List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
			int netId = -1;
			for (WifiConfiguration wc : list) {
				String ssid = wc.SSID.substring(1, wc.SSID.length()-1);
				if( ssid.equals(networkSSID)) {
					netId = wc.networkId;
					break;
				}
			}
			WifiConfiguration wc = new WifiConfiguration();
			wc.SSID = "\"" + networkSSID + "\"";
			wc.preSharedKey = "\"" + networkPass + "\"";
			wc.status = WifiConfiguration.Status.ENABLED;
			wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
			wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
			wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
			wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
			wifiManager.setWifiEnabled(true);
			if( netId == -1) {
				netId = wifiManager.addNetwork(wc);
			} 
			if( netId == -1){
				System.out.println("Not able to get ip");
				return null;
			}
			BtHwLayer.getInstance(activity).register();
			wifiManager.enableNetwork(netId, true);
			boolean isconnected = wifiManager.reconnect();
			if (isconnected) {
				boolean isSuccess = NetworkStateReceiver.waitReadyDevice(networkSSID);
				if( !isSuccess ) {
					return null;
				}
				ipaddr = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
				ipaddr = ipaddr.substring(0,ipaddr.lastIndexOf("."))+".1";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Enabling network ipaddr>>"+ipaddr);
		return ipaddr;
	}
	
	public void writeLog(String mesg) {
		 logContentBuf.append(mesg+"\n");
	}
	
	public String closeLog() {
		   return logContentBuf.toString();
		
	}
	
	public void deleteLog() {
		logContentBuf = null;
		instance = null;
	}
	
	public static void processMultipleNotification(byte[] readBytes, int noProcessedBytes, NotificationListener notificationListener, RoomsActivity activity) {
		byte numdevs = readBytes[noProcessedBytes + 2];
		byte[] data = new byte[numdevs*2];
		for (int i = 0; i < numdevs*2; i++) {
			int ith = noProcessedBytes+ i + 3;
			if( ith < readBytes.length) {
				data[i] = readBytes[ith];
			} else {
				printBytes("Some junk packets received: ith="+ith+" redabybytes length="+readBytes.length+" proceesed byts="+noProcessedBytes, readBytes);
				return;
			}
		}
		if( activity != null) {
			activity.notificationReceived(data);
		} else {
			notificationListener.notificationReceived(data);
		}
		noProcessedBytes = noProcessedBytes + 3 + numdevs*2;
		System.out.println("N>>>>>>>>>>>>>>>>>>>"+noProcessedBytes+" readbytes...."+readBytes.length);
		if( noProcessedBytes < readBytes.length) {
			processMultipleNotification(readBytes, noProcessedBytes, notificationListener, activity);
		}
	}

	public static String getPrintBytes(String tag, byte[] bytes) {
		String resp = "";
		for (int i = 0; i < bytes.length; i++) {
			resp += " " + Integer.toHexString(bytes[i]);
		}
		String msg = "Data:" + tag + " \t" + String.format("%03d", bytes.length) + ": " + resp;
		return msg;
	}
	
	public static void printBytes(String tag, byte[] bytes) {
		String resp = "";
		for (int i = 0; i < bytes.length; i++) {
			resp += " " + Integer.toHexString(bytes[i]);
		}
		String msg = "Data:" + tag + " \t" + String.format("%03d", bytes.length) + ": " + resp;
		System.out.println(msg);
		CommonUtils.getInstance().writeLog(msg);
	}
	
	public static boolean isActiveNetwork(Context context) {
		boolean isactive = false;
		try {
			ConnectivityManager cm = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);      
			isactive = (cm.getActiveNetworkInfo() != null);
		} catch(Exception e){
			
		}
		return isactive;
	}
	
	public static boolean isMobileDataConnection(Context context) {
		boolean isdata = false;
		try {
			ConnectivityManager cm = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);      
			isdata = (cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_MOBILE);
		} catch(Exception e){
		}
		return isdata;
	}
	
	int notificationId = 0;
	private int getNextNotificationNo() {
		notificationId++;
		return notificationId;
	}
	
	public  void addNotification(Activity context, String roomname, byte[] switchnamees, byte[] statuses) {
		
		String switchesstr = "";
		int index = 0;
		for(byte switchname:switchnamees){
			switchesstr += switchname+"/"+((statuses[index] == 0)?"Off":"On");
			index++;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		String currentDateandTime = sdf.format(new Date());
		String mesg = roomname+":"+switchesstr+" at "+currentDateandTime;
		  NotificationCompat.Builder builder =
	         new NotificationCompat.Builder(context)
	         .setSmallIcon(R.drawable.oohicon)
	         .setContentTitle("Zorba")
	         .setDefaults(Notification.DEFAULT_ALL)
	         .setPriority(NotificationCompat.PRIORITY_HIGH)
	         .setAutoCancel(true)
	         .setWhen(System.currentTimeMillis())
	         .setTicker("Zorba notification")
	         .setContentText(mesg);
	      Intent notificationIntent = new Intent(context, RoomsActivity.class);
	      PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent,
	         PendingIntent.FLAG_UPDATE_CURRENT);
	      builder.setContentIntent(contentIntent);

	      // Add as notification
	      NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	      manager.notify(getNextNotificationNo(), builder.build());
	   }

	public static int getTouchCount(View v) {
		return hiddenCountMap.get(v);
	}
	public static void increateCount(View v) {
		if( hiddenCountMap.containsKey(v)){
			hiddenCountMap.put(v, hiddenCountMap.get(v).intValue()+1);
		} else {
			hiddenCountMap.put(v, new Integer(0));
		}
	}
	public static void resetCount() {
		hiddenCountMap.clear();
	}

}

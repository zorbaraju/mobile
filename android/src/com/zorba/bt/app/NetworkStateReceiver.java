package com.zorba.bt.app;

import java.util.HashMap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkStateReceiver extends BroadcastReceiver {
	
	static HashMap<String, String> responseQueue = new HashMap<String, String>();
	static Object lock = new Object();
	   
    public void onReceive(Context context, Intent intent) {
    	System.out.println("Name of the devive connected iss............"+intent);
     if(intent.getExtras()!=null) {
    	 System.out.println("Name of the devive connected iss............"+intent.getExtras());
        NetworkInfo ni=(NetworkInfo) intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_INFO);
        System.out.println("Name of the devive connected iss............"+ni.getState());
        if(ni!=null && ni.getState()==NetworkInfo.State.CONNECTED) {
            String name= ni.getExtraInfo();
            System.out.println("Name of the devive connected iss............"+name);
            if( name == null ) {
            	return;
            }
            name = name.substring(1, name.length()-1);
			
            synchronized (lock) {
            	responseQueue.put(name, name);
                	lock.notifyAll();
			}
        }
     }
     if(intent.getExtras().getBoolean(ConnectivityManager.EXTRA_NO_CONNECTIVITY,Boolean.FALSE)) {
    	 System.out.println("There's no network connectivity");
     }
   }
    
    public static void removeDevice(String deviceName){
    	synchronized (lock) {
    		responseQueue.remove(deviceName);
    	}
    }
    public static boolean waitReadyDevice(String devicename){
    	boolean isSuccess = true;
    	int timeout = 0;
    	synchronized (lock) {
    		while( responseQueue.remove(devicename) == null)
				try {
					if( timeout >=30000) {
						isSuccess = false;
						break;
					}
					lock.wait(500);
					timeout += 500;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
    	}
    	return isSuccess;
    }
}

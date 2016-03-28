package com.zorba.bt.app;

import android.content.Context;

public class Logger {

	public static void e(Context context, String tag, String msg) {
		System.out.println("Error: "+tag+" : "+msg);
	}
}

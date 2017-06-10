package com.zorba.bt.app;

import com.zorba.bt.app.bluetooth.BtHwLayer;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.Window;

public class ZorbaActivity extends Activity {
	
	protected void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}

	
	@Override
	public void onResume() {
		super.onResume();
		BtHwLayer.getInstance(this).timeout();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		BtHwLayer.getInstance(this).timeout();
		return super.onTouchEvent(event);
	}
}

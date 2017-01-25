package com.zorba.bt.app;

import com.zorba.bt.app.bluetooth.BtHwLayer;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;

public abstract class ZorbaOnClickListener implements OnClickListener{

	@Override
	public void onClick(View v) {
		CommonUtils.resetCount();
		BtHwLayer.getInstance(null).timeout();
		zonClick(v);
	}

	public abstract void zonClick(View v);
}

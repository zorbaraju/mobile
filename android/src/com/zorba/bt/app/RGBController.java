package com.zorba.bt.app;

import com.chiralcode.colorpicker.ColorPicker;
import com.zorba.bt.app.bluetooth.BtHwLayer;
import com.zorba.bt.app.dao.RoomData;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class RGBController {

	Activity activity = null;
	public RGBController(Activity activity){
		this.activity = activity;
	}
	public void setRGBView(RoomData selectedRoom){
		BtHwLayer.getInstance(activity).initDevice(selectedRoom.getAddress());
		
		LinearLayout rgbPanel  = (LinearLayout)activity.findViewById(R.id.rgbPanel);
		((ScrollView) activity.findViewById(R.id.scrollView1)).setVisibility(View.GONE);
		((LinearLayout) activity.findViewById(R.id.rgbPanel)).setVisibility(View.VISIBLE);
		((LinearLayout) activity.findViewById(R.id.emptydevicepanel)).setVisibility(View.GONE);
		final ColorPicker colorPickerView = new ColorPicker(activity);
		int color = 0;
		try {
			byte[] irgb = BtHwLayer.getInstance(activity).readRGBToDevice();
			BtHwLayer.getInstance(activity).printBytes("Read...", irgb);
			color = (irgb[3] << 24) | (irgb[2] << 16) | (irgb[1] << 8) | irgb[0];
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		colorPickerView.setColor(color);
		System.out.println("setcolor...."+color+" ..."+Integer.toHexString(color));
		colorPickerView.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_UP) {
					int incolor = colorPickerView.getColor();
					byte si = (byte)(0x000000FF&(incolor>>24));
					byte sr = (byte)(0x000000FF&(incolor>>16));
					byte sg = (byte)(0x000000FF&(incolor>>8));
					byte sb = (byte)(0x000000FF&(incolor>>0));
					System.out.println("color...."+incolor+".."+Integer.toHexString(incolor)+" si="+Integer.toHexString(si)+" sr"+Integer.toHexString(sr)+" g="+Integer.toHexString(sg)+" b="+Integer.toHexString(sb));
					try {
						BtHwLayer.getInstance(activity).sendRGBToDevice(si,sr,sg,sb);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				return false;
			}
		});
		rgbPanel.removeAllViews();
		rgbPanel.addView(colorPickerView);
	}
}

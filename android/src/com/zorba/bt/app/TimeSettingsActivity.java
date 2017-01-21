package com.zorba.bt.app;

import com.zorba.bt.app.bluetooth.BtHwLayer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TimeSettingsActivity extends ZorbaActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_time_settings);
		constructTimePanel();
	}

	private void constructTimePanel() {
		LinearLayout roomContent = (LinearLayout) findViewById(R.id.settingspanel);
		
		LinearLayout timePanel = (LinearLayout) ((LayoutInflater)getSystemService("layout_inflater")).inflate(R.layout.timepanel, null);
		roomContent.addView(timePanel);
		Button setButton = (Button)findViewById(R.id.setTimeButton);
		Button getButton = (Button)findViewById(R.id.getTimeButton);
		final TextView timeLabel = (TextView)findViewById(R.id.timeLabel);
		setButton.setOnClickListener(new ZorbaOnClickListener() {
            public void zonClick(View v) {
				try {
					BtHwLayer.getInstance(TimeSettingsActivity.this).setDateAndTime();
					timeLabel.setText("Setting Time cmd is sent");
				} catch (Exception e) {
					timeLabel.setText("Error:"+e.getMessage());
				}
				
			}
		});
		
		getButton.setOnClickListener(new ZorbaOnClickListener() {
	         public void zonClick(View v) {
				try {
					timeLabel.setText("Get time cmd is being sent");
					String date = BtHwLayer.getInstance(TimeSettingsActivity.this).getDateAndTime();
					timeLabel.setText(date);
				} catch (Exception e) {
					timeLabel.setText("Error:"+e.getMessage());
				}
				
			}
		});
	}
}

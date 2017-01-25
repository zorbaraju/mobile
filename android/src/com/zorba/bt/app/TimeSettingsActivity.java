package com.zorba.bt.app;

import com.zorba.bt.app.bluetooth.BtHwLayer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TimeSettingsActivity extends ZorbaActivity {

	boolean isOOHEnabled = false;
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
		
		oohSettings();
		
		esbSettings();
		
		gatewaySettings();
	}
	
	private void oohSettings() {
		Button enableOOHButton = (Button)findViewById(R.id.enableOOHButton);
		Button readOOHButton = (Button)findViewById(R.id.readOOHStatusButton);
		final TextView oohStatusLabel = (TextView)findViewById(R.id.oohStatusLabel);
		enableOOHButton.setOnClickListener(new ZorbaOnClickListener() {
            public void zonClick(View v) {
				try {
					BtHwLayer.getInstance(TimeSettingsActivity.this).enableOOHCmd(!isOOHEnabled);
				} catch (Exception e) {
					CommonUtils.AlertBox(TimeSettingsActivity.this, "Enable Error", e.getMessage());
				}
				
			}
		});
		
		readOOHButton.setOnClickListener(new ZorbaOnClickListener() {
	         public void zonClick(View v) {
				try {
					isOOHEnabled = BtHwLayer.getInstance(TimeSettingsActivity.this).readOOHStatus();
					if(isOOHEnabled)
						oohStatusLabel.setText("Status is ON");
					else 
						oohStatusLabel.setText("Status is Off");
				} catch (Exception e) {
					oohStatusLabel.setText("Error:"+e.getMessage());
					CommonUtils.AlertBox(TimeSettingsActivity.this, "Read Error", e.getMessage());
				}
				
			}
		});
	}
	
	private void esbSettings() {
		Button resetESBButton = (Button)findViewById(R.id.resetESBButton);
		resetESBButton.setOnClickListener(new ZorbaOnClickListener() {
            public void zonClick(View v) {
				try {
					BtHwLayer.getInstance(TimeSettingsActivity.this).resetESBCmd();
				} catch (Exception e) {
					CommonUtils.AlertBox(TimeSettingsActivity.this, "Reset Error", e.getMessage());
				}
				
			}
		});
	}
	
	private void gatewaySettings() {
		Button setGatewayIPButton = (Button)findViewById(R.id.setGatewayIPButton);
		final EditText gatewaylabel = (EditText)findViewById(R.id.gatewaylabel);
		setGatewayIPButton.setOnClickListener(new ZorbaOnClickListener() {
            public void zonClick(View v) {
				try {
					BtHwLayer.getInstance(TimeSettingsActivity.this).setGatewayIPCmd(gatewaylabel.getText().toString());
				} catch (Exception e) {
					CommonUtils.AlertBox(TimeSettingsActivity.this, "Set gateway Error", e.getMessage());
				}
				
			}
		});
	}
	
}

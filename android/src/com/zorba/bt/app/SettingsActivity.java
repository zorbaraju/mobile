package com.zorba.bt.app;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.zorba.bt.app.bluetooth.BtHwLayer;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SettingsActivity extends ZorbaActivity {

	boolean isOOHEnabled = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settingspanel);
		TabView tabView = (TabView) findViewById(R.id.tabView1);
		tabView.addTab("Time", R.layout.timesettings);
		tabView.addTab("OOH", R.layout.oohsettings);
		tabView.addTab("ESB", R.layout.esbsettings);
		tabView.addTab("Gateway", R.layout.gatewaysettings);
		tabView.selectTab("Time");
		OnClickListener listener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				String tn = ((Button) v).getText().toString();
				if (tn.equals("Time"))
					timeSettings();
				else if (tn.equals("OOH"))
					oohSettings();
				else if (tn.equals("ESB"))
					esbSettings();
				else if (tn.equals("Gateway"))
					gatewaySettings();
			}
		};
		tabView.setTabSelectionListener(listener);
	}

	private void timeSettings() {
		Button setButton = (Button) findViewById(R.id.setTimeButton);
		Button getButton = (Button) findViewById(R.id.getTimeButton);
		final TextView timeLabel = (TextView) findViewById(R.id.timeLabel);
		setButton.setOnClickListener(new ZorbaOnClickListener() {
			public void zonClick(View v) {
				try {
					BtHwLayer.getInstance(SettingsActivity.this).setDateAndTime();
					timeLabel.setText("Setting Time cmd is sent");
				} catch (Exception e) {
					timeLabel.setText("Error:" + e.getMessage());
				}

			}
		});

		getButton.setOnClickListener(new ZorbaOnClickListener() {
			public void zonClick(View v) {
				try {
					timeLabel.setText("Get time cmd is being sent");
					byte datedata[] = BtHwLayer.getInstance(SettingsActivity.this).getDateAndTime();
					Calendar cal = Calendar.getInstance();
					cal.set(Calendar.DAY_OF_MONTH, datedata[0]);
					cal.set(Calendar.MONTH, datedata[1] - 1);
					cal.set(Calendar.YEAR, 2000 + datedata[2]);
					cal.set(Calendar.HOUR_OF_DAY, datedata[4]);
					cal.set(Calendar.MINUTE, datedata[5]);
					cal.set(Calendar.SECOND, datedata[6]);
					System.out.println("cal..." + cal);
					SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
					String currentDateandTime = sdf.format(cal.getTime());
					timeLabel.setText(currentDateandTime);
				} catch (Exception e) {
					timeLabel.setText("Error:" + e.getMessage());
				}

			}
		});

	}

	private void oohSettings() {
		Button enableOOHButton = (Button) findViewById(R.id.enableOOHButton);
		Button readOOHButton = (Button) findViewById(R.id.readOOHStatusButton);
		final TextView oohStatusLabel = (TextView) findViewById(R.id.oohStatusLabel);
		enableOOHButton.setOnClickListener(new ZorbaOnClickListener() {
			public void zonClick(View v) {
				try {
					BtHwLayer.getInstance(SettingsActivity.this).enableOOHCmd(!isOOHEnabled);
				} catch (Exception e) {
					CommonUtils.AlertBox(SettingsActivity.this, "Enable Error", e.getMessage());
				}

			}
		});

		readOOHButton.setOnClickListener(new ZorbaOnClickListener() {
			public void zonClick(View v) {
				try {
					isOOHEnabled = BtHwLayer.getInstance(SettingsActivity.this).readOOHStatus();
					if (isOOHEnabled)
						oohStatusLabel.setText("Status is ON");
					else
						oohStatusLabel.setText("Status is Off");
				} catch (Exception e) {
					oohStatusLabel.setText("Error:" + e.getMessage());
					CommonUtils.AlertBox(SettingsActivity.this, "Read Error", e.getMessage());
				}

			}
		});
	}

	private void esbSettings() {
		Button resetESBButton = (Button) findViewById(R.id.resetESBButton);
		resetESBButton.setOnClickListener(new ZorbaOnClickListener() {
			public void zonClick(View v) {
				try {
					BtHwLayer.getInstance(SettingsActivity.this).resetESBCmd();
				} catch (Exception e) {
					CommonUtils.AlertBox(SettingsActivity.this, "Reset Error", e.getMessage());
				}

			}
		});
	}

	private void gatewaySettings() {
		Button setGatewayIPButton = (Button) findViewById(R.id.setGatewayIPButton);
		final EditText gatewaylabel = (EditText) findViewById(R.id.gatewaylabel);
		setGatewayIPButton.setOnClickListener(new ZorbaOnClickListener() {
			public void zonClick(View v) {
				try {
					BtHwLayer.getInstance(SettingsActivity.this).setGatewayIPCmd(gatewaylabel.getText().toString());
				} catch (Exception e) {
					CommonUtils.AlertBox(SettingsActivity.this, "Set gateway Error", e.getMessage());
				}

			}
		});
	}

}

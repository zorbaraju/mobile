package com.zorba.bt.app;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import com.zorba.bt.app.bluetooth.BtHwLayer;
import com.zorba.bt.app.db.BtLocalDB;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SettingsActivity extends ZorbaActivity {

	boolean isOOHEnabled = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settingspanel);
		TabView tabView = (TabView) findViewById(R.id.tabView1);
		tabView.addTab("CHANGE TIME", R.layout.timesettings);
		tabView.addTab("OUT OF HOME SETTINGS", R.layout.oohsettings);
		//+spb 290117 for adding password page to settings page	tabView.addTab("ESB", R.layout.esbsettings);
		tabView.addTab("GATEWAY SETTINGS", R.layout.gatewaysettings);
		tabView.addTab("PASSWORD", R.layout.changepwd);//+spb 290117 for adding password page to settings page
		tabView.selectTab("CHANGE TIME");
		OnClickListener listener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				String tn = ((Button) v).getText().toString();
				if (tn.equals("CHANGE TIME"))
					timeSettings();
				else if (tn.equals("OUT OF HOME SETTINGS"))
					oohSettings();
				//+spb 290117 for adding password page to settings page
				//else if (tn.equals("ESB"))
					//esbSettings();
				//+spb 290117 for adding password page to settings page
				else if (tn.equals("GATEWAY SETTINGS"))
					gatewaySettings();
				//+spb 290117 for adding password page to settings page
				else if (tn.equals("PASSWORD"))
					changepwdSettings();
				//+spb 290117 for adding password page to settings page
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
					timeLabel.setText("Zorba Time is updated Successfully");
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
		Button resetESBButton = (Button) findViewById(R.id.resetESBButton);//+spb 290117 for ecb shift to ooh
			
		final TextView oohStatusLabel = (TextView) findViewById(R.id.oohStatusLabel);
		enableOOHButton.setOnClickListener(new ZorbaOnClickListener() {
			public void zonClick(View v) {
				try {
					BtHwLayer.getInstance(SettingsActivity.this).enableOOHCmd(!isOOHEnabled);
				} catch (Exception e) {
					CommonUtils.AlertBox(SettingsActivity.this, "Enable Error", e.getMessage());
				}
				//esbSettings();

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

	
	//+spb 290117 for ecb shift to ooh
	/*
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
	*/
	//+spb 290117 for ecb shift to ooh

	//+spb 290117 for adding password page to settings page
	private void changepwdSettings() {
		Button cancelButton = (Button)findViewById(R.id.cancelButton);
		OnClickListener cancelListener = new ZorbaOnClickListener() {
	         public void zonClick(View v) {
				finish();
			}
		};
		cancelButton.setOnClickListener(cancelListener);
		
		Button saveButton = (Button)findViewById(R.id.saveButton);
		OnClickListener saveListener = new ZorbaOnClickListener() {
	         public void zonClick(View v) {
				EditText currPwdText = (EditText)findViewById(R.id.currentPwdText);
				EditText newPwdText = (EditText)findViewById(R.id.newPwdText);
				EditText confirmPwdText = (EditText)findViewById(R.id.confirmPwdText);
				String currPwdStr = currPwdText.getText().toString();
				if( !currPwdStr.equals(BtLocalDB.getInstance(SettingsActivity.this).getDevicePwd()) ){
					CommonUtils.AlertBox(SettingsActivity.this, "Pwd Error", "Current pwd is not matched");
					return;
				}
				if( newPwdText.getText().toString().isEmpty()){
					CommonUtils.AlertBox(SettingsActivity.this, "Pwd Error", "Please enter new pwd");
					return;
				}
				if( !newPwdText.getText().toString().equals(confirmPwdText.getText().toString()) ) {
					CommonUtils.AlertBox(SettingsActivity.this, "Pwd Error", "Confirm pwd is not matched");
					return;
				}
				BtLocalDB.getInstance(SettingsActivity.this).setDevicePwd(newPwdText.getText().toString());
				finish();
			}
		};
		saveButton.setOnClickListener(saveListener);
	}
	


	//+spb 290117 for adding password page to settings page
	
	
	
	

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

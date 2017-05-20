package com.zorba.bt.app;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import com.zorba.bt.app.bluetooth.BtHwLayer;
import com.zorba.bt.app.db.BtLocalDB;
import com.zorba.bt.app.utils.BackgroundTaskDialog;

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
		tabView.addTab("TIME", R.layout.timesettings);
		tabView.addTab("OOHO", R.layout.oohsettings);
		tabView.addTab("GATEWAY", R.layout.gatewaysettings);
		tabView.addTab("SECURITY", R.layout.changepwd);
		tabView.selectTab("TIME");
		timeSettings();
		OnClickListener listener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				String tn = ((Button) v).getText().toString();
				if (tn.equals("TIME"))
					timeSettings();
				else if (tn.equals("OOHO"))
					oohSettings();
				else if (tn.equals("GATEWAY"))
					gatewaySettings();
				else if (tn.equals("SECURITY"))
					changepwdSettings();
			}
		};
		tabView.setTabSelectionListener(listener);
	}

	private void timeSettings() {
		final TextView timeLabel = (TextView) findViewById(R.id.timeLabel);
		((SvgView) this.findViewById(R.id.setTimeButton)).setOnClickListener(new ZorbaOnClickListener() {

			public void zonClick(View v) {
				try {
					BtHwLayer.getInstance(SettingsActivity.this).setDateAndTime();
					timeLabel.setText("Time updated successfully");
				} catch (Exception e) {
					timeLabel.setText("Error:No connection with the device");
				}

			}
		});
		((SvgView) this.findViewById(R.id.getTimeButton)).setOnClickListener(new ZorbaOnClickListener() {

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
					// -spb 060217 for aligning error timeLabel.setText("Error:"
					// + e.getMessage());
					timeLabel.setText("Error:No connection with the device");
				}

			}
		});

	}

	private void oohSettings() {

		// -spb 030217
		// Button enableOOHButton = (Button) findViewById(R.id.enableOOHButton);
		// Button readOOHButton = (Button)
		// findViewById(R.id.readOOHStatusButton);
		// Button resetESBButton = (Button)
		// findViewById(R.id.resetESBButton);//+spb 290117 for ecb shift to ooh
		// -spb 030217
		final TextView oohStatusLabel = (TextView) findViewById(R.id.oohStatusLabel);

		((SvgView) this.findViewById(R.id.enableOOHButton)).setOnClickListener(new ZorbaOnClickListener() {

			public void zonClick(View v) {
				try {
					BtHwLayer.getInstance(SettingsActivity.this).enableOOHCmd(true);
				} catch (Exception e) {
					CommonUtils.AlertBox(SettingsActivity.this, CommonUtils.getInstance().getErrorString("ERROR71"),
							CommonUtils.getInstance().getErrorString("ERROR72"));
				}
			}
		});
		
		((SvgView) this.findViewById(R.id.disableOOHButton)).setOnClickListener(new ZorbaOnClickListener() {

			public void zonClick(View v) {
				try {
					BtHwLayer.getInstance(SettingsActivity.this).enableOOHCmd(false);
				} catch (Exception e) {
					CommonUtils.AlertBox(SettingsActivity.this, CommonUtils.getInstance().getErrorString("ERROR71"),
							CommonUtils.getInstance().getErrorString("ERROR72"));
				}
			}
		});

		// -spb 030217 readOOHButton.setOnClickListener(new
		// ZorbaOnClickListener() {
		((SvgView) this.findViewById(R.id.readOOHStatusButton)).setOnClickListener(new ZorbaOnClickListener() {

			public void zonClick(View v) {
				try {
					isOOHEnabled = BtHwLayer.getInstance(SettingsActivity.this).readOOHStatus();
					if (isOOHEnabled)
						oohStatusLabel.setText("Status is ON");
					else
						oohStatusLabel.setText("Status is Off");
				} catch (Exception e) {
					// -spb 060217 for aligning error
					// oohStatusLabel.setText("Error:" + e.getMessage());
					oohStatusLabel.setText("Error: No connection with the device");
					// -spb 060217 for aligning error
					// CommonUtils.AlertBox(SettingsActivity.this, "Read Error",
					// e.getMessage());
					// -spb 270417 for errors
					// CommonUtils.AlertBox(SettingsActivity.this, "Read Error",
					// "AWS connection error");
					CommonUtils.AlertBox(SettingsActivity.this, CommonUtils.getInstance().getErrorString("ERROR47"),
							CommonUtils.getInstance().getErrorString("ERROR73"));
				}

			}
		});

		// -spb 030217 resetESBButton.setOnClickListener(new
		// ZorbaOnClickListener() {
		((SvgView) this.findViewById(R.id.resetESBButton)).setOnClickListener(new ZorbaOnClickListener() {

			public void zonClick(View v) {
				try {
					BtHwLayer.getInstance(SettingsActivity.this).resetESBCmd();
				} catch (Exception e) {
					// -spb 060217 for aligning error
					// CommonUtils.AlertBox(SettingsActivity.this, "Reset
					// Error", e.getMessage());
					// -spb 270417 for errors
					// CommonUtils.AlertBox(SettingsActivity.this, "Reset
					// Error","Unable to reset the ESB");
					CommonUtils.AlertBox(SettingsActivity.this, CommonUtils.getInstance().getErrorString("ERROR74"),
							CommonUtils.getInstance().getErrorString("ERROR75"));
				}

			}
		});
	}

	// +spb 290117 for ecb shift to ooh
	/*
	 * private void esbSettings() { Button resetESBButton = (Button)
	 * findViewById(R.id.resetESBButton); resetESBButton.setOnClickListener(new
	 * ZorbaOnClickListener() { public void zonClick(View v) { try {
	 * BtHwLayer.getInstance(SettingsActivity.this).resetESBCmd(); } catch
	 * (Exception e) { CommonUtils.AlertBox(SettingsActivity.this, "Reset Error"
	 * , e.getMessage()); }
	 * 
	 * } }); }
	 */
	// +spb 290117 for ecb shift to ooh

	// +spb 290117 for adding password page to settings page
	private void changepwdSettings() {
		// -spb 030217 Button cancelButton =
		// (Button)findViewById(R.id.cancelButton);
		// -spb 030217 OnClickListener cancelListener = new
		// ZorbaOnClickListener() {
		((SvgView) this.findViewById(R.id.cancelButton)).setOnClickListener(new ZorbaOnClickListener() {

			public void zonClick(View v) {
				finish();
			}
		});
		// -spb 030217 cancelButton.setOnClickListener(cancelListener);

		// -spb 030217 Button saveButton =
		// (Button)findViewById(R.id.saveButton);
		// -spb 030217 OnClickListener saveListener = new ZorbaOnClickListener()
		// {
		((SvgView) this.findViewById(R.id.saveButton)).setOnClickListener(new ZorbaOnClickListener() {

			public void zonClick(View v) {
				EditText currPwdText = (EditText) findViewById(R.id.currentPwdText);
				EditText newPwdText = (EditText) findViewById(R.id.newPwdText);
				EditText confirmPwdText = (EditText) findViewById(R.id.confirmPwdText);
				String currPwdStr = currPwdText.getText().toString();
				if (!currPwdStr.equals(BtLocalDB.getInstance(SettingsActivity.this).getDevicePwd())) {
					// -spb 270417 for errors
					// CommonUtils.AlertBox(SettingsActivity.this, "Pwd Error",
					// "Current pwd is not matched");
					CommonUtils.AlertBox(SettingsActivity.this, CommonUtils.getInstance().getErrorString("ERROR66"),
							CommonUtils.getInstance().getErrorString("ERROR67"));
					return;
				}
				if (newPwdText.getText().toString().isEmpty()) {
					// -spb 270417 for errors
					// CommonUtils.AlertBox(SettingsActivity.this, "Pwd Error",
					// "Please enter new pwd");
					CommonUtils.AlertBox(SettingsActivity.this, CommonUtils.getInstance().getErrorString("ERROR66"),
							CommonUtils.getInstance().getErrorString("ERROR68"));
					return;
				}
				if (!newPwdText.getText().toString().equals(confirmPwdText.getText().toString())) {
					// -spb 270417 for errors
					// CommonUtils.AlertBox(SettingsActivity.this, "Pwd Error",
					// "Confirm pwd is not matched");
					CommonUtils.AlertBox(SettingsActivity.this, CommonUtils.getInstance().getErrorString("ERROR66"),
							CommonUtils.getInstance().getErrorString("ERROR69"));
					return;
				}
				BtLocalDB.getInstance(SettingsActivity.this).setDevicePwd(newPwdText.getText().toString());
				finish();
			}
		});
	}
	
	private void gatewaySettings() {

		// -spb 030217 Button setGatewayIPButton = (Button)
		// findViewById(R.id.setGatewayIPButton);
		final EditText gatewaylabel = (EditText) findViewById(R.id.gatewaylabel);

		// -spb 030217 setButton.setOnClickListener(new ZorbaOnClickListener() {

		// -spb 030217 setGatewayIPButton.setOnClickListener(new
		// ZorbaOnClickListener() {
		((SvgView) this.findViewById(R.id.setGatewayIPButton)).setOnClickListener(new ZorbaOnClickListener() {

			public void zonClick(View v) {
				
				final String ip = gatewaylabel.getText().toString().trim();
				if( ip.isEmpty()){
					CommonUtils.AlertBox(SettingsActivity.this, "Gateway Ip", "Please enter gateway ip");
					return;	
				}
				BackgroundTaskDialog task = new BackgroundTaskDialog(SettingsActivity.this) {
					
					@Override
					public Object runTask(Object params) {
						try {
						BtHwLayer.getInstance(SettingsActivity.this).setGatewayIPCmd(ip);
						} catch (Exception e) {
							CommonUtils.AlertBox(SettingsActivity.this, CommonUtils.getInstance().getErrorString("ERROR69"),
									CommonUtils.getInstance().getErrorString("ERROR70"));
						}
						return null;
					}
					
					@Override
					public void finishedTask(Object result) {
						// TODO Auto-generated method stub
						
					}
				};
			}
		});
	}

}

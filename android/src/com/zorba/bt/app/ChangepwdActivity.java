package com.zorba.bt.app;

import com.zorba.bt.app.db.BtLocalDB;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class ChangepwdActivity extends ZorbaActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.changepwd);
		
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
				if( !currPwdStr.equals(BtLocalDB.getInstance(ChangepwdActivity.this).getDevicePwd()) ){
					//-spb 010217 for error msg chg CommonUtils.AlertBox(ChangepwdActivity.this, "Pwd Error", "Current pwd is not matched");
					//-spb 270417 for errors CommonUtils.AlertBox(ChangepwdActivity.this, "Incorrect password", "Kindly enter correct password");
					CommonUtils.AlertBox(ChangepwdActivity.this,  CommonUtils.getInstance().getErrorString("ERROR15"),  CommonUtils.getInstance().getErrorString("ERROR16"));
					return;
				}
				if( newPwdText.getText().toString().isEmpty()){
					//-spb 010217 for error msg chg CommonUtils.AlertBox(ChangepwdActivity.this, "Pwd Error", "Please enter new pwd");
					//-spb 270417 for errors CommonUtils.AlertBox(ChangepwdActivity.this, "Incorrect password", "Kindly enter new password");
					 CommonUtils.AlertBox(ChangepwdActivity.this,  CommonUtils.getInstance().getErrorString("ERROR15"),  CommonUtils.getInstance().getErrorString("ERROR17"));
					return;
				}
				if( !newPwdText.getText().toString().equals(confirmPwdText.getText().toString()) ) {
					//-spb 010217 for error msg chg  CommonUtils.AlertBox(ChangepwdActivity.this, "Pwd Error", "Confirm pwd is not matched");
					//-spb 270417 for errors CommonUtils.AlertBox(ChangepwdActivity.this, "Password mismatch", "Confirm password is not matched");
					 CommonUtils.AlertBox(ChangepwdActivity.this,  CommonUtils.getInstance().getErrorString("ERROR18"),  CommonUtils.getInstance().getErrorString("ERROR19"));
					return;
				}
				BtLocalDB.getInstance(ChangepwdActivity.this).setDevicePwd(newPwdText.getText().toString());
				finish();
			}
		};
		saveButton.setOnClickListener(saveListener);
	}
	
}


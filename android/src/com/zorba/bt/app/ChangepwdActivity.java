package com.zorba.bt.app;

import com.zorba.bt.app.db.BtLocalDB;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class ChangepwdActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.changepwd);
		
		Button cancelButton = (Button)findViewById(R.id.cancelButton);
		OnClickListener cancelListener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		};
		cancelButton.setOnClickListener(cancelListener);
		
		Button saveButton = (Button)findViewById(R.id.saveButton);
		OnClickListener saveListener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				EditText currPwdText = (EditText)findViewById(R.id.currentPwdText);
				EditText newPwdText = (EditText)findViewById(R.id.newPwdText);
				EditText confirmPwdText = (EditText)findViewById(R.id.confirmPwdText);
				String currPwdStr = currPwdText.getText().toString();
				if( !currPwdStr.equals(BtLocalDB.getInstance(ChangepwdActivity.this).getDevicePwd()) ){
					CommonUtils.AlertBox(ChangepwdActivity.this, "Pwd Error", "Current pwd is not matched");
					return;
				}
				if( newPwdText.getText().toString().isEmpty()){
					CommonUtils.AlertBox(ChangepwdActivity.this, "Pwd Error", "Please enter new pwd");
					return;
				}
				if( !newPwdText.getText().toString().equals(confirmPwdText.getText().toString()) ) {
					CommonUtils.AlertBox(ChangepwdActivity.this, "Pwd Error", "Confirm pwd is not matched");
					return;
				}
				BtLocalDB.getInstance(ChangepwdActivity.this).setDevicePwd(newPwdText.getText().toString());
				finish();
			}
		};
		saveButton.setOnClickListener(saveListener);
	}
	
}


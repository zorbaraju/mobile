package com.zorba.bt.app;

import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGParser;
import com.zorba.bt.app.bluetooth.AwsConnection;
import com.zorba.bt.app.db.BtLocalDB;

import android.content.Intent;
import android.graphics.Picture;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;

public class WelcomeActivity extends ZorbaActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		System.out.println("Connection type mobile:"+CommonUtils.isMobileDataConnection(this));
		setContentView(R.layout.activity_welcome);
		SvgView iview = (SvgView)findViewById(R.id.svgviewid);
		iview.setSvgDrawable(R.raw.welcome);
		RadioButton masterbox = (RadioButton)findViewById(R.id.master);
		masterbox.setChecked(true);
		Button gotoButton = (Button)findViewById(R.id.gotobutton);
		gotoButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View paramAnonymousView1) {
				
				String emailid = getValidEmailId();
				if( emailid == null) {
					CommonUtils.AlertBox(WelcomeActivity.this, "Error", "Enter valid email id");
					return;
				}
				RadioButton masterbox = (RadioButton)findViewById(R.id.master);
				saveEmailIdAndUserType(emailid, masterbox.isChecked());
				Intent welcomeIntent = new Intent(WelcomeActivity.this, MainActivity.class);
				welcomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
				WelcomeActivity.this.startActivity(welcomeIntent);
			}
		});
		EditText emailfield = (EditText)findViewById(R.id.emailfield);
		emailfield.requestFocus();
	}
	
	private String getValidEmailId() {
		EditText emailfield = (EditText)findViewById(R.id.emailfield);
		String emailid = emailfield.getText().toString();
		emailid = emailid.trim();
		if( emailid.isEmpty())
			return null;
		return emailid;
	}
	
	private void saveEmailIdAndUserType(String emailid, boolean isMaster) {
		BtLocalDB.getInstance(this).setEmailId(emailid);
		BtLocalDB.getInstance(this).setUserType(isMaster);
	}
}

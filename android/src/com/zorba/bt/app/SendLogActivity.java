package com.zorba.bt.app;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;

import com.zorba.bt.app.bluetooth.BtHwLayer;
import com.zorba.bt.app.utils.BackgroundTask;

public class SendLogActivity extends ZorbaActivity {
	TextView logView = null;
	ScrollView scrlv = null;

	protected void onCreate(Bundle var1) {
		super.onCreate(var1);
		this.setContentView(R.layout.sendlog);

		scrlv = (ScrollView) findViewById(R.id.scrlv);
		logView = (TextView) findViewById(R.id.logView);
		String content = CommonUtils.getInstance().closeLog();

		logView.setText(content);
		scrlv.fullScroll(View.FOCUS_DOWN);
		Button sendLogButton = (Button) findViewById(R.id.sendLog);
		sendLogButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				sendEmail();

			}
		});
		Button writeButton = (Button) findViewById(R.id.writeButton);
		writeButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				TextView writeView = (TextView) findViewById(R.id.writeTextView);
				String str = writeView.getText().toString();
				if (str.length() % 2 == 1) {
					CommonUtils.AlertBox(SendLogActivity.this, "Write Error", "Write even digits");
					return;
				}
				try {
					int numberBytes = str.length() / 2;
					byte wbytes[] = new BigInteger(str, 16).toByteArray();
					System.out.println("num..." + numberBytes + " wbytes=" + wbytes.length);
					BtHwLayer.getInstance(SendLogActivity.this).sendRawBytes(wbytes);
				} catch (Exception e) {
					CommonUtils.getInstance().writeLog(str+":"+e.getMessage());
					CommonUtils.AlertBox(SendLogActivity.this, "Write Error", e.getMessage());
				}
				String content = CommonUtils.getInstance().closeLog();

				logView.setText(content);
				scrlv.fullScroll(View.FOCUS_DOWN);
			}
		});
	}

	protected void sendEmail() {
		Logger.e(this, "email", "Send email");
		Intent var2 = new Intent("android.intent.action.SEND");
		var2.setData(Uri.parse("mailto:"));
		var2.setType("text/plain");
		var2.putExtra("android.intent.extra.EMAIL", new String[] { "uniraju@gmail.com" });
		var2.putExtra("android.intent.extra.CC", new String[] { "" });
		var2.putExtra("android.intent.extra.SUBJECT", "Bt Home Log");
		var2.putExtra("android.intent.extra.TEXT", logView.getText());
		// File var1 = new File(Environment.getExternalStorageDirectory(),
		// "bthome.log");
		// var2.putExtra("android.intent.extra.STREAM", Uri.parse("file://" +
		// var1));

		try {
			this.startActivity(Intent.createChooser(var2, "Send mail..."));
			finish();
			//CommonUtils.getInstance().deleteLog();
			Logger.e(this, "email", "Finished sending email...");

		} catch (ActivityNotFoundException var3) {
			Toast.makeText(this, "There is no email client installed.", 0).show();
		}

	}
}

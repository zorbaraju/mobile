package com.zorba.bt.app;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigInteger;

import com.zorba.bt.app.bluetooth.BtHwLayer;

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
		sendLogButton.setOnClickListener(new ZorbaOnClickListener() {
	         public void zonClick(View v) {
				sendEmail();

			}
		});
		Button writeButton = (Button) findViewById(R.id.writeButton);
		writeButton.setOnClickListener(new ZorbaOnClickListener() {
	         public void zonClick(View v) {
				TextView writeView = (TextView) findViewById(R.id.writeTextView);
				String str = writeView.getText().toString();
				if (str.length() % 2 == 1) {
					//-spb 270417 for errors CommonUtils.AlertBox(SendLogActivity.this, "Write Error", "Write even digits");
					CommonUtils.AlertBox(SendLogActivity.this,  CommonUtils.getInstance().getErrorString("ERROR64"),  CommonUtils.getInstance().getErrorString("ERROR65"));
					return;
				}
				try {
					int numberBytes = str.length() / 2;
					byte wbytes[] = new BigInteger(str, 16).toByteArray();
					System.out.println("num..." + numberBytes + " wbytes=" + wbytes.length);
					BtHwLayer.getInstance(SendLogActivity.this).sendRawBytes(wbytes);
				} catch (Exception e) {
					CommonUtils.getInstance().writeLog(str+":"+e.getMessage());
					//-spb 270417 for errors CommonUtils.AlertBox(SendLogActivity.this, "Write Error", e.getMessage());
					CommonUtils.AlertBox(SendLogActivity.this, CommonUtils.getInstance().getErrorString("ERROR64"), e.getMessage());
				}
				String content = CommonUtils.getInstance().closeLog();

				logView.setText(content);
				scrlv.fullScroll(View.FOCUS_DOWN);
			}
		});
	}

	protected void sendEmail() {
		Logger.e(this, "email", "Send email");
		Intent emailIntent = new Intent(Intent.ACTION_SEND);
		emailIntent.setData(Uri.parse("mailto:"));
		emailIntent.setType("text/plain");
		emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { "zorbasupp@gmail.com" });
		emailIntent.putExtra(Intent.EXTRA_CC, new String[] { "" });
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Bt Home Log");
		emailIntent.putExtra(Intent.EXTRA_TEXT, logView.getText());
		emailIntent.setType("message/rfc822");
		try {
			this.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
			finish();
			//CommonUtils.getInstance().deleteLog();
			Logger.e(this, "email", "Finished sending email...");

		} catch (ActivityNotFoundException var3) {
			Toast.makeText(this, "There is no email client installed.", 0).show();
		}

	}
}

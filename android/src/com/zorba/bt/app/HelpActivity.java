package com.zorba.bt.app;

import java.io.IOException;
import java.io.InputStream;

import com.zorba.bt.app.db.BtLocalDB;

import android.os.Bundle;
import android.text.Html;
import android.webkit.WebView;
import android.widget.TextView;

public class HelpActivity extends ZorbaActivity {
	protected void onCreate(Bundle var1) {
		super.onCreate(var1);
		this.setContentView(R.layout.help);
		try {
			String deviceConfig = BtLocalDB.getInstance(this).getConfiguration();
			final String mimeType = "text/html";
			final String encoding = "UTF-8";
			InputStream in = getAssets().open("help.html");
			byte[] buffer = new byte[in.available()];
			in.read(buffer);
			in.close();
			((WebView) this.findViewById(R.id.helpView)).loadData(new String(buffer) + " <br/><br/>" + deviceConfig,
					mimeType, encoding);
		} catch (IOException e) {
			e.printStackTrace();
		}

		/*
		 * ((WebView)this.findViewById(R.id.helpView)).loadUrl(
		 * "file:///android_asset/help.html"); TextView html = new
		 * TextView(getApplicationContext()); html.setText(Html.fromHtml());
		 * ((WebView)this.findViewById(R.id.helpView)).addView(html);
		 */
	}
}

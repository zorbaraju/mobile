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
			WebView engine = ((WebView) this.findViewById(R.id.helpView));
			engine.getSettings().setJavaScriptEnabled(true);
			String deviceConfig = BtLocalDB.getInstance(this).getConfiguration();
			final String mimeType = "text/html";
			final String encoding = "utf-8";
			InputStream in = getAssets().open("help.html");
			byte[] buffer = new byte[in.available()];
			in.read(buffer);
			in.close();
			String data = new String(buffer) ;
			data = data.replace("ReplaceConfig",deviceConfig);
			//data = "<html><body>Youtube video .. <br> <iframe class=\"youtube-player\" type=\"text/html\" width=\"640\" height=\"385\" src=\"http://www.youtube.com/embed/bIPcobKMB94\" frameborder=\"0\"></body></html>";
			engine.loadDataWithBaseURL("", data, mimeType, encoding, null);
		
			//-spb 250117 for video link engine.loadUrl("https://www.youtube.com/watch?v=hYMpMt0lwUY");
			
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

package com.zorba.bt.app;

import android.os.Bundle;
import android.webkit.WebView;

public class HelpActivity extends ZorbaActivity {
   protected void onCreate(Bundle var1) {
      super.onCreate(var1);
      this.setContentView(R.layout.help);
      ((WebView)this.findViewById(R.id.helpView)).loadUrl("file:///android_asset/help.html");
   }
}

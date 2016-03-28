package com.zorba.bt.app;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;
import java.io.File;

public class SendLogActivity extends ZorbaActivity {
   protected void onCreate(Bundle var1) {
      super.onCreate(var1);
      this.setContentView(2130903066);
      (new Thread(new Runnable() {
         public void run() {
            SendLogActivity.this.runOnUiThread(new Runnable() {
               public void run() {
                  SendLogActivity.this.sendEmail();
               }
            });
         }
      })).start();
   }

   protected void sendEmail() {
      Logger.e(this, "email", "Send email");
      Intent var2 = new Intent("android.intent.action.SEND");
      var2.setData(Uri.parse("mailto:"));
      var2.setType("text/plain");
      var2.putExtra("android.intent.extra.EMAIL", new String[]{"uniraju@gmail.com"});
      var2.putExtra("android.intent.extra.CC", new String[]{""});
      var2.putExtra("android.intent.extra.SUBJECT", "Bt Home Log");
      var2.putExtra("android.intent.extra.TEXT", "Email message goes here");
      File var1 = new File(Environment.getExternalStorageDirectory(), "bthome.log");
      var2.putExtra("android.intent.extra.STREAM", Uri.parse("file://" + var1));

      try {
         this.startActivity(Intent.createChooser(var2, "Send mail..."));
         Logger.e(this, "email", "Finished sending email...");
         this.finish();
         Logger.e(this, "email", "Finished sending email...");
      } catch (ActivityNotFoundException var3) {
         Toast.makeText(this, "There is no email client installed.", 0).show();
      }

   }
}

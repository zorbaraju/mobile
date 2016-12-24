package com.zorba.bt.app;

import com.zorba.bt.app.bluetooth.BtHwLayer;
import com.zorba.bt.app.dao.DeviceData;
import com.zorba.bt.app.db.BtLocalDB;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

public class AnalyticsActivity extends ZorbaActivity {
   TextView dcvalue = null;
   TextView dpvalue = null;

   private View getGridCell(String var1) {
      RelativeLayout var3 = new RelativeLayout(this);
      var3.setLayoutParams(new LayoutParams(-1, -2));
      LayoutParams var4 = new LayoutParams(-1, -2);
      var4.addRule(9, -1);
      LayoutParams var2 = new LayoutParams(-2, -2);
      var2.addRule(11, -1);
      TextView var5 = new TextView(this);
      var5.setTextSize(20.0F);
      var5.setText(var1);
      var5.setTextColor(Color.parseColor("#FFFFFF"));
      var5.setBackgroundColor(Color.parseColor("#202e39"));
      var5.setPadding(10, 5, 0, 5);
      var4.setMargins(0, 0, 0, 1);
      var3.addView(var5, var4);
      TextView var6 = new TextView(this);
      var6.setTextSize(20.0F);
      var6.setText(this.getPower(var1));
      var6.setBackgroundColor(Color.parseColor("#202e39"));
      var6.setTextColor(Color.parseColor("#eca538"));
      var6.setPadding(0, 5, 10, 5);
      var2.setMargins(0, 0, 0, 1);
      var3.addView(var6, var2);
      return var3;
   }

   private String getPower(String var1) {
      return "5 Watts";
   }

   private void populateView() {
      LinearLayout var2 = (LinearLayout)this.findViewById(2131165211);
      RelativeLayout var5 = new RelativeLayout(this);
      var5.setLayoutParams(new LayoutParams(-1, -2));
      LayoutParams var4 = new LayoutParams(-1, -2);
      var4.addRule(9, -1);
      LayoutParams var6 = new LayoutParams(-2, -2);
      var6.addRule(11, -1);
      TextView var3 = new TextView(this);
      var3.setTextSize(20.0F);
      var3.setText("Devices On");
      var3.setTextColor(Color.parseColor("#FFFFFF"));
      var3.setBackgroundColor(Color.parseColor("#202e39"));
      var3.setPadding(10, 5, 0, 5);
      var4.setMargins(0, 20, 0, 1);
      var5.addView(var3, var4);
      this.dcvalue = new TextView(this);
      this.dcvalue.setTextSize(20.0F);
      this.dcvalue.setText("0");
      this.dcvalue.setBackgroundColor(Color.parseColor("#202e39"));
      this.dcvalue.setTextColor(Color.parseColor("#eca538"));
      this.dcvalue.setPadding(0, 5, 10, 5);
      var6.setMargins(0, 20, 0, 1);
      var5.addView(this.dcvalue, var6);
      var2.addView(var5);
      RelativeLayout var7 = new RelativeLayout(this);
      var7.setLayoutParams(new LayoutParams(-1, -2));
      var4 = new LayoutParams(-1, -2);
      var4.addRule(9, -1);
      LayoutParams var8 = new LayoutParams(-2, -2);
      var8.addRule(11, -1);
      TextView var9 = new TextView(this);
      var9.setTextSize(20.0F);
      var9.setText("Total Power");
      var9.setTextColor(Color.parseColor("#FFFFFF"));
      var9.setBackgroundColor(Color.parseColor("#202e39"));
      var9.setPadding(10, 5, 0, 5);
      var4.setMargins(0, 0, 0, 50);
      var7.addView(var9, var4);
      this.dpvalue = new TextView(this);
      this.dpvalue.setTextSize(20.0F);
      this.dpvalue.setText("0 Watts");
      this.dpvalue.setBackgroundColor(Color.parseColor("#202e39"));
      this.dpvalue.setTextColor(Color.parseColor("#eca538"));
      this.dpvalue.setPadding(0, 5, 10, 5);
      var8.setMargins(0, 0, 0, 1);
      var7.addView(this.dpvalue, var8);
      var2.addView(var7);

      for(int var1 = 0; var1 < DeviceData.deviceTypes.length; ++var1) {
         var2.addView(this.getGridCell(DeviceData.deviceTypes[var1]));
      }

      this.updateValue();
   }

   private void updateValue() {
      this.dcvalue.setText("" + BtLocalDB.getInstance(this.getApplicationContext()).getDevicesOnCount());

      try {
         TextView var1 = this.dpvalue;
         StringBuilder var2 = new StringBuilder(String.valueOf(BtHwLayer.getInstance(this).readPower()));
         var1.setText(var2.append(" Watts").toString());
      } catch (Exception var3) {
         this.dpvalue.setText("0 Watts");
         var3.printStackTrace();
      }

   }

   protected void onCreate(Bundle var1) {
      super.onCreate(var1);
      this.setContentView(2130903046);
      this.populateView();
   }
}

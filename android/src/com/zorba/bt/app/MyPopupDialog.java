package com.zorba.bt.app;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;

import com.zorba.bt.app.dao.DeviceData;

public class MyPopupDialog extends LinearLayout {
   Runnable callback;
   Dialog dialog;
   ArrayList menuListItem;
   int selectedPosition;
   TextView textlistmenu;

   public MyPopupDialog(Context var1) {
      this(var1, (AttributeSet)null);
   }

   public MyPopupDialog(Context var1, AttributeSet var2) {
      super(var1, var2);
      this.menuListItem = new ArrayList();
      this.callback = null;
      this.selectedPosition = -1;
      this.textlistmenu = null;
      this.dialog = null;
      ((LayoutInflater)var1.getSystemService("layout_inflater")).inflate(R.layout.mylistmenu, this);
      this.dialog = new Dialog(var1);
      this.dialog.setContentView(R.layout.devicelist);
      this.dialog.setTitle("Choose device type");
      this.dialog.setCanceledOnTouchOutside(true);
      this.setOnClickListener(new OnClickListener() {
         public void onClick(View var1) {
            MyPopupDialog.this.dialog.show();
         }
      });
   }

   public void addCallback(Runnable var1) {
      this.callback = var1;
   }

   public int getSelectedItemPosition() {
      return this.selectedPosition;
   }

   public void setText(String text) {
	   this.textlistmenu.setText(text);
   }

   
   public String getText() {
      return this.textlistmenu.getText().toString();
   }

   public void setMenuForLight(int deviceTypeWithDimmable) {
      FlowLayout var4 = (FlowLayout)this.dialog.findViewById(R.id.devicetypecontent);
      var4.removeAllViews();
      String[] devicenames = DeviceData.nonlightdeviceTypes;
      if( deviceTypeWithDimmable == 0)
    	  devicenames = DeviceData.lightdeviceTypes;
      else if( deviceTypeWithDimmable == 1)
    	  devicenames = DeviceData.dimmablelightdeviceTypes;
      else if( deviceTypeWithDimmable == 2)
    	  devicenames = DeviceData.nonlightdeviceTypes;
      else if( deviceTypeWithDimmable == 3)
    	  devicenames = DeviceData.dimmablenonlightdeviceTypes;

      this.textlistmenu = (TextView)this.findViewById(R.id.textlistmenu);
      this.textlistmenu.setText(devicenames[0]);

      for(int var2 = 0; var2 < devicenames.length; ++var2) {
         ImageView var6 = new ImageView(this.getContext());
         LinearLayout var5 = new LinearLayout(this.getContext());
         var5.setOrientation(0);
         var5.setPadding(5, 5, 5, 5);
         var6.setBackgroundColor(Color.parseColor("#1d2832"));
         var6.setImageResource(CommonUtils.getDeviceImage(devicenames[var2], 1));
         var5.addView(var6);
         var4.addView(var5);
         final String settext  = devicenames[var2];
         var6.setOnClickListener(new OnClickListener() {
            public void onClick(View var1) {
               MyPopupDialog.this.textlistmenu.setText(settext);
               MyPopupDialog.this.dialog.dismiss();
            }
         });
      }

   }
}

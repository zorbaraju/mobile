package com.zorba.bt.app;

import com.zorba.bt.app.dao.DeviceData;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class SelectComp extends LinearLayout {
   LinearLayout compLaout = null;
   String controllerValue = "";
   TextView controllerValueText = null;
   int deviceIndex = -1;
   String deviceType = null;
   View view = null;

   public SelectComp(Context var1, DeviceData var2) {
      super(var1);
      this.deviceIndex = var2.getDevId();
      this.deviceType = var2.getType();
      ((LayoutInflater)var1.getSystemService("layout_inflater")).inflate(R.layout.selectcomp, this);
      TextView var3 = (TextView)this.findViewById(R.id.deviceid);
      ImageView var4 = (ImageView)this.findViewById(R.id.controllerImage);
      this.controllerValueText = (TextView)this.findViewById(R.id.controllerValue);
      var3.setText(var2.getName());
      LinearLayout var9 = (LinearLayout)this.findViewById(R.id.selectcontroller);
      var4.setBackgroundResource(CommonUtils.getDeviceImage(this.deviceType, 1));
      LayoutParams var8;
      if(!var2.isDimmable()) {
         final Switch var5 = new Switch(var1);
         var5.setChecked(true);
         var5.setGravity(17);
         this.view = var5;
         var5.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View var1, MotionEvent var2) {
               if(var2.getAction() == 1) {
                  SelectComp var4 = SelectComp.this;
                  String var3;
                  if(!var5.isChecked()) {
                     var3 = "On";
                  } else {
                     var3 = "Off";
                  }

                  var4.controllerValue = var3;
                  SelectComp.this.updateValue();
               }

               return false;
            }
         });
         var5.setOnClickListener(new OnClickListener() {
            public void onClick(View var1) {
               SelectComp var2 = SelectComp.this;
               String var3;
               if(var5.isChecked()) {
                  var3 = "On";
               } else {
                  var3 = "Off";
               }

               var2.controllerValue = var3;
               SelectComp.this.updateValue();
            }
         });
         String var6;
         if(var5.isChecked()) {
            var6 = "On";
         } else {
            var6 = "Off";
         }

         this.controllerValue = var6;
      } else {
         SeekBar var7 = new SeekBar(var1);
         var7.setMax(100);
         var8 = new LayoutParams(-1, -2);
         var8.gravity = 17;
         var7.setLayoutParams(var8);
         var7.setMinimumHeight(40);
         this.view = var7;
         var7.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar var1, int var2, boolean var3) {
               SelectComp.this.controllerValue = var1.getProgress() + " %";
               SelectComp.this.updateValue();
            }

            public void onStartTrackingTouch(SeekBar var1) {
            }

            public void onStopTrackingTouch(SeekBar var1) {
            }
         });
         this.controllerValue = var7.getProgress() + " %";
      }

      var8 = new LayoutParams(-1, -2);
      var8.gravity = 17;
      var9.addView(this.view, var8);
      this.updateValue();
   }

   public int getDeviceIndex() {
      return this.deviceIndex;
   }

   public void setVisibleController(boolean isVisible) {
	   if( isVisible) {
		   controllerValueText.setVisibility(View.VISIBLE);
		   view.setVisibility(View.VISIBLE);
	   } else {
		   controllerValueText.setVisibility(View.GONE);
		   view.setVisibility(View.GONE);
	   }
   }
   
   public int getDeviceValue() {
      int var1;
      if(!DeviceData.isDimmable(this.deviceType)) {
         if(((Switch)this.view).isChecked()) {
            var1 = 9;
         } else {
            var1 = 0;
         }
      } else {
         int var2 = ((SeekBar)this.view).getProgress() / 10;
         var1 = var2;
         if(var2 == 10) {
            var1 = 9;
         }
      }

      return var1;
   }
   
   public void setDeviceValue(int value) {
      if(!DeviceData.isDimmable(this.deviceType)) {
    	  ((Switch)this.view).setChecked(value == 9);
      } else {
    	  ((SeekBar)this.view).setProgress(value*10);
      }
   }

   public boolean isSelected() {
      return ((CheckBox)this.findViewById(R.id.selected)).isChecked();
   }

   public void setSelected(boolean selected) {
	   ((CheckBox)this.findViewById(R.id.selected)).setChecked(selected);
   }

   public void updateValue() {
      this.controllerValueText.setText(this.controllerValue);
   }
}

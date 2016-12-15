package com.zorba.bt.app;

import com.zorba.bt.app.dao.DeviceData;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ImageTextButton extends LinearLayout {
   ImageButton button;
   String deviceType;
   TextView labelText;
   boolean mShowText;
   int mTextPos;

   public ImageTextButton(Context var1) {
      this(var1, (AttributeSet)null);
   }

   public ImageTextButton(Context var1, AttributeSet var2) {
      super(var1, var2);
      this.mTextPos = 0;
      this.mShowText = false;
      this.button = null;
      this.labelText = null;
      this.deviceType = "";
      ((LayoutInflater)var1.getSystemService("layout_inflater")).inflate(R.layout.imagetextbutton, this);
      TypedArray var5 = var1.getTheme().obtainStyledAttributes(var2, R.styleable.ImageTextButton, 0, 0);
      this.button = (ImageButton)this.findViewById(R.id.imageview1);
      this.labelText = (TextView)this.findViewById(R.id.buttontext);

      try {
         this.mShowText = var5.getBoolean(0, false);
         this.mTextPos = var5.getInteger(1, 0);
      } finally {
         var5.recycle();
      }

   }

   public void changeDeviceButtonStyle(int var1) {
      this.setBackgroundImage(CommonUtils.getDeviceImage(this.deviceType, var1));
      if(var1 == 0) {
         this.setRoundColor(R.layout.roundblackcorner);
         this.labelText.setTextColor(Color.parseColor("#ffffff"));
      } else if(var1 > 0) {
         this.setRoundColor(R.layout.roundorangecorner);
         this.labelText.setTextColor(Color.parseColor("#eca538"));
      } else {
         this.setRoundColor(R.layout.roundwhitecorner);
         this.labelText.setTextColor(Color.parseColor("#ffffff"));
      }

   }

   public void changeDeviceButtonStyle(String devtype, int var2) {
      this.setBackgroundImage(CommonUtils.getDeviceImage(devtype, var2));
      if(var2 == 0) {
         this.setRoundColor(R.layout.roundblackcorner);
         this.labelText.setTextColor(Color.parseColor("#ffffff"));
      } else if(var2 > 0) {
         this.setRoundColor(R.layout.roundorangecorner);
         this.labelText.setTextColor(Color.parseColor("#eca538"));
      } else {
         this.setRoundColor(R.layout.roundwhitecorner);
         this.labelText.setTextColor(Color.parseColor("#ffffff"));
      }

   }

   public String getDeviceType() {
      return this.deviceType;
   }

   public String getText() {
      return this.labelText.getText().toString();
   }

   public boolean isShowText() {
      return this.mShowText;
   }

   public void setBackgroundImage(int var1) {
      this.button.setImageResource(var1);
   }

   public void setBorderSelected(boolean var1) {
      if(var1) {
         this.labelText.setBackgroundColor(Color.parseColor("#202e3b"));
         this.labelText.setTypeface(this.labelText.getTypeface(), 3);
      } else {
         this.labelText.setBackgroundColor(0);
         this.labelText.setTypeface(Typeface.DEFAULT);
      }

   }

   public void setDevice(DeviceData var1) {
      this.setDeviceType(var1.getType());
      this.setText(var1.getName());
      this.setId(var1.getDevId());
      this.changeDeviceButtonStyle(this.deviceType, var1.getStatus());
   }

   public void setDeviceType(String var1) {
      this.deviceType = var1;
   }

   public void setOnClickListener(OnClickListener var1) {
      this.button.setOnClickListener(var1);
      this.labelText.setOnClickListener(var1);
      super.setOnClickListener(var1);
   }

   public void setOnLongClickListener(OnLongClickListener var1) {
      this.button.setOnLongClickListener(var1);
      this.labelText.setOnLongClickListener(var1);
      super.setOnLongClickListener(var1);
   }

   public void setRoundColor(int var1) {
      this.button.setBackgroundDrawable(this.getResources().getDrawable(var1));
   }

   public void setShowText(boolean var1) {
      this.mShowText = var1;
      this.invalidate();
      this.requestLayout();
   }

   public void setText(String var1) {
      this.labelText.setText(var1);
   }
}

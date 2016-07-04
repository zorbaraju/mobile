package com.zorba.bt.app;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CustomCheckBox extends LinearLayout {
   CheckBox button;

   public CustomCheckBox(Context var1) {
      super(var1);
   }

   public CustomCheckBox(Context var1, AttributeSet var2) {
      super(var1, var2);
      this.button = null;
      ((LayoutInflater)var1.getSystemService("layout_inflater")).inflate(R.layout.customcheckbox, this);
      TypedArray var5 = var1.getTheme().obtainStyledAttributes(var2, R.styleable.CustomCheckBox, 0, 0);
      this.button = (CheckBox)this.findViewById(R.id.checkbox);
      TextView var6 = (TextView)this.findViewById(R.id.text);

      try {
         var6.setText(var5.getString(0));
      } finally {
         var5.recycle();
      }

   }
   
   public void setDaySelected(boolean isChecked) {
	   this.button.setChecked(isChecked);
   }

   public boolean isDaySelected() {
      return this.button.isChecked();
   }
}

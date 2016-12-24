package com.zorba.bt.app;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;

import com.zorba.bt.app.dao.RoomData;

public class TextAdapter extends ArrayAdapter {
   OnClickListener callback;
   Activity context;
   ArrayList list;

   public TextAdapter(Activity var1, ArrayList var2, OnClickListener var3) {
      super(var1, -1, var2);
      this.context = var1;
      this.list = var2;
      this.callback = var3;
   }

   public View getDropDownView(int var1, View var2, ViewGroup var3) {
      return this.getView(var1, var2, var3);
   }

   public View getView(int var1, View var2, ViewGroup var3) {
      TextView var4 = new TextView(this.context);
      var4.setTextColor(Color.parseColor("#ffffff"));
      var4.setBackgroundColor(Color.parseColor("#000000"));
      var4.setTextSize(20.0F);
      var4.setPadding(4, 6, 0, 6);
      var4.setTag(Integer.valueOf(var1));
      var4.setText(((RoomData)this.list.get(var1)).getName());
      var4.setOnClickListener(this.callback);
      return var4;
   }
}

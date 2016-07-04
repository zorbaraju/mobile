package com.zorba.bt.app;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import java.util.ArrayList;

public class MyListMenu extends LinearLayout {
   Runnable callback;
   ListPopupWindow listPopup;
   ArrayList<String> menuListItem;
   int selectedPosition;
   TextView textlistmenu;

   public MyListMenu(Context var1) {
      this(var1, (AttributeSet)null);
   }

   public MyListMenu(Context var1, AttributeSet var2) {
      super(var1, var2);
      this.menuListItem = new ArrayList<String>();
      this.listPopup = null;
      this.callback = null;
      this.selectedPosition = 0;
      this.textlistmenu = null;
      ((LayoutInflater)var1.getSystemService("layout_inflater")).inflate(R.layout.mylistmenu, this);
      this.textlistmenu = (TextView)this.findViewById(R.id.textlistmenu);
      this.listPopup = new ListPopupWindow(this.getContext());
      this.listPopup.setAnchorView(this);
      this.listPopup.setOnItemClickListener(new OnItemClickListener() {
         public void onItemClick(AdapterView<?> var1, View var2, int var3, long var4) {
            MyListMenu.this.listPopup.dismiss();
            MyListMenu.this.textlistmenu.setText((CharSequence)MyListMenu.this.menuListItem.get(var3));
            MyListMenu.this.selectedPosition = var3;
            if(MyListMenu.this.callback != null) {
               MyListMenu.this.callback.run();
            }

         }
      });
      this.setOnClickListener(new OnClickListener() {
         public void onClick(View var1) {
            MyListMenu.this.listPopup.show();
         }
      });
   }

   public void addCallback(Runnable var1) {
      this.callback = var1;
   }

   public void setSelectedItem(int selectIndex){
	   this.listPopup.setSelection(selectIndex);
	   textlistmenu.setText((CharSequence)MyListMenu.this.menuListItem.get(selectIndex));
	   this.selectedPosition = selectIndex;
   }
   
   public int getSelectedItemPosition() {
      return this.selectedPosition;
   }

   public String getText() {
      return this.textlistmenu.getText().toString();
   }

   public void setMenuItems(String[] var1) {
      this.menuListItem.clear();
      int var3 = var1.length;

      int var2;
      for(var2 = 0; var2 < var3; ++var2) {
         String var4 = var1[var2];
         this.menuListItem.add(var4);
      }

      if(this.menuListItem.size() > 0) {
         this.textlistmenu.setText((CharSequence)this.menuListItem.get(0));
      } else {
         this.textlistmenu.setText("");
      }

      MyListMenu.TextAdapter var5 = new MyListMenu.TextAdapter(this.getContext(), this.menuListItem);
      this.listPopup.setAdapter(var5);
      var2 = CommonUtils.measureContentWidth(this.listPopup.getListView(), var5) + 20;
      this.listPopup.setWidth(var2);
      this.textlistmenu.setWidth(var2);
   }

   class TextAdapter extends ArrayAdapter<String> {
      ArrayList<String> list;

      public TextAdapter(Context var2, ArrayList<String> var3) {
         super(var2, -1, var3);
         this.list = var3;
      }

      public View getDropDownView(int var1, View var2, ViewGroup var3) {
         return this.getView(var1, var2, var3);
      }

      public View getView(int var1, View var2, ViewGroup var3) {
         TextView var4 = new TextView(this.getContext());
         var4.setTextColor(Color.parseColor("#ffffff"));
         var4.setBackgroundColor(Color.parseColor("#000000"));
         var4.setTextSize(20.0F);
         var4.setTag(Integer.valueOf(var1));
         var4.setText((CharSequence)this.list.get(var1));
         var4.setPadding(10, 4, 0, 4);
         return var4;
      }
   }
}

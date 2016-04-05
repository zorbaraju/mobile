package com.zorba.bt.app;

import com.zorba.bt.app.db.BtLocalDB;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MyComp extends LinearLayout {
   FlowLayout compLaout = null;
   String compname = "";
   ImageView image = null;
   boolean isCollapsedEnabled = false;
   boolean isReset = false;
   MyComp[] siblingComps = new MyComp[0];

   public MyComp(Context var1, String var2) {
      super(var1);
      this.compname = var2;
      ((LayoutInflater)var1.getSystemService("layout_inflater")).inflate(R.layout.collapsepanel, this);
      TextView var4 = (TextView)this.findViewById(R.id.name);
      this.image = (ImageView)this.findViewById(R.id.loadingImage);
      Bitmap var3 = BitmapFactory.decodeResource(this.getResources(), R.drawable.downarrow);
      this.image.setImageBitmap(var3);
      var4.setText(var2);
      this.compLaout = (FlowLayout)this.findViewById(R.id.compfield1);
      ((RelativeLayout)this.findViewById(R.id.collapseheader)).setOnClickListener(new OnClickListener() {
         public void onClick(View var1) {
            Bitmap var5;
            if(MyComp.this.compLaout.getVisibility() == 8) {
               MyComp.this.showAddButton(true);
               MyComp.this.compLaout.setVisibility(0);
               if(MyComp.this.siblingComps != null) {
                  MyComp[] var4 = MyComp.this.siblingComps;
                  int var3 = var4.length;

                  for(int var2 = 0; var2 < var3; ++var2) {
                     var4[var2].expandComp(false);
                  }
               }

               var5 = BitmapFactory.decodeResource(MyComp.this.getResources(), R.drawable.downarrow);
               MyComp.this.image.setImageBitmap(var5);
            } else {
               MyComp.this.showAddButton(false);
               MyComp.this.showDeleteButton(false);
               MyComp.this.compLaout.setVisibility(8);
               var5 = BitmapFactory.decodeResource(MyComp.this.getResources(), R.drawable.rightarrow);
               MyComp.this.image.setImageBitmap(var5);
            }

         }
      });
      this.expandComp(true);
      ((ImageButton)this.findViewById(R.id.addbutton)).setOnClickListener(new OnClickListener() {
         public void onClick(View var1) {
            MyComp.this.deselectAll();
            MyComp.this.doAddAction();
         }
      });
      ((ImageButton)this.findViewById(R.id.deletebutton)).setOnClickListener(new OnClickListener() {
         public void onClick(View var1) {
            MyComp.this.doDeleteAction();
            MyComp.this.deselectAll();
            MyComp.this.showDeleteButton(false);
         }
      });
      this.showAddButton(true);
   }

   private void showButton(int var1, boolean var2) {
      ImageButton var3 = (ImageButton)this.findViewById(var1);
      if(var2) {
         var3.setVisibility(0);
      } else {
         var3.setVisibility(8);
      }

   }

   public void addMyView(ImageTextButton var1) {
      LayoutParams var2 = new LayoutParams(-2, -2);
      var2.setMargins(10, 10, 10, 10);
      var1.setLayoutParams(var2);
      this.compLaout.addView(var1);
      this.relayout();
   }

   public void deselectAll() {
      int var2 = this.compLaout.getChildCount();

      for(int var1 = 0; var1 < var2; ++var1) {
         ((ImageTextButton)this.compLaout.getChildAt(var1)).setBorderSelected(false);
      }

   }

   public void doAddAction() {
   }

   public void doDeleteAction() {
   }

   public void expandComp(boolean var1) {
      if(this.isCollapsedEnabled) {
         Bitmap var2;
         if(var1) {
            this.compLaout.setVisibility(0);
            var2 = BitmapFactory.decodeResource(this.getResources(), R.drawable.downarrow);
            this.image.setImageBitmap(var2);
            this.showAddButton(true);
         } else {
            this.compLaout.setVisibility(8);
            var2 = BitmapFactory.decodeResource(this.getResources(), R.drawable.rightarrow);
            this.image.setImageBitmap(var2);
            this.showAddButton(false);
            this.showDeleteButton(false);
            this.deselectAll();
         }
      }

   }

   public int getChildMaxWidth() {
      return this.compLaout.getChildMaxWidth();
   }

   public boolean isReset() {
      return this.isReset;
   }

   public void recalculateMaxWidth() {
      MyComp[] var3 = this.siblingComps;
      int var2 = var3.length;

      for(int var1 = 0; var1 < var2; ++var1) {
         var3[var1].compLaout.recalculateMaxWidth();
      }

      this.compLaout.recalculateMaxWidth();
   }

   public void relayout() {
      (new Handler(Looper.getMainLooper())).post(new Runnable() {
         public void run() {
            byte var4 = 0;
            int var1 = MyComp.this.compLaout.getChildMaxWidth();
            MyComp[] var7 = MyComp.this.siblingComps;
            int var5 = var7.length;

            int var2;
            int var3;
            for(var2 = 0; var2 < var5; var1 = var3) {
               MyComp var6 = var7[var2];
               var3 = var1;
               if(var6.getChildMaxWidth() > var1) {
                  var3 = var6.getChildMaxWidth();
               }

               ++var2;
            }

            MyComp[] var8 = MyComp.this.siblingComps;
            var3 = var8.length;

            for(var2 = var4; var2 < var3; ++var2) {
               var8[var2].setChildMaxWidth(var1);
            }

            MyComp.this.setChildMaxWidth(var1);
         }
      });
   }

   public void removeMyView(String var1) {
      int var3 = this.compLaout.getChildCount();

      for(int var2 = 0; var2 < var3; ++var2) {
         ImageTextButton var4 = (ImageTextButton)this.compLaout.getChildAt(var2);
         if(var4.getText().equals(var1)) {
            this.compLaout.removeView(var4);
            this.recalculateMaxWidth();
            this.relayout();
            break;
         }
      }

   }

   public void resetButtonInPanel(boolean var1) {
      this.isReset = true;
      int var3 = this.compLaout.getChildCount();

      for(int var2 = 0; var2 < var3; ++var2) {
         ImageTextButton var4 = (ImageTextButton)this.compLaout.getChildAt(var2);
         String var5 = var4.getDeviceType();
         if(var1) {
            var4.changeDeviceButtonStyle(-1);
            var4.setBackgroundImage(R.drawable.group);
         } else {
            var4.changeDeviceButtonStyle(var5, -1);
         }
      }

   }

   public void selectComp(ImageTextButton var1) {
      this.deselectAll();
      MyComp[] var4 = this.siblingComps;
      int var3 = var4.length;

      for(int var2 = 0; var2 < var3; ++var2) {
         MyComp var5 = var4[var2];
         var5.showDeleteButton(false);
         var5.deselectAll();
      }

      var1.setBorderSelected(true);
   }

   public void setChildMaxWidth(int var1) {
      this.compLaout.setChildMaxWidth(var1);
   }

   public void setSiblings(MyComp[] var1) {
      this.siblingComps = var1;
      this.relayout();
   }

   public void showAddButton(boolean var1) {
      this.showButton(R.id.addbutton, var1);
   }

   public void showDeleteButton(boolean var1) {
      this.showButton(R.id.deletebutton, var1);
   }

   public void updateButtonInPanel() {
      int var2 = this.compLaout.getChildCount();

      for(int var1 = 0; var1 < var2; ++var1) {
         ImageTextButton var5 = (ImageTextButton)this.compLaout.getChildAt(var1);
         String var4 = ""+var5.getId();
         int var3 = BtLocalDB.getInstance(this.getContext()).getDeviceStatus(var4);
         System.out.println("update button panelll..status.."+var3+"....id."+var4);
         if(var3 != -1) {
            var5.changeDeviceButtonStyle(var5.getDeviceType(), var3);
         }
      }

   }

   public void updateLiveButtonInPanel() {
      this.isReset = false;
      int var2 = this.compLaout.getChildCount();

      for(int var1 = 0; var1 < var2; ++var1) {
         ImageTextButton var3 = (ImageTextButton)this.compLaout.getChildAt(var1);
         var3.changeDeviceButtonStyle(0);
         var3.setBackgroundImage(R.drawable.group);
      }

   }
}

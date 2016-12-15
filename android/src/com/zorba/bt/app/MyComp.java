package com.zorba.bt.app;

import com.zorba.bt.app.db.BtLocalDB;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
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
   private boolean isEditNeeded = false;
   private int maxComp = -1;
   private boolean isAddButtonShown = false;

   public MyComp(Context context, String compName, int maxComps, boolean isEditNeeded) {
      super(context);
      this.compname = compName;
      this.maxComp = maxComps;
      ((LayoutInflater)context.getSystemService("layout_inflater")).inflate(R.layout.collapsepanel, this);
      TextView var4 = (TextView)this.findViewById(R.id.name);
      this.image = (ImageView)this.findViewById(R.id.loadingImage);
      Bitmap var3 = BitmapFactory.decodeResource(this.getResources(), R.drawable.downarrow);
      this.image.setImageBitmap(var3);
      var4.setText(compName);
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
      ((ImageButton)this.findViewById(R.id.configbutton)).setOnClickListener(new OnClickListener() {
          public void onClick(View var1) {
        	  MyComp.this.deselectAll();
              MyComp.this.doEditAction();
              MyComp.this.showDeleteButton(false);
          }
       });
      ((ImageButton)this.findViewById(R.id.deletebutton)).setOnClickListener(new OnClickListener() {
         public void onClick(View var1) {
            MyComp.this.doDeleteAction();
            MyComp.this.deselectAll();
            MyComp.this.showDeleteButton(false);
         }
      });
      this.isEditNeeded = isEditNeeded;

      this.showButton(R.id.configbutton, false);
      this.showAddButton(true);
   }
   
   public void enableEditMode(boolean isEditMode) {
	   this.isEditNeeded = isEditMode;
	   this.showButton(R.id.addbutton, this.isEditNeeded);
	   this.showButton(R.id.configbutton, false);
	   this.showButton(R.id.deletebutton, false);
   }

   private boolean isButtonShown(int buttonId) {
	   ImageButton imgButton = (ImageButton)this.findViewById(buttonId);
	   return imgButton.getVisibility() == Button.VISIBLE;
   }
   
   private void showButton(int buttonId, boolean show) {
      ImageButton imgButton = (ImageButton)this.findViewById(buttonId);
      if(show) {
    	  if( isEditNeeded ) {
    		  imgButton.setVisibility(Button.VISIBLE);
    	  }
      } else {
    	  imgButton.setVisibility(Button.GONE);
      }

   }

   public void addMyView(ImageTextButton var1) {
      LayoutParams var2 = new LayoutParams(-2, -2);
      var2.setMargins(10, 10, 10, 10);
      var1.setLayoutParams(var2);
      this.compLaout.addView(var1);
      if( maxComp != -1 && isAddButtonShown)
		  this.showButton(R.id.addbutton, this.compLaout.getChildCount() < maxComp);
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
   
   public void doEditAction() {
	   
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
            MyComp[] sibs = MyComp.this.siblingComps;
            
            int maxwidth = 0;
            for(int var2 = 0; var2 < sibs.length; var2++) {
               if(sibs[var2].getChildMaxWidth() > maxwidth) {
                  maxwidth = sibs[var2].getChildMaxWidth();
               }
            }
            
            for(int var2 = 0; var2 < sibs.length; var2++) {
            	sibs[var2].setChildMaxWidth(maxwidth);
             }

            MyComp.this.setChildMaxWidth(maxwidth);
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
      
      if( maxComp != -1 && isAddButtonShown)
		  this.showButton(R.id.addbutton, this.compLaout.getChildCount() < maxComp);
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

   public void showAddButton(boolean show) {
	  isAddButtonShown = show;
	  this.showButton(R.id.addbutton, isEditNeeded&show);
	  if( show && maxComp != -1)
		  this.showButton(R.id.addbutton, isEditNeeded&this.compLaout.getChildCount() < maxComp);
   }

   public void showDeleteButton(boolean show) {
      this.showButton(R.id.deletebutton, isEditNeeded&show);
      this.showButton(R.id.configbutton, isEditNeeded&show);
   }

   public void updateButtonInPanel() {
      int var2 = this.compLaout.getChildCount();

      for(int var1 = 0; var1 < var2; ++var1) {
         ImageTextButton var5 = (ImageTextButton)this.compLaout.getChildAt(var1);
         int devid = var5.getId();
         int var3 = BtLocalDB.getInstance(this.getContext()).getDeviceStatus((byte)devid);
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

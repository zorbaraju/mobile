package com.zorba.bt.app;

import com.zorba.bt.app.dao.DeviceData;
import com.zorba.bt.app.db.BtLocalDB;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MyComp extends LinearLayout {
   FlowLayout compLaout = null;
   String compname = "";
   SvgView image = null;
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
      this.image = (SvgView)this.findViewById(R.id.loadingImage);
      this.image.setImageResource(R.raw.downarrow);
      var4.setText(compName);
      this.compLaout = (FlowLayout)this.findViewById(R.id.compfield1);
      ((RelativeLayout)this.findViewById(R.id.collapseheader)).setOnClickListener(new ZorbaOnClickListener() {
          public void zonClick(View var1) {
            if(MyComp.this.compLaout.getVisibility() == 8) {
               MyComp.this.showAddButton(true);
               MyComp.this.compLaout.setVisibility(0);
               if(MyComp.this.siblingComps != null) {
                  MyComp[] myComps = MyComp.this.siblingComps;
                  int count = myComps.length;

                  for(int index = 0; index < count; ++index) {
                	  myComps[index].expandComp(false);
                  }
               }
               MyComp.this.image.setImageResource(R.raw.downarrow);
            } else {
               MyComp.this.showAddButton(false);
               MyComp.this.showDeleteButton(false);
               MyComp.this.compLaout.setVisibility(8);
               MyComp.this.image.setImageResource(R.raw.rightarrow);
            }

         }
      });
      this.expandComp(true);
      ((SvgView)this.findViewById(R.id.addbutton)).setOnClickListener(new ZorbaOnClickListener() {
          public void zonClick(View var1) {
            MyComp.this.deselectAll();
            MyComp.this.doAddAction();
         }
      });
      ((SvgView)this.findViewById(R.id.configbutton)).setOnClickListener(new ZorbaOnClickListener() {
          public void zonClick(View var1) {
        	  MyComp.this.deselectAll();
              MyComp.this.doEditAction();
              MyComp.this.showDeleteButton(false);
          }
       });
      ((SvgView)this.findViewById(R.id.deletebutton)).setOnClickListener(new ZorbaOnClickListener() {
          public void zonClick(View var1) {
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
	   SvgView imgButton = (SvgView)this.findViewById(buttonId);
	   return imgButton.getVisibility() == Button.VISIBLE;
   }
   
   private void showButton(int buttonId, boolean show) {
      SvgView imgButton = (SvgView)this.findViewById(buttonId);
      if(show) {
    	  if( isEditNeeded ) {
    		  imgButton.setVisibility(Button.VISIBLE);
    	  }
      } else {
    	  imgButton.setVisibility(Button.GONE);
      }

   }

   public void addMyView(ImageTextButton but) {
      LayoutParams lp = new LayoutParams(-2, -2);
      lp.setMargins(10, 10, 10, 10);
      but.setLayoutParams(lp);
      this.compLaout.addView(but);
      if( maxComp != -1 && isAddButtonShown)
		  this.showButton(R.id.addbutton, this.compLaout.getChildCount() < maxComp);
      this.relayout();
   }

   public void deselectAll() {
      int count = this.compLaout.getChildCount();

      for(int index = 0; index < count; ++index) {
         ((ImageTextButton)this.compLaout.getChildAt(index)).setBorderSelected(false);
      }

   }

   public void doAddAction() {
   }
   
   public void doEditAction() {
	   
   }
   public void doDeleteAction() {
   }

   public void expandComp(boolean expand) {
      if(this.isCollapsedEnabled) {
         if(expand) {
            this.compLaout.setVisibility(0);
            this.image.setImageResource(R.raw.downarrow);
            this.showAddButton(true);
         } else {
            this.compLaout.setVisibility(8);
            this.image.setImageResource(R.raw.rightarrow);
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
      MyComp[] siblings = this.siblingComps;
      int count = siblings.length;

      for(int index = 0; index < count; ++index) {
    	  siblings[index].compLaout.recalculateMaxWidth();
      }

      this.compLaout.recalculateMaxWidth();
   }

   public void relayout() {
      (new Handler(Looper.getMainLooper())).post(new Runnable() {
         public void run() {
            MyComp[] sibs = MyComp.this.siblingComps;
            
            int maxwidth = 0;
            for(int index = 0; index < sibs.length; index++) {
               if(sibs[index].getChildMaxWidth() > maxwidth) {
                  maxwidth = sibs[index].getChildMaxWidth();
               }
            }
            
            for(int index = 0; index < sibs.length; index++) {
            	sibs[index].setChildMaxWidth(maxwidth);
             }

            MyComp.this.setChildMaxWidth(maxwidth);
         }
      });
   }
   
	public void updateMyView(String compName, DeviceData deviceData) {
		int count = this.compLaout.getChildCount();

		for (int index = 0; index < count; ++index) {
			ImageTextButton imageTextButton = (ImageTextButton) this.compLaout.getChildAt(index);
			if (imageTextButton.getText().equals(compName)) {
				imageTextButton.setDevice(deviceData);
				break;
			}
		}
	}
	
	public void updateMyView(String compName, int resid) {
		int count = this.compLaout.getChildCount();

		for (int index = 0; index < count; ++index) {
			ImageTextButton imageTextButton = (ImageTextButton) this.compLaout.getChildAt(index);
			if (imageTextButton.getText().equals(compName)) {
				imageTextButton.setBackgroundImage(resid);
				imageTextButton.setImageResId(resid);
				break;
			}
		}
	}


   public void removeMyView(String compName) {
      int count = this.compLaout.getChildCount();

      for(int index = 0; index < count; ++index) {
         ImageTextButton imageTextButton = (ImageTextButton)this.compLaout.getChildAt(index);
         if(imageTextButton.getText().equals(compName)) {
            this.compLaout.removeView(imageTextButton);
            this.recalculateMaxWidth();
            this.relayout();
            break;
         }
      }
      
      if( maxComp != -1 && isAddButtonShown)
		  this.showButton(R.id.addbutton, this.compLaout.getChildCount() < maxComp);
   }

   public void resetButtonInPanel(boolean reset) {
      this.isReset = true;
      int count = this.compLaout.getChildCount();

      for(int index = 0; index < count; ++index) {
         ImageTextButton imagetextButton = (ImageTextButton)this.compLaout.getChildAt(index);
         if(reset) {
        	 imagetextButton.changeDeviceButtonStyle(-1);
        	 imagetextButton.setBackgroundImage(imagetextButton.getImageResId());
         } else {
        	 imagetextButton.changeDeviceButtonStyle(imagetextButton.getDeviceType(), -1);
         }
      }

   }

   public void selectComp(ImageTextButton button) {
      this.deselectAll();
      MyComp[] siblings = this.siblingComps;
      int count = siblings.length;

      for(int index = 0; index < count; ++index) {
         MyComp comp = siblings[index];
         comp.showDeleteButton(false);
         comp.deselectAll();
      }

      button.setBorderSelected(true);
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
      int count = this.compLaout.getChildCount();

      for(int index = 0; index < count; ++index) {
         ImageTextButton button = (ImageTextButton)this.compLaout.getChildAt(index);
         int devid = button.getId();
         int status = BtLocalDB.getInstance(this.getContext()).getDeviceStatus((byte)devid);
         if(status != -1) {
            button.changeDeviceButtonStyle(button.getDeviceType(), status);
         }
      }

   }

   public void updateLiveButtonInPanel() {
      this.isReset = false;
      int count = this.compLaout.getChildCount();

      for(int index = 0; index < count; ++index) {
         ImageTextButton but = (ImageTextButton)this.compLaout.getChildAt(index);
         but.changeDeviceButtonStyle(0);
         but.setBackgroundImage(but.getImageResId());
      }

   }
}

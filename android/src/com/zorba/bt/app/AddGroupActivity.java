package com.zorba.bt.app;

import com.zorba.bt.app.dao.DeviceData;
import com.zorba.bt.app.db.BtLocalDB;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AddGroupActivity extends ZorbaActivity {
   String deviceAddress = null;

   private void initListeners() {
      ((ImageButton)this.findViewById(R.id.cancel)).setOnClickListener(new OnClickListener() {
         public void onClick(View var1) {
            AddGroupActivity.this.finish();
         }
      });
      ((ImageButton)this.findViewById(R.id.save)).setOnClickListener(new OnClickListener() {
         public void onClick(View var1) {
            AddGroupActivity.this.saveGroup();
         }
      });
   }

   private void populateDevices() {
      LinearLayout var3 = (LinearLayout)this.findViewById(R.id.groupdevices);
      DeviceData[] var2 = BtLocalDB.getInstance(this).getDevices(this.deviceAddress);

      for(int var1 = 0; var1 < var2.length; ++var1) {
         if(!var2[var1].isUnknownType()) {
            SelectComp var4 = new SelectComp(this, var2[var1]);
            var4.setId(var2[var1].getDevId());
            var3.addView(var4);
         }
      }

      this.initListeners();
   }

   private void saveGroup() {
      EditText var3 = (EditText)this.findViewById(R.id.groupNameText);
      LinearLayout var6 = (LinearLayout)this.findViewById(R.id.groupdevices);
      String var5 = CommonUtils.isValidName(this, var3.getText().toString());
      if(var5 != null) {
         if(BtLocalDB.getInstance(this.getApplication()).isGroupNameExist(this.deviceAddress, var5)) {
            CommonUtils.AlertBox(this, "Already exist", "Name is exist already");
         } else {
            int var2 = var6.getChildCount();
            String var4 = "";

            String var8;
            for(int var1 = 0; var1 < var2; var4 = var8) {
               SelectComp var7 = (SelectComp)var6.getChildAt(var1);
               var8 = var4;
               if(var7.isSelected()) {
                  var8 = var7.getDeviceIndex() + "#" + var7.getDeviceValue();
                  if(var4.isEmpty()) {
                     var8 = var4 + var8;
                  } else {
                     var8 = var4 + "#" + var8;
                  }
               }

               ++var1;
            }

            if(var4.isEmpty()) {
               CommonUtils.AlertBox(this, "Save group", "No devices are selected");
            } else {
               var8 = var4.trim();
               BtLocalDB.getInstance(this).saveGroup(this.deviceAddress, var5, var8);
               Intent var9 = new Intent();
               var9.putExtra("name", var5);
               this.setResult(1, var9);
               this.finish();
            }
         }
      }

   }

   protected void onCreate(Bundle var1) {
      super.onCreate(var1);
      this.setContentView(R.layout.addgrouplayout);
      EditText var3 = (EditText)this.findViewById(R.id.groupNameText);
      var3.setFilters(new InputFilter[] {new InputFilter.LengthFilter(12)});
      ((TextView)this.findViewById(R.id.title)).setText("New Group");
      this.deviceAddress = this.getIntent().getExtras().getString("deviceAddress");
      this.populateDevices();
   }

   public void onDestroy() {
      super.onDestroy();
   }
}

package com.zorba.bt.app;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import java.util.ArrayList;

public class ImageTextAdapter extends ArrayAdapter<ImageTextData> {
   OnClickListener callback;
   Activity context;
   LayoutInflater inflater;
   ArrayList<ImageTextData> list;

   public ImageTextAdapter(Activity activity, ArrayList<ImageTextData> arrayList, OnClickListener cb) {
      super(activity, R.layout.imagetextitem , arrayList);
      this.context = activity;
      this.list = arrayList;
      this.callback = cb;
   }

   public View getDropDownView(int var1, View var2, ViewGroup var3) {
      return this.getView(var1, var2, var3);
   }

   public View getView(int position, View convertView, ViewGroup parent) {
      inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
      View inflatedView = inflater.inflate(R.layout.imagetextitem, parent, false);
      ImageButton var5 = (ImageButton)inflatedView.findViewById(R.id.imageview1);
      var5.setImageResource(((ImageTextData)this.list.get(position)).getImageId().intValue());
      TextView var6 = (TextView)inflatedView.findViewById(R.id.buttontext);
      var6.setText(((ImageTextData)this.list.get(position)).getText());
      var6.setTag(Integer.valueOf(position));
      var5.setTag(Integer.valueOf(position));
      var5.setOnClickListener(this.callback);
      var6.setOnClickListener(this.callback);
      return inflatedView;
   }
}

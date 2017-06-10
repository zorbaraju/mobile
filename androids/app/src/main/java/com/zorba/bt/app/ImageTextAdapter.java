package com.zorba.bt.app;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
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

   public View getDropDownView(int style, View view, ViewGroup group) {
      return this.getView(style, view, group);
   }

   public View getView(int position, View convertView, ViewGroup parent) {
      inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
      View inflatedView = inflater.inflate(R.layout.imagetextitem, parent, false);
      SvgView button = (SvgView)inflatedView.findViewById(R.id.imageview1);
      button.setImageResource(((ImageTextData)this.list.get(position)).getImageId().intValue());
      TextView textv = (TextView)inflatedView.findViewById(R.id.buttontext);
      textv.setText(((ImageTextData)this.list.get(position)).getText());
      textv.setTag(Integer.valueOf(position));
      button.setTag(Integer.valueOf(position));
      button.setOnClickListener(this.callback);
      textv.setOnClickListener(this.callback);
      return inflatedView;
      
   }
}

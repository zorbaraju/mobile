package com.zorba.bt.app;

public class ImageTextData {
   Integer imageId;
   String text;

   public ImageTextData(String var1, Integer var2) {
      this.text = var1;
      this.imageId = var2;
   }

   public Integer getImageId() {
      return this.imageId;
   }

   public String getText() {
      return this.text;
   }
}

package com.zorba.bt.app;
import java.util.ArrayList;

public class Group {

    private ImageTextData Name;
    private ArrayList<Child> Items;

    public ImageTextData getData() {
        return Name;
    }

    public void setData(ImageTextData name) {
        this.Name = name;
    }

    public ArrayList<Child> getItems() {
        return Items;
    }

    public void setItems(ArrayList<Child> Items) {
        this.Items = Items;
    }

}

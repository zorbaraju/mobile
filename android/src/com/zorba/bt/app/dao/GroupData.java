package com.zorba.bt.app.dao;

import com.zorba.bt.app.R;

public class GroupData {

	String name = "";
	String groupType = "";
	public static String groupTypes[] = {
			   "Happy", "Tired"
	   };
	public static int imageResIds[] = {
			   R.raw.happy, R.raw.tired
	   };
	
	public GroupData(String name, String type) {
		this.name = name;
		groupType = type;
	}
	
	public String getName() {
		return name;
	}
	
	public String getType() {
		return groupType;
	}
	
	public int getImageResId() {
		int index = 0;
		for(String t:groupTypes) {
			if( t.equals(groupType)) {
				return imageResIds[index];
			}
			index++;
		}
		return -1;
	}
}

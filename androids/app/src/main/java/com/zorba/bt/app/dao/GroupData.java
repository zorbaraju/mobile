package com.zorba.bt.app.dao;

import com.zorba.bt.app.R;

public class GroupData {

	String name = "";
	String groupType = "";
	public static String groupTypes[] = {
			   "Master  ", "Dinner  ", "Party  ", "Relax  ", "Romantic  "
	   };
	public static int imageResIds[] = {
			   R.raw.master, R.raw.dinner, R.raw.party, R.raw.relax, R.raw.romantic
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

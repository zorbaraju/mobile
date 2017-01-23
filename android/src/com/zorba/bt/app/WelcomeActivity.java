package com.zorba.bt.app;

import java.util.ArrayList;

import com.zorba.bt.app.db.BtLocalDB;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.RadioButton;
import android.widget.Toast;

public class WelcomeActivity extends Activity {

	 private ExpandListAdapter ExpAdapter;
	    private ArrayList<Group> ExpListItems;
	    private ExpandableListView ExpandList;
	    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		System.out.println("Connection type mobile:"+CommonUtils.isMobileDataConnection(this));
		setContentView(R.layout.activity_welcome);
		RadioButton masterbox = (RadioButton)findViewById(R.id.master);
		masterbox.setChecked(true);
		Button gotoButton = (Button)findViewById(R.id.gotobutton);
		gotoButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View paramAnonymousView1) {
				
				String emailid = getValidEmailId();
				if( emailid == null) {
					CommonUtils.AlertBox(WelcomeActivity.this, "Error", "Enter valid email id");
					if( ExpandList.getVisibility() != View.VISIBLE)
						ExpandList.setVisibility(View.VISIBLE);
					else
						ExpandList.setVisibility(View.GONE);
					return;
				}
				RadioButton masterbox = (RadioButton)findViewById(R.id.master);
				saveEmailIdAndUserType(emailid, masterbox.isChecked());
				Intent welcomeIntent = new Intent(WelcomeActivity.this, RoomsActivity.class);
				welcomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
				WelcomeActivity.this.startActivity(welcomeIntent);
			}
		});
		EditText emailfield = (EditText)findViewById(R.id.emailfield);
		emailfield.requestFocus();
		
		 ExpandList = (ExpandableListView) findViewById(R.id.exp_list);
	        ExpListItems = SetStandardGroups();
	        ExpAdapter = new ExpandListAdapter(WelcomeActivity.this, ExpListItems);
	        ExpandList.setAdapter(ExpAdapter);

	        
	        ExpandList.setOnChildClickListener(new OnChildClickListener() {

	            @Override
	            public boolean onChildClick(ExpandableListView parent, View v,
	                    int groupPosition, int childPosition, long id) {

	                ImageTextData group_name = ExpListItems.get(groupPosition).getData();

	                ArrayList<Child> ch_list = ExpListItems.get(
	                        groupPosition).getItems();

	                String child_name = ch_list.get(childPosition).getName();

	                showToastMsg(group_name.getText() + "n" + child_name);

	                return false;
	            }
	        });

	        ExpandList.setOnGroupExpandListener(new OnGroupExpandListener() {

	            @Override
	            public void onGroupExpand(int groupPosition) {
	            	ImageTextData group_name = ExpListItems.get(groupPosition).getData();
	                showToastMsg(group_name.getText() + "n Expanded");

	            }
	        });

	        ExpandList.setOnGroupCollapseListener(new OnGroupCollapseListener() {

	            @Override
	            public void onGroupCollapse(int groupPosition) {
	            	ImageTextData group_name = ExpListItems.get(groupPosition).getData();
	                showToastMsg(group_name.getText() + "n Expanded");

	            }
	        });
	        ExpandList.setVisibility(View.GONE);
	}
	
	public ArrayList<Group> SetStandardGroups() {

		ArrayList<ImageTextData> arrayList = new ArrayList<ImageTextData>();
		arrayList.add(new ImageTextData("Add Room", R.raw.addroom));
		arrayList.add(new ImageTextData("Device Configuration", R.raw.deviceconfig));
		arrayList.add(new ImageTextData("Help", R.raw.help));
		arrayList.add(new ImageTextData("About", R.raw.about));
		arrayList.add(new ImageTextData("Admin Settings", R.raw.settings));
		arrayList.add(new ImageTextData("Change Pwd", R.raw.changepassword));
		arrayList.add(new ImageTextData("Time Settings", R.raw.timesettings));
		arrayList.add(new ImageTextData("Send Log", R.raw.sendlog));
		arrayList.add(new ImageTextData("Enable OOH", R.raw.outofhome));
		arrayList.add(new ImageTextData("Mt Log", R.raw.mtlog));
		arrayList.add(new ImageTextData("Exit", R.raw.exit));
		
        ArrayList<Group> group_list = new ArrayList<Group>();
        ArrayList<Child> child_list;

        for(ImageTextData data: arrayList){
        	Group grp = new Group();
        	grp.setData(data);
        	group_list.add(grp);
        }
        

        return group_list;
    }

    public void showToastMsg(String Msg) {
        Toast.makeText(getApplicationContext(), Msg, Toast.LENGTH_SHORT).show();
    }


	private String getValidEmailId() {
		EditText emailfield = (EditText)findViewById(R.id.emailfield);
		String emailid = emailfield.getText().toString();
		emailid = emailid.trim();
		if( emailid.isEmpty())
			return null;
		return emailid;
	}
	
	private void saveEmailIdAndUserType(String emailid, boolean isMaster) {
		BtLocalDB.getInstance(this).setEmailId(emailid);
		BtLocalDB.getInstance(this).setUserType(isMaster);
	}
}

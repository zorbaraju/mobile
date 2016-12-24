package com.zorba.bt.app;

import java.util.ArrayList;

import com.zorba.bt.app.bluetooth.BtHwLayer;
import com.zorba.bt.app.bluetooth.ConnectionListener;
import com.zorba.bt.app.dao.RoomData;
import com.zorba.bt.app.db.BtLocalDB;
import com.zorba.bt.app.utils.BackgroundTaskDialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

public class RGBController implements ConnectionListener{

	byte[] r = {0x20, 0x10, 0x30, 0x08, 0x28};
	byte[] g = {(byte)0xa0, (byte)0x90, (byte)0xb0, (byte)0x88, (byte)0xa8};
	byte[] b = {(byte)0xc0, 0x50, 0x70, 0x48, 0x68};
	byte[][] remotevalues= { 
			{00, 0x20, 0x10, 0x30, 0x08, 0x28},
			{ 80, (byte)0xa0, (byte)0x90, (byte)0xb0, (byte)0x88, (byte)0xa8},
			{40, (byte)0x60, 0x50, 0x70, 0x48, 0x68},
			{(byte)0xc0, (byte)0xe0, (byte)0xd0, (byte)0xf0, (byte)0xc8, (byte)0xe8},
	};
	String[][] remotevaluescolor= { 
			{"#ffffff", "#ff0000", "#ff3000", "#fa5326","#be4e17", "#faee26"},
			{"#ffffff", "#00ff00", "#00d100", "#00ad00","#158580", "#155f80"},
			{"#000000", "#0000ff", "#2a00ff", "#0513c9","#321a7b", "#881a7b"},
			{"#00ff00", "#ffffff", "#90b9d2", "#90b9d2","#90b9d2", "#90b9d2"}
	};
	String[] remotestrt= { 
			"B-", "B+", "Off", "On",
			"","","","W",
			"","","","FL",
			"","","","S",
			"","","","FD",
			"","","","SM"
	};
	byte[] remotevalt= { 
			(byte)0x00,(byte)0x80,(byte)0x40,(byte)0xc0,
			(byte)0x20,(byte)0xa0,(byte)0x60,(byte)0xe0,
			(byte)0x10,(byte)0x90,(byte)0x50,(byte)0xd0,
			(byte)0x30,(byte)0xb0,(byte)0x70,(byte)0xf0,
			(byte)0x08,(byte)0x88,(byte)0x48,(byte)0xc8,
			(byte)0x28,(byte)0xa8,(byte)0x68,(byte)0xe8
	};	
		
	String remotecolort [] = { 
			"#ffffff", "#ffffff", "#000000", "#00ff00",
			"#ff0000", "#00ff00", "#0000ff", "#ffffff",
			"#ff3000", "#00d100", "#2a00ff", "#90b9d2",
			"#fa5326", "#00ad00", "#0513c9", "#90b9d2",
			"#be4e17", "#158580", "#321a7b", "#90b9d2", 
			"#faee26", "#155f80", "#881a7b", "#90b9d2"
	};
	
	AlertDialog dialog = null;
	
	Activity activity = null;
	LinearLayout rgbPanel  = null;
	BtHwLayer btHwLayer = null;
	
	public RGBController(Activity activity){
		this.activity = activity;
	}
	public void setRGBView(final RoomData selectedRoom){
		rgbPanel  = (LinearLayout)activity.findViewById(R.id.rgbPanel);
		((ScrollView) activity.findViewById(R.id.scrollView)).setVisibility(View.GONE);
		((LinearLayout) activity.findViewById(R.id.rgbPanel)).setVisibility(View.VISIBLE);
		((LinearLayout) activity.findViewById(R.id.emptydevicepanel)).setVisibility(View.GONE);
		/*final ColorPicker colorPickerView = new ColorPicker(activity);
		BackgroundTask task = new BackgroundTask() {
			
			@Override
			public Object runTask(Object params) {
				int color = 0;
				try {
					BtHwLayer.getInstance(activity).initDevice(selectedRoom.getAddress(), selectedRoom.getIpAddress());
					
					byte[] irgb = BtHwLayer.getInstance(activity).readRGBToDevice();
					byte ivalue = irgb[0];
					System.out.println("value read... is "+ivalue+" ...hex.."+Integer.toHexString(ivalue));
					for (byte v : r) {
						if( ivalue == v) {
							color =   (v << 16) ;
						}
					}
					for (byte v : g) {
						if( ivalue == v) {
							color =   (v << 8) ;
						}
					}
					for (byte v : b) {
						if( ivalue == v) {
							color =   v;
						}
					}
					BtHwLayer.getInstance(activity).printBytes("Read...", irgb);
					//color = (irgb[3] << 24) | (irgb[2] << 16) | (irgb[1] << 8) | irgb[0];
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				colorPickerView.setColor(color);
				System.out.println("setcolor...."+color+" ..."+Integer.toHexString(color));
				return null;
			}
			
			@Override
			public void finishedTask(Object result) {
				// TODO Auto-generated method stub
				
			}
		};
		colorPickerView.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_UP) {
					int incolor = colorPickerView.getColor();
					byte si = (byte)(0x000000FF&(incolor>>24));
					byte sr = (byte)(0x000000FF&(incolor>>16));
					byte sg = (byte)(0x000000FF&(incolor>>8));
					byte sb = (byte)(0x000000FF&(incolor>>0));
					si = 0;
					byte [] rgbbytes = {sr,sg,sb};
					int maxindex = 0;
					for(int i=0; i<rgbbytes.length; i++){
						if( rgbbytes[i]>si) {
							si = rgbbytes[i];
							maxindex = i;
						}
					}
					int div = si/50;
					if( maxindex == 0) {
						si = r[div];
					} else if( maxindex == 1) {
						si = g[div];
					} else {
						si = b[div];
					}
					System.out.println("maxindex="+maxindex+" si==="+si+" div="+div+" color...."+incolor+".."+Integer.toHexString(incolor)+" si="+Integer.toHexString(si)+" sr"+Integer.toHexString(sr)+" g="+Integer.toHexString(sg)+" b="+Integer.toHexString(sb));
					try {
						BtHwLayer.getInstance(activity).sendRGBToDevice(si,sr,sg,sb);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				return false;
			}
		});*/
		rgbPanel.removeAllViews();
		//rgbPanel.addView(colorPickerView);
		/*rgbPanel.setOrientation(LinearLayout.HORIZONTAL);
		LinearLayout rl[] = new LinearLayout[4];
		for(int c=0; c<rl.length; c++){
			rl[c]= new LinearLayout(activity);
			rl[c].setOrientation(LinearLayout.VERTICAL);
			LayoutParams lparams = new LayoutParams(-2, -2);
			lparams.setMargins(3, 3,3,3);
			rl[c].setLayoutParams(lparams);
			rl[c].setPadding(3, 3, 3, 3);
			for( int rr=0; rr<6; rr++) {
				Button b = new Button(activity);
				b.setText(""+remotevalues[c][rr]);
				b.setBackgroundColor(Color.parseColor(remotevaluescolor[c][rr]));
				rl[c].addView(b);
				final int row = rr;
				final int col = c;
				b.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						BackgroundTaskDialog dialog = new BackgroundTaskDialog(activity) {
							
							@Override
							public Object runTask(Object params) {
								try {
									btHwLayer.sendRGBToDevice(remotevalues[col][row],(byte)0,(byte)0,(byte)0);
								} catch (Exception e) {
									CommonUtils.AlertBox(activity, "Setting color", e.getMessage());
								}	
								return null;
							}
							
							@Override
							public void finishedTask(Object result) {
								// TODO Auto-generated method stub
								
							}
						};
					}
				});
			}
			rgbPanel.addView(rl[c]);
			
			
		}
		putOffLine(true);
		btHwLayer = BtHwLayer.getInstance(activity);
		btHwLayer.setConnectionListener(this);
		CommonUtils.printStackTrace();
		BackgroundTaskDialog dialog = new BackgroundTaskDialog(activity) {
			
			@Override
			public Object runTask(Object params) {
				String error = btHwLayer.initDevice(selectedRoom.getAddress(), selectedRoom.getIpAddress());
				if (error != null) {
					CommonUtils.AlertBox(activity, "No connection", error);
				}
				return null;
			}
			
			@Override
			public void finishedTask(Object result) {
				// TODO Auto-generated method stub
				
			}
		};*/
		/*new Thread(new Runnable() {
			public void run() {
				try {
					btHwLayer.initDevice(selectedRoom.getAddress(), selectedRoom.getIpAddress());
				} catch (Exception e) {
					CommonUtils.AlertBox(activity, "No connection", e.getMessage());
				}
			}
		}).start();*/
		GridView view = new GridView(activity);
		view.setNumColumns(4);
		rgbPanel.setPadding(30, 0, 30, 0);
		view.setVerticalSpacing(5);
		view.setHorizontalSpacing(5);
		final ArrayList<String> alist = new ArrayList<String>();
		for (String item : remotecolort) {
			alist.add(item);
		}
		TextAdapter adapter = new TextAdapter(activity,  alist, null) {
			public View getView(final int var1, View var2, ViewGroup var3) {
			      Button var4 = new Button(this.context);
			      var4.setTextColor(Color.BLUE);
			      var4.setBackgroundDrawable(activity.getResources().getDrawable(R.layout.roundwhitecorner));
			      GradientDrawable bgShape = (GradientDrawable)var4.getBackground();
			      bgShape.setColor(Color.parseColor(alist.get(var1)));
			      var4.setTextSize(20.0F);
			      var4.setTag(Integer.valueOf(var1));
			      var4.setText(remotestrt[var1]);
			      var4.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							BackgroundTaskDialog dialog = new BackgroundTaskDialog(activity) {
								
								@Override
								public Object runTask(Object params) {
									try {
										btHwLayer.sendRGBToDevice(remotevalt[var1],(byte)0,(byte)0,(byte)0);
									} catch (Exception e) {
										CommonUtils.AlertBox(activity, "Setting color", e.getMessage());
									}	
									return null;
								}
								
								@Override
								public void finishedTask(Object result) {
									// TODO Auto-generated method stub
									
								}
							};
						}
					});
			      return var4;
			   }
		};
		view.setAdapter(adapter);
		rgbPanel.addView(view);
		putOffLine(true);
		btHwLayer = BtHwLayer.getInstance(activity);
		btHwLayer.setConnectionListener(this);
		CommonUtils.printStackTrace();
		BackgroundTaskDialog dialog = new BackgroundTaskDialog(activity) {
			
			@Override
			public Object runTask(Object params) {
				String error = btHwLayer.initDevice(selectedRoom.getAddress(), selectedRoom.getSSID(), selectedRoom.getIpAddress(), BtLocalDB.getInstance(activity).getDevicePwd(), false);
				if (error != null) {
					CommonUtils.AlertBox(activity, "No connection", error);
				}
				return null;
			}
			
			@Override
			public void finishedTask(Object result) {
				// TODO Auto-generated method stub
				
			}
		};
	}
	
	public void connectionStarted(boolean isWifi) {
		putOffLine(false);
	}
	public void connectionLost() {
		putOffLine(true);
	}
	
	private void putOffLine(final boolean isOffline) {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				if( isOffline)
					rgbPanel.setBackgroundColor(Color.GRAY);
				else
					rgbPanel.setBackgroundColor(Color.parseColor("#ff131b22"));
			}
		});
		
	}
}

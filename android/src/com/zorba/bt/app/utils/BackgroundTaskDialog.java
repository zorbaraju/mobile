package com.zorba.bt.app.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Handler;

public abstract class BackgroundTaskDialog extends AsyncTask<Object, Object, Object> {

	boolean isdismissed = false;
	boolean waitDialogEnabled = true;
	private AlertDialog dialog;
	public BackgroundTaskDialog(Activity context) {
		if( waitDialogEnabled ) {
			Builder builder = new Builder(context);
			builder.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					cancelTask();
					dialog.cancel();
				}
			});
			//builder.setCancelable(true);
			dialog = builder.create();
			dialog.setTitle("Wait");
			dialog.setMessage("Please wait");
		}
		execute("");
	}

	public abstract Object runTask(Object params);

	public abstract void finishedTask(Object result);

	@Override
	protected Object doInBackground(Object... params) {
		return runTask(params[0]);
	}

	protected void onPreExecute() {
		if( waitDialogEnabled ) {
			this.dialog.setMessage("Please wait");
			final Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
			  @Override
			  public void run() {
				  if( !isdismissed )
					dialog.show();
			  }
			}, 200);
		}
    }

	protected void cancelTask() {
		
	}
	
    @Override
    protected void onPostExecute(Object result) {
    	isdismissed = true;
    	finishedTask(result);
        if (waitDialogEnabled && dialog.isShowing()) {
            dialog.dismiss();
        }
    }


}

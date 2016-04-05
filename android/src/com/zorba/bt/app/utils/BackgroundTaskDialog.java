package com.zorba.bt.app.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.os.AsyncTask;

public abstract class BackgroundTaskDialog extends AsyncTask<Object, Object, Object> {

	private AlertDialog dialog;
	public BackgroundTaskDialog(Activity context) {
		Builder builder = new Builder(context);
		builder.setCancelable(false);
		dialog = builder.create();
		dialog.setTitle("Wait");
		dialog.setMessage("Please wait");
		execute("");
	}

	public abstract Object runTask(Object params);

	public abstract void finishedTask(Object result);

	@Override
	protected Object doInBackground(Object... params) {
		return runTask(params[0]);
	}

	protected void onPreExecute() {
        this.dialog.setMessage("Please wait");
        this.dialog.show();
    }

    @Override
    protected void onPostExecute(Object result) {
    	finishedTask(result);
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }


}

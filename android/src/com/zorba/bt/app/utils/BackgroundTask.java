package com.zorba.bt.app.utils;

import android.os.AsyncTask;

public abstract class BackgroundTask extends AsyncTask<Object, Object, Object> {

	public BackgroundTask() {
		super();
		execute("");
	}

	public abstract Object runTask(Object params);

	public abstract void finishedTask(Object result);

	@Override
	protected Object doInBackground(Object... params) {
		return runTask(params[0]);
	}

	@Override
	protected void onPostExecute(Object result) {
		finishedTask(result);
	}

}

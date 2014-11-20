package com.ppinera.popcorntv.controller;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.content.Loader;
import android.os.AsyncTask;
import android.os.Bundle;
import com.ppinera.popcorntv.model.LoaderResponse;

public class URLLoader extends Loader<LoaderResponse> {

	public static final String INFO_KEY = "popcorntime_info";
	public static final String URL_KEY = "popcorntime_url";

	private Bundle data = null;
	private URLTask task = null;
	private LoaderResponse response = null;

	public URLLoader(Context context, Bundle data) {
		super(context);
		this.data = data;
	}

	@Override
	protected void onStartLoading() {
		if (response != null) {
			deliverResult(response);
			response = null;
		}
		super.onStartLoading();
	}

	@Override
	protected void onReset() {
		if (task != null && AsyncTask.Status.FINISHED != task.getStatus()) {
			task.cancel(true);
		}
		super.onReset();
	}

	@Override
	protected void onForceLoad() {
		super.onForceLoad();
		task = new URLTask();
		task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, data.getString(URL_KEY), data.getString(INFO_KEY));
	}

	private void setResponse(LoaderResponse response) {
		this.response = response;
	}

	private class URLTask extends AsyncTask<String, Void, LoaderResponse> {

		@Override
		protected LoaderResponse doInBackground(String... params) {
			LoaderResponse response = new LoaderResponse();
			try {
				// Log.d("tag", "URLLoader: " + params[0]);
				DefaultHttpClient client = new DefaultHttpClient();
				HttpResponse httpResponse = client.execute(new HttpGet(params[0]));
				HttpEntity entity = httpResponse.getEntity();
				response.data = EntityUtils.toString(entity);
				response.info = params[1];
			} catch (Exception ex) {
				response.error = ex.getMessage();
			}

			if (isCancelled()) {
				response = null;
			}

			return response;
		}

		@Override
		protected void onPostExecute(LoaderResponse result) {
			if (isStarted()) {
				deliverResult(result);
			} else {
				setResponse(result);
			}
		}
	}
}
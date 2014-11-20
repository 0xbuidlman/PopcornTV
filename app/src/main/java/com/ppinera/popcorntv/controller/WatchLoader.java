package com.ppinera.popcorntv.controller;

import android.content.Context;
import android.content.Loader;
import android.os.AsyncTask;
import android.os.Bundle;
import com.ppinera.popcorntv.model.LoaderResponse;
import com.ppinera.popcorntv.torrent.TorrentService;

public class WatchLoader extends Loader<LoaderResponse> {

	public static final String WATCH_FOLDER_PATH_KEY = "watch_folder_path";
	public static final String TORRENT_URL_KEY = "torrent_url";

	private Bundle data = null;
	private WatchTask task = null;
	private LoaderResponse response = null;

	public WatchLoader(Context context, Bundle data) {
		super(context);
		this.data = data;
	}

	@Override
	protected void onStartLoading() {
		super.onStartLoading();
		if (response != null) {
			deliverResult(response);
			response = null;
		}
	}

	@Override
	protected void onReset() {
		super.onReset();
		if (task != null && AsyncTask.Status.FINISHED != task.getStatus()) {
			task.cancel(true);
		}
	}

	@Override
	protected void onForceLoad() {
		super.onForceLoad();

		task = new WatchTask();
		task.execute(data.getString(WATCH_FOLDER_PATH_KEY), data.getString(TORRENT_URL_KEY));
	}

	private void setResponse(LoaderResponse response) {
		this.response = response;
	}

	private class WatchTask extends AsyncTask<String, Void, LoaderResponse> {

		@Override
		protected LoaderResponse doInBackground(String... params) {
			LoaderResponse response = new LoaderResponse();

			String cachePath = params[0];
			String torrentUrl = params[1];
			try {
				String torrentFilePath = cachePath + "/" + TorrentService.WATCH_NOW_TORRENT_FILE_NAME;
				TorrentService.loadTorrentFile(torrentUrl, torrentFilePath);
				response.data = torrentFilePath;
			} catch (Exception ex) {
				ex.printStackTrace();
				response.error = ex.getMessage();
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

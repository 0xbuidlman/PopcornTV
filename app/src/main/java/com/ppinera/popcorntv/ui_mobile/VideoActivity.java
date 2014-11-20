package com.ppinera.popcorntv.ui_mobile;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import dp.ws.popcorntime.R;
import com.ppinera.popcorntv.controller.URLLoader;
import com.ppinera.popcorntv.model.LoaderResponse;
import com.ppinera.popcorntv.model.videodata.VideoData;
import com.ppinera.popcorntv.model.videoinfo.VideoInfo;
import com.ppinera.popcorntv.ui_mobile.base.PopcornLoadActivity;
import com.ppinera.popcorntv.ui_mobile.base.VideoTypeFragment;

public class VideoActivity extends PopcornLoadActivity implements OnClickListener, LoaderCallbacks<LoaderResponse> {

	public static final String VIDEO_INFO_KEY = "popcorntime_video_info";

	private final int INFO_LOADER_ID = 1001;

	private VideoInfo mVideoInfo;
	private VideoTypeFragment videoFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.Theme_Popcorn_Classic);
		super.onCreate(savedInstanceState);

		mVideoInfo = getIntent().getExtras().getParcelable(VIDEO_INFO_KEY);

		// Header
		View header = setPopcornHeaderView(R.layout.header_video);
		header.findViewById(R.id.popcorn_action_back).setOnClickListener(VideoActivity.this);
		header.findViewById(R.id.popcorn_action_settings).setOnClickListener(VideoActivity.this);

		// Content
		setPopcornContentViewId(R.id.popcorn_content);
		setPopcornContentBackgroundResource(R.color.classic_video_body);

		restartInfoLoader();
	}

	@Override
	public void showContent() {
		replaceFragment(videoFragment);
	}

	@Override
	public void retryLoad() {
		restartInfoLoader();
	}

	private void restartInfoLoader() {
		Bundle data = new Bundle();
		data.putString(URLLoader.URL_KEY, mVideoInfo.getInfoUrl());
		data.putString(URLLoader.INFO_KEY, mVideoInfo.getType());
		getLoaderManager().restartLoader(INFO_LOADER_ID, data, this).forceLoad();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.popcorn_action_back:
			onBackPressed();
			break;
		case R.id.popcorn_action_settings:
			startActivity(new Intent(VideoActivity.this, SettingsActivity.class));
			break;
		default:
			break;
		}
	}

	@Override
	public Loader<LoaderResponse> onCreateLoader(int id, Bundle args) {
		showLoading();

		switch (id) {
		case INFO_LOADER_ID:
			return new URLLoader(VideoActivity.this, args);
		default:
			return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<LoaderResponse> loader, LoaderResponse response) {
		switch (loader.getId()) {
		case INFO_LOADER_ID:
			loaderHandler.sendMessage(loaderHandler.obtainMessage(INFO_LOADER_ID, response));
			break;
		default:
			break;
		}
	}

	@Override
	public void onLoaderReset(Loader<LoaderResponse> loader) {

	}

	private Handler loaderHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case INFO_LOADER_ID:
				infoFinished((LoaderResponse) msg.obj);
				break;
			default:
				break;
			}
		}
	};

	private void infoFinished(LoaderResponse response) {
		if (response.error != null) {
			showError();
		} else {
			if (VideoData.Type.MOVIES.equals(response.info)) {
				videoFragment = new VideoMovieFragment();
			} else if (VideoData.Type.TV_SHOWS.equals(response.info)) {
				videoFragment = new VideoTVShowFragment();
			}
			Bundle args = new Bundle();
			args.putString(VideoTypeFragment.RESPONSE_JSON_KEY, response.data);
			videoFragment.setArguments(args);
			showContent();
		}
	}

	@Override
	public void onBackPressed() {
		if (videoFragment != null && videoFragment.isAdded()) {
			videoFragment.onBackPressed();
		} else {
			super.onBackPressed();
		}
	}
}
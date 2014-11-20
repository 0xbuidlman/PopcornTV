package com.ppinera.popcorntv.ui_mobile;

import java.util.ArrayList;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.GridView;
import dp.ws.popcorntime.R;
import com.ppinera.popcorntv.controller.URLLoader;
import com.ppinera.popcorntv.controller.VideoAdapter;
import com.ppinera.popcorntv.model.LoaderResponse;
import com.ppinera.popcorntv.model.videodata.VideoData;
import com.ppinera.popcorntv.model.videoinfo.VideoInfo;
import com.ppinera.popcorntv.ui_mobile.locale.LocaleFragment;
import com.ppinera.popcorntv.utils.JSONHelper;

public class GridVideoFragment extends LocaleFragment implements LoaderCallbacks<LoaderResponse> {

	public static final String VIDEO_INFO_LIST_KEY = "popcorntime_video_info_list";

	private final int PAGE_ERROR_DELAY = 2500;
	private final int PAGE_LOADER_ID = 1001;
	private final int MAX_PAGE_COUNT = 6;
	private final int ROW_COUNT_FOR_NEXT_PAGE = 4;

	private GridView videoGrid;
	private VideoData mVideoData;
	private VideoAdapter mVideosAdapter;

	private boolean canLoadPage;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ArrayList<VideoInfo> info = getArguments().getParcelableArrayList(VIDEO_INFO_LIST_KEY);
		mVideosAdapter = new VideoAdapter(getActivity(), info);
		canLoadPage = true;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_grid_video, container, false);
		populateView(view);
		return view;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		// change orientation
		ViewGroup container = (ViewGroup) getView();
		container.removeAllViewsInLayout();
		View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_grid_video, container);
		populateView(view);
	}

	private void populateView(View view) {
		videoGrid = (GridView) view.findViewById(R.id.videos_content_view);
		videoGrid.setAdapter(mVideosAdapter);
		videoGrid.setOnScrollListener(contentScrollListener);
	}

	@Override
	public void onDestroy() {
		getLoaderManager().destroyLoader(PAGE_LOADER_ID);
		super.onDestroy();
	}

	@Override
	public Loader<LoaderResponse> onCreateLoader(int id, Bundle args) {
		switch (id) {
		case PAGE_LOADER_ID:
			return new URLLoader(getActivity(), args);
		default:
			return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<LoaderResponse> loader, LoaderResponse response) {
		switch (loader.getId()) {
		case PAGE_LOADER_ID:
			videosPageFinished(response);
			break;
		default:
			break;
		}
	}

	public void setVideoData(VideoData videoData) {
		this.mVideoData = videoData;
	}

	private void videosPageFinished(LoaderResponse response) {
		if (response.error != null) {
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					mVideoData.setPage(mVideoData.getPage() - 1);
					canLoadPage = true;
				}
			}, PAGE_ERROR_DELAY);
		} else {
			ArrayList<VideoInfo> data = null;
			try {
				if (VideoData.Type.MOVIES.equals(response.info)) {
					data = JSONHelper.parseMovies(response.data);
				} else if (VideoData.Type.TV_SHOWS.equals(response.info)) {
					data = JSONHelper.parseTVShows(response.data);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			if (data != null && data.size() > 0) {
				canLoadPage = true;
				mVideosAdapter.addData(data);
			}
		}
	}

	@Override
	public void onLoaderReset(Loader<LoaderResponse> loader) {

	}

	private void restartVideosPageLoader() {
		mVideoData.setPage(mVideoData.getPage() + 1);
		if (mVideoData.getPage() <= MAX_PAGE_COUNT) {
			Bundle data = new Bundle();
			data.putString(URLLoader.URL_KEY, mVideoData.getRequestURl());
			data.putString(URLLoader.INFO_KEY, mVideoData.getType());
			getLoaderManager().restartLoader(PAGE_LOADER_ID, data, this).forceLoad();
		}
	}

	private OnScrollListener contentScrollListener = new OnScrollListener() {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {

		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			if (canLoadPage && totalItemCount != 0) {
				int dec = totalItemCount - firstVisibleItem;
				if (dec <= videoGrid.getNumColumns() * ROW_COUNT_FOR_NEXT_PAGE) {
					canLoadPage = false;
					restartVideosPageLoader();
				}
			}
		}
	};
}
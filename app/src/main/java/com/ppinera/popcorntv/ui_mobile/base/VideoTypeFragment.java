package com.ppinera.popcorntv.ui_mobile.base;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.softwarrior.libtorrent.TorrentState;

import com.ppinera.popcorntv.PopcornApplication;
import dp.ws.popcorntime.R;
import com.ppinera.popcorntv.controller.WatchLoader;
import dp.ws.popcorntime.database.tables.Downloads;
import dp.ws.popcorntime.database.tables.Favorites;
import com.ppinera.popcorntv.model.DownloadInfo;
import com.ppinera.popcorntv.model.LoaderResponse;
import com.ppinera.popcorntv.model.WatchInfo;
import com.ppinera.popcorntv.model.videoinfo.Torrent;
import com.ppinera.popcorntv.model.videoinfo.VideoInfo;
import com.ppinera.popcorntv.subtitles.SubtitleListener;
import com.ppinera.popcorntv.subtitles.Subtitles;
import com.ppinera.popcorntv.ui_mobile.VLCPlayerActivity;
import com.ppinera.popcorntv.ui_mobile.VideoActivity;
import com.ppinera.popcorntv.utils.StorageHelper;

public abstract class VideoTypeFragment extends VideoBaseFragment implements LoaderCallbacks<LoaderResponse>, SubtitleListener {

	public static final String RESPONSE_JSON_KEY = "popcorntime_response_json";

	private final int WATCH_LOADER_ID = 1001;

	protected PopcornBaseActivity mActivity;
	protected Subtitles mSubtitles;
	protected Torrent torrentInfo;
	protected int torrentPosition = 0;
	private DisplayImageOptions imageOptions;
	private VideoInfo videoInfo;
	private boolean isFavorites;
	private ArrayAdapter<String> mSubtitleAdapter;
	private ArrayAdapter<String> mTorrentAdapter;

	// view
	protected View prepare;
	protected ToggleButton favorites;
	protected RatingBar rating;
	protected Spinner subtitleSpinner;
	protected Spinner torrentSpinner;
	private Animation prepareAnim;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActivity = (PopcornBaseActivity) getActivity();
		videoInfo = mActivity.getIntent().getExtras().getParcelable(VideoActivity.VIDEO_INFO_KEY);
		imageOptions = new DisplayImageOptions.Builder().cacheInMemory(false).imageScaleType(ImageScaleType.EXACTLY_STRETCHED).cacheOnDisk(true).build();
		prepareAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.popcorn_prepare);
		mSubtitleAdapter = new ArrayAdapter<String>(getActivity(), R.layout.item_spinner_video);
		mSubtitleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mTorrentAdapter = new ArrayAdapter<String>(getActivity(), R.layout.item_spinner_video);
		mTorrentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = mActivity.setPopcornSplashView(R.layout.view_prepare);
		prepare = view.findViewById(R.id.video_prepare);
		Button close = (Button) view.findViewById(R.id.video_prepare_close);
		close.setOnClickListener(closeListener);
		return view;
	}

	@Override
	protected void populateView(View view) {
		super.populateView(view);
		ImageLoader.getInstance().displayImage(videoInfo.posterBigUrl, poster, imageOptions);
		title.setText(Html.fromHtml("<b>" + videoInfo.title + "</b>"));
		rating = (RatingBar) view.findViewById(R.id.video_rating);
		rating.setRating(videoInfo.rating * RATING_COEF);
		favorites = (ToggleButton) view.findViewById(R.id.video_favorites);
		favorites.setChecked(isFavorites);
		favorites.setOnCheckedChangeListener(favoritesListener);
		subtitleSpinner = (Spinner) view.findViewById(R.id.video_subtitles);
		torrentSpinner = (Spinner) view.findViewById(R.id.video_torrents);

		initSubtitleSpinner();
		initTorrentSpinner();
	}

	@Override
	public void updateLocaleText() {
		super.updateLocaleText();
		subtitleSpinner.setPromptId(R.string.subtitles);
		torrentSpinner.setPromptId(R.string.torrents);
		replaceSubtitleData(mSubtitles.data);
	}

	@Override
	protected void checkIsDownloads() {
		if (torrentInfo == null) {
			downloadOpenBtn.setVisibility(View.GONE);
		} else {
			String selection = Downloads._TORRENT_URL + "=\"" + torrentInfo.url + "\"";
			Cursor cursor = Downloads.query(mActivity, null, selection, null, null);
			if (cursor != null) {
				if (cursor.getCount() > 0) {
					isDownloads = true;
					showOpenBtn();
				} else {
					isDownloads = false;
					showDownloadBtn();
				}
				cursor.close();
			}
		}
	}

	@Override
	protected long getFileSize() {
		if (torrentInfo != null) {
			return torrentInfo.size;
		}
		return 0;
	}

	@Override
	protected String getTorrentUrl() {
		if (torrentInfo != null) {
			return torrentInfo.url;
		}
		return null;
	}

	@Override
	protected DownloadInfo createDownloadInfo() {
		DownloadInfo info = new DownloadInfo();
		info.type = videoInfo.getType();
		info.imdb = videoInfo.imdb;
		info.torrentUrl = torrentInfo.url;
		info.fileName = torrentInfo.file;
		info.posterUrl = videoInfo.posterMediumUrl;
		info.title = videoInfo.title;
		info.summary = getDownloadSummary();
		info.subtitlesDataUrl = getSubtitlesDataUrl();
		info.state = TorrentState.DOWNLOADING;
		info.size = torrentInfo.size;
		return info;
	}

	@Override
	public Loader<LoaderResponse> onCreateLoader(int id, Bundle args) {
		switch (id) {
		case Subtitles.LOADER_ID:
			return mSubtitles.onCreateLoader(id, args);
		case WATCH_LOADER_ID:
			return new WatchLoader(getActivity(), args);
		default:
			return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<LoaderResponse> loader, LoaderResponse response) {
		switch (loader.getId()) {
		case Subtitles.LOADER_ID:
			mSubtitles.onLoadFinished(loader, response);
			break;
		case WATCH_LOADER_ID:
			loadWatchFinished(response);
		default:
			break;
		}
	}

	@Override
	public void onLoaderReset(Loader<LoaderResponse> loader) {

	}

	@Override
	public void onSubtitleLoadSucces(String data) {
		if (isAdded()) {
			replaceSubtitleData(mSubtitles.data);
		}
	}

	@Override
	public void onSubtitleLoadError(String message) {

	}

	public void onBackPressed() {
		if (mActivity.isPopcornSplashVisible()) {
			breakPrepare();
		} else {
			getActivity().finish();
		}
	}

	protected void checkIsFavorites(VideoInfo info) {
		String selection = Favorites._IMDB + "=\"" + info.imdb + "\"";
		Cursor cursor = Favorites.query(mActivity, null, selection, null, null);
		if (cursor != null && cursor.getCount() > 0) {
			isFavorites = true;
			Favorites.update(mActivity, info);
		} else {
			isFavorites = false;
		}
		cursor.close();
	}

	protected void replaceSubtitleData(List<String> subtitleSpinnerData) {
		if (subtitleSpinnerData != null && subtitleSpinnerData.size() > 0) {
			subtitleSpinnerData.set(0, getResources().getString(R.string.without_subtitle));

			mSubtitleAdapter.clear();
			mSubtitleAdapter.addAll(subtitleSpinnerData);

			subtitleSpinner.setVisibility(View.VISIBLE);
			subtitleSpinner.setSelection(mSubtitles.position, false);
		}
	}

	protected void replaceTorrentData(ArrayList<Torrent> torrents) {
		if (torrents.size() > 0) {
			List<String> torrentSpinnerData = new ArrayList<String>();
			for (int i = 0; i < torrents.size(); i++) {
				Torrent torrent = torrents.get(i);
				torrentSpinnerData.add(torrent.quality + ", " + getResources().getString(R.string.size) + " " + StorageHelper.getSizeText(torrent.size) + ", "
						+ getResources().getString(R.string.seeds) + " " + torrent.seeds + ", " + getResources().getString(R.string.peers) + " "
						+ torrent.peers);
			}

			mTorrentAdapter.clear();
			mTorrentAdapter.addAll(torrentSpinnerData);

			torrentSpinner.setVisibility(View.VISIBLE);
			torrentSpinner.setSelection(torrentPosition, false);
			// is spinner item now selected setSelection don't work
			updateTorrentInfo(torrentPosition);
			watchItNow.setVisibility(View.VISIBLE);
		} else {
			torrentSpinner.setVisibility(View.GONE);
			downloadOpenBtn.setVisibility(View.GONE);
			watchItNow.setVisibility(View.GONE);
		}
	}

	private void onFavoritesChecked(boolean isChecked) {
		if (isChecked) {
			Favorites.insert(mActivity, videoInfo);
		} else {
			Favorites.delete(mActivity, videoInfo);
		}
	}

	private void initSubtitleSpinner() {
		subtitleSpinner.setAdapter(mSubtitleAdapter);
		subtitleSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				mSubtitles.position = position;
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
	}

	private void initTorrentSpinner() {
		torrentSpinner.setAdapter(mTorrentAdapter);
		torrentSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				torrentPosition = position;
				updateTorrentInfo(position);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
	}

	private void loadWatchFinished(LoaderResponse response) {
		if (response.error != null) {
			Toast.makeText(getActivity(), R.string.error_metadata, Toast.LENGTH_SHORT).show();
		} else {
			if (response.data != null) {
				WatchInfo watchInfo = new WatchInfo();
				watchInfo.type = videoInfo.getType();
				watchInfo.imdb = videoInfo.imdb;
				watchInfo.torrentFilePath = response.data;
				watchInfo.fileName = torrentInfo.file;
				watchInfo.subtitlesDataUrl = getSubtitlesDataUrl();
				watchInfo.subtitlesPosition = mSubtitles.position;
				watchInfo.subtitlesData = mSubtitles.data;
				watchInfo.subtitlesUrls = mSubtitles.urls;
				VLCPlayerActivity.watch(mActivity, watchInfo);
			}
		}

		prepare.clearAnimation();
		mActivity.setPopcornSplashVisible(false);
	}

	private void breakPrepare() {
		getLoaderManager().destroyLoader(WATCH_LOADER_ID);
		prepare.clearAnimation();
		mActivity.setPopcornSplashVisible(false);
	}

	protected abstract void updateTorrentInfo(int position);

	protected abstract String getDownloadSummary();

	protected abstract String getSubtitlesDataUrl();

	/*
	 * TODO: Listeners
	 */

	private OnClickListener closeListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			breakPrepare();
		}
	};

	private OnCheckedChangeListener favoritesListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			onFavoritesChecked(isChecked);
			isFavorites = isChecked;
		}

	};

	/*
	 * TODO: Handler
	 */

	@Override
	protected boolean handleWatchDownload() {
		String selection = Downloads._TORRENT_URL + "=\"" + torrentInfo.url + "\"";
		Cursor cursor = Downloads.query(getActivity(), null, selection, null, null);
		if (cursor != null && cursor.getCount() == 1) {
			DownloadInfo info = new DownloadInfo();
			try {
				cursor.moveToFirst();
				info.populate(cursor);
				VLCPlayerActivity.watch(getActivity(), new WatchInfo(info, mSubtitles));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			isDownloads = false;
			showDownloadBtn();
		}

		return true;
	}

	@Override
	protected boolean handleWatch() {
		if (super.handleWatch()) {
			if (null != torrentInfo) {
				if (TextUtils.isEmpty(torrentInfo.url)) {
					return false;
				}
				SharedPreferences prefs = getActivity().getSharedPreferences(PopcornApplication.POPCORN_PREFERENCES, Activity.MODE_PRIVATE);
				String lastTorrentUrl = prefs.getString(LAST_TORRENT_PREF, "");
				if (!lastTorrentUrl.equals(torrentInfo.url)) {
					StorageHelper.getInstance().clearChacheDirectory();
					if (checkFreeSpace(cacheDirectory.getAbsolutePath(), torrentInfo.size)) {
						prefs.edit().putString(LAST_TORRENT_PREF, torrentInfo.url).commit();
					} else {
						prefs.edit().putString(LAST_TORRENT_PREF, "").commit();
						return false;
					}
				}

				prepare.startAnimation(prepareAnim);
				mActivity.setPopcornSplashVisible(true);

				Bundle data = new Bundle();
				data.putString(WatchLoader.WATCH_FOLDER_PATH_KEY, cacheDirectory.getAbsolutePath());
				data.putString(WatchLoader.TORRENT_URL_KEY, torrentInfo.url);
				getLoaderManager().restartLoader(WATCH_LOADER_ID, data, VideoTypeFragment.this).forceLoad();
			}
		} else {
			return false;
		}

		return true;
	}
}
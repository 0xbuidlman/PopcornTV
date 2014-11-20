package com.ppinera.popcorntv.ui_mobile.base;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.vlc.audio.AudioServiceController;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.MediaRouteButton;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.softwarrior.libtorrent.StorageMode;
import com.softwarrior.libtorrent.TorrentState;

import com.ppinera.popcorntv.PopcornApplication;
import dp.ws.popcorntime.R;
import dp.ws.popcorntime.chromecast.Chromecast;
import dp.ws.popcorntime.chromecast.PopcornCastListener;
import com.ppinera.popcorntv.model.LoaderResponse;
import com.ppinera.popcorntv.model.WatchInfo;
import com.ppinera.popcorntv.model.videodata.VideoData;
import com.ppinera.popcorntv.subtitles.SubtitleListener;
import com.ppinera.popcorntv.subtitles.Subtitles;
import com.ppinera.popcorntv.subtitles.format.VTT;
import com.ppinera.popcorntv.torrent.Prioritizer;
import com.ppinera.popcorntv.torrent.TorrentService;
import com.ppinera.popcorntv.torrent.VideoResult;
import com.ppinera.popcorntv.ui_mobile.VLCPlayerActivity;
import com.ppinera.popcorntv.ui_mobile.locale.LocaleFragmentActivity;
import com.ppinera.popcorntv.utils.ExtGenericFilter;
import com.ppinera.popcorntv.utils.StorageHelper;

public abstract class PlayerBaseActivity extends LocaleFragmentActivity implements PopcornCastListener, LoaderCallbacks<LoaderResponse>, SubtitleListener {

	public static final String SETTINGS_HW_ACCELERATION = "hardware-acceleration";

	private static final String WATCH_INFO_EXTARA_KEY = "watch_info";

	protected LibVLC mLibVLC;
	protected String mLocation;
	protected SharedPreferences mPreferences;
	protected TextView mTitle;
	protected Button mCloseButton;
	protected int savedIndexPosition = -1;
	private String mVideoPath;
	private boolean isPaused;
	private WatchInfo mWatchInfo;

	// torrent
	protected boolean isTorrentVideoReady = false;
	protected Prioritizer prioritizer;
	private PrepareVideoTask mPrepareVideoTask = null;
	private boolean wasPaused = false;
	private String mContentFile;
	private ProgressBar mTorrentProgressBar;
	private TextView mTorrentProgressText;

	// google cast
	protected Chromecast mChromecast;
	protected boolean isCastEnabled = false;
	private MediaRouteButton mRouteButton;
	private boolean isRouteSelected = false;
	private boolean isCastPlaying = false;

	// subtitle
	protected Subtitles mSubtitles;
	protected ImageButton mSubtitleButton;
	private String mSubtitlePath;
	private String mVTTSubtitlePath;
	private SubtitleLoadTask mSubtitleLoadTask;

	// Volume
	protected AudioManager mAudioManager;
	protected int mAudioMax;

	private SubtitleDialog mSubtitleDialog;
	private ErrorDialog mErrorDialog;
	private CastErrorDialog mCastErrorDialog;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		mPreferences = getSharedPreferences(PopcornApplication.POPCORN_PREFERENCES, Activity.MODE_PRIVATE);
		mWatchInfo = getIntent().getParcelableExtra(WATCH_INFO_EXTARA_KEY);
		prioritizer = new Prioritizer(TorrentService.LibTorrent);
		mSubtitles = new Subtitles(PlayerBaseActivity.this);
		mSubtitles.setSubtitleListener(PlayerBaseActivity.this);
		mSubtitles.position = mWatchInfo.subtitlesPosition;
		mSubtitles.data = mWatchInfo.subtitlesData;
		mSubtitles.urls = mWatchInfo.subtitlesUrls;

		mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		mAudioMax = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, mVolumeObserver);
	}

	protected void initPopcorn() {
		mCloseButton = (Button) findViewById(R.id.player_overlay_close);
		mCloseButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		mTorrentProgressBar = (ProgressBar) findViewById(R.id.torrent_progress_bar);
		mTorrentProgressText = (TextView) findViewById(R.id.torrent_progress_text);

		mRouteButton = (MediaRouteButton) findViewById(R.id.media_route_button);
		mChromecast = new Chromecast(PlayerBaseActivity.this, PlayerBaseActivity.this);
		mChromecast.onCreate(mRouteButton);

		mSubtitleButton = (ImageButton) findViewById(R.id.player_overlay_subtitle);
	}

	@Override
	protected void onPause() {
		isPaused = true;
		if (mChromecast != null) {
			mChromecast.onPause();
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		isPaused = false;
		super.onResume();
		if (mChromecast != null) {
			mChromecast.onResume();
		}
	}

	@Override
	protected void onDestroy() {
		if (mChromecast != null) {
			mChromecast.onDestroy();
		}
		cancelAsyncTask(mSubtitleLoadTask);
		destroyTorrent();
		getContentResolver().unregisterContentObserver(mVolumeObserver);
		super.onDestroy();
	}

	@Override
	public Loader<LoaderResponse> onCreateLoader(int id, Bundle args) {
		if (Subtitles.LOADER_ID == id) {
			return mSubtitles.onCreateLoader(id, args);
		}
		return null;
	}

	@Override
	public void onLoadFinished(Loader<LoaderResponse> loader, LoaderResponse response) {
		if (Subtitles.LOADER_ID == loader.getId()) {
			mSubtitles.onLoadFinished(loader, response);
		}
	}

	@Override
	public void onLoaderReset(Loader<LoaderResponse> laoder) {

	}

	/*
	 * TODO: Torrent
	 */

	protected void loadVideo() {
		if (mPrepareVideoTask != null) {
			return;
		}
		if (!TextUtils.isEmpty(mWatchInfo.torrentFilePath)) {
			mPrepareVideoTask = new PrepareVideoTask();
			mPrepareVideoTask.execute(PrepareVideoTask.LOAD, mWatchInfo.torrentFilePath, mWatchInfo.fileName);
		}
	}

	protected void reloadVideo() {
		AudioServiceController.getInstance().stop();
		mLibVLC.stop();
		cancelAsyncTask(mPrepareVideoTask);
		mPrepareVideoTask = new PrepareVideoTask();
		mPrepareVideoTask.execute(PrepareVideoTask.RELOAD);
	}

	private void destroyTorrent() {
		cancelAsyncTask(mPrepareVideoTask);
		prioritizer.stop();
		if (!TextUtils.isEmpty(mContentFile)) {
			if (mWatchInfo.isDownloads) {
				if (wasPaused) {
					TorrentService.LibTorrent.PauseTorrent(mContentFile);
				}
			} else {
				TorrentService.LibTorrent.PauseTorrent(mContentFile);
			}
		}
	}

	protected int getTorrentState() {
		if (TextUtils.isEmpty(mContentFile)) {
			return -1;
		}
		return TorrentService.LibTorrent.GetTorrentState(mContentFile);
	}

	protected long getProgressSizeMB() {
		if (TextUtils.isEmpty(mContentFile)) {
			return -1;
		}
		return TorrentService.LibTorrent.GetTorrentProgressSize(mContentFile);
	}

	/*
	 * TODO: Chromecast
	 */

	@Override
	public void onCastConnection() {
		mChromecast.setVolume(mAudioMax, mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
		if (!TextUtils.isEmpty(mSubtitlePath) && new File(mSubtitlePath).exists()) {
			boolean isConvert = VTT.convert(mSubtitlePath, mVTTSubtitlePath);
			if (isConvert) {
				mChromecast.loadMovieMedia(mLocation, mLibVLC.getTime(), mContentFile, mVTTSubtitlePath);
			} else {
				mChromecast.loadMovieMedia(mLocation, mLibVLC.getTime(), mContentFile, null);
			}
		} else {
			mChromecast.loadMovieMedia(mLocation, mLibVLC.getTime(), mContentFile, null);
		}
	}

	@Override
	public void onCastRouteSelected() {
		isRouteSelected = true;
		if (mLibVLC.isPlaying()) {
			mLibVLC.pause();
		}
	}

	@Override
	public void onCastRouteUnselected(long position) {
		isRouteSelected = false;
		if (position > 0) {
			mLibVLC.setTime(position);
		}
	}

	@Override
	public void onCastStatePlaying() {
		isCastPlaying = true;
	}

	@Override
	public void onCastStatePaused() {
		isCastPlaying = false;
	}

	@Override
	public void onCastStateIdle() {

	}

	@Override
	public void onCastStateBuffering() {

	}

	@Override
	public void onCastMediaLoadSuccess() {
		isCastEnabled = true;
		if (mSubtitles.position > 0) {
			sendSubtitleToChromecast(true);
		}
	}

	@Override
	public void onCastMediaLoadCancelInterrupt() {
		showCastErrorDialog();
	}

	@Override
	public void teardown() {
		isRouteSelected = false;
		isCastEnabled = false;
		isCastPlaying = false;
	}

	/*
	 * TODO: Subtitles
	 */

	private void restartSubtitlesLoader() {
		mSubtitleButton.setVisibility(View.GONE);
		mSubtitles.restartLoader(mWatchInfo.subtitlesDataUrl, PlayerBaseActivity.this);
	}

	@Override
	public void onSubtitleLoadSucces(String data) {
		try {
			JSONObject subs = new JSONObject(data);
			if (VideoData.Type.MOVIES.equals(mWatchInfo.type)) {
				mSubtitles.parseMovies(subs, mWatchInfo.imdb);
			} else if (VideoData.Type.TV_SHOWS.equals(mWatchInfo.type)) {
				mSubtitles.parseTVShows(subs);
			}
		} catch (JSONException e) {
		}

		if (mSubtitles.data != null && mSubtitles.data.size() > 0) {
			mSubtitles.data.set(0, getResources().getString(R.string.without_subtitle));
			initSubtitle();
			downloadSubtitle();
		}
	}

	@Override
	public void onSubtitleLoadError(String message) {
		// reload
	}

	protected void removeSubtitleFiles() {
		File path = new File(mVideoPath);
		StorageHelper.deleteRecursive(path, new ExtGenericFilter(Subtitles.FORMAT_SRT));
		StorageHelper.deleteRecursive(path, new ExtGenericFilter(Subtitles.FORMAT_VTT));
	}

	private void initSubtitle() {
		mSubtitleButton.setVisibility(View.VISIBLE);
		mSubtitleButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showSubtitlesDialog();
			}
		});
	}

	private void downloadSubtitle() {
		cancelAsyncTask(mSubtitleLoadTask);
		mSubtitleLoadTask = new SubtitleLoadTask();
		mSubtitleLoadTask.execute(mSubtitles.getUrl());
	}

	/*
	 * TODO: Player
	 */

	protected void popcornPlay(long time) {
		AudioServiceController.getInstance().stop();
		mLibVLC.setMediaList();
		mLibVLC.getMediaList().add(new Media(mLibVLC, "file://" + mLocation));
		savedIndexPosition = mLibVLC.getMediaList().size() - 1;
		if (isPaused) {
			return;
		}
		mLibVLC.playIndex(savedIndexPosition);
		mLibVLC.setTime(time);
	}

	protected void popcornPlay() {
		if (isCastEnabled) {
			mChromecast.play();
		} else {
			if (!isRouteSelected) {
				mLibVLC.play();
			}
		}
	}

	protected void popcornPause() {
		if (isCastEnabled) {
			mChromecast.pause();
		} else {
			if (!isRouteSelected) {
				mLibVLC.pause();
			}
		}
	}

	protected boolean popcornIsPlaying() {
		if (isCastEnabled) {
			return isCastPlaying;
		} else {
			return mLibVLC.isPlaying();
		}
	}

	protected void popcornSetTime(long position) {
		if (prioritizer.canSeekTo(mLibVLC.getLength(), position)) {
			mLibVLC.setTime(position);
			if (isCastEnabled) {
				mChromecast.setPosition(position);
			}
		}
	}

	/*
	 * TODO: Tasks
	 */

	private void cancelAsyncTask(AsyncTask<?, ?, ?> task) {
		if (task != null && AsyncTask.Status.FINISHED != task.getStatus()) {
			task.cancel(true);
		}
	}

	private class PrepareVideoTask extends AsyncTask<String, Integer, VideoResult> {

		public static final String LOAD = "load";
		public static final String RELOAD = "reload";

		@Override
		protected void onPreExecute() {
			isTorrentVideoReady = false;
			mCloseButton.setVisibility(View.VISIBLE);
			mTorrentProgressBar.setVisibility(View.VISIBLE);
			mTorrentProgressBar.setProgress(0);
			mTorrentProgressText.setVisibility(View.VISIBLE);
			mTorrentProgressText.setText(R.string.checking_data);
		}

		@Override
		protected VideoResult doInBackground(String... params) {
			if (LOAD == params[0]) {
				String torrentFilePath = params[1];
				String fileName = params[2];
				String savePath = torrentFilePath.substring(0, torrentFilePath.lastIndexOf("/"));

				if (!mWatchInfo.isDownloads) {
					TorrentService.checkLastWatched(mPreferences, TorrentService.LibTorrent.GetTorrentName(torrentFilePath));
					TorrentService.LibTorrent.AddTorrent(savePath, torrentFilePath, StorageMode.ALLOCATE, false);
				}

				mContentFile = TorrentService.LibTorrent.GetTorrentName(torrentFilePath);
				if (TextUtils.isEmpty(mContentFile)) {
					return VideoResult.TORRENT_NOT_ADDED;
				}

				String[] file = TorrentService.setFilePriority(mContentFile, savePath, fileName).split(TorrentService.FILE_INFO_DELIMITER);
				mLocation = file[0];
				long fileSize = Long.parseLong(file[1]);
				if (fileSize == -1) {
					TorrentService.LibTorrent.RemoveTorrent(mContentFile);
					return VideoResult.NO_VIDEO_FILE;
				}

				int state = TorrentService.LibTorrent.GetTorrentState(mContentFile);
				if (TorrentState.PAUSED == state) {
					wasPaused = true;
					TorrentService.LibTorrent.ResumeTorrent(mContentFile);
				} else {
					wasPaused = false;
				}

				if (!prioritizer.load(mContentFile)) {
					TorrentService.LibTorrent.RemoveTorrent(mContentFile);
					return VideoResult.ERROR;
				}
				if (!mWatchInfo.isDownloads) {
					TorrentService.saveLastWatched(mPreferences, mContentFile, mLocation);
				}
			} else if (RELOAD == params[0]) {
				prioritizer.reload();
			} else {
				return VideoResult.ERROR;
			}

			// load video file
			int progressPercentage = 0;
			while (true) {
				prioritizer.checkToStart();

				progressPercentage = prioritizer.getPrepareProgress();
				publishProgress(progressPercentage);

				if (progressPercentage == 100) {
					return VideoResult.SUCCESS;
				}

				if (isCancelled()) {
					return VideoResult.CANCELED;
				}

				try {
					Thread.sleep(1000);
				} catch (InterruptedException ie) {
					return VideoResult.CANCELED;
				}
			}
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			int progress = values[0];

			String peers = "0/0";
			String speed = "0kB/s";
			String info = TorrentService.LibTorrent.GetTorrentStatusText(mContentFile);
			if (info != null) {
				String[] fields = info.split("\n");
				for (int i = 0; i < fields.length; i++) {
					String[] key_value = fields[i].split(":");
					if (key_value.length == 2) {
						String key = key_value[0].toLowerCase().trim();
						String value = key_value[1].trim();
						if ("peers/cand".equals(key) && !"".equals(value)) {
							peers = value;
						} else if ("speed".equals(key) && !"".equals(value)) {
							speed = value;
						}
					}
				}

				mTorrentProgressBar.setProgress(progress);
				mTorrentProgressText.setText(peers + "\t\t\t" + speed + "\t\t\t" + progress + "%");
			}
		}

		@Override
		protected void onPostExecute(VideoResult result) {
			if (VideoResult.SUCCESS == result) {
				isTorrentVideoReady = true;
				mCloseButton.setVisibility(View.INVISIBLE);
				mTorrentProgressBar.setVisibility(View.INVISIBLE);
				mTorrentProgressText.setVisibility(View.INVISIBLE);
				mTitle.setText(mContentFile);

				mVideoPath = mLocation.substring(0, mLocation.lastIndexOf("/"));
				String subPath = mLocation.substring(0, mLocation.lastIndexOf(".") + 1);
				mSubtitlePath = subPath + Subtitles.FORMAT_SRT;
				mVTTSubtitlePath = subPath + Subtitles.FORMAT_VTT;

				// subtitle
				removeSubtitleFiles();
				if (mSubtitles.data != null) {
					initSubtitle();
					downloadSubtitle();
				} else {
					restartSubtitlesLoader();
				}
				popcornPlay(0);
			} else if (VideoResult.TORRENT_NOT_ADDED == result) {
				showErrorDialog("Something is wrong. Torrent not added.");
			} else if (VideoResult.NO_VIDEO_FILE == result) {
				showErrorDialog("Something is wrong. No file in torrent.");
			} else if (VideoResult.ERROR == result) {
				showErrorDialog("Error!!!");
			}
		}

	}

	private class SubtitleLoadTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			if (params[0] == null) {
				removeSubtitleFiles();
				mLibVLC.setSpuTrack(-1);
				if (isCastEnabled) {
					sendSubtitleToChromecast(false);
				}
			} else {
				try {
					Subtitles.load(PlayerBaseActivity.this, params[0], mSubtitlePath);
					mLibVLC.addSubtitleTrack(mSubtitlePath);
					if (isCastEnabled) {
						VTT.convert(mSubtitlePath, mVTTSubtitlePath);
						mChromecast.reloadMovie(mLocation, mContentFile, mVTTSubtitlePath);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return null;
		}

	}

	/*
	 * TODO: Dialogs
	 */

	private void showSubtitlesDialog() {
		if (mSubtitleDialog == null) {
			mSubtitleDialog = new SubtitleDialog();
		}
		if (!mSubtitleDialog.isAdded()) {
			mSubtitleDialog.show(getFragmentManager(), "player_subtitle_dialog");
		}
	}

	private void showErrorDialog(String message) {
		if (mErrorDialog == null) {
			mErrorDialog = new ErrorDialog();
		}
		if (!mErrorDialog.isAdded()) {
			mErrorDialog.setMessage(message);
			mErrorDialog.show(getFragmentManager(), "video_error");
		}
	}

	private void showCastErrorDialog() {
		if (mCastErrorDialog == null) {
			mCastErrorDialog = new CastErrorDialog();
		}
		if (!isFinishing() && !mCastErrorDialog.isAdded()) {
			mCastErrorDialog.show(getFragmentManager(), "cast_error");
		}
	}

	private class SubtitleDialog extends DialogFragment {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(getResources().getString(R.string.subtitles));
			builder.setSingleChoiceItems(mSubtitles.data.toArray(new String[mSubtitles.data.size()]), mSubtitles.position,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							mSubtitles.position = which;
							downloadSubtitle();
							dialog.dismiss();
						}
					});

			Dialog dialog = builder.create();
			dialog.setCanceledOnTouchOutside(true);
			dialog.setOwnerActivity(PlayerBaseActivity.this);
			return dialog;
		}
	}

	private class ErrorDialog extends DialogFragment {

		private String msg;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setCancelable(false);
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(getResources().getString(R.string.app_name));
			builder.setPositiveButton(getResources().getString(R.string.ok), null);
			builder.setMessage(msg);
			AlertDialog dialog = builder.create();
			dialog.show();
			Button update = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
			update.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					getActivity().finish();
				}
			});

			return dialog;
		}

		public void setMessage(String msg) {
			this.msg = msg;
		}
	}

	private class CastErrorDialog extends DialogFragment {

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setCancelable(false);
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(R.string.googlecast_error_title);
			builder.setMessage(R.string.googlecast_error_message);

			builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {

				}
			});

			return builder.create();
		}
	}

	/*
	 * TODO: Other
	 */

	protected boolean isResume() {
		if (mErrorDialog != null && mErrorDialog.isAdded()) {
			return false;
		}

		return true;
	}

	private void sendSubtitleToChromecast(boolean enable) {
		mChromecast.sendSubtitleVtt(enable);
	}

	private ContentObserver mVolumeObserver = new ContentObserver(new Handler()) {
		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			if (isCastEnabled && mChromecast != null) {
				mChromecast.setVolume(mAudioMax, mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
			}
		}
	};

	/*
	 * TODO: Statics
	 */

	public static void watch(Context context, WatchInfo watchInfo) {
		Intent intent = new Intent(context, VLCPlayerActivity.class);
		intent.putExtra(WATCH_INFO_EXTARA_KEY, watchInfo);
		context.startActivity(intent);
	}
}

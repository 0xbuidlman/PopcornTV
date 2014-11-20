package com.ppinera.popcorntv.ui_mobile.base;

import java.io.File;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import dp.ws.popcorntime.R;
import com.ppinera.popcorntv.model.DownloadInfo;
import com.ppinera.popcorntv.torrent.TorrentService;
import com.ppinera.popcorntv.ui_mobile.DownloadsActivity;
import com.ppinera.popcorntv.ui_mobile.locale.LocaleFragment;
import com.ppinera.popcorntv.utils.StorageHelper;

public abstract class VideoBaseFragment extends LocaleFragment {

	protected final String LAST_TORRENT_PREF = "last-torrent";

	private final int HANDLER_DOWNLOAD = 1;
	private final int HANDLER_WATCH_DOWNLOAD = 2;
	private final int HANDLER_WATCH = 3;

	private final int DOWNLOADS_REQUEST_CODE = 101;
	private final int STARS_COUNT = 5;
	private final float MAX_RATING = 10;

	protected final float RATING_COEF = STARS_COUNT / MAX_RATING;
	protected File cacheDirectory;
	protected boolean isDownloads;

	// view
	protected ImageView poster;
	protected TextView title;
	protected TextView description;
	protected Button downloadOpenBtn;
	protected Button watchItNow;

	private NoFreeSpaceDialog noFreeSpaceDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	protected void populateView(View view) {
		poster = (ImageView) view.findViewById(R.id.video_poster);
		title = (TextView) view.findViewById(R.id.video_title);
		description = (TextView) view.findViewById(R.id.video_description);
		downloadOpenBtn = (Button) view.findViewById(R.id.video_download_open);
		watchItNow = (Button) view.findViewById(R.id.video_watchitnow);
		watchItNow.setOnClickListener(watchItNowListener);
	}

	@Override
	public void updateLocaleText() {
		super.updateLocaleText();
		watchItNow.setText(R.string.watch_it_now);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (DOWNLOADS_REQUEST_CODE == requestCode) {
			if (Activity.RESULT_OK == resultCode) {
				checkIsDownloads();
			}
		}
	}

	protected void showDownloadBtn() {
		downloadOpenBtn.setBackgroundResource(R.drawable.download_btn_selector);
		downloadOpenBtn.setText(R.string.download);
		downloadOpenBtn.setOnClickListener(downloadListener);
		downloadOpenBtn.setVisibility(View.VISIBLE);
	}

	protected void showOpenBtn() {
		downloadOpenBtn.setBackgroundResource(R.drawable.open_btn_selector);
		downloadOpenBtn.setText(R.string.open);
		downloadOpenBtn.setOnClickListener(openListener);
		downloadOpenBtn.setVisibility(View.VISIBLE);
	}

	protected boolean checkFreeSpace(String path, long size) {
		long freeSpace = StorageHelper.getAvailableSpaceInBytes(path);
		if (freeSpace <= size) {
			showNoFreeSpaceDialog();
			return false;
		}
		return true;
	}

	protected abstract void checkIsDownloads();

	protected abstract long getFileSize();

	protected abstract String getTorrentUrl();

	protected abstract DownloadInfo createDownloadInfo();

	/*
	 * TODO: Listeners
	 */

	private OnClickListener downloadListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			handler.sendEmptyMessage(HANDLER_DOWNLOAD);
		}
	};

	private OnClickListener openListener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			Intent intent = new Intent(getActivity(), DownloadsActivity.class);
			intent.putExtra(DownloadsActivity.VIDEO_URL, getTorrentUrl());
			startActivityForResult(intent, DOWNLOADS_REQUEST_CODE);
		}
	};

	private OnClickListener watchItNowListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (isDownloads) {
				handler.sendEmptyMessage(HANDLER_WATCH_DOWNLOAD);
			} else {
				handler.sendEmptyMessage(HANDLER_WATCH);
			}
		}
	};

	/*
	 * TODO: Handler
	 */

	private Handler handler = new Handler() {

		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case HANDLER_DOWNLOAD:
				handleDownload();
				break;
			case HANDLER_WATCH_DOWNLOAD:
				handleWatchDownload();
				break;
			case HANDLER_WATCH:
				handleWatch();
				break;
			default:
				break;
			}
		}

	};

	protected boolean handleDownload() {
		File downloadDirectory = StorageHelper.getInstance().getDownloadsDirectory();
		if (null == downloadDirectory) {
			Toast.makeText(getActivity(), R.string.cache_folder_not_selected, Toast.LENGTH_SHORT).show();
			return false;
		} else {
			if (!downloadDirectory.exists()) {
				if (!downloadDirectory.mkdirs()) {
					Log.e("tag", "Cannot crate dir: " + downloadDirectory.getAbsolutePath());
					return false;
				}
			}
		}

		if (!checkFreeSpace(downloadDirectory.getAbsolutePath(), getFileSize())) {
			return false;
		}

		DownloadInfo info = createDownloadInfo();
		String uuid = UUID.randomUUID().toString();
		String directoryPath = downloadDirectory.getAbsolutePath() + "/" + uuid;
		info.directory = new File(directoryPath);
		if (info.directory.exists()) {
			StorageHelper.clearDirectory(info.directory);
		} else {
			if (!info.directory.mkdirs()) {
				Log.e("tag", "Cannot crate dir: " + info.directory.getAbsolutePath());
				return false;
			}
		}
		info.torrentFilePath = directoryPath + "/" + uuid + TorrentService.TORRENT_EXTENSION;

		isDownloads = true;
		showOpenBtn();
		TorrentService.add(getActivity(), info);

		return true;
	}

	protected boolean handleWatchDownload() {
		return true;
	}

	protected boolean handleWatch() {
		cacheDirectory = StorageHelper.getInstance().getCacheDirectory();
		if (null == cacheDirectory) {
			Toast.makeText(getActivity(), R.string.cache_folder_not_selected, Toast.LENGTH_SHORT).show();
			return false;
		}

		if (!cacheDirectory.exists()) {
			if (!cacheDirectory.mkdirs()) {
				Log.e("tag", "Cannot crate dir: " + cacheDirectory.getAbsolutePath());
				return false;
			}
		}

		return true;
	}

	/*
	 * TODO: Dialogs
	 */

	protected void showNoFreeSpaceDialog() {
		if (noFreeSpaceDialog == null) {
			noFreeSpaceDialog = new NoFreeSpaceDialog();
		}
		if (!noFreeSpaceDialog.isAdded()) {
			noFreeSpaceDialog.show(getFragmentManager(), "no_free_space_dialog");
		}
	}

	protected class NoFreeSpaceDialog extends DialogFragment {

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setCancelable(false);
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(R.string.app_name);
			builder.setMessage(R.string.no_free_space);
			builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					StorageHelper.getInstance().clearChacheDirectory();
				}
			});

			AlertDialog dialog = builder.create();
			dialog.show();

			return dialog;
		}
	}
}
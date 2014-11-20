package com.ppinera.popcorntv.ui_mobile;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;

import com.softwarrior.libtorrent.TorrentState;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import com.ppinera.popcorntv.PopcornApplication;
import dp.ws.popcorntime.R;
import dp.ws.popcorntime.database.tables.Downloads;
import com.ppinera.popcorntv.model.DownloadInfo;
import com.ppinera.popcorntv.model.WatchInfo;
import com.ppinera.popcorntv.torrent.Metadata;
import com.ppinera.popcorntv.torrent.TorrentService;
import com.ppinera.popcorntv.ui_mobile.base.VideoBaseFragment;
import com.ppinera.popcorntv.utils.StorageHelper;
import com.ppinera.popcorntv.utils.VideoUtils;

public class VideoCustomFragment extends VideoBaseFragment {

	public static final String SCHEME_KEY = "scheme";
	public static final String PATH_KEY = "path";

	public static final String SCHEME_FILE = "file";

	private Metadata metadata;
	private ArrayList<String> filesData = new ArrayList<String>();
	private ArrayList<Long> sizesData = new ArrayList<Long>();

	private String scheme;
	private String torrentPath;

	private ArrayAdapter<String> mCustomAdapter;
	private int filePosition;
	private String fileName;
	private long fileSize;

	// view
	private Spinner customSpinner;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mCustomAdapter = new ArrayAdapter<String>(getActivity(), R.layout.item_spinner_video);
		mCustomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		scheme = getArguments().getString(SCHEME_KEY);
		torrentPath = getArguments().getString(PATH_KEY);
		if (SCHEME_FILE.equals(scheme)) {
			torrentPath = torrentPath.replace("file://", "");
			parseMetadata(torrentPath);
		} else {
			Log.e("tag", "Unsupported scheme: " + scheme);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.fragment_video_custom, container, false);
		populateView(view);
		return view;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		// change orientation
		ViewGroup container = (ViewGroup) getView();
		container.removeAllViewsInLayout();
		mLocaleHelper.updateLocale();
		View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_video_custom, container);
		populateView(view);
	}

	@Override
	protected void populateView(View view) {
		super.populateView(view);
		customSpinner = (Spinner) view.findViewById(R.id.video_custom);

		if (metadata != null) {
			title.setText("Custom");
			initCustomSpinner();
			replaceCustomData();
		} else {
			title.setText("No torrent metadata");
			customSpinner.setVisibility(View.GONE);
			watchItNow.setVisibility(View.GONE);
		}

		updateLocaleText();
	}

	@Override
	public void updateLocaleText() {
		super.updateLocaleText();
		customSpinner.setPrompt("Files");
	}

	@Override
	protected void checkIsDownloads() {
		String selection = Downloads._FILE_NAME + "=\"" + fileName + "\"";
		Cursor cursor = Downloads.query(getActivity(), null, selection, null, null);
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

	@Override
	protected long getFileSize() {
		return fileSize;
	}

	@Override
	protected String getTorrentUrl() {
		if (SCHEME_FILE.equals(scheme)) {
			return "file://" + torrentPath;
		}
		return null;
	}

	@Override
	protected DownloadInfo createDownloadInfo() {
		DownloadInfo info = new DownloadInfo();
		info.torrentUrl = getTorrentUrl();
		info.fileName = fileName;
		info.title = "Custom";
		info.summary = fileName;
		info.state = TorrentState.DOWNLOADING;
		info.size = fileSize;
		return info;
	}

	private void parseMetadata(String path) {
		try {
			metadata = new Metadata(path);
			filesData.clear();
			sizesData.clear();
			ArrayList<HashMap<String, Object>> filesList = metadata.getFiles();
			if (filesList != null) {
				for (HashMap<String, Object> fileMap : filesList) {
					Metadata.File file = metadata.new File(fileMap);
					for (String filePath : file.path) {
						if (addToFiles(filePath)) {
							sizesData.add(file.length);
						}
					}
				}
			} else {
				if (addToFiles(metadata.getInfoName())) {
					sizesData.add(metadata.getInfoLength());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean addToFiles(String filePath) {
		int index = filePath.lastIndexOf(".");
		if (-1 != index) {
			String ext = filePath.substring(index + 1);
			if (VideoUtils.isVideoExtension(ext)) {
				filesData.add(filePath);
				return true;
			}
		}
		return false;
	}

	private void initCustomSpinner() {
		customSpinner.setAdapter(mCustomAdapter);
		customSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				filePosition = position;
				fileName = filesData.get(position);
				fileSize = sizesData.get(position);
				description.setText(fileName);
				checkIsDownloads();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
	}

	private void replaceCustomData() {
		if (filesData.size() > 0) {
			mCustomAdapter.clear();
			mCustomAdapter.addAll(filesData);
			customSpinner.setVisibility(View.VISIBLE);
			customSpinner.setSelection(filePosition, false);
			watchItNow.setVisibility(View.VISIBLE);
		} else {
			customSpinner.setVisibility(View.GONE);
			watchItNow.setVisibility(View.GONE);
		}
	}

	/*
	 * TODO: Handler
	 */

	@Override
	protected boolean handleWatchDownload() {
		String selection = Downloads._FILE_NAME + "=\"" + fileName + "\"";
		Cursor cursor = Downloads.query(getActivity(), null, selection, null, null);
		if (cursor != null && cursor.getCount() == 1) {
			DownloadInfo info = new DownloadInfo();
			try {
				cursor.moveToFirst();
				info.populate(cursor);
				VLCPlayerActivity.watch(getActivity(), new WatchInfo(info));
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
			SharedPreferences prefs = getActivity().getSharedPreferences(PopcornApplication.POPCORN_PREFERENCES, Activity.MODE_PRIVATE);
			String lastTorrent = prefs.getString(LAST_TORRENT_PREF, "");
			if (!lastTorrent.equals(fileName)) {
				StorageHelper.getInstance().clearChacheDirectory();
				if (checkFreeSpace(cacheDirectory.getAbsolutePath(), fileSize)) {
					prefs.edit().putString(LAST_TORRENT_PREF, fileName).commit();
				} else {
					prefs.edit().putString(LAST_TORRENT_PREF, "").commit();
					return false;
				}
			}
			try {
				String destPath = cacheDirectory.getAbsolutePath() + "/" + TorrentService.WATCH_NOW_TORRENT_FILE_NAME;
				if (SCHEME_FILE.equals(scheme)) {
					FileUtils.copyFile(new java.io.File(torrentPath), new java.io.File(destPath));
				} else {
					return false;
				}
				WatchInfo watchInfo = new WatchInfo();
				watchInfo.torrentFilePath = destPath;
				watchInfo.fileName = fileName;
				VLCPlayerActivity.watch(getActivity(), watchInfo);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			return false;
		}

		return true;
	}
}
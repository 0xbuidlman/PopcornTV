package com.ppinera.popcorntv.torrent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.FileUtils;

import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.softwarrior.libtorrent.LibTorrent;
import com.softwarrior.libtorrent.Priority;
import com.softwarrior.libtorrent.ProxyType;
import com.softwarrior.libtorrent.StorageMode;
import com.softwarrior.libtorrent.TorrentState;

import com.ppinera.popcorntv.PopcornApplication;
import com.ppinera.popcorntv.config.Configuration;
import dp.ws.popcorntime.database.tables.Downloads;
import com.ppinera.popcorntv.model.DownloadInfo;
import com.ppinera.popcorntv.utils.StorageHelper;

public class TorrentService extends IntentService {

	public static final LibTorrent LibTorrent = new LibTorrent();

	public static final String TORRENT_EXTENSION = ".torrent";
	public static final String WATCH_NOW_TORRENT_FILE_NAME = "watchnow" + TORRENT_EXTENSION;
	public static final String FILE_INFO_DELIMITER = "-->";

	public static final String IS_PROXY_ENABLE_KEY = "is-proxy-enable";
	public static final String NAME_OF_ACTION = "torrent_action";
	public static final boolean PROXY_DEFAULT = false;

	private static final int ACTION_INIT = 100;
	private static final int ACTION_ADD = 101;
	private static final int ACTION_REMOVE = 102;
	private static final int ACTION_RESUME = 103;
	private static final int ACTION_PAUSE = 104;
	private static final int ACTION_RESUME_ALL = 105;
	private static final int ACTION_PAUSE_ALL = 106;
	private static final int ACTION_REMOVE_ALL = 107;

	private static final String LAST_CONTENT = "last-content";
	private static final String LAST_FILE = "last-file";

	public TorrentService() {
		super(TorrentService.class.getName());
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent.hasExtra(NAME_OF_ACTION)) {
			int action = intent.getIntExtra(NAME_OF_ACTION, -1);
			switch (action) {
			case ACTION_INIT:
				init();
				break;
			case ACTION_ADD:
				add((DownloadInfo) intent.getParcelableExtra(DownloadInfo.EXTRA_KEY));
				break;
			case ACTION_REMOVE:
				remove((DownloadInfo) intent.getParcelableExtra(DownloadInfo.EXTRA_KEY));
				break;
			case ACTION_RESUME:
				resume((DownloadInfo) intent.getParcelableExtra(DownloadInfo.EXTRA_KEY));
				break;
			case ACTION_PAUSE:
				pause((DownloadInfo) intent.getParcelableExtra(DownloadInfo.EXTRA_KEY));
				break;
			case ACTION_RESUME_ALL:
				resumeAll();
				break;
			case ACTION_PAUSE_ALL:
				pauseAll();
				break;
			case ACTION_REMOVE_ALL:
				removeAll();
				break;
			default:
				break;
			}
		}
	}

	private void init() {
		Cursor cursor = Downloads.query(getApplicationContext(), null, null, null, null);
		if (cursor != null) {
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				DownloadInfo info = new DownloadInfo();
				do {
					try {
						info.populate(cursor);
						if (info.directory.exists() && new File(info.torrentFilePath).exists()) {
							LibTorrent.AddTorrent(info.directory.getAbsolutePath(), info.torrentFilePath, StorageMode.ALLOCATE, false);
							String contentFile = LibTorrent.GetTorrentName(info.torrentFilePath);
							if (TextUtils.isEmpty(contentFile)) {
								Downloads.delete(getApplicationContext(), info.id);
								if (info.directory.exists()) {
									StorageHelper.deleteDirectory(info.directory);
								}
							} else {
								setFilePriority(contentFile, info.directory.getAbsolutePath(), info.fileName);
								if (TorrentState.PAUSED == info.state) {
									LibTorrent.PauseTorrent(contentFile);
								}
							}
						} else {
							Downloads.delete(getApplicationContext(), info.id);
							if (info.directory.exists()) {
								StorageHelper.deleteDirectory(info.directory);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				} while (cursor.moveToNext());
			}
			cursor.close();
		}
	}

	private void add(DownloadInfo info) {
		if (TextUtils.isEmpty(info.torrentUrl)) {
			return;
		}
		Uri uri = Downloads.insert(getApplicationContext(), info);
		String url = info.torrentUrl;
		if (url.startsWith("file://")) {
			try {
				FileUtils.copyFile(new File(url.replace("file://", "")), new File(info.torrentFilePath));
			} catch (IOException e) {
				e.printStackTrace();
				getContentResolver().delete(uri, null, null);
				return;
			}
		} else {
			while (true) {
				try {
					loadTorrentFile(url, info.torrentFilePath);
					break;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		// check if last watched add to downloads
		String content = LibTorrent.GetTorrentName(info.torrentFilePath);
		if (!TextUtils.isEmpty(content)) {
			SharedPreferences prefs = getSharedPreferences(PopcornApplication.POPCORN_PREFERENCES, Activity.MODE_PRIVATE);
			String lastContent = prefs.getString(LAST_CONTENT, "");
			if (content.equals(lastContent)) {
				LibTorrent.RemoveTorrent(content);
				File srcFile = new File(prefs.getString(LAST_FILE, ""));
				if (srcFile.exists()) {
					File destFile = info.directory;
					String path = srcFile.getParent();
					if (!TextUtils.isEmpty(path)) {
						int index = path.lastIndexOf(StorageHelper.CACHE_FOLDER_NAME);
						if (-1 != index) {
							index += StorageHelper.CACHE_FOLDER_NAME.length();
							path = path.substring(index);
							destFile = new File(info.directory.getAbsolutePath() + path);
						}
					}
					try {
						FileUtils.moveFileToDirectory(srcFile, destFile, true);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				StorageHelper.getInstance().clearChacheDirectory();
				saveLastWatched(prefs, "", "");
			}
		}
		LibTorrent.AddTorrent(info.directory.getAbsolutePath(), info.torrentFilePath, StorageMode.ALLOCATE, false);
		String contentFile = LibTorrent.GetTorrentName(info.torrentFilePath);
		if (!TextUtils.isEmpty(contentFile)) {
			setFilePriority(contentFile, info.directory.getAbsolutePath(), info.fileName);
		} else {
			getContentResolver().delete(uri, null, null);
			if (info.directory.exists()) {
				StorageHelper.deleteDirectory(info.directory);
			}
		}
	}

	private void remove(DownloadInfo info) {
		LibTorrent.RemoveTorrent(LibTorrent.GetTorrentName(info.torrentFilePath));
		Downloads.delete(getApplicationContext(), info.id);
		if (info.directory.exists()) {
			StorageHelper.deleteDirectory(info.directory);
		}
	}

	private void resume(DownloadInfo info) {
		LibTorrent.ResumeTorrent(LibTorrent.GetTorrentName(info.torrentFilePath));
		if (TorrentState.PAUSED == info.state) {
			info.state = TorrentState.DOWNLOADING;
			Downloads.update(getApplicationContext(), info);
		}
	}

	private void pause(DownloadInfo info) {
		LibTorrent.PauseTorrent(LibTorrent.GetTorrentName(info.torrentFilePath));
		if (TorrentState.DOWNLOADING == info.state) {
			info.state = TorrentState.PAUSED;
			Downloads.update(getApplicationContext(), info);
		}
	}

	private void resumeAll() {
		String selection = Downloads._STATE + "=" + TorrentState.PAUSED;
		Cursor cursor = Downloads.query(getApplicationContext(), null, selection, null, null);
		if (cursor != null) {
			if (cursor.getCount() > 0) {
				DownloadInfo info = new DownloadInfo();
				cursor.moveToFirst();
				do {
					try {
						info.populate(cursor);
						resume(info);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} while (cursor.moveToNext());
			}
			cursor.close();
		}
	}

	private void pauseAll() {
		String selection = Downloads._STATE + "=" + TorrentState.DOWNLOADING;
		Cursor cursor = Downloads.query(getApplicationContext(), null, selection, null, null);
		if (cursor != null) {
			if (cursor.getCount() > 0) {
				DownloadInfo info = new DownloadInfo();
				cursor.moveToFirst();
				do {
					try {
						info.populate(cursor);
						pause(info);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} while (cursor.moveToNext());
			}
			cursor.close();
		}
	}

	private void removeAll() {
		Cursor cursor = Downloads.query(getApplicationContext(), null, null, null, null);
		if (cursor != null) {
			if (cursor.getCount() > 0) {
				DownloadInfo info = new DownloadInfo();
				cursor.moveToFirst();
				do {
					try {
						info.populate(cursor);
						LibTorrent.RemoveTorrent(LibTorrent.GetTorrentName(info.torrentFilePath));
					} catch (Exception e) {
						e.printStackTrace();
					}
				} while (cursor.moveToNext());
				getContentResolver().delete(Downloads.CONTENT_URI, null, null);
				StorageHelper.getInstance().clearDownloadsDirectory();
			}
			cursor.close();
		}
	}

	/*
	 * TODO: Static methods
	 */

	public static void start(Context context) {
		LibTorrent.SetSession(54321, 0, 0, true);
		LibTorrent.SetSessionOptions(false, true, false);
		SharedPreferences prefs = context.getSharedPreferences(PopcornApplication.POPCORN_PREFERENCES, Activity.MODE_PRIVATE);
		setProxy(prefs.getBoolean(IS_PROXY_ENABLE_KEY, PROXY_DEFAULT));
		LibTorrent.ResumeSession();

		Intent intent = new Intent(context, TorrentService.class);
		intent.putExtra(NAME_OF_ACTION, ACTION_INIT);
		context.startService(intent);
	}

	public static void stop() {
		LibTorrent.PauseSession();
		LibTorrent.AbortSession();
	}

	public static void setProxy(boolean enabled) {
		if (enabled) {
			LibTorrent.SetProxy(ProxyType.SOCKS_5_PW, Configuration.VPN.host, Configuration.VPN.port, Configuration.VPN.user, Configuration.VPN.pass);
		} else {
			LibTorrent.SetProxy(ProxyType.NONE, "", 0, "", "");
		}
	}

	public static void add(Context context, DownloadInfo info) {
		Intent intent = new Intent(context, TorrentService.class);
		intent.putExtra(NAME_OF_ACTION, ACTION_ADD);
		intent.putExtra(DownloadInfo.EXTRA_KEY, info);
		context.startService(intent);
	}

	public static void remove(Context context, DownloadInfo info) {
		Intent intent = new Intent(context, TorrentService.class);
		intent.putExtra(NAME_OF_ACTION, ACTION_REMOVE);
		intent.putExtra(DownloadInfo.EXTRA_KEY, info);
		context.startService(intent);
	}

	public static void resume(Context context, DownloadInfo info) {
		Intent intent = new Intent(context, TorrentService.class);
		intent.putExtra(NAME_OF_ACTION, ACTION_RESUME);
		intent.putExtra(DownloadInfo.EXTRA_KEY, info);
		context.startService(intent);
	}

	public static void pause(Context context, DownloadInfo info) {
		Intent intent = new Intent(context, TorrentService.class);
		intent.putExtra(NAME_OF_ACTION, ACTION_PAUSE);
		intent.putExtra(DownloadInfo.EXTRA_KEY, info);
		context.startService(intent);
	}

	public static void resumeAll(Context context) {
		Intent intent = new Intent(context, TorrentService.class);
		intent.putExtra(NAME_OF_ACTION, ACTION_RESUME_ALL);
		context.startService(intent);
	}

	public static void pauseAll(Context context) {
		Intent intent = new Intent(context, TorrentService.class);
		intent.putExtra(NAME_OF_ACTION, ACTION_PAUSE_ALL);
		context.startService(intent);
	}

	public static void removeAll(Context context) {
		Intent intent = new Intent(context, TorrentService.class);
		intent.putExtra(NAME_OF_ACTION, ACTION_REMOVE_ALL);
		context.startService(intent);
	}

	public static void loadTorrentFile(String url, String savePath) throws Exception {
		URLConnection connectionTorrent = new URL(url).openConnection();
		connectionTorrent.connect();

		InputStream is = connectionTorrent.getInputStream();
		OutputStream os = new FileOutputStream(savePath);

		int read;
		byte[] buffer = new byte[1024];
		while ((read = is.read(buffer)) != -1) {
			os.write(buffer, 0, read);
		}
		os.flush();
		is.close();
		os.close();
	}

	public static void checkLastWatched(SharedPreferences prefs, String content) {
		String lastContent = prefs.getString(LAST_CONTENT, "");
		if (!"".equals(lastContent)) {
			if (lastContent.equals(content)) {
				String lastFile = prefs.getString(LAST_FILE, "");
				if (!"".equals(lastFile)) {
					File f = new File(lastFile);
					if (!f.exists()) {
						LibTorrent.RemoveTorrent(lastContent);
						saveLastWatched(prefs, "", "");
					}
				}
			} else {
				LibTorrent.RemoveTorrent(lastContent);
				saveLastWatched(prefs, "", "");
			}
		}
	}

	public static void saveLastWatched(SharedPreferences prefs, String lastContent, String lastFile) {
		prefs.edit().putString(LAST_CONTENT, lastContent).putString(LAST_FILE, lastFile).commit();
	}

	public static void removeLastOnExit(SharedPreferences prefs) {
		String lastContent = prefs.getString(LAST_CONTENT, "");
		if (!"".equals(lastContent)) {
			LibTorrent.RemoveTorrent(lastContent);
		}
		saveLastWatched(prefs, "", "");
	}

	public static String setFilePriority(String contentFile, String savePath, String fileName) {
		String location = "";
		long size = -1;

		if (TextUtils.isEmpty(contentFile)) {
			return location + FILE_INFO_DELIMITER + size;
		}

		String torrentFiles = TorrentService.LibTorrent.GetTorrentFiles(contentFile);
		if (TextUtils.isEmpty(torrentFiles)) {
			return location + FILE_INFO_DELIMITER + size;
		}

		String[] files = torrentFiles.split("\\n");
		long[] sizes = new long[files.length];
		byte[] priorities = TorrentService.LibTorrent.GetTorrentFilesPriority(contentFile);

		for (int i = 0; i < files.length; i++) {
			String[] file = files[i].split(FILE_INFO_DELIMITER);
			files[i] = file[0];
			sizes[i] = Long.parseLong(file[1]);
		}

		if (!TextUtils.isEmpty(fileName)) {
			for (int i = 0; i < files.length; i++) {
				int index = files[i].lastIndexOf("/") + 1;
				String currentFileName = files[i].substring(index);
				if (currentFileName.equals(fileName)) {
					priorities[i] = Priority.NORMAL;
					size = sizes[i];
					location = savePath + "/" + files[i];
				} else {
					priorities[i] = Priority.DONT_DOWNLOAD;
				}
			}
		}

		if ("".equals(location) && sizes.length > 0) {
			int biggestFileIndex = 0;
			for (int i = 0; i < sizes.length; i++) {
				if (sizes[i] > size) {
					priorities[biggestFileIndex] = Priority.DONT_DOWNLOAD;
					size = sizes[i];
					biggestFileIndex = i;
					priorities[biggestFileIndex] = Priority.NORMAL;
				} else {
					priorities[i] = Priority.DONT_DOWNLOAD;
				}
			}
			location = savePath + "/" + files[biggestFileIndex];
		}

		TorrentService.LibTorrent.SetTorrentFilesPriority(priorities, contentFile);

		return location + FILE_INFO_DELIMITER + size;
	}

}
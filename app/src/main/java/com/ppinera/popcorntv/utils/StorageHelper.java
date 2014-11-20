package com.ppinera.popcorntv.utils;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.ppinera.popcorntv.PopcornApplication;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;

public class StorageHelper {

	private static final StorageHelper INSTANCE = new StorageHelper();

	public static final String ROOT_FOLDER_NAME = "time4popcorn";
	public static final String CACHE_FOLDER_NAME = "cache";
	public static final String DOWNLOADS_FOLDER_NAME = "downloads";

	public final static long SIZE_KB = 1024L;
	public final static long SIZE_MB = SIZE_KB * SIZE_KB;
	public final static long SIZE_GB = SIZE_KB * SIZE_KB * SIZE_KB;

	private final String ROOT_FOLDER_PREF_KEY = "chache-folder-path";

	private File rootDirectory = null;

	private StorageHelper() {

	}

	public static StorageHelper getInstance() {
		return INSTANCE;
	}

	/*
	 * Static methods
	 */

	public static void deleteDirectory(File parent) {
		if (parent != null && parent.isDirectory()) {
			try {
				FileUtils.deleteDirectory(parent);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void clearDirectory(File parent) {
		if (parent != null && parent.isDirectory()) {
			try {
				FileUtils.cleanDirectory(parent);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static String getDownloadFolderPath() {
		File file = null;
		if (Environment.getExternalStorageState() != null) {
			file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		} else {
			file = new File(Environment.getDataDirectory() + "/Download/");
		}

		if (!file.exists()) {
			file.mkdir();
		}

		return file.getAbsolutePath();
	}

	public static void deleteRecursive(File path, ExtGenericFilter filter) {
		if (path.exists() && path.isDirectory()) {
			if (filter != null) {
				for (File f : path.listFiles(filter)) {
					deleteRecursive(f, null);
				}
			} else {
				for (File f : path.listFiles()) {
					deleteRecursive(f, null);
				}
			}
		}
		path.delete();
	}

	public static long getAvailableSpaceInBytes(String path) {
		long availableSpace = -1L;
		try {
			StatFs stat = new StatFs(path);
			availableSpace = stat.getAvailableBytes();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return availableSpace;
	}

	public static long getAvailableSpaceInKB(String path) {
		return getAvailableSpaceInBytes(path) / SIZE_KB;
	}

	public static long getAvailableSpaceInMB(String path) {
		return getAvailableSpaceInBytes(path) / SIZE_MB;
	}

	public static long getAvailableSpaceInGB(String path) {
		return getAvailableSpaceInBytes(path) / SIZE_GB;
	}

	public static String getSizeText(long size) {
		String text;
		if (size >= SIZE_GB) {
			text = String.format("%.2f", ((float) size / SIZE_GB)) + " GB";
		} else if (size >= SIZE_MB) {
			text = String.format("%.2f", ((float) size / SIZE_MB)) + " MB";
		} else if (size >= SIZE_KB) {
			text = String.format("%.2f", ((float) size / SIZE_KB)) + " KB";
		} else {
			text = size + " B";
		}
		text = text.replace(',', '.');
		return text;
	}

	/*
	 * Methods
	 */

	public void init(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(PopcornApplication.POPCORN_PREFERENCES, Activity.MODE_PRIVATE);
		String path = preferences.getString(ROOT_FOLDER_PREF_KEY, "");
		if ("".equals(path)) {
			setRootDirectory(context, getDefaultRootDirectoryPath(context));
			clearRootDirectory();
		} else {
			rootDirectory = new File(path);
			if (!rootDirectory.exists()) {
				if (!rootDirectory.mkdirs()) {
					rootDirectory = null;
					preferences.edit().remove(ROOT_FOLDER_PREF_KEY).commit();
					setRootDirectory(context, getDefaultRootDirectoryPath(context));
				}
			}
		}
	}

	/*
	 * Root
	 */

	public void setRootDirectory(Context context, String path) {
		if (!TextUtils.isEmpty(path)) {
			setRootDirectory(context, new File(path));
		}
	}

	public void setRootDirectory(Context context, File directory) {
		if (null != directory && rootDirectory != directory) {
			if (rootDirectory != null && rootDirectory.exists()) {
				try {
					FileUtils.deleteDirectory(rootDirectory);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			rootDirectory = directory;
			if (!rootDirectory.exists()) {
				rootDirectory.mkdirs();
			}

			SharedPreferences preferences = context.getSharedPreferences(PopcornApplication.POPCORN_PREFERENCES, Activity.MODE_PRIVATE);
			preferences.edit().putString(ROOT_FOLDER_PREF_KEY, rootDirectory.getAbsolutePath()).commit();
		}
	}

	public File getRootDirectory() {
		return rootDirectory;
	}

	public String getRootDirectoryPath() {
		if (null == rootDirectory) {
			return null;
		}
		return rootDirectory.getAbsolutePath();
	}

	public void clearRootDirectory() {
		if (null != rootDirectory) {
			clearDirectory(rootDirectory);
		}
	}

	/*
	 * Cache
	 */

	public File getCacheDirectory() {
		if (null == rootDirectory) {
			return null;
		}
		return new File(rootDirectory.getAbsolutePath() + "/" + CACHE_FOLDER_NAME);
	}

	public String getCacheDirectoryPath() {
		if (null == rootDirectory) {
			return null;
		}
		return rootDirectory.getAbsolutePath() + "/" + CACHE_FOLDER_NAME;
	}

	public void clearChacheDirectory() {
		if (null != rootDirectory) {
			clearDirectory(getCacheDirectory());
		}
	}

	/*
	 * Downloads
	 */

	public File getDownloadsDirectory() {
		if (null == rootDirectory) {
			return null;
		}
		return new File(rootDirectory.getAbsolutePath() + "/" + DOWNLOADS_FOLDER_NAME);
	}

	public String getDownloadsDirectoryPath() {
		if (null == rootDirectory) {
			return null;
		}
		return rootDirectory.getAbsolutePath() + "/" + DOWNLOADS_FOLDER_NAME;
	}

	public void clearDownloadsDirectory() {
		if (null != rootDirectory) {
			clearDirectory(getDownloadsDirectory());
		}
	}

	/*
	 * Other
	 */

	private String getDefaultRootDirectoryPath(Context context) {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + ROOT_FOLDER_NAME;
		} else {
			File cache = context.getExternalFilesDir(null);
			if (cache != null) {
				return cache.getAbsolutePath() + "/" + ROOT_FOLDER_NAME;
			}
		}

		return null;
	}
}
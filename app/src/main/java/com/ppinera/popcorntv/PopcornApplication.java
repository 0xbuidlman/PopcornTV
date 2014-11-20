package com.ppinera.popcorntv;

import java.util.Locale;
import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.ppinera.popcorntv.subtitles.Subtitles;
import com.ppinera.popcorntv.torrent.TorrentService;
import com.ppinera.popcorntv.utils.LanguageUtil;
import com.ppinera.popcorntv.utils.StorageHelper;

@ReportsCrashes(formKey = "", mode = ReportingInteractionMode.SILENT, mailTo = "pepibumur@gmail.com")
public class PopcornApplication extends Application {

	public static final String LOG_TAG = "tag";
	public static final String POPCORN_PREFERENCES = "PopcornPreferences";
	public static final String CLEAR_ON_EXIT = "clear-on-exit";
	private final String APP_LOCALE = "app-locale";
	private SharedPreferences mPrefs;
	private Locale mLocale;

	@Override
	public void onCreate() {
		super.onCreate();
		ACRA.init(PopcornApplication.this);
		mPrefs = getSharedPreferences(PopcornApplication.POPCORN_PREFERENCES, Activity.MODE_PRIVATE);
		initSubtitleLanguage();
		initLocale();
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext()).build();
		ImageLoader.getInstance().init(config);
		StorageHelper.getInstance().init(PopcornApplication.this);
	}

	public Locale getAppLocale() {
		return mLocale;
	}

	public void changeLanguage(String lang) {
		if (mLocale.getLanguage().equals(lang)) {
			return;
		}

		mLocale = new Locale(lang);
		mPrefs.edit().putString(APP_LOCALE, lang).commit();
	}

	public void setSubtitleLanguage(String lang) {
		mPrefs.edit().putString(Subtitles.LANGUAGE, lang).commit();
	}

	public String getSubtitleLanguage() {
		return mPrefs.getString(Subtitles.LANGUAGE, "");
	}

	public void clearOnExit() {
		if (mPrefs.getBoolean(CLEAR_ON_EXIT, true)) {
			TorrentService.removeLastOnExit(mPrefs);
			StorageHelper.getInstance().clearChacheDirectory();
		}
	}

	private void initLocale() {
		String lang = LanguageUtil.getInterfaceSupportedIso(Locale.getDefault().getLanguage());
		if (mPrefs.contains(APP_LOCALE)) {
			lang = mPrefs.getString(APP_LOCALE, lang);
		} else {
			mPrefs.edit().putString(APP_LOCALE, lang).commit();
		}
		mLocale = new Locale(lang);
	}

	private void initSubtitleLanguage() {
		if (!mPrefs.contains(Subtitles.LANGUAGE)) {
			setSubtitleLanguage(LanguageUtil.isoToLanguage(Locale.getDefault().getLanguage()));
		}
	}
}
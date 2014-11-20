package com.ppinera.popcorntv.ui_mobile.locale;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import com.ppinera.popcorntv.PopcornApplication;

public abstract class LocaleFragmentActivity extends FragmentActivity implements LocaleListener {

	protected LocaleHelper mLocaleHelper;

	@Override
	protected void onCreate(Bundle arg0) {
		mLocaleHelper = new LocaleHelper((PopcornApplication) getApplication(), LocaleFragmentActivity.this);
		super.onCreate(arg0);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mLocaleHelper.checkLanguage();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mLocaleHelper.updateLocale();
	}

	@Override
	public void updateLocaleText() {

	}
}
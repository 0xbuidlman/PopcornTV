package com.ppinera.popcorntv.ui_mobile.locale;

import com.ppinera.popcorntv.PopcornApplication;
import android.app.Fragment;
import android.os.Bundle;

public class LocaleFragment extends Fragment implements LocaleListener {

	protected LocaleHelper mLocaleHelper;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		mLocaleHelper = new LocaleHelper((PopcornApplication) getActivity().getApplication(), LocaleFragment.this);
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		mLocaleHelper.checkLanguage();
	}

	@Override
	public void updateLocaleText() {

	}
}
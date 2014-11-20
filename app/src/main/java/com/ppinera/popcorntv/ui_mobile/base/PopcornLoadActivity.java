package com.ppinera.popcorntv.ui_mobile.base;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;

public abstract class PopcornLoadActivity extends PopcornBaseActivity implements ContentLoadListener {

	private int mContentViewID = -1;

	private LoadingFragment loadingFragment = new LoadingFragment();
	private NoConnectionFragment noConnectionFragment = new NoConnectionFragment();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		noConnectionFragment.setLoadListener(PopcornLoadActivity.this);
	}

	@Override
	public void updateLocaleText() {
		super.updateLocaleText();
		if (noConnectionFragment.isAdded()) {
			noConnectionFragment.updateLocaleText();
		}
	}

	@Override
	public void showLoading() {
		replaceFragment(loadingFragment);
	}

	@Override
	public void showError() {
		replaceFragment(noConnectionFragment);
	}

	@Override
	public void showContent() {

	}

	@Override
	public void retryLoad() {

	}

	public void setPopcornContentViewId(int id) {
		this.mContentViewID = id;
	}

	protected void replaceFragment(Fragment fragment) {
		if (mContentViewID != -1) {
			FragmentTransaction tr = getFragmentManager().beginTransaction();
			tr.replace(mContentViewID, fragment);
			tr.commitAllowingStateLoss();
		}
	}
}
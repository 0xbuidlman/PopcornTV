package com.ppinera.popcorntv.ui_mobile.base;

public interface ContentLoadListener {
	public void showLoading();

	public void showError();

	public void showContent();

	public void retryLoad();
}
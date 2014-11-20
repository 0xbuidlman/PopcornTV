package com.ppinera.popcorntv.ui_mobile;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import dp.ws.popcorntime.R;
import com.ppinera.popcorntv.utils.FullscreenableChromeClient;

public class TrailerActivity extends Activity {

	public static final String TRAILER_URL_KEY = "popcorn_trailer";

	private WebView trailer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_trailer);

		String url = getIntent().getStringExtra(TRAILER_URL_KEY);
		trailer = (WebView) findViewById(R.id.trailer_view);
		trailer.setWebChromeClient(new FullscreenableChromeClient(this));
		trailer.getSettings().setJavaScriptEnabled(true);
		trailer.getSettings().setDomStorageEnabled(true);
		trailer.loadUrl(url);
	}

	@Override
	protected void onDestroy() {
		trailer.loadUrl("about:blank");
		super.onDestroy();
	}
}
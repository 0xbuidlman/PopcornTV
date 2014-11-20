package com.ppinera.popcorntv.ui_mobile.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import dp.ws.popcorntime.R;
import com.ppinera.popcorntv.ui_mobile.locale.LocaleFragment;

public class NoConnectionFragment extends LocaleFragment implements OnClickListener {

	private TextView noConnection;
	private Button retry;
	private ContentLoadListener mLoadListener;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_no_connection, container, false);

		noConnection = (TextView) view.findViewById(R.id.no_connection_label);
		retry = (Button) view.findViewById(R.id.connection_retry_btn);
		retry.setOnClickListener(this);

		updateLocaleText();

		return view;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.connection_retry_btn:
			if (mLoadListener != null) {
				mLoadListener.retryLoad();
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void updateLocaleText() {
		super.updateLocaleText();
		noConnection.setText(R.string.no_connection);
		retry.setText(R.string.retry);
	}

	public void setLoadListener(ContentLoadListener listener) {
		mLoadListener = listener;
	}
}
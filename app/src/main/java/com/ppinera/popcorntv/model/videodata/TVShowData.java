package com.ppinera.popcorntv.model.videodata;

import android.content.Context;
import dp.ws.popcorntime.R;

public class TVShowData extends VideoData {

	// private String exclude = Quality.P_1080;

	public TVShowData(Context context) {
		type = Type.TV_SHOWS;
		sort = Sort.SEEDS;
		format = Format.MP4;// + "," + Format.AVI + "," + Format.MKV;
		requestGenres = context.getResources().getStringArray(R.array.request_genres);
	}

	@Override
	public String getRequestURl() {
		super.getRequestURl();

		// sb.append("&exclude=" + exclude);

		return sb.toString();
	}
}
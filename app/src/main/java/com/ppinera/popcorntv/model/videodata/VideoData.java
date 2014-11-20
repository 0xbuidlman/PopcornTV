package com.ppinera.popcorntv.model.videodata;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import dp.ws.popcorntime.R;
import android.content.Context;

public abstract class VideoData {

	public static final String REQUEST_URL_KEY = "popcorntime_video_request";
	public static final String TYPE_KEY = "popcorntime_video_type";

	protected final String TORRENTAPI_URL = "http://api.torrentsapi.com/";

	protected StringBuilder sb = new StringBuilder();

	protected String type;
	protected String sort;
	protected String format;
	protected int page = 1;
	protected String[] requestGenres;
	protected int currentGenrePosition = 0;
	protected String keywords;

	private String[] localeGenres;

	public String getType() {
		return type;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getPage() {
		return page;
	}

	public void setGenre(int position) {
		if (requestGenres != null && (position >= 0 || position < requestGenres.length)) {
			currentGenrePosition = position;
		}
	}

	public void setLocaleGenres(Context context) {
		localeGenres = context.getResources().getStringArray(R.array.genres);
	}

	public String[] getLocaleGenres() {
		return localeGenres;
	}

	public int getGenrePosition() {
		return currentGenrePosition;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public String getKeywords() {
		return keywords;
	}

	public String getRequestURl() {
		sb.setLength(0);
		sb.append(TORRENTAPI_URL);
		sb.append(type);
		sb.append("?sort=" + sort);
		sb.append("&format=" + format);
		sb.append("&page=" + page);
		if (keywords != null && keywords.length() > 0) {
			try {
				String kw = URLEncoder.encode(keywords, "UTF-8");
				sb.append("&keywords=" + kw);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		} else {
			if (0 != currentGenrePosition) {
				sb.append("&genre=" + requestGenres[currentGenrePosition]);
			}
		}

		return sb.toString();
	}

	public class Type {
		public static final String MOVIES = "list";
		public static final String TV_SHOWS = "shows";
	}

	public class Sort {
		public static final String SEEDS = "seeds";
	}

	public class Format {
		public static final String MP4 = "mp4";
		public static final String AVI = "avi";
		public static final String MKV = "mkv";
	}

	public class Quality {
		public static final String P_720 = "720p";
		public static final String P_1080 = "1080p";
	}
}
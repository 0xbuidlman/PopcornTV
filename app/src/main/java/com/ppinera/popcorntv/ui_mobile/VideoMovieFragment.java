package com.ppinera.popcorntv.ui_mobile;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import dp.ws.popcorntime.R;
import com.ppinera.popcorntv.controller.DownloadsAdapter;
import com.ppinera.popcorntv.model.videoinfo.movie.MovieInfo;
import com.ppinera.popcorntv.subtitles.Subtitles;
import com.ppinera.popcorntv.ui_mobile.base.VideoTypeFragment;
import com.ppinera.popcorntv.utils.StorageHelper;

public class VideoMovieFragment extends VideoTypeFragment {

	private final String SUBTITLE_URL = "http://api.yifysubtitles.com/subs/";

	private MovieInfo movieInfo;

	private TextView actors;
	private ImageButton trailer;
	private TextView trailerText;

	private String additionalDescription = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		movieInfo = mActivity.getIntent().getExtras().getParcelable(VideoActivity.VIDEO_INFO_KEY);
		parseInfoResponse(getArguments().getString(RESPONSE_JSON_KEY));
		mSubtitles = new Subtitles(getActivity());
		mSubtitles.setSubtitleListener(VideoMovieFragment.this);
		checkIsFavorites(movieInfo);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.fragment_video_movie, container, false);
		populateView(view);
		mSubtitles.restartLoader(getSubtitlesDataUrl(), VideoMovieFragment.this);
		return view;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		// change orientation
		ViewGroup container = (ViewGroup) getView();
		container.removeAllViewsInLayout();
		mLocaleHelper.updateLocale();
		View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_video_movie, container);
		populateView(view);
	}

	@Override
	public void updateLocaleText() {
		super.updateLocaleText();
		actors.setText(getString(R.string.actors) + ":\n" + Html.fromHtml(movieInfo.actors));
		trailerText.setText(R.string.trailer);
		replaceTorrentData(movieInfo.torrents);
	}

	@Override
	protected void populateView(View view) {
		super.populateView(view);
		description.setText(Html.fromHtml(movieInfo.description + additionalDescription));
		actors = (TextView) view.findViewById(R.id.video_movie_actors);
		trailer = (ImageButton) view.findViewById(R.id.video_trailer);
		trailer.setOnClickListener(trailerListener);
		trailer.setOnTouchListener(trailerTouchListener);
		trailerText = (TextView) view.findViewById(R.id.video_trailer_text);
		trailerText.setOnClickListener(trailerListener);
		trailerText.setOnTouchListener(trailerTouchListener);

		updateLocaleText();
	}

	private void parseInfoResponse(String json) {
		try {
			JSONObject info = new JSONObject(json);
			additionalDescription = " <b>" + info.getString("Country") + ". " + info.getString("Runtime") + ". " + info.getString("Year") + "</b>";
			if (TextUtils.isEmpty(movieInfo.actors)) {
				movieInfo.actors = info.getString("Actors");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onSubtitleLoadSucces(String data) {
		try {
			JSONObject subs = new JSONObject(data);
			mSubtitles.parseMovies(subs, movieInfo.imdb);
			super.onSubtitleLoadSucces(data);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void updateTorrentInfo(int position) {
		if (movieInfo.torrents.size() > position) {
			torrentInfo = movieInfo.torrents.get(position);
		} else {
			torrentInfo = null;
		}
		checkIsDownloads();
	}

	@Override
	protected String getDownloadSummary() {
		return DownloadsAdapter.SIZE_KEY + " " + StorageHelper.getSizeText(torrentInfo.size);
	}

	@Override
	protected String getSubtitlesDataUrl() {
		return SUBTITLE_URL + movieInfo.imdb;
	}

	private OnClickListener trailerListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(getActivity(), TrailerActivity.class);
			intent.putExtra(TrailerActivity.TRAILER_URL_KEY, movieInfo.trailer);
			startActivity(intent);
		}
	};

	private OnTouchListener trailerTouchListener = new OnTouchListener() {

		@SuppressLint("ClickableViewAccessibility")
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			int action = event.getAction();
			if (MotionEvent.ACTION_DOWN == action) {
				trailerTouch(v.getId(), true);
			} else if (MotionEvent.ACTION_UP == action) {
				trailerTouch(v.getId(), false);
			}

			return false;
		}
	};

	private void trailerTouch(int id, boolean isPressed) {
		if (R.id.video_trailer == id) {
			trailerText.setPressed(isPressed);
		} else if (R.id.video_trailer_text == id) {
			trailer.setPressed(isPressed);
		}
	}
}
package com.ppinera.popcorntv.ui_mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import dp.ws.popcorntime.R;
import com.ppinera.popcorntv.controller.DownloadsAdapter;
import com.ppinera.popcorntv.controller.EpisodeAdapter;
import com.ppinera.popcorntv.controller.SeasonAdapter;
import com.ppinera.popcorntv.model.videoinfo.Torrent;
import com.ppinera.popcorntv.model.videoinfo.tvshow.Episode;
import com.ppinera.popcorntv.model.videoinfo.tvshow.Season;
import com.ppinera.popcorntv.model.videoinfo.tvshow.TVShowInfo;
import com.ppinera.popcorntv.subtitles.Subtitles;
import com.ppinera.popcorntv.ui_mobile.base.VideoTypeFragment;
import com.ppinera.popcorntv.utils.StorageHelper;

public class VideoTVShowFragment extends VideoTypeFragment {

	private final String SUBTITLE_URL = "http://sub.torrentsapi.com/list?imdb=";

	private TVShowInfo tvshowInfo;

	private ListView seasonsView;
	private ListView episodesView;
	private TextView episodeTitle;
	private TextView episodeDescription;
	private TextView dataNotFound;

	private SeasonAdapter mSeasonAdapter;
	private EpisodeAdapter mEpisodeAdapter;

	private List<String> namesOfSeasons = new ArrayList<String>();

	private Season currentSeason = null;
	private Episode currentEpisode = null;
	private boolean changeOrientation = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		tvshowInfo = mActivity.getIntent().getExtras().getParcelable(VideoActivity.VIDEO_INFO_KEY);
		parseInfoResponse(getArguments().getString(RESPONSE_JSON_KEY));
		mSubtitles = new Subtitles(getActivity());
		mSubtitles.setSubtitleListener(VideoTVShowFragment.this);
		mSeasonAdapter = new SeasonAdapter(getActivity(), namesOfSeasons);
		mEpisodeAdapter = new EpisodeAdapter(getActivity());
		checkIsFavorites(tvshowInfo);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		View view = inflater.inflate(R.layout.fragment_video_tvshow, container, false);
		populateView(view);
		return view;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		// change orientation
		changeOrientation = true;
		ViewGroup container = (ViewGroup) getView();
		container.removeAllViewsInLayout();
		mLocaleHelper.updateLocale();
		View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_video_tvshow, container);
		populateView(view);
	}

	@Override
	public void updateLocaleText() {
		super.updateLocaleText();

		if (namesOfSeasons.size() > 0) {
			mSeasonAdapter.notifyDataSetInvalidated();
			mEpisodeAdapter.notifyDataSetInvalidated();
			replaceTorrentData(currentEpisode.torrents);
		} else {
			dataNotFound.setText(R.string.data_not_available);
		}
	}

	@Override
	protected void populateView(View view) {
		super.populateView(view);
		description.setText(Html.fromHtml(tvshowInfo.description));
		dataNotFound = (TextView) view.findViewById(R.id.video_data_not_found);

		if (namesOfSeasons.size() > 0) {
			dataNotFound.setVisibility(View.GONE);
			view.findViewById(R.id.video_data_view).setVisibility(View.VISIBLE);

			seasonsView = (ListView) view.findViewById(R.id.video_seasons);
			episodesView = (ListView) view.findViewById(R.id.video_episodes);
			episodeTitle = (TextView) view.findViewById(R.id.video_episode_title);
			episodeDescription = (TextView) view.findViewById(R.id.video_episode_description);
			episodeDescription.setMovementMethod(new ScrollingMovementMethod());

			initSeasonList();
			initEpisodeList();

			seasonsView.performItemClick(mSeasonAdapter.getView(tvshowInfo.seasonPosition, null, null), tvshowInfo.seasonPosition,
					mSeasonAdapter.getItemId(tvshowInfo.seasonPosition));
		} else {
			dataNotFound.setVisibility(View.VISIBLE);
			view.findViewById(R.id.video_data_view).setVisibility(View.GONE);
		}

		updateLocaleText();
	}

	private void parseInfoResponse(String json) {
		try {
			JSONObject jsonInfo = new JSONObject(json);
			List<String> seasonKeys = new ArrayList<String>();
			Iterator<?> iter = jsonInfo.keys();
			while (iter.hasNext()) {
				seasonKeys.add((String) iter.next());
			}
			Collections.sort(seasonKeys, seasonComporator);

			for (String seasonKey : seasonKeys) {
				JSONArray jsonSeasons = jsonInfo.getJSONArray(seasonKey);
				Season season = new Season();
				for (int i = 0; i < jsonSeasons.length(); i++) {
					JSONObject jsonEpisode = jsonSeasons.getJSONObject(i);
					Episode episode = new Episode();
					episode.title = jsonEpisode.getString("title");
					episode.description = jsonEpisode.getString("synopsis") + " <b>" + jsonEpisode.getString("run_time") + "</b>";
					if (!jsonEpisode.isNull("items")) {
						JSONArray jsonTorrents = jsonEpisode.getJSONArray("items");
						for (int k = 0; k < jsonTorrents.length(); k++) {
							JSONObject jsonTorrent = jsonTorrents.getJSONObject(k);
							Torrent torrent = new Torrent();
							torrent.url = jsonTorrent.getString("torrent_url");
							torrent.seeds = jsonTorrent.getInt("torrent_seeds");
							torrent.peers = jsonTorrent.getInt("torrent_peers");
							torrent.file = jsonTorrent.getString("file");
							torrent.quality = jsonTorrent.getString("quality");
							torrent.size = jsonTorrent.getLong("size_bytes");
							episode.torrents.add(torrent);
						}
					}
					season.namesOfEpisodes.add(Integer.toString(i + 1));
					season.episodes.add(episode);
				}
				namesOfSeasons.add(seasonKey);
				tvshowInfo.seasons.add(season);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onSubtitleLoadSucces(String data) {
		try {
			JSONObject subs = new JSONObject(data);
			mSubtitles.parseTVShows(subs);
			super.onSubtitleLoadSucces(data);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void updateTorrentInfo(int position) {
		if (currentEpisode.torrents.size() > position) {
			torrentInfo = currentEpisode.torrents.get(position);
		} else {
			torrentInfo = null;
		}
		checkIsDownloads();
	}

	@Override
	protected String getDownloadSummary() {
		return DownloadsAdapter.SEASON_KEY + " " + (tvshowInfo.seasonPosition + 1) + ", " + DownloadsAdapter.EPISODE_KEY + " "
				+ (tvshowInfo.episodePosition + 1) + "\n" + DownloadsAdapter.SIZE_KEY + " " + StorageHelper.getSizeText(torrentInfo.size);
	}

	@Override
	protected String getSubtitlesDataUrl() {
		return SUBTITLE_URL + tvshowInfo.imdb + "&s=" + (tvshowInfo.seasonPosition + 1) + "&e=" + tvshowInfo.episodePosition;
	}

	private void restartSubtitle() {
		subtitleSpinner.setVisibility(View.GONE);
		mSubtitles.restartLoader(getSubtitlesDataUrl(), VideoTVShowFragment.this);
	}

	private void initSeasonList() {
		seasonsView.setAdapter(mSeasonAdapter);
		seasonsView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				tvshowInfo.seasonPosition = position;
				currentSeason = tvshowInfo.seasons.get(position);
				mSeasonAdapter.setSelectedItem(position);
				mEpisodeAdapter.replaceData(currentSeason.namesOfEpisodes);

				if (!changeOrientation) {
					tvshowInfo.episodePosition = 0;
				}

				episodesView.performItemClick(mEpisodeAdapter.getView(tvshowInfo.episodePosition, null, null), tvshowInfo.episodePosition,
						mEpisodeAdapter.getItemId(tvshowInfo.episodePosition));
				episodesView.post(new Runnable() {

					@Override
					public void run() {
						episodesView.setSelection(tvshowInfo.episodePosition);
					}
				});
			}
		});
	}

	private void initEpisodeList() {
		episodesView.setAdapter(mEpisodeAdapter);
		episodesView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				tvshowInfo.episodePosition = position;
				currentEpisode = currentSeason.episodes.get(position);
				mEpisodeAdapter.setSelectedItem(position);
				episodeTitle.setText(Html.fromHtml("<b>" + currentEpisode.title + "</b>"));
				episodeDescription.setText(Html.fromHtml(currentEpisode.description));
				episodeDescription.post(new Runnable() {

					@Override
					public void run() {
						episodeDescription.scrollTo(0, 0);
					}
				});
				if (changeOrientation) {
					changeOrientation = false;
				} else {
					torrentPosition = 0;
					restartSubtitle();
				}
				replaceTorrentData(currentEpisode.torrents);
			}
		});
	}

	private Comparator<String> seasonComporator = new Comparator<String>() {

		@Override
		public int compare(String s1, String s2) {
			try {
				return Integer.parseInt(s1) - Integer.parseInt(s2);
			} catch (Exception ex) {
			}

			return 0;
		}
	};
}
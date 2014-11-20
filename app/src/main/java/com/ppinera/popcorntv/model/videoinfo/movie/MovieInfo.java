package com.ppinera.popcorntv.model.videoinfo.movie;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import dp.ws.popcorntime.database.tables.Favorites;
import com.ppinera.popcorntv.model.videodata.VideoData;
import com.ppinera.popcorntv.model.videoinfo.Torrent;
import com.ppinera.popcorntv.model.videoinfo.VideoInfo;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class MovieInfo extends VideoInfo {

	public ArrayList<Torrent> torrents = new ArrayList<Torrent>();

	public MovieInfo() {
		this.mType = VideoData.Type.MOVIES;
		this.mInfoUrl = "http://www.omdbapi.com/?i=";
	}

	private MovieInfo(Parcel parcel) {
		super(parcel);
		parcel.readTypedList(torrents, Torrent.CREATOR);
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeTypedList(torrents);
	}

	@Override
	public void populate(JSONObject video) throws Exception {
		super.populate(video);

		// torrents
		JSONArray jsonTorrents = video.getJSONArray("items");
		for (int j = 0; j < jsonTorrents.length(); j++) {
			JSONObject jsonTorrent = jsonTorrents.getJSONObject(j);

			Torrent torrent = new Torrent();
			torrent.url = jsonTorrent.getString("torrent_url");
			torrent.seeds = jsonTorrent.getInt("torrent_seeds");
			torrent.peers = jsonTorrent.getInt("torrent_peers");
			torrent.file = jsonTorrent.getString("file");
			torrent.quality = jsonTorrent.getString("quality");
			torrent.size = jsonTorrent.getLong("size_bytes");

			torrents.add(torrent);
		}
	}

	public void populate(Cursor cursor) throws Exception {
		super.populate(cursor);

		// torrents
		String json = cursor.getString(cursor.getColumnIndexOrThrow(Favorites._TORRENTS_INFO));
		if (!TextUtils.isEmpty(json)) {
			JSONArray jsonTorrents = new JSONArray(json);
			for (int j = 0; j < jsonTorrents.length(); j++) {
				Torrent torrent = new Torrent();
				torrent.populate(jsonTorrents.getJSONObject(j));
				torrents.add(torrent);
			}
		}
	}

	public String getTorrentsInfo() {
		JSONArray jsonTorrents = new JSONArray();

		try {
			for (int i = 0; i < torrents.size(); i++) {
				jsonTorrents.put(torrents.get(i).toJsonObject());
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return "";
		}

		return jsonTorrents.toString().trim();
	}

	public static final Parcelable.Creator<MovieInfo> CREATOR = new Parcelable.Creator<MovieInfo>() {

		public MovieInfo createFromParcel(Parcel in) {
			return new MovieInfo(in);
		}

		public MovieInfo[] newArray(int size) {
			return new MovieInfo[size];
		}
	};

}
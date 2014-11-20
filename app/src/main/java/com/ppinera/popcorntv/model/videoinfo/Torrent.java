package com.ppinera.popcorntv.model.videoinfo;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class Torrent implements Parcelable {

	public static final String URL_DB_KEY = "torrent_url";
	public static final String SEEDS_DB_KEY = "torrent_seeds";
	public static final String PEERS_DB_KEY = "torrent_peers";
	public static final String FILE_DB_KEY = "file";
	public static final String QUALITY_DB_KEY = "quality";
	public static final String SIZE_DB_KEY = "size_bytes";

	public String url;
	public int seeds;
	public int peers;
	public String file;
	public String quality;
	public long size;

	public Torrent() {

	}

	private Torrent(Parcel parcel) {
		url = parcel.readString();
		seeds = parcel.readInt();
		peers = parcel.readInt();
		file = parcel.readString();
		quality = parcel.readString();
		size = parcel.readLong();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(url);
		dest.writeInt(seeds);
		dest.writeInt(peers);
		dest.writeString(file);
		dest.writeString(quality);
		dest.writeLong(size);
	}

	public JSONObject toJsonObject() throws JSONException {
		JSONObject jsonTorrent = new JSONObject();

		jsonTorrent.put(URL_DB_KEY, url);
		jsonTorrent.put(SEEDS_DB_KEY, seeds);
		jsonTorrent.put(PEERS_DB_KEY, peers);
		jsonTorrent.put(FILE_DB_KEY, file);
		jsonTorrent.put(QUALITY_DB_KEY, quality);
		jsonTorrent.put(SIZE_DB_KEY, size);

		return jsonTorrent;
	}

	public void populate(JSONObject jsonTorrent) throws JSONException {
		url = jsonTorrent.getString(URL_DB_KEY);
		seeds = jsonTorrent.getInt(SEEDS_DB_KEY);
		peers = jsonTorrent.getInt(PEERS_DB_KEY);
		file = jsonTorrent.getString(FILE_DB_KEY);
		quality = jsonTorrent.getString(QUALITY_DB_KEY);
		size = jsonTorrent.getLong(SIZE_DB_KEY);
	}

	public static final Parcelable.Creator<Torrent> CREATOR = new Parcelable.Creator<Torrent>() {

		public Torrent createFromParcel(Parcel in) {
			return new Torrent(in);
		}

		public Torrent[] newArray(int size) {
			return new Torrent[size];
		}
	};

}
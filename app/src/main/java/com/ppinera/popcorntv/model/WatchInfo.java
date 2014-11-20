package com.ppinera.popcorntv.model;

import java.util.ArrayList;

import com.ppinera.popcorntv.subtitles.Subtitles;
import android.os.Parcel;
import android.os.Parcelable;

public class WatchInfo implements Parcelable {

	public boolean isDownloads = false;
	public String type;
	public String imdb;
	public String torrentFilePath;
	public String fileName;
	public String subtitlesDataUrl;
	public int subtitlesPosition = 0;
	public ArrayList<String> subtitlesData = null;
	public ArrayList<String> subtitlesUrls = null;

	public WatchInfo() {

	}

	public WatchInfo(DownloadInfo downloadInfo) {
		this(downloadInfo, null);
	}

	public WatchInfo(DownloadInfo downloadInfo, Subtitles subtitles) {
		if (downloadInfo != null) {
			isDownloads = true;
			type = downloadInfo.type;
			imdb = downloadInfo.imdb;
			torrentFilePath = downloadInfo.torrentFilePath;
			fileName = downloadInfo.fileName;
			subtitlesDataUrl = downloadInfo.subtitlesDataUrl;
		}
		if (subtitles != null) {
			subtitlesPosition = subtitles.position;
			subtitlesData = subtitles.data;
			subtitlesUrls = subtitles.urls;
		}
	}

	private WatchInfo(Parcel parcel) {
		isDownloads = parcel.readByte() == 1;
		type = parcel.readString();
		imdb = parcel.readString();
		torrentFilePath = parcel.readString();
		fileName = parcel.readString();
		subtitlesDataUrl = parcel.readString();
		subtitlesPosition = parcel.readInt();

		subtitlesData = new ArrayList<String>();
		parcel.readStringList(subtitlesData);
		if (subtitlesData.size() == 0) {
			subtitlesData = null;
			subtitlesUrls = null;
		} else {
			subtitlesUrls = new ArrayList<String>();
			parcel.readStringList(subtitlesUrls);
		}
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		parcel.writeByte((byte) (isDownloads ? 1 : 0));
		parcel.writeString(type);
		parcel.writeString(imdb);
		parcel.writeString(torrentFilePath);
		parcel.writeString(fileName);
		parcel.writeString(subtitlesDataUrl);
		parcel.writeInt(subtitlesPosition);
		parcel.writeStringList(subtitlesData);
		parcel.writeStringList(subtitlesUrls);
	}

	public static final Parcelable.Creator<WatchInfo> CREATOR = new Parcelable.Creator<WatchInfo>() {

		public WatchInfo createFromParcel(Parcel in) {
			return new WatchInfo(in);
		}

		public WatchInfo[] newArray(int size) {
			return new WatchInfo[size];
		}
	};
}
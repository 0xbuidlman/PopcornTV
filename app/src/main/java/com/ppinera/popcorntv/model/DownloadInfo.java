package com.ppinera.popcorntv.model;

import java.io.File;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

public class DownloadInfo implements Parcelable {

	public static final String EXTRA_KEY = "download_info";

	public long id;
	public String type;
	public String imdb;
	public String torrentUrl;
	public String fileName;
	public String posterUrl;
	public String title;
	public String summary;
	public String subtitlesDataUrl;
	public File directory;
	public String torrentFilePath;
	public int state;
	public long size;

	public DownloadInfo() {

	}

	@Override
	public int describeContents() {
		return 0;
	}

	private DownloadInfo(Parcel parcel) {
		id = parcel.readLong();
		type = parcel.readString();
		imdb = parcel.readString();
		torrentUrl = parcel.readString();
		fileName = parcel.readString();
		posterUrl = parcel.readString();
		title = parcel.readString();
		summary = parcel.readString();
		subtitlesDataUrl = parcel.readString();
		directory = new File(parcel.readString());
		torrentFilePath = parcel.readString();
		state = parcel.readInt();
		size = parcel.readLong();
	}

	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		parcel.writeLong(id);
		parcel.writeString(type);
		parcel.writeString(imdb);
		parcel.writeString(torrentUrl);
		parcel.writeString(fileName);
		parcel.writeString(posterUrl);
		parcel.writeString(title);
		parcel.writeString(summary);
		parcel.writeString(subtitlesDataUrl);
		parcel.writeString(directory.getAbsolutePath());
		parcel.writeString(torrentFilePath);
		parcel.writeInt(state);
		parcel.writeLong(size);
	}

	public void populate(Cursor cursor) throws Exception {
		id = cursor.getLong(cursor.getColumnIndexOrThrow(Downloads._ID));
		type = cursor.getString(cursor.getColumnIndexOrThrow(Downloads._TYPE));
		imdb = cursor.getString(cursor.getColumnIndexOrThrow(Downloads._IMDB));
		torrentUrl = cursor.getString(cursor.getColumnIndexOrThrow(Downloads._TORRENT_URL));
		fileName = cursor.getString(cursor.getColumnIndexOrThrow(Downloads._FILE_NAME));
		posterUrl = cursor.getString(cursor.getColumnIndexOrThrow(Downloads._POSTER_URL));
		title = cursor.getString(cursor.getColumnIndexOrThrow(Downloads._TITLE));
		summary = cursor.getString(cursor.getColumnIndexOrThrow(Downloads._SUMMARY));
		subtitlesDataUrl = cursor.getString(cursor.getColumnIndexOrThrow(Downloads._SUBTITLES_DATA_URL));
		directory = new File(cursor.getString(cursor.getColumnIndexOrThrow(Downloads._DIRECTORY)));
		torrentFilePath = cursor.getString(cursor.getColumnIndexOrThrow(Downloads._TORRENT_FILE_PATH));
		state = cursor.getInt(cursor.getColumnIndexOrThrow(Downloads._STATE));
		size = cursor.getLong(cursor.getColumnIndexOrThrow(Downloads._SIZE));
	}

	public static final Parcelable.Creator<DownloadInfo> CREATOR = new Parcelable.Creator<DownloadInfo>() {

		public DownloadInfo createFromParcel(Parcel in) {
			return new DownloadInfo(in);
		}

		public DownloadInfo[] newArray(int size) {
			return new DownloadInfo[size];
		}
	};

}
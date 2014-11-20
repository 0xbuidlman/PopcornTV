package com.ppinera.popcorntv.model.videoinfo;

import org.json.JSONObject;

import dp.ws.popcorntime.database.tables.Favorites;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

public abstract class VideoInfo implements Parcelable {

	protected String mType;
	protected String mInfoUrl;
	public String title;
	public String year;
	public float rating;
	public String imdb;
	public String actors = "";
	public String trailer = "";
	public String description = "";
	public String posterMediumUrl = "";
	public String posterBigUrl = "";

	public VideoInfo() {

	}

	protected VideoInfo(Parcel parcel) {
		mType = parcel.readString();
		mInfoUrl = parcel.readString();
		title = parcel.readString();
		year = parcel.readString();
		rating = parcel.readFloat();
		imdb = parcel.readString();
		actors = parcel.readString();
		trailer = parcel.readString();
		description = parcel.readString();
		posterMediumUrl = parcel.readString();
		posterBigUrl = parcel.readString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mType);
		dest.writeString(mInfoUrl);
		dest.writeString(title);
		dest.writeString(year);
		dest.writeFloat(rating);
		dest.writeString(imdb);
		dest.writeString(actors);
		dest.writeString(trailer);
		dest.writeString(description);
		dest.writeString(posterMediumUrl);
		dest.writeString(posterBigUrl);
	}

	public String getType() {
		return mType;
	}

	public String getInfoUrl() {
		return mInfoUrl;
	}

	public void populate(JSONObject video) throws Exception {
		this.title = video.getString("title");
		this.year = video.getString("year");
		this.rating = (float) video.getDouble("rating");
		this.imdb = video.getString("imdb");
		this.mInfoUrl += imdb;
		this.actors = video.getString("actors");

		String trailerID = video.getString("trailer");
		if (trailerID != null || !"".equals(trailerID)) {
			this.trailer = "http://www.youtube.com/embed/" + trailerID + "?autoplay=1";
		}

		this.description = video.getString("description");
		this.posterMediumUrl = video.getString("poster_med");
		this.posterBigUrl = video.getString("poster_big");
	}

	public void populate(Cursor cursor) throws Exception {
		this.title = cursor.getString(cursor.getColumnIndexOrThrow(Favorites._TITLE));
		this.year = cursor.getString(cursor.getColumnIndexOrThrow(Favorites._YEAR));
		this.rating = cursor.getFloat(cursor.getColumnIndexOrThrow(Favorites._RATING));
		this.imdb = cursor.getString(cursor.getColumnIndexOrThrow(Favorites._IMDB));
		this.mInfoUrl += imdb;
		this.actors = cursor.getString(cursor.getColumnIndexOrThrow(Favorites._ACTORS));
		this.trailer = cursor.getString(cursor.getColumnIndexOrThrow(Favorites._TRAILER));
		this.description = cursor.getString(cursor.getColumnIndexOrThrow(Favorites._DESCRIPTION));
		this.posterMediumUrl = cursor.getString(cursor.getColumnIndexOrThrow(Favorites._POSTER_MEDIUM_URL));
		this.posterBigUrl = cursor.getString(cursor.getColumnIndexOrThrow(Favorites._POSTER_BIG_URL));
	}
}
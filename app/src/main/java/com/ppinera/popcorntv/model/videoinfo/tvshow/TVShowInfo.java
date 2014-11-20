package com.ppinera.popcorntv.model.videoinfo.tvshow;

import java.util.ArrayList;

import com.ppinera.popcorntv.model.videodata.VideoData;
import com.ppinera.popcorntv.model.videoinfo.VideoInfo;
import android.os.Parcel;
import android.os.Parcelable;

public class TVShowInfo extends VideoInfo {

	public ArrayList<Season> seasons = new ArrayList<Season>();
	public int seasonPosition;
	public int episodePosition;

	public TVShowInfo() {
		this.mType = VideoData.Type.TV_SHOWS;
		this.mInfoUrl = "http://api.torrentsapi.com/show?formats=mp4&imdb=";
	}

	private TVShowInfo(Parcel parcel) {
		super(parcel);
		parcel.readTypedList(seasons, Season.CREATOR);
		seasonPosition = parcel.readInt();
		episodePosition = parcel.readInt();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeTypedList(seasons);
		dest.writeInt(seasonPosition);
		dest.writeInt(episodePosition);
	}

	public static final Parcelable.Creator<TVShowInfo> CREATOR = new Parcelable.Creator<TVShowInfo>() {

		public TVShowInfo createFromParcel(Parcel in) {
			return new TVShowInfo(in);
		}

		public TVShowInfo[] newArray(int size) {
			return new TVShowInfo[size];
		}
	};
}
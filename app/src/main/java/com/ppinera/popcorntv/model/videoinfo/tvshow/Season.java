package com.ppinera.popcorntv.model.videoinfo.tvshow;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

public class Season implements Parcelable {

	public ArrayList<String> namesOfEpisodes = new ArrayList<String>();
	public ArrayList<Episode> episodes = new ArrayList<Episode>();

	public Season() {

	}

	private Season(Parcel parcel) {
		parcel.readStringList(namesOfEpisodes);
		parcel.readTypedList(episodes, Episode.CREATOR);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeStringList(namesOfEpisodes);
		dest.writeTypedList(episodes);
	}

	public static final Parcelable.Creator<Season> CREATOR = new Parcelable.Creator<Season>() {

		public Season createFromParcel(Parcel in) {
			return new Season(in);
		}

		public Season[] newArray(int size) {
			return new Season[size];
		}
	};
}
package com.ppinera.popcorntv.subtitles;

public interface SubtitleListener {

	public void onSubtitleLoadSucces(String data);

	public void onSubtitleLoadError(String message);
}

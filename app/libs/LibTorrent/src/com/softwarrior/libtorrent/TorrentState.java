package com.softwarrior.libtorrent;

public class TorrentState {
	public static final int QEUED_FOR_CHECKING = 0;
	public static final int CHECKING_FILES = 1;
	public static final int DOWNLOADING_METADATA = 2;
	public static final int DOWNLOADING = 3;
	public static final int FINISHED = 4;
	public static final int SEEDING = 5;
	public static final int ALLOCATING = 6;
	public static final int CHECKING_RESUME_DATA = 7;
	public static final int PAUSED = 8;
}
package com.ppinera.popcorntv.torrent;

import android.os.Handler;
import android.util.Log;
import com.softwarrior.libtorrent.LibTorrent;
import com.softwarrior.libtorrent.Priority;

public class Prioritizer {

	private final int ACTIVE_PIECE_COUNT = 5;
	private final int PREPARE_PIECE_COUNT = 3;
	private final int UPDATE_TIME = 500;

	private LibTorrent libTorrent;
	private String contentFile;
	private int firstPieceIndex = -1;
	private int lastPieceIndex = -1;
	private int pieceIndex;
	private int pieceCount;
	private Handler handler;
	private boolean isHaveAllPieces;

	private boolean isStart;
	private int cPreparePieceCount;

	public Prioritizer(LibTorrent libTorrent) {
		this.libTorrent = libTorrent;
		handler = new Handler();
	}

	public boolean load(String contentFile) {
		this.contentFile = contentFile;
		handler.removeCallbacks(updater);
		isStart = false;
		cPreparePieceCount = PREPARE_PIECE_COUNT;
		firstPieceIndex = -1;
		lastPieceIndex = -1;
		int[] priorities = libTorrent.GetPiecePriorities(contentFile);
		for (int i = 0; i < priorities.length; i++) {
			if (priorities[i] != Priority.DONT_DOWNLOAD) {
				if (firstPieceIndex == -1) {
					firstPieceIndex = i;
				}
				priorities[i] = Priority.DONT_DOWNLOAD;
			} else {
				if (firstPieceIndex != -1) {
					if (lastPieceIndex == -1) {
						lastPieceIndex = i - 1;
					}
				}
			}
		}
		if (firstPieceIndex == -1) {
			return false;
		} else {
			if (lastPieceIndex == -1) {
				lastPieceIndex = priorities.length - 1;
			}
		}
		pieceIndex = firstPieceIndex;
		pieceCount = lastPieceIndex - firstPieceIndex + 1;
		if (pieceCount < 1) {
			return false;
		}

		for (int i = 0; i < cPreparePieceCount; i++) {
			priorities[lastPieceIndex - i] = Priority.MAXIMAL;
		}
		for (int i = 0; i < cPreparePieceCount + 2; i++) {
			priorities[firstPieceIndex + i] = Priority.NORMAL;
		}
		libTorrent.SetPiecePriorities(contentFile, priorities);

		return true;
	}

	public void reload() {
		handler.removeCallbacks(updater);
		isStart = false;
		if (firstPieceIndex != -1 && lastPieceIndex != -1) {
			int[] priorities = libTorrent.GetPiecePriorities(contentFile);
			priorities[lastPieceIndex - cPreparePieceCount] = Priority.MAXIMAL;
			priorities[firstPieceIndex + cPreparePieceCount] = Priority.NORMAL;
			libTorrent.SetPiecePriorities(contentFile, priorities);
			cPreparePieceCount += 1;
		}
	}

	public void checkToStart() {
		if (isStart) {
			return;
		}

		for (int i = 0; i < cPreparePieceCount; i++) {
			if (!libTorrent.HavePiece(contentFile, lastPieceIndex - i)) {
				return;
			}
		}

		isStart = true;
		Log.d("tag", "Start prioritizer!");

		updater.run();
	}

	public int getPrepareProgress() {
		double haveCount = 0;
		for (int i = 0; i < cPreparePieceCount; i++) {
			if (libTorrent.HavePiece(contentFile, firstPieceIndex + i)) {
				haveCount++;
			}
			if (libTorrent.HavePiece(contentFile, lastPieceIndex - i)) {
				haveCount++;
			}
		}
		double progress = haveCount / (cPreparePieceCount * 2);
		return (int) (progress * 100);
	}

	public void stop() {
		handler.removeCallbacks(updater);
		if (firstPieceIndex != -1 && lastPieceIndex != -1) {
			int[] piecePriorities = libTorrent.GetPiecePriorities(contentFile);
			if (piecePriorities == null) {
				return;
			}
			for (int i = firstPieceIndex; i <= lastPieceIndex; i++) {
				piecePriorities[i] = Priority.NORMAL;
			}
			libTorrent.SetPiecePriorities(contentFile, piecePriorities);
		}

	}

	public boolean canSeekTo(long length, long position) {
		if (isHaveAllPieces) {
			return true;
		}

		int seekPiece = (int) (position * pieceCount / length);
		if (pieceIndex > seekPiece) {
			return true;
		}

		return false;
	}

	public int getSeekProgress(int max) {
		if (isHaveAllPieces) {
			return max;
		}

		return max * pieceIndex / pieceCount;
	}

	private synchronized void updatePiecePriority() {
		int[] priorities = libTorrent.GetPiecePriorities(contentFile);
		if (priorities != null && priorities.length > 0) {
			isHaveAllPieces = true;
			int count = 0;
			int newIndex = -1;
			// String zzz = "| ";
			for (int i = pieceIndex; i <= lastPieceIndex; i++) {
				if (!libTorrent.HavePiece(contentFile, i)) {
					isHaveAllPieces = false;
					if (newIndex == -1) {
						newIndex = i;
					}
					if (Priority.DONT_DOWNLOAD == priorities[i]) {
						priorities[i] = Priority.MAXIMAL;
					}
					// zzz += i + " | ";
					count++;
				}

				if (count >= ACTIVE_PIECE_COUNT) {
					break;
				}
			}
			// Log.d("tag", zzz);
			if (newIndex != -1) {
				pieceIndex = newIndex;
			}
			libTorrent.SetPiecePriorities(contentFile, priorities);
		}
	}

	private Runnable updater = new Runnable() {

		@Override
		public void run() {
			updatePiecePriority();
			if (isHaveAllPieces == false) {
				handler.postDelayed(updater, UPDATE_TIME);
			}
		}
	};
}
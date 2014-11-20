package com.ppinera.popcorntv.torrent;

import java.util.ArrayList;
import java.util.HashMap;

public class Metadata {

	public static final String ANNOUNCE_KEY = "announce";
	public static final String ANNOUNCE_LIST_KEY = "announce-list";
	public static final String INFO_KEY = "info";
	public static final String INFO_FILES_KEY = "files";
	public static final String INFO_FILES_LENGTH_KEY = "length";
	public static final String INFO_FILES_PATH_KEY = "path";
	public static final String INFO_NAME_KEY = "name";
	public static final String INFO_LENGTH_KEY = "length";

	private HashMap<String, Object> data;

	@SuppressWarnings("unchecked")
	public Metadata(String filePath) throws Exception {
		Object obj = Bencode.Decoder.decode(filePath);
		if (obj instanceof HashMap) {
			this.data = (HashMap<String, Object>) obj;
		}
	}

	public <T> T getValue(String key) {
		return Utils.getValueFrom(data, key);
	}

	public String getAnnounce() {
		return getValue(ANNOUNCE_KEY);
	}

	public ArrayList<Object> getAnnounceList() {
		return getValue(ANNOUNCE_LIST_KEY);
	}

	public HashMap<String, Object> getInfo() {
		return getValue(INFO_KEY);
	}

	public ArrayList<HashMap<String, Object>> getFiles() {
		return Utils.getValueFrom(getInfo(), INFO_FILES_KEY);
	}

	public String getInfoName() {
		return Utils.getValueFrom(getInfo(), INFO_NAME_KEY);
	}

	public long getInfoLength() {
		return Utils.getValueFrom(getInfo(), INFO_LENGTH_KEY);
	}

	public static class Utils {

		@SuppressWarnings("unchecked")
		public static <T> T getValueFrom(HashMap<String, Object> map, String key) {
			if (map != null && map.containsKey(key)) {
				return (T) map.get(key);
			}
			return null;
		}
	}

	public class File {

		public long length;
		public ArrayList<String> path;

		public File(HashMap<String, Object> map) {
			length = Utils.getValueFrom(map, Metadata.INFO_FILES_LENGTH_KEY);
			path = Utils.getValueFrom(map, Metadata.INFO_FILES_PATH_KEY);
		}

		@Override
		public String toString() {
			return length + ": " + path.toString();
		}
	}
}
package com.ppinera.popcorntv.subtitles.format;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

public class JSON extends Format {

	private static final String CUE_ID_KEY = "id";
	private static final String CUE_START_KEY = "start";
	private static final String CUE_END_KEY = "end";
	private static final String CUE_TEXT_KEY = "text";

	private static final int ELEMENT_COUNT = 100;

	public static ArrayList<JSONArray> convert(String fileName) throws Exception {
		ArrayList<JSONArray> data = new ArrayList<JSONArray>();
		JSONArray subtitleJson = new JSONArray();
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		reader.mark(Integer.MAX_VALUE);
		if (65279 == reader.read()) {
			Log.w("tag", "Subtitle: UTF-8 with BOM!!!");
		} else {
			reader.reset();
		}

		String line;
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			if (isNumber(line)) {
				JSONObject cueJson = readCue(reader, line);
				subtitleJson.put(cueJson);
				// Log.d("tag", "cue json: " + cueJson.toString());
				if (subtitleJson.length() >= ELEMENT_COUNT) {
					data.add(subtitleJson);
					subtitleJson = new JSONArray();
				}
			}
		}

		Log.d("tag", "JSON convert complete!");
		reader.close();

		return data;
	}

	private static JSONObject readCue(BufferedReader reader, String textId) throws Exception {
		int id = Integer.parseInt(textId);
		String start = "0";
		String end = "0";
		String text = "";
		String[] times = reader.readLine().trim().split("-->");
		if (times.length == 2) {
			start = parseCueTime(times[0]);
			end = parseCueTime(times[1]);
		} else {
			throw new Exception("Wrong time line");
		}

		String _text;
		while (true) {
			reader.mark(Integer.MAX_VALUE);
			_text = reader.readLine();
			if (_text != null) {
				_text = _text.trim();
				if (isNumber(_text)) {
					reader.reset();
					break;
				} else {
					if (text.length() > 0) {
						text += " " + _text;
					} else {
						text += _text;
					}
				}
			} else {
				break;
			}
		}

		return createCue(id, start, end, text);
	}

	private static String parseCueTime(String textTime) throws Exception {
		String[] times = textTime.split(":");
		if (times.length != 3) {
			throw new Exception("Invalid time format");
		}
		String[] seconds = times[2].split(",");
		int time = 3600 * Integer.parseInt(times[0].trim()) + 60 * Integer.parseInt(times[1].trim()) + Integer.parseInt(seconds[0].trim());
		String ms = seconds[1].trim();

		if (ms.length() > 0) {
			return time + "." + ms.charAt(0);
		}

		return time + ".0";
	}

	private static JSONObject createCue(long id, String start, String end, String text) throws Exception {
		JSONObject cue = new JSONObject();
		cue.put(CUE_ID_KEY, id);
		cue.put(CUE_START_KEY, start);
		cue.put(CUE_END_KEY, end);
		cue.put(CUE_TEXT_KEY, text);
		return cue;
	}
}

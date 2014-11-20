package com.ppinera.popcorntv.subtitles.format;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import android.text.TextUtils;
import android.util.Log;

public class SRT extends Format {

	public static String convert(String str, String color) {
		BufferedReader reader = new BufferedReader(new StringReader(str));
		StringBuilder builder = new StringBuilder();

		try {
			reader.mark(Integer.MAX_VALUE);
			if (65279 == reader.read()) {
				Log.w("tag", "SRT convert: UTF-8 with BOM!");
			} else {
				reader.reset();
			}

			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (isNumber(line)) {
					builder.append(line);
					builder.append("\n");
					readSrtCue(reader, builder, color);
				}
			}
		} catch (Exception ex) {
			builder.setLength(0);
		}
		try {
			reader.close();
		} catch (IOException e) {
		}

		return builder.toString();
	}

	private static void readSrtCue(BufferedReader reader, StringBuilder builder, String color) throws Exception {
		String line = reader.readLine();
		if (TextUtils.isEmpty(line) || !line.contains("-->")) {
			throw new Exception("Time line is empty");
		}
		builder.append(line);
		builder.append("\n");

		String text = "";
		while (true) {
			reader.mark(Integer.MAX_VALUE);
			line = reader.readLine();
			if (TextUtils.isEmpty(line)) {
				break;
			} else {
				line = line.trim();
				if (isNumber(line)) {
					reader.reset();
					break;
				} else {
					text += line + "\n";
				}
			}
		}

		if (text.contains("<font")) {
			text = text.replaceAll("<font[^>]*>", "<font color=\"" + color + "\">");
		} else {
			text = "<font color=\"" + color + "\">" + text.trim() + "</font>\n";
		}

		builder.append(text);
		builder.append("\n");
	}
}
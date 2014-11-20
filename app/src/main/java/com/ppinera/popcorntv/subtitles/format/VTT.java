package com.ppinera.popcorntv.subtitles.format;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

import android.util.Log;

public class VTT extends Format {

	public static boolean convert(String srcPath, String destPath) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(srcPath));
			BufferedWriter writer = new BufferedWriter(new FileWriter(destPath));
			writer.write("WEBVTT");
			writer.newLine();
			writer.newLine();

			reader.mark(Integer.MAX_VALUE);
			if (65279 == reader.read()) {
				Log.w("tag", "VTT convert: UTF-8 with BOM!");
			} else {
				reader.reset();
			}

			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (isNumber(line)) {
					readVttCue(reader, writer);
				}
			}

			reader.close();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		Log.d("tag", "VTT convert complete!");
		return true;
	}

	private static void readVttCue(BufferedReader reader, BufferedWriter writer) throws Exception {
		String[] times = reader.readLine().trim().split("-->");
		if (times.length == 2) {
			writer.write(times[0].replace(",", ".") + " --> " + times[1].replace(",", "."));
			writer.newLine();
		} else {
			throw new Exception("Wrong time line");
		}

		while (true) {
			reader.mark(Integer.MAX_VALUE);
			String _text = reader.readLine();
			if (_text != null) {
				_text = _text.trim();
				if (isNumber(_text)) {
					reader.reset();
					break;
				} else {
					_text = _text.replaceAll("</*font[^>]*>", "");
					writer.write(_text);
					writer.newLine();
				}
			} else {
				break;
			}
		}
	}
}

package com.ppinera.popcorntv.torrent;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class Bencode {

	private final static int STREAM_MARK = 1;

	private final static int DICTIONARY = 'd';
	private final static int LIST = 'l';
	private final static int STRING_1 = '1';
	private final static int STRING_9 = '9';
	private final static int STRING_DELIMITER = ':';
	private final static int NUMBER = 'i';
	private final static int END = 'e';

	public static class Decoder {

		private static int symbol;

		public static Object decode(String filePath) throws Exception {
			return decode(new BufferedInputStream(new FileInputStream(filePath)));
		}

		public static Object decode(File file) throws Exception {
			return decode(new BufferedInputStream(new FileInputStream(file)));
		}

		public static Object decode(InputStream input) throws Exception {
			if (!input.markSupported()) {
				throw new Exception("InputStream don't support mark");
			}

			input.mark(STREAM_MARK);
			symbol = input.read();
			if (-1 == symbol) {
				throw new Exception("InputStream ended");
			} else if (DICTIONARY == symbol) {
				return decodeDictionary(input);
			} else if (LIST == symbol) {
				return decodeList(input);
			} else if (STRING_1 <= symbol && STRING_9 >= symbol) {
				return decodeString(input);
			} else if (NUMBER == symbol) {
				return decodeNumber(input);
			} else {
				throw new Exception("Illegal symbol " + (char) symbol);
			}
		}

		private static HashMap<String, Object> decodeDictionary(InputStream input) throws Exception {
			HashMap<String, Object> map = new HashMap<String, Object>();
			while (true) {
				input.mark(STREAM_MARK);
				symbol = input.read();
				if (-1 == symbol) {
					throw new Exception("InputStream ended");
				} else if (END == symbol) {
					break;
				} else {
					map.put(decodeString(input), decode(input));
				}
			}
			return map;
		}

		private static ArrayList<Object> decodeList(InputStream input) throws Exception {
			ArrayList<Object> list = new ArrayList<Object>();
			while (true) {
				input.mark(STREAM_MARK);
				symbol = input.read();
				if (-1 == symbol) {
					throw new Exception("InputStream ended");
				} else if (END == symbol) {
					break;
				} else {
					input.reset();
					list.add(decode(input));
				}
			}
			return list;
		}

		private static String decodeString(InputStream input) throws Exception {
			input.reset();
			String length = "";
			while (true) {
				symbol = input.read();
				if (-1 == symbol) {
					throw new Exception("InputStream ended");
				} else if (STRING_DELIMITER == symbol) {
					break;
				} else {
					length += Character.toString((char) symbol);
				}
			}
			byte[] str = new byte[Integer.parseInt(length)];
			input.read(str);
			return new String(str);
		}

		private static long decodeNumber(InputStream input) throws Exception {
			String number = "";
			while (true) {
				symbol = input.read();
				if (-1 == symbol) {
					throw new Exception("InputStream ended");
				} else if (END == symbol) {
					break;
				} else {
					number += Character.toString((char) symbol);
				}
			}
			return Long.parseLong(number);
		}
	}
}
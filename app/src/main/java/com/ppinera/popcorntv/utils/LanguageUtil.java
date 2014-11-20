package com.ppinera.popcorntv.utils;

import java.util.HashMap;

public class LanguageUtil {

	private static final String ENGLISH = "english";
	private static final String SPANISH = "spanish";
	private static final String PORTUGUESE = "portuguese";
	private static final String FRENCH = "french";
	private static final String ITALIAN = "italian";
	private static final String GERMAN = "german";
	private static final String RUSSIAN = "russian";
	private static final String ARABIC = "arabic";
	private static final String ROMANIAN = "romanian";
	private static final String HEBREW = "hebrew";
	private static final String POLISH = "polish";
	private static final String INDONESIAN = "indonesian";
	private static final String FINNISH = "finnish";
	private static final String URDU = "urdu";
	private static final String FARSI_PERSIAN = "farsi-persian";
	private static final String CROATIAN = "croatian";
	private static final String VIETNAMESE = "vietnamese";
	private static final String CHINESE = "chinese";
	private static final String DUTCH = "dutch";
	private static final String GREEK = "greek";
	private static final String SWEDISH = "swedish";
	private static final String BRAZILIAN_PORTUGUESE = "brazilian-portuguese";
	private static final String CZECH = "czech";
	private static final String HUNGARIAN = "hungarian";
	private static final String TURKISH = "turkish";
	private static final String KOREAN = "korean";
	private static final String NORWEGIAN = "norwegian";
	private static final String SERBIAN = "serbian";
	private static final String DANISH = "danish";
	private static final String BULGARIAN = "bulgarian";
	private static final String THAI = "thai";
	private static final String MALAY = "malay";
	private static final String BENGALI = "bengali";
	private static final String JAPANESE = "japanese";
	private static final String SLOVENIAN = "slovenian";
	private static final String MACEDONIAN = "macedonian";
	private static final String ALBANIAN = "albanian";
	private static final String BOSNIAN = "bosnian";
	private static final String SLOVAK = "slovak";
	private static final String ESTONIAN = "estonian";
	private static final String LATVIAN = "latvian";
	private static final String UKRAINIAN = "ukrainian";
	private static final String GALICIAN = "galician";
	private static final String CATALAN = "catalan";
	private static final String GEORGIAN = "georgian";
	private static final String LITHUANIAN = "lithuanian";
	private static final String SINHALA = "sinhala";

	// language native name
	private static final String NATIVE_ENGLISH = "English";
	private static final String NATIVE_SPANISH = "Español";
	private static final String NATIVE_PORTUGUESE = "Português";
	private static final String NATIVE_FRENCH = "Français";
	private static final String NATIVE_ITALIAN = "Italiano";
	private static final String NATIVE_GERMAN = "Deutsch";
	private static final String NATIVE_RUSSIAN = "Pусский";
	private static final String NATIVE_ARABIC = "العربية";
	private static final String NATIVE_ROMANIAN = "Român";
	private static final String NATIVE_HEBREW = "עברית";
	private static final String NATIVE_POLISH = "Polski";
	private static final String NATIVE_INDONESIAN = "Indonesia";
	private static final String NATIVE_FINNISH = "Suomi";
	private static final String NATIVE_URDU = "اردو";
	private static final String NATIVE_FARSI_PERSIAN = "فارسی";
	private static final String NATIVE_CROATIAN = "Hrvatska";
	private static final String NATIVE_VIETNAMESE = "Việt";
	private static final String NATIVE_CHINESE = "中文";
	private static final String NATIVE_DUTCH = "Nederlands";
	private static final String NATIVE_GREEK = "ελληνικά";
	private static final String NATIVE_SWEDISH = "Svenska";
	private static final String NATIVE_BRAZILIAN_PORTUGUESE = "Brazilian Portuguese";
	private static final String NATIVE_CZECH = "Čeština";
	private static final String NATIVE_HUNGARIAN = "Magyar";
	private static final String NATIVE_TURKISH = "Türkçe";
	private static final String NATIVE_KOREAN = "한국어";
	private static final String NATIVE_NORWEGIAN = "Norsk";
	private static final String NATIVE_SERBIAN = "Српски";
	private static final String NATIVE_DANISH = "Dansk";
	private static final String NATIVE_BULGARIAN = "Български";
	private static final String NATIVE_THAI = "ไทย";
	private static final String NATIVE_MALAY = "Melayu";
	private static final String NATIVE_BENGALI = "বাংলা";
	private static final String NATIVE_JAPANESE = "日本語";
	private static final String NATIVE_SLOVENIAN = "Slovenščina";
	private static final String NATIVE_MACEDONIAN = "Македонски";
	private static final String NATIVE_ALBANIAN = "Shqip";
	private static final String NATIVE_BOSNIAN = "Bosanski";
	private static final String NATIVE_SLOVAK = "Slovenčina";
	private static final String NATIVE_ESTONIAN = "Eesti";
	private static final String NATIVE_LATVIAN = "Latviešu";
	private static final String NATIVE_UKRAINIAN = "Українська";
	private static final String NATIVE_GALICIAN = "Galego";
	private static final String NATIVE_CATALAN = "Català";
	private static final String NATIVE_GEORGIAN = "ქართული";
	private static final String NATIVE_LITHUANIAN = "Lietuvių";
	private static final String NATIVE_SINHALA = "සිංහල";

	// ISO
	private static final String ISO_ENGLISH = "en";
	private static final String ISO_SPANISH = "es";
	private static final String ISO_PORTUGUESE = "pt";
	private static final String ISO_FRENCH = "fr";
	private static final String ISO_ITALIAN = "it";
	private static final String ISO_GERMAN = "de";
	private static final String ISO_RUSSIAN = "ru";
	private static final String ISO_ARABIC = "ar";
	private static final String ISO_ROMANIAN = "ro";
	private static final String ISO_HEBREW_1 = "he";
	private static final String ISO_HEBREW_2 = "iw";
	private static final String ISO_POLISH_1 = "po";
	private static final String ISO_POLISH_2 = "pl";
	private static final String ISO_INDONESIAN = "id";
	private static final String ISO_FINNISH = "fi";
	private static final String ISO_URDU = "ur";
	private static final String ISO_FARSI_PERSIAN = "fa";
	private static final String ISO_CROATIAN = "hr";
	private static final String ISO_VIETNAMESE = "vi";
	private static final String ISO_CHINESE = "zh";
	private static final String ISO_DUTCH = "nl";
	private static final String ISO_GREEK = "el";
	private static final String ISO_SWEDISH = "sv";
	private static final String ISO_BRAZILIAN_PORTUGUESE_1 = "pt-br";
	private static final String ISO_BRAZILIAN_PORTUGUESE_2 = "pb";
	private static final String ISO_CZECH = "cs";
	private static final String ISO_HUNGARIAN = "hu";
	private static final String ISO_TURKISH = "tr";
	private static final String ISO_KOREAN = "ko";
	private static final String ISO_NORWEGIAN = "no";
	private static final String ISO_SERBIAN = "sr";
	private static final String ISO_DANISH = "da";
	private static final String ISO_BULGARIAN = "bg";
	private static final String ISO_THAI = "th";
	private static final String ISO_MALAY = "ms";
	private static final String ISO_BENGALI = "bn";
	private static final String ISO_JAPANESE = "ja";
	private static final String ISO_SLOVENIAN = "sl";
	private static final String ISO_MACEDONIAN = "mk";
	private static final String ISO_ALBANIAN = "sq";
	private static final String ISO_BOSNIAN = "bs";
	private static final String ISO_SLOVAK = "sk";
	private static final String ISO_ESTONIAN = "et";
	private static final String ISO_LATVIAN = "lv";
	private static final String ISO_UKRAINIAN = "uk";
	private static final String ISO_GALICIAN = "gl";
	private static final String ISO_CATALAN = "ca";
	private static final String ISO_GEORGIAN = "ka";
	private static final String ISO_LITHUANIAN = "lt";
	private static final String ISO_SINHALA = "si";

	public static final String[] INTERFACE_ISO_LANGUAGES = new String[] { ISO_ENGLISH, ISO_RUSSIAN };
	public static final String[] INTERFACE_NATIVE_LANGUAGES = new String[] { NATIVE_ENGLISH, NATIVE_RUSSIAN };

	public static final String[] SUBTITLE_LANGUAGES = new String[] { "", ENGLISH, SPANISH, PORTUGUESE, FRENCH, ITALIAN, GERMAN, RUSSIAN, ARABIC, ROMANIAN,
			HEBREW, POLISH, INDONESIAN, FINNISH, URDU, FARSI_PERSIAN, CROATIAN, VIETNAMESE, CHINESE, DUTCH, GREEK, SWEDISH, BRAZILIAN_PORTUGUESE, CZECH,
			HUNGARIAN, TURKISH, KOREAN, NORWEGIAN, SERBIAN, DANISH, BULGARIAN, THAI, MALAY, BENGALI, JAPANESE, SLOVENIAN, MACEDONIAN, ALBANIAN, BOSNIAN,
			SLOVAK, ESTONIAN, LATVIAN, UKRAINIAN, GALICIAN, CATALAN, GEORGIAN, LITHUANIAN, SINHALA };

	public static final String[] SUBTITLE_NATIVE_LANGUAGES = new String[] { "", NATIVE_ENGLISH, NATIVE_SPANISH, NATIVE_PORTUGUESE, NATIVE_FRENCH,
			NATIVE_ITALIAN, NATIVE_GERMAN, NATIVE_RUSSIAN, NATIVE_ARABIC, NATIVE_ROMANIAN, NATIVE_HEBREW, NATIVE_POLISH, NATIVE_INDONESIAN, NATIVE_FINNISH,
			NATIVE_URDU, NATIVE_FARSI_PERSIAN, NATIVE_CROATIAN, NATIVE_VIETNAMESE, NATIVE_CHINESE, NATIVE_DUTCH, NATIVE_GREEK, NATIVE_SWEDISH,
			NATIVE_BRAZILIAN_PORTUGUESE, NATIVE_CZECH, NATIVE_HUNGARIAN, NATIVE_TURKISH, NATIVE_KOREAN, NATIVE_NORWEGIAN, NATIVE_SERBIAN, NATIVE_DANISH,
			NATIVE_BULGARIAN, NATIVE_THAI, NATIVE_MALAY, NATIVE_BENGALI, NATIVE_JAPANESE, NATIVE_SLOVENIAN, NATIVE_MACEDONIAN, NATIVE_ALBANIAN, NATIVE_BOSNIAN,
			NATIVE_SLOVAK, NATIVE_ESTONIAN, NATIVE_LATVIAN, NATIVE_UKRAINIAN, NATIVE_GALICIAN, NATIVE_CATALAN, NATIVE_GEORGIAN, NATIVE_LITHUANIAN,
			NATIVE_SINHALA };

	private static final HashMap<String, String> LANGUAGE_BY_NATIVE_LANGUAGE = new HashMap<String, String>();
	private static final HashMap<String, String> ISO_BY_LANGUAGE = new HashMap<String, String>();

	static {
		LANGUAGE_BY_NATIVE_LANGUAGE.put(ENGLISH, NATIVE_ENGLISH);
		LANGUAGE_BY_NATIVE_LANGUAGE.put(SPANISH, NATIVE_SPANISH);
		LANGUAGE_BY_NATIVE_LANGUAGE.put(PORTUGUESE, NATIVE_PORTUGUESE);
		LANGUAGE_BY_NATIVE_LANGUAGE.put(FRENCH, NATIVE_FRENCH);
		LANGUAGE_BY_NATIVE_LANGUAGE.put(ITALIAN, NATIVE_ITALIAN);
		LANGUAGE_BY_NATIVE_LANGUAGE.put(GERMAN, NATIVE_GERMAN);
		LANGUAGE_BY_NATIVE_LANGUAGE.put(RUSSIAN, NATIVE_RUSSIAN);
		LANGUAGE_BY_NATIVE_LANGUAGE.put(ARABIC, NATIVE_ARABIC);
		LANGUAGE_BY_NATIVE_LANGUAGE.put(ROMANIAN, NATIVE_ROMANIAN);
		LANGUAGE_BY_NATIVE_LANGUAGE.put(HEBREW, NATIVE_HEBREW);
		LANGUAGE_BY_NATIVE_LANGUAGE.put(POLISH, NATIVE_POLISH);
		LANGUAGE_BY_NATIVE_LANGUAGE.put(INDONESIAN, NATIVE_INDONESIAN);
		LANGUAGE_BY_NATIVE_LANGUAGE.put(FINNISH, NATIVE_FINNISH);
		LANGUAGE_BY_NATIVE_LANGUAGE.put(URDU, NATIVE_URDU);
		LANGUAGE_BY_NATIVE_LANGUAGE.put(FARSI_PERSIAN, NATIVE_FARSI_PERSIAN);
		LANGUAGE_BY_NATIVE_LANGUAGE.put(CROATIAN, NATIVE_CROATIAN);
		LANGUAGE_BY_NATIVE_LANGUAGE.put(VIETNAMESE, NATIVE_VIETNAMESE);
		LANGUAGE_BY_NATIVE_LANGUAGE.put(CHINESE, NATIVE_CHINESE);
		LANGUAGE_BY_NATIVE_LANGUAGE.put(DUTCH, NATIVE_DUTCH);
		LANGUAGE_BY_NATIVE_LANGUAGE.put(GREEK, NATIVE_GREEK);
		LANGUAGE_BY_NATIVE_LANGUAGE.put(SWEDISH, NATIVE_SWEDISH);
		LANGUAGE_BY_NATIVE_LANGUAGE.put(BRAZILIAN_PORTUGUESE, NATIVE_BRAZILIAN_PORTUGUESE);
		LANGUAGE_BY_NATIVE_LANGUAGE.put(CZECH, NATIVE_CZECH);
		LANGUAGE_BY_NATIVE_LANGUAGE.put(HUNGARIAN, NATIVE_HUNGARIAN);
		LANGUAGE_BY_NATIVE_LANGUAGE.put(TURKISH, NATIVE_TURKISH);
		LANGUAGE_BY_NATIVE_LANGUAGE.put(KOREAN, NATIVE_KOREAN);
		LANGUAGE_BY_NATIVE_LANGUAGE.put(NORWEGIAN, NATIVE_NORWEGIAN);
		LANGUAGE_BY_NATIVE_LANGUAGE.put(SERBIAN, NATIVE_SERBIAN);
		LANGUAGE_BY_NATIVE_LANGUAGE.put(DANISH, NATIVE_DANISH);
		LANGUAGE_BY_NATIVE_LANGUAGE.put(BULGARIAN, NATIVE_BULGARIAN);
		LANGUAGE_BY_NATIVE_LANGUAGE.put(THAI, NATIVE_THAI);
		LANGUAGE_BY_NATIVE_LANGUAGE.put(MALAY, NATIVE_MALAY);
		LANGUAGE_BY_NATIVE_LANGUAGE.put(BENGALI, NATIVE_BENGALI);
		LANGUAGE_BY_NATIVE_LANGUAGE.put(JAPANESE, NATIVE_JAPANESE);
		LANGUAGE_BY_NATIVE_LANGUAGE.put(SLOVENIAN, NATIVE_SLOVENIAN);
		LANGUAGE_BY_NATIVE_LANGUAGE.put(MACEDONIAN, NATIVE_MACEDONIAN);
		LANGUAGE_BY_NATIVE_LANGUAGE.put(ALBANIAN, NATIVE_ALBANIAN);
		LANGUAGE_BY_NATIVE_LANGUAGE.put(BOSNIAN, NATIVE_BOSNIAN);
		LANGUAGE_BY_NATIVE_LANGUAGE.put(SLOVAK, NATIVE_SLOVAK);
		LANGUAGE_BY_NATIVE_LANGUAGE.put(ESTONIAN, NATIVE_ESTONIAN);
		LANGUAGE_BY_NATIVE_LANGUAGE.put(LATVIAN, NATIVE_LATVIAN);
		LANGUAGE_BY_NATIVE_LANGUAGE.put(UKRAINIAN, NATIVE_UKRAINIAN);
		LANGUAGE_BY_NATIVE_LANGUAGE.put(GALICIAN, NATIVE_GALICIAN);
		LANGUAGE_BY_NATIVE_LANGUAGE.put(CATALAN, NATIVE_CATALAN);
		LANGUAGE_BY_NATIVE_LANGUAGE.put(GEORGIAN, NATIVE_GEORGIAN);
		LANGUAGE_BY_NATIVE_LANGUAGE.put(LITHUANIAN, NATIVE_LITHUANIAN);
		LANGUAGE_BY_NATIVE_LANGUAGE.put(SINHALA, NATIVE_SINHALA);

		ISO_BY_LANGUAGE.put(ISO_ENGLISH, ENGLISH);
		ISO_BY_LANGUAGE.put(ISO_SPANISH, SPANISH);
		ISO_BY_LANGUAGE.put(ISO_PORTUGUESE, PORTUGUESE);
		ISO_BY_LANGUAGE.put(ISO_FRENCH, FRENCH);
		ISO_BY_LANGUAGE.put(ISO_ITALIAN, ITALIAN);
		ISO_BY_LANGUAGE.put(ISO_GERMAN, GERMAN);
		ISO_BY_LANGUAGE.put(ISO_RUSSIAN, RUSSIAN);
		ISO_BY_LANGUAGE.put(ISO_ARABIC, ARABIC);
		ISO_BY_LANGUAGE.put(ISO_ROMANIAN, ROMANIAN);
		ISO_BY_LANGUAGE.put(ISO_HEBREW_1, HEBREW);
		ISO_BY_LANGUAGE.put(ISO_HEBREW_2, HEBREW);
		ISO_BY_LANGUAGE.put(ISO_POLISH_1, POLISH);
		ISO_BY_LANGUAGE.put(ISO_POLISH_2, POLISH);
		ISO_BY_LANGUAGE.put(ISO_INDONESIAN, INDONESIAN);
		ISO_BY_LANGUAGE.put(ISO_FINNISH, FINNISH);
		ISO_BY_LANGUAGE.put(ISO_URDU, URDU);
		ISO_BY_LANGUAGE.put(ISO_FARSI_PERSIAN, FARSI_PERSIAN);
		ISO_BY_LANGUAGE.put(ISO_CROATIAN, CROATIAN);
		ISO_BY_LANGUAGE.put(ISO_VIETNAMESE, VIETNAMESE);
		ISO_BY_LANGUAGE.put(ISO_CHINESE, CHINESE);
		ISO_BY_LANGUAGE.put(ISO_DUTCH, DUTCH);
		ISO_BY_LANGUAGE.put(ISO_GREEK, GREEK);
		ISO_BY_LANGUAGE.put(ISO_SWEDISH, SWEDISH);
		ISO_BY_LANGUAGE.put(ISO_BRAZILIAN_PORTUGUESE_1, BRAZILIAN_PORTUGUESE);
		ISO_BY_LANGUAGE.put(ISO_BRAZILIAN_PORTUGUESE_2, BRAZILIAN_PORTUGUESE);
		ISO_BY_LANGUAGE.put(ISO_CZECH, CZECH);
		ISO_BY_LANGUAGE.put(ISO_HUNGARIAN, HUNGARIAN);
		ISO_BY_LANGUAGE.put(ISO_TURKISH, TURKISH);
		ISO_BY_LANGUAGE.put(ISO_KOREAN, KOREAN);
		ISO_BY_LANGUAGE.put(ISO_NORWEGIAN, NORWEGIAN);
		ISO_BY_LANGUAGE.put(ISO_SERBIAN, SERBIAN);
		ISO_BY_LANGUAGE.put(ISO_DANISH, DANISH);
		ISO_BY_LANGUAGE.put(ISO_BULGARIAN, BULGARIAN);
		ISO_BY_LANGUAGE.put(ISO_THAI, THAI);
		ISO_BY_LANGUAGE.put(ISO_MALAY, MALAY);
		ISO_BY_LANGUAGE.put(ISO_BENGALI, BENGALI);
		ISO_BY_LANGUAGE.put(ISO_JAPANESE, JAPANESE);
		ISO_BY_LANGUAGE.put(ISO_SLOVENIAN, SLOVENIAN);
		ISO_BY_LANGUAGE.put(ISO_MACEDONIAN, MACEDONIAN);
		ISO_BY_LANGUAGE.put(ISO_ALBANIAN, ALBANIAN);
		ISO_BY_LANGUAGE.put(ISO_BOSNIAN, BOSNIAN);
		ISO_BY_LANGUAGE.put(ISO_SLOVAK, SLOVAK);
		ISO_BY_LANGUAGE.put(ISO_ESTONIAN, ESTONIAN);
		ISO_BY_LANGUAGE.put(ISO_LATVIAN, LATVIAN);
		ISO_BY_LANGUAGE.put(ISO_UKRAINIAN, UKRAINIAN);
		ISO_BY_LANGUAGE.put(ISO_GALICIAN, GALICIAN);
		ISO_BY_LANGUAGE.put(ISO_CATALAN, CATALAN);
		ISO_BY_LANGUAGE.put(ISO_GEORGIAN, GEORGIAN);
		ISO_BY_LANGUAGE.put(ISO_LITHUANIAN, LITHUANIAN);
		ISO_BY_LANGUAGE.put(ISO_SINHALA, SINHALA);
	}

	public static String languageToNativeLanguage(String lang) {
		lang = lang.toLowerCase();
		if (LANGUAGE_BY_NATIVE_LANGUAGE.containsKey(lang)) {
			return LANGUAGE_BY_NATIVE_LANGUAGE.get(lang);
		}

		return lang;
	}

	public static String isoToLanguage(String iso) {
		iso = iso.toLowerCase();
		if (ISO_BY_LANGUAGE.containsKey(iso)) {
			return ISO_BY_LANGUAGE.get(iso);
		}

		return iso;
	}

	public static String isoToNativeLanguage(String iso) {
		iso = iso.toLowerCase();
		return languageToNativeLanguage(isoToLanguage(iso));
	}

	public static String getInterfaceSupportedIso(String iso) {
		iso = iso.toLowerCase();
		for (String interfaceIso : INTERFACE_ISO_LANGUAGES) {
			if (interfaceIso.equals(iso)) {
				return iso;
			}
		}

		return ISO_ENGLISH;
	}

}
package marauroa.common.i18n;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * internationalization support
 *
 * @author hendrik
 */
public class I18N {
	private static ThreadLocal<Locale> threadLocale = new ThreadLocal<Locale>();
	private static Map<String, Map<String, String>> dictionaries = new HashMap<String, Map<String, String>>();

	/**
	 * sets the locale for this thread
	 *
	 * @param locale locale to set
	 */
	public static void setThreadLocale(Locale locale) {
		threadLocale.set(locale);
	}

	/**
	 * resets the locale for this thread
	 */
	public static void resetThreadLocale() {
		threadLocale.set(Locale.ENGLISH);
	}

	/**
	 * gets the locale for this thread
	 *
	 * @return locale
	 */
	public static Locale getLocale() {
		Locale res = threadLocale.get();
		if (res == null) {
			res = Locale.ENGLISH;
		}
		return res;
	}

	/**
	 * translates the text to the thread language
	 *
	 * @param key  text to translate
	 * @param args optional arguments
	 * @return     translated text
	 */
	public static String _(String key, Object... args) {
		Locale locale = getLocale();
		String value = null;
		Map<String, String> dictionary = dictionaries.get(locale.getLanguage());
		if (dictionary != null) {
			value = dictionary.get(key);
		}
		if (value == null) {
			value = key;
		}
		return String.format(locale, value, args);
	}
}

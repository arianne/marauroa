package marauroa.common.i18n;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import marauroa.common.Log4J;

/**
 * internationalization support
 *
 * @author hendrik
 */
public class I18N {
	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(I18N.class);

	private static ThreadLocal<Locale> threadLocale = new ThreadLocal<Locale>();
	private static Map<String, Map<String, String>> dictionaries = new HashMap<String, Map<String, String>>();
	private static Locale defaultLocale = Locale.ENGLISH;
	
	static {
		addDictionaryFolder(I18N.class.getPackage().getName().replace('.', '/'));
	}

	/**
	 * initialize the I18N system
	 *
	 * @param locale default locale
	 */
	public static void init(Locale locale) {
		I18N.defaultLocale = locale;
	}

	/**
	 * adds a dictionary folder, so that games can make use of the same translation meachnism
	 *
	 * @param folder classpath folder to add
	 */
	public static void addDictionaryFolder(String folder) {
		for (String language : Locale.getISOLanguages()) {
			InputStream is = I18N.class.getClassLoader().getResourceAsStream(folder + "/" + language + ".txt");
			if (is != null) {
				Map<String, String> dictionary = dictionaries.get(language);
				if (dictionary == null) {
					dictionary = new HashMap<String, String>();
					dictionaries.put(language, dictionary);
				}

				readFile(is, dictionary);
			}
		}
	}

	/**
	 * reads a file into a map
	 *
	 * @param is InputStream
	 * @param resultMap content is store in this map
	 */
	private static void readFile(InputStream is, Map<String, String> resultMap) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String line = reader.readLine();
			while (line != null) {
				int pos = line.indexOf("=");
				if (pos > -1) {
					String key = line.substring(0, pos);
					String value = line.substring(pos + 1);
					resultMap.put(key, value);
				}
				line = reader.readLine();
			}
		} catch (IOException e) {
			logger.error(e, e);
		}
		if (reader != null) {
			try {
				reader.close();
			} catch (IOException e) {
				logger.error(e, e);
			}
		}
	}

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
		threadLocale.set(defaultLocale);
	}

	/**
	 * gets the locale for this thread
	 *
	 * @return locale
	 */
	public static Locale getLocale() {
		Locale res = threadLocale.get();
		if (res == null) {
			res = defaultLocale;
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

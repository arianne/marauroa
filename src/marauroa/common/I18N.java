package marauroa.common;

import java.util.Locale;

/**
 * internationalization support
 *
 * @author hendrik
 */
public class I18N {
	private static ThreadLocal<Locale> threadLocale = new ThreadLocal<Locale>();

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

}

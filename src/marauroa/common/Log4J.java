/*
 * Log4J.java
 *
 * Created on 6. Juli 2005, 19:42
 */

package marauroa.common;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

/**
 * This is a convenience class for initializing log4j
 * 
 * @author Matthias Totz
 */
public class Log4J {
	private static final String LOG4J_PROPERTIES = "log4j.properties";

	/** default properties */
	private static final String DEFAULT_PROPERTIES = "log4j.rootLogger=INFO, Console\n"
			+ "log4j.appender.Console=org.apache.log4j.ConsoleAppender\n"
			+ "log4j.appender.Console.layout=org.apache.log4j.PatternLayout\n"
			+ "log4j.appender.Console.threshold=INFO\n"
			+ "log4j.appender.Console.layout.ConversionPattern=%-4r [%t] %-5p %c %x - %m%n\n";

	/** flag indicating a successful configuration */
	private static boolean configured = false;

	/** initializes log4j with the log4j.properties file. */
	public static void init() {
		init(LOG4J_PROPERTIES);
	}

	/** initializes log4j with a custom properties file. */
	public static void init(String filename) {
		if (configured) {
			return;
		}

		InputStream propsFile = Log4J.class.getClassLoader().getResourceAsStream(filename);
		try {
			Properties props = new Properties();
			if (propsFile == null) {
				System.err.println("Cannot find " + filename+ " in classpath. Using default properties.");
				props.load(new ByteArrayInputStream(DEFAULT_PROPERTIES.getBytes()));
			} else {
				System.out.println("Configuring Log4J using " + filename);
				props.load(propsFile);
			}
			PropertyConfigurator.configure(props);
			configured = true;
		} catch (IOException ioe) {
			System.err.println("cannot read property-file " + LOG4J_PROPERTIES+ " because " + ioe.getMessage());
		}
	}

	/**
	 * returns a logger for the given class. Use this function instead of
	 * <code>Logger.getLogger(clazz);</code>. If the logging mechanism
	 * changes it will be done here and not in every class using a logger.
	 * 
	 * @param clazz
	 *            the Class requesting a logger
	 * @return the logger instance
	 */
	public static marauroa.common.Logger getLogger(Class clazz) {
		return new marauroa.common.Logger(clazz);
	}
}

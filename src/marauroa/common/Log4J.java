/*
 * Log4J.java
 *
 * Created on 6. Juli 2005, 19:42
 */

package marauroa.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * This is a convenience class for initializing log4j
 *
 * @author Matthias Totz
 */
public class Log4J
{
  private static final String LOG4J_PROPERTIES = "marauroa/log4j.properties";
  /** default properties */
  private static final String DEFAULT_PROPERTIES =
                "log4j.rootLogger=INFO, Console\n"+
                "log4j.appender.Console=org.apache.log4j.ConsoleAppender\n"+
                "log4j.appender.Console.layout=org.apache.log4j.PatternLayout\n"+
                "log4j.appender.Console.layout.ConversionPattern=%-4r [%t] %-5p %c %x - %m%n\n";

  /** initializes log4j with the log4j.properties file. */
  public static void init()
  {
    InputStream propsFile = Log4J.class.getClassLoader().getResourceAsStream(LOG4J_PROPERTIES );
    
    if (propsFile == null)
    {
      System.err.println("Cannot find "+LOG4J_PROPERTIES+" in classpath. Using default properties.");
      PropertyConfigurator.configure(DEFAULT_PROPERTIES);
    }
    else
    {
      Properties props = new Properties();
      try
      {
        props.load(propsFile);
        PropertyConfigurator.configure(props);
      }
      catch (IOException ioe)
      {
        System.err.println("cannot read property-file "+LOG4J_PROPERTIES+" because "+ioe.getMessage());
      }
    }
  }
  
  /**
   * returns a logger for the given class. Use this function instead of
   * <code>Logger.getLogger(clazz);</code>. If the logging mechanism changes
   * it will be done here and not in every class using a logger.
   */
  public static Logger getLogger(Class clazz)
  {
    return Logger.getLogger(clazz);
  }
}

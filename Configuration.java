package marauroa;

import marauroa.*;
import java.util.Properties;
import java.io.*;

public class Configuration
  {
  private static String configurationFile="marauroa.ini";
  
  private Properties properties;
  private static Configuration configuration=null;
  
  static class PropertyNotFoundException extends Throwable
    {
    PropertyNotFoundException(String property)
      {
      super("Property ["+property+"] not found");
      }
    }
    
  private Configuration() 
    {
    try
      {
      properties=new Properties();
      properties.load(new FileInputStream(configurationFile));
      }
    catch(FileNotFoundException e)
      {
      marauroad.trace("Configuration","X","Configuration file not found: "+e.getMessage());
      System.exit(-1);
      }
    catch(IOException e)
      {
      marauroad.trace("Configuration","X","Error loading Configuration file: "+e.getMessage());
      System.exit(-1);
      }
    }    
    
  public static Configuration getConfiguration()
    {
    if(configuration==null)
      {
      configuration=new Configuration();
      }
      
    return configuration;
    }
  }
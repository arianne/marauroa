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
    
  public String get(String property) throws PropertyNotFoundException
    {
    String result=properties.getProperty(property);
    
    if(result==null)
      {
      throw new PropertyNotFoundException(property);
      }
      
    return result;
    }
    
  public void set(String property, String value)
    {
    properties.put(property,value);
    }
  
  public void store()
    {
    try
      {
      properties.store(new FileOutputStream(configurationFile),"Marauroa Configuration file");
      }
    catch(FileNotFoundException e)
      {
      marauroad.trace("Configuration::store","X","Configuration file not found: "+e.getMessage());
      System.exit(-1);
      }
    catch(IOException e)
      {
      marauroad.trace("Configuration::store","X","Error loading Configuration file: "+e.getMessage());
      System.exit(-1);
      }
    }
  }
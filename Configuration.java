package marauroa;

import marauroa.*;
import java.util.Properties;
import java.io.*;

public class Configuration
  {
  private static String configurationFile="marauroa.ini";
  
  private Properties properties;
  private static Configuration configuration=null;
  
  public static class PropertyNotFoundException extends Throwable
    {
    PropertyNotFoundException(String property)
      {
      super("Property ["+property+"] not found");
      }
    }
    
  public static class PropertyFileNotFoundException extends Throwable
    {
    private String message;
    PropertyFileNotFoundException()
      {
      super();
      String file=getClass().getClassLoader().getResource(configurationFile).getPath();
      message="Property File ["+file+"] not found";
      }
    
    public String getMessage()
      {
      return message;
      }     
    }

  private Configuration() throws PropertyFileNotFoundException
    {
    try
      {
      properties=new Properties();
      String file=getClass().getClassLoader().getResource(configurationFile).getPath();
      properties.load(new FileInputStream(file));
      }
    catch(FileNotFoundException e)
      {
      marauroad.trace("Configuration","X","Configuration file not found: "+e.getMessage());
      throw new PropertyFileNotFoundException();
      }
    catch(IOException e)
      {
      marauroad.trace("Configuration","X","Error loading Configuration file: "+e.getMessage());
      throw new PropertyFileNotFoundException();
      }
    }    
    
  public static Configuration getConfiguration() throws PropertyFileNotFoundException
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
  
  public void store() throws PropertyFileNotFoundException
    {
    try
      {
      String file=getClass().getClassLoader().getResource(configurationFile).getPath();
      properties.store(new FileOutputStream(file),"Marauroa Configuration file");
      }
    catch(FileNotFoundException e)
      {
      marauroad.trace("Configuration::store","X","Configuration file not found: "+e.getMessage());
      throw new PropertyFileNotFoundException();
      }
    catch(IOException e)
      {
      marauroad.trace("Configuration::store","X","Error loading Configuration file: "+e.getMessage());
      throw new PropertyFileNotFoundException();
      }
    }
  }
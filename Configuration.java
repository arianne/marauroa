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
    marauroad.trace("Configuration",">");
    try
      {
      properties=new Properties();
      String file=getClass().getClassLoader().getResource(configurationFile).getPath();
      properties.load(new FileInputStream(file));
      }
    catch(FileNotFoundException e)
      {
      marauroad.trace("Configuration","X","Configuration file not found: "+e.getMessage());
      marauroad.trace("Configuration","<");
      throw new PropertyFileNotFoundException();
      }
    catch(IOException e)
      {
      marauroad.trace("Configuration","X","Error loading Configuration file: "+e.getMessage());
      marauroad.trace("Configuration","<");
      throw new PropertyFileNotFoundException();
      }

    marauroad.trace("Configuration","<");
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
    marauroad.trace("Configuration::get",">");
    String result=properties.getProperty(property);
    
    if(result==null)
      {
      marauroad.trace("Configuration::get","<");
      throw new PropertyNotFoundException(property);
      }
      
    marauroad.trace("Configuration::get","<");
    return result;
    }
    
  public void set(String property, String value)
    {
    marauroad.trace("Configuration::set",">");
    properties.put(property,value);
    marauroad.trace("Configuration::set","<");
    }
  
  public void store() throws PropertyFileNotFoundException
    {
    marauroad.trace("Configuration::store",">");
    try
      {
      String file=getClass().getClassLoader().getResource(configurationFile).getPath();
      properties.store(new FileOutputStream(file),"Marauroa Configuration file");
      }
    catch(FileNotFoundException e)
      {
      marauroad.trace("Configuration::store","X","Configuration file not found: "+e.getMessage());
      marauroad.trace("Configuration::store","<");
      throw new PropertyFileNotFoundException();
      }
    catch(IOException e)
      {
      marauroad.trace("Configuration::store","X","Error loading Configuration file: "+e.getMessage());
      marauroad.trace("Configuration::store","<");
      throw new PropertyFileNotFoundException();
      }

    marauroad.trace("Configuration::store","<");
    }
  }
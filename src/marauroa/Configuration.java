/* $Id: Configuration.java,v 1.13 2004/07/07 10:07:04 arianne_rpg Exp $ */
/***************************************************************************
 *                      (C) Copyright 2003 - Marauroa                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa;

import marauroa.*;
import java.util.Properties;
import java.util.Enumeration;
import java.io.*;

/** This class is a basic configuration file manager */
public class Configuration
  {
  private static String configurationFile="marauroa.ini";
  private Properties properties;
  private static Configuration configuration=null;
  
  /** This method defines the default configuration file for all the instances 
   *  of Configuration
   *  @param conf the location of the file */
  public static void setConfigurationFile(String conf)
    {
    configurationFile=conf;
    }  
  
  /** This exception is thrown when a propierty of the file is not found */  
  public static class PropertyNotFoundException extends Exception
    {
    PropertyNotFoundException(String property)
      {
      super("Property ["+property+"] not found");
      }
    }

  /** This exception is thrown when the file is not found */  
  public static class PropertyFileNotFoundException extends Exception
    {
    private String message;
    PropertyFileNotFoundException()
      {
      super();
      message="Property File ["+configurationFile+"] not found";
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

      InputStream is = new FileInputStream(configurationFile);
      properties.load(is);
      }
    catch(FileNotFoundException e)
      {
      marauroad.trace("Configuration","X","Configuration file not found: "+configurationFile);
      marauroad.thrown("Configuration","X",e);
      throw new PropertyFileNotFoundException();
      }
    catch(IOException e)
      {
      marauroad.trace("Configuration","X","Error loading Configuration file");
      marauroad.thrown("Configuration","X",e);
      throw new PropertyFileNotFoundException();
      }
    finally
      {
      marauroad.trace("Configuration","<");
      }
    }
    
  /** This method returns an instance of Configuration 
   *  @return a shared instance of Configuration */
  public static Configuration getConfiguration() throws PropertyFileNotFoundException
    {
    marauroad.trace("Configuration::getConfiguration",">");
    try
      {
      if(configuration==null)
        {
        configuration=new Configuration();
        }
      return configuration;
      }
    finally
      {
      marauroad.trace("Configuration::getConfiguration","<");
      }
    }
    
  /** This method returns a String with the value of the property.
   *  @param property the property we want the value
   *  @return a string containing the value of the propierty
   *  @exception PropertyNotFound if the property is not found. */
  public String get(String property) throws PropertyNotFoundException
    {
    marauroad.trace("Configuration::get",">");
    try
      {
      String result=properties.getProperty(property);
    
      if(result==null)
        {
        marauroad.trace("Configuration::get","X","Property ["+property+"] not found");
        throw new PropertyNotFoundException(property);
        }
      marauroad.trace("Configuration::get","D","Property ["+property+"]="+result);
      return result;
      }
    finally
      {
      marauroad.trace("Configuration::get","<");
      }
    }
    
  /** This method set a property with the value we pass as parameter
   *  @param property the property we want to set the value
   *  @param value the value to set */
  public void set(String property, String value)
    {
    marauroad.trace("Configuration::set",">");
    try
      {
      marauroad.trace("Configuration::set","D","Property ["+property+"]="+value);
      properties.put(property,value);
      }
    finally
      {
      marauroad.trace("Configuration::set","<");
      }
    }
  
  /** This method returns an enumeration of the propierties that the file contains */
  public Enumeration propertyNames()
    {
    return properties.propertyNames();
    }
  }

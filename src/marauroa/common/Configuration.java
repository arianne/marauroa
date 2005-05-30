/* $Id: Configuration.java,v 1.1 2005/05/30 08:13:38 arianne_rpg Exp $ */
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
package marauroa.common;

import java.util.*;
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
  
  private Configuration() throws FileNotFoundException
    {
    Logger.trace("Configuration",">");
    try
      {
      properties=new Properties();

      InputStream is = new FileInputStream(configurationFile);
      properties.load(is);
      }
    catch(FileNotFoundException e)
      {
      Logger.trace("Configuration","X","Configuration file not found: "+configurationFile);
      Logger.thrown("Configuration","X",e);
      throw e;
      }
    catch(IOException e)
      {
      Logger.trace("Configuration","X","Error loading Configuration file");
      Logger.thrown("Configuration","X",e);
      throw new FileNotFoundException(configurationFile);
      }
    finally
      {
      Logger.trace("Configuration","<");
      }
    }
  
  /** This method returns an instance of Configuration 
   *  @return a shared instance of Configuration */
  public static Configuration getConfiguration() throws FileNotFoundException
    {
    Logger.trace("Configuration::getConfiguration",">");
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
      Logger.trace("Configuration::getConfiguration","<");
      }
    }
    
  /** This method returns a String with the value of the property.
   *  @param property the property we want the value
   *  @return a string containing the value of the propierty
   *  @exception PropertyNotFound if the property is not found. */
  public String get(String property) throws PropertyNotFoundException
    {
    Logger.trace("Configuration::get",">");
    try
      {
      String result=properties.getProperty(property);
    
      if(result==null)
        {
        Logger.trace("Configuration::get","X","Property ["+property+"] not found");
        throw new PropertyNotFoundException(property);
        }
      Logger.trace("Configuration::get","D","Property ["+property+"]="+result);
      return result;
      }
    finally
      {
      Logger.trace("Configuration::get","<");
      }
    }
    
  /** This method set a property with the value we pass as parameter
   *  @param property the property we want to set the value
   *  @param value the value to set */
  public void set(String property, String value)
    {
    Logger.trace("Configuration::set",">");
    try
      {
      Logger.trace("Configuration::set","D","Property ["+property+"]="+value);
      properties.put(property,value);
      }
    finally
      {
      Logger.trace("Configuration::set","<");
      }
    }
  
  /** This method returns an enumeration of the propierties that the file contains */
  public Enumeration propertyNames()
    {
    return properties.propertyNames();
    }
  }

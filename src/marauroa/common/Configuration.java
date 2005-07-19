/* $Id: Configuration.java,v 1.3 2005/07/19 20:56:42 mtotz Exp $ */
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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Properties;

/** This class is a basic configuration file manager */
public class Configuration
  {
  /** the logger instance. */
  private static final org.apache.log4j.Logger logger = Log4J.getLogger(Configuration.class);
  
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
    Log4J.startMethod(logger,"Configuration");
    try
      {
      properties=new Properties();

      InputStream is = new FileInputStream(configurationFile);
      properties.load(is);
      is.close();
      }
    catch(FileNotFoundException e)
      {
      logger.warn("Configuration file not found: "+configurationFile,e);
      throw e;
      }
    catch(IOException e)
      {
      logger.warn("Error loading Configuration file",e);
      throw new FileNotFoundException(configurationFile);
      }
    finally
      {
      Log4J.finishMethod(logger,"Configuration");
      }
    }
  
  /** This method returns an instance of Configuration 
   *  @return a shared instance of Configuration */
  public static Configuration getConfiguration() throws FileNotFoundException
    {
    Log4J.startMethod(logger,"getConfiguration");
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
      Log4J.finishMethod(logger,"getConfiguration");
      }
    }
    
  /** This method returns a String with the value of the property.
   *  @param property the property we want the value
   *  @return a string containing the value of the propierty
   *  @exception PropertyNotFound if the property is not found. */
  public String get(String property) throws PropertyNotFoundException
    {
    Log4J.startMethod(logger,"get");
    try
      {
      String result=properties.getProperty(property);
    
      if(result==null)
        {
        logger.debug("Property ["+property+"] not found");
        throw new PropertyNotFoundException(property);
        }
        
      logger.debug("Property ["+property+"]="+result);
      return result;
      }
    finally
      {
      Log4J.finishMethod(logger,"get");
      }
    }

  public boolean has(String property)
    {
    Log4J.startMethod(logger,"has");
    try
      {
      String result=properties.getProperty(property);
    
      if(result==null)
        {
        return false;
        }
        
      return true;
      }
    finally
      {
      Log4J.finishMethod(logger,"has");
      }
    }
    
  /** This method set a property with the value we pass as parameter
   *  @param property the property we want to set the value
   *  @param value the value to set */
  public void set(String property, String value)
    {
    Log4J.startMethod(logger,"set");
    try
      {
      logger.debug("Property ["+property+"]="+value);
      properties.put(property,value);

      OutputStream os = new FileOutputStream(configurationFile);
      properties.store(os,null);
      os.close();
      }
    catch(FileNotFoundException e)
      {
      logger.error("Configuration file not found: "+configurationFile,e);
      }
    catch(IOException e)
      {
      logger.error("Error storing Configuration file",e);
      }
    finally
      {
      Log4J.finishMethod(logger,"set");
      }
    }
  
  /** This method returns an enumeration of the propierties that the file contains */
  public Enumeration propertyNames()
    {
    return properties.propertyNames();
    }
  }

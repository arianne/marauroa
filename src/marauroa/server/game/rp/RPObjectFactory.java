/* $Id: RPObjectFactory.java,v 1.11 2010/06/12 15:08:42 nhnb Exp $ */
/***************************************************************************
 *						(C) Copyright 2003 - Marauroa					   *
 ***************************************************************************
 ***************************************************************************
 *																		   *
 *	 This program is free software; you can redistribute it and/or modify  *
 *	 it under the terms of the GNU General Public License as published by  *
 *	 the Free Software Foundation; either version 2 of the License, or	   *
 *	 (at your option) any later version.								   *
 *																		   *
 ***************************************************************************/
package marauroa.server.game.rp;

import marauroa.common.Configuration;
import marauroa.common.Log4J;
import marauroa.common.game.RPObject;
import marauroa.server.game.db.DatabaseFactory;


/**
 * This class is a factory to convert RPObjects into real objects for your game.
 * This class is invoked by whatever that load an object into the server:
 * - JDBCDatabase.
 * 
 * To make it work on your game you need to subclass and implement:
 * 
 *   static RPObjectFactory getFactory()
 * 
 * The method will be called through refletions.
 * @author miguel
 *
 */
public class RPObjectFactory {
	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(DatabaseFactory.class);

	/**
	 * This method returns an instance of RPObjectFactory choosen using the
	 * Configuration file.
	 *
	 * @return A shared instance of RPObjectFactory
     * @throws NoFactoryConfException if the factory is not configured correctly 
	 */
	public static RPObjectFactory get() throws NoFactoryConfException {
		try {
	        Configuration conf = Configuration.getConfiguration();
	        String factoryName = conf.get("factory_implementation");
	        if (factoryName == null) {
	        	factoryName = RPObjectFactory.class.getName();
	        }

	        return get(factoryName);
        } catch (Exception e) {
        	logger.error("Can't create factory", e);
        	throw new NoFactoryConfException(e);
        }
	}

	/**
	 * This method returns an instance of RPObjectFactory chosen using the
	 * param.
	 *
	 * @param factoryName
	 *            A String containing the type of factory. It should be the
	 *            complete class name. ie: marauroa.server.game.rp.RPObjectFactory
	 *
	 * @return A shared instance of RPObjectFactory
	 * @throws NoFactoryConfException if the factory is not configured correctly 
	 */
	public static RPObjectFactory get(String factoryName) throws NoFactoryConfException {
		try {
	        Class<?> databaseClass = Class.forName(factoryName);
	        java.lang.reflect.Method singleton = databaseClass.getDeclaredMethod("getFactory");
	        return (RPObjectFactory) singleton.invoke(null);
        } catch (Exception e) {
        	logger.error("Can't create factory("+factoryName+")", e);
        	throw new NoFactoryConfException(e);
        }
	}
	
	private static RPObjectFactory singleton;

	/**
	 * gets the ObjectFactory, creating a default one in case it does not exist already
	 *
	 * @return RPObjectFactory
	 */
	public static RPObjectFactory getFactory() {
		if(singleton==null) {
			singleton=new RPObjectFactory();
		}
		
		return singleton;
	}

	/**
	 * This method is called when object is serialized back from database to
	 * zone, so you can define which subclass of RPObject we are going to use.
	 * This implements a factory pattern.
	 *
	 * If you are not interested in this feature, just return the object
	 *
	 * @param object
	 *            the original object
	 * @return the new instance of the object
	 */
	public RPObject transform(RPObject object) {
		return object;
	}
}

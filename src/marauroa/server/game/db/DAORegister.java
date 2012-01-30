/***************************************************************************
 *                   (C) Copyright 2003-2011 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.server.game.db;

import java.util.HashMap;
import java.util.Map;

import marauroa.server.game.rp.RPObjectFactory;

/**
 * <p>Keeps track of the DAOs (data access objects) to use. They are registered 
 * using their class name:<br>
 * <code>register(CharacterDAO.class, new CharacterDAO());</code></p>
 *
 * <p>Games can add their own DAOs. They can even replace framework DAOs
 * with their own subclasses. Stendhal for example enhances the normal 
 * CharacterDAO with its own subclass to update a redundant table used 
 * by the Stendhal website: <br>
 * <code>DAORegister.get().register(CharacterDAO.class, new StendhalCharacterDAO());</code></p>
 * 
 * <p>Game should register their DAOs in the "initialize()" method of their DatabaseFactory,
 * as defined in the server.ini: <br>
 * <code>database_implementation=games.demo.server.DemoDatabaseFactory</code>
 * </p>
 *
 * @author hendrik
 */
public class DAORegister {

	private Map<Class<?>, Object> register = new HashMap<Class<?>, Object>();
	private RPObjectFactory factory;
	private static DAORegister instance;

	private DAORegister() {
		// hide constructor, this is a Singleton
	}

	/**
	 * gets the singleton DAORegister instance
	 *
	 * @return DAORegister
	 */
	public static DAORegister get() {
		if (instance == null) {
			DAORegister myInstance = new DAORegister();
			myInstance.registerDAOs();
			instance = myInstance;
		}
		return instance;
	}


	/**
	 * registers a DAO
	 *
	 * @param <T>   type of DOA
	 * @param clazz class of DOA
	 * @param object instance of DOA
	 */
	public <T> void register(Class<T> clazz, T object) {
		register.put(clazz, object);
	}

	/**
	 * gets the instance for the requested DAO
	 *
	 * @param <T>   type of DAO
	 * @param clazz class of DAP
	 * @return instance of DOA
	 * @throws IllegalArgumentException in case there is no instance registered for the specified class
	 */
	@SuppressWarnings("unchecked")
	public <T> T get(Class<T> clazz) {
		T res = (T) register.get(clazz);
		if (res == null) {
			throw new IllegalArgumentException("No DAO registered for class " + clazz);
		}
		return res;
	}


	/**
	 * registers the core DAOs provided by marauroa itself.
	 */
	private void registerDAOs() {
		factory = RPObjectFactory.get();

		register(AccountDAO.class, new AccountDAO());
		register(BanListDAO.class, new BanListDAO());
		register(CharacterDAO.class, new CharacterDAO());
		register(GameEventDAO.class, new GameEventDAO());
		register(LoginEventDAO.class, new LoginEventDAO());
		register(LoginSeedDAO.class, new LoginSeedDAO());
		register(RPObjectDAO.class, new RPObjectDAO(factory));
		register(RPZoneDAO.class, new RPZoneDAO(factory));
		register(StatisticsDAO.class, new StatisticsDAO());
	}

	/**
	 * gets the RPObjectFactory
	 *
	 * @return RPObjectFactory
	 */
	public RPObjectFactory getRPObjectFactory() {
		return factory;
	}

}

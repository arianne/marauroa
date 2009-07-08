package marauroa.server.game.db;

import java.util.HashMap;
import java.util.Map;

import marauroa.server.game.rp.RPObjectFactory;

/**
 * registers data access objects, so that they can be overriden by a user of the framework.
 *
 * @author hendrik
 */
public class DAORegister {

	private Map<Class<?>, Object> register = new HashMap<Class<?>, Object>();
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
			instance = new DAORegister();
			instance.registerDAOs();
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
			throw new IllegalArgumentException("No DOA registered for class " + clazz);
		}
		return res;
	}


	/**
	 * registers the core DAOs provided by mararuoa itself.
	 */
	private void registerDAOs() {
		RPObjectFactory factory = RPObjectFactory.get();

		register(AccountDAO.class, new AccountDAO());
		register(CharacterDAO.class, new CharacterDAO());
		register(GameEventDAO.class, new GameEventDAO());
		register(LoginEventDAO.class, new LoginEventDAO());
		register(RPObjectDAO.class, new RPObjectDAO(factory));
		register(RPZoneDAO.class, new RPZoneDAO(factory));
		register(StatisticsDAO.class, new StatisticsDAO());
	}
}

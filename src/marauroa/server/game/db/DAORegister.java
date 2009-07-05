package marauroa.server.game.db;

import java.util.HashMap;
import java.util.Map;

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
}

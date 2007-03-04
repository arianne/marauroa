package marauroa.test;

import marauroa.server.game.rp.RPWorld;

public class TestRPWorld extends RPWorld{

	private static TestRPWorld world;
	
	/**
	 * This method MUST be implemented in other for marauroa to be able to load this World implementation.
	 * There is no way of enforcing static methods on a Interface, so just keep this in mind when 
	 * writting your own game.
	 *  
	 * @return an unique instance of world.
	 */
	public static RPWorld get() {
		if(world==null) {
			world = new TestRPWorld();
		}
		
		return world;
	}
}

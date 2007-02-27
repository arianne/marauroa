package marauroa.server.game.db.test;

import static org.junit.Assert.*;

import java.util.Properties;

import marauroa.common.Log4J;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;
import marauroa.server.game.db.JDBCDatabase;
import marauroa.server.game.db.JDBCTransaction;
import marauroa.server.game.rp.MarauroaRPZone;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This test unit test the load and store methods of database.
 * @author miguel
 *
 */
public class TestRPZoneAccess {
	/**
	 * JDBCDatabase can only be instantiated by DatabaseFactory, so we extend instead
	 * JDBC Database and create a proper public constructor.
	 * @author miguel
	 *
	 */
	static class TestJDBC extends JDBCDatabase {
		public TestJDBC(Properties props) {
			super(props);
		}
	}

	private static TestJDBC database;

	/**
	 * Setup one time the database.
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void createDatabase() throws Exception {
		Log4J.init("marauroa/server/log4j.properties");

		Properties props = new Properties();

		props.put("jdbc_url", "jdbc:mysql://127.0.0.1/marauroatest");
		props.put("jdbc_class", "com.mysql.jdbc.Driver");
		props.put("jdbc_user", "junittest");
		props.put("jdbc_pwd", "passwd");

		database=new TestJDBC(props);
	}

	/**
	 * Test the store and load methods of database by creating a zone and adding a object
	 * and then storing it for at a later stage load the zone from database into a new zone
	 * instance.
	 * @throws Exception
	 */
	@Test
	public void storeAndLoadObjects() throws Exception {
		RPObject obj=new RPObject();

		obj.put("a",1);
		obj.put("b","1");
		obj.put("c",2.0);
		obj.put("d","string of text");

		obj.addSlot("lhand");
		obj.addSlot("rhand");

		obj.addEvent("chat", "Hi there!");
		obj.addEvent("chat", "Does this work?");

		RPSlot lhand=obj.getSlot("lhand");

		RPObject pocket=new RPObject();
		pocket.put("size", 1);
		pocket.addSlot("container");
		lhand.add(pocket);

		RPSlot container=pocket.getSlot("container");

		RPObject coin=new RPObject();
		coin.put("euro", 100);
		coin.put("value", 100);
		container.add(coin);

		MarauroaRPZone zone=new MarauroaRPZone("test");

		/* Define the object as storable */
		obj.store();

		zone.assignRPObjectID(obj);
		zone.add(obj);

		JDBCTransaction transaction=database.getTransaction();

		try {
			transaction.begin();

			database.storeRPZone(transaction, zone);

			MarauroaRPZone newzone=new MarauroaRPZone("test");
			database.loadRPZone(transaction, newzone);

			RPObject.ID id=new RPObject.ID(1,"test");
			assertEquals(zone.get(id),newzone.get(id));
		} finally {
			transaction.rollback();
		}
	}
}

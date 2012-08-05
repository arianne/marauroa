/***************************************************************************
 *                   (C) Copyright 2003-2009 - Marauroa                    *
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

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import marauroa.common.Log4J;
import marauroa.common.game.RPClassTestHelper;
import marauroa.common.game.RPEvent;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;
import marauroa.server.db.DBTransaction;
import marauroa.server.db.TransactionPool;
import marauroa.server.game.rp.MarauroaRPZone;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This test unit test the load and store methods of rpzoneDAO.
 *
 * @author miguel
 *
 */
public class RPZoneAccessTest {

	private static TransactionPool transactionPool;
	private static RPZoneDAO rpzoneDAO;

	private RPObject object;
	private MarauroaRPZone zone;

	/**
	 * Setup one time the rpzoneDAO.
	 *
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
		props.put("database_adapter", "marauroa.server.db.adapter.MySQLDatabaseAdapter");

		transactionPool = new TransactionPool(props);
		rpzoneDAO = DAORegister.get().get(RPZoneDAO.class);
	}

	/**
	 * Setup one time the rpzoneDAO.
	 *
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void closeDatabase() throws Exception {
		transactionPool.close();
	}
	/**
	 * Populates the zone with some objects.
	 *
	 */
	@Before
	public void populateZone() {
		object = new RPObject();
		object.put("a", 1);
		object.put("b", "1");
		object.put("c", 2.0);
		object.put("d", "string of text");

		object.addSlot("lhand");
		object.addSlot("rhand");

		RPClassTestHelper.generateRPClasses();
		RPEvent chat = new RPEvent("chat");
		chat.put("text", "Hi there");
		object.addEvent(chat);

		chat = new RPEvent("chat");
		chat.put("text", "Does this work?");
		object.addEvent(chat);

		RPSlot lhand = object.getSlot("lhand");

		RPObject pocket = new RPObject();
		pocket.put("size", 1);
		pocket.addSlot("container");
		lhand.add(pocket);

		RPSlot container = pocket.getSlot("container");

		RPObject coin = new RPObject();
		coin.put("euro", 100);
		coin.put("value", 100);
		container.add(coin);

		zone = new MarauroaRPZone("test");
		/* Define the object as storable */
		object.store();

		zone.assignRPObjectID(object);
		zone.add(object);
	}

	/**
	 * Test the store and load methods of database by creating a zone and adding
	 * a object and then storing it for at a later stage load the zone from
	 * database into a new zone instance.
	 *
	 * @throws Exception
	 */
	@Test
	public void storeAndLoadObjects() throws Exception {
		DBTransaction transaction = transactionPool.beginWork();

		try {
			rpzoneDAO.storeRPZone(transaction, zone);

			MarauroaRPZone newzone = new MarauroaRPZone("test");
			rpzoneDAO.loadRPZone(transaction, newzone);

			RPObject.ID id = new RPObject.ID(1, "test");
			assertEquals(zone.get(id), newzone.get(id));
		} finally {
			transactionPool.rollback(transaction);
		}
	}

	/**
	 * Test the store and load methods of database by creating a zone and adding
	 * a object and then storing it for at a later stage load the zone from
	 * database into a new zone instance and repeating the operation a second
	 * time ( to test database update ).
	 *
	 * @throws Exception
	 */
	@Test
	public void storeAndStoreAndLoadObjects() throws Exception {
		DBTransaction transaction = transactionPool.beginWork();

		try {
			rpzoneDAO.storeRPZone(transaction, zone);

			MarauroaRPZone newzone = new MarauroaRPZone("test");
			rpzoneDAO.loadRPZone(transaction, newzone);

			RPObject.ID id = new RPObject.ID(1, "test");
			assertEquals(zone.get(id), newzone.get(id));

			rpzoneDAO.storeRPZone(transaction, newzone);

			MarauroaRPZone doublestoredzone = new MarauroaRPZone("test");
			rpzoneDAO.loadRPZone(transaction, doublestoredzone);

			id = new RPObject.ID(1, "test");
			assertEquals(zone.get(id), doublestoredzone.get(id));
		} finally {
			transactionPool.rollback(transaction);
		}
	}
}

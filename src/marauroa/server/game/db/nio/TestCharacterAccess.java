/**
 * 
 */
package marauroa.server.game.db.nio;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import marauroa.common.Log4J;
import marauroa.common.crypto.Hash;
import marauroa.common.game.RPObject;
import marauroa.server.game.db.JDBCTransaction;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author miguel
 *
 */
public class TestCharacterAccess {

	static class TestJDBC extends NIOJDBCDatabase {
		public TestJDBC(Properties props) {
			super(props);			
		}
	}

	private static TestJDBC database;

	/**
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

	@Test
	public void addCharacter() throws SQLException, IOException {
		String username="testUser";
		String character="testCharacter";
		RPObject player=new RPObject();

		JDBCTransaction transaction=database.getTransaction();
		try {
			transaction.begin();		
			database.addPlayer(transaction, username, Hash.hash("testPassword"), "email@email.com");
			assertTrue(database.hasPlayer(transaction, username));
			database.addCharacter(transaction, username, character, player);
			assertTrue(database.hasCharacter(transaction, username, character));
		} finally {
			transaction.rollback();
		}
	}

	@Test(expected=SQLException.class) 
	public void doubleAddedCharacter() throws SQLException, IOException {
		String username="testUser";
		String character="testCharacter";
		RPObject player=new RPObject();

		JDBCTransaction transaction=database.getTransaction();			
		try{
			transaction.begin();

			database.addPlayer(transaction, username, Hash.hash("testPassword"), "email@email.com");
			assertTrue(database.hasPlayer(transaction, username));
			database.addCharacter(transaction, username, character, player);
			assertTrue(database.hasCharacter(transaction, username, character));
			database.addCharacter(transaction, username, character, player);

			fail("Character was added");
		} finally {
			transaction.rollback();
		}
	}

	@Test
	public void removeCharacter() throws SQLException, IOException {
		String username="testUser";
		String character="testCharacter";
		RPObject player=new RPObject();

		JDBCTransaction transaction=database.getTransaction();
		try {
			transaction.begin();		
			database.addPlayer(transaction, username, Hash.hash("testPassword"), "email@email.com");
			assertTrue(database.hasPlayer(transaction, username));
			database.addCharacter(transaction, username, character, player);
			assertTrue(database.hasCharacter(transaction, username, character));
			database.removeCharacter(transaction, username, character);
			assertFalse(database.hasPlayer(transaction, username));
		} finally {
			transaction.rollback();
		}
	}
}

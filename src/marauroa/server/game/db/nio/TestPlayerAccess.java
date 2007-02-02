/**
 * 
 */
package marauroa.server.game.db.nio;


import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.Properties;

import marauroa.common.Log4J;
import marauroa.common.crypto.Hash;
import marauroa.server.game.db.JDBCTransaction;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author miguel
 *
 */
public class TestPlayerAccess {
	
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
	public static void setUpBeforeClass() throws Exception {
		Log4J.init("marauroa/server/log4j.properties");
		
		Properties props = new Properties();

		props.put("jdbc_url", "jdbc:mysql://127.0.0.1/marauroatest");
		props.put("jdbc_class", "com.mysql.jdbc.Driver");
		props.put("jdbc_user", "junittest");
		props.put("jdbc_pwd", "passwd");

		database=new TestJDBC(props);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		database=null;
	}
	
	@Test
	public void addPlayer() throws SQLException {
		String username="testUser";

		JDBCTransaction transaction=database.getTransaction();
		try {
			transaction.begin();		
			database.addPlayer(transaction, username, Hash.hash("testPassword"), "email@email.com");
			assertTrue(database.hasPlayer(transaction, username));
		} finally {
			transaction.rollback();
		}
	}

	@Test(expected=SQLException.class) 
	public void doubleAddedPlayer() throws SQLException {
		String username="testUser";
		
		JDBCTransaction transaction=database.getTransaction();			
		try{
			transaction.begin();
			
			if(database.hasPlayer(transaction, username)) {
				fail("Player was not expected");
			}
			database.addPlayer(transaction, username, Hash.hash("testPassword"), "email@email.com");

			if(!database.hasPlayer(transaction, username)) {
				fail("Player was expected");
			}
			database.addPlayer(transaction, username, Hash.hash("testPassword"), "email@email.com");
			
			fail("Player was added");
		} finally {
			transaction.rollback();
		}
	}

	@Test
	public void getStatus() throws SQLException {
		String username="testUser";
		
		JDBCTransaction transaction=database.getTransaction();

		try {
			transaction.begin();
			database.addPlayer(transaction, username, Hash.hash("testPassword"), "email@email.com");
			assertEquals("active",database.getAccountStatus(transaction, username));
		} finally {
			transaction.rollback();
		}
	}
}

package marauroa;

import static org.junit.Assert.fail;

import java.util.Properties;

import marauroa.common.Log4J;
import marauroa.server.game.db.IDatabase;
import marauroa.server.game.db.JDBCDatabase;

import org.junit.Test;

public class DatabaseExistsTest {
	/**
	 * JDBCDatabase can only be instantiated by DatabaseFactory, so we extend
	 * instead JDBC Database and create a proper public constructor.
	 * 
	 * @author miguel
	 * 
	 */
	static class TestJDBC extends JDBCDatabase {

		public TestJDBC(Properties props) {
			super(props);
		}
	}

	@Test
	public void checkDatabaseExists() {
		try {
			Log4J.init("marauroa/server/log4j.properties");

			Properties props = new Properties();

			props.put("jdbc_url", "jdbc:mysql://127.0.0.1/marauroatest");
			props.put("jdbc_class", "com.mysql.jdbc.Driver");
			props.put("jdbc_user", "junittest");
			props.put("jdbc_pwd", "passwd");

			IDatabase database = new TestJDBC(props);
		} catch (Exception e) {
			fail("Database is not accessible. Please check \"marauroatest\" is created and that user \"junittest\" with password \"passwd\" can access it.");			
			System.exit(1);
		}
	}

}

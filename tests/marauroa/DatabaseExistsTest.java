/* $Id: DatabaseExistsTest.java,v 1.6 2009/07/05 11:55:07 nhnb Exp $ */
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
package marauroa;

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
	public void checkDatabaseExists() throws Exception {
		try {
			Log4J.init("marauroa/server/log4j.properties");

			Properties props = new Properties();

			props.put("jdbc_url", "jdbc:mysql://127.0.0.1/marauroatest");
			props.put("jdbc_class", "com.mysql.jdbc.Driver");
			props.put("jdbc_user", "junittest");
			props.put("jdbc_pwd", "passwd");

			IDatabase database = new TestJDBC(props);
			database.close();
		} catch (Exception e) {
			throw new Exception("Database is not accessible. Please check \"marauroatest\" is created and that user \"junittest\" with password \"passwd\" can access it.", e);
		}
	}
}

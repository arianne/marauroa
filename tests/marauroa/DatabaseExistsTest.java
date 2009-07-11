/* $Id: DatabaseExistsTest.java,v 1.7 2009/07/11 13:55:06 nhnb Exp $ */
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
import marauroa.server.db.TransactionPool;

import org.junit.Test;

public class DatabaseExistsTest {

	@Test
	public void checkDatabaseExists() throws Exception {
		try {
			Log4J.init("marauroa/server/log4j.properties");

			Properties props = new Properties();

			props.put("jdbc_url", "jdbc:mysql://127.0.0.1/marauroatest");
			props.put("jdbc_class", "com.mysql.jdbc.Driver");
			props.put("jdbc_user", "junittest");
			props.put("jdbc_pwd", "passwd");

			TransactionPool pool = new TransactionPool(props);
			pool.rollback(pool.beginWork());
			pool.close();
		} catch (Exception e) {
			throw new Exception("Database is not accessible. Please check \"marauroatest\" is created and that user \"junittest\" with password \"passwd\" can access it.", e);
		}
	}
}

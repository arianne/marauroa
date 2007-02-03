/* $Id: TestPlayerAccess.java,v 1.1 2007/02/03 17:41:52 arianne_rpg Exp $ */
/***************************************************************************
 *                      (C) Copyright 2007 - Marauroa                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.server.game.db.test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.SQLException;
import java.util.Properties;

import marauroa.common.Log4J;
import marauroa.common.crypto.Hash;
import marauroa.server.game.db.JDBCDatabase;
import marauroa.server.game.db.JDBCTransaction;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author miguel
 *
 */
public class TestPlayerAccess {
	
	static class TestJDBC extends JDBCDatabase {
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
	public void removePlayer() throws SQLException {
		String username="testUser";

		JDBCTransaction transaction=database.getTransaction();
		try {
			transaction.begin();		
			database.addPlayer(transaction, username, Hash.hash("testPassword"), "email@email.com");
			assertTrue(database.hasPlayer(transaction, username));
			database.removePlayer(transaction, username);
			assertFalse(database.hasPlayer(transaction, username));
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

	@Test
	public void setStatus() throws SQLException {
		String username="testUser";
		
		JDBCTransaction transaction=database.getTransaction();

		try {
			transaction.begin();
			database.addPlayer(transaction, username, Hash.hash("testPassword"), "email@email.com");
			assertEquals("active",database.getAccountStatus(transaction, username));
			database.setAccountStatus(transaction, username, "banned");
			assertEquals("banned",database.getAccountStatus(transaction, username));			
		} finally {
			transaction.rollback();
		}
	}
}

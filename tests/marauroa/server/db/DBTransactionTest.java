/* $Id: DBTransactionTest.java,v 1.3 2010/05/08 23:03:37 nhnb Exp $ */
/***************************************************************************
 *                   (C) Copyright 2007-2010 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.server.db;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * Tests for DBTransaction
 *
 * @author hendrik
 *
 */
public class DBTransactionTest {

	/**
	 * Tests for subst
	 *
	 * @throws SQLException 
	 */
	@Test
	public void testSubst() throws SQLException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("o", "0");
		params.put("p", "0, 1");
		params.put("x", "0, y, 1");
		params.put("injection", "0, 'y, 1");
		
		DBTransaction transaction = new DBTransaction(null);

		assertThat(transaction.subst("", null), equalTo(""));
		assertThat(transaction.subst("Hallo", null), equalTo("Hallo"));
		assertThat(transaction.subst("Hall[o", params), equalTo("Hall0"));
		assertThat(transaction.subst("Hall[o]", params), equalTo("Hall0"));
		assertThat(transaction.subst("[o]Hall[o]", params), equalTo("0Hall0"));
		assertThat(transaction.subst("Hal[l]o", params), equalTo("Halo"));
		assertThat(transaction.subst("id IN ([o])", params), equalTo("id IN (0)"));
		assertThat(transaction.subst("id IN ([p])", params), equalTo("id IN (0, 1)"));

		try {
			transaction.subst("id IN ([x])", params);
			fail("SQLException expected");
		} catch (SQLException e) {
			// ok;
		}

		try {
			transaction.subst("id = [x]", params);
			fail("SQLException expected");
		} catch (SQLException e) {
			// ok;
		}

		assertThat(transaction.subst("id = '[x]'", params), equalTo("id = '0, y, 1'"));
		assertThat(transaction.subst("id = '[injection]'", params), equalTo("id = '0, ''y, 1'"));

	}
}

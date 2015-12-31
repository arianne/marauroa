/***************************************************************************
 *                   (C) Copyright 2007-2015 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.server.db.adapter;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.sql.SQLException;

import org.junit.Test;

/**
 * Tests for MySQLDatabaseAdapter
 *
 * @author hendrik
 */
public class MySQLDatabaseAdapterTest {

	/**
	 * tests for rewrite sql
	 *
	 * @throws SQLException
	 */
	@Test
	public void testRewriteSql() throws SQLException {
		MySQLDatabaseAdapter adapter = new MySQLDatabaseAdapter();
		assertThat(adapter.rewriteSql(""), equalTo(""));
		assertThat(adapter.rewriteSql("cReaTe Table foo(bla VARCHAR(1));"), equalTo("cReaTe Table foo(bla VARCHAR(1)) TYPE=InnoDB;"));
		assertThat(adapter.rewriteSql("cReaTe Table foo(bla VARCHAR(1))  ;"), equalTo("cReaTe Table foo(bla VARCHAR(1))   TYPE=InnoDB;"));
	}

}

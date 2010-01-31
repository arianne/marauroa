/* $Id: MySQLDatabaseAdapterTest.java,v 1.1 2010/01/31 20:39:52 nhnb Exp $ */
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
package marauroa.server.db.adapter;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.junit.Test;

/**
 * Tests for MySQLDatabaseAdapter
 *
 * @author hendrik
 */
public class MySQLDatabaseAdapterTest {

	@Test
	public void testRewriteSql() {
		MySQLDatabaseAdapter adapter = new MySQLDatabaseAdapter();
		assertThat(adapter.rewriteSql(""), equalTo(""));
		assertThat(adapter.rewriteSql("cReaTe Table foo(bla VARCHAR(1));"), equalTo("cReaTe Table foo(bla VARCHAR(1)) TYPE=InnoDB;"));
		assertThat(adapter.rewriteSql("cReaTe Table foo(bla VARCHAR(1))  ;"), equalTo("cReaTe Table foo(bla VARCHAR(1))   TYPE=InnoDB;"));
	}

}

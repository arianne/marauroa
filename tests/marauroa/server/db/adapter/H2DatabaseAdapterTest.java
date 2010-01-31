/* $Id: H2DatabaseAdapterTest.java,v 1.1 2010/01/31 20:47:01 nhnb Exp $ */
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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

import org.junit.Test;

/**
 * testsfor H2DatabaseAdapter
 *
 * @author hendrik
 */
public class H2DatabaseAdapterTest {

	/**
	 * tests for rewriteSql
	 */
	@Test
	public void testRewriteSql() {
		H2DatabaseAdapter adapter = new H2DatabaseAdapter();
		assertThat(adapter.rewriteSql(""), equalTo(""));
		assertThat(adapter.rewriteSql("AltEr TablE loginEvent ADD COlumN (service CHAR(10));"), equalTo("AltEr TablE loginEvent ADD service CHAR(10);"));
		assertThat(adapter.rewriteSql("AltEr TablE loginEvent ADD COlumN   (  service CHAR(10)  )   ;  "), equalTo("AltEr TablE loginEvent ADD   service CHAR(10)  ;"));
	}

}

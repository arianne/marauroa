/***************************************************************************
 *                   (C) Copyright 2015-2016 - Marauroa                    *
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

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Tests for CreateIndexStatementParser
 *
 * @author hendrik
 */
@RunWith(Parameterized.class)
public class CreateIndexStatementParserTest {

	/**
	 * generates data for tests
	 *
	 * @return data
	 */
	@Parameters
	public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
        	{
        		"CREATE INDEX IF NOT EXISTS i_gameEvents_event_timedate ON gameEvents(event, timedate);",
        		"CREATE INDEX i_gameEvents_event_timedate ON gameEvents(event, timedate)"
        	},
        	{
        		"CREATE UNIQUE INDEX IF NOT EXISTS i_gameEvents_event_timedate ON gameEvents(event, timedate);",
        		"CREATE UNIQUE INDEX i_gameEvents_event_timedate ON gameEvents(event, timedate)"
        	},
        	{
        		"CREATE   INDEX   IF   NOT   EXISTS   i_gameEvents_event_timedate   ON   gameEvents  (event, timedate);",
        		"CREATE INDEX i_gameEvents_event_timedate ON gameEvents(event, timedate)"
        	},
        	{
        		"CREATE   UNIQUE   INDEX   IF   NOT   EXISTS   i_gameEvents_event_timedate   ON   gameEvents  (event, timedate);",
        		"CREATE UNIQUE INDEX i_gameEvents_event_timedate ON gameEvents(event, timedate)"
        	}
        });
	}

	private String input;
	private String output;

	/**
	 * Tests for CreateIndexStatementParser
	 *
	 * @param input input test data
	 * @param output expected output
	 */
	public CreateIndexStatementParserTest(String input, String output) {
		this.input = input;
		this.output = output;
	}

	/**
	 * tests for toString()
	 */
	@Test
	public void testToString() {
		CreateIndexStatementParser parser = new CreateIndexStatementParser(input);
		assertThat(parser.toString(), equalTo(output));
	}

}

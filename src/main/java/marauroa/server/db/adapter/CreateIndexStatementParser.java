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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * parses CREATE INDEX statements
 *
 * @author hendrik
 */
class CreateIndexStatementParser {
	private boolean unique;
	private String name;
	private String table;
	private String columns;

	public CreateIndexStatementParser(String sql) {
		String pattern = "(?i)"
			+ "create[ ]*((?:unique)?)[ ]+index [ ]*" // CREATE [UNIQUE] INDEX
			+ "(?:if[ ]+not[ ]+exists )?[ ]*"         // IF NOT EXISTS
			+ "([^ ]+)[ ]*on[ ]+"                     // name ON
			+ "([^ (]+)[ ]*"                          // table
			+ "(\\([^)]*\\));?";                      // (column,...)

		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(sql);

		if (!m.matches()) {
			throw new IllegalArgumentException("Failed to parse CREATE INDEX statement: " + sql);
		}

		unique = !m.group(1).equals("");
		name = m.group(2);
		table = m.group(3);
		columns = m.group(4);
	}

	/**
	 * gets the name of the index
	 *
	 * @return name of index
	 */
	public String getName() {
		return name;
	}

	/**
	 * gets the name of the table
	 *
	 * @return table name
	 */
	public String getTable() {
		return table;
	}

	/**
	 * creates a CREATE INDEX statement, without "IF NOT EXISTS" clause.
	 *
	 * @return sql
	 */
	public String toSqlWithoutIf() {
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE ");
		if (unique) {
			sb.append("UNIQUE ");
		}
		sb.append("INDEX ");
		sb.append(name);
		sb.append(" ON ");
		sb.append(table);
		sb.append(columns);
		return sb.toString();
	}

	@Override
	public String toString() {
		return toSqlWithoutIf();
	}
}

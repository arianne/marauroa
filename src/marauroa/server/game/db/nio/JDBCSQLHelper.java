package marauroa.server.game.db.nio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import marauroa.common.Log4J;
import marauroa.server.game.db.JDBCTransaction;

import org.apache.log4j.Logger;

public class JDBCSQLHelper {
	/** the logger instance. */
	private static final Logger logger = Log4J.getLogger(JDBCSQLHelper.class);
	
	private static JDBCSQLHelper sqlHelper;
		
	private JDBCSQLHelper() {
	}

	public static JDBCSQLHelper get() {
		if(sqlHelper==null) {
			sqlHelper=new JDBCSQLHelper();
		}

		return sqlHelper;
	}

	/**
	 * This method runs a SQL file using the given transaction.
	 * You are resposible of begining the transaction and commiting the changes or rollback on error.
	 * 
	 * @param transaction The JDBC Transaction that we are going to use.
	 * @param file The file name that contains the SQL commands.
	 * @return true if the whole file was executed or false in any other error.
	 */
	public boolean runDBScript(JDBCTransaction transaction, String file) {
		Log4J.startMethod(logger, "runDBScript");

		boolean ret = true;
		Connection con = transaction.getConnection();
		BufferedReader in = null;

		try {
			Statement stmt = con.createStatement();
			
			InputStream init_file = getClass().getClassLoader().getResourceAsStream(file);
			in = new BufferedReader(new InputStreamReader(init_file));

			StringBuffer is = new StringBuffer();
			
			String line;
			while ((line = in.readLine()) != null) {
				is.append(line);
				if (line.indexOf(';') != -1) {
					String query = is.toString();
					logger.debug("runDBScript is executing query " + query);
					stmt.addBatch(query);
					is = new StringBuffer();
				}
			}

			int ret_array[] = stmt.executeBatch();

			for (int i = 0; i < ret_array.length; i++) {
				if (ret_array[i] < 0) {
					ret = false;
					break;
				}
			}

			stmt.close();

			return ret;
		} catch (SQLException e) {
			logger.error("error running SQL Script (file: " + file + ")", e);
			return false;
		} catch (IOException e) {
			logger.error("error reading SQL Script (file: " + file + ")", e);
			return false;
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
			}

			Log4J.finishMethod(logger, "runDBScript");
		}
	}

}

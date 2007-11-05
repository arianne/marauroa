package marauroa.server.game.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

/**
 * Low level database access. Please use IDatabase for high level access,
 * this class is meant to help implementing subclasses of IDatabase.
 *
 * @author hendrik
 */
public class JDBCAccess {

	private Transaction transaction;
	private LinkedList<Statement> statements = null;
	private LinkedList<ResultSet> resultSets = null;

	/**
	 * Creates a new LowLevelDatabaseAccess object
	 *
	 * @param transaction JDBCTransaction
	 */
	public JDBCAccess(Transaction transaction) {
		this.transaction = transaction;
	}

	/**
	 * Executes an SQL statement
	 *
	 * @param sql sql
	 * @throws SQLException in case of an SQL error
	 */
	public void execute(String sql) throws SQLException {
		Connection connection = transaction.getConnection();
		Statement stmt = connection.createStatement();
		stmt.execute(sql);
		stmt.close();
	}

	/**
	 * Executes an SQL statement
	 *
	 * @param sql sql
	 * @param inputStreams data
	 * @throws SQLException in case of an SQL error
	 * @throws IOException in case of an IO error
	 */
	public void execute(String sql, InputStream... inputStreams) throws SQLException, IOException {
		Connection connection = transaction.getConnection();
		PreparedStatement stmt = connection.prepareStatement(sql);
		int i = 1; // yes, jdbc starts counting at 1.
		for (InputStream inputStream : inputStreams) {
			stmt.setBinaryStream(i, inputStream, inputStream.available());
			i++;
		}
		stmt.executeUpdate();
		stmt.close();
	}

	/**
	 * Executes a query. Note: You need to call the close() method
	 *
	 * @param sql query
	 * @return ResultSet
	 * @throws SQLException in case of an SQL error
	 */
	public ResultSet query(String sql) throws SQLException {
		Connection connection = transaction.getConnection();
		Statement stmt = connection.createStatement();
		ResultSet resultSet = stmt.executeQuery(sql);
		addToGarbageLists(stmt, resultSet);
		return resultSet;
	}

	/**
	 * initializes the collections where opened Statements and ResultSets
	 * are stored in order to have only one close methods.
	 */
	private void initGarbageLists() {
		if (statements == null) {
			statements = new LinkedList<Statement>();
			resultSets = new LinkedList<ResultSet>();
		}
	}

	/**
	 * Stores open statements and resultSets in garbages lists, so that
	 * they can be closed with one single close()-method
	 *
	 * @param statement Statement
	 * @param resultSet ResultSet
	 */
	private void addToGarbageLists(Statement statement, ResultSet resultSet) {
		initGarbageLists();
		statements.add(statement);
		resultSets.add(resultSet);
	}

	/**
	 * Clean up
	 *
	 * @throws SQLException in case of an unexspected JDBC error
	 */
	public void close() throws SQLException {
		if (statements != null) {
			// Note: Some JDBC drivers like Informix require resultSet.close() 
			// before statement.close() although the second one is supposed to
			// close open ResultSets by itself according to the API doc.
			for (ResultSet resultSet : resultSets) {
				resultSet.close();
			}
			for (Statement statement : statements) {
				statement.close();
			}
		}
	}

}

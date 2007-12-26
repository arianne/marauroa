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
public class JDBCAccess implements Accessor {

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

	public int execute(String sql) throws SQLException {
		int res = -2;
		Connection connection = transaction.getConnection();
		Statement statement = connection.createStatement();
		boolean resultType = statement.execute(sql);
		if (!resultType) {
			res = statement.getUpdateCount();
		}
		statement.close();
		return res;
	}

	public void execute(String sql, InputStream... inputStreams) throws SQLException, IOException {
		Connection connection = transaction.getConnection();
		PreparedStatement statement = connection.prepareStatement(sql);
		int i = 1; // yes, jdbc starts counting at 1.
		for (InputStream inputStream : inputStreams) {
			statement.setBinaryStream(i, inputStream, inputStream.available());
			i++;
		}
		statement.executeUpdate();
		statement.close();
	}

	public void executeBatch(String sql, InputStream... inputStreams) throws SQLException, IOException {
		Connection connection = transaction.getConnection();
		PreparedStatement statement = connection.prepareStatement(sql);
		int i = 1; // yes, jdbc starts counting at 1.
		for (InputStream inputStream : inputStreams) {
			statement.setBinaryStream(i, inputStream, inputStream.available());
			statement.executeUpdate();
		}
		statement.close();
	}

	public ResultSet query(String sql) throws SQLException {
		Connection connection = transaction.getConnection();
		Statement stmt = connection.createStatement();
		ResultSet resultSet = stmt.executeQuery(sql);
		addToGarbageLists(stmt, resultSet);
		return resultSet;
	}

	public int querySingleCellInt(String sql) throws SQLException {
		Connection connection = transaction.getConnection();
		Statement stmt = connection.createStatement();
		ResultSet resultSet = stmt.executeQuery(sql);
		resultSet.next();
		int res = resultSet.getInt(1);
		resultSet.close();
		stmt.close();
		return res;
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

package marauroa.server.game.db;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import marauroa.common.Log4J;
import marauroa.server.db.DBTransaction;
import marauroa.server.db.TransactionPool;
import marauroa.server.game.Statistics.Variables;

public class StatisticsDAO {
	private static final marauroa.common.Logger logger = Log4J.getLogger(StatisticsDAO.class);

	public void addStatisticsEvent(DBTransaction transaction, Variables var) {
		try {
			String query = "insert into statistics(timedate, bytes_send, bytes_recv, players_login, players_logout, players_timeout, players_online) "
				+ " values(NULL, [Bytes send], [Bytes recv], [Players login], [Players logout], [Players timeout], [Players online])";
			Map<String, Object> params = new HashMap<String, Object>();
			for (String key : var) {
				params.put(key, var.get(key));
			}
			transaction.execute(query, params);
		} catch (SQLException sqle) {
			logger.warn("Error adding statistics event", sqle);
		}
	}

	public void addStatisticsEvent(Variables var) {
		DBTransaction transaction = TransactionPool.get().beginWork();
		addStatisticsEvent(transaction, var);
		try {
			TransactionPool.get().commit(transaction);
		} catch (SQLException e) {
			logger.error(e, e);
		}
	}
}

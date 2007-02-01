package marauroa.server.game.db.nio;

import marauroa.server.game.Statistics.Variables;
import marauroa.server.game.db.Transaction;

public interface IEventsAccess {

	/**
	 * This method adds a logging game event to database, so later you can query it to get information
	 * about how game is evolving.  
	 * @param trans the database transaction
	 * @param source the source of the event
	 * @param event the event itself
	 * @param params any params the event may need.
	 */
	public void addGameEvent(Transaction trans, String source, String event, String... params);

	/**
	 * This method inserts in database a statistics events.
	 * @param trans the database transaction
	 * @param var the statistics variables. @See marauroa.server.game.Statistics.Variables
	 */
	public void addStatisticsEvent(Transaction trans, Variables var);

}
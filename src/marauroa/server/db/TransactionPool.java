/***************************************************************************
 *                   (C) Copyright 2007-2012 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.server.db;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import marauroa.common.Log4J;
import marauroa.common.Logger;
import marauroa.common.Pair;

/**
 * Connection Pool.
 *
 * @author hendrik
 */
public class TransactionPool {

	private static Logger logger = Log4J.getLogger(TransactionPool.class);
	private static TransactionPool dbtransactionPool = null;
	private AdapterFactory factory = null;
	private final Object wait = new Object();
	private Properties params = new Properties();
	private int count = 10;
	private final List<DBTransaction> dbtransactions = Collections.synchronizedList(new LinkedList<DBTransaction>());
	private final List<DBTransaction> freeDBTransactions = Collections.synchronizedList(new LinkedList<DBTransaction>());
	private final ThreadLocal<Set<DBTransaction>> threadTransactions = new ThreadLocal<Set<DBTransaction>>();
	private final Map<DBTransaction, Pair<String, StackTraceElement[]>> callers;
	private boolean closed = false;

	/**
	 * creates a DBTransactionPool
	 *
	 * @param connfiguration configuration
	 */
	public TransactionPool(Properties connfiguration) {
		params = connfiguration;
		count = Integer.parseInt(params.getProperty("count", "4"));
		callers = Collections.synchronizedMap(new HashMap<DBTransaction, Pair<String, StackTraceElement[]>>());
		factory = new AdapterFactory(connfiguration);
	}

	/**
	 * registers this TransactionPool as the global one.
	 */
	public void registerGlobally() {
		registerGlobal(this);
	}

	/**
	 * registers a TransactionPool as the global one.
	 *
	 * @param transactionPool the pool to register globally
	 */
	private static void registerGlobal(TransactionPool transactionPool) {
		TransactionPool.dbtransactionPool = transactionPool;
	}

	/**
	 * gets the TransactionPool
	 *
	 * @return TransactionPool
	 */
	public static synchronized TransactionPool get() {
		return dbtransactionPool;
	}

	private void createMinimumDBTransactions() {
		synchronized (wait) {
			while (dbtransactions.size() < count) {
				DBTransaction dbtransaction = new DBTransaction(factory.create());
				dbtransactions.add(dbtransaction);
				freeDBTransactions.add(dbtransaction);
			}
		}
	}

	/**
	 * starts a transaction and marks it as reserved
	 *
	 * @return DBTransaction
	 */
	public DBTransaction beginWork() {
		if (closed) {
			throw new RuntimeException("transaction pool has been closed");
		}
		DBTransaction dbtransaction = null;
		while (dbtransaction == null) {
			synchronized (wait) {
				createMinimumDBTransactions();
				while (freeDBTransactions.size() == 0) {
					createMinimumDBTransactions();
					try {
						logger.info("Waiting for a DBTransaction", new Throwable());
						dumpOpenTransactions();
						wait.wait();
					} catch (InterruptedException e) {
						logger.error(e, e);
					}
				}

				dbtransaction = freeDBTransactions.remove(0);
				addThreadTransaction(dbtransaction);
				// TODO: check that the connection is still alive
			}
		}
		logger.debug("getDBTransaction: " + dbtransaction, new Throwable());

		Thread currentThread = Thread.currentThread();
		callers.put(dbtransaction, new Pair<String, StackTraceElement[]>(currentThread.getName(), currentThread.getStackTrace()));
		dbtransaction.setThread(Thread.currentThread());
		return dbtransaction;
	}

	/**
	 * dumps a list of open transactions with their threads and stacktraces to the log file.
	 */
	public void dumpOpenTransactions() {
		for (Pair<String, StackTraceElement[]> pair : callers.values()) {
			logger.info("      * " + pair.first() + " " + Arrays.asList(pair.second()));
		}
	}

	/**
	 * commits this transaction and frees its reservation
	 *
	 * @param dbtransaction transaction
	 * @throws SQLException in case of an database error
	 */
	public void commit(DBTransaction dbtransaction) throws SQLException {
		try {
			dbtransaction.commit();
		} catch (SQLException e) {
			killTransaction(dbtransaction);
			throw e;
		}
		freeDBTransaction(dbtransaction);
	}

	/**
	 * rolls this transaction back and frees the reservation
	 *
	 * @param dbtransaction transaction
	 */
	public void rollback(DBTransaction dbtransaction) {
		try {
			dbtransaction.rollback();
			freeDBTransaction(dbtransaction);
		} catch (SQLException e) {
			killTransaction(dbtransaction);
			logger.warn(e, e);
		}
	}

	private void freeDBTransaction(DBTransaction dbtransaction) {
		logger.debug("freeDBTransaction: " + dbtransaction, new Throwable());
		synchronized (wait) {
			threadTransactions.get().remove(dbtransaction);
			callers.remove(dbtransaction);
			dbtransaction.setThread(null);
			if (dbtransactions.contains(dbtransaction)) {
				freeDBTransactions.add(dbtransaction);
			} else {
				logger.error("Unknown DBTransaction " + dbtransaction + " was not freed.", new Throwable());
			}
			wait.notifyAll();
		}
	}

	private void addThreadTransaction(DBTransaction dbtransaction) {
		Set<DBTransaction> set = threadTransactions.get();
		if (set == null) {
			set = new HashSet<DBTransaction>();
			threadTransactions.set(set);
		}
		set.add(dbtransaction);
	}

	/**
	 * Kicks all transactions which were started in the current thread
	 */
	public void kickHangingTransactionsOfThisThread() {
		Set<DBTransaction> set = threadTransactions.get();
		if ((set == null) || set.isEmpty()) {
			return;
		}

		synchronized (wait) {
			for (DBTransaction dbtransaction : set) {
				killTransaction(dbtransaction);
				logger.error("Hanging transaction " + dbtransaction + " was kicked.");
			}
		}
		set.clear();
	}

	/**
	 * kills a transaction by rolling it back and closing it;
	 * it will be removed from the pool
	 *
	 * @param dbtransaction DBTransaction
	 */
	private void killTransaction(DBTransaction dbtransaction) {
		try {
			dbtransaction.rollback();
		} catch (SQLException e) {
			logger.debug(e, e);
		}
		dbtransaction.close();
		dbtransactions.remove(dbtransaction);
		callers.remove(dbtransaction);
	}

	/**
	 * closes the transaction pool
	 */
	public void close() {
		closed = true;
		for (DBTransaction transaction : dbtransactions) {
			transaction.close();
		}
	}
}

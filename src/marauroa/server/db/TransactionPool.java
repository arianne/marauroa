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
    private Object wait = new Object();
    private Properties params = new Properties();
    private int count = 10;

    private List<DBTransaction> dbtransactions = Collections.synchronizedList(new LinkedList<DBTransaction>()); 
    private List<DBTransaction> freeDBTransactions = Collections.synchronizedList(new LinkedList<DBTransaction>());
    private ThreadLocal<Set<DBTransaction>> threadTransactions = new ThreadLocal<Set<DBTransaction>>();
    private Map<DBTransaction, Pair<String, StackTraceElement[]>> callers;

	private boolean closed = false; 

    /**
     * creates a DBTransactionPool
     *
     * @param connfiguration connfiguration
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
        TransactionPool.dbtransactionPool = this;
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
                        Thread.sleep(1000);
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
     * commits this transaction and frees it reservation
     *
     * @param dbtransaction transaction
     * @throws SQLException in case of an database error
     */
    public void commit(DBTransaction dbtransaction) throws SQLException {
    	try {
			dbtransaction.commit();
		} catch (SQLException e) {
			freeDBTransaction(dbtransaction);
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
    	dbtransaction.rollback();
    	freeDBTransaction(dbtransaction);
    }
    
    private void freeDBTransaction(DBTransaction dbtransaction) {
        logger.debug("freeDBTransaction: " + dbtransaction, new Throwable());
        synchronized (wait) {
	        threadTransactions.get().remove(dbtransaction);
	        callers.remove(dbtransaction);
	        if (dbtransactions.contains(dbtransaction)) {
	            freeDBTransactions.add(dbtransaction);
	        } else {
	            logger.error("Unbekannter DBTransaction " + dbtransaction + " nicht freigegeben.", new Throwable());
	        }
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
     * Kicks all transaction which where started in the current thread
     */
    public void kickHangingTransactionsOfThisThread() {
    	Set<DBTransaction> set = threadTransactions.get();
    	if (set == null) {
    		return;
    	}
    	for (DBTransaction dbtransaction : set) {
    		dbtransaction.rollback();
    		dbtransaction.close();
    		dbtransactions.remove(dbtransaction);
    		logger.error("Hanging transaction " + dbtransaction + " was kicked.");
    	}
    	set.clear();
    }

    /**
     * closes the transaction pool
     */
	public void close() {
		closed  = true;
		for (DBTransaction transaction : dbtransactions) {
			transaction.close();
		}
	}
}

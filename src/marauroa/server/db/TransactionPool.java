package marauroa.server.db;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import marauroa.common.Log4J;
import marauroa.common.Logger;

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

    private List<DBTransaction> dbtransactions = new LinkedList<DBTransaction>(); 
    private List<DBTransaction> freeDBTransactions = new LinkedList<DBTransaction>();
	private boolean closed = false; 

    /**
     * creates a DBTransactionPool
     *
     * @param connfiguration connfiguration
     */
    public TransactionPool(Properties connfiguration) {
    	params = connfiguration;
        count = Integer.parseInt(params.getProperty("count", "4"));
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
        while (dbtransactions.size() < count) {
            DBTransaction dbtransaction = new DBTransaction(factory.create());
            dbtransactions.add(dbtransaction);
            freeDBTransactions.add(dbtransaction);
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
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        logger.error(e, e);
                    }
                }

                dbtransaction = freeDBTransactions.remove(0);
                // TODO: check that the connection is still alive
            }
        }
        logger.debug("getDBTransaction: " + dbtransaction, new Throwable());
        return dbtransaction;
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
        if (dbtransactions.contains(dbtransaction)) {
            freeDBTransactions.add(dbtransaction);
        } else {
            logger.error("Unbekannter DBTransaction " + dbtransaction + " nicht freigegeben.", new Throwable());
        }
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

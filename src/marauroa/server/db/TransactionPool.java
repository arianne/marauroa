package marauroa.server.db;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * Connection Pool.
 *
 * @author hendrik
 */
public class TransactionPool {
    private static Logger logger = Logger.getLogger(TransactionPool.class);
    private static TransactionPool dbtransactionPool = null;
    private AdapterFactory factory = null;
    private Object wait = new Object();
    private Properties params = new Properties();
    private int count = 10;

    private List<DBTransaction> dbtransactions = new LinkedList<DBTransaction>(); 
    private List<DBTransaction> freeDBTransactions = new LinkedList<DBTransaction>(); 

    /**
     * creates a DBTransactionPool
     *
     * @param connfiguration connfiguration
     */
    public TransactionPool(Properties connfiguration) {
    	params = connfiguration;
        count = Integer.parseInt(params.getProperty("count"));
        TransactionPool.dbtransactionPool = this;
        factory = new AdapterFactory(connfiguration);
    }

    public static synchronized TransactionPool getDBTransactionPool() {
        return dbtransactionPool;
    }

    private void createMinimumDBTransactions() {
        while (dbtransactions.size() < count) {
            DBTransaction dbtransaction = new DBTransaction(factory.create());
            dbtransactions.add(dbtransaction);
            freeDBTransactions.add(dbtransaction);
        }
    }

    public DBTransaction beginWork() {
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

    public void commit(DBTransaction dbtransaction) {
    	dbtransaction.commit();
    	freeDBTransaction(dbtransaction);
    }

    public void rollback(DBTransaction dbtransaction) {
    	dbtransaction.rollback();
    	freeDBTransaction(dbtransaction);
    }
    
    public void freeDBTransaction(DBTransaction dbtransaction) {
        logger.debug("freeDBTransaction: " + dbtransaction, new Throwable());
        if (dbtransactions.contains(dbtransaction)) {
            freeDBTransactions.add(dbtransaction);
        } else {
            logger.error("Unbekannter DBTransaction " + dbtransaction + " nicht freigegeben.", new Throwable());
        }
    }
}

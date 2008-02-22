package marauroa.test.ping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observer;

import marauroa.client.ClientFramework;
import marauroa.client.net.IPerceptionListener;
import marauroa.client.net.PerceptionHandler;
import marauroa.common.Log4J;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPObject.ID;
import marauroa.common.net.message.MessageS2CPerception;
import marauroa.common.net.message.TransferContent;


public class PingClient extends ClientFramework {
	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(ClientFramework.class);
	
	/**
	 * Perception listener that handle how perceptions are applied.
	 * @author miguel
	 *
	 */
	class PingPerceptionListener implements IPerceptionListener {

		public boolean onAdded(RPObject object) {
	        // TODO Auto-generated method stub
	        return false;
        }

		public boolean onClear() {
	        // TODO Auto-generated method stub
	        return false;
        }

		public boolean onDeleted(RPObject object) {
	        // TODO Auto-generated method stub
	        return false;
        }

		public void onException(Exception exception, MessageS2CPerception perception) {
	        // TODO Auto-generated method stub
	        
        }

		public boolean onModifiedAdded(RPObject object, RPObject changes) {
	        // TODO Auto-generated method stub
	        return false;
        }

		public boolean onModifiedDeleted(RPObject object, RPObject changes) {
	        // TODO Auto-generated method stub
	        return false;
        }

		public boolean onMyRPObject(RPObject added, RPObject deleted) {
	        // TODO Auto-generated method stub
	        return false;
        }

		public void onPerceptionBegin(byte type, int timestamp) {
	        // TODO Auto-generated method stub
	        
        }

		public void onPerceptionEnd(byte type, int timestamp) {
	        // TODO Auto-generated method stub
	        
        }

		public void onSynced() {
	        // TODO Auto-generated method stub
	        
        }

		public void onUnsynced() {
	        // TODO Auto-generated method stub
	        
        }
		
	}

	/**
	 * List of characters this player owns.
	 */
	private String[] characters;
	
	/**
	 * Perception handler to process messages received from server. 
	 */
	private PerceptionHandler handler;
	
	/**
	 * Stores all the zone objects.
	 */
	private Map<ID, RPObject> objects;

	public PingClient(String loggingProperties) {
	    super(loggingProperties);
	    
		PingPerceptionListener listener = new PingPerceptionListener();
		handler = new PerceptionHandler(listener);
		
		objects=new HashMap<ID,RPObject>();
    }

	@Override
    protected String getGameName() {		
	    return "ping";
    }

	@Override
    protected String getVersionNumber() {
	    return "0.00";
    }

	@Override
    protected void onAvailableCharacters(String[] characters) {
		this.characters=characters;
    }

	@Override
    protected void onPerception(MessageS2CPerception message) {
	    try {
	        handler.apply(message, objects);
        } catch (Exception e) {
        	logger.fatal(e);
        }	    
    }

	@Override
    protected void onPreviousLogins(List<String> previousLogins) {
		/*
		 * We are not interested.
		 */	    
    }

	@Override
    protected void onServerInfo(String[] info) {
		/*
		 * We are not interested.
		 */	    
    }

	@Override
    protected void onTransfer(List<TransferContent> items) {
		/*
		 * We are not interested.
		 */	    
    }

	@Override
    protected List<TransferContent> onTransferREQ(List<TransferContent> items) {
		/*
		 * For each of the items, we accept them.
		 */
		for(TransferContent item: items) {
			item.ack=true;
		}
		
		return items;
    }

}

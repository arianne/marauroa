/* $Id: PingClient.java,v 1.3 2009/12/24 12:58:16 nhnb Exp $ */
/***************************************************************************
 *						(C) Copyright 2003 - Marauroa					   *
 ***************************************************************************
 ***************************************************************************
 *																		   *
 *	 This program is free software; you can redistribute it and/or modify  *
 *	 it under the terms of the GNU General Public License as published by  *
 *	 the Free Software Foundation; either version 2 of the License, or	   *
 *	 (at your option) any later version.								   *
 *																		   *
 ***************************************************************************/
package marauroa.ping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import marauroa.client.ClientFramework;
import marauroa.client.net.IPerceptionListener;
import marauroa.client.net.PerceptionHandler;
import marauroa.common.Log4J;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPObject.ID;
import marauroa.common.net.message.MessageS2CPerception;
import marauroa.common.net.message.TransferContent;
import marauroa.ping.client.Speaker;
import marauroa.ping.client.SpeakerView;


public class PingClient extends ClientFramework {
	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(ClientFramework.class);
	
	/**
	 * Stores all the zone objects.
	 */
	private Map<ID, RPObject> objects;

	private Map<ID, Speaker> speakers;
	private Map<Speaker,SpeakerView> speakersView;
	
	/**
	 * Perception listener that handle how perceptions are applied.
	 * @author miguel
	 *
	 */
	class PingPerceptionListener implements IPerceptionListener {

		/**
		 * We define how we handle when a new object is added to the zone.
		 * So when a new object appears we create a model for it and based
		 * on the model we create a view.
		 */
		public boolean onAdded(RPObject object) {
	        Speaker speaker=new Speaker();
	        speakers.put(object.getID(), speaker);
	        
	        SpeakerView view=new SpeakerView(speaker);	        
	        speakersView.put(speaker,view);
	        
	        return false;
        }

		/**
		 * When we are asked to remove all the objects.
		 */
		public boolean onClear() {
			speakers.clear();
			speakersView.clear();
			
			return false;
        }

		/**
		 * When a object is removed from world.
		 */
		public boolean onDeleted(RPObject object) {
			Speaker removed=speakers.remove(object.getID());
			speakersView.remove(removed);
			
			return false;
		}

		public void onException(Exception exception, MessageS2CPerception perception) {
			exception.printStackTrace();
        }

		public boolean onModifiedAdded(RPObject object, RPObject changes) {
	        Speaker speaker=speakers.get(object.getID());
	        speaker.onAddedChanges(changes);
	        
	        return false;
        }

		public boolean onModifiedDeleted(RPObject object, RPObject changes) {
	        Speaker speaker=speakers.get(object.getID());
	        speaker.onDeletedChanges(changes);
	        
	        return false;
        }

		public boolean onMyRPObject(RPObject added, RPObject deleted) {
	        return false;
        }

		public void onPerceptionBegin(byte type, int timestamp) {
			// do nothing, but method is required by interface
        }

		public void onPerceptionEnd(byte type, int timestamp) {
			// do nothing, but method is required by interface
        }

		public void onSynced() {
			// do nothing, but method is required by interface
        }

		public void onUnsynced() {
			// do nothing, but method is required by interface
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
	
	public String[] getAvailableCharacters() {
		return characters;
	}

	@Override
    protected void onPerception(MessageS2CPerception message) {
	    try {
	        handler.apply(message, objects);
        } catch (Exception e) {
        	logger.error(e);
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
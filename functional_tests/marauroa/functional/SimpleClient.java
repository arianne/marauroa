/* $Id: SimpleClient.java,v 1.3 2009/12/24 13:15:42 nhnb Exp $ */
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
package marauroa.functional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import marauroa.client.ClientFramework;
import marauroa.client.net.IPerceptionListener;
import marauroa.client.net.PerceptionHandler;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPObject.ID;
import marauroa.common.net.message.MessageS2CPerception;
import marauroa.common.net.message.TransferContent;
import marauroa.functional.basic.TestHelper;

public class SimpleClient extends ClientFramework {

	private String[] characters;

	private PerceptionHandler handler;

	private MockPerceptionListener listener;

	private boolean isSynced;

	private int perceptions;

	class MockPerceptionListener implements IPerceptionListener {

		private Map<ID, RPObject> objects;

		public MockPerceptionListener() {
			objects = new HashMap<ID, RPObject>();
		}

		public Map<ID, RPObject> getContainer() {
			return objects;
		}

		public boolean onAdded(RPObject object) {
			// do nothing, but method is required by interface
			return false;
		}

		public boolean onClear() {
			// do nothing, but method is required by interface
			return false;
		}

		public boolean onDeleted(RPObject object) {
			// do nothing, but method is required by interface
			return false;
		}

		public void onException(Exception e, MessageS2CPerception perception) {
			System.out.println("Got " + e + " when applying " + perception);
			e.printStackTrace();
		}

		public boolean onModifiedAdded(RPObject object, RPObject changes) {
			// do nothing, but method is required by interface
			return false;
		}

		public boolean onModifiedDeleted(RPObject object, RPObject changes) {
			// do nothing, but method is required by interface
			return false;
		}

		public boolean onMyRPObject(RPObject added, RPObject deleted) {
			// do nothing, but method is required by interface
			return false;
		}

		public void onPerceptionBegin(byte type, int timestamp) {
			perceptions++;
		}

		public void onPerceptionEnd(byte type, int timestamp) {
			// do nothing, but method is required by interface
		}

		public void onSynced() {
			isSynced = true;
		}

		public void onUnsynced() {
			isSynced = false;
		}
	}

	public SimpleClient(String loggingProperties) {
		super(loggingProperties);

		listener = new MockPerceptionListener();
		handler = new PerceptionHandler(listener);
		isSynced = false;
		perceptions = 0;
	}

	public String[] getCharacters() {
		return characters;
	}

	public boolean isSynchronized() {
		return isSynced;
	}

	public Map<ID, RPObject> getObjects() {
		return listener.getContainer();
	}

	public int getPerceptions() {
		return perceptions;
	}

	@Override
	protected String getGameName() {
		return "TestFramework";
	}

	@Override
	protected String getVersionNumber() {
		return "0.00";
	}

	@Override
	protected void onAvailableCharacters(String[] characters) {
		this.characters = characters;
	}

	@Override
	protected void onPerception(MessageS2CPerception message) {
		try {
			handler.apply(message, listener.getContainer());
		} catch (Exception e) {
			System.out.println("Exception with perception " + message);
			e.printStackTrace();
			TestHelper.fail();
		}
	}

	@Override
	protected void onServerInfo(String[] info) {
		/*
		 * I don't care about them
		 */
	}

	@Override
	protected void onTransfer(List<TransferContent> items) {
		for(TransferContent item: items) {
			if (!item.name.equals("test_content")) {
				TestHelper.fail();
			}

			int j=1;
			for(byte i: item.data) {
				if(i!=j) {
					System.out.println("i="+i+" but was="+j);
					TestHelper.fail();
				}

				j++;
			}
		}

	}

	@Override
	protected List<TransferContent> onTransferREQ(List<TransferContent> items) {
		for(TransferContent i: items) {
			i.ack=true;
		}
		
		return items;
	}

	@Override
	protected void onPreviousLogins(List<String> previousLogins) {
		System.out.println("Listing previous logins");
		for (String line : previousLogins) {
			System.out.println(line);
		}
	}

}

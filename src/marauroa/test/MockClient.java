package marauroa.test;

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

public class MockClient extends ClientFramework {

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

		public void onException(Exception e, MessageS2CPerception perception) throws Exception {
			System.out.println("Got " + e + " when applying " + perception);
			e.printStackTrace();
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
			perceptions++;
		}

		public void onPerceptionEnd(byte type, int timestamp) {
		}

		public void onSynced() {
			isSynced = true;
		}

		public void onUnsynced() {
			isSynced = false;
		}
	}

	public MockClient(String loggingProperties) {
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
		// TODO Auto-generated method stub

	}

	@Override
	protected List<TransferContent> onTransferREQ(List<TransferContent> items) {
		// TODO Auto-generated method stub
		return null;
	}

}

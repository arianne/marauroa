package marauroa.test;

import java.util.List;

import marauroa.client.ClientFramework;
import marauroa.common.net.message.MessageS2CPerception;
import marauroa.common.net.message.TransferContent;

public class TestClient extends ClientFramework {

	public TestClient(String loggingProperties) {
		super(loggingProperties);
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
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onPerception(MessageS2CPerception message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onServerInfo(String[] info) {
		// TODO Auto-generated method stub
		
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

package marauroa.test;

import java.util.List;

import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPObjectInvalidException;
import marauroa.common.game.RPObjectNotFoundException;
import marauroa.server.game.AccountResult;
import marauroa.server.game.rp.IRPRuleProcessor;
import marauroa.server.game.rp.RPServerManager;

public class TestRPRuleProcessor implements IRPRuleProcessor{

	public void beginTurn() {
		// TODO Auto-generated method stub
		
	}

	public boolean checkGameVersion(String game, String version) {
		// TODO Auto-generated method stub
		return false;
	}

	public AccountResult createAccount(String username, String password, String email, RPObject template) {
		// TODO Auto-generated method stub
		return null;
	}

	public void endTurn() {
		// TODO Auto-generated method stub
		
	}

	public void execute(RPObject object, RPAction action) {
		// TODO Auto-generated method stub
		
	}

	public boolean onActionAdd(RPObject object, RPAction action, List<RPAction> actionList) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean onExit(RPObject object) throws RPObjectNotFoundException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean onInit(RPObject object) throws RPObjectInvalidException {
		// TODO Auto-generated method stub
		return false;
	}

	public void onTimeout(RPObject object) throws RPObjectNotFoundException {
		// TODO Auto-generated method stub
		
	}

	public void setContext(RPServerManager rpman) {
		// TODO Auto-generated method stub
		
	}

}

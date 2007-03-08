package marauroa.test;

import java.sql.SQLException;
import java.util.List;

import marauroa.common.Log4J;
import marauroa.common.crypto.Hash;
import marauroa.common.game.AccountResult;
import marauroa.common.game.CharacterResult;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPObjectInvalidException;
import marauroa.common.game.RPObjectNotFoundException;
import marauroa.common.game.Result;
import marauroa.server.game.db.DatabaseFactory;
import marauroa.server.game.db.IDatabase;
import marauroa.server.game.db.JDBCSQLHelper;
import marauroa.server.game.db.Transaction;
import marauroa.server.game.rp.IRPRuleProcessor;
import marauroa.server.game.rp.RPServerManager;
import marauroa.server.game.rp.RPWorld;

public class MockRPRuleProcessor implements IRPRuleProcessor{
	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(MockRPRuleProcessor.class);

	private IDatabase db;
	private RPWorld world;

	public MockRPRuleProcessor() {
		db=DatabaseFactory.getDatabase();
		JDBCSQLHelper sql=JDBCSQLHelper.get();
		world=MockRPWorld.get();

		try {
			Transaction transaction=db.getTransaction();
			transaction.begin();
			sql.runDBScript(transaction, "marauroa/test/clear.sql");
			sql.runDBScript(transaction, "marauroa/server/marauroa_init.sql");
			transaction.commit();
		} catch(SQLException e) {
			e.printStackTrace();
			TestHelper.fail();
		}
	}

	private static MockRPRuleProcessor rules;

	/**
	 * This method MUST be implemented in other for marauroa to be able to load this World implementation.
	 * There is no way of enforcing static methods on a Interface, so just keep this in mind when
	 * writting your own game.
	 *
	 * @return an unique instance of world.
	 */
	public static IRPRuleProcessor get() {
		if(rules==null) {
			rules = new MockRPRuleProcessor();
		}

		return rules;
	}

	public void beginTurn() {
		// TODO Auto-generated method stub

	}

	/**
	 * Checks if game is correct.
	 * We expect TestFramework at 0.00 version.
	 */
	public boolean checkGameVersion(String game, String version) {
		TestHelper.assertEquals("TestFramework", game);
		TestHelper.assertEquals("0.00", version);

		logger.info("Client uses:"+game+":"+version);

		return game.equals("TestFramework") && version.equals("0.00");
	}

	/**
	 * Create an account for a player.
	 */
	public AccountResult createAccount(String username, String password, String email) {
		Transaction trans=db.getTransaction();
		try {
			trans.begin();

			if(db.hasPlayer(trans,username)) {
				logger.warn("Account already exist: "+username);
				return new AccountResult(Result.FAILED_PLAYER_EXISTS, username);
			}

			db.addPlayer(trans, username, Hash.hash(password), email);

			trans.commit();
			return new AccountResult(Result.OK_CREATED, username);
		} catch(SQLException e) {
			try {
				trans.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			TestHelper.fail();
			return new AccountResult(Result.FAILED_EXCEPTION, username);
		}
	}

	/**
	 * Create a character for a player
	 */
	public CharacterResult createCharacter(String username, String character, RPObject template) {
		Transaction trans=db.getTransaction();
		try {
			RPObject player=new RPObject(template);
			
			player.put("name", character);
			player.put("version", "0.00");
			player.put("ATK",50);

			if(db.hasCharacter(trans, username, character)) {
				logger.warn("Character already exist: "+character);
				return new CharacterResult(Result.FAILED_PLAYER_EXISTS, character, player);
			}

			db.addCharacter(trans, username, character, player);
			return new CharacterResult(Result.OK_CREATED, character, player);
		} catch(Exception e) {
			try {
				trans.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			TestHelper.fail();
			return new CharacterResult(Result.FAILED_EXCEPTION, character, template);
		}
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
		RPObject result=world.remove(object.getID());
		TestHelper.assertNotNull(result);
		return true;
	}

	public boolean onInit(RPObject object) throws RPObjectInvalidException {
		object.put("zoneid","test");
		world.add(object);
		
		return true;
	}

	public void onTimeout(RPObject object) throws RPObjectNotFoundException {
		// TODO Auto-generated method stub

	}

	public void setContext(RPServerManager rpman) {
		// TODO Auto-generated method stub
	}
}

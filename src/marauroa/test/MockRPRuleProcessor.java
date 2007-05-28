package marauroa.test;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import marauroa.common.Log4J;
import marauroa.common.crypto.Hash;
import marauroa.common.game.AccountResult;
import marauroa.common.game.CharacterResult;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPEvent;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPObjectInvalidException;
import marauroa.common.game.RPObjectNotFoundException;
import marauroa.common.game.Result;
import marauroa.common.net.message.TransferContent;
import marauroa.server.game.db.DatabaseFactory;
import marauroa.server.game.db.IDatabase;
import marauroa.server.game.db.JDBCSQLHelper;
import marauroa.server.game.db.Transaction;
import marauroa.server.game.rp.IRPRuleProcessor;
import marauroa.server.game.rp.RPServerManager;
import marauroa.server.game.rp.RPWorld;
import marauroa.server.net.validator.ConnectionValidator;

public class MockRPRuleProcessor implements IRPRuleProcessor {

	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(MockRPRuleProcessor.class);

	private IDatabase db;

	private RPWorld world;

	private List<RPObject> players;

	private RPServerManager rpman;

	public MockRPRuleProcessor() {
		db = DatabaseFactory.getDatabase();
		JDBCSQLHelper sql = JDBCSQLHelper.get();
		world = MockRPWorld.get();

		try {
			Transaction transaction = db.getTransaction();
			transaction.begin();
			sql.runDBScript(transaction, "marauroa/test/clear.sql");
			sql.runDBScript(transaction, "marauroa/server/marauroa_init.sql");
			transaction.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			TestHelper.fail();
		}

		players = new LinkedList<RPObject>();
	}

	private static MockRPRuleProcessor rules;

	/**
	 * This method MUST be implemented in other for marauroa to be able to load
	 * this World implementation. There is no way of enforcing static methods on
	 * a Interface, so just keep this in mind when writting your own game.
	 * 
	 * @return an unique instance of world.
	 */
	public static IRPRuleProcessor get() {
		if (rules == null) {
			rules = new MockRPRuleProcessor();
		}

		return rules;
	}

	public void beginTurn() {
		for (RPObject player : players) {
			int x = 0;
			if (player.has("x")) {
				x = player.getInt("x");
			}

			player.put("x", x + 1);
			world.modify(player);
		}
	}

	/**
	 * Checks if game is correct. We expect TestFramework at 0.00 version.
	 */
	public boolean checkGameVersion(String game, String version) {
		TestHelper.assertEquals("TestFramework", game);
		TestHelper.assertEquals("0.00", version);

		logger.info("Client uses:" + game + ":" + version);

		return game.equals("TestFramework") && version.equals("0.00");
	}

	/**
	 * Create an account for a player.
	 */
	public AccountResult createAccount(String username, String password, String email) {
		Transaction trans = db.getTransaction();
		try {
			trans.begin();

			if (db.hasPlayer(trans, username)) {
				logger.warn("Account already exist: " + username);
				return new AccountResult(Result.FAILED_PLAYER_EXISTS, username);
			}

			db.addPlayer(trans, username, Hash.hash(password), email);

			trans.commit();
			return new AccountResult(Result.OK_CREATED, username);
		} catch (SQLException e) {
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
		Transaction trans = db.getTransaction();
		try {
			RPObject player = new RPObject(template);

			player.put("name", character);
			player.put("version", "0.00");
			player.put("ATK", 50);

			if (db.hasCharacter(trans, username, character)) {
				logger.warn("Character already exist: " + character);
				return new CharacterResult(Result.FAILED_PLAYER_EXISTS, character, player);
			}

			db.addCharacter(trans, username, character, player);
			return new CharacterResult(Result.OK_CREATED, character, player);
		} catch (Exception e) {
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
		logger.info("There are " + players.size() + " players in server");

	}

	public void execute(RPObject object, RPAction action) {
		logger.info(object.get("name") + " running " + action);

		RPEvent chat = new RPEvent("chat");
		chat.put("text", action.get("text"));
		object.addEvent(chat);

		world.modify(object);
	}

	public boolean onActionAdd(RPObject object, RPAction action, List<RPAction> actionList) {
		return true;
	}

	public boolean onExit(RPObject object) throws RPObjectNotFoundException {
		RPObject result = world.remove(object.getID());
		TestHelper.assertNotNull(result);

		players.remove(object);
		return true;
	}

	public boolean onInit(RPObject object) throws RPObjectInvalidException {
		object.put("zoneid", "test");
		world.add(object);

		players.add(object);
		
		TransferContent content=new TransferContent("test_content", 1, new byte[] {1,2,3,4,5,6,7,8,9,0});
		
		rpman.transferContent(object, content);

		return true;
	}

	public void onTimeout(RPObject object) throws RPObjectNotFoundException {
		logger.warn("Client " + object.get("name") + " timeout");
		onExit(object);
	}

	public void setContext(RPServerManager rpman) {
		this.rpman = rpman;
	}

	public ConnectionValidator getValidator() {
		return rpman.getValidator();
	}
}

package marauroa.common.game;

import marauroa.common.game.Definition.Type;

/**
 * helper class for test using RPClass
 *
 * @author hendrik
 */
public class RPClassTestHelper {

	public static void generateRPClasses() {
		if (!RPClass.hasRPClass("chat")) {
			final RPClass chat = new RPClass("chat");
			chat.addAttribute("text", Type.STRING);
		}
		if (!RPClass.hasRPClass("test")) {
			final RPClass chat = new RPClass("test");
			chat.addAttribute("value", Type.STRING);
		}
		if (!RPClass.hasRPClass("tax")) {
			final RPClass chat = new RPClass("tax");
			chat.addAttribute("bill", Type.STRING);
		}
	}
}

/**
 * This is possibly the most complex part of all the middleware that makes up Arianne.<br>
 * Role Playing Design is the determining factor on how easy is to create a new game for 
 * Arianne. 
 * <p>
 * We had to choose easing the creation of turn time limited based games. 
 * Arianne will work better with this kind of games (also known as realtime games).
 * <p>
 * Role Playing Design tries to be generic and game agnostic (independant of the game being made).
 * The very basic idea behind RPManager is:
 * <pre>
 forever
 {
 Execute Actions
 Send Perceptions
 Wait for next turn
 }
 * </pre>
 * To achieve this we use several classes:<ul>
 * <li>RPManager is coded in Marauroa and doesn't need to be modified.
 * <li>IRPRuleProcessor is the interface that you need to modify in order to personalize the actions that your game will execute.
 * <li>RPWorld is the class that you need to extend in order to implement the onInit and onFinish methods which personalize what happens when you initialise the server and what happens when you close the server.
 * <li>IRPZone is an interface that you could implement if you wanted to achive the highest personalization possible of the engine, however, I would extend MarauroaRPZone
 * </ul>
 */
package marauroa.server.game.rp;


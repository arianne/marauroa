/**
 * The package stores the game related code: glue logic and RP system framework.
 * <p>
 * It is build of several parts:<ul>
 * <li>GameServerManager that handles all the glue logic to login players, logout them, create
 * accounts, select characters, etc...
 * <li>IDatabase that implements the persistence engine.
 * <li>PlayerContainer that keeps information about players.
 * <li>RPServerManager that runs the RP framework.
 * </ul>
 */
package marauroa.server.game;
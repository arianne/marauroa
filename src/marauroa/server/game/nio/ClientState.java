package marauroa.server.game.nio;

/** This enum describe one of the possible state of the client. */
public enum ClientState {
	/** Connection is accepted but login stage is not completed. */
	CONNECTION_ACCEPTED, 
	/** Login identification is completed but still choosing character- */
	LOGIN_COMPLETE, 
	/** Client is already playing. */
	GAME_BEGIN,
	/** The client has requested logout and the petition is accepted */
	LOGOUT_ACCEPTED
}
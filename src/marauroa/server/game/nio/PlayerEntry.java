package marauroa.server.game.nio;

import java.nio.channels.SocketChannel;
import java.util.List;

import marauroa.common.crypto.RSAKey;
import marauroa.common.game.RPObject;
import marauroa.common.net.Message;
import marauroa.common.net.TransferContent;

public class PlayerEntry {
	/** 
	 * This class store the information needed to allow a secure login.
	 * Once login is completed the information is cleared.
	 * @author miguel
	 */
	static public class SecuredLoginInfo {
		byte[] clientNonceHash;
		byte[] serverNonce;
		byte[] clientNonce;
		String userName;
		byte[] password;
		RSAKey key;

		SecuredLoginInfo(RSAKey key) {
			this.key = key;
		}
	}

	/** The state in which this player is */
	public ClientState state;

	/** The runtime clientid */
	public int clientid;
	
	/** The client associated SocketChannel */
	public SocketChannel channel;

	/**
	 * The login Info. It is created after the first login message and
	 * destroyed after the login is finished.
	 */
	public SecuredLoginInfo loginInformations;

	/** The name of the player */
	public String username;

	/** The name of the choosen character */
	public String character;

	/** The object of the player */
	public RPObject object;

	/** A counter to detect dropped packets or bad order at client side */
	public int perception_counter;
	
	/** It is true if client notified us that it got out of sync */
	public boolean requestedSync;

	/** Contains the content that is going to be transfered to client */
	List<TransferContent> contentToTransfer;
	
	
	public PlayerEntry(SocketChannel channel) {
		this.channel=channel;
		
		clientid=Message.CLIENTID_INVALID;
		loginInformations=null;
		username=null;
		character=null;
		object=null;
		perception_counter=0;
		requestedSync=false;
		contentToTransfer=null;
	}	  

	/** Returns the next perception timestamp. */
	public int getPerceptionTimestamp() {
		return perception_counter++;
	}

	/** Clears the contents to be transfered */
	public void clearContent() {
		contentToTransfer = null;
	}

	public TransferContent getContent(String name) {
		if (contentToTransfer == null) {
			return null;
		}

		for (TransferContent item : contentToTransfer) {
			if (item.name.equals(name)) {
				return item;
			}
		}

		return null;
	}
	
	
}

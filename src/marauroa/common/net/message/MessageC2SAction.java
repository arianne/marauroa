/***************************************************************************
 *                   (C) Copyright 2003-2011 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.common.net.message;

import java.awt.HeadlessException;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.io.IOException;
import java.util.Map;

import marauroa.common.game.RPAction;
import marauroa.common.net.Channel;

/**
 * This message indicate the server the action the player's avatar wants to perform.
 *
 * @see marauroa.common.net.message.Message
 */
public class MessageC2SAction extends Message {

	/** The action to do will be understood by IRPRuleProcessor */
	private RPAction action;
	/** the priority of the action */
	byte priority = -1;

	/** Constructor for allowing creation of an empty message */
	public MessageC2SAction() {
		super(MessageType.C2S_ACTION, null);
	}

	/**
	 * Constructor with a TCP/IP source/destination of the message and action to send to serve.
	 *
	 * @param source
	 *            The TCP/IP address associated to this message
	 * @param action
	 *            the action that we sent to server.
	 */
	public MessageC2SAction(Channel source, RPAction action) {
		super(MessageType.C2S_ACTION, source);
		this.action = action;
	}

	/**
	 * This method returns the action
	 *
	 * @return the action
	 */
	public RPAction getRPAction() {
		return action;
	}

	/**
	 * This method returns the priority
	 *
	 * @return priority
	 */
	public byte getPriority() {
		return priority;
	}

	/**
	 * This method returns a String that represent the object
	 *
	 * @return a string representing the object.
	 */
	@Override
	public String toString() {
		return "Message (C2S Action) from (" + getAddress() + ") CONTENTS: (" + action.toString() + ")";
	}

	@Override
	public void writeObject(marauroa.common.net.OutputSerializer out) throws IOException {
		super.writeObject(out);
		action.writeObject(out);

		// get priority
		try {
			KeyboardFocusManager keyboardFocusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
			Window window = keyboardFocusManager.getActiveWindow();
			out.write((byte) ((window != null) ? 0 : 1));
		} catch (HeadlessException e) {
			out.write((byte) 2);
		}
	}

	@Override
	public void readObject(marauroa.common.net.InputSerializer in) throws IOException {
		super.readObject(in);
		action = (RPAction) in.readObject(new RPAction());

		if (in.available() >= 1) {
			priority = in.readByte();
		}

		if (type != MessageType.C2S_ACTION) {
			throw new IOException();
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void readFromMap(Map<String, Object> in) throws IOException {
		super.readFromMap(in);
		action = new RPAction();
		action.readFromMap((Map<String, Object>) in.get("a"));

		if (type != MessageType.C2S_ACTION) {
			throw new IOException();
		}
	}
}

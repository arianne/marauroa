/***************************************************************************
 *				(C) Copyright 2003-2010 - The Arianne Project			   *
 ***************************************************************************
 ***************************************************************************
 *																		   *
 *	 This program is free software; you can redistribute it and/or modify  *
 *	 it under the terms of the GNU General Public License as published by  *
 *	 the Free Software Foundation; either version 2 of the License, or	   *
 *	 (at your option) any later version.								   *
 *																		   *
 ***************************************************************************/
package marauroa.tools.protocolanalyser;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.nio.channels.SocketChannel;
import java.util.List;

import marauroa.common.Log4J;
import marauroa.common.Utility;
import marauroa.common.net.Decoder;
import marauroa.common.net.InvalidVersionException;
import marauroa.common.net.message.Message;


/**
 * A protocol analyser. You can sniff the network dump with tools like Wireshark or Tcpdump.
 * The let Wireshark "follow the tcp stream", and save the result in raw format.
 *
 * @author hendrik
 */
public class ProtocolAnalyser {
	private Decoder decoder = Decoder.get();

	/**
	 * reads a network dump file and prints the packages, their content and the
	 * way how Marauroa parses the data.
	 *
	 * @param is InputStream
	 * @throws IOException in case of an I/O error
	 * @throws InvalidVersionException if the version of Marauroa used to create
	 *         the dump and the one used to parse it are incompatible
	 */
	public void dump(InputStream is) throws IOException, InvalidVersionException {
		SocketChannel channel = new FakeSocketChannel(InetAddress.getByName("localhost"), 32123);

		while (true) {
			//read a packet from the opened file
			byte[] data = new byte[100];
			int cnt = is.read(data);
			System.out.println(Utility.dumpByteArray(data));
			List<Message> messages = decoder.decode(channel, data);
//			breakPoint(messages);
			System.out.println(messages);
			System.out.println();
			if (cnt < data.length) {
				break;
			}
		}
	}

	/*
	void breakPoint(List<Message> messages) {
		if (messages != null) {
			for (Message message : messages) {
				if (message instanceof MessageS2CPerception) {
					if (((MessageS2CPerception) message).getPerceptionTimestamp() == 61) {
						System.out.println("---------");
					}
				}
			}
		}
	}
*/

	/**
	 * reads a network dump file and prints the packages, their content and the
	 * way how Marauroa parses the data.
	 *
	 * @param args 
	 */
	public static void main(String[] args) throws IOException, InvalidVersionException {
		Log4J.init();

		if (args.length != 1) {
			System.out.println("java " + ProtocolAnalyser.class.getName() + " <filename>");
			return;
		}

		//open a file to read saved packets
		InputStream is = new FileInputStream(args[0]);

		new ProtocolAnalyser().dump(is);

		is.close();
	}
}

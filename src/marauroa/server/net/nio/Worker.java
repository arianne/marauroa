/* $Id: Worker.java,v 1.2 2006/12/18 21:11:06 arianne_rpg Exp $ */
/***************************************************************************
 *                      (C) Copyright 2003 - Marauroa                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.server.net.nio;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;



public class Worker implements Runnable, IWorker {
	private List<DataEvent> queue = new LinkedList<DataEvent>();
	private NioServer server;
	
	/* (non-Javadoc)
	 * @see IWorker#onConnect(java.nio.channels.SocketChannel)
	 */
	public void onConnect(SocketChannel socket) {		
	}
	
	/* (non-Javadoc)
	 * @see IWorker#onDisconnect(java.nio.channels.SocketChannel)
	 */
	public void onDisconnect(SocketChannel socket) {		
	}
	
	/* (non-Javadoc)
	 * @see IWorker#onData(NioServer, java.nio.channels.SocketChannel, byte[], int)
	 */
	public void onData(NioServer server, SocketChannel socket, byte[] data, int count) {
		byte[] dataCopy = new byte[count];
		System.arraycopy(data, 0, dataCopy, 0, count);
		synchronized(queue) {
			queue.add(new DataEvent(socket, dataCopy));
			queue.notify();
		}
	}
	
	public void setServer(NioServer server) {
		this.server=server;
	}

	public void run() {
		DataEvent dataEvent;
		
		while(true) {
			// Wait for data to become available
			synchronized(queue) {
				while(queue.isEmpty()) {
					try {
						queue.wait();
					} catch (InterruptedException e) {
					}
				}
				dataEvent = (DataEvent) queue.remove(0);
			}
			
			// Return to sender
			server.send(dataEvent.channel, dataEvent.data);
		}
	}
}

package marauroa.common.net;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import marauroa.common.net.message.Message;


/**
 * This class decode a stream of bytes and build a Marauroa message with it.
 * Decoder follow singleton pattern.
 * @author miguel
 */
public class Decoder {
	/** This class handle not completed messages. 
	 *  TODO: add some kind of timeout.*/
	class MessageParts {
		public int size;
		public List<byte[]> parts;
		
		public MessageParts(int size) {
			this.size=size;
			parts=new LinkedList<byte[]>();
		}

		/** Adds a new part to complete the message */
		public void add(byte[] data) {
			parts.add(data);			
		}

		/** Try to build the message for the channel using the existing parts or return null if it is not completed yet. */
		public Message build(SocketChannel channel) throws IOException, InvalidVersionException {
			int length=0;
			for(byte[] p: parts) {
				length+=p.length;
			}
			
			if(length!=size) {
				return null;
			}
			
			byte[] data=new byte[size-4];
			
			int offset=0;
			for(byte[] p: parts) {
				int amount=p.length-(offset==0?4:0);
				System.arraycopy(p, (offset==0?4:0) , data, offset, amount);
				offset+=amount;				
			}
			
			Message msg=msgFactory.getMessage(data, channel);
			return msg;
		}
	}
	
	private Map<SocketChannel, MessageParts> content;

	/** MessageFactory */
	private MessageFactory msgFactory;

	private static Decoder instance;

	public static Decoder get() {
		if(instance==null) {
			instance=new Decoder();
		}

		return instance;
	}

	public Decoder() {
		content=new HashMap<SocketChannel, MessageParts>();
		msgFactory = MessageFactory.getFactory();
	}

	/** 
	 * Decodes a message from a stream of bytes recieved from channel
	 * @param channel the socket from where data was recieved
	 * @param data the data recieved
	 * @return a message or null if it was not possible
	 * @throws IOException if there is a problem building the message
	 * @throws InvalidVersionException if the message version mismatch the expected version
	 */
	public Message decode(SocketChannel channel, byte[] data) throws IOException, InvalidVersionException {
		Message msg=null;

		int size = (data[0] & 0xFF)
		+ ((data[1] & 0xFF) << 8)
		+ ((data[2] & 0xFF) << 16)
		+ ((data[3] & 0xFF) << 24);

		if(data.length==size) {
			byte[] buffer=new byte[size - 4];
			System.arraycopy(data, 4, buffer, 0, size-4);

			msg = msgFactory.getMessage(buffer, channel);
		} else {
			/* Size is not the expected: We should store and wait for message completion. */
			MessageParts buffers=content.get(channel);
			
			if(buffers==null) {
				buffers=new MessageParts(size);
				content.put(channel, buffers);
			}
			
			buffers.add(data);
			
			msg=buffers.build(channel);
			
			if(msg!=null) {
				content.remove(channel);
			}
		}

		return msg;
	}
}

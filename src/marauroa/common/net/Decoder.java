package marauroa.common.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * This class decode a stream of bytes and build a Marauroa message with it.
 * Decoder follow singleton pattern.
 * @author miguel
 */
public class Decoder {
	// TODO: Fix me
	class MessageParts {
		public int size;
		public List<byte[]> parts;
		
		public MessageParts(int size) {
			this.size=size;
			parts=new LinkedList<byte[]>();
		}

		public void add(byte[] data) {
			parts.add(data);			
		}

		public Message build(SocketChannel channel) throws IOException, InvalidVersionException {
			int length=0;
			for(byte[] p: parts) {
				length+=p.length;
			}
			
			if(length!=size) {
				return null;
			}
			
			byte[] data=new byte[size-4];
			
			boolean first=true;
			for(byte[] p: parts) {
				System.arraycopy(p, (first?4:0) , data, 0, p.length-(first?4:0));
				first=false;
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
			/* TODO: Size is not the expected: We should store and wait for message completion. */
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

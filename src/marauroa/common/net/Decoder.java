package marauroa.common.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;


public class Decoder {
	private Map<InetSocketAddress,byte[]> content;

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
		content=null;
		msgFactory = MessageFactory.getFactory();		
	}
	
	public Message decode(InetSocketAddress address, byte[] data) throws IOException, InvalidVersionException {
		Message msg=null;
		
		int size = (data[0] & 0xFF)
		+ ((data[1] & 0xFF) << 8)
		+ ((data[2] & 0xFF) << 16)
		+ ((data[3] & 0xFF) << 24);
		
		if(data.length==size) {
			byte[] buffer=new byte[size-4];
			System.arraycopy(data, 4, buffer, 0, size-4);

			msg = msgFactory.getMessage(buffer, address);
		} else {
			/* TODO: Size is not the expected: We should store and wait for message completion. */
			throw new IOException();
		}
		
		return msg;
	}
}

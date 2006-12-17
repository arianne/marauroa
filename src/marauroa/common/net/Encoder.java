package marauroa.common.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class Encoder {
	private Encoder() {		
	}
	
	private static Encoder instance;
	public static Encoder get() {
		if(instance==null) {
			instance=new Encoder();
		}
		
		return instance;		
	}
	
	public byte[] encode(Message msg) throws IOException {
		int size=0;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		OutputSerializer s = new OutputSerializer(out);

		s.write(size);
		s.write(msg);
		
		byte[] data = out.toByteArray();
		
		size = data.length;
		data[0] = (byte) ((size >>  0) & 255);
		data[1] = (byte) ((size >>  8) & 255);
		data[2] = (byte) ((size >> 16) & 255);
		data[3] = (byte) ((size >> 24) & 255);

		return data;
	}
}

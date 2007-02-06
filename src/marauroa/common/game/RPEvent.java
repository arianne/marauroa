package marauroa.common.game;

import java.io.IOException;

import marauroa.common.net.InputSerializer;
import marauroa.common.net.OutputSerializer;

public class RPEvent implements marauroa.common.net.Serializable {
	private String name;
	private String value;
	
	public RPEvent() {		
	}

	public RPEvent(String name, String value) {
		put(name,value);
	}
	
	public void put(String name, String value) {
		this.name=name;
		this.value=value;
	}
	
	public String getKey() {
		return name;
	}
	
	public String getValue() {
		return value;
	}
	
	public int getInt() {
		return Integer.parseInt(value);
	}

	public float getFloat() {
		return Float.parseFloat(value);
	}

	public void readObject(InputSerializer in) throws IOException, ClassNotFoundException {
		name=in.read255LongString();
		value=in.read255LongString();
	}

	public void writeObject(OutputSerializer out) throws IOException {
		out.write255LongString(name);
		out.write255LongString(value);
	}
}

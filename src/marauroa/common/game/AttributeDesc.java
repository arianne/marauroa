package marauroa.common.game;

import java.util.HashMap;
import java.util.Map;

class AttributeDesc implements marauroa.common.net.Serializable {
	private static short lastCode = 0;

	private static Map<String, Short> attributeIntegerMap = new HashMap<String, Short>();

	private static short getValidCode(String name) {
		if (!attributeIntegerMap.containsKey(name)) {
			attributeIntegerMap.put(name, new Short(++lastCode));
		}

		return (attributeIntegerMap.get(name)).shortValue();
	}

	public AttributeDesc() {
	}

	public AttributeDesc(String name, byte type, byte flags) {
		code = getValidCode(name);
		this.name = name;
		this.type = type;
		this.flags = flags;
	}

	public short code;

	public String name;

	public byte type;

	public byte flags;

	public void writeObject(marauroa.common.net.OutputSerializer out)
			throws java.io.IOException {
		out.write(code);
		out.write(name);
		out.write(type);
		out.write(flags);
	}

	public void readObject(marauroa.common.net.InputSerializer in)
			throws java.io.IOException, java.lang.ClassNotFoundException {
		code = in.readShort();
		name = in.readString();
		type = in.readByte();
		flags = in.readByte();
	}
}

package marauroa.common.game;

import marauroa.common.game.Definition.Type;

/**
 * the default RPClass for lazy developers. You won't get any
 * advantages on the engine by using it.
 *
 * @author migel, hendrik
 */
class DefaultRPClass extends RPClass {

	/**
	 * generates the default RPClass
	 */
	DefaultRPClass() {
		super("");
	}

	@Override
	public short getCode(Definition.DefinitionClass clazz, String name) {
		return -1;
	}

	@Override
	public Definition getDefinition(Definition.DefinitionClass clazz, String name) {
		Definition def = new Definition(clazz);

		def.setCode((short) -1);
		def.setName(name);
		def.setType(Type.VERY_LONG_STRING);
		/*
		 * On Default RPClass we assume that strings that start with #
		 * are hidden.
		 */
		if (name.startsWith("#")) {
			def.setFlags(Definition.HIDDEN);
		} else {
			def.setFlags(Definition.STANDARD);
		}

		def.setCapacity((byte) -1);

		if (name.startsWith("!")) {
			def.setFlags(Definition.PRIVATE);
		}

		return def;
	}

}

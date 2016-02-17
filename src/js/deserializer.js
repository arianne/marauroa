/***************************************************************************
 *                   (C) Copyright 2011-2016 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/

"use strict";

var marauroa = window.marauroa || {};

marauroa.Deserializer = function(buffer) {
	var offset = 0;
	var view = new DataView(buffer);

	/**
	 * This method reads a byte from the Serializer
	 *
	 * @return the byte serialized
	 */
	this.readByte = function() {
		offset++;
		return view.getUInt8(offset - 1);
	}

	/**
	 * This method reads a short from the Serializer
	 *
	 * @return the short serialized
	 */
	this.readShort = function() {
		offset += 2;
		return view.getInt16(offset - 2, true);
	}

	/**
	 * This method reads a int from the Serializer
	 *
	 * @return the int serialized
	 */
	this.readInt = function() {
		offset += 4;
		return view.getInt32(offset - 4, true);
	}

	/**
	 * This method reads a byte array from the Serializer
	 *
	 * @return the byte array serialized
	 */
	this.readByteArray = function() {
		var size = view.getUint32(offset, true);
		offset += size + 4;
		return new DataView(buffer, offset - size, size);
	}

	/**
	 * This method reads a byte array of a maximum length of 255 entries
	 *
	 * @return the byte array serialized
	 */
	this.read255LongByteArray = function() {
		var size = view.getUint8(offset, true);
		offset += size + 1;
		return new DataView(buffer, offset - size, size);
	}

	/**
	 * This method reads a byte array of a maximum length of 65536 entries
	 *
	 * @return the byte array serialized
	 */
	this.read65536LongByteArray = function() {
		var size = view.getUint16(offset, true);
		offset += size + 1;
	}


	/**
	 * This method reads a float from the Serializer
	 *
	 * @return the float serialized
	 */
	this.readFloat = function() {
		offset += 4;
		return view.getFloat32(offset - 4, true);
	}

	/**
	 * This method reads a String from the Serializer
	 *
	 * @return the String serialized
	 */
	this.readString = function() {
		return new TextDecoder("utf-8").decode(this.readByteArray());
	}

	/**
	 * This method reads a short string (whose size is smaller than 255 bytes
	 * long)
	 *
	 * @return the String serialized
	 */
	this.read255LongString = function() {
		return new TextDecoder("utf-8").decode(this.read255LongByteArray());
	}

	/**
	 * This method reads a long string (whose size is smaller than 65536 bytes
	 * long)
	 *
	 * @return the String serialized
	 */
	this.read65536LongString = function() {
		return new TextDecoder("utf-8").decode(this.read65536LongByteArray());
	}

	/**
	 * This method reads a String array from the Serializer
	 *
	 * @return the String array serialized
	 */
	this.readStringArray = function() {
		var size = this.readInt();
		var res = [];
		for (var i = 0; i < size; i++) {
			res.push(this.readString());
		}
		return res;
	}

	/**
	 * reads an Attributes-object from the stream
	 *
	 * @param obj object to read into
	 * @return obj
	 */
	this.readAttributes = function(obj) {
		this.readString();
		var size = this.readInt();
		for (var i = 0; i < size; i++) {
			var code = this.readShort();
			if (code !== -1) {
				console.error("RPClass not supported, yet.");
				return obj;
			}
			var key = this.readString();
			var value = this.readString();
			obj[key] = value;
		}
		return obj;
	}

	this.readRPObject = function(obj) {
		this.readAttributes(obj);
		
		return obj;
	}
}

/**
 * created a deserializer from a deflated data stream that was encoded using base64.
 *
 * @param base64 base64 string
 * @return Deserializer
 */
marauroa.Deserializer.fromDeflatedBase64 = function(base64) {
    var d = window.atob(base64);
    var binary = window.RawDeflate.inflate(d.substring(2, d.length - 4));
    return marauroa.Deserializer.fromBinaryString(binary);
};

/**
 * created a deserializer from a data stream that was encoded using base64.
 *
 * @param base64 base64 string
 * @return Deserializer
 */
marauroa.Deserializer.fromBase64 = function(base64) {
    return marauroa.Deserializer.fromBinaryString(atob(base64));
};


/**
 * created a deserializer from a data stream
 *
 * @param binary binary string
 * @return Deserializer
 */
marauroa.Deserializer.fromBinaryString = function(binary) {
    var len = binary.length;
    var bytes = new Uint8Array( len );
    for (var i = 0; i < len; i++) {
        bytes[i] = binary.charCodeAt(i);
    }
    return new marauroa.Deserializer(bytes.buffer);
};
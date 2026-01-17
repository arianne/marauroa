/***************************************************************************
 *                   (C) Copyright 2011-2026 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/

export class Deserializer {
	private offset: number = 0;
	private view: DataView;

	constructor(private buffer: ArrayBufferLike) {
		this.offset = 0;
		this.view = new DataView(buffer);
	}

	/**
	 * This method reads a byte from the Serializer
	 *
	 * @return the byte serialized
	 */
	readByte() {
		this.offset++;
		return this.view.getUint8(this.offset - 1);
	}

	/**
	 * This method reads a short from the Serializer
	 *
	 * @return the short serialized
	 */
	readShort() {
		this.offset += 2;
		return this.view.getInt16(this.offset - 2, true);
	}

	/**
	 * This method reads a int from the Serializer
	 *
	 * @return the int serialized
	 */
	readInt() {
		this.offset += 4;
		return this.view.getInt32(this.offset - 4, true);
	}

	/**
	 * This method reads a byte array from the Serializer
	 *
	 * @return the byte array serialized
	 */
	readByteArray() {
		let size = this.view.getUint32(this.offset, true);
		this.offset += size + 4;
		return new DataView(this.buffer, this.offset - size, size);
	}

	/**
	 * This method reads a byte array of a maximum length of 255 entries
	 *
	 * @return the byte array serialized
	 */
	read255LongByteArray() {
		let size = this.view.getUint8(this.offset);
		this.offset += size + 1;
		return new DataView(this.buffer, this.offset - size, size);
	}

	/**
	 * This method reads a byte array of a maximum length of 65536 entries
	 *
	 * @return the byte array serialized
	 */
	read65536LongByteArray() {
		let size = this.view.getUint16(this.offset, true);
		this.offset += size + 1;
		return new DataView(this.buffer, this.offset - size, size);
	}


	/**
	 * This method reads a float from the Serializer
	 *
	 * @return the float serialized
	 */
	readFloat() {
		this.offset += 4;
		return this.view.getFloat32(this.offset - 4, true);
	}

	/**
	 * This method reads a String from the Serializer
	 *
	 * @return the String serialized
	 */
	readString() {
		return new TextDecoder("utf-8").decode(this.readByteArray());
	}

	/**
	 * This method reads a short string (whose size is smaller than 255 bytes
	 * long)
	 *
	 * @return the String serialized
	 */
	read255LongString() {
		return new TextDecoder("utf-8").decode(this.read255LongByteArray());
	}

	/**
	 * This method reads a long string (whose size is smaller than 65536 bytes
	 * long)
	 *
	 * @return the String serialized
	 */
	read65536LongString() {
		return new TextDecoder("utf-8").decode(this.read65536LongByteArray());
	}

	/**
	 * This method reads a String array from the Serializer
	 *
	 * @return the String array serialized
	 */
	readStringArray() {
		let size = this.readInt();
		let res: string[] = [];
		for (let i = 0; i < size; i++) {
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
	readAttributes(obj: Record<string, string>) {
		this.readString();
		let size = this.readInt();
		for (let i = 0; i < size; i++) {
			let code = this.readShort();
			if (code !== -1) {
				console.error("RPClass not supported, yet.");
				return obj;
			}
			let key = this.readString();
			let value = this.readString();
			obj[key] = value;
		}
		return obj;
	}

	readRPObject(obj: Record<string, string>) {
		this.readAttributes(obj);

		return obj;
	}

	static binaryStringToUint(binary: string) {
		let len = binary.length;
		let bytes = new Uint8Array(len);
		for (let i = 0; i < len; i++) {
			bytes[i] = binary.charCodeAt(i);
		}
		return bytes.buffer
	}

	/**
	 * created a deserializer from a deflated data stream that was encoded using base64.
	 *
	 * @param base64 base64 string
	 * @return Deserializer
	 */
	static fromDeflatedBase64(base64: string) {
		let d = window.atob(base64);
		let buffer = Deserializer.binaryStringToUint(d.substring(2, d.length - 4));
		let inflate: any = new (window as any)["Zlib"]["RawInflate"](buffer);
		let data = inflate["decompress"]();
		return new Deserializer(data.buffer);
	}

	/**
	 * created a deserializer from a data stream that was encoded using base64.
	 *
	 * @param base64 base64 string
	 * @return Deserializer
	 */
	static fromBase64(base64: string) {
		return Deserializer.fromBinaryString(atob(base64));
	}


	/**
	 * created a deserializer from a data stream
	 *
	 * @param binary binary string
	 * @return Deserializer
	 */
	static fromBinaryString(binary: string) {
		let len = binary.length;
		let bytes = new Uint8Array(len);
		for (let i = 0; i < len; i++) {
			bytes[i] = binary.charCodeAt(i);
		}
		return new Deserializer(bytes.buffer);
	}
}
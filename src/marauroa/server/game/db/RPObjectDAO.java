/* $Id: RPObjectDAO.java,v 1.16 2010/06/11 21:18:32 nhnb Exp $ */
/***************************************************************************
 *                   (C) Copyright 2003-2009 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.server.game.db;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import marauroa.common.Log4J;
import marauroa.common.game.DetailLevel;
import marauroa.common.game.RPObject;
import marauroa.common.net.InputSerializer;
import marauroa.common.net.NetConst;
import marauroa.common.net.OutputSerializer;
import marauroa.server.db.DBTransaction;
import marauroa.server.db.TransactionPool;
import marauroa.server.game.rp.RPObjectFactory;

/**
 * data access object for RPObjects
 *
 * @author miguel, hendrik
 */
public class RPObjectDAO {
	private static final marauroa.common.Logger logger = Log4J.getLogger(RPObjectDAO.class);

	/** factory for creating object instances */
	protected RPObjectFactory factory;

	/**
	 * creates a new RPObjectDAO
	 *
	 * @param factory factory for creating object instances
	 */
	protected RPObjectDAO(RPObjectFactory factory) {
		this.factory = factory;
	}

	/**
	 * loads an RPObject form the database, using the factory to create the correct subclass
	 *
	 * @param transaction DBTransaction
	 * @param objectid database-id of the RPObject
	 * @return RPObject
	 * @throws IOException in case of an input/output error
	 * @throws SQLException in case of an database error
	 */
	public RPObject loadRPObject(DBTransaction transaction, int objectid) throws SQLException, IOException {
		return loadRPObject(transaction, objectid, true);
	}

	/**
	 * loads an RPObject form the database
	 *
	 * @param transaction DBTransaction
	 * @param objectid database-id of the RPObject
	 * @param transform use the factory to create a subclass of RPObject
	 * @return RPObject
	 * @throws IOException in case of an input/output error
	 * @throws SQLException in case of an database error
	 */
	public RPObject loadRPObject(DBTransaction transaction, int objectid, boolean transform) throws SQLException, IOException {
		String query = "select data, protocol_version from rpobject where object_id=[objectid]";
		logger.debug("loadRPObject is executing query " + query);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("objectid", objectid);

		ResultSet resultSet = transaction.query(query, params);

		if (resultSet.next()) {
		    Blob data = resultSet.getBlob("data");
			int protocolVersion = NetConst.FIRST_VERSION_WITH_MULTI_VERSION_SUPPORT - 1;
			Object temp = resultSet.getObject("protocol_version");
			if (temp != null) {
				protocolVersion = ((Integer) temp).intValue(); 
			}
			RPObject object = readRPObject(objectid, data, protocolVersion, transform);
		    resultSet.close();
			return object;
		}
	    resultSet.close();
		return null;
	}

	/**
	 * reads an RPObject that has already been loaded from the database
	 *
	 * @param objectid  object_id of RPObject
	 * @param data      blob data
	 * @param protocolVersion version of serialization protocol
	 * @param transform should it be transformed using the RPObjectFactory
	 * @return RPBobject
	 * @throws IOException in case of an input/output error
	 * @throws SQLException in case of an database error
	 */
	public RPObject readRPObject(int objectid, Blob data, int protocolVersion, boolean transform) throws SQLException, IOException {
	    InputStream input = data.getBinaryStream();
	    ByteArrayOutputStream output = new ByteArrayOutputStream();

	    // set read buffer size
	    byte[] rb = new byte[1024];
	    int ch = 0;
	    // process blob
	    while ((ch = input.read(rb)) != -1) {
	    	output.write(rb, 0, ch);
	    }
	    byte[] content = output.toByteArray();
	    input.close();
	    output.close();

	    ByteArrayInputStream inStream = new ByteArrayInputStream(content);
	    InflaterInputStream szlib = new InflaterInputStream(inStream, new Inflater());
	    InputSerializer inputSerializer = new InputSerializer(szlib);
	    inputSerializer.setProtocolVersion(protocolVersion);

	    RPObject object = (RPObject) inputSerializer.readObject(new RPObject());

	    if (transform) {
	    	object = factory.transform(object);
	    }

	    object.put("#db_id", objectid);
	    return object;
    }

	/**
	 * deletes an RPObject from the database
	 *
	 * @param transaction DBTransaction
	 * @param objectid database-id of the RPObject
	 * @return objectid
	 * @throws SQLException in case of an database error
	 */
	public int removeRPObject(DBTransaction transaction, int objectid) throws SQLException {
		String query = "delete from rpobject where object_id=[objectid]";
		logger.debug("removeRPObject is executing query " + query);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("objectid", objectid);

		transaction.execute(query, params);

		return objectid;
	}

	/**
	 * does the rpobject with the specified database id exist?
	 *
	 * @param transaction DBTransaction
	 * @param objectid database-id of the RPObject
	 * @return true, if it exists; false otherwise
	 * @throws SQLException in case of an database error
	 */
	public boolean hasRPObject(DBTransaction transaction, int objectid) throws SQLException {
		String query = "select count(*) as amount from rpobject where object_id=[objectid]";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("objectid", objectid);

		logger.debug("hasRPObject is executing query " + query);

		int count = transaction.querySingleCellInt(query, params);
		return count > 0;
	}

	/**
	 * saves an RPObject to the database
	 *
	 * @param transaction DBTransaction
	 * @param object RPObject to save
	 * @return objectid
	 * @throws IOException in case of an input/output error
	 * @throws SQLException in case of an database error
	 */
	public int storeRPObject(DBTransaction transaction, RPObject object) throws IOException, SQLException {
		ByteArrayOutputStream array = new ByteArrayOutputStream();
		DeflaterOutputStream out_stream = new DeflaterOutputStream(array);
		OutputSerializer serializer = new OutputSerializer(out_stream);
		int protocolVersion = serializer.getProtocolVersion();

		try {
			object.writeObject(serializer, DetailLevel.FULL);
			out_stream.close();
		} catch (IOException e) {
			logger.warn("Error while serializing rpobject: " + object, e);
			throw e;
		}

		// setup stream for blob
		ByteArrayInputStream inStream = new ByteArrayInputStream(array.toByteArray());

		int object_id = -1;

		if (object.has("#db_id")) {
			object_id = object.getInt("#db_id");
		}

		String query;
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("object_id", object_id);
		params.put("protocolVersion", protocolVersion);


		if (object_id != -1 && hasRPObject(transaction, object_id)) {
			query = "update rpobject set data=?, protocol_version=[protocolVersion] where object_id=[object_id]";
		} else {
			query = "insert into rpobject (data, protocol_version) values(?, [protocolVersion])";
		}
		logger.debug("storeRPObject is executing query " + query);

		transaction.execute(query, params, inStream);


		// If object is new, get the objectid we gave it.
		if (object_id == -1) {
			object_id = transaction.getLastInsertId("rpobject", "id");

			// We alter the original object to add the proper db_id
			object.put("#db_id", object_id);
		} else {
			object_id = object.getInt("#db_id");
		}

		return object_id;
	}

	/**
	 * loads an RPObject form the database, using the factory to create the correct subclass
	 *
	 * @param objectid database-id of the RPObject
	 * @param transform use the factory to create a subclass of RPObject
	 * @return RPObject
	 * @throws IOException in case of an input/output error
	 * @throws SQLException in case of an database error
	 */
	public RPObject loadRPObject(int objectid, boolean transform) throws SQLException, IOException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			RPObject res = loadRPObject(transaction, objectid, transform);
			return res;
		} finally {
			TransactionPool.get().commit(transaction);
		}
	}

	/**
	 * loads an RPObject form the database
	 *
	 * @param objectid database-id of the RPObject
	 * @return RPObject
	 * @throws IOException in case of an input/output error
	 * @throws SQLException in case of an database error
	 */
	public RPObject loadRPObject(int objectid) throws SQLException, IOException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			RPObject res = loadRPObject(transaction, objectid, true);
			return res;
		} finally {
			TransactionPool.get().commit(transaction);
		}
	}

	/**
	 * deletes an RPObject from the database
	 *
	 * @param objectid database-id of the RPObject
	 * @return objectid
	 * @throws SQLException in case of an database error
	 */
	public int removeRPObject(int objectid) throws SQLException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			int res = removeRPObject(transaction, objectid);
			return res;
		} finally {
			TransactionPool.get().commit(transaction);
		}
	}

	/**
	 * does the rpobject with the specified database id exist?
	 *
	 * @param objectid database-id of the RPObject
	 * @return true, if it exists; false otherwise
	 * @throws SQLException in case of an database error
	 */
	public boolean hasRPObject(int objectid) throws SQLException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			boolean res = hasRPObject(transaction, objectid);
			return res;
		} finally {
			TransactionPool.get().commit(transaction);
		}
	}

	/**
	 * saves an RPObject to the database
	 *
	 * @param object RPObject to save
	 * @return objectid
	 * @throws IOException in case of an input/output error
	 * @throws SQLException in case of an database error
	 */
	public int storeRPObject(RPObject object) throws IOException, SQLException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			int res = storeRPObject(transaction, object);
			return res;
		} finally {
			TransactionPool.get().commit(transaction);
		}
	}
}

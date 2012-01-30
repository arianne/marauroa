/***************************************************************************
 *                   (C) Copyright 2003-2010 - Marauroa                    *
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
import marauroa.common.game.IRPZone;
import marauroa.common.game.RPObject;
import marauroa.common.net.InputSerializer;
import marauroa.common.net.NetConst;
import marauroa.common.net.OutputSerializer;
import marauroa.server.db.DBTransaction;
import marauroa.server.db.StringChecker;
import marauroa.server.db.TransactionPool;
import marauroa.server.game.rp.RPObjectFactory;

/**
 * data access object for RPZones
 *
 * @author miguel, hendrik
 */
public class RPZoneDAO {
	private static final marauroa.common.Logger logger = Log4J.getLogger(RPZoneDAO.class);

	/** factory for creating object instances */
	protected RPObjectFactory factory;

	/**
	 * creates a new RPZoneDAO
	 *
	 * @param factory factory for creating object instances
	 */
	protected RPZoneDAO(RPObjectFactory factory) {
		this.factory = factory;
	}

	/**
	 * loads storable objects for the specified zone from the database
	 *
	 * @param transaction DBTransaction
	 * @param zone IRPZone
	 * @throws IOException in case of an input/output error
	 * @throws SQLException in case of an database error
	 */
	public void loadRPZone(DBTransaction transaction, IRPZone zone) throws SQLException, IOException {
		String zoneid = zone.getID().getID();

		String query = "select data, protocol_version from rpzone where zone_id='[zoneid]'";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("zoneid", zoneid);
		logger.debug("loadRPZone is executing query " + query);

		ResultSet resultSet = transaction.query(query, params);

		if (resultSet.next()) {
			Blob data = resultSet.getBlob("data");
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

			int protocolVersion = NetConst.FIRST_VERSION_WITH_MULTI_VERSION_SUPPORT - 1;
			Object temp = resultSet.getObject("protocol_version");
			if (temp != null) {
				protocolVersion = ((Integer) temp).intValue(); 
			}
			inputSerializer.setProtocolVersion(protocolVersion);

			int amount = inputSerializer.readInt();

			for (int i = 0; i < amount; i++) {
				try {
					RPObject object = factory.transform((RPObject) inputSerializer.readObject(new RPObject()));

					if (object != null) {
						/* Give the object a valid id and add it */
						zone.assignRPObjectID(object);
						zone.add(object);
					}
				} catch (Exception e) {
					logger.error("Problem loading RPZone: ", e);
				}
			}
		}
		resultSet.close();
	}

	/**
	 * saves storable objects for the specified zone to the database
	 *
	 * @param transaction DBTransaction
	 * @param zone IRPZone
	 * @throws IOException in case of an input/output error
	 * @throws SQLException in case of an database error
	 */
	public void storeRPZone(DBTransaction transaction, IRPZone zone) throws IOException, SQLException {
		storeRPZone(transaction, zone, zone);
	}


	/**
	 * saves storable objects for the specified zone to the database
	 *
	 * @param transaction DBTransaction
	 * @param zone IRPZone
	 * @param content the RPObjects of that zone
	 * @throws IOException in case of an input/output error
	 * @throws SQLException in case of an database error
	 */
	public void storeRPZone(DBTransaction transaction, IRPZone zone, Iterable<RPObject> content) throws IOException, SQLException {
		String zoneid = zone.getID().getID();
		if (!StringChecker.validString(zoneid)) {
			throw new SQLException("Invalid string zoneid=(" + zoneid + ")");
		}

		ByteArrayOutputStream array = new ByteArrayOutputStream();
		DeflaterOutputStream out_stream = new DeflaterOutputStream(array);
		OutputSerializer os = new OutputSerializer(out_stream);

		/* compute how many storable objects exists in zone. */
		int amount = 0;
		for (RPObject object : content) {
			if (object.isStorable()) {
				amount++;
			}
		}

		os.write(amount);

		boolean empty = true;
		for (RPObject object : content) {
			if (object.isStorable()) {
				object.writeObject(os, DetailLevel.FULL);
				empty = false;
			}
		}

		out_stream.close();

		/* Setup the stream for a blob */
		ByteArrayInputStream inStream = new ByteArrayInputStream(array.toByteArray());

		String query;

		if (hasRPZone(transaction, zone.getID())) {
			query = "update rpzone set data=?, protocol_version=[protocolVersion] where zone_id='[zoneid]'";
		} else {
			// do not add empty zones
			if (empty) {
				return;
			}
			query = "insert into rpzone(zone_id, data, protocol_version) values('[zoneid]', ?, [protocolVersion])";
		}
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("zoneid", zoneid);
		params.put("protocolVersion", os.getProtocolVersion());
		logger.debug("storeRPZone is executing query " + query);

		transaction.execute(query, params, inStream);
	}


	/**
	 * is the specified zone saved to the database
	 *
	 * @param transaction DBTransaction
	 * @param zone id of zone
	 * @return true, if the zone exists; false otherwise
	 * @throws SQLException in case of an database error
	 */
	public boolean hasRPZone(DBTransaction transaction, IRPZone.ID zone) throws SQLException {
		String query = "SELECT count(*) as amount FROM rpzone where zone_id='[zoneid]'";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("zoneid", zone.getID());
		int count = transaction.querySingleCellInt(query, params);
		return count > 0;
	}


	/**
	 * loads storable objects for the specified zone from the database
	 *
	 * @param zone IRPZone
	 * @throws IOException in case of an input/output error
	 * @throws SQLException in case of an database error
	 */
	public void loadRPZone(IRPZone zone) throws SQLException, IOException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			loadRPZone(transaction, zone);
		} finally {
			TransactionPool.get().commit(transaction);
		}
	}

	/**
	 * saves storable objects for the specified zone to the database
	 *
	 * @param zone IRPZone
	 * @throws IOException in case of an input/output error
	 * @throws SQLException in case of an database error
	 */
	public void storeRPZone(IRPZone zone) throws IOException, SQLException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			storeRPZone(transaction, zone);
		} finally {
			TransactionPool.get().commit(transaction);
		}
	}

	/**
	 * is the specified zone saved to the database
	 *
	 * @param zone id of zone
	 * @return true, if the zone exists; false otherwise
	 * @throws SQLException in case of an database error
	 */
	public boolean hasRPZone(IRPZone.ID zone) throws SQLException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			boolean res = hasRPZone(transaction, zone);
			return res;
		} finally {
			TransactionPool.get().commit(transaction);
		}
	}
}

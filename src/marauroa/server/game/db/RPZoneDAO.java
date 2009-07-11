/* $Id: RPZoneDAO.java,v 1.7 2009/07/11 11:52:44 nhnb Exp $ */
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
import marauroa.common.game.IRPZone;
import marauroa.common.game.RPObject;
import marauroa.common.net.InputSerializer;
import marauroa.common.net.OutputSerializer;
import marauroa.server.db.DBTransaction;
import marauroa.server.db.StringChecker;
import marauroa.server.db.TransactionPool;
import marauroa.server.game.rp.RPObjectFactory;

public class RPZoneDAO {
	private static final marauroa.common.Logger logger = Log4J.getLogger(RPZoneDAO.class);

	protected RPObjectFactory factory;

	public RPZoneDAO(RPObjectFactory factory) {
		this.factory = factory;
	}

	public void loadRPZone(DBTransaction transaction, IRPZone zone) throws SQLException, IOException {
		String zoneid = zone.getID().getID();

		String query = "select data from rpzone where zone_id='[zoneid]'";
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
			InputSerializer inser = new InputSerializer(szlib);

			int amount = inser.readInt();

			for (int i = 0; i < amount; i++) {
				try {
					RPObject object = factory.transform((RPObject) inser.readObject(new RPObject()));

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
	}

	public void storeRPZone(DBTransaction transaction, IRPZone zone) throws IOException, SQLException {
		String zoneid = zone.getID().getID();
		if (!StringChecker.validString(zoneid)) {
			throw new SQLException("Invalid string zoneid=(" + zoneid + ")");
		}

		ByteArrayOutputStream array = new ByteArrayOutputStream();
		DeflaterOutputStream out_stream = new DeflaterOutputStream(array);
		OutputSerializer os = new OutputSerializer(out_stream);

		/* compute how many storable objects exists in zone. */
		int amount = 0;
		for (RPObject object : zone) {
			if (object.isStorable()) {
				amount++;
			}
		}

		os.write(amount);

		boolean empty = true;
		for (RPObject object : zone) {
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
			query = "update rpzone set data=? where zone_id='[zoneid]'";
		} else {
			// do not add empty zones
			if (empty) {
				return;
			}
			query = "insert into rpzone(zone_id, data) values('[zoneid]',?)";
		}
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("zoneid", zoneid);
		logger.debug("storeRPZone is executing query " + query);

		transaction.execute(query, params, inStream);
	}

	public boolean hasRPZone(DBTransaction transaction, IRPZone.ID zone) throws SQLException {
		String query = "SELECT count(*) as amount FROM rpzone where zone_id='[zoneid]'";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("zoneid", zone.getID());
		int count = transaction.querySingleCellInt(query, params);
		return count > 0;
	}


	public void loadRPZone(IRPZone zone) throws SQLException, IOException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		loadRPZone(transaction, zone);
		TransactionPool.get().commit(transaction);		
	}

	public void storeRPZone(IRPZone zone) throws IOException, SQLException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		storeRPZone(transaction, zone);
		TransactionPool.get().commit(transaction);	
	}

	public boolean hasRPZone(IRPZone.ID zone) throws SQLException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		boolean res = hasRPZone(transaction, zone);
		TransactionPool.get().commit(transaction);
		return res;
	}
}

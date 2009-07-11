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
import marauroa.common.net.OutputSerializer;
import marauroa.server.db.DBTransaction;
import marauroa.server.db.TransactionPool;
import marauroa.server.game.rp.RPObjectFactory;

public class RPObjectDAO {
	private static final marauroa.common.Logger logger = Log4J.getLogger(RPObjectDAO.class);

	protected RPObjectFactory factory;

	public RPObjectDAO(RPObjectFactory factory) {
		this.factory = factory;
	}
	
	public RPObject loadRPObject(DBTransaction transaction, int objectid) throws SQLException, IOException {
		String query = "select data from rpobject where object_id=[objectid]";
		logger.debug("loadRPObject is executing query " + query);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("objectid", objectid);

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

			RPObject object = null;

			object = factory.transform((RPObject) inser.readObject(new RPObject()));
			object.put("#db_id", objectid);

			return object;
		}

		return null;
	}

	public int removeRPObject(DBTransaction transaction, int objectid) throws SQLException {
		String query = "delete from rpobject where object_id=[objectid]";
		logger.debug("removeRPObject is executing query " + query);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("objectid", objectid);

		transaction.execute(query, params);

		return objectid;
	}

	public boolean hasRPObject(DBTransaction transaction, int objectid) throws SQLException {
		String query = "select count(*) as amount from rpobject where object_id=[objectid]";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("objectid", objectid);

		logger.debug("hasRPObject is executing query " + query);

		int count = transaction.querySingleCellInt(query, params);
		return count > 0;
	}

	public int storeRPObject(DBTransaction transaction, RPObject object) throws IOException, SQLException {
		ByteArrayOutputStream array = new ByteArrayOutputStream();
		DeflaterOutputStream out_stream = new DeflaterOutputStream(array);
		OutputSerializer serializer = new OutputSerializer(out_stream);

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

		if (object_id != -1 && hasRPObject(transaction, object_id)) {
			query = "update rpobject set data=? where object_id=[object_id]";
		} else {
			query = "insert into rpobject (object_id, data) values(null, ?)";
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

	public RPObject loadRPObject(int objectid) throws SQLException, IOException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		RPObject res = loadRPObject(transaction, objectid);
		TransactionPool.get().commit(transaction);
		return res;
	}

	public int removeRPObject(int objectid) throws SQLException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		int res = removeRPObject(transaction, objectid);
		TransactionPool.get().commit(transaction);
		return res;
	}

	public boolean hasRPObject(int objectid) throws SQLException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		boolean res = hasRPObject(transaction, objectid);
		TransactionPool.get().commit(transaction);
		return res;
	}

	public int storeRPObject(RPObject object) throws IOException, SQLException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		int res = storeRPObject(transaction, object);
		TransactionPool.get().commit(transaction);
		return res;
	}
}

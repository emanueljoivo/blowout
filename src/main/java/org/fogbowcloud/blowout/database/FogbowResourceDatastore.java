package org.fogbowcloud.blowout.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.fogbowcloud.blowout.core.model.Specification;
import org.fogbowcloud.blowout.core.constants.AppPropertiesConstants;
import org.fogbowcloud.blowout.infrastructure.model.FogbowResource;
import org.json.JSONException;
import org.json.JSONObject;

public class FogbowResourceDatastore {

	private static final Logger LOGGER = Logger.getLogger(FogbowResourceDatastore.class);

	protected static final String FOGBOW_RESOURCE_TABLE_NAME = "fogbow_resource_store";

	protected static final String RESOURCE_ID = "resource_id";
	protected static final String ORDER_ID = "order_id";
	protected static final String INSTANCE_ID = "instance_id";
	protected static final String SPEC = "spec";

	protected static final String MANAGER_DATASTORE_SQLITE_DRIVER = "org.sqlite.JDBC";

	private static final String INSERT_FOGBOW_RESOURCE_SQL = "INSERT INTO " + FOGBOW_RESOURCE_TABLE_NAME
			+ " VALUES(?,?,?,?)";
	private static final String UPDATE_FOGBOW_RESOURCE = "UPDATE " + FOGBOW_RESOURCE_TABLE_NAME + " SET " + ORDER_ID
			+ "=? , " + INSTANCE_ID + "=? WHERE " + RESOURCE_ID + "=?";
	private static final String SELECT_REQUEST_ID = "SELECT * FROM " + FOGBOW_RESOURCE_TABLE_NAME;
	private static final String DELETE_ALL_CONTENT_SQL = "DELETE FROM " + FOGBOW_RESOURCE_TABLE_NAME;
	private static final String DELETE_BY_RESOURCE_ID_SQL = DELETE_ALL_CONTENT_SQL + " WHERE " + RESOURCE_ID + "=? ";

	private final String dataStoreURL;

	public FogbowResourceDatastore(Properties properties) {
		this.dataStoreURL = properties.getProperty(AppPropertiesConstants.DB_DATASTORE_URL);

		Statement statement = null;
		Connection connection = null;
		try {
			LOGGER.debug("DatastoreURL: " + dataStoreURL);

			Class.forName(MANAGER_DATASTORE_SQLITE_DRIVER);

			connection = getConnection();
			connection.setAutoCommit(false);
			statement = connection.createStatement();
			statement.execute("CREATE TABLE IF NOT EXISTS " + FOGBOW_RESOURCE_TABLE_NAME + "(" + RESOURCE_ID
					+ " VARCHAR(255) PRIMARY KEY," + ORDER_ID + " VARCHAR(255)," + INSTANCE_ID + " VARCHAR(255)," + SPEC
					+ " TEXT " + ")");
			statement.close();
			connection.commit();

		} catch (Exception e) {
			LOGGER.error("Error while initializing the DataStore.", e);
		} finally {
			close(statement, connection);
		}
	}

	public Connection getConnection() throws SQLException {
		try {
			return DriverManager.getConnection(this.dataStoreURL);
		} catch (SQLException e) {
			LOGGER.error("Error while getting a new connection from the connection pool.", e);
			throw e;
		}
	}

	private void close(Statement statement, Connection conn) {
		if (statement != null) {
			try {
				if (!statement.isClosed()) {
					statement.close();
				}
			} catch (SQLException e) {
				LOGGER.error("Couldn't close statement");
			}
		}

		if (conn != null) {
			try {
				if (!conn.isClosed()) {
					conn.close();
				}
			} catch (SQLException e) {
				LOGGER.error("Couldn't close connection");
			}
		}
	}

	public boolean addFogbowResource(FogbowResource fogbowResource) {
		LOGGER.debug("Adding resource id: " + fogbowResource.getId());
		PreparedStatement insertResourceStatement = null;
		Connection connection = null;
		try {
			connection = getConnection();
			connection.setAutoCommit(false);

			insertResourceStatement = prepare(connection, INSERT_FOGBOW_RESOURCE_SQL);

			setResourcers(insertResourceStatement, fogbowResource);

			boolean result = insertResourceStatement.execute();
			connection.commit();
			return result;
		} catch (SQLException e) {
			LOGGER.error("Couldn't store the current resource id", e);
			try {
				if (connection != null) {
					connection.rollback();
				}
			} catch (SQLException e1) {
				LOGGER.error("Couldn't rollback transaction.", e1);
			}
			return false;
		} finally {
			close(insertResourceStatement, connection);
		}
	}

	public boolean addResourceIds(List<FogbowResource> fogbowResources) {
		LOGGER.debug("Adding resource ids");
		PreparedStatement insertResourcesStatement = null;
		Connection connection = null;
		try {
			connection = getConnection();
			connection.setAutoCommit(false);

			insertResourcesStatement = prepare(connection, INSERT_FOGBOW_RESOURCE_SQL);

			for (FogbowResource fogbowResource : fogbowResources) {

				setResourcers(insertResourcesStatement, fogbowResource);

				insertResourcesStatement.addBatch();
				connection.rollback();
			}

			if (hasBatchExecutionError(insertResourcesStatement.executeBatch())) {
				return false;
			}
			connection.commit();
			return true;
		} catch (SQLException e) {
			dealWithSQLException(connection, "Couldn't store the current resource id", e);
			return false;
		} finally {
			close(insertResourcesStatement, connection);
		}
	}

	public boolean updateFogbowResource(FogbowResource fogbowResource) {
		LOGGER.debug("Updating resource id: " + fogbowResource.getId());
		PreparedStatement updateFogbowResourceStatment = null;
		Connection connection = null;
		try {
			connection = getConnection();
			connection.setAutoCommit(false);

			updateFogbowResourceStatment = prepare(connection, UPDATE_FOGBOW_RESOURCE);
			updateFogbowResourceStatment.setString(1, fogbowResource.getComputeOrderId());
			updateFogbowResourceStatment.setString(2, fogbowResource.getInstanceId());
			updateFogbowResourceStatment.setString(3, fogbowResource.getId());
			boolean result = updateFogbowResourceStatment.executeUpdate() > 0;
			connection.commit();
			return result;
		} catch (SQLException e) {
			dealWithSQLException(connection, "Couldn't update fogbow resource " + fogbowResource.getId(), e);
			return false;
		} finally {
			close(updateFogbowResourceStatment, connection);
		}
	}

	protected PreparedStatement prepare(Connection connection, String statement) throws SQLException {
		return connection.prepareStatement(statement);
	}

	public List<FogbowResource> getAllFogbowResources() {
		List<FogbowResource> fogbowResources = new ArrayList<FogbowResource>();
		Statement getRequestIdStatement = null;
		Connection connection = null;
		try {
			connection = getConnection();
			getRequestIdStatement = connection.createStatement();
			getRequestIdStatement.execute(SELECT_REQUEST_ID);
			ResultSet result = getRequestIdStatement.getResultSet();

			while (result.next()) {
				FogbowResource fogbowresource = createFogbowResource(result);
				fogbowResources.add(fogbowresource);
			}

			return fogbowResources;

		} catch (Exception e) {
			LOGGER.error("Couldn't recover request Ids from DB", e);
			return null;
		} finally {
			close(getRequestIdStatement, connection);
		}
	}

	public boolean deleteFogbowResourceById(FogbowResource fogbowResource) {
		LOGGER.debug("Deleting resource id: " + fogbowResource.getId());
		PreparedStatement deleteResourceId = null;
		Connection connection = null;
		try {
			connection = getConnection();
			connection.setAutoCommit(false);

			deleteResourceId = prepare(connection, DELETE_BY_RESOURCE_ID_SQL);
			deleteResourceId.setString(1, fogbowResource.getId());
			boolean result = deleteResourceId.execute();
			connection.commit();
			return result;
		} catch (SQLException e) {
			dealWithSQLException(connection,"Couldn't delete the resource " + fogbowResource.getId(), e);
			return false;
		} finally {
			close(deleteResourceId, connection);
		}
	}

	public boolean deleteAll() {
		LOGGER.debug("Deleting all resources");
		PreparedStatement deleteOldContent = null;
		Connection connection = null;
		try {
			connection = getConnection();
			connection.setAutoCommit(false);
			deleteOldContent = connection.prepareStatement(DELETE_ALL_CONTENT_SQL);
			deleteOldContent.executeUpdate();
			connection.commit();
			return true;
		} catch (SQLException e) {
			dealWithSQLException(connection, "Couldn't delete all resource ids", e);
			return false;
		} finally {
			close(deleteOldContent, connection);
		}
	}

	private void setResourcers(PreparedStatement preparedStatement, FogbowResource fogbowResource) throws SQLException {

		String specification = null;

		if (fogbowResource.getRequestedSpec() != null) {
			JSONObject json = fogbowResource.getRequestedSpec().toJSON();
			specification = json.toString();
		}

		preparedStatement.setString(1, fogbowResource.getId());
		preparedStatement.setString(2, fogbowResource.getComputeOrderId());
		preparedStatement.setString(3, fogbowResource.getInstanceId());

		if (specification == null) {
			preparedStatement.setNull(4, Types.VARCHAR);
		} else {
			preparedStatement.setString(4, specification);
		}
	}

	private void dealWithSQLException(Connection connection, String errorMessage, Exception e) {
		LOGGER.error(errorMessage, e);
		try {
			if (connection != null) {
				connection.rollback();
			}
		} catch (SQLException e1) {
			LOGGER.error("Couldn't rollback transaction.", e1);
		}
	}

	private FogbowResource createFogbowResource(ResultSet result) throws SQLException, JSONException {
		String id = result.getString(RESOURCE_ID);
		String orderId = result.getString(ORDER_ID);
		String instanceId = result.getString(INSTANCE_ID);
		String specification = result.getString(SPEC);

		JSONObject jsonSpec = new JSONObject(specification);
		Specification spec = Specification.fromJSON(jsonSpec);

		FogbowResource fogbowResource = new FogbowResource(id, orderId, spec);
		fogbowResource.setInstanceId(instanceId);
		return fogbowResource;
	}

	private boolean hasBatchExecutionError(int[] executeBatch) {
		for (int i : executeBatch) {
			if (i == PreparedStatement.EXECUTE_FAILED) {
				return true;
			}
		}
		return false;
	}
}

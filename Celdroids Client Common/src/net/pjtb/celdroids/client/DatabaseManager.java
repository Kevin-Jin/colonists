package net.pjtb.celdroids.client;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Provides a central location to store database connections used for pooling,
 * and common operations when accessing the database (such as the finalizing of
 * PreparedStatements, ResultSets). Depending on the value of the "threadLocal"
 * argument passed to setProps, this class may either use a ThreadLocal model
 * or a CachedConnectionPool model.
 */
public class DatabaseManager {
	private final List<Connection> allConnections;
	private final ConnectionPool connections;

	public DatabaseManager(String url, String user, String password, boolean threadLocal) {
		allConnections = new LinkedList<Connection>();
		connections = threadLocal ? new ThreadLocalConnections(url, user, password) : new CachedConnectionPool(url, user, password);
	}

	public void initialize(String driver) throws SQLException {
		try {
			Class.forName(driver); //load the jdbc driver
		} catch (ClassNotFoundException e) {
			throw new SQLException("Unable to find JDBC library.");
		}
	}

	public Connection getConnection() throws SQLException {
		return connections.getConnection();
	}

	public void cleanup(ResultSet rs, PreparedStatement ps, Connection con) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException ex) {
				
			}
		}
		if (ps != null) {
			try {
				ps.close();
			} catch (SQLException ex) {
				
			}
		}
		if (con != null)
			connections.returnConnection(con);
	}

	public Map<Connection, SQLException> closeAll() {
		Map<Connection, SQLException> exceptions = new HashMap<Connection, SQLException>();
		synchronized (allConnections) {
			for (Iterator<Connection> iter = allConnections.iterator(); iter.hasNext();) {
				Connection con = iter.next();
				try {
					con.close();
					iter.remove();
				} catch (SQLException e) {
					exceptions.put(con, e);
				}
			}
		}
		return exceptions;
	}

	private interface ConnectionPool {
		public Connection getConnection() throws SQLException;
		public void returnConnection(Connection con);
	}

	private class ThreadLocalConnections extends ThreadLocal<Connection> implements ConnectionPool {
		private final ThreadLocal<SQLException> exceptions;
		private final String url, user, password;

		/* package-private */ ThreadLocalConnections(String url, String user, String password) {
			exceptions = new ThreadLocal<SQLException>();
			this.url = url;
			this.user = user;
			this.password = password;
		}

		@Override
		protected Connection initialValue() {
			try {
				Connection con = DriverManager.getConnection(url, user, password);
				synchronized (allConnections) {
					allConnections.add(con);
				}
				return con;
			} catch (SQLException e) {
				exceptions.set(/*new SQLException("Could not connect to database.", */e/*)*/);
				return null;
			}
		}

		private Connection tryGetConnection() throws SQLException {
			Connection con = get();
			if (con == null) {
				remove();
				SQLException ex = exceptions.get();
				exceptions.remove();
				throw ex;
			}
			return con;
		}

		@Override
		public Connection getConnection() throws SQLException {
			Connection con = tryGetConnection();
			/*if (!con.isValid(0)) {
				try {
					con.close();
					synchronized (allConnections) {
						allConnections.remove(con);
					}
				} catch (SQLException e) {
					throw new SQLException("Could not remove invalid connection to database.", e);
				}
				remove();
				con = tryGetConnection();
			}*/
			return con;
		}

		@Override
		public void returnConnection(Connection con) {
			
		}
	}

	private class CachedConnectionPool implements ConnectionPool {
		private final Queue<Connection> available;
		private final String url, user, password;

		/* package-private */ CachedConnectionPool(String url, String user, String password) {
			available = new ConcurrentLinkedQueue<Connection>();
			this.url = url;
			this.user = user;
			this.password = password;
		}

		@Override
		public Connection getConnection() throws SQLException {
			Connection next = available.poll();
			/*while (next != null && !next.isValid(0)) {
				try {
					next.close();
					synchronized (allConnections) {
						allConnections.remove(next);
					}
				} catch (SQLException e) {
					throw new SQLException("Could not remove invalid connection to database.", e);
				}
				next = available.poll();
			}*/
			if (next == null) {
				next = DriverManager.getConnection(url, user, password);
				synchronized (allConnections) {
					allConnections.add(next);
				}
			}
			return next;
		}

		@Override
		public void returnConnection(Connection con) {
			available.offer(con);
		}
	}
}

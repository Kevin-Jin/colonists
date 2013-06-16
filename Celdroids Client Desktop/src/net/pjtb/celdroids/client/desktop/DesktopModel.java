package net.pjtb.celdroids.client.desktop;

import java.io.File;
import java.sql.SQLException;

import net.pjtb.celdroids.client.DatabaseManager;
import net.pjtb.celdroids.client.Model;

public class DesktopModel extends Model {
	private final File dbPath;

	public DesktopModel(File database) {
		dbPath = database;
	}

	@Override
	public void startLoadingResources(float minSplashTime) {
		try {
			db = new DatabaseManager("jdbc:sqlite:" + dbPath.getAbsolutePath() + "/saves.sqlite", null, null, false);
			db.initialize("org.sqlite.JDBC");
			db.cleanup(null, null, db.getConnection()); // test for connection errors
		} catch (SQLException e) {
			e.printStackTrace();
		}
		super.startLoadingResources(minSplashTime);
	}
}

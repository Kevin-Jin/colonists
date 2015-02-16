package in.kevinj.colonists.client.desktop;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import in.kevinj.colonists.Constants;
import in.kevinj.colonists.client.Game;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class DesktopGame {
	public static void main(String[] args) {
		Set<String> remainingArgs = new HashSet<String>(Arrays.asList(args));
		boolean fullscreen = remainingArgs.remove("fullscreen");
		boolean unlimitedFps = remainingArgs.remove("novsync");
		boolean useFrame = !remainingArgs.remove("noframe");
		File dbPath = new File(System.getProperty("in.kevinj.colonists.client.desktop.state.dir", "state"));
		if (!dbPath.exists() && !dbPath.mkdirs())
			throw new RuntimeException("Could not create database directory " + dbPath.getAbsolutePath());

		DesktopModel model = new DesktopModel(dbPath);
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "Colonists";
		cfg.width = Constants.WIDTH / 2;
		cfg.height = Constants.HEIGHT / 2;
		cfg.useGL20 = false;
		cfg.foregroundFPS = cfg.backgroundFPS = 0;
		cfg.vSyncEnabled = !unlimitedFps;
		cfg.resizable = true;
		cfg.fullscreen = fullscreen;
		cfg.stencil = 8;
		if (!useFrame)
			System.setProperty("org.lwjgl.opengl.Window.undecorated", "true");
		System.setProperty("org.lwjgl.opengl.Display.allowSoftwareOpenGL", "true");
		new LwjglApplication(new Game(model), cfg);
	}
}

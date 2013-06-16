package net.pjtb.celdroids.client.desktop;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.pjtb.celdroids.Constants;
import net.pjtb.celdroids.client.Game;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class DesktopGame {
	public static void main(String[] args) {
		Set<String> remainingArgs = new HashSet<String>(Arrays.asList(args));
		boolean fullscreen = remainingArgs.remove("fullscreen");
		boolean unlimitedFps = remainingArgs.remove("novsync");
		boolean useFrame = remainingArgs.remove("frame");
		File dbPath = new File(System.getProperty("net.pjtb.celdroids.client.desktop.state.dir", "state"));
		if (!dbPath.exists() && !dbPath.mkdirs())
			throw new RuntimeException("Could not create database directory " + dbPath.getAbsolutePath());

		DesktopModel model = new DesktopModel(dbPath);
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "Celdroids";
		cfg.width = Constants.WIDTH;
		cfg.height = Constants.HEIGHT;
		cfg.useGL20 = false;
		cfg.useCPUSynch = false;
		cfg.vSyncEnabled = !unlimitedFps;
		cfg.resizable = false;
		cfg.fullscreen = fullscreen;
		if (!useFrame)
			System.setProperty("org.lwjgl.opengl.Window.undecorated", "true");
		System.setProperty("org.lwjgl.opengl.Display.allowSoftwareOpenGL", "true");
		new LwjglApplication(new Game(model), cfg);
	}
}

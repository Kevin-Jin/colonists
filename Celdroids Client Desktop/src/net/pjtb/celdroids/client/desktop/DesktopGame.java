package net.pjtb.celdroids.client.desktop;

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
		boolean fullscreen = remainingArgs.contains("fullscreen");
		if (fullscreen)
			remainingArgs.remove("fullscreen");
		boolean unlimitedFps = remainingArgs.contains("novsync");
		if (unlimitedFps)
			remainingArgs.remove("novsync");

		DesktopModel model = new DesktopModel();
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "Celdroids";
		cfg.width = Constants.WIDTH;
		cfg.height = Constants.HEIGHT;
		cfg.useGL20 = false;
		cfg.useCPUSynch = false;
		cfg.vSyncEnabled = !unlimitedFps;
		cfg.resizable = false;
		cfg.fullscreen = fullscreen;
		System.setProperty("org.lwjgl.opengl.Window.undecorated", "true");
		System.setProperty("org.lwjgl.opengl.Display.allowSoftwareOpenGL", "true");
		new LwjglApplication(new Game(model), cfg);
	}
}

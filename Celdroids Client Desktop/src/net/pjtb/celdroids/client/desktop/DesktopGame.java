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
		boolean fullscreen = remainingArgs.remove("fullscreen");
		boolean unlimitedFps = remainingArgs.remove("novsync");
		boolean useFrame = remainingArgs.remove("frame");

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
		if (!useFrame)
			System.setProperty("org.lwjgl.opengl.Window.undecorated", "true");
		System.setProperty("org.lwjgl.opengl.Display.allowSoftwareOpenGL", "true");
		new LwjglApplication(new Game(model), cfg);
	}
}

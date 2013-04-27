package net.pjtb.celdroids.client.desktop;

import net.pjtb.celdroids.client.Game;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;

public class DesktopGame {
	public static void main(String[] args) {
		DesktopModel model = new DesktopModel();
		model.createScenes();
		new LwjglApplication(new Game(model), "Celdroids", 1280, 720, false);
		model.loadResources();
	}
}

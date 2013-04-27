package net.pjtb.celdroids.client.scenes;

import java.util.EnumMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;

public class MainMenuScene implements Scene {
	public enum MainMenuSubSceneType { ROOT, DIRECT_IP_CONNECT, P2P_CONNECT }

	protected final Map<MainMenuSubSceneType, Scene> subScenes;
	private Scene subScene;

	public MainMenuScene() {
		subScenes = new EnumMap<MainMenuSubSceneType, Scene>(MainMenuSubSceneType.class);
		subScenes.put(MainMenuSubSceneType.DIRECT_IP_CONNECT, new DirectConnectSelectionScene());
	}

	@Override
	public void swappedIn() {
		Gdx.gl10.glClearColor(0.5f, 0.5f, 0.5f, 1);
	}

	@Override
	public void pause() {
		if (subScene != null)
			subScene.pause();
	}

	@Override
	public void resume() {
		if (subScene != null)
			subScene.resume();
	}

	@Override
	public void update(float tDelta) {
		//update our own scene stuff first, then subscene
		if (subScene != null)
			subScene.update(tDelta);
	}

	@Override
	public void draw() {
		Mesh m = new Mesh(true, 3, 3, new VertexAttribute(Usage.Position, 3, "a_position"));
		m.setVertices(new float[] { 0, 10, 0, 1280, 10, 0, 640, 720, 0 });
		m.setIndices(new short[] { 0, 1, 2 });
		m.render(GL10.GL_TRIANGLES, 0, 3);

		if (Gdx.input.isTouched())
			Gdx.gl10.glColor4f(1, 0, 1, 1);
		else
			Gdx.gl10.glColor4f(1, 1, 0, 1);
		m = new Mesh(true, 3, 3, new VertexAttribute(Usage.Position, 3, "b_position"));
		m.setVertices(new float[] { 1, 1, 0, 1278, 1, 0, 1278, 718, 0 });
		m.setIndices(new short[] { 0, 1, 2 });
		m.render(GL10.GL_LINE_STRIP, 0, 3);

		m = new Mesh(true, 3, 3, new VertexAttribute(Usage.Position, 3, "c_position"));
		m.setVertices(new float[] { 1278, 718, 0, 1, 718, 0, 1, 1, 0 });
		m.setIndices(new short[] { 0, 1, 2 });
		m.render(GL10.GL_LINE_STRIP, 0, 3);
		//draw our own scene stuff first, then overlay with subscene
		if (subScene != null)
			subScene.draw();
	}

	@Override
	public void swappedOut() {
		
	}
}

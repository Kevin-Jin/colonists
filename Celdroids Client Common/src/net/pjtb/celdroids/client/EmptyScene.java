package net.pjtb.celdroids.client;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class EmptyScene implements Scene {
	public static final EmptyScene instance = new EmptyScene();

	private EmptyScene() {
		
	}

	@Override
	public void swappedIn(boolean transition) {
		
	}

	@Override
	public void pause() {
		
	}

	@Override
	public void resume() {
		
	}

	@Override
	public void update(float tDelta) {
		
	}

	@Override
	public void draw(SpriteBatch batch) {
		
	}

	@Override
	public void swappedOut(boolean transition) {
		
	}

	@Override
	public Scene getSubscene() {
		return null;
	}

	@Override
	public void setSubscene(Scene scene) {
		
	}
}

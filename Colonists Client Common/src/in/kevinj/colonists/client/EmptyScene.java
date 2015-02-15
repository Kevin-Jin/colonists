package in.kevinj.colonists.client;

import in.kevinj.colonists.client.PriorityQueueAssetManager.LoadEntry;

import java.util.Collection;
import java.util.Collections;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class EmptyScene implements Scene {
	public static final EmptyScene instance = new EmptyScene();

	private EmptyScene() {
		
	}

	@Override
	public Collection<LoadEntry> getAssetDependencies() {
		return Collections.emptyList();
	}

	@Override
	public Collection<String> getSpriteSheetDependencies() {
		return Collections.emptyList();
	}

	@Override
	public void swappedIn(boolean transition) {
		
	}

	@Override
	public void resize(int width, int height) {
		
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

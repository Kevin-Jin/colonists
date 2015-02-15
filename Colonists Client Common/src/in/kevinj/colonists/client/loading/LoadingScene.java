package in.kevinj.colonists.client.loading;

import java.util.Collection;
import java.util.Collections;

import in.kevinj.colonists.Constants;
import in.kevinj.colonists.client.Model;
import in.kevinj.colonists.client.Scene;
import in.kevinj.colonists.client.PriorityQueueAssetManager.LoadEntry;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class LoadingScene implements Scene {
	private final Model model;

	public LoadingScene(Model model) {
		this.model = model;
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
		if (model.controller.wasBackPressed && !Gdx.input.isKeyPressed(Keys.ESCAPE) && !Gdx.input.isKeyPressed(Keys.BACK)) {
			Gdx.app.exit();
		} else if (model.controller.wasMenuPressed && !Gdx.input.isKeyPressed(Keys.ENTER) && !Gdx.input.isKeyPressed(Keys.MENU)) {
			
		}
	}

	@Override
	public void draw(SpriteBatch batch) {
		Texture image = model.assets.get("images/backgrounds/splash.png", Texture.class);
		batch.draw(image, 0, Constants.HEIGHT - image.getHeight());
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

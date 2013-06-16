package net.pjtb.celdroids.client.loading;

import net.pjtb.celdroids.Constants;
import net.pjtb.celdroids.client.Model;
import net.pjtb.celdroids.client.Scene;

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

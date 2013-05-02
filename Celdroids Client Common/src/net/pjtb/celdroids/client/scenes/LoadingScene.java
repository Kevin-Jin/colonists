package net.pjtb.celdroids.client.scenes;

import com.badlogic.gdx.graphics.Texture;

import net.pjtb.celdroids.Constants;
import net.pjtb.celdroids.client.Model;

public class LoadingScene implements Scene {
	private final Model model;

	public LoadingScene(Model model) {
		this.model = model;
	}

	@Override
	public void swappedIn() {
		
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
	public void draw() {
		Texture image = model.assets.get("images/backgrounds/splash.png", Texture.class);

		model.batch.begin();
		model.batch.draw(image, 0, Constants.HEIGHT - image.getHeight());
		model.batch.end();
	}

	@Override
	public void swappedOut() {
		
	}
}

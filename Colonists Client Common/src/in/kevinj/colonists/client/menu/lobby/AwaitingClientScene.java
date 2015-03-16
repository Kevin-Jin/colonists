package in.kevinj.colonists.client.menu.lobby;

import in.kevinj.colonists.Constants;
import in.kevinj.colonists.NetworkPlayer;
import in.kevinj.colonists.client.Button;
import in.kevinj.colonists.client.Model;
import in.kevinj.colonists.client.PopupScene;
import in.kevinj.colonists.client.Scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.NumberUtils;

public class AwaitingClientScene extends PopupScene {
	private final AwaitingClientModel model;

	private final Scene parentScene;

	private final float successTint, errorTint;

	private final Button close;

	public AwaitingClientScene(Model m, Scene mainMenuScene) {
		super(m);
		this.model = new AwaitingClientModel(m);

		this.parentScene = mainMenuScene;

		successTint = NumberUtils.intToFloatColor(0xFF << 24 | 0x00 << 16 | 0xFF << 8 | 0xFF);
		errorTint = NumberUtils.intToFloatColor(0xFF << 24 | 0x00 << 16 | 0x00 << 8 | 0xFF);

		this.close = new Button(m, "Okay", new Runnable() {
			@Override
			public void run() {
				swappedOut(true);
				parentScene.setSubscene(null);
			}
		}, (Constants.WIDTH - 256) / 2, getButtonY(), 256, 128);
	}

	@Override
	public void update(float tDelta) {
		NetworkPlayer op = model.update(tDelta);
		if (op != null) {
			swappedOut(true);
			model.parent.sceneToShow.setSubscene(null);
			model.parent.worldModel.initRemote(op, false);
			model.parent.swapScene(model.parent.scenes.get(Model.SceneType.WORLD));
			return;
		}

		close.hidden = !model.error;
		close.update(tDelta);

		if (model.parent.controller.wasBackPressed && !Gdx.input.isKeyPressed(Keys.ESCAPE) && !Gdx.input.isKeyPressed(Keys.BACK)) {
			swappedOut(true);
			parentScene.setSubscene(null);
		} else if (model.parent.controller.wasMenuPressed && !Gdx.input.isKeyPressed(Keys.ENTER) && !Gdx.input.isKeyPressed(Keys.MENU)) {
			
		}
	}

	@Override
	public void draw(SpriteBatch batch) {
		super.draw(batch);
		this.drawText(batch, model.message, model.error ? errorTint : successTint);

		if (model.error)
			close.draw(batch);
	}

	@Override
	public void swappedIn(boolean transition) {
		if (transition)
			model.swappedIn();
	}

	@Override
	public void pause() {
		
	}

	@Override
	public void resume() {
		
	}

	@Override
	public void swappedOut(boolean transition) {
		if (transition)
			model.swappedOut();
	}

	@Override
	public Scene getSubscene() {
		return null;
	}

	@Override
	public void setSubscene(Scene scene) {
		
	}
}

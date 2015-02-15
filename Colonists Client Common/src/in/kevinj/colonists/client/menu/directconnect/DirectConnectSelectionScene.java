package in.kevinj.colonists.client.menu.directconnect;

import in.kevinj.colonists.client.Model;
import in.kevinj.colonists.client.NetworkPlayerBattleOpponent;
import in.kevinj.colonists.client.PopupScene;
import in.kevinj.colonists.client.Scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.NumberUtils;

public class DirectConnectSelectionScene extends PopupScene {
	private final DirectConnectModel model;

	private final Scene parentScene;

	private final float promptTint, entryTint, successTint, errorTint, inactiveTint;

	public DirectConnectSelectionScene(Model m, Scene mainMenuScene) {
		super(m);
		this.model = new DirectConnectModel(m);

		this.parentScene = mainMenuScene;

		promptTint = NumberUtils.intToFloatColor(0xFF << 24 | 0xFF << 16 | 0x00 << 8 | 0x00);
		entryTint = NumberUtils.intToFloatColor(0xFF << 24 | 0x00 << 16 | 0x00 << 8 | 0xFF);
		successTint = NumberUtils.intToFloatColor(0xFF << 24 | 0x00 << 16 | 0xFF << 8 | 0xFF);
		errorTint = NumberUtils.intToFloatColor(0xFF << 24 | 0x00 << 16 | 0x00 << 8 | 0xFF);
		inactiveTint = NumberUtils.intToFloatColor(0xFF << 24 | 0xBF << 16 | 0xBF << 8 | 0xBF);
	}

	@Override
	public void swappedIn(boolean transition) {
		Gdx.input.setOnscreenKeyboardVisible(true);
		Gdx.input.setInputProcessor(model);
		//otherwise, key repeat delay seems to be too low on LWJGL backend
		Gdx.graphics.setContinuousRendering(true);
	}

	@Override
	public void pause() {
		
	}

	@Override
	public void resume() {
		
	}

	@Override
	public void update(float tDelta) {
		NetworkPlayerBattleOpponent op = model.update(tDelta);
		if (op != null) {
			swappedOut(true);
			model.parent.sceneToShow.setSubscene(null);
			model.parent.battleModel.initRemote(op, true);
			model.parent.swapScene(model.parent.scenes.get(Model.SceneType.BATTLE));
			return;
		}

		if (model.parent.controller.wasBackPressed && !Gdx.input.isKeyPressed(Keys.ESCAPE) && !Gdx.input.isKeyPressed(Keys.BACK)) {
			swappedOut(true);
			parentScene.setSubscene(null);
		}
	}

	@Override
	public void draw(SpriteBatch batch) {
		super.draw(batch);
		BitmapFont fnt = model.parent.assets.get("fonts/buttons.fnt", BitmapFont.class);
		int currentHeight = this.drawText(batch, "Enter the address:", promptTint, fnt);
		currentHeight = this.drawText(batch, model.entered + "_", model.inactive ? inactiveTint : entryTint, currentHeight, fnt);
		currentHeight = this.drawText(batch, model.message, model.error ? errorTint : successTint, currentHeight, fnt);
	}

	@Override
	public void swappedOut(boolean transition) {
		if (transition)
			model.swappedOut();
		Gdx.graphics.setContinuousRendering(false);
		Gdx.input.setInputProcessor(null);
		Gdx.input.setOnscreenKeyboardVisible(false);
	}

	@Override
	public Scene getSubscene() {
		return null;
	}

	@Override
	public void setSubscene(Scene scene) {
		
	}
}

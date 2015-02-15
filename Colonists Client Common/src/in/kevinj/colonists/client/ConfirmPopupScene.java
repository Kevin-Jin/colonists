package in.kevinj.colonists.client;

import in.kevinj.colonists.Constants;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.NumberUtils;

public class ConfirmPopupScene extends PopupScene {
	private final float fontTint;
	private final String text;
	private final Model.SceneType nextScene;
	private final Button yes, no;

	public ConfirmPopupScene(Model m, String text, Model.SceneType nextScene) {
		super(m);

		fontTint = NumberUtils.intToFloatColor(0xFF << 24 | 0xFF << 16 | 0x00 << 8 | 0x00);
		this.text = text;
		this.nextScene = nextScene;
		this.yes = new Button(model, "Yes", new Runnable() {
			@Override
			public void run() {
				proceed();
			}
		}, (Constants.WIDTH - 522) / 2, getButtonY(), 256, 128);
		this.no = new Button(model, "No", new Runnable() {
			@Override
			public void run() {
				back();
			}
		}, (Constants.WIDTH - 522) / 2 + 266, getButtonY(), 256, 128);
	}

	private void proceed() {
		swappedOut(true);
		model.sceneToShow.setSubscene(null);
		model.swapScene(model.scenes.get(nextScene));
	}

	private void back() {
		swappedOut(true);
		model.sceneToShow.setSubscene(null);
	}

	@Override
	public void update(float tDelta) {
		yes.update(tDelta);
		no.update(tDelta);

		if (model.controller.wasBackPressed && !Gdx.input.isKeyPressed(Keys.ESCAPE) && !Gdx.input.isKeyPressed(Keys.BACK)) {
			back();
		} else if (model.controller.wasMenuPressed && !Gdx.input.isKeyPressed(Keys.ENTER) && !Gdx.input.isKeyPressed(Keys.MENU)) {
			
		}
	}

	@Override
	public void draw(SpriteBatch batch) {
		super.draw(batch);
		this.drawText(batch, text, fontTint);

		yes.draw(batch);
		no.draw(batch);
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

package in.kevinj.colonists.client;

import java.util.Collection;
import java.util.Collections;

import in.kevinj.colonists.Constants;
import in.kevinj.colonists.client.PriorityQueueAssetManager.LoadEntry;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public abstract class PopupScene implements Scene {
	protected final Model model;
	protected final int width, height, paddingTop, paddingBottom;

	private final ShapeRenderer shapeRenderer;

	public PopupScene(Model m, int width, int height) {
		this. model = m;
		this.width = width;
		this.height = height;

		paddingTop = height - Constants.HEIGHT / 2;
		paddingBottom = Constants.HEIGHT / 2 - 400;
		shapeRenderer = new ShapeRenderer();
	}

	public PopupScene(Model m) {
		this(m, Constants.WIDTH - 20, Constants.WIDTH * 4 / 3 - 20);
	}

	protected int getButtonY() {
		return (Constants.HEIGHT - height) / 2 + paddingBottom;
	}

	@Override
	public Collection<LoadEntry> getAssetDependencies() {
		return Collections.emptyList();
	}

	@Override
	public Collection<String> getSpriteSheetDependencies() {
		return Collections.emptyList();
	}

	protected int drawText(SpriteBatch batch, String message, float tint, int topY, BitmapFont fnt) {
		if (message != null && !message.isEmpty()) {
			TextBounds bnds = fnt.getWrappedBounds(message, width - 100);
			fnt.setColor(tint);
			fnt.drawWrapped(batch, message, (Constants.WIDTH - bnds.width) / 2, topY, width - 100);
			topY -= bnds.height + fnt.getCapHeight();
		}
		return topY;
	}

	protected int drawText(SpriteBatch batch, String message, float tint, BitmapFont fnt) {
		return drawText(batch, message, tint, (Constants.HEIGHT - height) / 2 + height - paddingTop, fnt);
	}

	protected int drawText(SpriteBatch batch, String message, float tint) {
		return drawText(batch, message, tint, model.assets.get("fonts/buttons.fnt", BitmapFont.class));
	}

	@Override
	public void resize(int width, int height) {
		
	}

	@Override
	public void draw(SpriteBatch batch) {
		batch.end();
		model.cam.apply(Gdx.gl10);
		Gdx.gl10.glEnable(GL10.GL_BLEND);
		Gdx.gl10.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		model.cam.apply(Gdx.gl10);
		shapeRenderer.setColor(0, 0, 0, 0.5f);
		shapeRenderer.rect(0, Constants.HEIGHT, Constants.WIDTH, -Constants.HEIGHT);
		shapeRenderer.end();
		Gdx.gl10.glDisable(GL10.GL_BLEND);
		batch.begin();
		model.cam.apply(Gdx.gl10);

		Sprite s = model.sprites.get("popup/confirmBackground");
		s.setBounds((Constants.WIDTH - width) / 2, (Constants.HEIGHT - height) / 2, width, height);
		s.draw(batch);
	}
}

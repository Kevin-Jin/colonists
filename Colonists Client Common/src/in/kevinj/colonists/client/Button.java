package in.kevinj.colonists.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.NumberUtils;

public class Button implements ViewComponent {
	private final Model model;
	private final ScaleDisplay transformer;

	public String text;
	public Runnable callback;
	private final int x, y, width, height;
	private final String inactiveSprite, activeSprite;
	private final float tint, fontTint;
	private boolean down, pressed, target;

	public boolean hidden;

	public Button(Model model, String text, Runnable task, int x, int y, int width, int height, String inactiveSprite, String activeSprite, int tintR, int tintG, int tintB, int tintA, int fontR, int fontG, int fontB, int fontA, ScaleDisplay transformer) {
		this.model = model;
		this.transformer = transformer;

		this.text = text;
		this.callback = task;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.inactiveSprite = inactiveSprite;
		this.activeSprite = activeSprite;

		tint = NumberUtils.intToFloatColor(tintA << 24 | tintB << 16 | tintG << 8 | tintR);
		fontTint = NumberUtils.intToFloatColor(fontA << 24 | fontB << 16 | fontG << 8 | fontR);
	}

	public Button(Model model, String text, Runnable task, int x, int y, int width, int height, String inactiveSprite, String activeSprite, int tintR, int tintG, int tintB, int tintA, int fontR, int fontG, int fontB, int fontA) {
		this(model, text, task, x, y, width, height, "ui/button/regular", "ui/button/pressed", tintR, tintG, tintB, tintA, fontR, fontG, fontB, fontA, null);
	}

	public Button(Model model, String text, Runnable task, int x, int y, int width, int height, ScaleDisplay transformer) {
		this(model, text, task, x, y, width, height, "ui/button/regular", "ui/button/pressed", 255, 255, 255, 255, 191, 191, 191, 255, transformer);
	}

	public Button(Model model, String text, Runnable task, int x, int y, int width, int height) {
		this(model, text, task, x, y, width, height, null);
	}

	@Override
	public void update(float tDelta) {
		if (hidden)
			return;

		Vector3 cursor = model.controller.getCursor(transformer);
		boolean wasDown = down;
		down = Gdx.input.isButtonPressed(Buttons.LEFT);
		boolean inBounds = (cursor.x > x && cursor.x < x + width && cursor.y > y && cursor.y < y + height);
		if (!down) {
			pressed = false;
			if (wasDown && target && inBounds)
				callback.run();
		} else {
			if (!wasDown)
				target = pressed = inBounds;
			else if (target)
				pressed = inBounds;
		}
	}

	@Override
	public void draw(SpriteBatch batch) {
		Sprite s;
		if (pressed)
			s = model.sprites.get(activeSprite);
		else
			s = model.sprites.get(inactiveSprite);
		s.setBounds(x, y, width, height);
		s.setColor(tint);
		s.draw(batch);
		if (text != null) {
			BitmapFont fnt = model.assets.get("fonts/buttons.fnt", BitmapFont.class);
			TextBounds bnds = fnt.getBounds(text);
			fnt.setColor(fontTint);
			fnt.draw(batch, text, x + (width - bnds.width) / 2, y + (height + bnds.height) / 2);
		}
	}
}

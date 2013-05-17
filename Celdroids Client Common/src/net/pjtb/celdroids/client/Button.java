package net.pjtb.celdroids.client;

import net.pjtb.celdroids.Constants;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Button implements ViewComponent {
	private final Model model;

	private final String text;
	private final Runnable callback;
	private final int x, y, width, height;
	private final String inactiveSprite, activeSprite;
	private boolean down, pressed, target;

	public boolean hidden;

	public Button(Model model, String text, Runnable task, int x, int y, int width, int height, String inactiveSprite, String activeSprite) {
		this.model = model;

		this.text = text;
		this.callback = task;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.inactiveSprite = inactiveSprite;
		this.activeSprite = activeSprite;
	}

	public Button(Model model, String text, Runnable task, int x, int y, int width, int height) {
		this(model, text, task, x, y, width, height, "buttonRegular", "buttonPressed");
	}

	@Override
	public void update(float tDelta) {
		if (hidden)
			return;

		int cursorX = Gdx.input.getX();
		int cursorY = Constants.HEIGHT - Gdx.input.getY();
		boolean wasDown = down;
		down = Gdx.input.isButtonPressed(Buttons.LEFT);
		boolean inBounds = (cursorX > x && cursorX < x + width && cursorY > y && cursorY < y + height);
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
	public void draw() {
		model.batch.begin();
		Sprite s;
		if (pressed)
			s = model.sprites.get(activeSprite);
		else
			s = model.sprites.get(inactiveSprite);
		s.setBounds(x, y, width, height);
		BitmapFont fnt = model.assets.get("fonts/buttons.fnt", BitmapFont.class);
		s.draw(model.batch);
		TextBounds bnds = fnt.getBounds(text);
		fnt.draw(model.batch, text, x + (width - bnds.width) / 2, y + (height + bnds.height) / 2);
		model.batch.end();
	}
}

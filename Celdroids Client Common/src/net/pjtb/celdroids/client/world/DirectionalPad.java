package net.pjtb.celdroids.client.world;

import net.pjtb.celdroids.client.ControllerHelper;
import net.pjtb.celdroids.client.Model;
import net.pjtb.celdroids.client.ViewComponent;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class DirectionalPad implements ViewComponent {
	public enum State { UP, RIGHT, DOWN, LEFT, NONE }

	private final Model model;

	private boolean down, target;

	public boolean hidden;
	public State state;

	public DirectionalPad(Model model) {
		this.model = model;

		state = State.NONE;
	}

	@Override
	public void update(float tDelta) {
		if (hidden)
			return;

		boolean wasDown = down;
		down = Gdx.input.isButtonPressed(Buttons.LEFT);
		if (!down) {
			state = State.NONE;
			target = false;
		} else {
			int dpadX = ControllerHelper.getCursorX() - 960;
			int dpadY = ControllerHelper.getCursorY() - 200;
			boolean inBounds = (dpadX >= 0 && dpadY >= 0 && dpadX < 320 && dpadY < 320 && (dpadX < 121 || dpadX > 199 || dpadY < 121 || dpadY > 199));
			if (!wasDown)
				target = inBounds;

			if (inBounds && target) {
				if (dpadX > dpadY) {
					if (320 - dpadX > dpadY)
						state = State.DOWN;
					else if (320 - dpadX < dpadY)
						state = State.RIGHT;
				} else if (dpadX < dpadY) {
					if (320 - dpadX > dpadY)
						state = State.LEFT;
					else if (320 - dpadX < dpadY)
						state = State.UP;
				}
			} else {
				state = State.NONE;
			}
		}
	}

	@Override
	public void draw(SpriteBatch batch) {
		Sprite s;
		switch (state) {
			case UP:
				s = model.sprites.get("arrowKeyHeld");
				s.setBounds(960, 399, 320, 121);
				s.draw(batch);
				break;
			case RIGHT:
				s = model.sprites.get("arrowKeyHeld");
				s.setBounds(1159, 200, 121, 320);
				s.rotate90(true);
				s.draw(batch);
				s.rotate90(false);
				break;
			case DOWN:
				s = model.sprites.get("arrowKeyHeld");
				s.setBounds(960, 200, 320, 121);
				s.rotate90(true);
				s.rotate90(true);
				s.draw(batch);
				s.rotate90(true);
				s.rotate90(true);
				break;
			case LEFT:
				s = model.sprites.get("arrowKeyHeld");
				s.setBounds(960, 200, 121, 320);
				s.rotate90(true);
				s.rotate90(true);
				s.rotate90(true);
				s.draw(batch);
				s.rotate90(true);
				break;
			case NONE:
				break;
		}
	}
}

package in.kevinj.colonists.client.world;

import in.kevinj.colonists.Constants;
import in.kevinj.colonists.client.Model;
import in.kevinj.colonists.client.ViewComponent;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class DirectionalPad implements ViewComponent {
	public enum State {
		UP, RIGHT, DOWN, LEFT, NONE
	}

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
			int dpadX = model.controller.getCursorX(null) - WorldModel.MAP_VIEW_WIDTH;
			int dpadY = model.controller.getCursorY(null) - 200;
			boolean inBounds = (dpadX >= 0 && dpadY >= 0 && dpadX < WorldModel.CONTROL_VIEW_WIDTH && dpadY < WorldModel.CONTROL_VIEW_WIDTH);
			if (!wasDown)
				target = inBounds;

			if (target && inBounds && (dpadX < 121 || dpadX > 199 || dpadY < 121 || dpadY > 199)) {
				if (dpadX > dpadY) {
					if (WorldModel.CONTROL_VIEW_WIDTH - dpadX > dpadY)
						state = State.DOWN;
					else if (WorldModel.CONTROL_VIEW_WIDTH - dpadX < dpadY)
						state = State.RIGHT;
				} else if (dpadX < dpadY) {
					if (WorldModel.CONTROL_VIEW_WIDTH - dpadX > dpadY)
						state = State.LEFT;
					else if (WorldModel.CONTROL_VIEW_WIDTH - dpadX < dpadY)
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
				s = model.sprites.get("ui/worldScene/selectedArrow");
				s.setBounds(WorldModel.MAP_VIEW_WIDTH, 399, WorldModel.CONTROL_VIEW_WIDTH, 121);
				s.draw(batch);
				break;
			case RIGHT:
				s = model.sprites.get("ui/worldScene/selectedArrow");
				s.setBounds(Constants.WIDTH - 121, 200, 121, WorldModel.CONTROL_VIEW_WIDTH);
				s.rotate90(true);
				s.draw(batch);
				s.rotate90(false);
				break;
			case DOWN:
				s = model.sprites.get("ui/worldScene/selectedArrow");
				s.setBounds(WorldModel.MAP_VIEW_WIDTH, 200, WorldModel.CONTROL_VIEW_WIDTH, 121);
				s.rotate90(true);
				s.rotate90(true);
				s.draw(batch);
				s.rotate90(true);
				s.rotate90(true);
				break;
			case LEFT:
				s = model.sprites.get("ui/worldScene/selectedArrow");
				s.setBounds(WorldModel.MAP_VIEW_WIDTH, 200, 121, WorldModel.CONTROL_VIEW_WIDTH);
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

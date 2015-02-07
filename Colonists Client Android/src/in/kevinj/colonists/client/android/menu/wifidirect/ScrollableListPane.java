package in.kevinj.colonists.client.android.menu.wifidirect;

import java.util.ArrayList;
import java.util.List;

import in.kevinj.colonists.Constants;
import in.kevinj.colonists.client.Model;
import in.kevinj.colonists.client.ViewComponent;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.NumberUtils;

//TODO: inertia
//delay after touching an entry before it is highlighted (i.e. when the intention is scrolling)
//prevent small delta Ys from showing immediate effects - instead, add them to an accumulator that will be released when a threshold is reached
public class ScrollableListPane<T> implements ViewComponent {
	public interface SelectTask<T> {
		public void selected(T value);
	}

	private static class Entry<T> {
		public String name;
		public T value;

		public Entry(String name, T value) {
			this.name = name;
			this.value = value;
		}
	}

	private static final int
		HORIZONTAL_MARGIN = 300,
		VERTICAL_MARGIN = 5,
		SELECTION_HEIGHT = 128,
		SPACER_HEIGHT = 2,
		SCROLL_SENSITIVITY = 10
	;

	private final Model model;

	private final SelectTask<T> selectionTasks;
	private final ShapeRenderer shapeRenderer;
	private final List<Entry<T>> selections;
	private final float fontTint, selectedFontTint;

	private boolean down;
	private int initialSelectedYOffset;
	private int yOffset;
	private int selected;

	public volatile String text;
	public volatile boolean error;
	private final float textTint, textErrorTint;

	public ScrollableListPane(String intialText, Model model, SelectTask<T> tasks) {
		this.model = model;
		this.text = intialText;

		selectionTasks = tasks;
		shapeRenderer = new ShapeRenderer();
		selections = new ArrayList<Entry<T>>();
		fontTint = NumberUtils.intToFloatColor(0xFF << 24 | 0x00 << 16 | 0x00 << 8 | 0x00);
		selectedFontTint = NumberUtils.intToFloatColor(0xFF << 24 | 0xFF << 16 | 0xFF << 8 | 0xFF);

		selected = -1;

		textTint = NumberUtils.intToFloatColor(0xFF << 24 | 0x00 << 16 | 0xFF << 8 | 0xFF);
		textErrorTint = NumberUtils.intToFloatColor(0xFF << 24 | 0x00 << 16 | 0x00 << 8 | 0xFF);
	}

	public void addSelection(String name, T value) {
		selections.add(new Entry<T>(name, value));
	}

	public void clearSelections() {
		selections.clear();
	}

	@Override
	public void update(float tDelta) {
		boolean wasDown = down;
		down = Gdx.input.isButtonPressed(Buttons.LEFT);
		if (down && wasDown)
			yOffset = Math.max(Math.min(Gdx.input.getDeltaY() + yOffset, SELECTION_HEIGHT / 2), Math.min(-(SELECTION_HEIGHT + SPACER_HEIGHT) * selections.size() + Constants.HEIGHT - VERTICAL_MARGIN * 2, 0) - SELECTION_HEIGHT / 2);
		else
			yOffset = Math.max(Math.min(yOffset, 0), Math.min(-(SELECTION_HEIGHT + SPACER_HEIGHT) * selections.size() + Constants.HEIGHT - VERTICAL_MARGIN * 2, 0));

		int relativeX = Gdx.input.getX() - HORIZONTAL_MARGIN;
		int relativeY = Gdx.input.getY() - yOffset - VERTICAL_MARGIN;

		if (!down && wasDown) {
			if (selected != -1) {
				selectionTasks.selected(selections.get(selected).value);
				selected = -1;
			}
		} else if (down && !wasDown) {
			if (relativeX >= 0 && relativeX < Constants.WIDTH - HORIZONTAL_MARGIN && relativeY >= 0 && relativeY < (SELECTION_HEIGHT + SPACER_HEIGHT) * selections.size()) {
				selected = relativeY / (SELECTION_HEIGHT + SPACER_HEIGHT);
				initialSelectedYOffset = yOffset;
			} else {
				selected = -1;
			}
		} else if (down && wasDown) {
			if (Math.abs(initialSelectedYOffset - yOffset) > SCROLL_SENSITIVITY)
				selected = -1;
		}
	}

	@Override
	public void draw(SpriteBatch batch) {
		Gdx.gl10.glEnable(GL10.GL_SCISSOR_TEST);
		Gdx.gl10.glScissor(HORIZONTAL_MARGIN, VERTICAL_MARGIN, Constants.WIDTH - 2 * HORIZONTAL_MARGIN, Constants.HEIGHT - 2 * VERTICAL_MARGIN);
		try {
			batch.end();
			shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
			model.cam.apply(Gdx.gl10);
			shapeRenderer.setColor((float) 0xE4 / 0xFF, (float) 0xE4 / 0xFF, (float) 0xE4 / 0xFF, 1);
			shapeRenderer.rect(HORIZONTAL_MARGIN, Constants.HEIGHT - VERTICAL_MARGIN, Constants.WIDTH - 2 * HORIZONTAL_MARGIN, VERTICAL_MARGIN - Constants.HEIGHT);
			shapeRenderer.end();
			batch.begin();
			model.cam.apply(Gdx.gl10);

			BitmapFont fnt = model.assets.get("fonts/buttons.fnt", BitmapFont.class);
			if (text != null) {
				TextBounds bnds = fnt.getBounds(text);
				fnt.setColor(error ? textErrorTint : textTint);
				fnt.draw(batch, text, (Constants.WIDTH - bnds.width) / 2, (Constants.HEIGHT + bnds.height) / 2);
				return;
			}

			fnt.setColor(fontTint);
			for (int i = 0; i < selections.size(); i++) {
				batch.end();
				shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
				model.cam.apply(Gdx.gl10);
				if (i == selected) {
					shapeRenderer.setColor((float) 0x33 / 0xFF, (float) 0xB5 / 0xFF, (float) 0xE5 / 0xFF, 1);
					shapeRenderer.rect(HORIZONTAL_MARGIN, Constants.HEIGHT - VERTICAL_MARGIN - (SELECTION_HEIGHT + SPACER_HEIGHT) * i - yOffset, Constants.WIDTH - 2 * HORIZONTAL_MARGIN, -SELECTION_HEIGHT);
					fnt.setColor(selectedFontTint);
				} else {
					fnt.setColor(fontTint);
				}
				shapeRenderer.setColor((float) 0x9D / 0xFF, (float) 0x9D / 0xFF, (float) 0x9D / 0xFF, 1);
				shapeRenderer.rect(HORIZONTAL_MARGIN, Constants.HEIGHT - VERTICAL_MARGIN - (SELECTION_HEIGHT + SPACER_HEIGHT) * i - yOffset - SELECTION_HEIGHT, Constants.WIDTH - 2 * HORIZONTAL_MARGIN, -SPACER_HEIGHT);
				shapeRenderer.end();
				batch.begin();
				model.cam.apply(Gdx.gl10);
				TextBounds bnds = fnt.getBounds(selections.get(i).name);
				fnt.draw(batch, selections.get(i).name, (Constants.WIDTH - bnds.width) / 2, Constants.HEIGHT - VERTICAL_MARGIN - (SELECTION_HEIGHT + SPACER_HEIGHT) * i - yOffset - (SELECTION_HEIGHT - bnds.height) / 2);
			}
		} finally {
			Gdx.gl10.glDisable(GL10.GL_SCISSOR_TEST);
		}
	}
}

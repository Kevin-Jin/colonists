package in.kevinj.colonists.client.world;

import in.kevinj.colonists.client.Model;
import in.kevinj.colonists.client.ViewComponent;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.NumberUtils;

public class FanSelect implements ViewComponent {
	private static final double NORMALIZE_CENTER = 0; // [-PI, PI]

	private static double normalize(double theta) {
		return theta - 2 * Math.PI * Math.floor((theta + Math.PI - NORMALIZE_CENTER) / (2 * Math.PI));
	}

	public interface SelectTask {
		public void selected(int index);
	}

	private final Model model;

	private final String text;
	private final int centerX, centerY;
	private final double angleMin, angleMax;
	private final int triggerRadiusSq, selectionRadius, outerBoundaryRadiusSq;
	private final float fontTint, selectedFontTint, groupFontTint;

	private double angleInterval;
	private double[] selectionXs;
	private double[] selectionYs;
	private String[] selectionTexts;
	private SelectTask selectionTasks;

	private boolean down, expanded, target;
	private int selected;

	public boolean hidden;

	public FanSelect(Model model, int centerX, int centerY, double angleStart, double angleEnd, int triggerRadius, int selectionRadius, String group) {
		this.model = model;

		this.text = group;
		this.centerX = centerX;
		this.centerY = centerY;
		this.angleMin = angleStart;
		this.angleMax = angleEnd;
		this.triggerRadiusSq = triggerRadius * triggerRadius;
		this.selectionRadius = selectionRadius;
		this.outerBoundaryRadiusSq = (selectionRadius + 100) * (selectionRadius + 100);
		fontTint = NumberUtils.intToFloatColor(0x7F << 24 | 0x7F << 16 | 0x7F << 8 | 0x7F);
		selectedFontTint = NumberUtils.intToFloatColor(0xFF << 24 | 0x00 << 16 | 0x00 << 8 | 0x00);
		groupFontTint = NumberUtils.intToFloatColor(0xFF << 24 | 0x00 << 16 | 0x00 << 8 | 0xFF);
	}

	public void setSelections(SelectTask tasks, String... texts) {
		selectionXs = new double[texts.length];
		selectionYs = new double[selectionXs.length];
		selectionTexts = texts;
		selectionTasks = tasks;

		if (selectionXs.length == 1) {
			angleInterval = Double.POSITIVE_INFINITY;
			double angle = (angleMax + angleMin) / 2;
			selectionXs[0] = Math.cos(angle) * selectionRadius + centerX;
			selectionYs[0] = Math.sin(angle) * selectionRadius + centerY;
		} else {
			angleInterval = (angleMax - angleMin) / (selectionXs.length - 1);
			for (int i = 0; i < selectionXs.length; i++) {
				double angle = angleMin + angleInterval * i;
				selectionXs[i] = Math.cos(angle) * selectionRadius + centerX;
				selectionYs[i] = Math.sin(angle) * selectionRadius + centerY;
			}
		}
	}

	private double distanceSqFromCenter(double x, double y) {
		double a = centerX - x;
		double b = centerY - y;
		return a * a + b * b;
	}

	@Override
	public void update(float tDelta) {
		if (hidden)
			return;

		Vector3 cursor = model.controller.getCursor(null);
		boolean wasDown = down;
		down = Gdx.input.isButtonPressed(Buttons.LEFT);
		double distanceSqFromCenter = distanceSqFromCenter(cursor.x, cursor.y);
		boolean inBoundsFromCenter = distanceSqFromCenter < triggerRadiusSq;
		if (!down) {
			if (wasDown && target && expanded) {
				expanded = false;
				if (selected != -1)
					selectionTasks.selected(selected);
			}
		} else {
			if (!wasDown) {
				expanded = target = inBoundsFromCenter;
				selected = -1;
			} else if (target && distanceSqFromCenter >= triggerRadiusSq && distanceSqFromCenter < outerBoundaryRadiusSq) {
				selected = (int) (normalize(Math.atan2(cursor.y - centerY, cursor.x - centerX) - angleMin + angleInterval / 2) / angleInterval);
				if (selected < 0 || selected >= selectionXs.length)
					selected = -1;
			} else {
				selected = -1;
			}
		}
	}

	@Override
	public void draw(SpriteBatch batch) {
		BitmapFont fnt = model.assets.get("fonts/buttons.fnt", BitmapFont.class);
		if (expanded) {
			for (int i = 0; i < selectionXs.length; i++) {
				double x = selectionXs[i];
				double y = selectionYs[i];

				TextBounds bnds = fnt.getBounds(selectionTexts[i]);
				fnt.setColor(i == selected ? selectedFontTint : fontTint);
				fnt.draw(batch, selectionTexts[i], (float) x - bnds.width / 2, (float) y + bnds.height / 2);
			}
		} else {
			TextBounds bnds = fnt.getBounds(text);
			fnt.setColor(groupFontTint);
			fnt.draw(batch, text, centerX - bnds.width / 2, centerY + bnds.height / 2);
		}
	}
}

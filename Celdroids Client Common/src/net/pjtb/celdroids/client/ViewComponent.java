package net.pjtb.celdroids.client;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface ViewComponent {
	public void update(float tDelta);

	public void draw(SpriteBatch batch);
}

package net.pjtb.celdroids.client.scenes;

public interface Scene {
	public void swappedIn();

	public void pause();

	public void resume();

	public void update(float tDelta);

	public void draw();

	public void swappedOut();
}

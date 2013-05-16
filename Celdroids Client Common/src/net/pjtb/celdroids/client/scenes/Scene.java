package net.pjtb.celdroids.client.scenes;

import net.pjtb.celdroids.client.CanvasComponent;

public interface Scene extends CanvasComponent {
	public void swappedIn(boolean transition);

	public void pause();

	public void resume();

	public void swappedOut(boolean transition);

	public Scene getSubscene();

	public void setSubscene(Scene scene);
}

package in.kevinj.colonists.client;

import java.util.Collection;

public interface Scene extends ViewComponent {
	public Collection<PriorityQueueAssetManager.LoadEntry> getAssetDependencies();

	public Collection<String> getSpriteSheetDependencies();

	public void swappedIn(boolean transition);

	public void resize(int width, int height);

	public void pause();

	public void resume();

	public void swappedOut(boolean transition);

	public Scene getSubscene();

	public void setSubscene(Scene scene);
}

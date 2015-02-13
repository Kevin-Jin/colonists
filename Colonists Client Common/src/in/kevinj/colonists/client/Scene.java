package in.kevinj.colonists.client;

public interface Scene extends ViewComponent {
	public void swappedIn(boolean transition);

	public void resize(int width, int height);

	public void pause();

	public void resume();

	public void swappedOut(boolean transition);

	public Scene getSubscene();

	public void setSubscene(Scene scene);
}

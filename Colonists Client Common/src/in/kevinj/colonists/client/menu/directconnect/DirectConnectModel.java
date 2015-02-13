package in.kevinj.colonists.client.menu.directconnect;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import in.kevinj.colonists.Constants;
import in.kevinj.colonists.NioSession;
import in.kevinj.colonists.client.ConnectStatusPopupModel;
import in.kevinj.colonists.client.Model;
import in.kevinj.colonists.client.NetworkPlayerBattleOpponent;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector3;

public class DirectConnectModel extends ConnectStatusPopupModel implements InputProcessor {
	public String entered;
	public boolean inactive;

	public DirectConnectModel(Model model) {
		super(model);

		entered = "";
	}

	@Override
	public boolean keyDown(int keycode) {
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	protected void failed(String message) {
		super.failed(message);
		inactive = false;
		Gdx.input.setOnscreenKeyboardVisible(true);
	}

	@Override
	public NetworkPlayerBattleOpponent update(float tDelta) {
		Vector3 cursor = parent.controller.getCursor(null);
		int leftX = (Constants.WIDTH - 970) / 2, bottomY = (Constants.HEIGHT - 300) / 2;
		// in case user hid soft keyboard
		if (Gdx.input.isButtonPressed(Buttons.LEFT) && (cursor.x >= leftX && cursor.y >= bottomY && cursor.x < leftX + 970 && cursor.y < bottomY + 300))
			Gdx.input.setOnscreenKeyboardVisible(true);

		return super.update(tDelta);
	}

	private void connect(final String dnsOrIp, final int port) {
		progress("Resolving...");
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					final InetAddress addr = InetAddress.getByName(dnsOrIp);
					Gdx.app.postRunnable(new Runnable() {
						@Override
						public void run() {
							progress("Attempting connection...");
							state = NioSession.beginCreateClient(new InetSocketAddress(addr, port), Constants.SOCKET_TIMEOUT);
						}
					});
				} catch (UnknownHostException e) {
					Gdx.app.postRunnable(new Runnable() {
						@Override
						public void run() {
							failed("Unreachable address. Try again.");
						}
					});
				}
			}
		}, "async-dns-resolve").start();
	}

	@Override
	public boolean keyTyped(char character) {
		if (inactive)
			return false;

		switch (character) {
			case '\r':
			case '\n':
				inactive = true;
				Gdx.input.setOnscreenKeyboardVisible(false);
				int portSplit = entered.lastIndexOf(':');
				if (portSplit != -1) {
					try {
						connect(entered.substring(0, portSplit), Integer.parseInt(entered.substring(portSplit + 1)));
					} catch (NumberFormatException e) {
						failed("Unreachable address. Try again.");
					}
				} else {
					connect(entered, Constants.PORT);
				}
				return true;
			case '\b':
				if (entered.length() > 0)
					entered = entered.substring(0, entered.length() - 1);
				return true;
			default:
				if (character == '.' || character == '-' || character == ':' || character >= '0' && character <= '9' || character >= 'a' && character <= 'z' || character >= 'A' && character <= 'Z') {
					entered += character;
					return true;
				}
				return false;
		}
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		return false;
	}
}

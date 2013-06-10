package net.pjtb.celdroids.client.menu.directconnect;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import net.pjtb.celdroids.Constants;
import net.pjtb.celdroids.NioSession;
import net.pjtb.celdroids.NioSession.IncompleteNioSession;
import net.pjtb.celdroids.client.ControllerHelper;
import net.pjtb.celdroids.client.Model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Input.Buttons;

public class DirectConnectModel implements InputProcessor {
	public final Model parent;

	public String entered;
	public InetAddress address;
	public String message;
	public boolean error, inactive;

	private IncompleteNioSession connectState;

	public DirectConnectModel(Model model) {
		this.parent = model;

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

	public void update(float tDelta) {
		int cursorX = ControllerHelper.getCursorX();
		int cursorY = ControllerHelper.getCursorY();
		int leftX = (Constants.WIDTH - 970) / 2, bottomY = (Constants.HEIGHT - 300) / 2;
		//in case user hid soft keyboard
		if (Gdx.input.isButtonPressed(Buttons.LEFT) && (cursorX >= leftX && cursorY >= bottomY && cursorX < leftX + 970 && cursorY < bottomY + 300))
			Gdx.input.setOnscreenKeyboardVisible(true);

		if (connectState == null)
			return;

		NioSession ses = connectState.updateConnectStatus(tDelta);
		if (connectState.error != null) {
			message = connectState.error;
			error = true;
			inactive = false;
			Gdx.input.setOnscreenKeyboardVisible(true);
			connectState = null;
		} else if (ses != null) {
			message = "Success!";
			error = false;
			connectState = null;
			//TODO: go to BattleScene
		}
	}

	private void connect(final String dnsOrIp, final int port) {
		message = "Resolving...";
		error = false;
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					final InetAddress addr = InetAddress.getByName(dnsOrIp);
					Gdx.app.postRunnable(new Runnable() {
						@Override
						public void run() {
							address = addr;
							message = "Attempting connection...";
							error = false;
							connectState = NioSession.beginCreateClient(new InetSocketAddress(addr, port), Constants.SOCKET_TIMEOUT);
						}
					});
				} catch (UnknownHostException e) {
					Gdx.app.postRunnable(new Runnable() {
						@Override
						public void run() {
							message = "Unreachable address. Try again.";
							error = true;
							inactive = false;
							Gdx.input.setOnscreenKeyboardVisible(true);
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
						message = "Unreachable address. Try again.";
						error = true;
						inactive = false;
						Gdx.input.setOnscreenKeyboardVisible(true);
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

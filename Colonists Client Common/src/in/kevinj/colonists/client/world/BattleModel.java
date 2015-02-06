package in.kevinj.colonists.client.world;

import in.kevinj.colonists.client.AiBattleOpponent;
import in.kevinj.colonists.client.BattleOpponent;
import in.kevinj.colonists.client.ConfirmPopupScene;
import in.kevinj.colonists.client.LocalPlayerBattleOpponent;
import in.kevinj.colonists.client.Model;
import in.kevinj.colonists.client.NetworkPlayerBattleOpponent;
import in.kevinj.colonists.client.Scene;

import java.util.EnumMap;
import java.util.Map;

public class BattleModel {
	public enum BattleSubSceneType {
		CONFIRM_FLEE_POPUP
	}

	public final Model parent;

	protected final Map<BattleSubSceneType, Scene> subScenes;
	private final Scene fleeToWorldPopup, fleeToMenuPopup;

	public BattleAnimation currentAnimation;

	public final LocalPlayerBattleOpponent self;
	public BattleOpponent op;

	public boolean selfTurn, canAct;

	public BattleModel(Model model) {
		this.parent = model;

		subScenes = new EnumMap<BattleSubSceneType, Scene>(BattleSubSceneType.class);
		fleeToWorldPopup = new ConfirmPopupScene(parent, "Are you sure you want to flee?", Model.SceneType.WORLD);
		fleeToMenuPopup = new ConfirmPopupScene(parent, "Are you sure you want to flee?", Model.SceneType.MAIN_MENU);

		this.self = new LocalPlayerBattleOpponent(null, this);
	}

	private void init() {
		
	}

	public void initLocal(AiBattleOpponent op) {
		init();
		this.op = op;
		subScenes.put(BattleSubSceneType.CONFIRM_FLEE_POPUP, fleeToWorldPopup);
	}

	public void initRemote(NetworkPlayerBattleOpponent op, boolean swapTurnsAtEnd) {
		init();
		this.op = op;
		subScenes.put(BattleSubSceneType.CONFIRM_FLEE_POPUP, fleeToMenuPopup);
	}
}

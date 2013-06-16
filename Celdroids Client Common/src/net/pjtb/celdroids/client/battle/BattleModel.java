package net.pjtb.celdroids.client.battle;

import java.util.EnumMap;
import java.util.Map;

import net.pjtb.celdroids.client.AiBattleOpponent;
import net.pjtb.celdroids.client.BattleOpponent;
import net.pjtb.celdroids.client.CeldroidMonster;
import net.pjtb.celdroids.client.CeldroidProperties;
import net.pjtb.celdroids.client.ConfirmPopupScene;
import net.pjtb.celdroids.client.LocalPlayerBattleOpponent;
import net.pjtb.celdroids.client.Model;
import net.pjtb.celdroids.client.NetworkPlayerBattleOpponent;
import net.pjtb.celdroids.client.Scene;

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
	public boolean showSelfCeldroid, showOpponentCeldroid;

	public BattleModel(Model model) {
		this.parent = model;

		subScenes = new EnumMap<BattleSubSceneType, Scene>(BattleSubSceneType.class);
		fleeToWorldPopup = new ConfirmPopupScene(parent, "Are you sure you want to flee?", Model.SceneType.WORLD);
		fleeToMenuPopup = new ConfirmPopupScene(parent, "Are you sure you want to flee?", Model.SceneType.MAIN_MENU);

		this.self = new LocalPlayerBattleOpponent(null, this);
	}

	private void init() {
		self.party.clear();
		self.party.add(new CeldroidMonster(parent.assets.get("monsters/fire1.json", CeldroidProperties.class), null));
		self.party.add(new CeldroidMonster(parent.assets.get("monsters/water1.json", CeldroidProperties.class), null));
		self.party.add(new CeldroidMonster(parent.assets.get("monsters/rock1.json", CeldroidProperties.class), null));
	}

	public void initLocal(AiBattleOpponent op) {
		self.smnAnimation.swapTurnsAtEnd = false;
		op.smnAnimation.swapTurnsAtEnd = true;
		init();
		this.op = op;
		subScenes.put(BattleSubSceneType.CONFIRM_FLEE_POPUP, fleeToWorldPopup);
	}

	public void initRemote(NetworkPlayerBattleOpponent op, boolean swapTurnsAtEnd) {
		self.smnAnimation.swapTurnsAtEnd = swapTurnsAtEnd;
		op.smnAnimation.swapTurnsAtEnd = !swapTurnsAtEnd;
		init();
		this.op = op;
		subScenes.put(BattleSubSceneType.CONFIRM_FLEE_POPUP, fleeToMenuPopup);
	}
}

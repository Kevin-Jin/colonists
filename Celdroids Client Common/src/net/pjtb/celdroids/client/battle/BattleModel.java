package net.pjtb.celdroids.client.battle;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.pjtb.celdroids.client.AiBattleOpponent;
import net.pjtb.celdroids.client.BattleOpponent;
import net.pjtb.celdroids.client.CeldroidMonster;
import net.pjtb.celdroids.client.CeldroidProperties;
import net.pjtb.celdroids.client.ConfirmPopupScene;
import net.pjtb.celdroids.client.Model;
import net.pjtb.celdroids.client.PlayerBattleOpponent;
import net.pjtb.celdroids.client.Scene;

public class BattleModel {
	public enum BattleSubSceneType { CONFIRM_FLEE_POPUP }

	public final Model parent;

	protected final Map<BattleSubSceneType, Scene> subScenes;
	private final Scene fleeToWorldPopup, fleeToMenuPopup;

	public final List<CeldroidMonster> party;
	public BattleAnimation currentAnimation;

	public BattleOpponent op;

	public boolean selfTurn, canAct;
	public boolean showSelfCeldroid, showOpponentCeldroid;

	public BattleModel(Model model) {
		this.parent = model;

		subScenes = new EnumMap<BattleSubSceneType, Scene>(BattleSubSceneType.class);
		fleeToWorldPopup = new ConfirmPopupScene(parent, "Are you sure you want to flee?", Model.SceneType.WORLD);
		fleeToMenuPopup = new ConfirmPopupScene(parent, "Are you sure you want to flee?", Model.SceneType.MAIN_MENU);

		this.party = new ArrayList<CeldroidMonster>();
	}

	public void initLocal(AiBattleOpponent op) {
		this.op = op;
		subScenes.put(BattleSubSceneType.CONFIRM_FLEE_POPUP, fleeToWorldPopup);
	}

	public void initRemote(PlayerBattleOpponent op) {
		this.op = op;
		subScenes.put(BattleSubSceneType.CONFIRM_FLEE_POPUP, fleeToMenuPopup);
	}

	public void updateParty() {
		party.clear();
		party.add(new CeldroidMonster(parent.assets.get("monsters/fire1.json", CeldroidProperties.class), null));
		party.add(new CeldroidMonster(parent.assets.get("monsters/water1.json", CeldroidProperties.class), null));
		party.add(new CeldroidMonster(parent.assets.get("monsters/rock1.json", CeldroidProperties.class), null));
	}

	public void swapPartyLead(int i) {
		CeldroidMonster currentLead = party.get(0);
		party.set(0, party.get(i));
		party.set(i, currentLead);
	}
}

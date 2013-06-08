package net.pjtb.celdroids.client.battle;

import java.util.ArrayList;
import java.util.List;

import net.pjtb.celdroids.client.CeldroidMonster;
import net.pjtb.celdroids.client.CeldroidProperties;
import net.pjtb.celdroids.client.Model;

public class BattleModel {
	public final Model parent;

	public final List<CeldroidMonster> party;
	public BattleAnimation currentAnimation;

	public boolean selfTurn, canAct;

	public BattleModel(Model model) {
		this.parent = model;

		this.party = new ArrayList<CeldroidMonster>();

		selfTurn = true;
		canAct = true;
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

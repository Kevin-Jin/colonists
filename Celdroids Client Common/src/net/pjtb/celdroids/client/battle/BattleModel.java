package net.pjtb.celdroids.client.battle;

import java.util.ArrayList;
import java.util.List;

import net.pjtb.celdroids.client.CeldroidMonster;
import net.pjtb.celdroids.client.CeldroidProperties;
import net.pjtb.celdroids.client.Model;

public class BattleModel {
	public final Model parent;

	public final List<CeldroidMonster> party;

	public BattleModel(Model model) {
		this.parent = model;

		this.party = new ArrayList<CeldroidMonster>();
		party.add(new CeldroidMonster(new CeldroidProperties("Water", "monster/water/evol1"), null));
		party.add(new CeldroidMonster(new CeldroidProperties("Rock", "monster/rock/evol1"), null));
	}

	public void swapPartyLead(int i) {
		CeldroidMonster currentLead = party.get(0);
		party.set(0, party.get(i));
		party.set(i, currentLead);
	}
}

package net.pjtb.celdroids.client;

import java.util.ArrayList;
import java.util.List;

public abstract class BattleOpponent {
	public final String name;

	public final List<CeldroidMonster> party;

	public BattleOpponent(String name) {
		this.name = name;
		party = new ArrayList<CeldroidMonster>();
	}

	public abstract String nextMove();
}

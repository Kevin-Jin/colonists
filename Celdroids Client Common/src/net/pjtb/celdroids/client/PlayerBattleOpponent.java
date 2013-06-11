package net.pjtb.celdroids.client;

import net.pjtb.celdroids.Session;

public class PlayerBattleOpponent extends BattleOpponent {
	private final Session ses;

	public PlayerBattleOpponent(String name, Session ses) {
		super(name);
		this.ses = ses;
	}

	@Override
	public String nextMove() {
		return null;
	}
}

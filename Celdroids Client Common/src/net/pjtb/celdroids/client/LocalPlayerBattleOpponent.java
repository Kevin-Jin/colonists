package net.pjtb.celdroids.client;

import net.pjtb.celdroids.client.battle.BattleAnimation;
import net.pjtb.celdroids.client.battle.BattleModel;

public class LocalPlayerBattleOpponent extends BattleOpponent {
	public LocalPlayerBattleOpponent(String name, BattleModel model) {
		super(name, model);
	}

	@Override
	public void sendMove(BattleAnimation anim) {
		queue.add(anim);
	}
}

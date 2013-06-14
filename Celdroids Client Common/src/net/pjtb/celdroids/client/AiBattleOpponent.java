package net.pjtb.celdroids.client;

import java.util.List;

import net.pjtb.celdroids.client.battle.BattleAnimation;
import net.pjtb.celdroids.client.battle.BattleModel;

public class AiBattleOpponent extends BattleOpponent {
	public AiBattleOpponent(String name, BattleModel model) {
		super(name, model);

		smnAnimation.reset();
		queue.add(smnAnimation);
	}

	@Override
	public BattleAnimation getNextMove(BattleModel model) {
		BattleAnimation anim = super.getNextMove(model);
		if (anim != null)
			return anim;

		//TODO: actual intelligence
		List<CeldroidBattleMove> moves = party.get(0).monsterType.moves;
		atkAnimation.reset(model.parent.assets.get(moves.get((int) (Math.random() * moves.size())).file, CeldroidBattleMove.class));
		return atkAnimation;
	}

	@Override
	public void sendMove(BattleAnimation anim) {
		
	}
}

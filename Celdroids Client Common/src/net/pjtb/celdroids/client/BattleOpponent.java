package net.pjtb.celdroids.client;

import java.util.ArrayList;
import java.util.List;

import net.pjtb.celdroids.client.battle.AttackAnimation;
import net.pjtb.celdroids.client.battle.BattleAnimation;
import net.pjtb.celdroids.client.battle.BattleModel;
import net.pjtb.celdroids.client.battle.DismissAnimation;
import net.pjtb.celdroids.client.battle.SummonAnimation;

public abstract class BattleOpponent {
	public final String name;

	public final List<CeldroidMonster> party;

	protected final List<BattleAnimation> queue;
	public final AttackAnimation atkAnimation;
	public final SummonAnimation smnAnimation;
	public final DismissAnimation dmAnimation;

	public BattleOpponent(String name, BattleModel model) {
		this.name = name;
		party = new ArrayList<CeldroidMonster>();

		queue = new ArrayList<BattleAnimation>();
		atkAnimation = new AttackAnimation(model);
		smnAnimation = new SummonAnimation(model);
		dmAnimation = new DismissAnimation(model);
	}

	public BattleAnimation getNextMove(BattleModel model) {
		if (!queue.isEmpty())
			return queue.remove(0);
		return null;
	}

	public abstract void sendMove(BattleAnimation anim);
}

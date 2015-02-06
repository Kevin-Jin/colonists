package in.kevinj.colonists.client;

import in.kevinj.colonists.client.world.BattleAnimation;
import in.kevinj.colonists.client.world.BattleModel;

public abstract class BattleOpponent {
	public final String name;

	public BattleOpponent(String name, BattleModel model) {
		this.name = name;
	}

	public abstract void sendMove(BattleAnimation anim);

	public void sendFlee() {
		
	}
}

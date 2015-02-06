package in.kevinj.colonists.client;

import in.kevinj.colonists.client.world.BattleAnimation;
import in.kevinj.colonists.client.world.BattleModel;

public class LocalPlayerBattleOpponent extends BattleOpponent {
	public LocalPlayerBattleOpponent(String name, BattleModel model) {
		super(name, model);
	}

	@Override
	public void sendMove(BattleAnimation anim) {

	}
}

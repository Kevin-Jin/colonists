package in.kevinj.colonists.client;

import in.kevinj.colonists.client.world.BattleModel;

//Players will trade request, use year of plenty, or attempt to rob a resource
//on resources they want but are unlikely to obtain (i.e. few settlements near
//the tile or tiles are bad numbers). likeliness to obtain = sum on tiles w/ res
//[(settlements on tile * 1 + towns on tile * 2) * (6 - Math.abs(7 - tile #))]
//TODO: define when to use monopoly, when to trade.
//TODO: define greedy, prefer to sacrifice resources to bank/port or wait for
//better trade.
//
public class TrainerProperties {
	public final String trainerName;

	/**
	 * Offensive vs. defensive. If one move can be offensive and defensive at
	 *   the same time, either type of AI will prefer the move over others.
	 *   Ability to accurately predict is based on {@link #observantFactor}.
	 * Effects:
	 *  - Robber: Defensive AI sabotages players most likely to win.
	 *            Offensive AI uses it for their own gain.
	 *  - Roads:  Defensive AI blocks players building a longest road.
	 *            Offensive AI builds longest road.
	 *  - Trades: Defensive AI avoids trades that let others win.
	 *            Offensive AI trades for their own gain.
	 */
	public final int offensiveFactor;
	/**
	 * Largest army inclined (LAI) vs. longest road inclined (LRI), i.e. does
	 *   the user prefer investing in cities and development cards or in roads
	 *   and settlements.
	 * Effects:
	 *  - Invest: LAI AI settle near ore, sheep, and wheat.
	 *            LRI AI settle near wood, brick.
	 *            Note: AI aims to get all 5 resources when placing first 2
	 *              settlements. It spends next few moves settling near any
	 *              missed. Applies only when all resources are held and AI is
	 *              deciding between two tiles with same numbers and distance.
	 */
	public final int largestArmyFactor;
	/**
	 * Blind vs. observant. More observant pays attention to responses to trade
	 *   requests, results of trades, and which resources are drawn by others.
	 *   Holds onto less cards in case 7 is rolled. Increases with skill.
	 * Effects:
	 *  - Blind will go to second best option when placing settlements and will
	 *      play obvious moves without analyzing plans of others.
	 *	- Observant will more effectively use monopoly card.
	 *  - Blind will ask for trades that are more beneficial to other players.
	 *  - Observant will assume all development cards in a player's hand is a
	 *      VP card, thereby switching to defensive mode earlier.
	 */
	public final int observantFactor;
	/**
	 * Strategic vs. tactical. Strategic players make moves by analyzing
	 *   future outcomes and possibilities from move made now, subjecting
	 *   themselves to risk if other players prevent their play. Tactical
	 *   players seek to increase short term gain with less risk, e.g. increase
	 *   their resources, build, buy development cards. Tactical players do
	 *   everything as soon as they can, whereas strategic ones hold off on some
	 *   actions to surprise with large gains of VP in one move (e.g. hoarding
	 *   wood and brick [or road building, year of plenty, monopoly card] to
	 *   build longest road, or hoarding knight cards to suddenly get largest
	 *   army). Increases with skill.
	 */
	public final int strategicFactor;

	/**
	 * Private constructor can still be called final variables can still be
	 * assigned by reflection in Json class.
	 * Just set values to garbage.
	 */
	private TrainerProperties() {
		trainerName = null;
		offensiveFactor = 0;
		largestArmyFactor = 0;
		observantFactor = 0;
		strategicFactor = 0;
	}

	public AiBattleOpponent createInstance(BattleModel model) {
		AiBattleOpponent op = new AiBattleOpponent(trainerName, model);
		return op;
	}
}

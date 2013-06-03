package net.pjtb.celdroids.client;

public class CeldroidMonster {
	public final CeldroidProperties monsterType;
	public String nickName;

	public CeldroidMonster(CeldroidProperties monsterType, String nickName) {
		this.monsterType = monsterType;
		this.nickName = nickName;
	}

	public String getName() {
		if (nickName == null)
			return monsterType.name;
		return nickName;
	}
}

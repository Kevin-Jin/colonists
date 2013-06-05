package net.pjtb.celdroids.client;

import java.util.ArrayList;

public class CeldroidBattleMove {
	public final String name;
	public final String spriteDirectory;
	public final int frameCount;
	public final ArrayList<CeldroidElement> element;
	public final int damage;
	public final ArrayList<CeldroidDebuff> inflicts;
	public final boolean selfOrigin;
	public final boolean loop;

	public CeldroidBattleMove() {
		name = null;
		spriteDirectory = null;
		frameCount = 0;
		element = null;
		damage = 0;
		inflicts = null;
		selfOrigin = false;
		loop = false;
	}
}

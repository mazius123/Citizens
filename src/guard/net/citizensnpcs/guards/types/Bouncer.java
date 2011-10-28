package net.citizensnpcs.guards.types;

import net.citizensnpcs.Settings;
import net.citizensnpcs.guards.Guard;
import net.citizensnpcs.guards.GuardUpdater;
import net.citizensnpcs.guards.Targeter;
import net.citizensnpcs.resources.npclib.HumanNPC;
import net.citizensnpcs.utils.LocationUtils;
import net.citizensnpcs.utils.PathUtils;

import org.bukkit.entity.LivingEntity;

public class Bouncer implements GuardUpdater {

	private boolean continueReturn(HumanNPC npc) {
		return LocationUtils.withinRange(npc.getLocation(),
				npc.getBaseLocation(), 3.5);
	}

	private boolean findTarget(HumanNPC npc) {
		Guard guard = npc.getType("guard");
		LivingEntity entity = Targeter.findTarget(Targeter.getNearby(
				npc.getPlayer(), guard.getProtectionRadius()), npc);
		if (entity != null)
			guard.target(entity, npc);
		return entity != null;
	}

	private boolean keepAttacking(HumanNPC npc) {
		Guard guard = npc.getType("guard");
		return npc.getHandle().hasTarget()
				&& LocationUtils.withinRange(npc.getLocation(),
						npc.getBaseLocation(), guard.getProtectionRadius());
	}

	@Override
	public void onDamage(HumanNPC npc, LivingEntity attacker) {
		Guard guard = npc.getType("guard");
		guard.target(attacker, npc);
	}

	private boolean startReturning(HumanNPC npc) {
		PathUtils.createPath(npc, npc.getBaseLocation(), -1,
				Settings.getInt("MaxStationaryReturnTicks"));
		return true;
	}

	@Override
	public GuardStatus updateStatus(GuardStatus current, HumanNPC npc) {
		switch (current) {
		case NORMAL:
			if (findTarget(npc)) {
				npc.setPaused(true);
				return GuardStatus.ATTACKING;
			}
			break;
		case ATTACKING:
			if (!keepAttacking(npc)) {
				startReturning(npc);
				return GuardStatus.RETURNING;
			}
			break;
		case RETURNING:
			if (!continueReturn(npc)) {
				npc.setPaused(false);
				return GuardStatus.NORMAL;
			}
			break;
		}
		return current;
	}
}

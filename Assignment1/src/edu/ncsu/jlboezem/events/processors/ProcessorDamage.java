package edu.ncsu.jlboezem.events.processors;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import edu.ncsu.jlboezem.common.properties.Damageable;
import edu.ncsu.jlboezem.common.properties.Damaging;
import edu.ncsu.jlboezem.common.properties.Spawner;
import edu.ncsu.jlboezem.communication.ConnectionManager;
import edu.ncsu.jlboezem.events.Event;
import edu.ncsu.jlboezem.events.EventManager;
import edu.ncsu.jlboezem.threading.ThreadManager;

public class ProcessorDamage extends Processor {
	@Override
	public boolean handle(Event e) {
		final String guid = (String) e.getInfo().get("guid");
		final String oGuid = (String) e.getInfo().get("oGuid");
		final String playerGuid;
		if (null == guid)
			return true;
		if (!guid.matches(".*Player.*")) {
			if (null != oGuid) {
				playerGuid = oGuid;
			} else {
				playerGuid = null;
			}
		} else {
			playerGuid = guid;
		}
		if (Damageable.INSTANCE.isAlive(playerGuid) && e.getType().matches(".*Collision.*")) {
			if (null != guid && null != oGuid) {
				if (Damageable.INSTANCE.hasGameObject(guid) && Damaging.INSTANCE.hasGameObject(oGuid)) {
					doDamage(guid, oGuid);
				}
				if (Damageable.INSTANCE.hasGameObject(oGuid) && Damaging.INSTANCE.hasGameObject(guid)) {
					doDamage(oGuid, guid);
				}
			}
		}
		if (e.getType().matches(".*PlayerSpawn.*")) {
			EventManager.INSTANCE.removeEventsThatMatch(
					event -> event.getTimestamp() >= ConnectionManager.INSTANCE.getGameTimeline().getElapsedTime()
					&& event.getType().matches("(Player(?!Spawn)|Collision)") 
					&& (event.getInfo().getOrDefault("guid", "").equals(guid) || event.getInfo().getOrDefault("oGuid", "").equals(guid)));
			Spawner.INSTANCE.spawn("PlayerSpawner", guid);
			//System.out.println("Player spawn!");
		} else if (Damageable.INSTANCE.isAlive(guid) && e.getType().matches(".*PlayerDeath.*")) {
			EventManager.INSTANCE.removeEventsThatMatch(
					event -> event.getTimestamp() >= ConnectionManager.INSTANCE.getGameTimeline().getElapsedTime() 
					&& event.getType().matches(".*(Player(?!Spawn)|Collision).*") 
					&& (event.getInfo().getOrDefault("guid", "").equals(guid) || event.getInfo().getOrDefault("oGuid", "").equals(guid)));
			ProcessorPlayer.clearPlayer(guid);
		}
		return true;
	}

	
	private void doDamage(String reciever, String dealer) {
		Damageable.INSTANCE.updateObject(reciever, -Damaging.INSTANCE.getDamage(dealer));
		//Drawable.INSTANCE.setOpacity(reciever, Damageable.INSTANCE.getHealth(reciever));
		if (Damageable.INSTANCE.getHealth(reciever) <= 0) {
			Map<String, Object> infoDeath = new ConcurrentHashMap<String, Object>();
			infoDeath.put("guid", reciever);
			Map<String, Object> infoSpawn = new ConcurrentHashMap<String, Object>();
			infoSpawn.put("guid", reciever);
			EventManager.INSTANCE.addEvent(new Event("PlayerDeath", infoDeath, ConnectionManager.INSTANCE.getGameTimeline().getElapsedTime(), ThreadManager.INSTANCE.getLocalName()));
			EventManager.INSTANCE.addEvent(new Event("PlayerSpawn", infoSpawn, ConnectionManager.INSTANCE.getGameTimeline().getElapsedTime() + 1000, ThreadManager.INSTANCE.getLocalName()));
		}
	}
	@Override
	public void subscribe() {
		EventManager.INSTANCE.subscribe(this, ".*Collision.*");
		EventManager.INSTANCE.subscribe(this, ".*Death.*");
		EventManager.INSTANCE.subscribe(this, ".*Player.*");
	}

}

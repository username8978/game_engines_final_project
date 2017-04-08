package edu.ncsu.jlboezem.events.processors;

import java.util.HashMap;
import java.util.Map;

import edu.ncsu.jlboezem.common.Vector2D;
import edu.ncsu.jlboezem.common.properties.Damageable;
import edu.ncsu.jlboezem.common.properties.Drawable;
import edu.ncsu.jlboezem.common.properties.Mobile;
import edu.ncsu.jlboezem.common.properties.Player;
import edu.ncsu.jlboezem.common.properties.Positional;
import edu.ncsu.jlboezem.common.properties.Shaped;
import edu.ncsu.jlboezem.communication.ConnectionManager;
import edu.ncsu.jlboezem.events.Event;
import edu.ncsu.jlboezem.events.EventManager;
import edu.ncsu.jlboezem.threading.ThreadManager;

public class ProcessorPlayer extends Processor {
	@Override
	public boolean handle(Event e) {
		final float MOVE_AMOUNT = 2.5f;
		final String guid = (String) e.getInfo().get("guid");
		if (null == guid)
			return true;
		if (e.getType().matches(".*PlayerMove.*")) {
			final Vector2D position = (Vector2D) e.getInfo().get("Position");
			if (null != position)
				Positional.INSTANCE.setPosition(guid, position.clone());
			String direction = (String) e.getInfo().get("Direction");
			if (Player.INSTANCE.getIsPlayerMovingOnInput(guid, direction)) {
				return true;
			}
			switch (direction) {
			case "Jump":
				//Up
				Positional.INSTANCE.updateObject(guid, 0, -MOVE_AMOUNT*5);
				Mobile.INSTANCE.updateVelocity(guid, 0, -MOVE_AMOUNT*2);
				EventManager.INSTANCE.removeEventsThatMatch(
						event -> event.getTimestamp() >= ConnectionManager.INSTANCE.getGameTimeline().getElapsedTime() 
						&& event.getType().matches(".*(PlayerStopMove).*") 
						&& (event.getInfo().getOrDefault("guid", "").equals(guid) || event.getInfo().getOrDefault("oGuid", "").equals(guid))
						&& event.getInfo().getOrDefault("Direction", "").equals("Up"));
				Map<String, Object> info = new HashMap<String, Object>();
				info.put("guid", guid);
				info.put("Direction", "Up");
				EventManager.INSTANCE.addEvent(new Event("PlayerStopMove", info, ConnectionManager.INSTANCE.getGameTimeline().getElapsedTime() + 1000, ThreadManager.INSTANCE.getLocalName()));
				Player.INSTANCE.setPlayerMovingOnInput(guid, "Jump", true);
				return true;
			case "Right":
				//Right
				Mobile.INSTANCE.setVelocityX(guid, MOVE_AMOUNT);

				Player.INSTANCE.setPlayerMovingOnInput(guid, "Right", true);
				return true;
			case "Down":
				//Down
				//TODO: add?
				return true;
			case "Left":
				//Left
				Mobile.INSTANCE.setVelocityX(guid, -MOVE_AMOUNT);

				Player.INSTANCE.setPlayerMovingOnInput(guid, "Left", true);
				return true;
			}
		} else if (Player.INSTANCE.getIsPlayerMovingOnInput(guid) && e.getType().matches(".*PlayerStopMove.*")) {
			String direction = (String) e.getInfo().get("Direction");
			switch (direction) {
			case "Up":
				//Up
				if (!Player.INSTANCE.getIsPlayerMovingOnInput(guid, "Jump")) {
					return true;
				}
				Mobile.INSTANCE.updateVelocity(guid, 0, MOVE_AMOUNT*2);
				System.out.println("Jump Expired");
				//Player.INSTANCE.setPlayerMovingOnInput(guid, "Jump", false);
				return true;
			case "Right":
				//Right
				if (!Player.INSTANCE.getIsPlayerMovingOnInput(guid, direction)) {
					return true;
				}
				Mobile.INSTANCE.setVelocityX(guid, 0);
				Player.INSTANCE.setPlayerMovingOnInput(guid, direction, false);
				return true;
			case "Down":
				//Down
				//TODO: add?
				return true;
			case "Left":
				//Left
				if (!Player.INSTANCE.getIsPlayerMovingOnInput(guid, direction)) {
					return true;
				}
				Mobile.INSTANCE.setVelocityX(guid, 0);
				Player.INSTANCE.setPlayerMovingOnInput(guid, direction, false);
				return true;
			}
		} else if (e.getType().matches(".*PlayerDropped.*")) {
			clearPlayer((String) e.getInfo().get("guid"));
		} else if (e.getType().matches(".*Collision.*")) {
			final String oGuid = (String) e.getInfo().get("oGuid");
			final String playerGuid;
			if (!guid.matches(".*Player.*")) {
				if (null != oGuid) {
					playerGuid = oGuid;
				} else {
					playerGuid = null;
				}
			} else {
				playerGuid = guid;
			}
			if (null != playerGuid) {
				Mobile.INSTANCE.setVelocityY(playerGuid, 0);
				Player.INSTANCE.setPlayerMovingOnInput(playerGuid, "Jump", false);
				EventManager.INSTANCE.removeEventsThatMatch(
						event -> event.getTimestamp() >= ConnectionManager.INSTANCE.getGameTimeline().getElapsedTime() 
						&& event.getType().matches(".*(PlayerStopMove).*") 
						&& (event.getInfo().getOrDefault("guid", "").equals(guid) || event.getInfo().getOrDefault("oGuid", "").equals(guid))
						&& event.getInfo().getOrDefault("Direction", "").equals("Up"));
			}
		}
		return true;
	}

	static void clearPlayer(String guid) {
		//TODO: Trigger drop event, make sure everything is removed.
		Positional.INSTANCE.removeObject(guid);
		Mobile.INSTANCE.removeObject(guid);
		Drawable.INSTANCE.removeObject(guid);
		Damageable.INSTANCE.removeObject(guid);
		Shaped.INSTANCE.removeObject(guid);
	}

	@Override
	public void subscribe() {
		EventManager.INSTANCE.subscribe(this, ".*(Player|Collision).*");
	}

}

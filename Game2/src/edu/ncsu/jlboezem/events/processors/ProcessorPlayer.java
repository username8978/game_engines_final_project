package edu.ncsu.jlboezem.events.processors;

import java.util.HashMap;
import java.util.Map;

import edu.ncsu.jlboezem.common.Vector2D;
import edu.ncsu.jlboezem.common.properties.Damageable;
import edu.ncsu.jlboezem.common.properties.Drawable;
import edu.ncsu.jlboezem.common.properties.Mobile;
import edu.ncsu.jlboezem.common.properties.Player;
import edu.ncsu.jlboezem.common.properties.Positional;
import edu.ncsu.jlboezem.common.properties.Scripted;
import edu.ncsu.jlboezem.common.properties.Shaped;
import edu.ncsu.jlboezem.common.properties.Spawner;
import edu.ncsu.jlboezem.communication.ConnectionManager;
import edu.ncsu.jlboezem.events.Event;
import edu.ncsu.jlboezem.events.EventManager;
import edu.ncsu.jlboezem.threading.ThreadManager;

public class ProcessorPlayer extends Processor {
	@Override
	public boolean handle(Event e) {
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
			case "Up":
				//Up
				Mobile.INSTANCE.setVelocityY(guid, -Player.INSTANCE.MOVE_AMOUNT);

				Player.INSTANCE.setPlayerMovingOnInput(guid, "Up", true);
				return true;
			case "Right":
				//Right
				Mobile.INSTANCE.setVelocityX(guid, Player.INSTANCE.MOVE_AMOUNT);

				Player.INSTANCE.setPlayerMovingOnInput(guid, "Right", true);
				return true;
			case "Down":
				Mobile.INSTANCE.setVelocityY(guid, Player.INSTANCE.MOVE_AMOUNT);

				Player.INSTANCE.setPlayerMovingOnInput(guid, "Down", true);
				return true;
			case "Left":
				//Left
				Mobile.INSTANCE.setVelocityX(guid, -Player.INSTANCE.MOVE_AMOUNT);

				Player.INSTANCE.setPlayerMovingOnInput(guid, "Left", true);
				return true;
			case "Boost":
				Scripted.INSTANCE.bindArgument("PlayerInstance", Player.INSTANCE);
				Scripted.INSTANCE.loadScript("boost");
				Scripted.INSTANCE.executeScript();
			}
		} else if (Player.INSTANCE.getIsPlayerMovingOnInput(guid) && e.getType().matches(".*PlayerStopMove.*")) {
			String direction = (String) e.getInfo().get("Direction");
			switch (direction) {
			case "Up":
				//Up
				if (!Player.INSTANCE.getIsPlayerMovingOnInput(guid, "Up")) {
					return true;
				}
				Mobile.INSTANCE.setVelocityY(guid, 0);
				Player.INSTANCE.setPlayerMovingOnInput(guid, direction, false);
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
				if (!Player.INSTANCE.getIsPlayerMovingOnInput(guid, direction)) {
					return true;
				}
				Mobile.INSTANCE.setVelocityY(guid, 0);
				Player.INSTANCE.setPlayerMovingOnInput(guid, direction, false);
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
		EventManager.INSTANCE.subscribe(this, ".*(Player).*");
	}

}

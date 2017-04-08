package edu.ncsu.jlboezem.events.processors;

import java.util.HashMap;
import java.util.Map;

import edu.ncsu.jlboezem.common.properties.Player;
import edu.ncsu.jlboezem.common.properties.Positional;
import edu.ncsu.jlboezem.common.properties.Scripted;
import edu.ncsu.jlboezem.common.properties.Spawner;
import edu.ncsu.jlboezem.events.Event;
import edu.ncsu.jlboezem.events.EventManager;

public class ProcessorKeyPress extends Processor {

	@Override
	public void subscribe() {
		EventManager.INSTANCE.subscribe(this, ".*Key.*");
	}

	@Override
	public boolean handle(Event e) {
		Map<String, Object> info = new HashMap<String, Object>();
		String action = ((String)e.getInfo().get("Action"));
		String newType = null;
		if (null == action)
			return true;
		if (action.matches(".*Player.*Move")) {
			info.put("Direction", action.substring(action.indexOf("Player") + "Player".length(), action.indexOf("Move")));
			info.put("guid", e.getInfo().get("guid"));
			if (e.getType().contains("Released")) {
				newType = ">PlayerStopMove";
			} else if (e.getType().contains("Pressed")) {
				newType = ">PlayerMove";
			}
			info.put("Position", Positional.INSTANCE.getPosition((String) e.getInfo().get("guid")));
		} else if (action.matches(".*Replay.*")) {
			newType = "!*" + action;
		}
		EventManager.INSTANCE.addSpecialEvent(new Event(newType, info, e.getTimestamp() + 1, e.getFrom()));
	/*	switch ((String)e.getInfo().get("Action")) {
		case "LeftMove":
			info.put("Direction", "Left");
		} /*
		if (keybinds.get("LeftMove") == key) {
		Map<String, Object> info = new HashMap<String, Object>();
		info.put("Direction", "Left");
		info.put("guid", "Player" + clientName);
		EventManager.INSTANCE.addSpecialEvent(new Event("PlayerMove", info, ConnectionManager.INSTANCE.getGameTimeline().getTime(), ThreadManager.INSTANCE.getLocalName()));
	} else if (keybinds.get("RightMove") == key) {
		Map<String, Object> info = new HashMap<String, Object>();
		info.put("Direction", "Right");
		info.put("guid", "Player" + clientName);
		EventManager.INSTANCE.addSpecialEvent(new Event("PlayerMove", info, ConnectionManager.INSTANCE.getGameTimeline().getTime(), ThreadManager.INSTANCE.getLocalName()));
	} else if (keybinds.get("UpMove") == key && ConnectionManager.INSTANCE.getGameTimeline().getTime() - lastUp > upTimeDelay) {
		lastUp = ConnectionManager.INSTANCE.getGameTimeline().getTime();
		Map<String, Object> info = new HashMap<String, Object>();
		info.put("Direction", "Up");
		info.put("guid", "Player" + clientName);
		EventManager.INSTANCE.addSpecialEvent(new Event("PlayerMove", info, ConnectionManager.INSTANCE.getGameTimeline().getTime(), ThreadManager.INSTANCE.getLocalName()));
	} else if (keybinds.get("DownMove") == key) {
		Map<String, Object> info = new HashMap<String, Object>();
		info.put("Direction", "Down");
		info.put("guid", "Player" + clientName);
		EventManager.INSTANCE.addSpecialEvent(new Event("PlayerMove", info, ConnectionManager.INSTANCE.getGameTimeline().getTime(), ThreadManager.INSTANCE.getLocalName()));
	} else if (keybinds.get("ReplayRecordStart") == key) {
		EventManager.INSTANCE.addSpecialEvent(new Event("!ReplayRecordStart", null, ConnectionManager.INSTANCE.getGameTimeline().getTime(), ThreadManager.INSTANCE.getLocalName()));
	} */
		//Event action = new Event("", info, e.getTimestamp() + 1, e.getFrom());
		return true;
	}

}

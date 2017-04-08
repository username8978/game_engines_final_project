package edu.ncsu.jlboezem.events.processors;

import edu.ncsu.jlboezem.common.Vector2D;
import edu.ncsu.jlboezem.common.properties.Positional;
import edu.ncsu.jlboezem.events.Event;
import edu.ncsu.jlboezem.events.EventManager;

public class ProcessorCollision extends Processor {

	@Override
	public void subscribe() {
		EventManager.INSTANCE.subscribe(this, ".*Collision.*");
	}

	@Override
	public boolean handle(Event e) {
		String guid = (String) e.getInfo().get("guid");
		String oGuid = (String) e.getInfo().get("oGuid");
		Vector2D newPos = (Vector2D) e.getInfo().get("NewPos");
		Vector2D oNewPos = (Vector2D) e.getInfo().get("oNewPos");
		if (null != newPos) {
			//Mobile.INSTANCE.setVelocityY(guid, 0);
			Positional.INSTANCE.setPosition(guid, newPos);
		}
		if (null != oNewPos) {
			//Mobile.INSTANCE.setVelocityY(oGuid, 0);
			Positional.INSTANCE.setPosition(oGuid, oNewPos);
		}
		return true;
	}

}

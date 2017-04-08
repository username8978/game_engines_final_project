package edu.ncsu.jlboezem.events.processors;

import edu.ncsu.jlboezem.communication.packets.PacketEvent;
import edu.ncsu.jlboezem.events.Event;
import edu.ncsu.jlboezem.events.EventManager;
import edu.ncsu.jlboezem.threading.ThreadManager;

public class ProcessorNetwork extends Processor {

	@Override
	public void subscribe() {
		if (!ThreadManager.INSTANCE.getLocalName().equals("Server")) {
			EventManager.INSTANCE.subscribe(this, ".*>.*");
			System.out.println("Registering as " + "client");
			return;
		}
		EventManager.INSTANCE.subscribe(this, ".*");
	}

	@Override
	public boolean handle(Event e) {
		if (e.getType().contains("!") || e.getType().contains("-")) {
			return true;
		}
		ThreadManager.INSTANCE.writeToQueueLike("Events.*", new PacketEvent(e));
		return true;
	}

}

package edu.ncsu.jlboezem.communication.packets;

import edu.ncsu.jlboezem.events.Event;

public class PacketEvent extends Packet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 278335858627217663L;
	private Event event;
	public PacketEvent(Event e) {
		event = e;
	}
	
	public Event getEvent() {
		return event;
	}
	@Override
	public Object[] getContents() {
		// TODO Auto-generated method stub
		return null;
	}

}

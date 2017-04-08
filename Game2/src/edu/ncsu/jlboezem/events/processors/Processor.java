package edu.ncsu.jlboezem.events.processors;

import edu.ncsu.jlboezem.events.Event;

/**
 * This class will be the parent for all Processors. Processors will handle Events by subscribing to the EventManager and doing whatever is needed when they receive them.
 * @author John
 *
 */
public abstract class Processor {
	public abstract void subscribe();
	public abstract boolean handle(Event e);
}

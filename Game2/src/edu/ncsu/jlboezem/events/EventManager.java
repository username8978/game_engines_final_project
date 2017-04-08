package edu.ncsu.jlboezem.events;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.Predicate;

import edu.ncsu.jlboezem.common.Ticking;
import edu.ncsu.jlboezem.communication.ConnectionManager;
import edu.ncsu.jlboezem.events.processors.Processor;

public class EventManager implements Ticking {
	public static final EventManager INSTANCE = new EventManager();
	private ConcurrentHashMap<String, LinkedBlockingQueue<Processor>> processors;
	private PriorityBlockingQueue<Event> events;
	
	private PriorityBlockingQueue<Event> currentQueue;
	
	public EventManager() {
		processors = new ConcurrentHashMap<String, LinkedBlockingQueue<Processor>>();
		events = new PriorityBlockingQueue<Event>();
		currentQueue = events;
	}
	
	public void addEvent(Event e) {
		if (ConnectionManager.INSTANCE.getShouldMakeEvents() && !ConnectionManager.INSTANCE.isShouldPauseMakingEvents()) {
			events.add(e);
		}
	}
	
	public void tick(long timestep) {
		while (true) {
			final Event event = currentQueue.peek();
			if (event == null) {
				if (events != currentQueue) {
					System.out.println(currentQueue.size());
					final Event stopEvent = new Event("!ReplayPlayStop", null, 0, null);
					Map<Processor, Boolean> handled = new HashMap<Processor, Boolean>();
					processors.entrySet().stream().filter(entry -> stopEvent.getType().matches(entry.getKey())).forEach(entry -> entry.getValue().forEach(p -> handled.put(p, handled.get(p) == null ? p.handle(stopEvent) : true)));
				} else {
					return;
				}
			} else if (event.getTimestamp() <= ConnectionManager.INSTANCE.getGameTimeline().getElapsedTime()) {
				currentQueue.poll();
				if (!event.getType().matches(".*(Collision|KeyPressed|KeyReleased|.*).*"))
					System.out.println(ConnectionManager.INSTANCE.getGameTimeline().getElapsedTime() + ": " + event.getType() + " at " + event.getTimestamp());
				Map<Processor, Boolean> handled = new HashMap<Processor, Boolean>();
				// Loop through all matching processors and handle event.
				processors.entrySet().stream().filter(entry -> event.getType().matches(entry.getKey())).forEach(entry -> entry.getValue().forEach(p -> handled.put(p, handled.get(p) == null ? p.handle(event) : true)));
			} else {
				//System.out.println("return2");
				return;
			}
		}
	}
	
	/**
	 * Subscribes a processor to an event type
	 * @param processor to subscribe
	 * @param eventType to subscribe to
	 * @return true if subscribed, false if failed.
	 */
	public boolean subscribe(Processor processor, String eventType) {
		LinkedBlockingQueue<Processor> list = processors.get(eventType);
		if (null == list) {
			list = new LinkedBlockingQueue<>();
			processors.put(eventType, list);
		}
		return list.add(processor);
	}

	public void removeEventsThatMatch(Predicate<Event> filter) {
		events.removeIf(event -> filter.test(event));
	}
	
	public void addSpecialEvent(Event e) {
		if (ConnectionManager.INSTANCE.getShouldMakeEvents()) {
			events.add(e);
		} else if (e.getType().matches(".*\\*.*")) {
			// Event outside of any queue, do now
			Map<Processor, Boolean> handled = new HashMap<Processor, Boolean>();
			// Loop through all matching processors and handle event.
			processors.entrySet().stream().filter(entry -> e.getType().matches(entry.getKey())).forEach(entry -> entry.getValue().forEach(p -> handled.put(p, handled.get(p) == null ? p.handle(e) : true)));
		} else if (e.getType().matches(".*!.*")) {
			// Network based event, add to normal queue
			events.add(e);
		} else if (e.getType().matches(".*>.*") && !ConnectionManager.INSTANCE.isShouldPauseMakingEvents()) {
			// Move Event, should be added unless explicitly paused
			events.add(e);
		}
	}
	
	public void setCurrentQueue(PriorityBlockingQueue<Event> queue) {
		if (queue == null)
			currentQueue = events;
		else
			currentQueue = queue;
	}
	
	
}

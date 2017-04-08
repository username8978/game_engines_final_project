package edu.ncsu.jlboezem.events.processors;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

import edu.ncsu.jlboezem.common.Timeline;
import edu.ncsu.jlboezem.common.properties.Damageable;
import edu.ncsu.jlboezem.common.properties.Damaging;
import edu.ncsu.jlboezem.common.properties.Drawable;
import edu.ncsu.jlboezem.common.properties.Mobile;
import edu.ncsu.jlboezem.common.properties.Pathed;
import edu.ncsu.jlboezem.common.properties.Player;
import edu.ncsu.jlboezem.common.properties.Positional;
import edu.ncsu.jlboezem.common.properties.Property;
import edu.ncsu.jlboezem.common.properties.Shaped;
import edu.ncsu.jlboezem.common.properties.Spawner;
import edu.ncsu.jlboezem.communication.ConnectionManager;
import edu.ncsu.jlboezem.communication.packets.PacketLevelData;
import edu.ncsu.jlboezem.communication.packets.PacketLevelRequest;
import edu.ncsu.jlboezem.events.Event;
import edu.ncsu.jlboezem.events.EventManager;
import edu.ncsu.jlboezem.threading.ThreadManager;

public class ProcessorReplays extends Processor {
	private PriorityBlockingQueue<Event> events;
	private boolean isRecording;
	private boolean isReplaying;
	private boolean wasMakingEvents;
	public ProcessorReplays() {
		events = new PriorityBlockingQueue<Event>();
		isRecording = false;
		isReplaying = false;
	}
	@Override
	public boolean handle(Event e) {
		if (e.getType().matches(".*Replay.*")) {
			if (e.getType().contains("ReplayPlayStop")) {
				System.out.println(e + " and is replaying  " + isReplaying);
			}
			if (e.getType().matches(".*ReplayRecordStart.*")) {
				isRecording = true;
				events.clear();
				saveLevel("ReplayStart.sav");
			} else if (isRecording && e.getType().matches(".*ReplayRecordStop.*")) {
				isRecording = false;
				isReplaying = true;
				saveLevel("ReplayEnd.sav");
				ConnectionManager.INSTANCE.setShouldPauseMakingEvents(true);
				System.out.println("Saved with " + ConnectionManager.INSTANCE.getGameTimeline().getElapsedTime() + " with realtime " + ConnectionManager.INSTANCE.getRealTimeline().getElapsedTime());
				wasMakingEvents = ConnectionManager.INSTANCE.getShouldMakeEvents();
				ConnectionManager.INSTANCE.setShouldMakeEvents(false);
				EventManager.INSTANCE.setCurrentQueue(events);
				loadLevel("ReplayStart.sav");
				System.out.println("Starting at " + ConnectionManager.INSTANCE.getGameTimeline().getElapsedTime() + " with realtime " + ConnectionManager.INSTANCE.getRealTimeline().getElapsedTime());
				events.forEach(event -> System.out.println("Replay" + event));
//				System.out.println(ConnectionManager.INSTANCE.getGameTimeline());
//				System.out.println(ConnectionManager.INSTANCE.getRealTimeline());
//				System.out.println(events.peek());
			} else if (isReplaying && e.getType().matches(".*ReplayPlayStop.*"))  {
				System.out.println("ReplayStop");
				if (ConnectionManager.INSTANCE.getType().equals("Client")) {
					loadLevel("ReplayEnd.sav");
					ConnectionManager.INSTANCE.setShouldPauseMakingEvents(false);
					ConnectionManager.INSTANCE.setShouldMakeEvents(wasMakingEvents);
					EventManager.INSTANCE.setCurrentQueue(null);
				} else if (ConnectionManager.INSTANCE.getType().equals("ClientOfServer")) {
					try {
						ConnectionManager.INSTANCE.setShouldPauseMakingEvents(false);
						ConnectionManager.INSTANCE.setShouldMakeEvents(wasMakingEvents);
						EventManager.INSTANCE.setCurrentQueue(null);
						ThreadManager.INSTANCE.writeToQueue("Send" + ConnectionManager.INSTANCE.getServer(), new PacketLevelRequest());
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				isReplaying = false;
			} else if (isReplaying && e.getType().matches(".*ReplayPlaySpeed.*")) {
				char digit = e.getType().charAt(e.getType().indexOf("ReplayPlaySpeed") + "ReplayPlaySpeed".length());
				switch (digit) {
				case '1':
					ConnectionManager.INSTANCE.getRealTimeline().setMultiplier((byte) (Timeline.DIVISION_MASK | 2));
					break;
				case '2':
					ConnectionManager.INSTANCE.getRealTimeline().setMultiplier((byte) (1));
					break;
				case '3':
					ConnectionManager.INSTANCE.getRealTimeline().setMultiplier((byte) (2));
					break;
				case '>':
					ConnectionManager.INSTANCE.getRealTimeline().setMultiplier((byte) (100));
					break;
				case '<':
					ConnectionManager.INSTANCE.getRealTimeline().setMultiplier((byte) (0x8A));
					break;
				}
			}
		} else if (isRecording && !e.getType().contains("-")) {
			events.add(new Event("!" + e.getType() + "-", e.getInfo(), e.getTimestamp(), e.getFrom()));
			//System.out.println(events.size());
		}
		return true;
	}
	private void saveLevel(String file) {
		ConcurrentHashMap<String, Property> properties = new ConcurrentHashMap<String, Property>();
		properties.put(Damageable.class.getCanonicalName(), Damageable.INSTANCE);
		properties.put(Damaging.class.getCanonicalName(), Damaging.INSTANCE);
		properties.put(Drawable.class.getCanonicalName(), Drawable.INSTANCE);
		properties.put(Mobile.class.getCanonicalName(), Mobile.INSTANCE);
		properties.put(Pathed.class.getCanonicalName(), Pathed.INSTANCE);
		properties.put(Positional.class.getCanonicalName(), Positional.INSTANCE);
		properties.put(Shaped.class.getCanonicalName(), Shaped.INSTANCE);
		properties.put(Spawner.class.getCanonicalName(), Spawner.INSTANCE);
		properties.put(Player.class.getCanonicalName(), Player.INSTANCE);
		ObjectOutputStream saveStateFileOut = null;
		try {
			new File(file).delete();
			FileOutputStream saveStateFile = new FileOutputStream(new File(file));
			saveStateFileOut = new ObjectOutputStream(saveStateFile);
			saveStateFileOut.writeObject(new PacketLevelData(properties, ConnectionManager.INSTANCE.getGameTimeline(), ConnectionManager.INSTANCE.getRealTimeline()));
			saveStateFileOut.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {
		}
	}
	void loadLevel(String file) {
		ObjectInputStream saveStateFileIn = null;
		try {
			FileInputStream saveStateFile = new FileInputStream(new File(file));
			saveStateFileIn = new ObjectInputStream(saveStateFile);
			PacketLevelData level = (PacketLevelData) saveStateFileIn.readObject();
			Damageable.INSTANCE.replaceWith((Damageable) level.getProperties().get(Damageable.class.getCanonicalName()));
			Damaging.INSTANCE.replaceWith((Damaging) level.getProperties().get(Damaging.class.getCanonicalName()));
			Drawable.INSTANCE.replaceWith((Drawable) level.getProperties().get(Drawable.class.getCanonicalName()));
			Mobile.INSTANCE.replaceWith((Mobile) level.getProperties().get(Mobile.class.getCanonicalName()));
			Pathed.INSTANCE.replaceWith((Pathed) level.getProperties().get(Pathed.class.getCanonicalName()));
			Positional.INSTANCE.replaceWith((Positional) level.getProperties().get(Positional.class.getCanonicalName()));
			Shaped.INSTANCE.replaceWith((Shaped) level.getProperties().get(Shaped.class.getCanonicalName()));
			Spawner.INSTANCE.replaceWith((Spawner) level.getProperties().get(Spawner.class.getCanonicalName()));
			Player.INSTANCE.replaceWith((Player) level.getProperties().get(Player.class.getCanonicalName()));
			ConnectionManager.INSTANCE.setGameTimeline(level.getGameTimeline());
			ConnectionManager.INSTANCE.setRealTimeline(level.getServerTime());
			saveStateFileIn.close();
			System.out.println("loaded Level");
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public void subscribe() {
		EventManager.INSTANCE.subscribe(this, ".*[^\\*].*[^-]");
	}
	
}

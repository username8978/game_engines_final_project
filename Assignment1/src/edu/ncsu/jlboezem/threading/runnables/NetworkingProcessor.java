package edu.ncsu.jlboezem.threading.runnables;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import edu.ncsu.jlboezem.common.properties.Damageable;
import edu.ncsu.jlboezem.common.properties.Damaging;
import edu.ncsu.jlboezem.common.properties.Drawable;
import edu.ncsu.jlboezem.common.properties.Mobile;
import edu.ncsu.jlboezem.common.properties.Pathed;
import edu.ncsu.jlboezem.common.properties.Player;
import edu.ncsu.jlboezem.common.properties.Positional;
import edu.ncsu.jlboezem.common.properties.Property;
import edu.ncsu.jlboezem.common.properties.Scripted;
import edu.ncsu.jlboezem.common.properties.Shaped;
import edu.ncsu.jlboezem.common.properties.Spawner;
import edu.ncsu.jlboezem.communication.ConnectionManager;
import edu.ncsu.jlboezem.communication.packets.Packet;
import edu.ncsu.jlboezem.communication.packets.PacketAuth;
import edu.ncsu.jlboezem.communication.packets.PacketEvent;
import edu.ncsu.jlboezem.communication.packets.PacketLevelData;
import edu.ncsu.jlboezem.communication.packets.PacketLevelRequest;
import edu.ncsu.jlboezem.communication.packets.PacketLevelUpdate;
import edu.ncsu.jlboezem.communication.packets.PacketLevelUpdateRequest;
import edu.ncsu.jlboezem.communication.packets.PacketStopThread;
import edu.ncsu.jlboezem.events.Event;
import edu.ncsu.jlboezem.events.EventManager;
import edu.ncsu.jlboezem.server.Server;
import edu.ncsu.jlboezem.threading.ThreadManager;

public class NetworkingProcessor implements Runnable {

	@Override
	public void run() {
		try {
			while (!ThreadManager.INSTANCE.isStopping()) {
				Packet input = ThreadManager.INSTANCE.getInput(this.getClass());
				if (input instanceof PacketStopThread)
					break;
				
				
				try {
					input = ThreadManager.INSTANCE.waitForQueueData("Receive");
				} catch(NullPointerException e) {
					continue;
				}
				if (input instanceof PacketAuth) {
					System.out.println(input.getFrom() + " connects as " + input.getContents()[0]);
					Server.INSTANCE.clientToUsername.put(input.getFrom(), (String) input.getContents()[0]);
				} else if (input instanceof PacketLevelRequest && ThreadManager.INSTANCE.getLocalName().equals("Server")) {
					ConcurrentHashMap<String, Property> properties = new ConcurrentHashMap<String, Property>();
					//TODO: make extensible
					boolean newPlayer = null == Positional.INSTANCE.getPosition("Player" + Server.INSTANCE.clientToUsername.get(input.from));
					if (newPlayer) {
						//Spawner.INSTANCE.spawn("PlayerSpawner", "Player" + Server.INSTANCE.clientToUsername.get(input.from));
						Map<String, Object> infoSpawn = new ConcurrentHashMap<String, Object>();
						infoSpawn.put("guid", "Player" + Server.INSTANCE.clientToUsername.get(input.from));
						EventManager.INSTANCE.addEvent(new Event("PlayerSpawn", infoSpawn, ConnectionManager.INSTANCE.getGameTimeline().getElapsedTime() + 1000, ThreadManager.INSTANCE.getLocalName()));
					}
					properties.put(Damageable.class.getCanonicalName(), Damageable.INSTANCE);
					properties.put(Damaging.class.getCanonicalName(), Damaging.INSTANCE);
					properties.put(Drawable.class.getCanonicalName(), Drawable.INSTANCE);
					properties.put(Mobile.class.getCanonicalName(), Mobile.INSTANCE);
					properties.put(Pathed.class.getCanonicalName(), Pathed.INSTANCE);
					properties.put(Positional.class.getCanonicalName(), Positional.INSTANCE);
					properties.put(Shaped.class.getCanonicalName(), Shaped.INSTANCE);
					properties.put(Spawner.class.getCanonicalName(), Spawner.INSTANCE);
					properties.put(Scripted.class.getCanonicalName(), Scripted.INSTANCE);
					properties.put(Player.class.getCanonicalName(), Player.INSTANCE);
					PacketLevelData packet = new PacketLevelData(properties, ConnectionManager.INSTANCE.getGameTimeline(), ConnectionManager.INSTANCE.getRealTimeline());
					ThreadManager.INSTANCE.writeToQueueLike(newPlayer ? "Send.*" : "Send" + input.from, packet);
					System.out.println("Updating Clients with level.");
				} else if (input instanceof PacketLevelData) {
					PacketLevelData level = (PacketLevelData) input;
					System.out.println("Updating with level");
					Damageable.INSTANCE.replaceWith((Damageable) level.getProperties().get(Damageable.class.getCanonicalName()));
					Damaging.INSTANCE.replaceWith((Damaging) level.getProperties().get(Damaging.class.getCanonicalName()));
					Drawable.INSTANCE.replaceWith((Drawable) level.getProperties().get(Drawable.class.getCanonicalName()));
					Mobile.INSTANCE.replaceWith((Mobile) level.getProperties().get(Mobile.class.getCanonicalName()));
					Pathed.INSTANCE.replaceWith((Pathed) level.getProperties().get(Pathed.class.getCanonicalName()));
					Positional.INSTANCE.replaceWith((Positional) level.getProperties().get(Positional.class.getCanonicalName()));
					Shaped.INSTANCE.replaceWith((Shaped) level.getProperties().get(Shaped.class.getCanonicalName()));
					Spawner.INSTANCE.replaceWith((Spawner) level.getProperties().get(Spawner.class.getCanonicalName()));
					Scripted.INSTANCE.replaceWith((Scripted) level.getProperties().get(Scripted.class.getCanonicalName()));
					Player.INSTANCE.replaceWith((Player) level.getProperties().get(Player.class.getCanonicalName()));
					ConnectionManager.INSTANCE.setGameTimeline(level.getGameTimeline());
					ConnectionManager.INSTANCE.setRealTimeline(level.getServerTime());
				} else if (input instanceof PacketLevelUpdateRequest) {
					ConcurrentHashMap<String, Property> properties = new ConcurrentHashMap<String, Property>();
					properties.put(Mobile.class.getCanonicalName(), Mobile.INSTANCE);
					properties.put(Positional.class.getCanonicalName(), Positional.INSTANCE);
				} else if (input instanceof PacketLevelUpdate) {
					PacketLevelData level = (PacketLevelData) input;
					Mobile.INSTANCE.replaceWith((Mobile) level.getProperties().get(Mobile.class.getCanonicalName()));
					Positional.INSTANCE.replaceWith((Positional) level.getProperties().get(Positional.class.getCanonicalName()));
				} else if (input instanceof PacketEvent) {
					if (!ThreadManager.INSTANCE.getLocalName().equals(((PacketEvent) input).getEvent().getFrom())) {
						//System.out.println(((PacketEvent) input).getEvent().getFrom() + " " + ((PacketEvent) input).getEvent().getType());
						Event event = ((PacketEvent) input).getEvent();
						event.setType("!" + event.getType());
						EventManager.INSTANCE.addSpecialEvent(event); //TODO: not replicate events from client to themselves.
						if (ThreadManager.INSTANCE.getLocalName().equals("Server")) {
//							ConcurrentHashMap<String, Property> properties = new ConcurrentHashMap<String, Property>();
//							properties.put(Damageable.class.getCanonicalName(), Damageable.INSTANCE);
//							properties.put(Damaging.class.getCanonicalName(), Damaging.INSTANCE);
//							properties.put(Drawable.class.getCanonicalName(), Drawable.INSTANCE);
//							properties.put(Forceable.class.getCanonicalName(), Forceable.INSTANCE);
//							properties.put(Positional.class.getCanonicalName(), Positional.INSTANCE);
//							properties.put(Shaped.class.getCanonicalName(), Shaped.INSTANCE);
//							properties.put(Spawner.class.getCanonicalName(), Spawner.INSTANCE);
//							PacketLevelData packet = new PacketLevelData(properties);
//							ThreadManager.INSTANCE.writeToQueueLike("Send.*", packet);
						}
					}
				} else {
					System.out.println("?????");
				}
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

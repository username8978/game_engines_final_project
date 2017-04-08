package edu.ncsu.jlboezem.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import processing.core.PApplet;
import edu.ncsu.jlboezem.common.GameObjectCreator;
import edu.ncsu.jlboezem.common.Vector2D;
import edu.ncsu.jlboezem.common.properties.Damaging;
import edu.ncsu.jlboezem.common.properties.Drawable;
import edu.ncsu.jlboezem.common.properties.Mobile;
import edu.ncsu.jlboezem.common.properties.Pathed;
import edu.ncsu.jlboezem.common.properties.Scripted;
import edu.ncsu.jlboezem.common.properties.Spawner;
import edu.ncsu.jlboezem.communication.ConnectionManager;
import edu.ncsu.jlboezem.events.Event;
import edu.ncsu.jlboezem.events.EventManager;
import edu.ncsu.jlboezem.events.processors.ProcessorCollision;
import edu.ncsu.jlboezem.events.processors.ProcessorDamage;
import edu.ncsu.jlboezem.events.processors.ProcessorKeyPress;
import edu.ncsu.jlboezem.events.processors.ProcessorNetwork;
import edu.ncsu.jlboezem.events.processors.ProcessorPlayer;
import edu.ncsu.jlboezem.events.processors.ProcessorReplays;
import edu.ncsu.jlboezem.threading.ThreadManager;
import edu.ncsu.jlboezem.threading.ThreadManager.ThreadType;

public class Server extends PApplet {
	private static final long serialVersionUID = -7936884546003750125L;

	public static Server INSTANCE;

	public static final int SCREEN_WIDTH = 1000;
	public static final int SCREEN_HEIGHT = 1000;
	public static final int PORT = 9999;
	private final Random myRandom = new Random();
	
	public ConcurrentHashMap<String, String> clientToUsername;

	private HashMap<String, Character> keybinds;

	public void setup() {
		INSTANCE = this;
		clientToUsername = new ConcurrentHashMap<String, String>();
		size(SCREEN_WIDTH, SCREEN_HEIGHT);
		ConnectionManager.INSTANCE.setType("Server");
		Mobile.INSTANCE.maxVelocity = new Vector2D(75, 75);
		keybinds = new HashMap<String, Character>();
		keybinds.put("PlayerLeftMove", 'a');
		keybinds.put("PlayerRightMove", 'd');
		keybinds.put("PlayerUpMove", 'w');
		keybinds.put("PlayerDownMove", 's');
		keybinds.put("PlayerBoostMove", 'x');
		GameObjectCreator.addPlatform("GroundPlatform", new Vector2D(20, SCREEN_HEIGHT - 20), SCREEN_WIDTH-40, 20, 0xE1E1E1, 50);
		Drawable.INSTANCE.removeObject("GroundPlatform");
		Damaging.INSTANCE.addObject("GroundPlatform", 100);
		GameObjectCreator.addPlatform("CeilingPlatform", new Vector2D(20, 0), SCREEN_WIDTH - 40, 20, 0xE1E1E1, 50);
		Drawable.INSTANCE.removeObject("CeilingPlatform");
		Damaging.INSTANCE.addObject("CeilingPlatform", 100);
		GameObjectCreator.addPlatform("LeftPlatform", new Vector2D(0, 20), 20, SCREEN_HEIGHT - 40, 0xE1E1E1, 50);
		Drawable.INSTANCE.removeObject("LeftPlatform");
		Damaging.INSTANCE.addObject("LeftPlatform", 100);
		//GameObjectCreator.addPlatform("RightPlatform", new Vector2D( SCREEN_WIDTH - 20, 20), 20, SCREEN_HEIGHT - 40, 0xE1E1E1, 50);
		GameObjectCreator.addPlatform("DeathZone", new Vector2D( SCREEN_WIDTH - 20, 20), 20, SCREEN_HEIGHT - 40, 0xE1E1E1, 50);
		Drawable.INSTANCE.removeObject("DeathZone");
		Damaging.INSTANCE.addObject("DeathZone", 100);
		final int WALL_OFFSET = 0;
		final int SPAWN_SIDE = 20;
		final int SHOT_WIDTH = 20;
		final int SHOT_HEIGHT = 20;
		final int SPAWN_X_OFFSET = 5 + SHOT_WIDTH;
		final int SPAWN_Y_OFFSET = 5 + SHOT_HEIGHT;
		GameObjectCreator.addPlatform("MovingSpawn1", new Vector2D(SCREEN_WIDTH - SPAWN_SIDE, SCREEN_HEIGHT - SPAWN_SIDE), SPAWN_SIDE, SPAWN_SIDE, 0xAAAAAA, 100);
		Pathed.INSTANCE.addObject("MovingSpawn1", new Vector2D(SCREEN_WIDTH - SPAWN_SIDE,  SCREEN_HEIGHT - SPAWN_SIDE), new Vector2D(SCREEN_WIDTH - SPAWN_SIDE, WALL_OFFSET), true);
		GameObjectCreator.addPlatform("MovingSpawn2", new Vector2D(WALL_OFFSET, SCREEN_HEIGHT - SPAWN_SIDE), SPAWN_SIDE, SPAWN_SIDE, 0xAAAAAA, 100);
		Pathed.INSTANCE.addObject("MovingSpawn2", new Vector2D(WALL_OFFSET,  SCREEN_HEIGHT - SPAWN_SIDE), new Vector2D(SCREEN_WIDTH - SPAWN_SIDE, SCREEN_HEIGHT - SPAWN_SIDE), true);
		GameObjectCreator.addPlatform("MovingSpawn3", new Vector2D(SCREEN_WIDTH - SPAWN_SIDE, WALL_OFFSET), SPAWN_SIDE, SPAWN_SIDE, 0xAAAAAA, 100);
		Pathed.INSTANCE.addObject("MovingSpawn3", new Vector2D(SCREEN_WIDTH - SPAWN_SIDE,  WALL_OFFSET), new Vector2D(WALL_OFFSET, WALL_OFFSET), true);
		GameObjectCreator.addPlatform("MovingSpawn4", new Vector2D(WALL_OFFSET, WALL_OFFSET), SPAWN_SIDE, SPAWN_SIDE, 0xAAAAAA, 100);
		Pathed.INSTANCE.addObject("MovingSpawn4", new Vector2D(WALL_OFFSET, WALL_OFFSET), new Vector2D(WALL_OFFSET, SCREEN_HEIGHT - SPAWN_SIDE), true);
		GameObjectCreator.addPlatform("MovingSpawn5", new Vector2D(WALL_OFFSET, SCREEN_HEIGHT - SPAWN_SIDE), SPAWN_SIDE, SPAWN_SIDE, 0xAAAAAA, 100);
		Pathed.INSTANCE.addObject("MovingSpawn5", new Vector2D(WALL_OFFSET, SCREEN_HEIGHT - SPAWN_SIDE), new Vector2D(WALL_OFFSET, WALL_OFFSET), true);
		GameObjectCreator.addPlatform("MovingSpawn6", new Vector2D(WALL_OFFSET, WALL_OFFSET), SPAWN_SIDE, SPAWN_SIDE, 0xAAAAAA, 100);
		Pathed.INSTANCE.addObject("MovingSpawn6", new Vector2D(WALL_OFFSET, WALL_OFFSET), new Vector2D(SCREEN_WIDTH - SPAWN_SIDE,  WALL_OFFSET), true);
		GameObjectCreator.addPlatform("MovingSpawn7", new Vector2D(SCREEN_WIDTH - SPAWN_SIDE,  SCREEN_HEIGHT - SPAWN_SIDE), SPAWN_SIDE, SPAWN_SIDE, 0xAAAAAA, 100);
		Pathed.INSTANCE.addObject("MovingSpawn7", new Vector2D(SCREEN_WIDTH - SPAWN_SIDE,  SCREEN_HEIGHT - SPAWN_SIDE), new Vector2D(WALL_OFFSET, SCREEN_HEIGHT - SPAWN_SIDE), true);
		GameObjectCreator.addPlatform("MovingSpawn8", new Vector2D(SCREEN_WIDTH - SPAWN_SIDE, WALL_OFFSET), SPAWN_SIDE, SPAWN_SIDE, 0xAAAAAA, 100);
		Pathed.INSTANCE.addObject("MovingSpawn8", new Vector2D(SCREEN_WIDTH - SPAWN_SIDE, WALL_OFFSET), new Vector2D(SCREEN_WIDTH - SPAWN_SIDE,  SCREEN_HEIGHT - SPAWN_SIDE), true);

		//Add Moving Spawns to Spawners:
		Spawner.INSTANCE.addObject("MovingSpawn1", new Object[] { "Shot", new Vector2D(-SPAWN_X_OFFSET, 0), SHOT_WIDTH, SHOT_HEIGHT, 0xFFAAAA, 100 });
		Spawner.INSTANCE.addObject("MovingSpawn2", new Object[] { "Shot", new Vector2D(0, -SPAWN_Y_OFFSET), SHOT_WIDTH, SHOT_HEIGHT, 0xFFAAAA, 100 });
		Spawner.INSTANCE.addObject("MovingSpawn3", new Object[] { "Shot", new Vector2D(0, SPAWN_Y_OFFSET), SHOT_WIDTH, SHOT_HEIGHT, 0xFFAAAA, 100 });
		Spawner.INSTANCE.addObject("MovingSpawn4", new Object[] { "Shot", new Vector2D(SPAWN_X_OFFSET, 0), SHOT_WIDTH, SHOT_HEIGHT, 0xFFAAAA, 100 });
		Spawner.INSTANCE.addObject("MovingSpawn5", new Object[] { "Shot", new Vector2D(SPAWN_X_OFFSET, 0), SHOT_WIDTH, SHOT_HEIGHT, 0xFFAAAA, 100 });
		Spawner.INSTANCE.addObject("MovingSpawn6", new Object[] { "Shot", new Vector2D(0, SPAWN_Y_OFFSET), SHOT_WIDTH, SHOT_HEIGHT, 0xFFAAAA, 100 });
		Spawner.INSTANCE.addObject("MovingSpawn7", new Object[] { "Shot", new Vector2D(0, -SPAWN_Y_OFFSET), SHOT_WIDTH, SHOT_HEIGHT, 0xFFAAAA, 100 });
		Spawner.INSTANCE.addObject("MovingSpawn8", new Object[] { "Shot", new Vector2D(-SPAWN_X_OFFSET, 0), SHOT_WIDTH, SHOT_HEIGHT, 0xFFAAAA, 100 });
		Spawner.INSTANCE.addObject("PlayerSpawner", new Object[] { "Player", new Vector2D(SCREEN_WIDTH / 2f, SCREEN_HEIGHT / 2f), 10, 10, 0xAA00DD, 100 });
		ConnectionManager.INSTANCE.setShouldMakeEvents(true);
		Scripted.INSTANCE.addObject("boost", "scripts/boost.js");
		try {
			ServerSocket serverSock = new ServerSocket(Server.PORT);
			ThreadManager.INSTANCE.setServerSocket(serverSock);
			ThreadManager.INSTANCE.setLocalName("Server");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ThreadManager.INSTANCE.addThread(ThreadType.NetworkingListen);
		ThreadManager.INSTANCE.addThread(ThreadType.NetworkingProcessor);
		ProcessorDamage damage = new ProcessorDamage();
		damage.subscribe();
		ProcessorPlayer player = new ProcessorPlayer();
		player.subscribe();
		ProcessorNetwork network = new ProcessorNetwork();
		network.subscribe();
		ProcessorCollision collision = new ProcessorCollision();
		collision.subscribe();

		Drawable.INSTANCE.setup(this);
		lastTick = System.currentTimeMillis();
	}
	public static void main(String[] args) {
		PApplet.main(new String[] { "--present", "edu.ncsu.jlboezem.server.Server" });
	}
	long lastTick = 0;
	@Override
	public void draw() {
		long currentTime = System.currentTimeMillis();
		long minTickSize = 17;
		long myTime;
		myTime = ConnectionManager.INSTANCE.getGameTimeline().getElapsedTime();
		while (myTime < ConnectionManager.INSTANCE.getRealTimeline().getElapsedTime()) {
			Mobile.INSTANCE.tick(minTickSize);
			Pathed.INSTANCE.tick(minTickSize);
			EventManager.INSTANCE.tick(minTickSize);
			if (ConnectionManager.INSTANCE.getGameTimeline().getElapsedTime() % (20 * minTickSize) == 0) {
				switch (myRandom.nextInt(5)) {
				case 0:
			//		Spawner.INSTANCE.spawn("MovingSpawn1", "TestShot" + currentTime + "a");
					Map<String, Object> infoSpawn = new ConcurrentHashMap<String, Object>();
					Map<String, Object> infoSpawn2 = new ConcurrentHashMap<String, Object>();
					Map<String, Object> infoSpawn3 = new ConcurrentHashMap<String, Object>();
					Map<String, Object> infoSpawn4 = new ConcurrentHashMap<String, Object>();
					infoSpawn.put("guid", "Shot" + currentTime + "a");
					infoSpawn.put("Spawn", "MovingSpawn1");
					EventManager.INSTANCE.addEvent(new Event("Spawn", infoSpawn, ConnectionManager.INSTANCE.getGameTimeline().getElapsedTime(), ThreadManager.INSTANCE.getLocalName()));
					infoSpawn2.put("guid", "Shot" + currentTime + "b");
					infoSpawn2.put("Spawn", "MovingSpawn2");
					EventManager.INSTANCE.addEvent(new Event("Spawn", infoSpawn2, ConnectionManager.INSTANCE.getGameTimeline().getElapsedTime(), ThreadManager.INSTANCE.getLocalName()));
					infoSpawn3.put("guid", "Shot" + currentTime + "c");
					infoSpawn3.put("Spawn", "MovingSpawn3");
					EventManager.INSTANCE.addEvent(new Event("Spawn", infoSpawn3, ConnectionManager.INSTANCE.getGameTimeline().getElapsedTime(), ThreadManager.INSTANCE.getLocalName()));
					infoSpawn4.put("guid", "Shot" + currentTime + "d");
					infoSpawn4.put("Spawn", "MovingSpawn4");
					EventManager.INSTANCE.addEvent(new Event("Spawn", infoSpawn4, ConnectionManager.INSTANCE.getGameTimeline().getElapsedTime(), ThreadManager.INSTANCE.getLocalName()));
					break;
				case 1:
					Map<String, Object> infoSpawn5 = new ConcurrentHashMap<String, Object>();
					Map<String, Object> infoSpawn6 = new ConcurrentHashMap<String, Object>();
					Map<String, Object> infoSpawn7 = new ConcurrentHashMap<String, Object>();
					Map<String, Object> infoSpawn8 = new ConcurrentHashMap<String, Object>();
					infoSpawn5.put("guid", "Shot" + currentTime + "e");
					infoSpawn5.put("Spawn", "MovingSpawn5");
					EventManager.INSTANCE.addEvent(new Event("Spawn", infoSpawn5, ConnectionManager.INSTANCE.getGameTimeline().getElapsedTime(), ThreadManager.INSTANCE.getLocalName()));
					infoSpawn6.put("guid", "Shot" + currentTime + "f");
					infoSpawn6.put("Spawn", "MovingSpawn6");
					EventManager.INSTANCE.addEvent(new Event("Spawn", infoSpawn6, ConnectionManager.INSTANCE.getGameTimeline().getElapsedTime(), ThreadManager.INSTANCE.getLocalName()));
					infoSpawn7.put("guid", "Shot" + currentTime + "g");
					infoSpawn7.put("Spawn", "MovingSpawn7");
					EventManager.INSTANCE.addEvent(new Event("Spawn", infoSpawn7, ConnectionManager.INSTANCE.getGameTimeline().getElapsedTime(), ThreadManager.INSTANCE.getLocalName()));
					infoSpawn8.put("guid", "Shot" + currentTime + "h");
					infoSpawn8.put("Spawn", "MovingSpawn8");
					EventManager.INSTANCE.addEvent(new Event("Spawn", infoSpawn8, ConnectionManager.INSTANCE.getGameTimeline().getElapsedTime(), ThreadManager.INSTANCE.getLocalName()));
				}
			}
			ConnectionManager.INSTANCE.getGameTimeline().tick();
			myTime = ConnectionManager.INSTANCE.getGameTimeline().getElapsedTime();
		}
		long timestep = (currentTime - lastTick);
		Drawable.INSTANCE.draw();
		ConnectionManager.INSTANCE.getLoopTimeline().tick();
		ConnectionManager.INSTANCE.getRealTimeline().tick(timestep);
		lastTick = currentTime;
	}
	
	@Override
	public void keyPressed() {
		keybinds.forEach((bind, input) -> {
			if (input == key) {
				Map<String, Object> info = new HashMap<String, Object>();
				info.put("Action", bind);
				info.put("guid", "Player" + "Server");
				EventManager.INSTANCE.addSpecialEvent(new Event("!*KeyPressed", info, ConnectionManager.INSTANCE.getGameTimeline().getElapsedTime(), ThreadManager.INSTANCE.getLocalName()));
			}
		}); /*
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
	}
	@Override
	public void keyReleased() {
		keybinds.forEach((bind, input) -> {
			if (input == key) {
				Map<String, Object> info = new HashMap<String, Object>();
				info.put("Action", bind);
				info.put("guid", "Player" + "Server");
				EventManager.INSTANCE.addSpecialEvent(new Event("!*KeyReleased", info, ConnectionManager.INSTANCE.getGameTimeline().getElapsedTime(), ThreadManager.INSTANCE.getLocalName()));
			}
		});
		/*if (keybinds.get("LeftMove") == key) {
			Map<String, Object> info = new HashMap<String, Object>();
			info.put("Direction", "Left");
			info.put("guid", "Player" + clientName);
			EventManager.INSTANCE.addSpecialEvent(new Event("PlayerStopMove", info, ConnectionManager.INSTANCE.getGameTimeline().getTime(), ThreadManager.INSTANCE.getLocalName()));
		} else if (keybinds.get("RightMove") == key) {
			Map<String, Object> info = new HashMap<String, Object>();
			info.put("Direction", "Right");
			info.put("guid", "Player" + clientName);
			EventManager.INSTANCE.addSpecialEvent(new Event("PlayerStopMove", info, ConnectionManager.INSTANCE.getGameTimeline().getTime(), ThreadManager.INSTANCE.getLocalName()));
		} else if (keybinds.get("UpMove") == key) {
			//Forceable.INSTANCE.removeForce("Player", "Gravity");
		} else if (keybinds.get("DownMove") == key) {
			//Forceable.INSTANCE.removeForce("Player", "Gravity");
		}*/
		super.keyReleased();
	}

}

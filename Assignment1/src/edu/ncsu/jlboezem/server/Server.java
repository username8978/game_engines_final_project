package edu.ncsu.jlboezem.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;
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
	
	public ConcurrentHashMap<String, String> clientToUsername;

	private HashMap<String, Character> keybinds;

	public void setup() {
		INSTANCE = this;
		clientToUsername = new ConcurrentHashMap<String, String>();
		size(SCREEN_WIDTH, SCREEN_HEIGHT);
		ConnectionManager.INSTANCE.setType("Server");
		Mobile.INSTANCE.maxVelocity = new Vector2D(75, 75);
		keybinds = new HashMap<String, Character>();
		keybinds.put("LeftMove", 'a');
		keybinds.put("RightMove", 'd');
		keybinds.put("UpMove", 'w');
		keybinds.put("DownMove", 's');
		GameObjectCreator.addPlatform("Platform1", new Vector2D(500, 930), 10, 50, 0xE1E1E1, 100);
		GameObjectCreator.addPlatform("GroundPlatform", new Vector2D(20, SCREEN_HEIGHT - 20), SCREEN_WIDTH-40, 20, 0xE1E1E1, 50);
		Damaging.INSTANCE.addObject("GroundPlatform", 100);
		GameObjectCreator.addPlatform("CeilingPlatform", new Vector2D(20, 0), SCREEN_WIDTH - 40, 20, 0xE1E1E1, 50);
		GameObjectCreator.addPlatform("LeftPlatform", new Vector2D(0, 20), 20, SCREEN_HEIGHT - 40, 0xE1E1E1, 50);
		GameObjectCreator.addPlatform("DeathZone", new Vector2D( SCREEN_WIDTH - 20, 20), 20, SCREEN_HEIGHT - 40, 0xE1E1E1, 50);
		Drawable.INSTANCE.removeObject("DeathZone");
		Damaging.INSTANCE.addObject("DeathZone", 100);
		GameObjectCreator.addPlatform("MovingPlatform", new Vector2D(SCREEN_WIDTH/ 4, SCREEN_HEIGHT - 50), 20, 20, 0xAAAAAA, 100);
		Pathed.INSTANCE.addObject("MovingPlatform", new Vector2D(SCREEN_WIDTH/4,  SCREEN_HEIGHT - 40), new Vector2D(3 * SCREEN_WIDTH/4, 3*SCREEN_HEIGHT/4), true);
		Scripted.INSTANCE.addObject("derp", "scripts/derp.js");
		Scripted.INSTANCE.addObject("deathEvent", "scripts/eventScript.js");
		Spawner.INSTANCE.addObject("PlayerSpawner", new Object[] { "", new Vector2D(SCREEN_WIDTH / 2f, SCREEN_HEIGHT / 2f), 20, 50, 0xAA00DD, 100 });
		ConnectionManager.INSTANCE.setShouldMakeEvents(true);
		Map<String, Object> infoSpawn = new ConcurrentHashMap<String, Object>();
		infoSpawn.put("guid", "Player" + "Server");
		EventManager.INSTANCE.addEvent(new Event("PlayerSpawn", infoSpawn, ConnectionManager.INSTANCE.getGameTimeline().getElapsedTime() + 1000, ThreadManager.INSTANCE.getLocalName()));
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
			ConnectionManager.INSTANCE.getGameTimeline().tick();
			myTime = ConnectionManager.INSTANCE.getGameTimeline().getElapsedTime();
		}
		long timestep = (currentTime - lastTick);
		Drawable.INSTANCE.draw();
		ConnectionManager.INSTANCE.getLoopTimeline().tick();
		ConnectionManager.INSTANCE.getRealTimeline().tick(timestep);
		lastTick = currentTime;
	}
	public void keyPressed() {
		if (keybinds.get("LeftMove") == key) {
			Map<String, Object> info = new HashMap<String, Object>();
			info.put("Direction", "Left");
			info.put("guid", "Player" + "Server");
			EventManager.INSTANCE.addEvent(new Event("PlayerMove", info, ConnectionManager.INSTANCE.getGameTimeline().getElapsedTime(), ThreadManager.INSTANCE.getLocalName()));
		} else if (keybinds.get("RightMove") == key) {
			Map<String, Object> info = new HashMap<String, Object>();
			info.put("Direction", "Right");
			info.put("guid", "Player" + "Server");
			EventManager.INSTANCE.addEvent(new Event("PlayerMove", info, ConnectionManager.INSTANCE.getGameTimeline().getElapsedTime(), ThreadManager.INSTANCE.getLocalName()));
		/*} else if (keybinds.get("UpMove") == key && gameTimeline.getTime() - lastUp > upTimeDelay) {
			lastUp = gameTimeline.getTime();
			Map<String, Object> info = new HashMap<String, Object>();
			info.put("Direction", "Up");
			info.put("guid", "Player" + "Server");
			EventManager.INSTANCE.addEvent(new Event("PlayerMove", info, ConnectionManager.INSTANCE.getGameTimeline().getTime(), ThreadManager.INSTANCE.getLocalName()));
		*/} else if (keybinds.get("DownMove") == key) {
			
		}
	}
	
	@Override
	public void keyReleased() {
		if (keybinds.get("LeftMove") == key) {
			Map<String, Object> info = new HashMap<String, Object>();
			info.put("Direction", "Left");
			info.put("guid", "Player" + "Server");
			EventManager.INSTANCE.addEvent(new Event("PlayerStopMove", info, ConnectionManager.INSTANCE.getGameTimeline().getElapsedTime(), ThreadManager.INSTANCE.getLocalName()));
		} else if (keybinds.get("RightMove") == key) {
			Map<String, Object> info = new HashMap<String, Object>();
			info.put("Direction", "Right");
			info.put("guid", "Player" + "Server");
			EventManager.INSTANCE.addEvent(new Event("PlayerStopMove", info, ConnectionManager.INSTANCE.getGameTimeline().getElapsedTime(), ThreadManager.INSTANCE.getLocalName()));
		} else if (keybinds.get("UpMove") == key) {
			//Forceable.INSTANCE.removeForce("Player", "Gravity");
		} else if (keybinds.get("DownMove") == key) {
			//Forceable.INSTANCE.removeForce("Player", "Gravity");
		}
		super.keyReleased();
	}
}

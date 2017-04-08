package edu.ncsu.jlboezem.client;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import edu.ncsu.jlboezem.common.GameObjectCreator;
import edu.ncsu.jlboezem.common.Vector2D;
import edu.ncsu.jlboezem.common.properties.Damaging;
import edu.ncsu.jlboezem.common.properties.Drawable;
import edu.ncsu.jlboezem.common.properties.Mobile;
import edu.ncsu.jlboezem.common.properties.Pathed;
import edu.ncsu.jlboezem.common.properties.Scripted;
import edu.ncsu.jlboezem.common.properties.Spawner;
import edu.ncsu.jlboezem.communication.ConnectionManager;
import edu.ncsu.jlboezem.communication.packets.PacketAuth;
import edu.ncsu.jlboezem.communication.packets.PacketLevelRequest;
import edu.ncsu.jlboezem.events.Event;
import edu.ncsu.jlboezem.events.EventManager;
import edu.ncsu.jlboezem.events.processors.ProcessorCollision;
import edu.ncsu.jlboezem.events.processors.ProcessorDamage;
import edu.ncsu.jlboezem.events.processors.ProcessorKeyPress;
import edu.ncsu.jlboezem.events.processors.ProcessorNetwork;
import edu.ncsu.jlboezem.events.processors.ProcessorPlayer;
import edu.ncsu.jlboezem.events.processors.ProcessorReplays;
import edu.ncsu.jlboezem.events.processors.ProcessorScripts;
import edu.ncsu.jlboezem.threading.ThreadManager;
import processing.core.*;

public class Client extends PApplet {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8475193083131370059L;
	public static Client INSTANCE;
	public static final int SCREEN_WIDTH = 1000;
	public static final int SCREEN_HEIGHT = 1000;
	
	public Map<String, Character> keybinds;

	public static int PORT = 9999;
	public static String SERVER_ADDRESS = "127.0.0.1";
	public Socket socket;
	
	private Random myRandom;
	private String clientName;

	private long lastTick = 0;
	
	public String getClientName() {
		return clientName;
	}

	public void setup() {
		INSTANCE = this;
		size(SCREEN_WIDTH, SCREEN_HEIGHT);
		ConnectionManager.INSTANCE.setType("Client");
		keybinds = new HashMap<String, Character>();
		keybinds.put("PlayerLeftMove", 'a');
		keybinds.put("PlayerRightMove", 'd');
		keybinds.put("PlayerJumpMove", 'w');
		keybinds.put("PlayerDownMove", 's');
		keybinds.put("ReplayRecordStart", 'n');
		keybinds.put("ReplayRecordStop", 'm');
		keybinds.put("ReplayPlaySpeed1", '1');
		keybinds.put("ReplayPlaySpeed2", '2');
		keybinds.put("ReplayPlaySpeed3", '3');
		keybinds.put("ReplayPlaySpeed>", '5');
		keybinds.put("ReplayPlaySpeed<", '4');
		keybinds.put("Derp", 'k');
		
		Mobile.INSTANCE.maxVelocity = new Vector2D(75, 75);
		Drawable.INSTANCE.setup(this);
		
		clientName = "Username" + (int)random(9999f);
		ThreadManager.INSTANCE.setLocalName(clientName);
		try {
			socket = new Socket(SERVER_ADDRESS, PORT);
			ThreadManager.INSTANCE.receiveSocketsWaiting.add(socket);
			ThreadManager.INSTANCE.sendSocketsWaiting.add(socket);
			boolean added = ThreadManager.INSTANCE.addThread(ThreadManager.ThreadType.NetworkingReceive);
			if (!added) {
				System.out.println("Networking Receive Thread couldn't be created.");
			}
			added = ThreadManager.INSTANCE.addThread(ThreadManager.ThreadType.NetworkingSend);
			if (!added) {
				System.out.println("Networking Send Thread couldn't be created.");
			}
			added = ThreadManager.INSTANCE.addThread(ThreadManager.ThreadType.NetworkingProcessor);
			if (!added) {
				System.out.println("Networking Processor Thread couldn't be created.");
			}
			try {
				ThreadManager.INSTANCE.writeToQueue("Send" + getServerSockName(), new PacketAuth(clientName));
				ThreadManager.INSTANCE.writeToQueue("Send" + getServerSockName(), new PacketLevelRequest());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ConnectionManager.INSTANCE.setShouldMakeEvents(false);
			ConnectionManager.INSTANCE.setType("ClientOfServer");
			ConnectionManager.INSTANCE.setServer(getServerSockName());
		} catch (Exception e) {
			GameObjectCreator.addPlatform("Platform1", new Vector2D(500, 930), 10, 50, 0xE1E1E1, 100);
			GameObjectCreator.addPlatform("GroundPlatform", new Vector2D(20, SCREEN_HEIGHT - 20), SCREEN_WIDTH-40, 20, 0xE1E1E1, 50);
			Damaging.INSTANCE.addObject("GroundPlatform", 100);
			GameObjectCreator.addPlatform("CeilingPlatform", new Vector2D(20, 0), SCREEN_WIDTH - 40, 20, 0xE1E1E1, 50);
			GameObjectCreator.addPlatform("LeftPlatform", new Vector2D(0, 20), 20, SCREEN_HEIGHT - 40, 0xE1E1E1, 50);
			//GameObjectCreator.addPlatform("RightPlatform", new Vector2D( SCREEN_WIDTH - 20, 20), 20, SCREEN_HEIGHT - 40, 0xE1E1E1, 50);
			GameObjectCreator.addPlatform("DeathZone", new Vector2D( SCREEN_WIDTH - 20, 20), 20, SCREEN_HEIGHT - 40, 0xE1E1E1, 50);
			Drawable.INSTANCE.removeObject("DeathZone");
			Damaging.INSTANCE.addObject("DeathZone", 100);
			GameObjectCreator.addPlatform("MovingPlatform", new Vector2D(SCREEN_WIDTH/ 4, SCREEN_HEIGHT - 50), 20, 20, 0xAAAAAA, 100);
			Pathed.INSTANCE.addObject("MovingPlatform", new Vector2D(SCREEN_WIDTH/4,  SCREEN_HEIGHT - 40), new Vector2D(3 * SCREEN_WIDTH/4, 3*SCREEN_HEIGHT/4), true);
			Spawner.INSTANCE.addObject("PlayerSpawner", new Object[] { "", new Vector2D(SCREEN_WIDTH / 2f, SCREEN_HEIGHT / 2f), 20, 50, 0xAA00DD, 100 });
			//Spawner.INSTANCE.spawn("PlayerSpawner", "Player" + clientName);
			Scripted.INSTANCE.addObject("derp", "scripts/derp.js");
			Scripted.INSTANCE.addObject("deathEvent", "scripts/eventScript.js");
			ConnectionManager.INSTANCE.setShouldMakeEvents(true);
			Map<String, Object> infoSpawn = new ConcurrentHashMap<String, Object>();
			infoSpawn.put("guid", "Player" + clientName);
			EventManager.INSTANCE.addEvent(new Event("PlayerSpawn", infoSpawn, ConnectionManager.INSTANCE.getGameTimeline().getElapsedTime() + 1000, ThreadManager.INSTANCE.getLocalName()));
		}
		//GameObjectCreator.addMobileObject("Player" + clientName, new Vector2D(500, 300), 100, 10, 30, 0xAA01FF, 100);
		ProcessorReplays replay = new ProcessorReplays();
		replay.subscribe();
		ProcessorDamage damage = new ProcessorDamage();
		damage.subscribe();
		ProcessorPlayer player = new ProcessorPlayer();
		player.subscribe();
		ProcessorNetwork network = new ProcessorNetwork();
		network.subscribe();
		ProcessorCollision collision = new ProcessorCollision();
		collision.subscribe();
		ProcessorKeyPress keyPress = new ProcessorKeyPress();
		keyPress.subscribe();
		ProcessorScripts deathEventScript = new ProcessorScripts("deathEvent", ".*");
		deathEventScript.subscribe();
		lastTick = System.currentTimeMillis();
	}

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
			//System.out.println(myTime + " " + ConnectionManager.INSTANCE.getRealTimeline().getElapsedTime() + " --> " + (myTime - ConnectionManager.INSTANCE.getRealTimeline().getElapsedTime()));
		}
		Drawable.INSTANCE.draw();
		long timestep = (currentTime - lastTick);
		ConnectionManager.INSTANCE.getLoopTimeline().tick();
		ConnectionManager.INSTANCE.getRealTimeline().tick(timestep);//System.currentTimeMillis() - lastTick);
		lastTick = currentTime;
		if (timestep > 100) System.out.println("large timestep: " + timestep);
	}
	
	public void keyPressed() {
		keybinds.forEach((bind, input) -> {
			if (input == key) {
				Map<String, Object> info = new HashMap<String, Object>();
				info.put("Action", bind);
				info.put("guid", "Player" + clientName);
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
				info.put("guid", "Player" + clientName);
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
	public static void main(String args[]) {
		PApplet.main(new String[] { "--present", "edu.ncsu.jlboezem.client.Client" });
	}

	public Random getRandom() {
		return myRandom;
	}
	private String getServerSockName() {
		return socket.getInetAddress().getCanonicalHostName() + ":" + socket.getPort();
	}
}

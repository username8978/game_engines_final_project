package edu.ncsu.jlboezem.common.properties;

import java.io.IOException;
import java.io.Serializable;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import edu.ncsu.jlboezem.client.Client;
import edu.ncsu.jlboezem.common.GameObjectCreator;
import edu.ncsu.jlboezem.common.Vector2D;

public class Spawner extends Property implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2243330439489655445L;
	public static final Spawner INSTANCE = new Spawner();
	private Random myRandom;
	private ConcurrentHashMap<String, Object[]> data;
	
	private Spawner() {
		data = new ConcurrentHashMap<String, Object[]>();
		myRandom = new Random();
	}
	/**
	 * 
	 * @param guid
	 * @param type
	 * @param data array of information needed to create object, guid in index 0 is ignored.
	 */
	public void addObject(String guid, final Object[] data) {
		super.addObject(guid);
		this.data.put(guid, data.clone());
	}
	
	private void writeObject(java.io.ObjectOutputStream out) {
		try {
			super.writeBaseObject(out);
			out.writeObject(INSTANCE.data);
			out.writeObject(myRandom);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void readObject(java.io.ObjectInputStream in) {
		try {
			super.readBaseObject(in);
			data = (ConcurrentHashMap<String, Object[]>) in.readObject();
			myRandom = (Random) in.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void spawn(String spawnerGuid, String playerGuid) {
		try {
			Object[] data = this.data.get(spawnerGuid).clone();
			if (data == null)
				return;
			String type = (String) data[0];
			if (type.contains("Player")) {
				System.out.println("Respawning Player");
				data[0] = playerGuid;
				//System.out.println(data[1]);
				GameObjectCreator.addObject(GameObjectCreator.GameObjectType.Mobile, data.clone());
				Damageable.INSTANCE.addObject(playerGuid, 100); //TODO: allow non-player stuff
				Player.INSTANCE.addObject(playerGuid);
			} else if (type.contains("Shot")) {
				data[0] = playerGuid;
				Vector2D myPos = Positional.INSTANCE.getPosition(spawnerGuid);
				Vector2D offset = ((Vector2D)data[1]);
				data[1] = Vector2D.add(myPos, offset);
				GameObjectCreator.addObject(GameObjectCreator.GameObjectType.Mobile, data.clone());
				Damageable.INSTANCE.addObject(playerGuid, 10);
				Damaging.INSTANCE.addObject(playerGuid, 1000);
				final int speed = 2;
				Mobile.INSTANCE.setVelocity(playerGuid, new Vector2D(offset.x > 0 ? speed + myRandom.nextInt(5) : offset.x == 0 ? 0 : -(speed + myRandom.nextInt(5)), offset.y > 0 ? (speed + myRandom.nextInt(5)) : offset.y == 0 ? 0 : -(speed + myRandom.nextInt(5))));
			}
		} catch(Exception e) {
			e.printStackTrace(); //TODO
		}
	}
	public void replaceWith(Spawner other) {
		this.data = other.data;
		this.myRandom = other.myRandom;
		super.replaceWith(other);
	}
	public Random getRandom() {
		return myRandom;
	}
}

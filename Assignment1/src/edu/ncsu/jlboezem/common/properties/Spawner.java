package edu.ncsu.jlboezem.common.properties;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import edu.ncsu.jlboezem.common.GameObjectCreator;

public class Spawner extends Property implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2243330439489655445L;
	public static final Spawner INSTANCE = new Spawner();
	private ConcurrentHashMap<String, Object[]> data;
	
	private Spawner() {
		data = new ConcurrentHashMap<String, Object[]>();
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
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void readObject(java.io.ObjectInputStream in) {
		try {
			super.readBaseObject(in);
			data = (ConcurrentHashMap<String, Object[]>) in.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void spawn(String spawnerGuid, String playerGuid) {
		try {
			Object[] data = this.data.get(spawnerGuid);
			data[0] = playerGuid;
			//System.out.println(data[1]);
			GameObjectCreator.addObject(GameObjectCreator.GameObjectType.Mobile, data.clone());
			Damageable.INSTANCE.addObject(playerGuid, 100); //TODO: allow non-player stuff
			Player.INSTANCE.addObject(playerGuid);
		} catch(Exception e) {
			e.printStackTrace(); //TODO
		}
	}
	public void replaceWith(Spawner other) {
		this.data = other.data;
		super.replaceWith(other);
	}
}

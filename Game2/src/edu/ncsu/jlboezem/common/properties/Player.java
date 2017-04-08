package edu.ncsu.jlboezem.common.properties;

import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Player extends Property implements Serializable {
	public static final Player INSTANCE = new Player();
	private static final long serialVersionUID = -6895348495911332300L;

	public float MOVE_AMOUNT = 2.5f;
	private ConcurrentHashMap<String, ConcurrentHashMap<String, Boolean>> movingOnInput;
	
	private Player() {
		movingOnInput = new ConcurrentHashMap<String, ConcurrentHashMap<String,Boolean>>();
	}
	
	@Override
	public void addObject(String guid) {
		super.addObject(guid);
		movingOnInput.put(guid, new ConcurrentHashMap<String, Boolean>());
	}
	
	@Override
	public void removeObject(String guid) {
		super.removeObject(guid);
		movingOnInput.remove(guid);
	}
	
	public List<String> getPlayerGuids() {
		List<String> list = new LinkedList<String>();
		list.addAll(objects);
		return list;
	}
	public boolean getIsPlayerMovingOnInput(String guid) {
		return hasGameObject(guid) ? movingOnInput.get(guid).containsValue(Boolean.TRUE) : false;
	}
	public boolean getIsPlayerMovingOnInput(String guid, String input) {
		return hasGameObject(guid) ? movingOnInput.get(guid).containsKey(input) ? movingOnInput.get(guid).get(input) : false : false;
	}
	public void setPlayerMovingOnInput(String guid, String input, boolean value) {
		if (hasGameObject(guid)) movingOnInput.get(guid).put(input, value);
	}

	public void replaceWith(Player other) {
		this.movingOnInput = other.movingOnInput;
		super.replaceWith(other);
	}
	private void writeObject(java.io.ObjectOutputStream out) {
		try {
			super.writeBaseObject(out);
			out.writeObject(INSTANCE.movingOnInput);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void readObject(java.io.ObjectInputStream in) {
		try {
			super.readBaseObject(in);
			movingOnInput = (ConcurrentHashMap<String, ConcurrentHashMap<String,Boolean>>) in.readObject();
			//position.entrySet().forEach(e -> System.out.println(System.currentTimeMillis()+ "     " + e.getKey() + ":" + e.getValue()));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}

package edu.ncsu.jlboezem.common.properties;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

import edu.ncsu.jlboezem.common.Vector2D;

public class Positional extends Property implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4592532721576251948L;
	public static final Positional INSTANCE = new Positional();
	private ConcurrentHashMap<String, Vector2D> positions;
	
	private Positional() {
		positions = new ConcurrentHashMap<String, Vector2D>();
	}

	public void addObject(String guid, Vector2D pos) {
		super.addObject(guid);
		positions.put(guid, pos);
	}
	public void removeObject(String guid) {
		super.removeObject(guid);
	}
	public void updateObject(String guid, double x, double y) { //TODO: rename
		if (null == positions.get(guid)) {
			return;
		}
		positions.get(guid).x += x;
		positions.get(guid).y += y;
	}
	public Vector2D getPosition(String guid) {
		return positions.get(guid);
	}
	private void writeObject(java.io.ObjectOutputStream out) {
		try {
			super.writeBaseObject(out);
			out.writeObject(INSTANCE.positions);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void readObject(java.io.ObjectInputStream in) {
		try {
			super.readBaseObject(in);
			positions = (ConcurrentHashMap<String, Vector2D>) in.readObject();
			//position.entrySet().forEach(e -> System.out.println(System.currentTimeMillis()+ "     " + e.getKey() + ":" + e.getValue()));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	public void replaceWith(Positional other) {
		this.positions = other.positions;
		super.replaceWith(other);
		//System.out.println(other.position == position);
	}

	public void setPosition(String guid, Vector2D value) {
		if (positions.containsKey(guid))
			this.positions.put(guid, value);
	}
}

package edu.ncsu.jlboezem.common.properties;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

public class Damageable extends Property implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7043875735191710071L;
	public static final Damageable INSTANCE = new Damageable();
	private ConcurrentHashMap<String, Integer> health;
	private ConcurrentHashMap<String, Integer> baseHealth;
	private ConcurrentHashMap<String, Boolean> alive;
	
	private Damageable() {
		health = new ConcurrentHashMap<String, Integer>();
		baseHealth = new ConcurrentHashMap<String, Integer>();
		alive = new ConcurrentHashMap<String, Boolean>();
	}
	public void addObject(String guid, Integer startingHealth) {
		super.addObject(guid);
		health.put(guid, startingHealth);
		baseHealth.put(guid, startingHealth);
		alive.put(guid, true);
	}
	
	public void updateObject(String guid, int healthDiff) {
		health.put(guid, health.getOrDefault(guid, 0) + healthDiff);
	}
	public int getHealth(String guid) {
		return health.get(guid);
	}
	public int getBaseHealth(String guid) {
		return baseHealth.get(guid);
	}
	public boolean isAlive(String guid) {
		return alive.getOrDefault(guid, true);
	}
	public void removeObject(String guid) {
		super.removeObject(guid);
		health.remove(guid);
		baseHealth.remove(guid);
	}
	private void writeObject(java.io.ObjectOutputStream out) {
		try {
			super.writeBaseObject(out);
			out.writeObject(INSTANCE.health);
			out.writeObject(INSTANCE.baseHealth);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void readObject(java.io.ObjectInputStream in) {
		try {
			super.readBaseObject(in);
			health = (ConcurrentHashMap<String, Integer>) in.readObject();
			baseHealth = (ConcurrentHashMap<String, Integer>) in.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void replaceWith(Damageable other) {
		this.health = other.health;
		this.baseHealth = other.baseHealth;
		super.replaceWith(other);
	}
}

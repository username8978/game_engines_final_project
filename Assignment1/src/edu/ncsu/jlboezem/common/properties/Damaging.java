package edu.ncsu.jlboezem.common.properties;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

public class Damaging extends Property implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7338442645770762891L;
	public static final Damaging INSTANCE = new Damaging();
	private ConcurrentHashMap<String, Integer> damage;
	
	private Damaging() {
		damage = new ConcurrentHashMap<String, Integer>();
	}
	public void addObject(String guid, Integer damage) {
		super.addObject(guid);
		this.damage.put(guid, damage);
	}
	
	public void updateObject(String guid, int damageDiff) {
		damage.put(guid, damage.get(guid) + damageDiff);
	}
	public void removeObject(String guid) {
		super.removeObject(guid);
		damage.remove(guid);
	}
	private void writeObject(java.io.ObjectOutputStream out) {
		try {
			super.writeBaseObject(out);
			out.writeObject(INSTANCE.damage);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void readObject(java.io.ObjectInputStream in) {
		try {
			super.readBaseObject(in);
			damage = (ConcurrentHashMap<String, Integer>) in.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public int getDamage(String guid) {
		Integer dmg = damage.get(guid);
		return null != dmg ? dmg : 0;
	}
	public void replaceWith(Damaging other) {
		this.damage = other.damage;
		super.replaceWith(other);
	}
}

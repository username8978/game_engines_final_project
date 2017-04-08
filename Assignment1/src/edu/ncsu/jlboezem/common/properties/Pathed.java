package edu.ncsu.jlboezem.common.properties;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import edu.ncsu.jlboezem.common.Ticking;
import edu.ncsu.jlboezem.common.Vector2D;

public class Pathed extends Property implements Ticking, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -283607764838974083L;

	public static final Pathed INSTANCE = new Pathed();
	
	private ConcurrentHashMap<String, Vector2D> start;
	private ConcurrentHashMap<String, Vector2D> end;
	private ConcurrentHashMap<String, Boolean> doReverse;
	
	public Pathed() {
		start = new ConcurrentHashMap<String, Vector2D>();
		end = new ConcurrentHashMap<String, Vector2D>();
		doReverse = new ConcurrentHashMap<String, Boolean>();
	}
	public void tick(long timestep) {
		double adjustedTimestep = 1;
		doReverse.entrySet().forEach(entry -> Positional.INSTANCE.updateObject(entry.getKey(), (end.get(entry.getKey()).x - start.get(entry.getKey()).x) / (10 * timestep), (end.get(entry.getKey()).y - start.get(entry.getKey()).y) / (10 * timestep)));
		for (Entry<String, Vector2D> entry : end.entrySet()) {
			Vector2D pos = Positional.INSTANCE.getPosition(entry.getKey());
			double signX = Math.signum(entry.getValue().x - start.get(entry.getKey()).x);
			double signY = Math.signum(entry.getValue().y - start.get(entry.getKey()).y);
			if (Math.signum(pos.x - entry.getValue().x) == signX && Math.signum(pos.y - entry.getValue().y) == signY) {
				if (doReverse.get(entry.getKey())) {
					Vector2D st = start.get(entry.getKey()).clone();
					start.put(entry.getKey(), entry.getValue());
					end.put(entry.getKey(), st);
				} else {
					start.put(entry.getKey(), entry.getValue());
				}
			}
		}
	}
	
	public void addObject(String guid, Vector2D start, Vector2D end, boolean doReverse) {
		super.addObject(guid);
		this.start.put(guid, start.clone());
		this.end.put(guid, end.clone());
		this.doReverse.put(guid, doReverse);
	}
	public void removeObject(String guid) {
		super.removeObject(guid);
		start.remove(guid);
		end.remove(guid);
		doReverse.remove(guid);
	}
	public void replaceWith(Pathed other) {
		this.start = other.start;
		this.end = other.end;
		this.doReverse = other.doReverse;
		super.replaceWith(other);
	}

	private void writeObject(java.io.ObjectOutputStream out) {
		try {
			super.writeBaseObject(out);
			out.writeObject(INSTANCE.start);
			out.writeObject(INSTANCE.end);
			out.writeObject(INSTANCE.doReverse);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void readObject(java.io.ObjectInputStream in) {
		try {
			super.readBaseObject(in);
			start = (ConcurrentHashMap<String, Vector2D>) in.readObject();
			end = (ConcurrentHashMap<String, Vector2D>) in.readObject();
			doReverse = (ConcurrentHashMap<String, Boolean>) in.readObject();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}

package edu.ncsu.jlboezem.common.properties;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import edu.ncsu.jlboezem.common.Ticking;
import edu.ncsu.jlboezem.common.Vector2D;

public class Mobile extends Property implements Serializable, Ticking {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8195751970265325650L;
	public static final Mobile INSTANCE = new Mobile();
	public ConcurrentHashMap<String, Vector2D> velocity;
	
	private LinkedBlockingQueue<String> immobile;
	
	public Vector2D maxVelocity;
	private Mobile() {
		velocity = new ConcurrentHashMap<String, Vector2D>();
		immobile = new LinkedBlockingQueue<String>();
	}
	
	public void tick(long timestep) {
		double adjustedTimestep = timestep / 10d;
		for (String guid : velocity.keySet())
		{
			if (immobile.contains(guid)) {
				continue;
			}
			Vector2D pos = new Vector2D(0, 0);
			if (null == velocity.get(guid)) {
				continue;
			}
			pos.y = adjustedTimestep * (velocity.get(guid).y + adjustedTimestep);
			pos.x = adjustedTimestep * (velocity.get(guid).x);
			Shaped.INSTANCE.tick(timestep);
			Positional.INSTANCE.updateObject(guid, pos.x, pos.y);
			Vector2D vel = velocity.get(guid);
			if (vel.x > maxVelocity.x) {
				velocity.get(guid).x = maxVelocity.x;
			} else if (vel.x < -maxVelocity.x) {
				velocity.get(guid).x = -maxVelocity.x;
			}
			if (vel.y > maxVelocity.y) {
				velocity.get(guid).y = maxVelocity.y;
			} else if (vel.y < -maxVelocity.y) {
				velocity.get(guid).y = -maxVelocity.y;
			}
		}
	}
	
	public void addObject(String guid) {
		super.addObject(guid);
		velocity.put(guid, new Vector2D(0, 0));
	}
	public void removeObject(String guid) {
		super.removeObject(guid);
		velocity.remove(guid);
	}
	
	public void updateVelocity(String guid, double x, double y) {
		if (!hasGameObject(guid))
			return;
		velocity.getOrDefault(guid, new Vector2D(0,0)).x += x;
		velocity.getOrDefault(guid, new Vector2D(0,0)).y += y;
	}
	public void setVelocityX(String guid, double x) {
		if (hasGameObject(guid))
			velocity.getOrDefault(guid, new Vector2D(0,0)).x = x;
	}
	public void setVelocityY(String guid, double y) {
		if (hasGameObject(guid))
			velocity.getOrDefault(guid, new Vector2D(0,0)).y = y;
	}
	public Vector2D getVelocity(String guid) {
		return velocity.getOrDefault(guid, new Vector2D(0,0));
	}
	public void setImmobile(String guid, boolean value) {
		if (!hasGameObject(guid)) {
			return;
		}
		if (value) {
			if (!immobile.contains(guid)) {
				immobile.add(guid);
			}
		} else {
			immobile.remove(guid);
		}
	}
	
	private void writeObject(java.io.ObjectOutputStream out) {
		try {
			super.writeBaseObject(out);
			out.writeObject(INSTANCE.velocity);
			out.writeObject(INSTANCE.maxVelocity);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void readObject(java.io.ObjectInputStream in) {
		try {
			super.readBaseObject(in);
			velocity = (ConcurrentHashMap<String, Vector2D>) in.readObject();
			maxVelocity = (Vector2D) in.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void replaceWith(Mobile other) {
		this.velocity = other.velocity;
		this.maxVelocity = other.maxVelocity;
		super.replaceWith(other);
	}

	public void setVelocity(String guid, Vector2D vel) {
		velocity.put(guid, vel);
	}
}

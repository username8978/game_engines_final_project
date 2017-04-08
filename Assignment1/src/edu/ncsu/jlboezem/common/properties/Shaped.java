package edu.ncsu.jlboezem.common.properties;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import edu.ncsu.jlboezem.common.Ticking;
import edu.ncsu.jlboezem.common.Vector2D;
import edu.ncsu.jlboezem.communication.ConnectionManager;
import edu.ncsu.jlboezem.events.Event;
import edu.ncsu.jlboezem.events.EventManager;
import edu.ncsu.jlboezem.threading.ThreadManager;

//TODO: condense values
public class Shaped extends Property implements Serializable, Ticking {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5623131831041516256L;
	public static final Shaped INSTANCE = new Shaped();
	private ConcurrentHashMap<String, Float> value1, value2;
	private ConcurrentHashMap<String, Boolean> collideable;

	private LinkedBlockingQueue<String> collisions;
	
	
	private Shaped() {
		value1 = new ConcurrentHashMap<String, Float>();
		value2 = new ConcurrentHashMap<String, Float>();
		collideable = new ConcurrentHashMap<String, Boolean>();
		collisions = new LinkedBlockingQueue<String>();
	}

	public void tick(long timestep) {
		List<String> usedGuids = new LinkedList<String>();
		// Remove old collisions and their normals
		List<String> invalidCollisions = new LinkedList<String>();
		for (String coll : collisions) {
			Vector2D pos = Positional.INSTANCE.getPosition(coll.split(":")[0]);
			java.awt.Rectangle rect = new java.awt.Rectangle((int) pos.x, (int) pos.y, value1.get(coll.split(":")[0]).intValue(), value2.get(coll.split(":")[0]).intValue());
			Vector2D oPos = Positional.INSTANCE.getPosition(coll.split(":")[1]);
			java.awt.Rectangle oRect = new java.awt.Rectangle((int) oPos.x,
					(int) oPos.y, value1.get(coll.split(":")[1]).intValue(), value2.get(
							coll.split(":")[1]).intValue());
			if (!rect.intersects(oRect)) {
				invalidCollisions.add(coll);
				//Mobile.INSTANCE.updateVelocity(coll.getKey()[0], -coll.getValue().x, -coll.getValue().y);
				//mobile.removeForce(coll.getKey()[1], "NormalY" + coll.getKey()[1]); //TODO: see what works better
				//mobile.removeForce(coll[0], "NormalX" + coll.getKey()[1]);
			}
		}
		collisions.removeAll(invalidCollisions);
		for (String guid : collideable.keySet()) {
			usedGuids.add(guid);
			// TODO: better collision
			Vector2D pos = Positional.INSTANCE.getPosition(guid);
			if (null == pos || null == value1.get(guid) || null == value2.get(guid)) {
				continue;
			}
			java.awt.Rectangle rect = new java.awt.Rectangle((int) pos.x,
					(int) pos.y, value1.get(guid).intValue(), value2.get(guid)
							.intValue());
			for (String oGuid : collideable.keySet()) {
				if (usedGuids.contains(oGuid) || collisions.contains(guid + ":" + oGuid)) {
					continue;// TODO
				}	
				Vector2D oPos = Positional.INSTANCE.getPosition(oGuid);
				if (null == oPos || null == value1.get(oGuid) || null == value2.get(oGuid)) {
					continue;
				}
				java.awt.Rectangle oRect = new java.awt.Rectangle((int) oPos.x,
						(int) oPos.y, value1.get(oGuid).intValue(), value2.get(
								oGuid).intValue());
				if (rect.intersects(oRect)) {
					Rectangle2D intersectRect = rect.createIntersection(oRect);
					handleCollisions(guid, oGuid, rect, oRect, intersectRect);
				}
			}
		}
	}

	private void addCollision(String guid, String oGuid, Vector2D normal, Vector2D oNewPos) {
		String coll = guid + ":" + oGuid;
		if (!collisions.contains(coll)) {
			//collisions.add(coll);
			if (ConnectionManager.INSTANCE.getShouldMakeEvents()) {
				HashMap<String, Object> info = new HashMap<String, Object>();
				info.put("guid", guid);
				info.put("oGuid", oGuid);
				info.put("NewPos", normal);
				info.put("oNewPos", oNewPos);
				Event collision = new Event("Collision", info, ConnectionManager.INSTANCE.getGameTimeline().getElapsedTime(), ThreadManager.INSTANCE.getLocalName());
				EventManager.INSTANCE.addEvent(collision);
			}
		} else {
			System.out.println("Dup");
		}
	}

	public void addObject(String guid, float value1Value, float value2Value,
			boolean collideableValue) {
		super.addObject(guid);
		value1.put(guid, value1Value);
		value2.put(guid, value2Value);
		collideable.put(guid, collideableValue);
	}
	
	public void removeObject(String guid) {
		super.removeObject(guid);
		value1.remove(guid);
		value2.remove(guid);
		collideable.remove(guid);
	}

	public void adjustValues(String guid, float value1Diff, float value2Diff) {
		value1.put(guid, value1.get(guid) + value1Diff);
		value2.put(guid, value2.get(guid) + value2Diff);
	}

	private void handleCollisions(String guid, String oGuid, java.awt.Rectangle rect, java.awt.Rectangle oRect, Rectangle2D intersectRect) {
		final Vector2D startPos = new Vector2D(rect.getX(), rect.getY());
		Vector2D newPos = startPos.clone();
		final Vector2D oStartPos = new Vector2D(oRect.getX(), oRect.getY());
		Vector2D oNewPos = oStartPos.clone();
		if (Math.abs(intersectRect.getCenterX() - rect.getCenterX()) < Math.abs(intersectRect.getCenterY() - rect.getCenterY()) && Mobile.INSTANCE.hasGameObject(guid)) {
			if (intersectRect.getCenterY() > rect.getCenterY()) {
				newPos.y = rect.getY() - intersectRect.getHeight();
				//normal.y = -NORMAL_VELOCITY;
			} else if (intersectRect.getCenterY() < rect.getCenterY()) {
				newPos.y = rect.getY() + intersectRect.getHeight();
			}
		} else if (Mobile.INSTANCE.hasGameObject(guid)) {
			if (intersectRect.getCenterX() > rect.getCenterX()) {
				newPos.x = rect.getX() - intersectRect.getWidth();
			} else if (intersectRect.getCenterX() < rect.getCenterX()) {
				newPos.x = rect.getX() + intersectRect.getWidth();
			}
		}

		if (Math.abs(intersectRect.getCenterX() - oRect.getCenterX()) < Math.abs(intersectRect.getCenterY() - oRect.getCenterY()) && Mobile.INSTANCE.hasGameObject(oGuid)) {
			if (intersectRect.getCenterY() > oRect.getCenterY()) {
				oNewPos.y = oRect.getY() - intersectRect.getHeight();
			} else if (intersectRect.getCenterY() < oRect.getCenterY()) {
				oNewPos.y = oRect.getY() + intersectRect.getHeight();
			}
		} else if (Mobile.INSTANCE.hasGameObject(oGuid)) {
			if (intersectRect.getCenterX() > oRect.getCenterX()) {
				oNewPos.x = oRect.getX() - intersectRect.getWidth();
			} else if (intersectRect.getCenterX() < oRect.getCenterX()) {
				oNewPos.x = oRect.getX() + intersectRect.getWidth();
			}
		}
		if (startPos.equals(newPos)) {
			newPos = null;
		}
		if (oStartPos.equals(oNewPos)) {
			oNewPos = null;
		}
		addCollision(guid, oGuid, newPos, oNewPos);
	}
	
	private void writeObject(java.io.ObjectOutputStream out) {
		try {
			super.writeBaseObject(out);
			out.writeObject(INSTANCE.collideable);
			out.writeObject(INSTANCE.collisions);
			out.writeObject(INSTANCE.value1);
			out.writeObject(INSTANCE.value2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void readObject(java.io.ObjectInputStream in) {
		try {
			super.readBaseObject(in);
			collideable = (ConcurrentHashMap<String, Boolean>) in.readObject();
			collisions = (LinkedBlockingQueue<String>) in.readObject();
			value1 = (ConcurrentHashMap<String, Float>) in.readObject();
			value2 = (ConcurrentHashMap<String, Float>) in.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void replaceWith(Shaped other) {
		this.value1 = other.value1;
		this.value2 = other.value2;
		this.collideable = other.collideable;
		this.collisions = other.collisions;
		super.replaceWith(other);
	}

	public float getValue1(String guid) {
		return value1.get(guid);
	}
	public float getValue2(String guid) {
		return value2.get(guid);
	}
}

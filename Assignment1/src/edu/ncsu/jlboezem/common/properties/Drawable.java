package edu.ncsu.jlboezem.common.properties;


import java.io.IOException;
import java.io.Serializable;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import processing.core.PApplet;
import edu.ncsu.jlboezem.common.Vector2D;


public class Drawable extends Property implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5190117598882504162L;

	public static final Drawable INSTANCE = new Drawable();
	
	private ConcurrentHashMap<String, int[]> drawings;
	
	private PApplet owner;
	
	private Drawable() {
		drawings = new ConcurrentHashMap<String, int[]>();
	}

	public void setup(PApplet parent) {
		owner = parent;
	}
	public void draw() {
		owner.background(100);
		owner.noStroke();
		for (Entry<String, int[]> entry : drawings.entrySet())
		{
			if (!(Positional.INSTANCE.hasGameObject(entry.getKey()) && Shaped.INSTANCE.hasGameObject(entry.getKey()))) {
				continue;
			}
			owner.fill(owner.color((entry.getValue()[0] & 0xFF0000) >> 4, (entry.getValue()[0] & 0x00FF00) >> 2, entry.getValue()[0] & 0x0000FF), entry.getValue()[1]);
			Vector2D pos = Positional.INSTANCE.getPosition(entry.getKey());
			if (null == pos) {
				return;
			}
			Float value1 = Shaped.INSTANCE.getValue1(entry.getKey());
			Float value2 = Shaped.INSTANCE.getValue2(entry.getKey());
			if (null == value1 || null == value2) {
				return;
			}
			owner.rect((float)pos.x, (float)pos.y, value1, value2);
		}
	}
	
	public void addObject(String guid, int rgb, int opacity) {
		super.addObject(guid);
		drawings.put(guid, new int[] { rgb, opacity });
	}
	public void removeObject(String guid) {
		super.removeObject(guid);
		drawings.remove(guid);
	}
	public int[] getDrawing(String guid) {
		return drawings.get(guid);
	}
	public void setDrawing(String guid, int[] values) {
		drawings.put(guid, values);
	}
	public void setOpacity(String guid, int health) {
		int[] values = getDrawing(guid);
		values[1] = health;
		drawings.put(guid, values);
	}
	private void writeObject(java.io.ObjectOutputStream out) {
		try {
			super.writeBaseObject(out);
			out.writeObject(INSTANCE.drawings);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void readObject(java.io.ObjectInputStream in) {
		try {
			super.readBaseObject(in);
			drawings = (ConcurrentHashMap<String, int[]>) in.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void replaceWith(Drawable other) {
		this.drawings = other.drawings;
		super.replaceWith(other);
	}

}

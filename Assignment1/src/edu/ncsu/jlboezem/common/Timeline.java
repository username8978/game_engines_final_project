package edu.ncsu.jlboezem.common;

import java.io.IOException;
import java.io.Serializable;

public class Timeline implements Serializable {
	private static final long serialVersionUID = 3098097587898554779L;
	private long ticSize;
	private long anchor;
	private long time;
	private byte multiplier = (byte) 0x01;

	public static final byte DIVISION_MASK = (byte) 0x80;
	
	public Timeline(long ticSize, long anchor) {
		this.ticSize = ticSize;
		this.anchor = anchor;
		this.time = anchor;
	}
	
	public void tick() {
		long tic = ticSize;
		if (DIVISION_MASK == (multiplier & DIVISION_MASK)) {
			tic /= (multiplier & 0x7F);
		} else {
			tic *= multiplier;
		}
		time += tic;
	}
	public long getTime() {
		return time;
	}
	public long getElapsedTime() {
		return time - anchor;
	}
	public void tick(long timestep) {
		if (DIVISION_MASK == (multiplier & DIVISION_MASK)) {
			timestep /= (multiplier & ~DIVISION_MASK);
		} else {
			timestep *= multiplier;
		}
		time += timestep;
		//System.out.println(time + ", " + timestep + ", " + multiplier);
	}
	private void writeObject(java.io.ObjectOutputStream out) {
		try {
			out.writeLong(ticSize);
			out.writeLong(anchor);
			out.writeLong(time);
			out.writeByte(multiplier);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void readObject(java.io.ObjectInputStream in) {
		try {
			ticSize = in.readLong();
			anchor = in.readLong();
			time = in.readLong();
			multiplier = in.readByte();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public byte getMultiplier() {
		return multiplier;
	}

	public void setMultiplier(byte multiplier) {
		this.multiplier = multiplier;
	}

	public String toString() {
		return "Time: " + time + "; ticSize: " + ticSize + "; Anchor: " + anchor + "; multiplier: " + multiplier + "; Elapsed Time: " + getElapsedTime();
	}
}

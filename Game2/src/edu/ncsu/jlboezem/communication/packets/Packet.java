package edu.ncsu.jlboezem.communication.packets;

import java.io.Serializable;

public class Packet implements Serializable {
	private static final long serialVersionUID = -1118397781235751489L;
	public String from;
	private Object[] contents;
	
	public Packet() {
		contents = null;
	}
	public Packet(Object[] contents) {
		this.contents = contents;
	}
	public String getFrom() {
		return from;
	}
	public Object[] getContents() {
		return contents;
	}
}

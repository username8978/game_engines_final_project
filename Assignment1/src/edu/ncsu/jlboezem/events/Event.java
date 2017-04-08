package edu.ncsu.jlboezem.events;

import java.io.Serializable;
import java.util.Map;

public class Event implements Comparable<Event>, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 482692183200815477L;
	private String type;
	public void setType(String type) {
		this.type = type;
	}
	private Map<String, Object> info;
	private long timestamp;
	private String from;
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public Event(String type, Map<String, Object> info, long l, String from) {
		this.type = type;
		this.info = info;
		this.timestamp = l;
		this.from = from;
	}
	public String getType() {
		return type;
	}
	public Map<String, Object> getInfo() {
		return info;
	}
	public long getTimestamp() {
		return timestamp;
	}
	@Override
	public int compareTo(Event other) {
		return Long.compare(this.timestamp, other.timestamp);
	}
	
	public boolean equals(Event other) {
		return other.type == this.type && other.info.equals(this.info);
	}
	@Override
	public String toString() {
		return from + "@" + timestamp + ": " + type;
	}
}

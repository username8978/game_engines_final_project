package edu.ncsu.jlboezem.communication.packets;

import edu.ncsu.jlboezem.common.Timeline;
import edu.ncsu.jlboezem.common.properties.Property;

public class PacketLevelData extends Packet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6499250296729718976L;
	private java.util.concurrent.ConcurrentHashMap<String, Property> properties;
	private Timeline gameTimeline;
	private Timeline realTimeline;
	
	public PacketLevelData(java.util.concurrent.ConcurrentHashMap<String, Property> props, Timeline gameTimeline, Timeline realTimeline) {
		properties = props;
		this.gameTimeline = gameTimeline;
		this.realTimeline = realTimeline;
	}
	
	public java.util.concurrent.ConcurrentHashMap<String, Property> getProperties() {
		return properties;
	}
	public Timeline getGameTimeline() {
		return gameTimeline;
	}
	public Timeline getServerTime() {
		return realTimeline;
	}
	@Override
	public Object[] getContents() {
		return new Object[] { properties, gameTimeline, realTimeline };
	}

}

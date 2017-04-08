package edu.ncsu.jlboezem.communication.packets;

import edu.ncsu.jlboezem.common.properties.Property;

public class PacketLevelUpdate extends Packet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6499250296729718976L;
	private java.util.concurrent.ConcurrentHashMap<String, Property> properties;
	
	public PacketLevelUpdate(java.util.concurrent.ConcurrentHashMap<String, Property> props) {
		properties = props;
	}
	
	public java.util.concurrent.ConcurrentHashMap<String, Property> getProperties() {
		return properties;
	}
	@Override
	public Object[] getContents() {
		return new Object[] { properties };
	}

}

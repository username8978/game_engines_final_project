package edu.ncsu.jlboezem.communication.packets;

import java.io.Serializable;

public class PacketAuth extends Packet implements Serializable {
	private static final long serialVersionUID = 347872825802866580L;
	
	private String username;
	//TODO: password

	public PacketAuth(String user) {
		username = user;
	}
	
	@Override
	public Object[] getContents() {
		return new Object[] { username };
	}
}

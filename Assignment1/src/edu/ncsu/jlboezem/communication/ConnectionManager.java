package edu.ncsu.jlboezem.communication;

import java.util.concurrent.LinkedBlockingQueue;

import edu.ncsu.jlboezem.common.Timeline;

public class ConnectionManager {
	//TODO implement
	public static final ConnectionManager INSTANCE = new ConnectionManager();
	private String type;
	private boolean shouldMakeEvents;
	private boolean shouldPauseMakingEvents;
	private Timeline loopTimeline;
	private Timeline realTimeline;
	private Timeline gameTimeline;
	private LinkedBlockingQueue<String> playerGuids;
	private String server;

	public ConnectionManager() {
		type = "";
		playerGuids = new LinkedBlockingQueue<String>();
		loopTimeline = new Timeline(1, 0);
		realTimeline = new Timeline(1, System.currentTimeMillis());
		gameTimeline = new Timeline(17, System.currentTimeMillis()); //TODO: fix
	}
	
	public void setType(String type) {
		this.type = type;
	}
	public String getType() {
		return type;
	}
	public void setShouldMakeEvents(boolean shouldMakeEvents) {
		this.shouldMakeEvents = shouldMakeEvents;
	}
	public boolean getShouldMakeEvents() {
		return shouldMakeEvents;
	}
	
	public void addPlayer(String guid) {
		playerGuids.add(guid);
	}
	public void removePlayer(String guid) {
		playerGuids.remove(guid);
	}
	public Timeline getLoopTimeline() {
		return loopTimeline;
	}
	public Timeline getRealTimeline() {
		return realTimeline;
	}

	public Timeline getGameTimeline() {
		return gameTimeline;
	}

	public void setGameTimeline(Timeline gameTimeline) {
		this.gameTimeline = gameTimeline;
	}

	public void setRealTimeline(Timeline realTimeline) {
		this.realTimeline = realTimeline;
	}

	public boolean isShouldPauseMakingEvents() {
		return shouldPauseMakingEvents;
	}

	public void setShouldPauseMakingEvents(boolean shouldPauseMakingEvents) {
		this.shouldPauseMakingEvents = shouldPauseMakingEvents;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}
}

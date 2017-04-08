package edu.ncsu.jlboezem.events.processors;

import edu.ncsu.jlboezem.common.properties.Scripted;
import edu.ncsu.jlboezem.events.Event;
import edu.ncsu.jlboezem.events.EventManager;

public class ProcessorScripts extends Processor {

	private String subscription;
	private String script;
	
	public ProcessorScripts(String script, String subscription) {
		this.script = script;
		this.subscription = subscription;
	}
	@Override
	public void subscribe() {
		EventManager.INSTANCE.subscribe(this, subscription);
	}

	@Override
	public boolean handle(Event e) {
		Scripted.INSTANCE.loadScript(script);
		Scripted.INSTANCE.executeScript(e);
		return true;
	}

}

package edu.ncsu.jlboezem.common.properties;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class Scripted extends Property {
	/* The javax.script JavaScript engine used by this class. */
	private ScriptEngine js_engine = new ScriptEngineManager().getEngineByName("JavaScript");
	/* The Invocable reference to the engine. */
	private Invocable js_invocable = (Invocable) js_engine;
	/**
	 * 
	 */
	private static final long serialVersionUID = 5746424780991356317L;
	public static final Scripted INSTANCE = new Scripted();
	private ConcurrentHashMap<String, String> scripts;
	private Object currentScript;
	
	private Scripted() {
		scripts = new ConcurrentHashMap<String, String>();
	}

	public void addObject(String guid, String script) {
		super.addObject(guid);
		scripts.put(guid, script);
	}
	public void removeObject(String guid) {
		super.removeObject(guid);
	}

	public String getScript(String guid) {
		return scripts.get(guid);
	}
	/**
	 * Used to bind the provided object to the name in the scope of the scripts
	 * being executed by this engine.
	 */
	public void bindArgument(String name, Object obj) {
		js_engine.put(name,obj);
	}
	
	/**
	 * Will load the script source from the provided filename.
	 */
	public void loadScript(String script_name) {
		try {
			if (!scripts.containsKey(script_name))
				return;
			js_engine.eval(new java.io.FileReader(scripts.get(script_name)));
			currentScript = script_name;
		}
		catch(ScriptException se) {
			se.printStackTrace();
		}
		catch(java.io.IOException iox) {
			iox.printStackTrace();
		}
	}

	/**
	 * Will invoke the "update" function of the script loaded by this engine
	 * without any parameters.
	 */
	public void executeScript() {
		try {
			js_invocable.invokeFunction("update");
		}
		catch(ScriptException se) {
			se.printStackTrace();
		}
		catch(NoSuchMethodException nsme) {
			nsme.printStackTrace();
		}
	}

	/**
	 * Will invoke the "update" function of the script loaded by this engine
	 * with the provided list of parameters.
	 */
	public void executeScript(Object... args) {
		try {
			js_invocable.invokeFunction("update",args);
		}
		catch(ScriptException se) {
			se.printStackTrace();
		}
		catch(NoSuchMethodException nsme) {
			nsme.printStackTrace();
		}
	}
	
	private void writeObject(java.io.ObjectOutputStream out) {
		try {
			super.writeBaseObject(out);
			out.writeObject(INSTANCE.scripts);
			out.writeObject(currentScript);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void readObject(java.io.ObjectInputStream in) {
		try {
			super.readBaseObject(in);
			scripts = (ConcurrentHashMap<String, String>) in.readObject();
			String filename = (String) in.readObject();
			js_engine = new ScriptEngineManager().getEngineByName("JavaScript");
			js_invocable = (Invocable) js_engine;
			if (null != filename) {
				js_engine.eval(new java.io.FileReader(scripts.get(filename)));
			}
			//position.entrySet().forEach(e -> System.out.println(System.currentTimeMillis()+ "     " + e.getKey() + ":" + e.getValue()));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	public void replaceWith(Scripted other) {
		this.scripts = other.scripts;
		super.replaceWith(other);
		//System.out.println(other.position == position);
	}
}

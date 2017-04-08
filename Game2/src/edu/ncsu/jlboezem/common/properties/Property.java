package edu.ncsu.jlboezem.common.properties;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.LinkedBlockingQueue;

public class Property implements Serializable {
	private static final long serialVersionUID = 4458199776630853437L;
	public static final Property INSTANCE = new Property();
	LinkedBlockingQueue<String> objects;
	public Property() {
		objects = new LinkedBlockingQueue<String>();
	}
	public void addObject(String guid) {
		objects.add(guid);
	}
	public void removeObject(String guid) {
		objects.remove(guid);
	}
	public boolean hasGameObject(String guid) {
		return objects.contains(guid);
	}
	void readBaseObject(java.io.ObjectInputStream in) {
		try {
			objects = (LinkedBlockingQueue<String>) in.readObject();
			//position.entrySet().forEach(e -> System.out.println(System.currentTimeMillis()+ "     " + e.getKey() + ":" + e.getValue()));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	void writeBaseObject(ObjectOutputStream out) {
		try {
			out.writeObject(objects);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void replaceWith(Property other) {
		this.objects = other.objects;
	}
}

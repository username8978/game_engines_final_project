package edu.ncsu.jlboezem.common;

import edu.ncsu.jlboezem.common.properties.Drawable;
import edu.ncsu.jlboezem.common.properties.Mobile;
import edu.ncsu.jlboezem.common.properties.Positional;
import edu.ncsu.jlboezem.common.properties.Shaped;

//TODO: rework
public class GameObjectCreator {

	public enum GameObjectType {
		Platform, Mobile
	}
	public static void addObject(GameObjectType type, Object[] data) {
		switch (type) {
		case Platform:
			addPlatform((String)data[0], (Vector2D)data[1], (int)data[2], (int)data[3], (int)data[4], (int)data[5]);
			break;
		case Mobile:
			addMobileObject((String)data[0], (Vector2D)data[1], (int)data[2], (int)data[3], (int)data[4], (int)data[5]);
			break;
		default:
			System.out.println("Unknown type: " + type);
			break;
		}
	}
	public static void addObject(String type, Object[] data) {
		if ("Platform".equalsIgnoreCase(type)) {
			addPlatform((String)data[0], (Vector2D)data[1], (int)data[2], (int)data[3], (int)data[4], (int)data[5]);
		} else if ("Mobile".equalsIgnoreCase(type)) {
			addMobileObject((String)data[0], (Vector2D)data[1], (int)data[2], (int)data[3], (int)data[4], (int)data[5]);
		} else {
			System.out.println("Unknown type: " + type);
		}
	}
	
	public static void addPlatform(String guid, Vector2D pos, int width, int height, int color, int opacity) {
		Positional.INSTANCE.addObject(guid, pos);
		
		Shaped.INSTANCE.addObject(guid, width, height, true);
		
		//Mobile.INSTANCE.addObject(guid);
		//Mobile.INSTANCE.setImmobile(guid, true);
		//Mobile.INSTANCE.removeForce(guid, "Gravity"); //TODO: re-implement gravity
		
		Drawable.INSTANCE.addObject(guid, color, opacity);
	}

	public static void addMobileObject(String guid, final Vector2D pos, int shapeValue1, int shapeValue2, int color, int opacity) {
		Positional.INSTANCE.addObject(guid, pos.clone());
		
		Shaped.INSTANCE.addObject(guid, shapeValue1, shapeValue2, true);
		
		Mobile.INSTANCE.addObject(guid);
		
		Drawable.INSTANCE.addObject(guid, color, opacity);
	}

	public static int getTypeSize(GameObjectType type) {
		switch (type) {
		case Platform:
			return 6;
		case Mobile:
			return 6;
		default:
			return -1;
		}
	}
	public static int getTypeSize(String type) {
		if ("Platform".equalsIgnoreCase(type)) {
			return 6;
		} else if ("Mobile".equalsIgnoreCase(type)) {
			return 6;
		} else {
			return -1;
		}
	}
}

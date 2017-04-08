package edu.ncsu.jlboezem.common;

import java.io.Serializable;

public class Vector2D implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3864142964176297552L;
	public double x;
	public double y;
	public Vector2D(double x, double y) {
		this.x = x;
		this.y = y;
	}
	public String toString() {
		return "(" + x + "," + y + ")";
	}
	public boolean equals(Vector2D obj) {
		return obj.x == this.x && obj.y == this.y;
	}
	@Override
	public Vector2D clone() {
		return new Vector2D(x, y);
	}
}
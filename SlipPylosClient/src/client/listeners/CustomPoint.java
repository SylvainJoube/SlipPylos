package client.listeners;

import java.awt.Point;

@SuppressWarnings("serial")
public class CustomPoint extends Point {
	public boolean validPoint = false;
	public CustomPoint(int x, int y) {
		super(x, y);
	}
}
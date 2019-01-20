package client.listeners;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public class GenericMouseMotionListener implements MouseMotionListener {
	
	@Override
	public void mouseDragged(MouseEvent event) {
		mouseMoved(event);
		//LogWriter.Log("MyMouseMotionListener.mouseDragged :  event.getX = " + event.getX() + "event.getY = " + event.getY());
	}
	
	@Override
	public void mouseMoved(MouseEvent event) {
		//LogWriter.Log("MyMouseMotionListener.mouseMoved :  event.getX = " + event.getX() + "event.getY = " + event.getY());
		
		ListenerMouseEvent custonEvent = new ListenerMouseEvent(event.getButton(), event.getX(), event.getY(), ListenerMouseEventType.MOVED);
		Listeners.addListenerFromThread(custonEvent);
		// Cette action sera traitée de manière thread-safe dans le thread principal, via GameEventHandler.staticLoop();
		
	}
	
}

package client.listeners;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class GenericMouseListener implements MouseListener {
	
	@Override public void mouseExited(MouseEvent event) {
		//LogWriter.Log("MyMouseListener.mouseExited :  event.getX = " + event.getX() + "event.getY = " + event.getY());
	}
	@Override public void mousePressed(MouseEvent event) {
		//LogWriter.Log("MyMouseListener.mousePressed :  event.getX = " + event.getX() + "event.getY = " + event.getY());
		ListenerMouseEvent custonEvent = new ListenerMouseEvent(event.getButton(), event.getX(), event.getY(), ListenerMouseEventType.PRESSED);
		Listeners.addListenerFromThread(custonEvent);
		// Cette action sera traitée de manière thread-safe dans le thread principal, via GameEventHandler.staticLoop();
		
	}
	
	@Override public void mouseReleased(MouseEvent event) {

		ListenerMouseEvent custonEvent = new ListenerMouseEvent(event.getButton(), event.getX(), event.getY(), ListenerMouseEventType.RELEASED);
		Listeners.addListenerFromThread(custonEvent);
		// Cette action sera traitée de manière thread-safe dans le thread principal, via GameEventHandler.staticLoop();
		

	}
	@Override public void mouseClicked(MouseEvent event) {
		
	}
	@Override public void mouseEntered(MouseEvent event) {
		
	}
}
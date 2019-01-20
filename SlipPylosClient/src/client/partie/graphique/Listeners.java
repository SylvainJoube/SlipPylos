package client.partie.graphique;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import commun.partie.nonGraphique.PylosCell;
import commun.partie.nonGraphique.PylosCellResult;
import commun.partie.nonGraphique.PylosGridArray;
import commun.partie.nonGraphique.PylosPartie;
import commun.partie.nonGraphique.PylosPoint;
import commun.partie.nonGraphique.TeamType;

/** La classe Listeners existe pour faire en sorte que les retours d'information soient thread-safe.
 *  Elle stocke les évènements
 *  Listeners n'est pas à instancier
 */

public class Listeners {
	
	private static ArrayList<ListenerMouseEvent> mouseEventList = new ArrayList<ListenerMouseEvent>();
	private static AtomicBoolean canAccessToMouseEventList = new AtomicBoolean(true);
	private static Object mouseEventList_lock = new Object();
	
	/** Rechercher le plus ancien évènement non encore traîté
	 * @return le plus ancien ListenerMouseEvent non encore traîté
	 */
	public static ListenerMouseEvent getMouseEvent() {
		if (canAccessToMouseEventList.get() == false) return null; // aucun évènement accessible, pour ne pas bloquer le thread avec un synchronized
		synchronized(mouseEventList_lock) {
			if (mouseEventList.size() == 0) return null;
			ListenerMouseEvent result = mouseEventList.get(0);
			mouseEventList.remove(0);
			return result;
		}
	}
	
	/** Ajouter un évènement via un Listener
	 * @param arg_addEvent
	 */
	public static void addListenerFromThread(ListenerMouseEvent arg_addEvent) {
		synchronized(mouseEventList_lock) {
			canAccessToMouseEventList.set(false); // est là pour ne pas bloquer le thread qui interroge getMouseEvent() avec synchronized
			mouseEventList.add(arg_addEvent);
			canAccessToMouseEventList.set(true);
		}
	}
	
	
}

class CustomPoint extends Point {
	public boolean validPoint = false;
	public CustomPoint(int x, int y) {
		super(x, y);
	}
}

class MouseIsAbovePionSelection {
	public static boolean check(int xMouse, int yMouse) {
		int xDraw = GameHandler.jeuActuel.xDessinJetonsJoueur;
		int yDraw = GameHandler.jeuActuel.yDessinJetonsJoueur;
		
		if ((xDraw <= xMouse)
		&& (xDraw + CellDetection.cellWidth > xMouse)
		&& (yDraw <= yMouse)
		&& (yDraw + CellDetection.cellHeight > yMouse)) {
			return true;
		}
		return false;
	}
}

class MyMouseListener implements MouseListener {
	
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

class MyMouseMotionListener implements MouseMotionListener {
	
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
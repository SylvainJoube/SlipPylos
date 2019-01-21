package client.listeners;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
/*import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import client.partie.graphique.CellDetection;
import client.partie.graphique.GameHandler;
import commun.partie.nonGraphique.PylosCell;
import commun.partie.nonGraphique.PylosCellResult;
import commun.partie.nonGraphique.PylosGridArray;
import commun.partie.nonGraphique.PylosPartie;
import commun.partie.nonGraphique.PylosPoint;
import commun.partie.nonGraphique.TeamType;*/

/** La classe Listeners existe pour faire en sorte que les retours d'information soient thread-safe.
 *  Elle stocke les évènements
 *  Listeners n'est pas à instancier
 *  <br> <br>
 *  Deux gestions des évènements sont possibles : une image à image et une orientée évènements
 *  -> Soit on récupère le liste des évènements et on traîte cette liste (plusieurs clics par frame/image possibles)
 *  -> Soit on faut un refreshFrameListenerEvents en début de frame et un clearFrameListenerEvents en fin de frame.
 */

public class Listeners {
	
	private static ArrayList<ListenerMouseEvent> mouseEventList = new ArrayList<ListenerMouseEvent>();
	private static AtomicBoolean canAccessToMouseEventList = new AtomicBoolean(true);
	private static Object mouseEventList_lock = new Object();
	
	static private Listeners mainListener = new Listeners(); // le code de la création sera exécuté donc une fois
	
	// Pour la gestion image à image :
	
	
	static private boolean mousePressedThisFrame = false;
	static private boolean mouseReleasedThisFrame = false;
	static private boolean mouseMovedThisFrame = false;
	static private int currentMouseX = 0;
	static private int currentMouseY = 0;
	
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
			currentMouseX = arg_addEvent.mouseX; // mise à jour immédiate, pour frame_getMouse...(), pour la gestion d'évènements framé à frame et non évènements par évènements
			currentMouseY = arg_addEvent.mouseY;
			switch (arg_addEvent.eventType) {
			case MOVED : mouseMovedThisFrame = true; break;
			case PRESSED : mousePressedThisFrame = true; break;
			case RELEASED : mouseReleasedThisFrame = true; break;
			default : break;
			}
		}
	}
	
	public static void clearEvents() {
		// Pas d'utilisation de canAccessToMouseEventList en vérification : le code doit être exécuté immédiatement
		synchronized(mouseEventList_lock) {
			mouseEventList.clear();
		}
	}
	
	/** Regarde ce qui s'est passé depuis la dernière actualisation
	 *  Cette méthode est moins réactive que via la gestion par évènements
	 *  (la position de la souris est commune à tous les évènements, ici)
	 *  mais la gestion des collisions est simplifiée avec cette méthode :
	 *    la gestion des collisions peut se faire en même temps que les graphismes,
	 *    sans nécessiter de gestion séparée
	 */
	public static void refreshFrameListenerEvents() {
		synchronized(mouseEventList_lock) { // attente du lock obligatoire
			// Réinitialisation des paramètres
			mousePressedThisFrame = false;
			mouseReleasedThisFrame = false;
			mouseMovedThisFrame = false;
			// pas de modification pour currentMouseX, currentMouseY si aucun évènement
			// Dans tous les cas, je prends la dernière position connue pour la souris
			for (ListenerMouseEvent mEvent : mouseEventList) {
				
				switch (mEvent.eventType) {
				case MOVED : mouseMovedThisFrame = true; break;
				case PRESSED : mousePressedThisFrame = true; break;
				case RELEASED : mouseReleasedThisFrame = true; break;
				default : break;
				}
				currentMouseX = mEvent.mouseX;
				currentMouseY = mEvent.mouseY;
				
			}
			mouseEventList.clear();
		}
	}
	
	public static int frame_getMouseX() {
		return currentMouseX;
	}
	public static int frame_getMouseY() {
		return currentMouseY;
	}
	public static boolean frame_mousePressed() {
		return mousePressedThisFrame;
	}
	public static boolean frame_mouseReleased() {
		return mouseReleasedThisFrame;
	}
	public static boolean frame_mouseMoved() {
		return mouseMovedThisFrame;
	}
	/** Réinitialiser l'état de la souris sans supprimer les évènements
	 */
	public static void frame_clearMouseSate() {
		mousePressedThisFrame = false;
		mouseReleasedThisFrame = false;
		mouseMovedThisFrame = false;
	}
	
	
	
}



package client.partie.graphique;

import client.listeners.ListenerMouseEvent;
import client.listeners.Listeners;

/** Gère uniquement les évènements en jeu (partie)
 * Faire un autre EventHandler pour les autres salles (connexion, choix partie...)
 * 
 */
public class GameEventHandler {
	
	/** Boucle exécutant les évènements en attente
	 */
	private static boolean staticLoop_iteration() {
		ListenerMouseEvent mouseEvent = Listeners.getMouseEvent();
		if (mouseEvent == null) return false;
		//ListenerMouseEventType.MOVED
		switch (mouseEvent.eventType) {
		case MOVED :
			if (GameHandler.jeuActuel == null) break;
			GameHandler.jeuActuel.mouseMoved(mouseEvent);
			break;
		case PRESSED :
			if (GameHandler.jeuActuel == null) break;
			GameHandler.jeuActuel.mousePressed(mouseEvent);
			break;
		case RELEASED :
			if (GameHandler.jeuActuel == null) break;
			GameHandler.jeuActuel.mouseReleased(mouseEvent);
			break;
		default : break;
		}
		
		return true;
	}

	/** Boucle exécutant les évènements en attente
	 */
	public static void staticLoop() {
		while (staticLoop_iteration());
	}
	
	
	
}

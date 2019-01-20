package client.partie.graphique;

import client.listeners.Listeners;
import client.outils.graphiques.GraphicsHandler;
import client.room1.Room1Handler;
import commun.partie.nonGraphique.ModeDeJeu;

public class MainUnit {
	public static void main(String[] args) throws Exception {
		System.out.println("Lancement de l'application...");
		
		
		//Room1Handler room1Handler = new Room1Handler();
		
		//GameHandler game = new GameHandler(ModeDeJeu.SOLO_LOCAL);
		
		// Créer l'objet gHandler en dernier
		GraphicsHandler gHandler = new GraphicsHandler();
		//room1Handler.setAsCurrentRoom();
		//game.setAsCurrentRoom();
		//gHandler.currentRoomType = RoomType.MENU_CHOIX_TYPE_PARTIE;
		
		
		gHandler.roomGoTo_menuChoixTypePartie();
		gHandler.loop();
		
		
	}
}

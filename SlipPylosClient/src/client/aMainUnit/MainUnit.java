package client.aMainUnit;

import client.outils.graphiques.GraphicsHandler;

public class MainUnit {
	
	// variables réutilisées dans client.roomInternet RoomInternetHandler.java
	
	public static final String internetServerIP = "localhost";//"pylos.jeanpierre.moe";//"192.168.0.23";//"pylos.jeanpierre.moe";//"192.168.0.23";// // = localhost
	public static final int internetServerPort = 3393;
	
	
	public static void main(String[] args) throws Exception {
		System.out.println("Lancement de l'application...");
		
		//Room1Handler room1Handler = new Room1Handler();
		
		//GameHandler game = new GameHandler(ModeDeJeu.SOLO_LOCAL);
		
		// Créer l'objet gHandler en dernier
		GraphicsHandler gHandler = new GraphicsHandler();
		//room1Handler.setAsCurrentRoom();
		//game.setAsCurrentRoom();
		//gHandler.currentRoomType = RoomType.MENU_CHOIX_TYPE_PARTIE;
		
		
		GraphicsHandler.roomGoTo_menuChoixTypePartie();
		gHandler.loop();
		
		
	}
}

package client.partie.graphique;

import commun.partie.nonGraphique.ModeDeJeu;

public class MainUnit {
	public static void main(String[] args) throws Exception {
		System.out.println("Lancement de l'application...");
		
		GameHandler game = new GameHandler(ModeDeJeu.SOLO_LOCAL);
		
		// Créer l'objet gHandler en dernier
		GraphicsHandler gHandler = new GraphicsHandler();
		game.setAsCurrentRoom();
		gHandler.loop();
		
		
	}
}

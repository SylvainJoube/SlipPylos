package partie.graphique;

public class MainUnit {
	public static void main(String[] args) {
		System.out.println("Lancement de l'application...");
		GameHandler game = new GameHandler();
		GameHandler.jeuActuel = game;
		game.initWindow();
		game.gameLoop();
		
		
	}
}

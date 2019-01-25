package server.main;

import java.util.ArrayList;

public class GestionDesParties {
	
	public ArrayList<GestionPartie> a1Partie = new ArrayList<GestionPartie>();
	public static GestionDesParties instance = null;
	
	
	
	public GestionDesParties() {
		if (instance != null) {
			System.err.println("ERREUR GestionDesParties constructeur : une instance existe déjà.");
		}
		GestionDesParties.instance = this;
	}
	
	public void loopParties() {
		
	}
	
}

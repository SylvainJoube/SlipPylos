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
	
	public static void loopGestionParties_static() {
		//if (instance == null) return;
		instance.loopGestionParties();
	}
	
	public void loopGestionParties() {
		int iPartie = 0;
		while (iPartie < a1Partie.size()) {
			GestionPartie gestionPartie = a1Partie.get(iPartie);
			if (gestionPartie.terminee) {
				a1Partie.remove(iPartie);
				continue;
			}
			gestionPartie.loop();
			iPartie++;
		}
	}
	
}

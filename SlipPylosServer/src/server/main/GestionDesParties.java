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
	
	/** Suppression des parties à détruire, loop des parties à garder
	 */
	public void loopGestionParties() {
		int iPartie = 0;
		while (iPartie < a1Partie.size()) {
			GestionPartie gestionPartie = a1Partie.get(iPartie);
			if (gestionPartie.terminee || gestionPartie.doitSupprimerPartieDesQuePossible) {
				a1Partie.remove(iPartie); // le GC supprime aussi la PylosPartie et tout ce qui traine
				continue;
			}
			gestionPartie.loop();
			iPartie++;
		}
	}
	
	public void unJoueurSeDeconnecte(ServerClient joueurDeconnecte) {
		int iPartie = 0;
		while (iPartie < a1Partie.size()) {
			GestionPartie gestionPartie = a1Partie.get(iPartie);
			boolean partieTrouvee = gestionPartie.testerJoueurQuittePartie(joueurDeconnecte);
			if (partieTrouvee) break; // un joueur ne peut être que dans une seule partie (sauf bug...)
			iPartie++;
		}
	}
	
}

package commun.partie.nonGraphique;

import java.util.ArrayList;

import slip.network.buffers.NetBuffer;



public class PylosPartie_coupComplet {
	
	public TeamType equipeQuiJoueLeCoup = TeamType.INVALIDE; // utile pour la sauvegarde/chargement des parties : afficher les coups d'une partie
	
	// 1) Poser de sa réserve ou déplacer un pion
	public int xCell, yCell, hauteur;
	public boolean doitReprendrePionsEnsuite = false; // true si je viens de faire un carré ou une ligne
	public boolean estUnDeplacement = false;
	public int xCell_init = -1,
			   yCell_init = -1, // si c'est un déplacement, uniquement
			   hauteur_init = -1;
	
	public PylosPartie_coupComplet(int arg_hauteur, int arg_xCell, int arg_yCell, boolean arg_estUnDeplacement) {
		hauteur = arg_hauteur;
		xCell = arg_xCell;
		yCell = arg_yCell;
		estUnDeplacement = arg_estUnDeplacement;
	}
	
	public PylosPartie_coupComplet() {
		
	}
	
	
	//int nombreDePionsEconomises = 0; // 1 si déplacer un pion, 0 si poser un pion de sa réserve + 1à2 si reprendre des pions. Donc entre 0 et 3. (3 = bien, 0 = normal/mauvais)
	
	public ArrayList<PylosPartie_coupComplet> listePionsARecuperer = null; // créé si je reprends 1 ou + pions (et donc que doitReprendrePionsEnsuite == true)
	
	public NetBuffer writeToNetBuffer() {
		NetBuffer result = new NetBuffer();
		result.writeInt(hauteur);
		result.writeInt(xCell);
		result.writeInt(yCell);
		result.writeBool(estUnDeplacement);
		if (estUnDeplacement) {
			result.writeInt(hauteur_init);
			result.writeInt(xCell_init);
			result.writeInt(yCell_init);
		}
		
		result.writeBool(doitReprendrePionsEnsuite);
		if (doitReprendrePionsEnsuite) {
			int nbReprendre = listePionsARecuperer.size();
			result.writeInt(nbReprendre);
			for (int iPion = 0; iPion < nbReprendre; iPion++) {
				PylosPartie_coupComplet reprendrePion = listePionsARecuperer.get(iPion);
				result.writeInt(reprendrePion.hauteur);
				result.writeInt(reprendrePion.xCell);
				result.writeInt(reprendrePion.yCell);
			}
		}
		return result;
	}
	
	public static PylosPartie_coupComplet readFromNetBuffer(NetBuffer from) {
		PylosPartie_coupComplet result = new PylosPartie_coupComplet();
		result.hauteur = from.readInt();
		result.xCell = from.readInt();
		result.yCell = from.readInt();
		result.estUnDeplacement = from.readBool();
		if (result.estUnDeplacement) {
			result.hauteur_init = from.readInt();
			result.xCell_init = from.readInt();
			result.yCell_init = from.readInt();
		}
		result.doitReprendrePionsEnsuite = from.readBool();
		if (result.doitReprendrePionsEnsuite) {
			result.listePionsARecuperer = new ArrayList<PylosPartie_coupComplet>();
			int nbReprendre = from.readInt();
			for (int iPion = 0; iPion < nbReprendre; iPion++) {
				PylosPartie_coupComplet reprendrePion = new PylosPartie_coupComplet();
				reprendrePion.hauteur = from.readInt();
				reprendrePion.xCell = from.readInt();
				reprendrePion.yCell = from.readInt();
				result.listePionsARecuperer.add(reprendrePion);
			}
		}
		return result;
	}
	
	
	
	public void drawOnScreen() {
		System.out.println("PylosPartie_coup");
		if (estUnDeplacement) {
			System.out.println(" est un déplacement");
		} else System.out.println(" posé depuis la réserve");
		System.out.println("  position(h, x, y) = " + hauteur + " " + xCell + " " + yCell);
		if (estUnDeplacement)
			System.out.println("  déplacement_de(h, x, y) = " + hauteur_init + " " + xCell_init + " " + yCell_init);
		System.out.println("  doitReprendrePionsEnsuite = " + doitReprendrePionsEnsuite);
		if (listePionsARecuperer != null) for (PylosPartie_coupComplet recuperer : listePionsARecuperer) {
			System.out.println("  Récupérer le pion :");
			System.out.println("    position(h, x, y) = " + recuperer.hauteur + " " + recuperer.xCell + " " + recuperer.yCell);
		}
	}
	
	
}
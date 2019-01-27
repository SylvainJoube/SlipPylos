package commun.partie.nonGraphique;

import java.util.ArrayList;

import slip.network.buffers.NetBuffer;

public class PylosPartie_actionSimple {
	
	public TeamType equipeQuiJoueLeCoup = TeamType.INVALIDE;
	
	// 1) Poser de sa réserve ou déplacer un pion
	public int hauteur, xCell, yCell;
	//public boolean doitReprendrePionsEnsuite = false; // true si je viens de faire un carré ou une ligne
	//public boolean estUnDeplacement = false;
	//public boolean estUneRepriseDePion = false;
	//public boolean estUnePoseDeSaReserve = true; // par défaut
	// Vraie énuméraion non faite pour économiser du TEMPS !
	
	public int typeAction; // 0 poser de sa réserve, 1 déplacement, 2 reprendre un (seul) pion, 3 passer le tour
	/// Un coup complet peut être composé de 3 actionSimple (1 déplacement + 2 reprises)
	
	public int xCell_init = -1,
			   yCell_init = -1, // si c'est un déplacement (typeAction = 1), uniquement
			   hauteur_init = -1;

	
	public PylosPartie_actionSimple() {
		///System.out.println("PylosPartie_actionSimple constructeur 0");
	}
	
	public PylosPartie_actionSimple(int arg_typeAction) { // pas de equipeQuiFaitAction si juste passer le tour
		typeAction = arg_typeAction;
		//System.out.println("PylosPartie_actionSimple constructeur 1 arg_typeAction = " + arg_typeAction);
	}
	
	public PylosPartie_actionSimple(TeamType arg_equipeQuiJoueLeCoup, int arg_typeAction, int arg_hauteur, int arg_xCell, int arg_yCell) {
		typeAction = arg_typeAction;
		hauteur = arg_hauteur;
		xCell = arg_xCell;
		yCell = arg_yCell;
		equipeQuiJoueLeCoup = arg_equipeQuiJoueLeCoup;
		//System.out.println("PylosPartie_actionSimple constructeur 2 typeAction = " + typeAction + " hauteur=" + hauteur + " xCell="+xCell+" yCell="+yCell);
	}
	
	public PylosPartie_actionSimple(TeamType arg_equipeQuiJoueLeCoup, int arg_typeAction, int arg_hauteur, int arg_xCell, int arg_yCell, int arg_hauteur_init, int arg_xCell_init, int arg_yCell_init) {
		typeAction = arg_typeAction;
		hauteur = arg_hauteur;
		xCell = arg_xCell;
		yCell = arg_yCell;
		hauteur_init = arg_hauteur_init;
		xCell_init = arg_xCell_init;
		yCell_init = arg_yCell_init;
		equipeQuiJoueLeCoup = arg_equipeQuiJoueLeCoup;
		//System.out.println("PylosPartie_actionSimple constructeur 3 typeAction = " + typeAction + " hauteur=" + hauteur + " xCell="+xCell+" yCell="+yCell + " hauteur_init=" + hauteur_init + " xCell_init="+xCell_init+" yCell_init="+yCell_init);
	}
	
	public NetBuffer writeToNetBuffer() {
		NetBuffer result = new NetBuffer();
		//result.writeInt(100); // type de message (pour les échanges client <-> serveur
		result.writeInt(typeAction);
		if (typeAction != 3) { // 3 = passer le tour
			result.writeInt(equipeQuiJoueLeCoup.asInt);
			//System.err.println("PylosPartie_actionSimple.writeToNetBuffer equipeAsInt = " + equipeQuiJoueLeCoup.asInt);
			result.writeInt(hauteur);
			result.writeInt(xCell);
			result.writeInt(yCell);
			if (typeAction == 1) { // déplacement
				result.writeInt(hauteur_init);
				result.writeInt(xCell_init);
				result.writeInt(yCell_init);
			}
		}
		return result;
	}
	
	public void writeToNetBuffer(NetBuffer writeToBuff) {
		writeToBuff.writeInt(typeAction);
		if (typeAction != 3) { // 3 = passer le tour
			writeToBuff.writeInt(equipeQuiJoueLeCoup.asInt);
			//System.err.println("PylosPartie_actionSimple.writeToNetBuffer equipeAsInt = " + equipeQuiJoueLeCoup.asInt);
			writeToBuff.writeInt(hauteur);
			writeToBuff.writeInt(xCell);
			writeToBuff.writeInt(yCell);
			if (typeAction == 1) { // déplacement
				writeToBuff.writeInt(hauteur_init);
				writeToBuff.writeInt(xCell_init);
				writeToBuff.writeInt(yCell_init);
			}
		}
	}
	
	public static PylosPartie_actionSimple readFromNetBuffer(NetBuffer from) {
		
		PylosPartie_actionSimple result = new PylosPartie_actionSimple();
		// 100 = from.readInt();
		result.typeAction = from.readInt();
		if (result.typeAction != 3) { // 3 = passer le tour
			int equipeAsInt = from.readInt();
			//System.err.println("PylosPartie_actionSimple.readFromNetBuffer equipeAsInt = " + equipeAsInt);
			result.equipeQuiJoueLeCoup = TeamType.fromInt(equipeAsInt);
			result.hauteur = from.readInt();
			result.xCell = from.readInt();
			result.yCell = from.readInt();
			if (result.typeAction == 1) { // déplacement
				result.hauteur_init = from.readInt();
				result.xCell_init = from.readInt();
				result.yCell_init = from.readInt();
			}
		}
		return result;
	}
	
	/*public void drawOnScreen() {
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
	}*/
	
	
}
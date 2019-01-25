package commun.partie.nonGraphique;

public enum PylosPartieVariante {
	INCONNU(-1),
	POUR_ENFANTS(1),    // enfants : seule règle : "monter un pion"
	NORMAL(2),          // normal : ajout de "former un carré de sa couleur"
	JOUEURS_AVERTIS(3); // avertis : ajout de "former une ligne de sa couleur" (3+ alignés, pas en diagonale)
	
	public final int asInt;
	PylosPartieVariante(int intValue) {
		asInt = intValue;
	}
	
	public static PylosPartieVariante fromInt(int intValue) {
		switch (intValue) {
		case -1 : return INCONNU;
		case 1 : return POUR_ENFANTS;
		case 2 : return NORMAL;
		case 3 : return JOUEURS_AVERTIS;
		default : return INCONNU;
		}
	}
	
	
}

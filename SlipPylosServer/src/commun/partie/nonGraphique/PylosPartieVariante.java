package commun.partie.nonGraphique;

public enum PylosPartieVariante {
	POUR_ENFANTS,    // enfants : seule règle : "monter un pion"
	NORMAL,          // normal : ajout de "former un carré de sa couleur"
	JOUEURS_AVERTIS; // avertis : ajout de "former une ligne de sa couleur" (3+ alignés, pas en diagonale)
}

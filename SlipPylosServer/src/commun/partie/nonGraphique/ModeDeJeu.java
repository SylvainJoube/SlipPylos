package commun.partie.nonGraphique;

public enum ModeDeJeu {
	SOLO_LOCAL, // solo contre une IA
	HOT_SEAT, // contre un autre joueur
	RESEAU_LOCAL, // en réseau local : une personne sera le serveur
	INTERNET; // sur internet, parties classées et non classées, matchmaking
}

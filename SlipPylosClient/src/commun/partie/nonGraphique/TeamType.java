package commun.partie.nonGraphique;

public enum TeamType {
	INVALIDE,
	AUCUNE,
	BLANC,
	NOIR;
	
	public TeamType equipeOpposee() {
		if (this == NOIR) return BLANC;
		if (this == BLANC) return NOIR;
		if (this == AUCUNE) return AUCUNE;
		if (this == INVALIDE) return INVALIDE;
		return INVALIDE;
	}
}

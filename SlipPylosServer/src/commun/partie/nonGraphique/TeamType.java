package commun.partie.nonGraphique;

public enum TeamType {
	INVALIDE(-1),
	AUCUNE(0),
	BLANC(1),
	NOIR(2);
	
	public TeamType equipeOpposee() {
		if (this == NOIR) return BLANC;
		if (this == BLANC) return NOIR;
		if (this == AUCUNE) return AUCUNE;
		if (this == INVALIDE) return INVALIDE;
		return INVALIDE;
	}
	
	public final int asInt;
	TeamType(int arg_asInt) {
		asInt = arg_asInt;
	}
	
	public final String asString() {
		if (this == NOIR) return "noir";
		if (this == BLANC) return "blanc";
		if (this == AUCUNE) return "aucune";
		if (this == INVALIDE) return "invalide";
		return "invalide";
	}
	
	
	public static TeamType fromInt(int arg_fromInt) {
		switch (arg_fromInt) {
		case -1 : return TeamType.INVALIDE;
		case 0 : return TeamType.AUCUNE;
		case 1 : return TeamType.BLANC;
		case 2 : return TeamType.NOIR;
		default : return TeamType.INVALIDE;
		}
	}
	
}

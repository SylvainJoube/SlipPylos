package commun.partie.nonGraphique;

public class PylosPartie_evaluationCoup {
	public boolean doitReprendreDesPions;
	public boolean peutPoserIci;
	
	public PylosPartie_evaluationCoup(boolean arg_doitReprendreDesPions, boolean arg_peutPoserIci) {
		doitReprendreDesPions = arg_doitReprendreDesPions;
		peutPoserIci = arg_peutPoserIci;
	}
	
	public PylosPartie_evaluationCoup() {
		doitReprendreDesPions = false;
		peutPoserIci = false;
	}
}

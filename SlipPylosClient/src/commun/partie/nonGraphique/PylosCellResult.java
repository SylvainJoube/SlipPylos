package commun.partie.nonGraphique;


/**
 * 
 * 
 *
 */
public class PylosCellResult extends PylosCell {
	
	//public TeamType occupeePar; // pas occupée
	public boolean peutPoserIci = false; // pas accessible (accessible = 4 blocs au-dessous)
	
	public PylosCellResult(int arg_xCell, int arg_yCell, int arg_hauteur, TeamType arg_occupeePar, boolean arg_peutPoserIci) {
		super(arg_xCell, arg_yCell, arg_hauteur);
		occupeePar = arg_occupeePar;
		peutPoserIci = arg_peutPoserIci;
	}
	
	public PylosCellResult(PylosCell inheritFromCell, boolean arg_peutPoserIci) {
		super(inheritFromCell.xCell, inheritFromCell.yCell, inheritFromCell.hauteur);
		occupeePar = inheritFromCell.occupeePar;
		peutPoserIci = arg_peutPoserIci;
	}
	
	/*public PylosCell getGameCell() {
		return this;
		//PylosCell result = new PylosCell(xCell);
	}*/
	
}

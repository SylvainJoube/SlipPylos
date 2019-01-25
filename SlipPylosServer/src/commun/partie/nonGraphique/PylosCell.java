package commun.partie.nonGraphique;

/**
 * 
 * 
 *
 */

public class PylosCell {
	public int hauteur; // niveau dans la grille de jeu : 0 (directement sur le plateau), 1 (sur les premi�res boules), 2 (sur les boules 1), ...
	public int xCell, yCell; // position de la case dans la grille
	public TeamType occupeePar; // équipe qui a cette case
	
	public PylosCell(int arg_xCell, int arg_yCell, int arg_hauteur) {
		xCell = arg_xCell;
		yCell = arg_yCell;
		hauteur = arg_hauteur;
		occupeePar = TeamType.AUCUNE;
	}
	
	public boolean estIdentique(PylosCell compareTo) {
		if (compareTo == null)
			return false;
		if ((compareTo.xCell == xCell)
		&& (compareTo.yCell == yCell)
		&& (compareTo.hauteur == hauteur)) {
			return true;
		}
		return false;
	}
	/**
	 * Copie cette instance de PylosCell.
	 * @return une PylosCell identique � celle-ci.
	 */
	public PylosCell copy() {
		PylosCell result = new PylosCell(xCell, yCell, hauteur);
		result.occupeePar = this.occupeePar;
		return result;
	}
}
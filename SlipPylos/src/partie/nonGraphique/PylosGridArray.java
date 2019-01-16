package partie.nonGraphique;

/**
 * 
 * Tableau de Grille de pylos : chaque grille dans la liste de PylosGrid a une hauteur donn�e
 *
 */
public class PylosGridArray {
	
	public int nbCasesCoteBase;
	public PylosGrid[] a1Grid; // Grille, � hautreur fix�e
	
	/**
	 * Retourne la hauteur maximale du plateau (commen�ant � 0) soit l'index de la grille la plus haute.
	 * @return hauteur maximale du plateau. 0 veut dire une seule couche de haut, 1 deux couches de haut etc.
	 */
	public int getHauteurMax() {
		return nbCasesCoteBase - 1;
	}
	
	/**
	 * Constructeur.
	 * @param arg_nbCasesCoteBase nombre de cases de c�t� � la base de la pyramide (nb de cases de c�t� du plateau)
	 */
	public PylosGridArray(int arg_nbCasesCoteBase) {
		this(arg_nbCasesCoteBase, true);
		/*nbCasesCoteBase = arg_nbCasesCoteBase;
		a1Grid = new PylosGrid[nbCasesCoteBase];
		int hauteurMax = getHauteurMax();
		for (int hauteurActuelle = 0; hauteurActuelle <= hauteurMax; hauteurActuelle++) {
			a1Grid[hauteurActuelle] = new PylosGrid(nbCasesCoteBase - hauteurActuelle, nbCasesCoteBase - hauteurActuelle, hauteurActuelle, this);
		}*/
	}

	/**
	 * Overload du constructeur, pour ne pas initialiser les grilles (c'est inutile dans le cas d'une copie par exemple)
	 * @param arg_nbCasesCoteBase nombre de cases de la base de la pyramide
	 * @param initialiseGrids initialiser la grille (true) ou non (false). Le tableau est toujours initialis� � la bonne taille.
	 */
	public PylosGridArray(int arg_nbCasesCoteBase, boolean initialiseGrids) {
		nbCasesCoteBase = arg_nbCasesCoteBase;
		a1Grid = new PylosGrid[nbCasesCoteBase]; // toujours initialis�
		if (initialiseGrids) { // seulement cr�er les grilles si besoin
			int hauteurMax = getHauteurMax();
			for (int hauteurActuelle = 0; hauteurActuelle <= hauteurMax; hauteurActuelle++) {
				a1Grid[hauteurActuelle] = new PylosGrid(nbCasesCoteBase - hauteurActuelle, nbCasesCoteBase - hauteurActuelle, hauteurActuelle, this);
			}
		}
	}
	
	/**
	 * Attribuer une �quipe � une cellule donn�e.
	 * @param gridHeight hauteur (= index) de la grille sur laquelle placer la case
	 * @param xCell position X dans la grille
	 * @param yCell position Y dans la grille
	 * @param teamType type de l'�quipe � placer ici
	 */
	public void setCell(int gridHeight, int xCell, int yCell, TeamType teamType) {
		if (gridHeight < 0) {
			Log.write("ERREUR PylosGridArray.setCell : gridHeight = " + Integer.toString(gridHeight) + " < 0 ill�gal.");
			return;
		}
		if (gridHeight > getHauteurMax()) {
			Log.write("ERREUR PylosGridArray.setCell : gridHeight = " + Integer.toString(gridHeight) + " > getHauteurMax() = " + Integer.toString(getHauteurMax()));
			return;
		}
		PylosGrid grid = a1Grid[gridHeight];
		grid.setCell(xCell, yCell, teamType);
	}
	
	/**
	 * Copie le tableau de grilles, typiquement utile pour l'IA et la simulation des futurs �tats possibles du jeu.
	 * Tous les objets sont copi�s.
	 * @return une instance de PylosGridArray identique � celle-ci, tous les objets la consitituant sont copi�s.
	 */
	public PylosGridArray copy() {
		PylosGridArray result = new PylosGridArray(nbCasesCoteBase);
		for (int haut = 0; haut <= getHauteurMax(); haut++) {
			result.a1Grid[haut] = this.a1Grid[haut].copy(result);
		}
		return result;
	}
	
	/**
	 * 
	 * @param hauteur la heuteur de la grille, commen�ant � 0 (index)
	 * @param xCell
	 * @param yCell
	 * @param teamTypeAtCellPosition
	 * @return
	 */
	public boolean willFormSameColorRectangle(int hauteur, int xCell, int yCell, TeamType teamTypeAtCellPosition) {
		if (hauteur < 0) return false;
		if (hauteur >= getHauteurMax()) return false;
		PylosGrid grille = a1Grid[hauteur];
		return grille.willFormSameColorRectangle(xCell, yCell, teamTypeAtCellPosition);
	}
	
}

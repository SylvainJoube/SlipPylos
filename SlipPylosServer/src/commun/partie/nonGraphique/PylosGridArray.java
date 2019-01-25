package commun.partie.nonGraphique;

/**
 * 
 * Tableau de Grille de pylos : chaque grille dans la liste de PylosGrid a une hauteur donnée
 *
 */
public class PylosGridArray {
	
	public int nbCasesCoteBase;
	public PylosGrid[] a1Grid; // Grille, hautreur fixée
	private PylosPartie partieActuelle;
	
	/** Retourne la hauteur maximale du plateau (commençant à 0) soit l'index de la grille la plus haute.
	 *  @return hauteur maximale du plateau. 0 veut dire une seule couche de haut, 1 deux couches de haut etc.
	 */
	public int getHauteurMax() {
		return nbCasesCoteBase - 1;
	}
	
	/** Constructeur.
	 *  @param arg_nbCasesCoteBase nombre de cases de côté à la base de la pyramide (nb de cases de c�t� du plateau)
	 */
	public PylosGridArray(int arg_nbCasesCoteBase, PylosPartie arg_partieActuelle) {
		this(arg_nbCasesCoteBase, true, arg_partieActuelle);
		/*nbCasesCoteBase = arg_nbCasesCoteBase;
		a1Grid = new PylosGrid[nbCasesCoteBase];
		int hauteurMax = getHauteurMax();
		for (int hauteurActuelle = 0; hauteurActuelle <= hauteurMax; hauteurActuelle++) {
			a1Grid[hauteurActuelle] = new PylosGrid(nbCasesCoteBase - hauteurActuelle, nbCasesCoteBase - hauteurActuelle, hauteurActuelle, this);
		}*/
	}

	/** Overload du constructeur, pour ne pas initialiser les grilles (c'est inutile dans le cas d'une copie par exemple)
	 *  @param arg_nbCasesCoteBase nombre de cases de la base de la pyramide
	 *  @param initialiseGrids initialiser la grille (true) ou non (false). Le tableau est toujours initialis� � la bonne taille.
	 */
	public PylosGridArray(int arg_nbCasesCoteBase, boolean initialiseGrids, PylosPartie arg_partieActuelle) {
		nbCasesCoteBase = arg_nbCasesCoteBase;
		partieActuelle = arg_partieActuelle;
		a1Grid = new PylosGrid[nbCasesCoteBase]; // toujours initialis�
		if (initialiseGrids) { // seulement cr�er les grilles si besoin
			int hauteurMax = getHauteurMax();
			for (int hauteurActuelle = 0; hauteurActuelle <= hauteurMax; hauteurActuelle++) {
				a1Grid[hauteurActuelle] = new PylosGrid(nbCasesCoteBase - hauteurActuelle, nbCasesCoteBase - hauteurActuelle, hauteurActuelle, this);
			}
		}
	}
	
	/** Attribuer une équipe à une cellule donnée.
	 *  @param gridHeight hauteur (= index) de la grille sur laquelle placer la case
	 *  @param xCell position X dans la grille
	 *  @param yCell position Y dans la grille
	 *  @param teamType type de l'équipe à placer ici
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
	
	/** Copie le tableau de grilles, typiquement utile pour l'IA et la simulation des futurs états possibles du jeu.
	 *  Tous les objets sont copiés.
	 *  @return une instance de PylosGridArray identique à celle-ci, tous les objets la consitituant sont copi�s.
	 */
	public PylosGridArray copy(PylosPartie arg_partieActuelle) {
		PylosGridArray result = new PylosGridArray(nbCasesCoteBase, arg_partieActuelle);
		for (int haut = 0; haut <= getHauteurMax(); haut++) {
			result.a1Grid[haut] = this.a1Grid[haut].copy(result);
		}
		return result;
	}
	
	/**
	 * 
	 * @param hauteur la hauteur de la grille, commençant à 0 (index)
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
	
	public boolean willFormSameColorLine(int hauteur, int xCell, int yCell, TeamType teamTypeAtCellPosition) {
		if (hauteur < 0) return false;
		if (hauteur >= getHauteurMax() - 1) return false; // seuls les lignes de 3+ de long sont valides (exit dernière hauteur et avant-dernière) (Pylos classique : 3 et 4 valides)
		PylosGrid grille = a1Grid[hauteur];
		return grille.willFormSameColorLine(xCell, yCell, teamTypeAtCellPosition);
	}
	
	
	public boolean isValidPawnPosition(int hauteur, int xCell, int yCell) {
		if (hauteur < 0) return false;
		if (hauteur >= a1Grid.length) return false;
		PylosGrid grid = a1Grid[hauteur];
		if (xCell < 0 || yCell < 0 || xCell >= grid.gridWidth || yCell >= grid.gridHeight) return false;
		return true; // toutes les conditions sont passées avec succès
	}
	public boolean isValidPawnPosition(PylosCell checkCell) {
		if (checkCell == null) return false;
		return isValidPawnPosition(checkCell.hauteur, checkCell.xCell, checkCell.yCell);
	}
	
	/**Regarde s'il est possible de bouger ce pion :
	 *  - regarde s'il existe bien sur le plateau
	 *  - regarde s'il ne soutient aucune bille
	 *  - regarde si le pion que je veux déplacer ne va pas s'appuier sur son ancienne position
	 * @param checkCell
	 * @param fromCell
	 * @return
	 */
	public boolean canMovePawn(PylosCell checkCell, PylosCell fromCell) {
		if (fromCell == null) {
			//System.out.println("PylosGridArray.canMovePawn : fromCell == null");
			return canMovePawn(checkCell.hauteur, checkCell.xCell, checkCell.yCell, -1, -1, -1);
		} else {
			//System.out.println("PylosGridArray.canMovePawn : fromCell != null");
			return canMovePawn(checkCell.hauteur, checkCell.xCell, checkCell.yCell, fromCell.hauteur, fromCell.xCell, fromCell.yCell);
		}
	}
	/** Regarde s'il est possible de bouger ce pion :
	 *  - regarde s'il existe bien sur le plateau
	 *  - regarde s'il ne soutient aucune bille
	 *  Et si initial_... != -1 :
	 *    - regarde si le pion que je veux déplacer ne va pas s'appuier sur son ancienne position
	 * @param hauteur commence (toujours) de 0 (la hauteur le plateau)
	 * @param hauteur
	 * @param xCell
	 * @param yCell
	 * @param initial_hauteur
	 * @param initial_xCell
	 * @param initial_yCell
	 * @return
	 */
	public boolean canMovePawn(int hauteur, int xCell, int yCell, int initial_hauteur, int initial_xCell, int initial_yCell) {
		
		if ( ! isValidPawnPosition(hauteur, xCell, yCell) ) return false;
		
		// Vérification de la hauteur : si il y a une case initiale, sa hauteur doit être strictement inférieure à la nouvelle hauteur
		if (initial_hauteur >= 0) { // i.e. != -1
			if (initial_hauteur >= hauteur) return false; // la hauteur initiale doit être inférieure strictement à la nouvelle hauteur
		}
		
		// Je vérifie qu'il n'y a pas de pions au-dessus
		if (hauteur + 1 >= a1Grid.length) return false; // impossible de bouger le denier pion !
		PylosGrid aboveGrid = a1Grid[hauteur + 1];
		// Liste des cases au-dessus
		PylosCell[] a1CheckCell = new PylosCell[4];
		a1CheckCell[0] = new PylosCell(xCell - 1, yCell - 1, hauteur + 1); // haut gauche
		a1CheckCell[1] = new PylosCell(xCell - 1, yCell    , hauteur + 1); // haut droite
		a1CheckCell[2] = new PylosCell(xCell    , yCell - 1, hauteur + 1); // bas gauche
		a1CheckCell[3] = new PylosCell(xCell    , yCell    , hauteur + 1); // bas droite
		// Vérification que toutes les cases au-dessus ne sont pas prises
		for (int iCell = 0; iCell < a1CheckCell.length; iCell++) {
			PylosCell currentCell = a1CheckCell[iCell];
			TeamType teamAtPos = aboveGrid.getTeamAtCellPosition(currentCell.xCell, currentCell.yCell);
			if (teamAtPos != TeamType.AUCUNE && teamAtPos != TeamType.INVALIDE) { // == NOIR || == BLANC ici
				return false;
			}
		}
		// Maintenant, je regarde si le pion que je veux déplacer ne va pas s'appuier sur son ancienne position
		if (initial_hauteur != -1 && initial_xCell != -1 && initial_yCell != -1) {
			if (hauteur == 0) return false; // impossible de déplacer un pion vers la hauteur 0 (toujours 1+)
			// Liste des cases au-dessous
			a1CheckCell = new PylosCell[4];
			a1CheckCell[0] = new PylosCell(xCell    , yCell    , hauteur - 1); // haut gauche  au passage, on a pas forcément hauteur-1 == initial_hauteur, hauteur-2 == initial_hauteur est aussi possible
			a1CheckCell[1] = new PylosCell(xCell    , yCell + 1, hauteur - 1); // bas gauche
			a1CheckCell[2] = new PylosCell(xCell + 1, yCell    , hauteur - 1); // haut droite
			a1CheckCell[3] = new PylosCell(xCell + 1, yCell + 1, hauteur - 1); // bas droite
			// Je vérifie donc que je ne déplace pas une bille pour la mettre au-dessus d'elle-même
			for (int iCell = 0; iCell < a1CheckCell.length; iCell++) {
				PylosCell currentCell = a1CheckCell[iCell];
				if (currentCell.hauteur == initial_hauteur && currentCell.xCell == initial_xCell && currentCell.yCell == initial_yCell)
					return false;
			}
		}
		return true;
	}
	
	
	// Retourne true si la hauteur est valide, que la case est libre et qu'il y a 4 boules au-dessous
	public boolean canPlaceAtPosition(int hauteur, int xCell, int yCell) {
		if (hauteur < 0) return false; // trop bas
		if (hauteur >= a1Grid.length) return false; // trop haut
		PylosGrid grid = a1Grid[hauteur];
		return grid.canPlaceAtPosition(xCell, yCell);
	}
	
	/** N'utiliser cette fonction que depui un objet PylosPartie (dans la fonction )
	 *  ou depuis un objet IA. Cette fonction met juste à jour les cases, sans aucune vérification ni crédit/débit de piont à l'équipe qui joue.
	 *  @param equipeQuiFaitAction
	 *  @param hauteur
	 *  @param xCell
	 *  @param yCell
	 *  @param hauteur_initiale
	 *  @param xCell_initiale
	 *  @param yCell_initiale
	 */
	public void deplacerUnPion_forcerDepuisPartie(TeamType equipeQuiFaitAction, int hauteur, int xCell, int yCell, int hauteur_initiale, int xCell_initiale, int yCell_initiale) {
		// Je supprime l'ancien pion
		setCell(hauteur_initiale, xCell_initiale, yCell_initiale, TeamType.AUCUNE);
		// J'ajoute le nouveau pion
		setCell(hauteur, xCell, yCell, equipeQuiFaitAction);
	}
	
	public TeamType getCellTeam(int gridHeight, int xCell, int yCell) {
		if (isValidPawnPosition(gridHeight, xCell, yCell) == false) return TeamType.INVALIDE;
		PylosGrid grid = a1Grid[gridHeight];
		return grid.getTeamAtCellPosition_noCheck(xCell, yCell);
	}
	
}

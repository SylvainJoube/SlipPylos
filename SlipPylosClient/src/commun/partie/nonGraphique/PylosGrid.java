package commun.partie.nonGraphique;

/**
 * Grille de jeu, à une hauteur donnée
 * - A un tableau des cases, les cases à cette hauteur
 *
 */
public class PylosGrid {
	public int hauteur; // 0 - * (0 - 3 pour du 4x4)
	public int gridWidth, gridHeight;
	public PylosCell[][] a2Cell;
	//public PylosPartie partie; // Partie actuelle, avec toutes les variables utiles et l'état du jeu
	public PylosGridArray pylosGridArray;
	
	
	public PylosGrid(int arg_width, int arg_height, int arg_hauteur, PylosGridArray arg_pylosGridArray) { /* PylosPartie arg_partie*/
		gridWidth = arg_width;
		gridHeight = arg_height;
		hauteur = arg_hauteur;
		pylosGridArray = arg_pylosGridArray;
		//partie = arg_partie;
		a2Cell = new PylosCell[gridWidth][gridHeight];
		for (int xCell = 0; xCell < gridWidth; xCell++)
		for (int yCell = 0; yCell < gridHeight; yCell++) {
			a2Cell[xCell][yCell] = new PylosCell(xCell, yCell, hauteur);
		}
	}
	
	// Equipe qui occupe cette cellule : sans vérification de la validité de la position
	public TeamType getTeamAtCellPosition_noCheck(int xCell, int yCell) {
		PylosCell currentCell = a2Cell[xCell][yCell];
		return currentCell.occupeePar;
	}
	
	public TeamType getTeamAtCellPosition(int xCell, int yCell) {
		if (!isValidCellPosition(xCell, yCell)) return TeamType.INVALIDE; // et non null
		PylosCell currentCell = a2Cell[xCell][yCell];
		return currentCell.occupeePar;
	}
	
	// Savoir si la case est libre
	public boolean cellIsFree(int xCell, int yCell) {
		if (getTeamAtCellPosition(xCell, yCell) == TeamType.AUCUNE)
			return true;
		return false;
	}
	
	// Retourne true si la case est libre et qu'il y a 4 boules au-dessous
	public boolean canPlaceAtPosition(int xCell, int yCell) {
		if (getTeamAtCellPosition(xCell, yCell) != TeamType.AUCUNE) return false; // case d�j� prise
		
		//System.out.println("PylosGrid.canPlaceAtPosition hauteur = " + hauteur);
		if (hauteur == 0) return true; // case non prise et au-dessous, c'est le plateau : OK
		
		// Je regarde s'il y a bien les 4 boules n�cessaires en-dessous
		// C'est à dire, pour la grille en dessous :
		//System.out.println("2PylosGrid.canPlaceAtPosition partie.a1Grid... h-1 = " + (hauteur - 1) + " xCell = "+xCell+" yCell="+yCell);
		PylosGrid underlyingGrid = pylosGridArray.a1Grid[hauteur - 1];
		//System.out.println("PylosGrid.canPlaceAtPosition partie.a1Grid...");
		
		if (underlyingGrid.cellIsFree(xCell, yCell)) return false;
		if (underlyingGrid.cellIsFree(xCell+1, yCell)) return false;
		if (underlyingGrid.cellIsFree(xCell, yCell+1)) return false;
		if (underlyingGrid.cellIsFree(xCell+1, yCell+1)) return false;
		return true; // les 4 cellules du dessous sont prises, c'est bon !
		
	}
	
	public void setCell_noCheck(int xCell, int yCell, TeamType teamType) {
		a2Cell[xCell][yCell].occupeePar = teamType;
	}
	
	/*
	/** Retourne true si le pion peut être bougé : s'il ne soutient pas une autre bille, 
	 * @return
	 * /
	public boolean canMovePawn(PylosCell checkCell) {
		// 1) Je regarde si cette cellule est bien valide
		
	}*/
	
	public boolean setCell(int xCell, int yCell, TeamType teamType) {
		if (!isValidCellPosition(xCell, yCell)) return false;
		a2Cell[xCell][yCell].occupeePar = teamType;
		return true;
	}
	
	public boolean isValidCellPosition(int xCell, int yCell) {
		if (xCell < 0) return false;
		if (yCell < 0) return false;
		if (xCell >= gridWidth) return false;
		if (yCell >= gridHeight) return false;
		return true;
	}
	
	/**
	 * Copier cette grille et en retourner une nouvelle.
	 * @param arg_newPylosGridArray l'objet PylosGridArray auquel appartient cette grille
	 * @return une nouvelle grille, identique à cette instance (copie des objets cellule �galement)
	 */
	public PylosGrid copy(PylosGridArray arg_newPylosGridArray) {
		PylosGrid result = new PylosGrid(gridWidth, gridHeight, hauteur, arg_newPylosGridArray);
		for (int xCell = 0; xCell < gridWidth; xCell++)
		for (int yCell = 0; yCell < gridHeight; yCell++) {
			result.a2Cell[xCell][yCell] = a2Cell[xCell][yCell].copy();
		}
		return result;
	}
	
	public boolean willFormSameColorRectangle(int xCell, int yCell, TeamType teamTypeAtCellPosition) {
		if (!isValidCellPosition(xCell, yCell)) return false;
		// Je fais comme si le pion était posé, qu'il le soit ou non
		boolean okGauche = (getTeamAtCellPosition(xCell - 1, yCell) == teamTypeAtCellPosition);
		boolean okDroite = (getTeamAtCellPosition(xCell + 1, yCell) == teamTypeAtCellPosition);
		boolean okHaut = (getTeamAtCellPosition(xCell, yCell - 1) == teamTypeAtCellPosition);
		boolean okBas = (getTeamAtCellPosition(xCell, yCell + 1) == teamTypeAtCellPosition);

		if (okGauche) {
			// Haut gauche
			if (okHaut && (getTeamAtCellPosition(xCell - 1, yCell - 1) == teamTypeAtCellPosition))
				return true;
			// Bas gauche
			if (okBas && (getTeamAtCellPosition(xCell - 1, yCell + 1) == teamTypeAtCellPosition))
				return true;
		}
		if (okDroite) {
			// Haut droit
			if (okHaut && (getTeamAtCellPosition(xCell + 1, yCell - 1) == teamTypeAtCellPosition))
				return true;
			// Bas droit
			if (okBas && (getTeamAtCellPosition(xCell + 1, yCell + 1) == teamTypeAtCellPosition))
				return true;
		}
		return false;
	}
	
	
	
}

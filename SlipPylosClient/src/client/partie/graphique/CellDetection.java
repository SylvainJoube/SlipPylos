package client.partie.graphique;


import commun.partie.nonGraphique.*;

/**
 * 
 * [Partie graphique]
 * Classe utile � la d�tection de quelle case est sous la souris � un instant donn�.
 * @author admin
 *
 */

public class CellDetection {
	
	public static int cellWidth = 100; // largeur d'une case de jeu
	public static int cellHeight = 100; // hauteur d'une case de jeu
	
	/**
	 * 
	 * @param mousePos    position de la souris � l'�cran
	 * @param baseGridPos position de la grille de base (hauteur 0) � l'�cran
	 * @return
	 */
	public static PylosCell getCellUnderMouse_atFixedHeight(PylosPoint mousePos, PylosPoint baseGridPos, PylosPartie currentGame, int hauteurActuelle) {
		//PylosCell result = null;
		//System.out.println("hauteurActuelle = " + hauteurActuelle);
		// Calcul de la position de la souris dans la grille de hauteur 0
		PylosPoint mousePosInBaseGrid = new PylosPoint();
		mousePosInBaseGrid.x = mousePos.x - baseGridPos.x;
		mousePosInBaseGrid.y = mousePos.y - baseGridPos.y;
		// Je regarde si la souris est bien dans la grille
		
		// Nombre de c�t�s de la grille actuelle (hauteurActuelle)
		int nbCasesCoteGrilleActuelle = currentGame.nbCasesCote - hauteurActuelle;
		
		// nombre de cellules de large et de haut de la grille actuelle
		int gridWidthInCells  = nbCasesCoteGrilleActuelle;
		int gridHeightInCells = nbCasesCoteGrilleActuelle;
		
		// Position de départ de la grille de hauteur hauteurActuelle
		int gridStartPosX = baseGridPos.x + hauteurActuelle * (cellWidth / 2);
		int gridStartPosY = baseGridPos.y + hauteurActuelle * (cellHeight / 2);
		// derni�re case de la grille
		int gridStopPosX = gridStartPosX + cellWidth * (gridWidthInCells + 1); // derni�re case + 1
		int gridStopPosY = gridStartPosY + cellHeight * (gridHeightInCells + 1);

		if (mousePos.x < gridStartPosX) return null; // hors de la grille à gauche
		if (mousePos.y < gridStartPosY) return null; // hors de la grille en haut
		if (mousePos.x > gridStopPosX) return null; // hors de la grille à droite
		if (mousePos.y > gridStopPosY) return null; // hors de la grille en bas

		int xCellInThisGrid = (mousePos.x - gridStartPosX) / cellWidth;
		int yCellInThisGrid = (mousePos.y - gridStartPosY) / cellHeight;

		//System.out.println("CellDetection.getCellUnderMouse_atFixedHeight : hauteurActuelle = " + hauteurActuelle);
		PylosGrid grilleActuelle = currentGame.plateauActuel.a1Grid[hauteurActuelle];
		
		if (xCellInThisGrid < 0) return null;
		if (yCellInThisGrid < 0) return null;

		if (xCellInThisGrid >= grilleActuelle.gridWidth) return null;
		if (yCellInThisGrid >= grilleActuelle.gridHeight) return null;
		

		//System.out.println("CellDetection.getCellUnderMouse_atFixedHeight : xCellInThisGrid = " + xCellInThisGrid + "  yCellInThisGrid = " + yCellInThisGrid);
		PylosCell celluleActuelle = grilleActuelle.a2Cell[xCellInThisGrid][yCellInThisGrid];
		//System.out.println("------ CellDetection.getCellUnderMouse_atFixedHeight : OK celule trouv�e - hauteurActuelle = " + hauteurActuelle + " celluleActuelle.hauteur = " + celluleActuelle.hauteur);
		
		// si la cellule actuelle est prise, je regarde la grille de niveau sup�rieur
		return celluleActuelle;
	}
	
	
	
	
	/**
	 * Cherche la case en dessous de la souris, en prenant en compte l'état actuel du jeu
	 * @param mousePos
	 * @param baseGridPos
	 * @param currentGame
	 * @return
	 */
	public static PylosCellResult getCellUnderMouse(PylosPoint mousePos, PylosPoint baseGridPos, PylosPartie currentGame, TeamType equipeRecherchee, int hauteurMinimaleRecherchee) {
		
		/* -> Je recherche la cellule sur laquelle je suis :
		 * Je regarde la case à la hauteur h, si elle est dans la grille et qu'elle est libre, je la prends
		 *   si elle n'est pas dans la grille, je sors de la boucle
		 *   si elle est dans la grille mais pas libre, je la retiens et je passe à la hauteur h+1
		 */
		PylosCell lastValidCell = null;
		
		if (equipeRecherchee == TeamType.AUCUNE) { // recherche 
			
			for (int hauteur = 0; hauteur <= currentGame.hauteurMax; hauteur++) {
				PylosCell currentCellUnderMouse = getCellUnderMouse_atFixedHeight(mousePos, baseGridPos, currentGame, hauteur);
				// Si je ne trouve aucune case sous la souris, c'est que je suis hors de la grille de hauteur height, je m'arr�te
				if (currentCellUnderMouse == null)
					break;
				//if (currentCellUnderMouse.hauteur != hauteur)
				//	System.out.println("ERREUR      CellDetection.getCellUnderMouse hauteur = " + hauteur + " != currentCellUnderMouse.hauteur = " +currentCellUnderMouse.hauteur);
				lastValidCell = currentCellUnderMouse;
				// Si la case est libre, je retourne cette case via lastValidCell
				if (currentCellUnderMouse.occupeePar == equipeRecherchee) // equipeRecherchee = TeamType.AUCUNE
					break;
			}
		} else {
			for (int hauteur = currentGame.hauteurMax; hauteur >= 0; hauteur--) {
				PylosCell currentCellUnderMouse = getCellUnderMouse_atFixedHeight(mousePos, baseGridPos, currentGame, hauteur);
				// Si je ne trouve aucune case sous la souris, c'est que je suis hors de la grille de hauteur height, je m'arr�te
				if (currentCellUnderMouse == null)
					continue;
				//if (currentCellUnderMouse.hauteur != hauteur)
				//	System.out.println("ERREUR      CellDetection.getCellUnderMouse hauteur = " + hauteur + " != currentCellUnderMouse.hauteur = " +currentCellUnderMouse.hauteur);
				lastValidCell = currentCellUnderMouse;
				// Si la case est libre, je retourne cette case via lastValidCell
				if (currentCellUnderMouse.occupeePar == equipeRecherchee) // equipeRecherchee = TeamType.AUCUNE
					break;
			}
		}
		
		
		if (lastValidCell == null) return null; // aucune case valide trouvée
		if (lastValidCell.occupeePar != equipeRecherchee) return null;

		PylosGrid currentGrid = currentGame.plateauActuel.a1Grid[lastValidCell.hauteur];
		//System.out.println("CellDetection.getCellUnderMouse 3 lastValidCell.hauteur = " + lastValidCell.hauteur + " currentGrid.hauteur = " + currentGrid.hauteur);
		
		
		boolean peutPoserIci = currentGrid.canPlaceAtPosition(lastValidCell.xCell, lastValidCell.yCell);
		if (hauteurMinimaleRecherchee >= 0 && peutPoserIci) // s'il y a une condition sur la hauteur de recherche
		if (lastValidCell.hauteur < hauteurMinimaleRecherchee) { // si je suis en dessous de cette heuteur minimale, je ne peux pas poser ici
			peutPoserIci = false;
		}
		PylosCellResult result = new PylosCellResult(lastValidCell, peutPoserIci);
		
		return result;
	}
	
	/*
	private static PylosCell getCellUnderMouse_loopIteration(int hauteur, PylosPoint mousePos, PylosPoint baseGridPos, PylosPartie currentGame, TeamType equipeRecherchee) {
		
		PylosCell currentCellUnderMouse = getCellUnderMouse_atFixedHeight(mousePos, baseGridPos, currentGame, hauteur);
		// Si je ne trouve aucune case sous la souris, c'est que je suis hors de la grille de hauteur height, je m'arr�te
		if (currentCellUnderMouse == null)
			break;
		if (currentCellUnderMouse.hauteur != hauteur)
			System.out.println("ERREUR      CellDetection.getCellUnderMouse hauteur = " + hauteur + " != currentCellUnderMouse.hauteur = " +currentCellUnderMouse.hauteur);
		lastValidCell = currentCellUnderMouse;
		// Si la case est libre, je retourne cette case via lastValidCell
		if (currentCellUnderMouse.occupeePar == equipeRecherchee) // equipeRecherchee = TeamType.AUCUNE
			break;
		
	}*/
	
	
	public static PylosPoint getPosInGridFromCellRes(PylosCellResult inputCell) {
		//outputResult = null;
		//if (inputCell == null) return null;
		int xOffset = getGridXOffsetPx(inputCell.hauteur);//inputCell.hauteur * cellWidth / 2;
		int yOffset = getGridYOffsetPx(inputCell.hauteur);//inputCell.hauteur * cellHeight / 2;

		PylosPoint result = new PylosPoint();
		result.x = inputCell.xCell * cellWidth + xOffset;
		result.y = inputCell.yCell * cellHeight + yOffset;
		
		return result;
	}

	public static int getGridXOffsetPx(int hauteurActuelle) {
		return hauteurActuelle * cellWidth / 2;
	}
	public static int getGridYOffsetPx(int hauteurActuelle) {
		return hauteurActuelle * cellHeight / 2;
	}
	
}













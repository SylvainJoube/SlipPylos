package commun.partie.nonGraphique;

import java.util.ArrayList;

public class IA_v0_stupid {
	
	
	
	public static void playOnce(PylosPartie partie, TeamType monEquipe, int profondeurDeRecherche) {
		
		//IA_v0_stupid ia = new IA_v0_stupid();
		ArrayList<PylosCell> a1PlayableCell = getPlayableCells(partie.plateauActuel);
		PylosCell choosenCell = null;
		if (a1PlayableCell.size() == 0) return;
		choosenCell = a1PlayableCell.get(0);
		partie.plateauActuel.setCell(choosenCell.hauteur, choosenCell.xCell, choosenCell.yCell, monEquipe);
		// j'enlve un pion à l'équipe qui vient de le poser
		if (monEquipe == TeamType.BLANC) partie.nbJetonsBlanc--;
		if (monEquipe == TeamType.NOIR)  partie.nbJetonsNoir--; 
	}
	
	// Implémenté par compatilité pour IA_v1 et v2
	public static void joueUnCoup(TeamType equipeAJouer, PylosPartie partie, int profondeurRecherche) {
		playOnce(partie, equipeAJouer, profondeurRecherche);
	}
	
	public static ArrayList<PylosCell> getPlayableCells(PylosGridArray plateauActuel) {
		ArrayList<PylosCell> result = new ArrayList<PylosCell>();
		for (int iGrid = 0; iGrid <= plateauActuel.getHauteurMax(); iGrid++) { // toutes les grilles
			PylosGrid grid = plateauActuel.a1Grid[iGrid];
			for (int xCell = 0; xCell < grid.gridWidth; xCell++) { // toutes les cases en x
				for (int yCell = 0; yCell < grid.gridHeight; yCell++) { // toutes les cases en y
					if (grid.canPlaceAtPosition(xCell, yCell)) { // si je peux placer un pion � cette position, je place
						PylosCell cell = new PylosCell(xCell, yCell, iGrid);
						result.add(cell);
					}
				}
			}
		}
		return result;
	}
	/*
	
	
	
	// Poser un pion sans faire aucune vérification
	public void poseJeton_noCheck(int gridHeight, int xCell, int yCell, TeamType teamQuiJoueLePion) {
		// j'enlve un pion à l'équipe qui vient de le poser
		if (teamQuiJoueLePion == TeamType.BLANC) nbPionsBlanc--;
		if (teamQuiJoueLePion == TeamType.NOIR)  nbPionsNoir--;
		// je pose le pion
		setCell(gridHeight, xCell, yCell, teamQuiJoueLePion);
		// Je regarde si un carré est créé : si la cellule (xCell, yCell, hauteur) appartient à un carré de ma couleur, j'ajoute 1 pion pour moi, et mon adversaire en perd un (simplifié)
		PylosGrid grid = plateauActuel.a1Grid[gridHeight];
		if (grid.willFormSameColorRectangle(xCell, yCell, teamQuiJoueLePion)) {
			if (teamQuiJoueLePion == TeamType.BLANC) {
				nbPionsBlanc++;
				nbPionsNoir--;
			}
			if (teamQuiJoueLePion == TeamType.NOIR) {
				nbPionsNoir++;
				nbPionsBlanc--;
			}
		}
	}*/
}

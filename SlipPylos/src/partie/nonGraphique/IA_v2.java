package partie.nonGraphique;

import java.util.ArrayList;

/**
 * Deuxi�me version de l'IA
 * J'essaie de maximiser mon score, en prenant, � chaque �tape, l'action qui a le plus de chances de rapporter des points.
 * Score de la case : NbPionsDiff�rence(moi-adversaire)/NbCoupsJou�s
 * 
 * Je pose mon pion sur une case pour que :
 * - pour tous les coups adverses, je gage autant de points que possible, i.e. pour que (somme de tous les scores / nombre de coups adverses) soit le plus �lev�
 * 
 * 
 * Pour chaque case, j'�value toutes les �tats du jeu possibles et je retourne la valeur (somme des scores) / (nombre de coups)
 * 
 * @author admin
 *
 */

class IA_v2_recur {
	int nbPionsBlanc, nbPionsNoir;
	TeamType equipeQuiJoueCeTour;
	TeamType equipeQueJeDoisAider; // �quipe que je dois essayer de faire gagner
	PylosGridArray plateauActuel; // plateau une fois le coup jou�
	//boolean rechercherMax; // rechercher max : true si je recherche le score maximal de equipeQuiJoueCeTour, 
	
	
	public IA_v2_recur(TeamType arg_equipeQueJeDoisAider, TeamType arg_equipeQuiJoueCeTour, int arg_nbPionsBlanc, int arg_nbPionsNoir, PylosGridArray ancienPlateauACopier) {
		equipeQueJeDoisAider = arg_equipeQueJeDoisAider;
		equipeQuiJoueCeTour = arg_equipeQuiJoueCeTour;
		nbPionsBlanc = arg_nbPionsBlanc;
		nbPionsNoir = arg_nbPionsNoir;
		plateauActuel = ancienPlateauACopier.copy();
	}
	public IA_v2_recur(IA_v2_recur iaTourPrecedent) {
		if (iaTourPrecedent.equipeQuiJoueCeTour == TeamType.NOIR) equipeQuiJoueCeTour = TeamType.BLANC;
		else 													  equipeQuiJoueCeTour = TeamType.NOIR;
		nbPionsBlanc = iaTourPrecedent.nbPionsBlanc;
		nbPionsNoir = iaTourPrecedent.nbPionsNoir;
		plateauActuel = iaTourPrecedent.plateauActuel;//.copy();//.copy(); /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ 
		equipeQueJeDoisAider = iaTourPrecedent.equipeQueJeDoisAider; // toujours la m�me �quipe que je veux aider
	}
	
	
	
	public int evaluateScore(TeamType equipeEvaluee) {
		int result = 0;
		if (equipeEvaluee == TeamType.BLANC) result = nbPionsBlanc - nbPionsNoir;
		if (equipeEvaluee == TeamType.NOIR)  result = nbPionsNoir - nbPionsBlanc;
		if (equipeEvaluee == TeamType.AUCUNE) result = 0;
		return  result;
	}
	
	public void setCell(int gridHeight, int xCell, int yCell, TeamType teamType) {
		plateauActuel.setCell(gridHeight, xCell, yCell, teamType);
	}

	
	// Poser un pion sans faire aucune v�rification
	public void posePion_noCheck(int gridHeight, int xCell, int yCell, TeamType teamQuiJoueLePion) {
		// j'enl�ve un pion � l'�quipe qui vient de le poser
		if (teamQuiJoueLePion == TeamType.BLANC)
			nbPionsBlanc--;
		if (teamQuiJoueLePion == TeamType.NOIR)
			nbPionsNoir--;
		// je pose le pion
		setCell(gridHeight, xCell, yCell, teamQuiJoueLePion);
		// Je regarde si un carr� est cr�� : si la cellule (xCell, yCell, hauteur) appartient � un carr� de ma couleur, j'ajoute 1 pion pour moi, et mon adversaire en perd un (simplifi�)
		PylosGrid grid = plateauActuel.a1Grid[gridHeight];
		if (grid.willFormSameColorRectangle(xCell, yCell, this.equipeQuiJoueCeTour)) {
			if (teamQuiJoueLePion == TeamType.BLANC) {
				nbPionsBlanc++;
				nbPionsNoir--;
			}
			if (teamQuiJoueLePion == TeamType.NOIR) {
				nbPionsNoir++;
				nbPionsBlanc--;
			}
		}
	}
	
	public double computeScore(int profondeurDeRechercheRestante) { // Le score de cet �tat du jeu 
		// Condition d'arr�t : une �quipe vient de perdre (est � court de pions) ou j'ai atteint une profondeur suffisante
		if ( (nbPionsBlanc == 0) || (nbPionsNoir == 0) ) {
			return evaluateScore(equipeQueJeDoisAider); //nbPionsBlanc - nbPionsNoir;
		}
		if (profondeurDeRechercheRestante <= 0)
			return evaluateScore(equipeQueJeDoisAider);
		
		double sommeDesScores = 0;
		double nombreDeCasPossibles = 0;
		for (int iGrid = 0; iGrid <= plateauActuel.getHauteurMax(); iGrid++) { // toutes les grilles
			PylosGrid grid = plateauActuel.a1Grid[iGrid];
			for (int xCell = 0; xCell < grid.gridWidth; xCell++) { // toutes les cases en x
				for (int yCell = 0; yCell < grid.gridHeight; yCell++) { // toutes les cases en y
					if (grid.canPlaceAtPosition(xCell, yCell)) { // si je peux placer un pion � cette position, je place
						

						int ancienNbBlanc = nbPionsBlanc;
						int ancienNbNoir = nbPionsNoir;
						IA_v2_recur etatSuivantPossible = new IA_v2_recur(this);
						etatSuivantPossible.posePion_noCheck(iGrid, xCell, yCell, equipeQuiJoueCeTour);
						sommeDesScores += etatSuivantPossible.computeScore(profondeurDeRechercheRestante - 1);
						nombreDeCasPossibles++;
						
						// Retour � l'�tat pr�c�dent
						nbPionsBlanc = ancienNbBlanc;
						nbPionsNoir = ancienNbNoir;
						etatSuivantPossible.setCell(iGrid, xCell, yCell, TeamType.AUCUNE);
					}
				}
			}
		}
		
		if (nombreDeCasPossibles == 0) {
			Log.write("ERREUR IA_v2_recur.computeScore : nombreDeCasPossibles == 0");
			return evaluateScore(equipeQueJeDoisAider);
		}
		
		return (sommeDesScores / nombreDeCasPossibles);
	}
	
	
	
}

class IA_v2_cell {
	int xCell, yCell, hauteur;
	PylosGrid onGrid;
	double score;
	boolean scoreDefini = false;
	
	public IA_v2_cell(int x, int y, int h, PylosGrid grid) {
		xCell = x;
		yCell = y;
		hauteur = h;
		onGrid = grid;
		score = 0;
	}
	public void setScore(double arg_score) {
		score = arg_score;
		scoreDefini = true;
	}
}


public class IA_v2 {
	
	public static void joueUnCoup(TeamType equipeAJouer, PylosPartie partie, int profondeurRecherche) {
		
		boolean doitInitialiserScore = true;
		double meilleurScore = -1000;
		int retenu_hauteur = 0;
		int retenu_xCell = 0;
		int retenu_yCell = 0;
		IA_v2_recur iaRecurRetenue = null;
		//ArrayList<IA_v2_cell> sameScoreCells = new ArrayList<IA_v2_cell>();
		
		for (int iGrid = 0; iGrid <= partie.plateauActuel.getHauteurMax(); iGrid++) { // toutes les grilles
			PylosGrid grid = partie.plateauActuel.a1Grid[iGrid];
			for (int xCell = 0; xCell < grid.gridWidth; xCell++) { // toutes les cases en x
				for (int yCell = 0; yCell < grid.gridHeight; yCell++) { // toutes les cases en y
					if (grid.canPlaceAtPosition(xCell, yCell)) { // si je peux placer un pion � cette position, je place
						
						IA_v2_recur iaRecur = new IA_v2_recur(equipeAJouer, equipeAJouer, partie.nbJetonsBlanc, partie.nbJetonsNoir, partie.plateauActuel);
						iaRecur.posePion_noCheck(iGrid, xCell, yCell, equipeAJouer);
						
						double scoreCellule = iaRecur.computeScore(profondeurRecherche);
						iaRecur.setCell(iGrid, xCell, yCell, TeamType.AUCUNE); // undo de ma derni�re action (la valeur des pions n'est pas chang�e, elle est stock�e par l'IA r�cursive)
						Log.write("IA_v2.joueUnCoup : (x, y, h) = " + Integer.toString(xCell) + ", " + Integer.toString(yCell) + ", " + Integer.toString(iGrid) + "  scoreCellule = " + Double.toString(scoreCellule));
						
						if ((doitInitialiserScore) || (scoreCellule > meilleurScore) ) {
							doitInitialiserScore = false;
							meilleurScore = scoreCellule;
							retenu_hauteur = iGrid;
							retenu_xCell = xCell;
							retenu_yCell = yCell;
							iaRecurRetenue = iaRecur;
							
						}
					}
				}
			}
		}
		if (doitInitialiserScore) {
			Log.write("ERREUR IA_v2.joueUnCoup : doitInitialiserScore == true");
			return;
		}
		partie.setCell(retenu_hauteur, retenu_xCell, retenu_yCell, equipeAJouer);
		partie.nbJetonsBlanc = iaRecurRetenue.nbPionsBlanc;
		partie.nbJetonsNoir = iaRecurRetenue.nbPionsNoir;
	}
	
}

package commun.partie.nonGraphique;

import java.util.ArrayList;
import java.util.Random;

class IA_v3_recur {
	
}

/*class IA_v3_caseRetenue {
	int xCell, yCell, hauteur;
	PylosGrid onGrid;
	double score;
	public IA_v3_cell(int x, int y, int h, PylosGrid grid) {
		xCell = x;
		yCell = y;
		hauteur = h;
		onGrid = grid;
	}
}*/

class IA_v3_cell {
	int xCell, yCell, hauteur;
	PylosGrid onGrid;
	double score;
	boolean scoreDefini = false;
	
	public IA_v3_cell(int x, int y, int h, PylosGrid grid) {
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
/*
class IA_v3_score {
	double score;
	public IA_v3_score(double arg_score) {
		score = arg_score;
	}
}

class IA_v3_jetonsNb {
	int nbJetonsNoir, nbJetonsBlanc;
	public IA_v3_jetonsNb(int nbNoir, int nbBlanc) {
		nbJetonsNoir = nbNoir;
		nbJetonsBlanc = nbBlanc;
	}
}*/

public class IA_v3 {
	
	
	public static ArrayList<IA_v3_cell> getPlayableCells(PylosGridArray plateauActuel) {
		ArrayList<IA_v3_cell> result = new ArrayList<IA_v3_cell>();
		for (int iGrid = 0; iGrid <= plateauActuel.getHauteurMax(); iGrid++) { // toutes les grilles
			PylosGrid grid = plateauActuel.a1Grid[iGrid];
			for (int xCell = 0; xCell < grid.gridWidth; xCell++) { // toutes les cases en x
				for (int yCell = 0; yCell < grid.gridHeight; yCell++) { // toutes les cases en y
					if (grid.canPlaceAtPosition(xCell, yCell)) { // si je peux placer un pion � cette position, je place
						IA_v3_cell cell = new IA_v3_cell(xCell, yCell, iGrid, grid);
						result.add(cell);
					}
				}
			}
		}
		return result;
	}
	
	public static void playOnce(PylosPartie partie, TeamType monEquipe, int profondeurDeRecherche) {
		IA_v3 ia = new IA_v3();
		ia.playOnce_inst(partie, monEquipe, profondeurDeRecherche);
		ia = null;
	}
	
	// Impl�ment� par compatilit� pour IA_v1 et v2
	public static void joueUnCoup(TeamType equipeAJouer, PylosPartie partie, int profondeurRecherche) {
		playOnce(partie, equipeAJouer, profondeurRecherche);
	}

	int nbPionsBlanc, nbPionsNoir;
	PylosGridArray plateauActuel;
	
	public void playOnce_inst(PylosPartie partie, TeamType monEquipe, int profondeurDeRecherche) {

		if ( (monEquipe == TeamType.BLANC) && (partie.nbJetonsBlanc == 0) ) return;
		if ( (monEquipe == TeamType.NOIR) && (partie.nbJetonsNoir == 0) ) return;
		
		nbPionsBlanc = partie.nbJetonsBlanc;
		nbPionsNoir = partie.nbJetonsNoir;
		plateauActuel = partie.plateauActuel.copy();
		//IA_v3_jetonsNb jetonNb = new IA_v3_jetonsNb(partie.nbJetonsNoir, partie.nbJetonsBlanc);
		//IA_v3_jetonsNb jetonNbInitial = new IA_v3_jetonsNb(partie.nbJetonsNoir, partie.nbJetonsBlanc);
		IA_v3_cell caseRetenue = null;
		
		
		
		// C'est � moi de jouer : Je prends le score maximal des cases �valu�es cette it�ration
		ArrayList<IA_v3_cell> playableCells = getPlayableCells(plateauActuel);
		ArrayList<IA_v3_cell> sameScoreCells = new ArrayList<IA_v3_cell>();
		
		for (int iCell = 0; iCell < playableCells.size(); iCell++) {

			int nbPionsBlanc_init = nbPionsBlanc;
			int nbPionsNoir_init = nbPionsNoir;
			IA_v3_cell caseEvaluee = playableCells.get(iCell);
			poseJeton_noCheck(caseEvaluee.hauteur, caseEvaluee.xCell, caseEvaluee.yCell, monEquipe); // incr�mente les compteurs de points
			double score = evaluateCell_tourAdverse(caseEvaluee, monEquipe, profondeurDeRecherche - 1);
			caseEvaluee.setScore(score);
			
			// Initialisation de la case retenue
			if (caseRetenue == null) {
				sameScoreCells.clear();
				caseRetenue = caseEvaluee;
			}
			// score actuel plus �lev� : je prends cette case
			if (caseRetenue.score < score) {
				sameScoreCells.clear();
				caseRetenue = caseEvaluee;
			}
			if (caseRetenue.score == caseEvaluee.score) {
				sameScoreCells.add(caseEvaluee);
			}
			
			
			// Remise des anciennes valeurs
			nbPionsBlanc = nbPionsBlanc_init;
			nbPionsNoir = nbPionsNoir_init;
			setCell(caseEvaluee.hauteur, caseEvaluee.xCell, caseEvaluee.yCell, TeamType.AUCUNE);
		}
		
		if (caseRetenue == null) {
			Log.write("ERREUR IA_v3.playOnce : caseRetenue == null");
			return;
		}
		// Poser al�atoirement en piochant dans sameScoreCells
		int size = sameScoreCells.size();
		
		if (size == 0) {
			Log.write("ERREUR IA_v3.playOnce_inst : size == 0");
			return;
		}
		
		Random rand = new Random();
		rand.setSeed(System.currentTimeMillis());
		int choosenIndex = rand.nextInt(size);
		
		caseRetenue = sameScoreCells.get(choosenIndex);
		Log.write("IA_v3.playOnce_inst : choosenIndex = " + choosenIndex);
		
		poseJeton_noCheck(caseRetenue.hauteur, caseRetenue.xCell, caseRetenue.yCell, monEquipe); // incr�mente les compteurs de points
		// Pose aussi sur le plateau du jeu, comme c'est aussi celui de la partie
		partie.nbJetonsBlanc = nbPionsBlanc;
		partie.nbJetonsNoir = nbPionsNoir;
		partie.plateauActuel = plateauActuel;
	}
	
	
	// Evaluer le score d'une cellule : je viens de jouer une case, je prends le minimum de mon score, pour tous les coups adverses ce tour
	public double evaluateCell_tourAdverse(IA_v3_cell caseEvalueeCetteIteration, TeamType monEquipe, int profondeurDeRecherche) {
		PylosGrid grid = caseEvalueeCetteIteration.onGrid; //partie.getGrid(hauteur);
		if (grid == null) {
			Log.write("ERREUR IA_v3.evaluateCell_tourAdverse : grid == null");
			return 0;
		}
		/*if (!grid.canPlaceAtPosition(caseEvalueeCetteIteration.xCell, caseEvalueeCetteIteration.yCell)) {
			Log.write("ERREUR IA_v3.evaluateCell_tourAdverse : impossible de placer � cette position.");
			return 0;
		}*/
		
		if ( (profondeurDeRecherche <= 0) || (nbPionsBlanc <= 0) || (nbPionsNoir <= 0) ) {
			return evaluateScoreFor(monEquipe);
		}
		
		// C'est � mon adversaire de jouer : Je prends le score minimal (pour moi) des cases �valu�es cette it�ration
		ArrayList<IA_v3_cell> playableCells = getPlayableCells(plateauActuel);
		/* D�j� initialis�es :
		 * nbPionsBlanc = partie.nbJetonsBlanc;
		 * nbPionsNoir = partie.nbJetonsNoir;
		 * plateauActuel = partie.plateauActuel;
		 */
		IA_v3_cell caseRetenue = null;
		
		TeamType equipeAdverse = null;
		if (monEquipe == TeamType.NOIR)  equipeAdverse = TeamType.BLANC;
		if (monEquipe == TeamType.BLANC) equipeAdverse = TeamType.NOIR;
		
		for (int iCell = 0; iCell < playableCells.size(); iCell++) {

			int nbPionsBlanc_init = nbPionsBlanc;
			int nbPionsNoir_init = nbPionsNoir;
			IA_v3_cell caseEvaluee = playableCells.get(iCell);
			poseJeton_noCheck(caseEvaluee.hauteur, caseEvaluee.xCell, caseEvaluee.yCell, equipeAdverse); // incr�mente les compteurs de points
			double score = evaluateCell_monTour(caseEvaluee, monEquipe, profondeurDeRecherche - 1);
			caseEvaluee.setScore(score);
			
			// Initialisation de la case retenue
			if (caseRetenue == null) {
				caseRetenue = caseEvaluee;
			}
			// score actuel moins �lev� : je prends cette case
			if (caseRetenue.score > score) {
				caseRetenue = caseEvaluee;
			}
			
			// Remise des anciennes valeurs
			nbPionsBlanc = nbPionsBlanc_init;
			nbPionsNoir = nbPionsNoir_init;
			setCell(caseEvaluee.hauteur, caseEvaluee.xCell, caseEvaluee.yCell, TeamType.AUCUNE);
		}
		
		if (caseRetenue == null) {
			Log.write("ERREUR IA_v3.playOnce : caseRetenue == null");
			return 0;
		}
		
		return caseRetenue.score;
	}
	
	// Evaluer le score d'une cellule : l'adversaire vient de jouer une case, je prends le maximum de mon score, pour tous mes coups ce tour
	public double evaluateCell_monTour(IA_v3_cell caseEvalueeCetteIteration, TeamType monEquipe, int profondeurDeRecherche) {
		PylosGrid grid = caseEvalueeCetteIteration.onGrid; //partie.getGrid(hauteur);
		if (grid == null) {
			Log.write("ERREUR IA_v3.evaluateCell_first : grid == null");
			return 0;
		}
		/*if (!grid.canPlaceAtPosition(caseEvalueeCetteIteration.xCell, caseEvalueeCetteIteration.yCell)) {
			Log.write("ERREUR IA_v3.evaluateCell_first : impossible de placer � cette position.");
			return 0;
		}*/
		
		if ( (profondeurDeRecherche <= 0) || (nbPionsBlanc <= 0) || (nbPionsNoir <= 0) ) {
			return evaluateScoreFor(monEquipe);
		}
		
		// C'est � moi de jouer : Je prends le score maximal (pour moi) des cases �valu�es cette it�ration
		ArrayList<IA_v3_cell> playableCells = getPlayableCells(plateauActuel);
		/* D�j� initialis�es :
		 * nbPionsBlanc = partie.nbJetonsBlanc;
		 * nbPionsNoir = partie.nbJetonsNoir;
		 * plateauActuel = partie.plateauActuel;
		 */
		IA_v3_cell caseRetenue = null;
		
		for (int iCell = 0; iCell < playableCells.size(); iCell++) {

			int nbPionsBlanc_init = nbPionsBlanc;
			int nbPionsNoir_init = nbPionsNoir;
			IA_v3_cell caseEvaluee = playableCells.get(iCell);
			poseJeton_noCheck(caseEvaluee.hauteur, caseEvaluee.xCell, caseEvaluee.yCell, monEquipe); // incr�mente les compteurs de points
			double score = evaluateCell_tourAdverse(caseEvaluee, monEquipe, profondeurDeRecherche - 1);
			caseEvaluee.setScore(score);
			
			// Initialisation de la case retenue
			if (caseRetenue == null) {
				caseRetenue = caseEvaluee;
			}
			// score actuel plus �lev� : je prends cette case (j'abaisse autant que je peux le score)
			if (caseRetenue.score < score) {
				caseRetenue = caseEvaluee;
			}
			
			// Remise des anciennes valeurs
			nbPionsBlanc = nbPionsBlanc_init;
			nbPionsNoir = nbPionsNoir_init;
			setCell(caseEvaluee.hauteur, caseEvaluee.xCell, caseEvaluee.yCell, TeamType.AUCUNE);
		}
		
		if (caseRetenue == null) {
			Log.write("ERREUR IA_v3.playOnce : caseRetenue == null");
			return 0;
		}
		
		return caseRetenue.score;
	}
	
	
	// Poser un pion sans faire aucune v�rification
	public void poseJeton_noCheck(int gridHeight, int xCell, int yCell, TeamType teamQuiJoueLePion) {
		// j'enl�ve un pion � l'�quipe qui vient de le poser
		if (teamQuiJoueLePion == TeamType.BLANC) nbPionsBlanc--;
		if (teamQuiJoueLePion == TeamType.NOIR)  nbPionsNoir--;
		// je pose le pion
		setCell(gridHeight, xCell, yCell, teamQuiJoueLePion);
		// Je regarde si un carr� est cr�� : si la cellule (xCell, yCell, hauteur) appartient � un carr� de ma couleur, j'ajoute 1 pion pour moi, et mon adversaire en perd un (simplifi�)
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
	}
	public void setCell(int gridHeight, int xCell, int yCell, TeamType teamType) {
		plateauActuel.setCell(gridHeight, xCell, yCell, teamType);
	}
	
	public double evaluateScoreFor(TeamType monEquipe) {
		if (monEquipe == TeamType.BLANC) return (nbPionsBlanc - nbPionsNoir);
		if (monEquipe == TeamType.NOIR) return (nbPionsNoir - nbPionsBlanc);
		return 0;
	}
}

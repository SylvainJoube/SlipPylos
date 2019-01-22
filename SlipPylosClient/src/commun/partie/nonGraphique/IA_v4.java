package commun.partie.nonGraphique;

import java.util.ArrayList;
import java.util.Random;


class IA_v4_cell {
	int xCell, yCell, hauteur;
	PylosGrid onGrid;
	double score;
	boolean scoreDefini = false;
	
	public IA_v4_cell(int x, int y, int h, PylosGrid grid) {
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

public class IA_v4 {
	
	
	public static ArrayList<IA_v4_cell> getPlayableCells(PylosGridArray plateauActuel) {
		ArrayList<IA_v4_cell> result = new ArrayList<IA_v4_cell>();
		for (int iGrid = 0; iGrid <= plateauActuel.getHauteurMax(); iGrid++) { // toutes les grilles
			PylosGrid grid = plateauActuel.a1Grid[iGrid];
			for (int xCell = 0; xCell < grid.gridWidth; xCell++) { // toutes les cases en x
				for (int yCell = 0; yCell < grid.gridHeight; yCell++) { // toutes les cases en y
					if (grid.canPlaceAtPosition(xCell, yCell)) { // si je peux placer un pion � cette position, je place
						IA_v4_cell cell = new IA_v4_cell(xCell, yCell, iGrid, grid);
						result.add(cell);
					}
				}
			}
		}
		return result;
	}
	
	public static void playOnce(PylosPartie partie, TeamType monEquipe, int profondeurDeRecherche) {
		IA_v4 ia = new IA_v4();
		
		ia.playOnce_inst(partie, monEquipe, profondeurDeRecherche, true, true);
		ia = null;
	}
	
	// Implémenté par compatilité pour IA_v1 et v2
	public static void joueUnCoup(TeamType equipeAJouer, PylosPartie partie, int profondeurRecherche) {
		playOnce(partie, equipeAJouer, profondeurRecherche);
	}

	int nbPionsBlanc, nbPionsNoir;
	PylosGridArray plateauActuel;
	
	public double playOnce_inst(PylosPartie partie, TeamType monEquipe, int profondeurDeRecherche, boolean playForReal, boolean evaluateMax) {
		if (playForReal) {
			nbPionsBlanc = partie.nbJetonsBlanc;
			nbPionsNoir = partie.nbJetonsNoir;
			plateauActuel = partie.plateauActuel.copy(partie); // pour ne pas emb�ter l'affichage graphique
		} else {
			// c'est déjà initialisé
		}
		
		if ( (profondeurDeRecherche <= 0) || (nbPionsBlanc <= 0) || (nbPionsNoir <= 0) ) {
			return evaluateScoreFor(monEquipe);
		}
		
		IA_v4_cell caseRetenue = null;
		
		// C'est à moi de jouer : Je prends le score maximal des cases évaluées cette itération
		ArrayList<IA_v4_cell> playableCells = getPlayableCells(plateauActuel);
		ArrayList<IA_v4_cell> sameScoreCells = new ArrayList<IA_v4_cell>();

		TeamType equipeQuiJoue = null;
		if (evaluateMax) equipeQuiJoue = monEquipe;
		else {
			if (monEquipe == TeamType.NOIR)  equipeQuiJoue = TeamType.BLANC;
			if (monEquipe == TeamType.BLANC) equipeQuiJoue = TeamType.NOIR;
		}
		
		
		for (int iCell = 0; iCell < playableCells.size(); iCell++) {
			int nbPionsBlanc_init = nbPionsBlanc;
			int nbPionsNoir_init = nbPionsNoir;
			IA_v4_cell caseEvaluee = playableCells.get(iCell);
			poseJeton_noCheck(caseEvaluee.hauteur, caseEvaluee.xCell, caseEvaluee.yCell, equipeQuiJoue); // incrémente les compteurs de points
			double score = playOnce_inst(null /* pour être sur de ne rien modifier */, monEquipe, profondeurDeRecherche - 1, false, !evaluateMax);
			
			caseEvaluee.setScore(score);
			
			// Initialisation de la case retenue
			if (caseRetenue == null) {
				sameScoreCells.clear();
				caseRetenue = caseEvaluee;
			}
			
			// score actuel plus élevé : je prends cette case
			if (
			   (evaluateMax && (caseRetenue.score < score)) // prendre le score maximal
			|| ((!evaluateMax) && (caseRetenue.score > score)) // prendre le score minimal
			   ) {
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
			Log.write("ERREUR IA_v4.playOnce_inst : caseRetenue == null");
			return 0;
		}
		// Poser aléatoirement en piochant dans sameScoreCells
		int size = sameScoreCells.size();
		
		if (size == 0) {
			Log.write("ERREUR IA_v4.playOnce_inst : size == 0");
			return 0;
		}
		
		
		Random rand = new Random();
		rand.setSeed(System.currentTimeMillis());
		int choosenIndex = rand.nextInt(size);
		
		IA_v4_cell caseRetenue_alt = caseRetenue;
		caseRetenue = sameScoreCells.get(choosenIndex);
		//Log.write("IA_v4.playOnce_inst : choosenIndex = " + choosenIndex);
		
		if (playForReal) {
			poseJeton_noCheck(caseRetenue.hauteur, caseRetenue.xCell, caseRetenue.yCell, monEquipe); // incrémente les compteurs de points
			partie.plateauActuel = plateauActuel;
			// Pose aussi sur le plateau du jeu, comme c'est aussi celui de la partie
			partie.nbJetonsBlanc = nbPionsBlanc;
			partie.nbJetonsNoir = nbPionsNoir;
			return caseRetenue_alt.score; // est inutile, je le mets quand-même
		} else {
			return caseRetenue_alt.score;//caseRetenue.score;
		}
	}
	
	
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

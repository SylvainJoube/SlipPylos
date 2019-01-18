package commun.partie.nonGraphique;

import java.util.ArrayList;

// IA Instance : �tat de la partie virtuel, si l'IA joue tel et tel coup
// IA v1 cherchant seulement � minimiser les pertes maximales que l'IA peut subir.
// Une IA alternative pourrait essayer de maximiser son score global, mais risquerait de perdre plus (et donc de perdre la partie contre un bon joueur)
class IA_v1_recur {
	int nbPionsBlanc, nbPionsNoir;
	TeamType equipeQuiJoueCeTour;
	TeamType equipeQueJeDoisAider; // �quipe que je dois essayer de faire gagner
	PylosGridArray plateauActuel; // plateau une fois le coup jou�
	//boolean rechercherMax; // rechercher max : true si je recherche le score maximal de equipeQuiJoueCeTour, 
	
	
	
	public IA_v1_recur(TeamType arg_equipeQueJeDoisAider, TeamType arg_equipeQuiJoueCeTour, int arg_nbPionsBlanc, int arg_nbPionsNoir, PylosGridArray ancienPlateauACopier) {
		equipeQueJeDoisAider = arg_equipeQueJeDoisAider;
		equipeQuiJoueCeTour = arg_equipeQuiJoueCeTour;
		nbPionsBlanc = arg_nbPionsBlanc;
		nbPionsNoir = arg_nbPionsNoir;
		plateauActuel = ancienPlateauACopier.copy();
	}
	public IA_v1_recur(IA_v1_recur iaTourPrecedent) {
		if (iaTourPrecedent.equipeQuiJoueCeTour == TeamType.NOIR) equipeQuiJoueCeTour = TeamType.BLANC;
		else 													  equipeQuiJoueCeTour = TeamType.NOIR;
		nbPionsBlanc = iaTourPrecedent.nbPionsBlanc;
		nbPionsNoir = iaTourPrecedent.nbPionsNoir;
		plateauActuel = iaTourPrecedent.plateauActuel;//.copy();  /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\
		equipeQueJeDoisAider = iaTourPrecedent.equipeQueJeDoisAider; // toujours la m�me �quipe que je veux aider
	}
	
	
	
	public int evaluateScore(TeamType equipeEvaluee) {
		if (equipeEvaluee == TeamType.BLANC) return nbPionsBlanc - nbPionsNoir;
		if (equipeEvaluee == TeamType.NOIR)  return nbPionsNoir - nbPionsBlanc;
		if (equipeEvaluee == TeamType.AUCUNE) return 0;
		return 0;
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
		// Je regarde si un carré est créé : si la cellule (xCell, yCell, hauteur) appartient à un carré de ma couleur, j'ajoute 1 pion pour moi, et mon adversaire en perd un (simplifi�)
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
	
	/*public int evaluateForMe() { /// fonction d'évaluation de l'état actuel (représenté par cette �tape)
		return nbPionsMoi - nbPionsAutre;
	}
	public int evaluateForOpponent() { /// fonction d'�valuation de l'�tat actuel (repr�sent� par cette �tape)
		return nbPionsMoi - nbPionsAutre;
	}*/
	
	// 1) je simule le fait de lancer un pion
	
	
	public int computeMaxScore(int profondeurDeRechercheRestante, boolean setForReal) { // Je tente de maximiser le score pour l'�quipe equipeEvaluee (i.e. minimiser le score pour l'�qupe adverse)
		// Pour toutes les cases libres accessibles, je tente de placer un pion
		// Je cherche � maximiser mon score
		double rememberScore = 0; // sera r�initialis� au premier passage dans la boucle
		boolean rememberScore_hasToInit = true;
		int choosen_h = -1;
		int choosen_x = -1;
		int choosen_y = -1;
		double cScore;
		class loc_ChoosenCell {
			public int haut, xCell, yCell;
			public loc_ChoosenCell(int arg_haut, int arg_x, int arg_y) {
				haut = arg_haut;
				xCell = arg_x;
				yCell = arg_y;
			}
		}
		ArrayList<loc_ChoosenCell> a1ChoosenCell = new ArrayList<loc_ChoosenCell>();
		
		
		// Condition d'arr�t : une �quipe vient de perdre (est à court de pions) ou j'ai atteint une profondeur suffisante
		if ( (nbPionsBlanc == 0) || (nbPionsNoir == 0) ) {
			return evaluateScore(equipeQueJeDoisAider); //nbPionsBlanc - nbPionsNoir;
		}
		if (profondeurDeRechercheRestante <= 0)
			return evaluateScore(equipeQueJeDoisAider);
		
		boolean findMyMaximalScore;
		// Quand c'est moi qui joue : je prends le score maximal de mon adversaire (i.e. mon score minimal) quand je pose mon pion
		if (equipeQueJeDoisAider == equipeQuiJoueCeTour) findMyMaximalScore = true;
		else										     findMyMaximalScore = false;
		// Quand c'est mon adversaire qui joue : je prends mon score minimal quand il joue
		/*if (equipeQueJeDoisAider != equipeQuiJoueCeTour) findMyMaximalScore = true;*/
		
		
		// i.e. : quand c'est � moi de jouer, je regarde le score maximal de mon adversaire et je joue pour maximiser son score
		//        quand c'est � mon adversaire de jouer, je regarde mon score m
		// i.e. je fais comme si mon adversaire allait jouer le meilleur coup, et je minimise ses gains, je r�duis mes pertes
		
		
		// Je scan tout les coups possibles :
		// 1) Scan de toutes les cases o� je peux poser un pion
		for (int iGrid = 0; iGrid <= plateauActuel.getHauteurMax(); iGrid++) {
			PylosGrid grid = plateauActuel.a1Grid[iGrid];
			for (int xCell = 0; xCell < grid.gridWidth; xCell++) { // toutes les cases en x
				for (int yCell = 0; yCell < grid.gridHeight; yCell++) { // toutes les cases en y
					if (grid.canPlaceAtPosition(xCell, yCell)) { // si je peux placer un pion � cette position, j'essaie de le placer
						// Je regarde ce que �a fait sur le score global, cr�ation d'une nouvelle instance pour "simuler" le coup

						int ancienNbBlanc = nbPionsBlanc;
						int ancienNbNoir = nbPionsNoir;
						
						IA_v1_recur iaTourSuivant = new IA_v1_recur(this);
						iaTourSuivant.posePion_noCheck(iGrid, xCell, yCell, equipeQuiJoueCeTour);
						// j'enl�ve un pion � l'�quipe qui vient de le poser
						/*if (this.equipeQuiJoueCeTour == TeamType.BLANC)
							iaTourSuivant.nbPionsBlanc--;
						if (this.equipeQuiJoueCeTour == TeamType.NOIR)
							iaTourSuivant.nbPionsNoir--;
						// Je regarde si un carr� est cr�� : si la cellule (xCell, yCell, hauteur) appartient � un carr� de 
						iaTourSuivant.setCell(iGrid, xCell, yCell, this.equipeQuiJoueCeTour);
						if (grid.willFormSameColorRectangle(xCell, yCell, this.equipeQuiJoueCeTour)) {
							if (this.equipeQuiJoueCeTour == TeamType.BLANC) {
								iaTourSuivant.nbPionsBlanc++;
								iaTourSuivant.nbPionsNoir--;
							}
							if (this.equipeQuiJoueCeTour == TeamType.NOIR) {
								iaTourSuivant.nbPionsNoir++;
								iaTourSuivant.nbPionsBlanc--;
							}
						}*/
						
						
						cScore = iaTourSuivant.computeMaxScore(profondeurDeRechercheRestante - 1, false);

						//if (setForReal) {
							Log.write("IA_v1_recur : [" + profondeurDeRechercheRestante + "]   (h, x, y) = (" + iGrid + ", " + xCell + ", " + yCell + ")  cScore = " + cScore);
							
						//}
						
						if ( (rememberScore_hasToInit)
						|| (((findMyMaximalScore && (rememberScore < cScore)) // trouver le score maximal et score plus �lev� que le retenu pr�c�demment
							|| ((!findMyMaximalScore) && (rememberScore > cScore)) )
					       )
						   )	{ // trouver le score minimal et score trouv� inf�rieur au score retenu pr�c�demment
							if (rememberScore_hasToInit) rememberScore = cScore - 10;
							rememberScore_hasToInit = false;
							
							if (rememberScore != cScore) {
								a1ChoosenCell.clear();
							}
							
							loc_ChoosenCell cell = new loc_ChoosenCell(iGrid, xCell, yCell);
							a1ChoosenCell.add(cell);
							rememberScore = cScore;
							choosen_h = iGrid;
							choosen_x = xCell;
							choosen_y = yCell;
						}
						// Retour � l'�tat pr�c�dent
						nbPionsBlanc = ancienNbBlanc;
						nbPionsNoir = ancienNbNoir;
						iaTourSuivant.setCell(iGrid, xCell, yCell, TeamType.AUCUNE);
						//break;
					}
				}
			}
		}
		
		// Je joue le coup retenu, s'il y en a un
		if (rememberScore_hasToInit) {
			Log.write("ERREUR Ia_v1_recur : rememberScore_hasToInit == true, score inconnu, aucune case � jouer valide.");
			return 0;
		}
		
		if (setForReal)
			this.posePion_noCheck(choosen_h, choosen_x, choosen_y, equipeQuiJoueCeTour);
		
		return evaluateScore(equipeQueJeDoisAider);
		
	}
	
	
	
}

/**
 * Probable bug quelque part, l'IA perd tout le temps.
 * @author admin
 *
 */
public class IA_v1 {
	
	TeamType equipe;
	PylosPartie partie;
	
	public IA_v1(TeamType arg_equipe, PylosPartie arg_partie) {
		equipe = arg_equipe;
		partie = arg_partie;
	}
	
	// IA V0
	// Objectif : Chercher quel coup jouer � ce tour
	/// Analyse de tout les coups possible ce tour, et choix de celui qui aura le meilleur score (ou ex-aequo)
	// Pour savoir quel coup est le meilleur, il faut remonter l'arbre jusqu'au feuilles, c'est � dire jusqu'au r�sultat final
	//   (c'est � dire, score total � la fin, i.e. nombre de pions de son �quipe restants lorsque l'adversaire en a le plus)
	// Donc pour chaque coup possible, je dois analyser tout l'arbre de possibilit�s qui en d�coule.
	
	// R�gles du jeu beaucoup plus simples : faire un carr� de sa couleur ajoute 1 pion et retire un pion � l'adversaire
	// La fonction d'�valuation est simple : score de mon �quipe = nbPionsMoi - nbPionsAutre;
	
	
	// 1) Jouer al�atoirement, sur une case valide
	// Je balaie chaque grille et je regarde si je peux jouer
	public void joueUnCoup0() {
		boolean celluleEstChoisie = false;
		for (int iGrid = 0; iGrid <= partie.hauteurMax; iGrid++) {
			if (celluleEstChoisie)
				break;
			PylosGrid grid = partie.plateauActuel.a1Grid[iGrid];
			for (int xCell = 0; xCell < grid.gridWidth; xCell++) {
				if (celluleEstChoisie)
					break;
				for (int yCell = 0; yCell < grid.gridHeight; yCell++) {
					if (grid.canPlaceAtPosition(xCell, yCell)) {
						grid.setCell(xCell, yCell, equipe);
						celluleEstChoisie = true;
						break;
					}
				}
			}
		}
	}
	
	public static void joueUnCoup(TeamType equipe, PylosPartie partie, int profondeurDeRechercheMax) {
		IA_v1_recur ia_v1_recur = new IA_v1_recur(equipe, equipe, partie.nbJetonsBlanc, partie.nbJetonsNoir, partie.plateauActuel.copy());
		ia_v1_recur.computeMaxScore(profondeurDeRechercheMax, true);
		partie.plateauActuel = ia_v1_recur.plateauActuel; // switch du plateau
		partie.nbJetonsBlanc = ia_v1_recur.nbPionsBlanc;
		partie.nbJetonsNoir = ia_v1_recur.nbPionsNoir;
	}
	
	
	
	
}

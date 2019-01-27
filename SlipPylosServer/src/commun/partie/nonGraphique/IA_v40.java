package commun.partie.nonGraphique;

import java.util.ArrayList;
import java.util.Random;

import slip.network.buffers.NetBuffer;

/**
 * IA v40, extension de l'IA v4 avec les fonctionnalités du jeu final
 * 2019-01-23 : IA fonctionnelle, manque élagague + évaluation des pions à reprendre à finir
 * 2019-01-24 : élagage en cours : arrêté car ne marchant pas bien ><"
 */

// Poser un pion quelque part (ou en déplacer un si IA_v40_poserPion.estUnDeplacement)
class IA_v40_poserPion {
	public int xCell, yCell, hauteur;
	public boolean doitReprendrePionsEnsuite = false; // true si je viens de faire un carré ou une ligne
	public boolean estUnDeplacement = false;
	public int xCell_init = -1,
			   yCell_init = -1, // si c'est un déplacement, uniquement
			   hauteur_init = -1;
	public IA_v40_poserPion(int arg_hauteur, int arg_xCell, int arg_yCell, boolean arg_estUnDeplacement) {
		hauteur = arg_hauteur;
		xCell = arg_xCell;
		yCell = arg_yCell;
		estUnDeplacement = arg_estUnDeplacement;
	}
}

/*
// Déplacer un pion
class IA_v40_deplacerPion extends IA_v40_poserPion {
	
}*/

//Pion qu'il est possible de déplacer
class IA_v40_peutDeplacerCePion {
	public int xCell, yCell, hauteur;
	public IA_v40_peutDeplacerCePion(int arg_hauteur, int arg_xCell, int arg_yCell) {
		hauteur = arg_hauteur;
		xCell = arg_xCell;
		yCell = arg_yCell;
	}
}

//Pion qu'il est possible de récupérer
class IA_v40_peutRecupererCePion {
	public int xCell, yCell, hauteur;
	public IA_v40_peutRecupererCePion(int arg_hauteur, int arg_xCell, int arg_yCell) {
		hauteur = arg_hauteur;
		xCell = arg_xCell;
		yCell = arg_yCell;
	}
	public IA_v40_peutRecupererCePion(IA_v40_peutDeplacerCePion depuis) {
		hauteur = depuis.hauteur;
		xCell = depuis.xCell;
		yCell = depuis.yCell;
	}
}


class IA_v40_cell {
	int xCell, yCell, hauteur;
	PylosGrid onGrid;
	double score;
	boolean scoreDefini = false;
	
	boolean doitReprendrePionsEnsuite = false; // true si je viens de faire un carré ou une ligne
	
	public IA_v40_cell(int x, int y, int h, PylosGrid grid) {
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

// Un coup peut avoir plusieurs actions
enum IA_v40_coupActionType {
	POSER_PION_DEPUIS_RESERVE,
	DEPLACER_PION,
	REPRENDRE_PION;
}

// Une action d'un coup
class IA_v40_coupAction {
	
}

// Un coup joué par l'IA (ou une simulation du joueur par l'IA), i.e. toutes les actions faites dans l'ordre par l'IA
class IA_v40_coup {
	
	public TeamType equipeQuiJoueLeCoup = TeamType.INVALIDE; // utile pour la sauvegarde/chargement des parties : afficher les coups d'une partie
	public IA_v40_poserPion pionAPoserOuDeplacer;
	// dans pionAPoserOuDeplacer : public boolean doitReprendrePionsEnsuite = false; // true si je viens de faire un carré ou une ligne
	public ArrayList<IA_v40_peutRecupererCePion> listePionsARecuperer = null; // créé si je reprends 1 ou + pions (et donc que doitReprendrePionsEnsuite == true)
	
	int nombreDePionsEconomises = 0; // 1 si déplacer un pion, 0 si poser un pion de sa réserve + 1à2 si reprendre des pions. Donc entre 0 et 3. (3 = bien, 0 = normal/mauvais)
	//ArrayList<IA_v40_coupAction> listeDesActions = new ArrayList<IA_v40_coupAction>();
	
	public void drawOnScreen() {
		System.out.println("IA_v40_coup");
		
		if (pionAPoserOuDeplacer.estUnDeplacement) {
			System.out.println(" est un déplacement");
		} else System.out.println(" posé depuis la réserve");
		
		

		System.out.println("  position(h, x, y) = " + pionAPoserOuDeplacer.hauteur + " " + pionAPoserOuDeplacer.xCell + " " + pionAPoserOuDeplacer.yCell);
		 
		 if (pionAPoserOuDeplacer.estUnDeplacement)
			 System.out.println("  déplacement_de(h, x, y) = " + pionAPoserOuDeplacer.hauteur_init + " " + pionAPoserOuDeplacer.xCell_init + " " + pionAPoserOuDeplacer.yCell_init);
		 
		 System.out.println("  nombreDePionsEconomises = " + nombreDePionsEconomises);
		 
		 if (listePionsARecuperer != null)
			 System.out.println("  nombreDePionsRepris = " + listePionsARecuperer.size());
		 
	}
	
}

public class IA_v40 {
	
	// Faire la liste de tous les coups possibles sur le plateauActuel
	// Pour chaque profondeur, le min-max regarde regarde tous les coups possibles, puis les applique un par un et regarde le score.
	
	// Un coup est :
	// 1 Jouer un pion
	// 1.1 Poser un pion de sa réserve
	// 1.2 Déplacer un pion du plateau (le faire remonter)
	// 2 Agir en fonction :
	// 2.1 Ne rien faire de plus
	// 2.2 si un carré ou une ligne est formée : reprendre 2 pions, un 1 seul si aucun autre à reprendre
	
	// Avant le coup, l'IA n'a pas encore joué, après le coup, elle peut passer le tour.
	
	/** Retourne la liste de TOUS les coups possible à ce tour
	 *  @param plateauActuel
	 *  @param equipeAFaireJouer
	 *  @return
	 */
	public static ArrayList<IA_v40_coup> listerTousLesCoupsPossiblesCeTour(PylosGridArray plateauActuel, TeamType equipeAFaireJouer) {//listeDesCoupsPossiblesActuellement = new Arra
		// 1) Je place un pion quelque part
		// 2) Je déplace un pion
		
		// Cas 1 : j'ai encore des pions, je place un pion quelque part.
		
		// Je fais la liste de toutes les cases où je peux poser un pion de ma réserve
		 ArrayList<IA_v40_poserPion> listeDeTotesLesCasesOuJePeuxPoserOuDeplacerUnPion = listerToutesLesCasesOuJePeuxPoserMonPion(plateauActuel, equipeAFaireJouer, false, -1, -1, -1);
		 
		 // Je fais la liste de tout les pions que je peux bouger (sans l'ajouter à listeDeTotesLesCasesOuJePeuxPoserMonPion)
		 ArrayList<IA_v40_peutDeplacerCePion> listeDesPionsQueJePeuxDeplacer = listerToutesLesPionsQueJePeuxDeplacer(plateauActuel, equipeAFaireJouer);
		 
		 // Pour chaque pion que je peux bouger, le fais la liste de toutes les cases où je peux le bouger
		 for (int indexPionQueJePeuxDeplacer = 0; indexPionQueJePeuxDeplacer < listeDesPionsQueJePeuxDeplacer.size(); indexPionQueJePeuxDeplacer++) {
			 
			 IA_v40_peutDeplacerCePion peutDeplacerCePion = listeDesPionsQueJePeuxDeplacer.get(indexPionQueJePeuxDeplacer);
			 
			 // Liste de toutes les cases où je peux mettre mon pion
			 ArrayList<IA_v40_poserPion> toutesLesCasesOuJePeuxDeplacerCePion = listerToutesLesCasesOuJePeuxPoserMonPion(plateauActuel, equipeAFaireJouer, true, peutDeplacerCePion.hauteur, peutDeplacerCePion.xCell, peutDeplacerCePion.yCell);
			 
			 // Ajout de toutes les positions où je peux poser le pion que je veux déplacer
			 listeDeTotesLesCasesOuJePeuxPoserOuDeplacerUnPion.addAll(toutesLesCasesOuJePeuxDeplacerCePion);
			 
		 }
		 
		 // listeDeTotesLesCasesOuJePeuxPoserMonPion contient désormais toutes les positions où je peux poser un pion de ma réserve,
		 /// et tout les agencements possibles pour les déplacements de pions
		 /// Pour chaque index de la liste, il est indiqué si c'est un déplacement et s'il est possible de reprendre des pions.
		 
		 // Je convertis la liste listeDeTotesLesCasesOuJePeuxPoserMonPion
		 // en une liste de coups possibles.
		 // S'il est possible de reprendre des pions, il faut que je regarde toutes les combinaisons possibles :
		 //// pour tous les pions que je peux reprendre : pour toutes les positions ...
		 
		 ArrayList<IA_v40_coup> listeDesCoupsPossibles = new ArrayList<IA_v40_coup>();
		 
		 for (int iPion = 0; iPion < listeDeTotesLesCasesOuJePeuxPoserOuDeplacerUnPion.size(); iPion++) {
			 
			 IA_v40_poserPion poserPion = listeDeTotesLesCasesOuJePeuxPoserOuDeplacerUnPion.get(iPion);
			 
			 // --- 1) Ajout du pion au plateau, suppression de l'ancienne position du pion s'il s'agit d'un déplacement
			 if (poserPion.estUnDeplacement) {
				 plateauActuel.setCell(poserPion.hauteur_init, poserPion.xCell_init, poserPion.yCell_init, TeamType.AUCUNE);
			 }
			 plateauActuel.setCell(poserPion.hauteur, poserPion.xCell, poserPion.yCell, equipeAFaireJouer);
			 
			 
			 IA_v40_coup coupActuel = new IA_v40_coup();
			 coupActuel.pionAPoserOuDeplacer = poserPion;
			 //coupActuel.doitReprendrePionsEnsuite = poserPion.doitReprendrePionsEnsuite;
			 if (poserPion.doitReprendrePionsEnsuite) {
				 // Short-cut : je ferai plus tard le détail, je me contente de reprendre les 2 premiers pions que je peux récupérer
				 ArrayList<IA_v40_peutDeplacerCePion> listeDePionsQueJePeuxReprendre = listerToutesLesPionsQueJePeuxDeplacer(plateauActuel, equipeAFaireJouer); // Deplacer == Reprendre dans ce contexte !
				 //IA_v40_peutDeplacerCePion pionActuel = new IA_v40_peutDeplacerCePion(poserPion.hauteur, poserPion.xCell, poserPion.yCell);
				 //System.out.println("IA_v40.listerTousLesCoupsPossiblesCeTour : reprendre à la hauteur " + poserPion.hauteur);
				 // étant un bug : ne pas ajouter 2 fois le pion, il est déjà ajouté au plateau en ( --- 1) ) plisteDePionsQueJePeuxReprendre.add(pionActuel); // ajout du pion que je vais poser à la liste des pions que je peux reprendre !
				 
				 coupActuel.listePionsARecuperer = new ArrayList<IA_v40_peutRecupererCePion>();
				 
				 // Reprendre au plus 2 pions
				 int reprendreNb = 2;
				 if (listeDePionsQueJePeuxReprendre.size() < reprendreNb)
					 reprendreNb = listeDePionsQueJePeuxReprendre.size();
				 
				 
				// C'était juste un débug : Vérifier si les pions sont biens différents :
				/*if (reprendreNb == 2) {
					
					IA_v40_peutDeplacerCePion recupPion1 = listeDePionsQueJePeuxReprendre.get(0);
					IA_v40_peutDeplacerCePion recupPion2 = listeDePionsQueJePeuxReprendre.get(1);
					if (recupPion1.hauteur == recupPion2.hauteur && recupPion1.xCell == recupPion2.xCell && recupPion1.yCell == recupPion2.yCell) {
						System.err.println("ERREUR PINONS IA_v40.listerTousLesCoupsPossiblesCeTour, PIONS IDENTIQUES.");
					}
				}*/
				 
				 // J'ajoute les 2 pions à reprendre
				 for (int iReprendrePion = 0; iReprendrePion < reprendreNb; iReprendrePion++) {
					 
					 // Je convertis mon IA_v40_peutDeplacerCePion en -> IA_v40_peutRecupererCePion (même chose, noms différents)
					 IA_v40_peutDeplacerCePion pionARecuperer_malType = listeDePionsQueJePeuxReprendre.get(iReprendrePion);
					 IA_v40_peutRecupererCePion pionARecuperer = new IA_v40_peutRecupererCePion(pionARecuperer_malType);
					 
					 coupActuel.listePionsARecuperer.add(pionARecuperer);
				 }
				 
			 }
			 
			 // Calcul du nombre réel de pions économisés
			 coupActuel.nombreDePionsEconomises = 0;
			 if (poserPion.estUnDeplacement) coupActuel.nombreDePionsEconomises += 1;
			 if (coupActuel.listePionsARecuperer != null)
				 coupActuel.nombreDePionsEconomises += coupActuel.listePionsARecuperer.size();
			 
			 // Ajout du coup à la liste de tous les coups possibles
			 listeDesCoupsPossibles.add(coupActuel);
			 
			 // --- 2) Suppression du pion du plateau
			 plateauActuel.setCell(poserPion.hauteur, poserPion.xCell, poserPion.yCell, TeamType.AUCUNE);
			 if (poserPion.estUnDeplacement) {
				 plateauActuel.setCell(poserPion.hauteur_init, poserPion.xCell_init, poserPion.yCell_init, equipeAFaireJouer);
			 }
			 
		 }
		 
		 /*// AFFICHAGE GRAPHIQUE 
		 System.out.println("IA_v40.listerTousLesCoupsPossiblesCeTour : size = " + listeDesCoupsPossibles.size());
		 for (int iCoup = 0; iCoup < listeDesCoupsPossibles.size(); iCoup++) {
			 System.out.println("coup possible " + iCoup);
			 IA_v40_coup coup = listeDesCoupsPossibles.get(iCoup);
			 IA_v40_poserPion pion = coup.pionAPoserOuDeplacer;
			 if (pion.estUnDeplacement) System.out.println("  déplacement");
			 else                       System.out.println("  poser de la réserve");

			 System.out.println("  position(h, x, y) = " + pion.hauteur + " " + pion.xCell + " " + pion.yCell);
			 
			 if (pion.estUnDeplacement)
				 System.out.println("  déplacement_de(h, x, y) = " + pion.hauteur_init + " " + pion.xCell_init + " " + pion.yCell_init);
			 
			 System.out.println("  nombreDePionsEconomises = " + coup.nombreDePionsEconomises);
			 
			 if (coup.listePionsARecuperer != null)
				 System.out.println("  nombreDePionsRepris = " + coup.listePionsARecuperer.size());
			 
		 }*/
		 
		 
		 return listeDesCoupsPossibles;
		 
		 // Puis, si je dois reprendre des pions, je fais la liste de toutes les configurations possibles
		 
		 
		 // J'y ajoute tout les déplacements possibles : 
		
		
		
		// Pour le pion que je viens de placer : je regarde tout ce que je peux faire ensuite
		
		
	}
	
	// Liste toutes les cases où je peux poser un pion, stocke dans les IA_v40_cell si je dois faire une action ensuite ou non (reprendre des pions)
	/** Retourne la liste de toutes les cases où je peux poser mon pion : ilpeut s'agit d'un pion veant de la réserve ou d'un déplacement.
	 *  @param plateauActuel
	 *  @param equipeAFaireJouer
	 *  @param estUnDeplacement
	 *  @param hauteur_init
	 *  @param xCell_init
	 *  @param yCell_init
	 *  @return
	 */
	private static ArrayList<IA_v40_poserPion> listerToutesLesCasesOuJePeuxPoserMonPion(PylosGridArray plateauActuel, TeamType equipeAFaireJouer, boolean estUnDeplacement, int hauteur_init, int xCell_init, int yCell_init) {
		
		// Initialisation de la liste à retourner
		ArrayList<IA_v40_poserPion> result = new ArrayList<IA_v40_poserPion>();
		
		// Je parcours toutes les grilles
		for (int iGrid = 0; iGrid <= plateauActuel.getHauteurMax(); iGrid++) {
			PylosGrid grid = plateauActuel.a1Grid[iGrid];
			
			// Je parcours les cases de la grille de hauteur iGrid
			for (int xCell = 0; xCell < grid.gridWidth; xCell++) for (int yCell = 0; yCell < grid.gridHeight; yCell++) { // toutes les cases en x et en y
				
				boolean ajouterCettePosition = false;
				
				// Si je peux placer un pion à cette position :
				if (grid.canPlaceAtPosition(xCell, yCell)) {
					
					// Si ce n'est pas un déplacement, j'ajoute cette case à la lise des cases possibles
					if (estUnDeplacement == false) {
						ajouterCettePosition = true;
					} else // if (estUnDeplacement == true)
					// Si c'est un déplacement, je vérifie qu'il est possible de bouger le pion (qu'aucun autre pion ne s'appuie sur lui)
					///  et qu'il va pas s'appuier pas sur son ancienne position
					{
						if (iGrid > hauteur_init) {
							boolean peutBougerPion = plateauActuel.canMovePawn(iGrid, xCell, yCell, hauteur_init, xCell_init, yCell_init);
							if (peutBougerPion) {
								ajouterCettePosition = true;
							}
						}
					}
				}
				
				// Ajouter cette position (je peux donc bien poser/déplacer mon pion ici)
				if (ajouterCettePosition) {
					IA_v40_poserPion cell = new IA_v40_poserPion(iGrid, xCell, yCell, estUnDeplacement);
					if (estUnDeplacement) {
						cell.hauteur_init = hauteur_init;
						cell.xCell_init = xCell_init;
						cell.yCell_init = yCell_init;
					}
					result.add(cell);
					
					// Si j'ajoute cette position, je regarde si un carré ou une ligne est faite :
					if (plateauActuel.willFormSameColorRectangle(iGrid, xCell, yCell, equipeAFaireJouer)
					 || plateauActuel.willFormSameColorLine(iGrid, xCell, yCell, equipeAFaireJouer)) {
						cell.doitReprendrePionsEnsuite = true;
					}
				}
			}
		}
		return result;
	}
	
	/** Retourne la liste de tous les pions que je peux déplacer/prendre à ce tour (i.e. les pions qui ne soutuennent aucun autre pion, il n'y a rien au-dessus d'eux)
	 *  @param plateauActuel
	 *  @param equipeAFaireJouer
	 *  @return
	 */
	private static ArrayList<IA_v40_peutDeplacerCePion> listerToutesLesPionsQueJePeuxDeplacer(PylosGridArray plateauActuel, TeamType equipeAFaireJouer) {
		
		// Initialisation de la liste à retourner
		ArrayList<IA_v40_peutDeplacerCePion> result = new ArrayList<IA_v40_peutDeplacerCePion>();
		
		// Je parcours toutes les grilles
		for (int iGrid = 0; iGrid <= plateauActuel.getHauteurMax(); iGrid++) {
			PylosGrid grid = plateauActuel.a1Grid[iGrid];
			
			// Je parcours les cases de la grille de hauteur iGrid
			for (int xCell = 0; xCell < grid.gridWidth; xCell++) for (int yCell = 0; yCell < grid.gridHeight; yCell++) { // toutes les cases en x et en y
				
				if (grid.getTeamAtCellPosition_noCheck(xCell, yCell) != equipeAFaireJouer)
					continue;
				//boolean ajouterCettePosition = false;
				
				// Si je peux bouger ce pion (i.e. qu'il ne soutient aucun autre pion), je l'ajoute à la liste
				if (plateauActuel.canMovePawn(iGrid, xCell, yCell, -1, -1, -1)) { 
					IA_v40_peutDeplacerCePion cell = new IA_v40_peutDeplacerCePion(iGrid, xCell, yCell);
					result.add(cell);
					
				}
			}
		}
		// Débug OK - laissé pour archive - DEBUG : je regarde s'il y a des doublons : (il n'y en a jamais)
		/*for (int i = 0; i < result.size(); i++) {
			IA_v40_peutDeplacerCePion cell1 = result.get(i);
			for (int ii = 0; ii < result.size(); ii++) {
				if (i == ii) continue;
				IA_v40_peutDeplacerCePion cell2 = result.get(ii);
				if (cell1.hauteur == cell2.hauteur && cell1.xCell == cell2.xCell && cell1.yCell == cell2.yCell) {
					System.err.println("ERRRRRRRRRRRRRRRREUR IA_v40.listerToutesLesPionsQueJePeuxDeplacer : PIONS IDENTIQUES ! i="+i+" ii="+ii);
					
				}
			}
		}*/
		
		return result;
	}
	
	
	/** Test graphique (en cours)
	 * @param arg_partieActuelle
	 * @param equipeAFaireJouer
	 * @param coup
	 * @param ajouterAListeGraphiquePartie
	 * @return
	 */
	public static boolean appliquerCoupSurPartieActuelle_static(PylosPartie arg_partieActuelle, TeamType equipeAFaireJouer, IA_v40_coup coup, boolean ajouterAListeGraphiquePartie) {
		
		
		
		IA_v40 ia = new IA_v40();
		ia.nbPionsBlanc = arg_partieActuelle.nbJetonsBlanc;
		ia.nbPionsNoir = arg_partieActuelle.nbJetonsNoir;
		ia.plateauActuel = arg_partieActuelle.plateauActuel.copy(null);
		ia.partieActuelle = arg_partieActuelle;
		ia.equipeIA = equipeAFaireJouer;
		ia.coupAJouerCeTour = null;
		
		boolean passerLeTour = ia.appliquerCoupAuPlateauActuel(arg_partieActuelle.plateauActuel, equipeAFaireJouer, coup, ajouterAListeGraphiquePartie);
		
		arg_partieActuelle.nbJetonsBlanc = ia.nbPionsBlanc;
		arg_partieActuelle.nbJetonsNoir  = ia.nbPionsNoir;
		arg_partieActuelle.joueurAJoueUnPion = true;
		arg_partieActuelle.peutReprendrePionsNb = 0;
		
		//coupAJouerCeTour.drawOnScreen();
		
		System.out.println("appliquerCoupSurPartieActuelle_static - passerLeTour = "+passerLeTour);
		
		if (passerLeTour && arg_partieActuelle.actionsGraphiques_peutPasserLeTour()) arg_partieActuelle.tourSuivant(); // le tour sera passé automatiquement sinon, plus tard, via PylosPartie.actionsGraphiques_loopEffectuerAction();
		
		
		
		return passerLeTour;
		
	}
	
	
	/** Appliquer un coup au plateau actuel
	 *  @param plateauActuel
	 *  @param equipeAFaireJouer
	 *  @param coup
	 *  @param etapeGraphique pour l'affichage lent, pas à pas
	 *  @return true s'il n'y a pas d'actions en attente graphique, false sinon (= ne pas passer le tour)
	 */
	public boolean appliquerCoupAuPlateauActuel(PylosGridArray plateauActuel, TeamType equipeAFaireJouer, IA_v40_coup coup, boolean ajouterAListeGraphiquePartie) {
		
		if (estServeurInternet) ajouterAListeGraphiquePartie = false;
		
		IA_v40_poserPion pion = coup.pionAPoserOuDeplacer;
		
		// Pas un déplacement
		if (pion.estUnDeplacement == false) {
			plateauActuel.setCell(pion.hauteur, pion.xCell, pion.yCell, equipeAFaireJouer);
			if (ajouterAListeGraphiquePartie) {
				PylosPartie_actionSimple actionActuelle = new PylosPartie_actionSimple(equipeAFaireJouer, 0, pion.hauteur, pion.xCell, pion.yCell);
				NetBuffer actionActuelleAsNetBuffer = actionActuelle.writeToNetBuffer();
				partieActuelle.listeDeMessagesAEnvoyerAuxJoueurs.add(actionActuelleAsNetBuffer);
				
			}
			
		} else { // Un déplacement
			plateauActuel.deplacerUnPion_forcerDepuisPartie(equipeAFaireJouer, pion.hauteur, pion.xCell, pion.yCell, pion.hauteur_init, pion.xCell_init, pion.yCell_init);
			
			if (ajouterAListeGraphiquePartie) {
				PylosPartie_actionSimple actionActuelle = new PylosPartie_actionSimple(equipeAFaireJouer, 1, pion.hauteur, pion.xCell, pion.yCell, pion.hauteur_init, pion.xCell_init, pion.yCell_init);
				NetBuffer actionActuelleAsNetBuffer = actionActuelle.writeToNetBuffer();
				partieActuelle.listeDeMessagesAEnvoyerAuxJoueurs.add(actionActuelleAsNetBuffer);
			}
		}
		
		// Enlever/créditer les pions
		if (equipeAFaireJouer == TeamType.BLANC) nbPionsBlanc += -1 + coup.nombreDePionsEconomises;
		if (equipeAFaireJouer == TeamType.NOIR)   nbPionsNoir += -1 + coup.nombreDePionsEconomises;
		
		
		
		if (coup.listePionsARecuperer != null) {
			
			// Récurérer les pions qui me sont dus
			for (int iRecupererPion = 0; iRecupererPion < coup.listePionsARecuperer.size(); iRecupererPion++) {
				IA_v40_peutRecupererCePion recupererPion = coup.listePionsARecuperer.get(iRecupererPion);
				if (ajouterAListeGraphiquePartie == false) // ajouter directement au plateau
					plateauActuel.setCell(recupererPion.hauteur, recupererPion.xCell, recupererPion.yCell, TeamType.AUCUNE);
				else // ajouter à la liste graphique de la partie
					partieActuelle.actionsGraphiques_ajouterCase(recupererPion.hauteur, recupererPion.xCell, recupererPion.yCell, TeamType.AUCUNE);
				
				if (ajouterAListeGraphiquePartie) {
					PylosPartie_actionSimple actionActuelle = new PylosPartie_actionSimple(equipeAFaireJouer, 2, pion.hauteur, pion.xCell, pion.yCell);
					NetBuffer actionActuelleAsNetBuffer = actionActuelle.writeToNetBuffer();
					partieActuelle.listeDeMessagesAEnvoyerAuxJoueurs.add(actionActuelleAsNetBuffer);
				}
				
			}
			
			// Vérifier si les pions sont biens différents :
			if (coup.listePionsARecuperer.size() == 2) {
				
				IA_v40_peutRecupererCePion recupPion1 = coup.listePionsARecuperer.get(0);
				IA_v40_peutRecupererCePion recupPion2 = coup.listePionsARecuperer.get(1);
				if (recupPion1.hauteur == recupPion2.hauteur && recupPion1.xCell == recupPion2.xCell && recupPion1.yCell == recupPion2.yCell) {
					System.err.println("ERREUR PINONS IA_v40.appliquerCoupAuPlateauActuel, PIONS IDENTIQUES.");
				}
				
				
				
				
				//System.err.println("ERREUR PINONS IA_v40.appliquerCoupAuPlateauActuel, reprendre deux pions !");
			}
			
			
			
			
			/* ancien débug - gardé pour archive
			if (coup.listePionsARecuperer.size() > 2) {
				System.err.println("ERREUR PINONS déplacement IA_v40.appliquerCoupAuPlateauActuel, plus de deux pions à reprendre coup.listePionsARecuperer.size() = " + coup.listePionsARecuperer.size());
			}
			// Vérifier si les pions sont biens différents :
			if (coup.nombreDePionsEconomises > 3) {
				System.err.println("ERREUR PINONS déplacement IA_v40.appliquerCoupAuPlateauActuel, nombreDePionsEconomises = " + coup.nombreDePionsEconomises);
			}
			// DEBUG ---
			if (pion.estUnDeplacement) {
				
				if (coup.nombreDePionsEconomises - 1 != coup.listePionsARecuperer.size())
					System.err.println("ERREUR PINONS déplacement IA_v40.appliquerCoupAuPlateauActuel, listSize = " + coup.listePionsARecuperer.size() + " !=  " + (coup.nombreDePionsEconomises - 1));
			} else {
				if (coup.nombreDePionsEconomises != coup.listePionsARecuperer.size())
					System.err.println("ERREUR PINONS bouger pion IA_v40.appliquerCoupAuPlateauActuel, listSize = " + coup.listePionsARecuperer.size() + " !=  " + (coup.nombreDePionsEconomises - 1));
			}
			*/
			
			
			if (coup.listePionsARecuperer.size() != 0 && ajouterAListeGraphiquePartie && (estServeurInternet == false)) {
				partieActuelle.actionsGraphiques_setTimer();
				return false;
			}
			//IA_v40_peutRecupererCePion recupererPion = coup.listePionsARecuperer.get(etapeGraphique - 1);
			//plateauActuel.setCell(recupererPion.hauteur, recupererPion.xCell, recupererPion.yCell, TeamType.AUCUNE);
			//return coup.listePionsARecuperer.size() - etapeGraphique;
		}
		return true;
	}
	
	/** Appliquer un coup au plateau actuel
	 *  @param plateauActuel
	 *  @param equipeAFaireJouer
	 *  @param coup
	 */
	public boolean appliquerCoupAuPlateauActuel(PylosGridArray plateauActuel, TeamType equipeAFaireJouer, IA_v40_coup coup) {
		return appliquerCoupAuPlateauActuel(plateauActuel, equipeAFaireJouer, coup, false);
	}
	
	/** Annuler un coup au plateau actuel. (annule les effets de appliquerCoupAuPlateauActuel())
	 *  Aucune vérification n'est faite, mais le coup est supposé avoir été joué,
	 *  et être le dernier coup joué.
	 *  @param plateauActuel
	 *  @param equipeAFaireJouer
	 *  @param coup
	 */
	public void annulerCoupDuPlateauActuel(PylosGridArray plateauActuel, TeamType equipeAFaireJouer, IA_v40_coup coup) {
		IA_v40_poserPion pion = coup.pionAPoserOuDeplacer;
		
		// Je remet les pions que j'ai pris, si j'en ai repris
		if (coup.listePionsARecuperer != null)
		for (int iRecupererPion = 0; iRecupererPion < coup.listePionsARecuperer.size(); iRecupererPion++) {
			IA_v40_peutRecupererCePion recupererPion = coup.listePionsARecuperer.get(iRecupererPion);
			plateauActuel.setCell(recupererPion.hauteur, recupererPion.xCell, recupererPion.yCell, equipeAFaireJouer);
		}
		
		// Je supprime le pion que j'ai posé / je déplace dans l'autre sens le pion que j'ai déplacé
		// Pas un déplacement
		if (pion.estUnDeplacement == false) {
			plateauActuel.setCell(pion.hauteur, pion.xCell, pion.yCell, TeamType.AUCUNE);
		} else { // Un déplacement
			plateauActuel.setCell(pion.hauteur_init, pion.xCell_init, pion.yCell_init, equipeAFaireJouer);
			plateauActuel.setCell(pion.hauteur, pion.xCell, pion.yCell, TeamType.AUCUNE);
		}

		// Enlever/créditer les pions
		if (equipeAFaireJouer == TeamType.BLANC) nbPionsBlanc -= -1 + coup.nombreDePionsEconomises;
		if (equipeAFaireJouer == TeamType.NOIR)   nbPionsNoir -= -1 + coup.nombreDePionsEconomises;
		
		
	}
	
	public static boolean ceJoueurPeutFaireQuelqueChosePendantSonTour(PylosPartie partie, TeamType equipeDuJoueur) {
		
		if (equipeDuJoueur == TeamType.BLANC && partie.nbJetonsBlanc > 0) return true; // Il a encore des pions, il peut faire quelque chose
		if (equipeDuJoueur == TeamType.NOIR && partie.nbJetonsNoir > 0) return true; // Il a encore des pions, il peut faire quelque chose
		//if (partie.peutReprendrePionsNb > 1) return true; // doit reprendre au moins un pion !
		
		// Si le joueur n'a plus de pions, je regarde s'il peut bouger un pion
		
		// Je fais la liste de tout les pions que je peux bouger
		 ArrayList<IA_v40_peutDeplacerCePion> listeDesPionsQueJePeuxDeplacer = listerToutesLesPionsQueJePeuxDeplacer(partie.plateauActuel, equipeDuJoueur);
		 
		 // Pour chaque pion que je peux bouger, le fais la liste de toutes les cases où je peux le bouger
		 for (int indexPionQueJePeuxDeplacer = 0; indexPionQueJePeuxDeplacer < listeDesPionsQueJePeuxDeplacer.size(); indexPionQueJePeuxDeplacer++) {
			 
			 IA_v40_peutDeplacerCePion peutDeplacerCePion = listeDesPionsQueJePeuxDeplacer.get(indexPionQueJePeuxDeplacer);
			 
			 // Liste de toutes les cases où je peux mettre mon pion
			 ArrayList<IA_v40_poserPion> toutesLesCasesOuJePeuxDeplacerCePion = listerToutesLesCasesOuJePeuxPoserMonPion(partie.plateauActuel, equipeDuJoueur, true, peutDeplacerCePion.hauteur, peutDeplacerCePion.xCell, peutDeplacerCePion.yCell);
			 
			 // S'il y a au moins une position où je peux poser le pion que je veux déplacer, je retourne vrai
			 if (toutesLesCasesOuJePeuxDeplacerCePion.size() != 0)
				 return true; // le joueur peut faire quelque chose !
		 }
		 
		 return false; // le joueur ne peut plus rien faire
	}
	
	
	/*
	 * Fonctionnement résumé :
	 * - L'IA joue le meilleur coup
	 * - L'autre joueur joue le meilleur coup
	 * ...
	 * jusqu'à fonction d'évaluation
	 * */
	
	
	private int nbPionsBlanc, nbPionsNoir;
	private PylosGridArray plateauActuel;
	private PylosPartie partieActuelle;
	private TeamType equipeIA;
	private IA_v40_coup coupAJouerCeTour;
	private IA_elagage1 elagageGlobalDeCeTour;
	public final int NOMBRE_DE_NOEUDS_RETENUS_A_UNE_PRONFONDEUR_DONNEE = 1000;
	private int profondeurMaxChoisie;
	
	public final boolean ACTIVER_ELAGAGE = false;
	//private int etapesGraphiquesRestantes, compteurEtapeGraphique;
	

	//public static ArrayList<IA_v40_coup> serveurInternetUniquement_listeCoupIA = new ArrayList<IA_v40_coup>();
	
	public static ArrayList<PylosPartie_actionSimple> serveurInternetUniquement_listeActionsSimplesIA = new ArrayList<PylosPartie_actionSimple>();
	
	public static boolean estServeurInternet = false;
	
	public void faireJouerIA(PylosPartie arg_partie, TeamType arg_equipeIA, int arg_profondeur, int nombreDeNoeudsAUneProfondeurDonnee) {
		
		nbPionsBlanc = arg_partie.nbJetonsBlanc;
		nbPionsNoir = arg_partie.nbJetonsNoir;
		plateauActuel = arg_partie.plateauActuel.copy(null);
		partieActuelle = arg_partie;
		equipeIA = arg_equipeIA;
		coupAJouerCeTour = null;
		if (ACTIVER_ELAGAGE) {
			profondeurMaxChoisie = arg_profondeur;
			elagageGlobalDeCeTour = new IA_elagage1(arg_profondeur + 20, nombreDeNoeudsAUneProfondeurDonnee);
		}
		
		// Si l'IA a encore des pions et pas le joueur, je fais en sorte que l'IA joue le premier coup valide qu'elle peut, en prenant dans sa réserve
		
		// prendre le score maximum des coups que je peux jouer
		// prendre le maximum des coups que l'autre peut jouer
		// etc.
		nombreDePossibilitesCalculeesTotal = 0;
		int scorePrevuIA = faireJouerIA_instance(true, arg_profondeur, true);
		//System.out.println("IA_v40.faireJouerIA : scorePrevuIA = " + scorePrevuIA);
		//etapesGraphiquesRestantes = 0;
		//compteurEtapeGraphique = 0;
		if (coupAJouerCeTour != null) {
			
			//partieActuelle.ajouterCoupAEffectuerLentementParIA_coup = coupAJouerCeTour;
			//partieActuelle.ajouterCoupAEffectuerLentementParIA_lastTimeDone = System.currentTimeMillis();
			
			
			//etapesGraphiquesRestantes = 
			boolean passerLeTour = appliquerCoupAuPlateauActuel(arg_partie.plateauActuel, arg_equipeIA, coupAJouerCeTour, true);
			//serveurInternetUniquement_listeCoupIA.add(coupAJouerCeTour); // clear par le serveur !!
			
			if (estServeurInternet) {
				// Décomposition du coup en actions simples, et ajout à la liste serveurInternetUniquement_listeActionsSimplesIA
				//coupAJouerCeTour.equipeQuiJoueLeCoup
				IA_v40_poserPion pionAPoserOuDeplacer = coupAJouerCeTour.pionAPoserOuDeplacer;
				PylosPartie_actionSimple action1 = new PylosPartie_actionSimple();
				if (pionAPoserOuDeplacer.estUnDeplacement) {
					action1.typeAction = 1;
					action1.equipeQuiJoueLeCoup = arg_equipeIA;
					action1.hauteur = pionAPoserOuDeplacer.hauteur;
					action1.xCell = pionAPoserOuDeplacer.xCell;
					action1.yCell = pionAPoserOuDeplacer.yCell;
					action1.hauteur_init = pionAPoserOuDeplacer.hauteur_init;
					action1.xCell_init = pionAPoserOuDeplacer.xCell_init;
					action1.yCell_init = pionAPoserOuDeplacer.yCell_init;
				} else {
					action1.typeAction = 0;
					action1.equipeQuiJoueLeCoup = arg_equipeIA;
					action1.hauteur = pionAPoserOuDeplacer.hauteur;
					action1.xCell = pionAPoserOuDeplacer.xCell;
					action1.yCell = pionAPoserOuDeplacer.yCell;
				}
				// Ajout de la prmeière action
				serveurInternetUniquement_listeActionsSimplesIA.add(action1);
				
				// Reprendre des pions
				//int nbReprendre = coupAJouerCeTour.listePionsARecuperer.size();
				if (coupAJouerCeTour.listePionsARecuperer != null)
				for (IA_v40_peutRecupererCePion recupPion : coupAJouerCeTour.listePionsARecuperer) { //int iReprendre = 0; iReprendre < nbReprendre; iReprendre++) {
					PylosPartie_actionSimple actionRecuperer = new PylosPartie_actionSimple();
					actionRecuperer.typeAction = 2;
					actionRecuperer.equipeQuiJoueLeCoup = arg_equipeIA;
					actionRecuperer.hauteur = recupPion.hauteur;
					actionRecuperer.xCell = recupPion.xCell;
					actionRecuperer.yCell = recupPion.yCell;
					serveurInternetUniquement_listeActionsSimplesIA.add(actionRecuperer);
				}
			}
			//compteurEtapeGraphique++;
			
			arg_partie.nbJetonsBlanc = nbPionsBlanc;
			arg_partie.nbJetonsNoir = nbPionsNoir;
			arg_partie.joueurAJoueUnPion = true;
			arg_partie.peutReprendrePionsNb = 0;
			
			
			
			//coupAJouerCeTour.drawOnScreen();
			
			
			System.out.println("IA_v40.faireJouerIA : nombreDePossibilitesCalculeesTotal = " + nombreDePossibilitesCalculeesTotal);
			
			boolean graphicPeutPasserTour = partieActuelle.actionsGraphiques_peutPasserLeTour();
			
			if (estServeurInternet) {
				graphicPeutPasserTour = true;
			}
			
			if (passerLeTour && graphicPeutPasserTour) {
				arg_partie.tourSuivant(); // le tour sera passé automatiquement sinon, plus tard, via PylosPartie.actionsGraphiques_loopEffectuerAction();
				
				if (estServeurInternet) {
					PylosPartie_actionSimple actionPasserTour = new PylosPartie_actionSimple(3);
					actionPasserTour.equipeQuiJoueLeCoup = arg_equipeIA;
					serveurInternetUniquement_listeActionsSimplesIA.add(actionPasserTour);
				}
			}
			
			
			
		} else {
			//System.err.println("ERREUR IA_v40.faireJouerIA : coupAJouerCeTour == null");
			if (partieActuelle.actionsGraphiques_peutPasserLeTour() || estServeurInternet) {
				arg_partie.tourSuivant();
				if (estServeurInternet) {
					PylosPartie_actionSimple actionPasserTour = new PylosPartie_actionSimple(3);
					actionPasserTour.equipeQuiJoueLeCoup = arg_equipeIA;
					serveurInternetUniquement_listeActionsSimplesIA.add(actionPasserTour);
				}
			}
		}
		
		
		
	}
	
	
	/** 
	 *  @return true s'il reste encore des étapes graphiques
	 */
	/*public static boolean faireJouerIA_graphiquement() {
		
		etapesGraphiquesRestantes = appliquerCoupAuPlateauActuel(arg_partie.plateauActuel, arg_equipeIA, coupAJouerCeTour, compteurEtapeGraphique);
		compteurEtapeGraphique++;
		return (etapesGraphiquesRestantes > 0);
		
	}*/
	
	private int nombreDePossibilitesCalculeesTotal;
	
	private int calculerSoreDeIA() {
		// equipe
		if (equipeIA == TeamType.NOIR) return nbPionsNoir - nbPionsBlanc; // score pour l'équipe noire
		if (equipeIA == TeamType.BLANC) return nbPionsBlanc - nbPionsNoir; // score pour l'équipe blanche
		
		/*
		if (equipeQuiJoue == TeamType.NOIR) return nbPionsNoir - nbPionsBlanc; // score pour l'équipe noire
		if (equipeQuiJoue == TeamType.BLANC) return nbPionsBlanc - nbPionsNoir; // score pour l'équipe blanche
		*/
		return -10000; // en cas d'erreur (improbable, ne devrait pas arriver)
		
	}
	
	private boolean ACTIVER_ELAGAGE_GWENDAL = false;
	
	/** 
	 *  @param tourDeIA
	 *  @param arg_profondeurRestante
	 *  @return le score de l'IA
	 */
	private int faireJouerIA_instance(boolean tourDeIA,  int arg_profondeurRestante, boolean premiereIteration) {
		
		if (ACTIVER_ELAGAGE_GWENDAL) {
			//"Elagage": quand le score du noeud ( la diffenrece entre les points  noirs et blancs) depasse la limite subjective, on ne continue pas son exploitation pour eviter des calcus inutiles 
			if (equipeIA == TeamType.NOIR && (nbPionsNoir - nbPionsBlanc) >= 3)
					return -500; // score pour l'équipe noire
			if (equipeIA == TeamType.BLANC && (nbPionsBlanc - nbPionsNoir) >= 3) // score pour l'équipe blanche
					return -500;
		}
		
		
		if (ACTIVER_ELAGAGE) {
			int profondeurActuelle = profondeurMaxChoisie - arg_profondeurRestante;
			boolean calculerCeNoeud = elagageGlobalDeCeTour.testerScoreNoeud(profondeurActuelle, calculerSoreDeIA(), tourDeIA);
			if (calculerCeNoeud == false) return -10000;
		}
		nombreDePossibilitesCalculeesTotal++;
		
		TeamType equipeQuiJoue;
		if (tourDeIA) equipeQuiJoue = equipeIA;
		else          equipeQuiJoue = equipeIA.equipeOpposee();
		
		int nombreDePionsAdversaire;
		switch (equipeIA.equipeOpposee()) {
		case NOIR : nombreDePionsAdversaire = partieActuelle.nbJetonsNoir; break;
		case BLANC : nombreDePionsAdversaire = partieActuelle.nbJetonsBlanc; break;
		default : nombreDePionsAdversaire = 0; break;
		}
		
		// Première itération (si premiereIteration == true) (au passage, c'est le tour de l'IA, forcément !)
		// Ici, je regarde si mon adversaire n'a plus de pions.
		// Si c'est le cas, je met le premier pion que j'ai en réserve sur le terrain
		if (premiereIteration) {
			boolean iaPeutFaireQuelqueChose = ceJoueurPeutFaireQuelqueChosePendantSonTour(partieActuelle, equipeQuiJoue/* = equipeIA*/);
			//System.out.println("IA_v40.faireJouerIA_instance : iaPeutFaireQuelqueChose = " + iaPeutFaireQuelqueChose + "  tourDeIA="+tourDeIA + "  " + partieActuelle.equipeIA);
			if (iaPeutFaireQuelqueChose == false) {
				return 0; // l'IA ne peut rien faire
			}
			if (nombreDePionsAdversaire <= 0) {
				// Je regarde la première case où je peux poser mon pion
				
				// Je parcours toutes les grilles
				for (int iGrid = 0; iGrid <= plateauActuel.getHauteurMax(); iGrid++) {
					PylosGrid grid = plateauActuel.a1Grid[iGrid];
					
					// Je parcours les cases de la grille de hauteur iGrid
					for (int xCell = 0; xCell < grid.gridWidth; xCell++) for (int yCell = 0; yCell < grid.gridHeight; yCell++) { // toutes les cases en x et en y
						
						if (grid.getTeamAtCellPosition_noCheck(xCell, yCell) == TeamType.AUCUNE) {
							IA_v40_coup coup = new IA_v40_coup();
							IA_v40_poserPion pionAPoser = new IA_v40_poserPion(iGrid, xCell, yCell, false);
							coup.pionAPoserOuDeplacer = pionAPoser;
							coupAJouerCeTour = coup;
							return 0; // l'IA va faire quelque chose car coupAJouerCeTour != null
						}
					}
				}

				//System.out.println("IA_v40.faireJouerIA_instance : nombreDePionsAdversaire = " + nombreDePionsAdversaire);
			}
			// Si j'ai des pions 
		}
		
		if (premiereIteration == false) // s'il s'agit de la première itération et que je peux jouer (j'ai fait return 0; si je ne peux pas jouer, ci-dessus)
		if (arg_profondeurRestante <= 0 || nbPionsBlanc <= 0 || nbPionsNoir <= 0) {
			
			return calculerSoreDeIA();
			/*
			if (equipeIA == TeamType.NOIR) return nbPionsNoir - nbPionsBlanc; // score pour l'équipe noire
			if (equipeIA == TeamType.BLANC) return nbPionsBlanc - nbPionsNoir; // score pour l'équipe blanche
			
			/ *
			if (equipeQuiJoue == TeamType.NOIR) return nbPionsNoir - nbPionsBlanc; // score pour l'équipe noire
			if (equipeQuiJoue == TeamType.BLANC) return nbPionsBlanc - nbPionsNoir; // score pour l'équipe blanche
			* /
			return -10000; // en cas d'erreur (improbable, ne devrait pas arriver)
			*/
		}
		
		ArrayList<IA_v40_coup> listeDeTousLesCoupsPossibles = listerTousLesCoupsPossiblesCeTour(plateauActuel, equipeQuiJoue);
		
		int coupRetenuIndex = -1;
		int coupRetenuScore = -10000;
		// Si c'est le tour de l'IA : je maximise son score, je prend la meilleure branche
		// Si c'est le tour de l'adversaire : je minimise le score de l'IA
		for (int iCoup = 0; iCoup < listeDeTousLesCoupsPossibles.size(); iCoup++) {
			IA_v40_coup coup = listeDeTousLesCoupsPossibles.get(iCoup);
			appliquerCoupAuPlateauActuel(plateauActuel, equipeQuiJoue, coup);
			
			int score = faireJouerIA_instance((tourDeIA == false), arg_profondeurRestante - 1, false);
			if (coupRetenuIndex == -1) {
				coupRetenuScore = score; // initialisation du score s'il n'est pas encore initialisé
				coupRetenuIndex = iCoup;
			}
			
			if (tourDeIA) { // maximiser le score de l'IA
				if (score > coupRetenuScore) {
					coupRetenuScore = score; // le score actuel est plus grand que le score retenu, je prends ce coup.
					coupRetenuIndex = iCoup;
				}
			} else { // tour adverse, minimiser le score de l'IA (maximiser le score adverse)
				if (score < coupRetenuScore) {
					coupRetenuScore = score; // le score actuel est plus petit que le score retenu, je prends ce coup.
					coupRetenuIndex = iCoup;
				}
			}
			
			annulerCoupDuPlateauActuel(plateauActuel, equipeQuiJoue, coup);
		}
		
		// Je prends le coup choisi (meilleur coup pour l'IA ou pire coup pour l'IA suivant que c'est à son tour de jouer ou à l'adversaire)
		if (coupRetenuIndex != -1) {
			// Je prends le meilleur coup (selon mes critères)
			IA_v40_coup coup = listeDeTousLesCoupsPossibles.get(coupRetenuIndex);
			// j'ajoute ce coup au plateau
			appliquerCoupAuPlateauActuel(plateauActuel, equipeQuiJoue, coup);
			// Je fais jouer en récursif
			int result = faireJouerIA_instance((tourDeIA == false), arg_profondeurRestante - 1, false);
			// J'annule le coup prédécent
			annulerCoupDuPlateauActuel(plateauActuel, equipeQuiJoue, coup);
			coupAJouerCeTour = coup;
			return result;
			
		} else return -10000; // erreur
	}
	
	// -> faire une fonction pour voir si un joueur peut bouger un pion s'il n'a plus de pions. Pour ne pas passer son tour automatiquement s'il peut encore faire quelque chose !!
	
	
	
	public static void playOnce(PylosPartie partie, TeamType monEquipe, int profondeurDeRecherche, int nombreDeNoeudsAUneProfondeurDonnee) {
		IA_v40 ia = new IA_v40();
		
		ia.faireJouerIA(partie, monEquipe, profondeurDeRecherche, nombreDeNoeudsAUneProfondeurDonnee);
		ia = null;
	}
	
	// Implémenté par compatilité pour IA_v1 et v2
	//public static void joueUnCoup(TeamType equipeAJouer, PylosPartie partie, int profondeurRecherche) {
	//	playOnce(partie, equipeAJouer, profondeurRecherche);
	//}
	
	
	// Fonctions d'élagage : liste des scores des noeuds : je ne retiens que les 40 ou 100 meilleurs noeuds, peu importe la profondeur : score minimal que doit avoir un noeud pour ne pas être abandonné
	// -> augmentation pseudo-linéaire et non exponentielle
	
	
	
	
}


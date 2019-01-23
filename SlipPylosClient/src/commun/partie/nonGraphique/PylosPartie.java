package commun.partie.nonGraphique;


public class PylosPartie {
	
	public int nbJetonsBlanc = 0;//15;
	public int nbJetonsNoir = 0;//15;
	public static final int nbCasesCote = 4; // nombre de cases par coté
	private int nbJetonsTotal;
	public int hauteurMax = nbCasesCote - 1;
	//public PylosGrid[] a1Grid;
	public PylosGridArray plateauActuel;
	public TeamType tourDe = TeamType.NOIR;
	//public IA_v1 ia = new IA_v1(TeamType.NOIR, this);
	public TeamType equipeJoueur = TeamType.BLANC;
	public TeamType equipeIA = TeamType.NOIR; // si il y a une IA (solo local)
	public PylosPartieVariante varianteDuJeu = PylosPartieVariante.JOUEURS_AVERTIS;
	
	
	//public boolean joueurAUtiliseSaReserve = false; // 1 utilisation de la réserve de pions par tour, max
	public boolean joueurAJoueUnPion = false; // Le joueur ne peut jouer qu'une seule fois un pion (poser un pion de sa réserve ou bouger un de ses pions pour le réhausser)
	public int peutReprendrePionsNb = 0; // Si le joueur peut reprendre des pions (après avoir joué)
	public ModeDeJeu modeDeJeu = ModeDeJeu.HOT_SEAT; // par défaut, la partie est en local
	
	
	public int computeNbJetonsTotal() {
		int nbJetons = 0;
		for (int lenCote = nbCasesCote; lenCote >= 1; lenCote--) {
			nbJetons += lenCote * lenCote;
		}
		return nbJetons;
	}
	
	/** 
	 * @param arg_modeDeJeu
	 * @param cEstLeTourDe
	 * @param equipeJoueurActuel ma couleur, la couleur du joueur du client actuel
	 */
	public PylosPartie(ModeDeJeu arg_modeDeJeu, TeamType cEstLeTourDe, TeamType equipeJoueurActuel) { // constructeur
		plateauActuel = new PylosGridArray(nbCasesCote, this);
		nbJetonsTotal = computeNbJetonsTotal();
		nbJetonsBlanc = nbJetonsTotal / 2;
		nbJetonsNoir = nbJetonsTotal / 2;
		modeDeJeu = arg_modeDeJeu;
		
		tourDe = cEstLeTourDe;
		if (tourDe != TeamType.NOIR && tourDe != TeamType.BLANC) // erreur dans l'équipe qui doit jouer
			tourDe = TeamType.BLANC;

		if (equipeJoueurActuel != TeamType.NOIR && equipeJoueurActuel != TeamType.BLANC) // erreur dans l'équipe qui doit jouer
			equipeJoueurActuel = TeamType.BLANC;
		equipeJoueur = equipeJoueurActuel; // écrasé si HOT_SEAT
		
		if (modeDeJeu == ModeDeJeu.HOT_SEAT) {
			equipeJoueur = tourDe;
		}
		
		if (modeDeJeu == ModeDeJeu.SOLO_LOCAL) {
			if (equipeJoueur == TeamType.BLANC) equipeIA = TeamType.NOIR;
			if (equipeJoueur == TeamType.NOIR) equipeIA = TeamType.BLANC;
		}
		
		if (tourDe != equipeJoueur && modeDeJeu == ModeDeJeu.SOLO_LOCAL) {
			faireJouerIA();
			tourSuivant();
		}
		

		
		
		
		// Création de toutes les grilles
		/*a1Grid = new PylosGrid[nbCasesCote];
		for (int hauteurActuelle = 0; hauteurActuelle <= hauteurMax; hauteurActuelle++) {
			a1Grid[hauteurActuelle] = new PylosGrid(nbCasesCote - hauteurActuelle, nbCasesCote - hauteurActuelle, hauteurActuelle, this);
		}*/
	}
	public void setCell(int gridHeight, int xCell, int yCell, TeamType teamType) {
		plateauActuel.setCell(gridHeight, xCell, yCell, teamType);
		
		
		
		//a1Grid[gridHeight].setCell(xCell, yCell, teamType);
	}
	public PylosGrid getGrid(int hauteur) {
		if (hauteur < 0) return null;
		if (hauteur >= hauteurMax) return null;
		return plateauActuel.a1Grid[hauteur];
	}
	
	public boolean tourSuivant() {
		if (nbJetonsBlanc <= 0 && nbJetonsNoir <= 0) return false; // fin de la partie !
		
		if (nbJetonsBlanc == 0 || nbJetonsNoir == 0) {
			peutReprendrePionsNb = 0;
			joueurAJoueUnPion = true;
		}
		
		int pionsRestantsAuJoueurActuel = 0;
		int pionsRestantsAuJoueurSuivant = 0;
		switch (tourDe) {
		case BLANC :
			pionsRestantsAuJoueurActuel = nbJetonsBlanc;
			pionsRestantsAuJoueurSuivant = nbJetonsNoir;
			break;
		case NOIR :
			pionsRestantsAuJoueurActuel = nbJetonsNoir;
			pionsRestantsAuJoueurSuivant = nbJetonsBlanc;
			break;
		default : break;
		}
		
		if (pionsRestantsAuJoueurActuel > 0) { // encore des pions
			if (!joueurAJoueUnPion) { // encore des pions et pas encore joué : impossible de passer le tour
				System.out.println("PylosPartie : Impossible de passer votre tour lorsque vous n'avez pas encore joué !");
				//LogWriter.Log("MyMouseListener.mousePressed : Impossible de passer votre tour lorsque vous n'avez pas encore joué !");
				return false;
			}
		}
		
		if (peutReprendrePionsNb > 1) { // doit reprendre des pions : impossible de passer le tour
			System.out.println("PylosPartie : Impossible de passer votre tour lorsque vous n'avez repris au moins un des pions qui vons sont dûs !");
			//LogWriter.Log("MyMouseListener.mousePressed : Impossible de passer votre tour lorsque vous n'avez repris au moins un des pions qui vons sont dûs !");
			return false;
		}
		
		peutReprendrePionsNb = 0;
		joueurAJoueUnPion = false;
		
		boolean passerLeTourDuJoueurSuivant = false;
		// Si le joueur actuel a encore des pions et a joué, et que le joueur suivant n'a plus de pions : c'est à nouveau le tour du joueur actuel.
		if (pionsRestantsAuJoueurActuel != 0 && pionsRestantsAuJoueurSuivant <= 0) {
			passerLeTourDuJoueurSuivant = true;
		}
		
		
		// Si un des joueurs n'a plus de pions, je passe son tour, pour laisser l'adversaire finir la pyramide
		
		if (passerLeTourDuJoueurSuivant == false) {
			// Changement de l'équipe qui joue
			if (tourDe == TeamType.BLANC)
				tourDe = TeamType.NOIR;
			else
				tourDe = TeamType.BLANC;
		}
		
		
		
		
		/*if (modeDeJeu == ModeDeJeu.SOLO_LOCAL) {
		}*/
		
		if (modeDeJeu == ModeDeJeu.HOT_SEAT) {
			equipeJoueur = tourDe;
		}
		
		/*if (tourDe == ia.equipe) {
			ia.joueUnCoup();
			System.out.println("PylosPartie : Tour suivant");
			tourSuivant();
		}*/
		
		// Jeu contre l'IA en solo local
		if (modeDeJeu == ModeDeJeu.SOLO_LOCAL && tourDe != equipeJoueur) {
			faireJouerIA();
			tourSuivant();
		}
		
		return true;
	}
	
	private void faireJouerIA() {
		//IA_v0_stupid.joueUnCoup(TeamType.NOIR, this, 4000);
		
		IA_v4.joueUnCoup(equipeIA, this, 4);
		//IA_v4.joueUnCoup(TeamType.NOIR, this, 4);
		joueurAJoueUnPion = true;
		// peutReprendrePionsNb = 0; normallement géré par l'IA
		System.out.println("PylosPartie : faireJouerIA()");
	}
	
	// Poser un pion à partir de sa réserve
	public boolean poseUnPionDeSaReserve(TeamType equipeQuiFaitAction, int hauteur, int xCell, int yCell) {
		if (tourDe != equipeQuiFaitAction) return false; // si ce n'est pas à cette équipe de jouer
		if (joueurAJoueUnPion) return false; // déjà joué ce tour, impossible de rejouer
		if (plateauActuel.canPlaceAtPosition(hauteur, xCell, yCell) == false) return false; // position invalide, ou pas de cellule en-dessous
		// Je vérifie le nombre de jetons restant
		switch (tourDe) {
		case NOIR :
			if (nbJetonsNoir <= 0) return false; // pas assez de jetons
			nbJetonsNoir--;
			break;
		case BLANC :
			if (nbJetonsBlanc <= 0) return false; // pas assez de jetons
			nbJetonsBlanc--;
			break;
		default : break;
		}
		// cf l. 136
		// Ici, la position est valide et j'ai assez de jetons, je peux poser mon pion !
		// J'ajoute le nouveau pion
		setCell(hauteur, xCell, yCell, equipeQuiFaitAction);
		joueurAJoueUnPion = true;
		boolean doitReprendreDesPions = false;
		if (plateauActuel.willFormSameColorRectangle(hauteur, xCell, yCell, equipeQuiFaitAction)) {
			doitReprendreDesPions = true;
		}
		if (plateauActuel.willFormSameColorLine(hauteur, xCell, yCell, equipeQuiFaitAction)) {
			doitReprendreDesPions = true;
		}
		if (doitReprendreDesPions) { // Si je pose mon dernier pion et que peux en reprendre :
			// J'oblige à en reprendre si mon adversaire a encore des pions;
			// le seul cas où je ne demande pas de reprendre des pions est quand l'adversaire n'a plus de pions
			if (equipeQuiFaitAction == TeamType.BLANC && nbJetonsNoir == 0) doitReprendreDesPions = false;
			if (equipeQuiFaitAction == TeamType.NOIR && nbJetonsBlanc == 0) doitReprendreDesPions = false;
		}
		if (doitReprendreDesPions) peutReprendrePionsNb = 2;
		
		if ((nbJetonsBlanc == 0 || nbJetonsNoir == 0) && peutReprendrePionsNb <= 1) {
			peutReprendrePionsNb = 0;
			tourSuivant();
		}
		return true;
	}
	
	public boolean deplacerUnPion(TeamType equipeQuiFaitAction, int hauteur, int xCell, int yCell, int hauteur_initiale, int xCell_initiale, int yCell_initiale) {
		if (tourDe != equipeQuiFaitAction) return false; // si ce n'est pas à cette équipe de jouer
		if (joueurAJoueUnPion) return false; // déjà joué ce tour, impossible de rejouer
		if (plateauActuel.canPlaceAtPosition(hauteur, xCell, yCell) == false) return false; // position invalide, ou pas de cellule en-dessous
		boolean peutBougerPion = plateauActuel.canMovePawn(hauteur, xCell, yCell, hauteur_initiale, xCell_initiale, yCell_initiale);
		if (peutBougerPion == false) return false;
		// Je supprime l'ancien pion
		setCell(hauteur_initiale, xCell_initiale, yCell_initiale, TeamType.AUCUNE);
		// J'ajoute le nouveau pion
		setCell(hauteur, xCell, yCell, equipeQuiFaitAction);
		joueurAJoueUnPion = true;
		if (plateauActuel.willFormSameColorRectangle(hauteur, xCell, yCell, equipeQuiFaitAction)) {
			peutReprendrePionsNb = 2;
		}
		if (plateauActuel.willFormSameColorLine(hauteur, xCell, yCell, equipeQuiFaitAction)) {
			peutReprendrePionsNb = 2;
		}
		if ((nbJetonsBlanc == 0 || nbJetonsNoir == 0) && peutReprendrePionsNb <= 1) {
			peutReprendrePionsNb = 0;
			tourSuivant();
		}
		return true;
	}
	

	public void reprendUnPion(TeamType equipeQuiFaitAction, int hauteur, int xCell, int yCell) {
		// equipeQuiFaitAction == tourDe, obligatoirement
		
		setCell(hauteur, xCell, yCell, TeamType.AUCUNE);
		if (equipeQuiFaitAction == TeamType.BLANC) nbJetonsBlanc++;
		if (equipeQuiFaitAction == TeamType.NOIR)  nbJetonsNoir++;
		
		peutReprendrePionsNb--;
	}
	
	/** Passe le tour automatiquement si c'est possible : TODO
	 * @return
	 */
	public boolean tourSuivant_automatique() {
		// TODO TODOOUUUUU !
		return false;
	}
	
	public TeamType getEquipeAdverse() {
		if (equipeJoueur == TeamType.NOIR) return TeamType.BLANC;
		if (equipeJoueur == TeamType.BLANC) return TeamType.NOIR;
		return TeamType.AUCUNE;
	}
	
}

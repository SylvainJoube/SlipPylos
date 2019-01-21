package commun.partie.nonGraphique;


public class PylosPartie {
	
	public int nbJetonsBlanc = 0;//15;
	public int nbJetonsNoir = 0;//15;
	public static final int nbCasesCote = 3; // nombre de cases par c�t�
	private int nbJetonsTotal;
	public int hauteurMax = nbCasesCote - 1;
	//public PylosGrid[] a1Grid;
	public PylosGridArray plateauActuel;
	public TeamType tourDe = TeamType.NOIR;
	//public IA_v1 ia = new IA_v1(TeamType.NOIR, this);
	public TeamType equipeJoueur = TeamType.BLANC;
	
	
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
	
	public PylosPartie(ModeDeJeu arg_modeDeJeu) { // constructeur
		plateauActuel = new PylosGridArray(nbCasesCote);
		nbJetonsTotal = computeNbJetonsTotal();
		nbJetonsBlanc = nbJetonsTotal / 2 + 200;
		nbJetonsNoir = nbJetonsTotal / 2 + 200;
		modeDeJeu = arg_modeDeJeu;
		if (modeDeJeu == ModeDeJeu.HOT_SEAT) {
			equipeJoueur = tourDe;
		}
		
		if (tourDe != equipeJoueur && modeDeJeu == ModeDeJeu.SOLO_LOCAL) {
			faireJouerIA();
			tourSuivant();
		}
		
		
		// Cr�ation de toutes les grilles
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
			
			/*if (tourDe == TeamType.NOIR) {
				
				
				IA_v0_stupid.joueUnCoup(TeamType.NOIR, this, 4000);
				joueurAJoueUnPion = true;
				// peutReprendrePionsNb = 0; normallement géré par l'IA
				//IA_v4.joueUnCoup(TeamType.NOIR, this, 4);
				System.out.println("PylosPartie : Tour suivant (l'IA vient de jouer)");
				/*try {
					Thread.sleep(0);
				} catch (InterruptedException e) {
					
				}* /
				tourSuivant();
			}
		}*/
		/*if (tourDe == TeamType.BLANC) {
			IA_v4.joueUnCoup(TeamType.BLANC, this, 4);
			System.out.println("PylosPartie : Tour suivant");
			try {
				Thread.sleep(40);
			} catch (InterruptedException e) {
				
			}
			tourSuivant();
		}*/
		
		return true;
	}
	
	private void faireJouerIA() {
		IA_v0_stupid.joueUnCoup(TeamType.NOIR, this, 4000);
		joueurAJoueUnPion = true;
		System.out.println("PylosPartie : faireJouerIA()");
	}
	
	
}

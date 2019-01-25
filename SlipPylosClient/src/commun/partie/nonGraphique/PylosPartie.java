package commun.partie.nonGraphique;

import java.util.ArrayList;

import slip.network.buffers.NetBuffer;
import slip.network.tcp.TCPClient;

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
	private TCPClient autreJoueurTCPLocal = null; // seulement utile en TCP local
	
	public int numeroDeTour = 0;
	public boolean modeServeurInternet = false; // false si client, true si exécuté depuis le serveur
	// + ici : variables nécessaires au serveur
	
	
	// Pour une partie de type "INTERNET", chaque action est mise (dans le bon ordre !) dans la liste listeDeMessagesAEnvoyerAuxJoueurs.
	// Ainsi, le serveur de jeu n'a qu'à récupérer le NetBuffer et vérifier si le coup est valide, puis à le ré-envoyer aux clients.
	
	//public ArrayList<NetBuffer> listeDeMessagesAEnvoyerAuxJoueurs = new ArrayList<NetBuffer>();
	//private PylosPartie_coup coupEnCours = null; // est ajouté à listeDeMessagesAEnvoyerAuxJoueurs

	
	public ArrayList<NetBuffer> listeDeMessagesAEnvoyerAuxJoueurs = new ArrayList<NetBuffer>();
	public ArrayList<NetBuffer> listeDeMessagesEnvoyesMaisPasRecusDuServeur = new ArrayList<NetBuffer>(); // pour vérifier que tout mes coups ont bien été pris en compte
	
	
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
	public PylosPartie(ModeDeJeu arg_modeDeJeu, TeamType cEstLeTourDe, TeamType equipeJoueurActuel, TCPClient arg_autreJoueurTCPLocal, boolean arg_modeServeurInternet) { // constructeur
		plateauActuel = new PylosGridArray(nbCasesCote, this);
		nbJetonsTotal = computeNbJetonsTotal();
		nbJetonsBlanc = nbJetonsTotal / 2;
		nbJetonsNoir = nbJetonsTotal / 2;
		modeDeJeu = arg_modeDeJeu;
		autreJoueurTCPLocal = arg_autreJoueurTCPLocal;
		modeServeurInternet = arg_modeServeurInternet;
		
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
		return tourSuivant(true);
	}
	
	public boolean tourSuivant(boolean sendToLocalNetwork /* = false en valeur défaut*/) {
		//System.out.println("PylosPartie.tourSuivant : tourDe = " + tourDe);
		if (nbJetonsBlanc <= 0 && nbJetonsNoir <= 0) return false; // fin de la partie !
		
		// Je regarde 
		/*
		if (nbJetonsBlanc == 0 || nbJetonsNoir == 0) {
			peutReprendrePionsNb = 0;
			joueurAJoueUnPion = true;
		}
		*/
		
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
		
		
		
		//if (pionsRestantsAuJoueurActuel > 0)  // encore des pions
		if (IA_v40.ceJoueurPeutFaireQuelqueChosePendantSonTour(this, tourDe)) { // encore quelque chose à faire
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
		numeroDeTour++;
		
		boolean passerLeTourDuJoueurSuivant = false;
		// Si le joueur actuel a encore des pions et a joué, et que le joueur suivant n'a plus de pions : c'est à nouveau le tour du joueur actuel.
		boolean leJoueurSuivantPeutFaireQuelqueChose = IA_v40.ceJoueurPeutFaireQuelqueChosePendantSonTour(this, tourDe.equipeOpposee());
		if (pionsRestantsAuJoueurActuel != 0 && (leJoueurSuivantPeutFaireQuelqueChose == false) ) { // pionsRestantsAuJoueurSuivant <= 0
			passerLeTourDuJoueurSuivant = true;
		}
		
		
		// Si un des joueurs n'a plus de pions, je passe son tour, pour laisser l'adversaire finir la pyramide
		
		if (passerLeTourDuJoueurSuivant == false) {
			// Changement de l'équipe qui joue
			if (tourDe == TeamType.BLANC)
				tourDe = TeamType.NOIR;
			else
				tourDe = TeamType.BLANC;

			//IA_v40.listerTousLesCoupsPossiblesCeTour(plateauActuel, tourDe);
		} else numeroDeTour++; // deux tours en un seul
		
		PylosPartie_actionSimple actionActuelle = new PylosPartie_actionSimple(3);
		NetBuffer actionPasserleTour = actionActuelle.writeToNetBuffer();
		listeDeMessagesAEnvoyerAuxJoueurs.add(actionPasserleTour);
		
		//System.out.println("PylosPartie.tourSuivant : passerLeTourDuJoueurSuivant = " + passerLeTourDuJoueurSuivant);
		
		
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
		
		if (sendToLocalNetwork && modeDeJeu == ModeDeJeu.RESEAU_LOCAL) {
			if (autreJoueurTCPLocal == null) {
				// erreur
				System.out.println("ERREUR : PylosPartie.tourSuivant() autre client = null");
			}
			if (autreJoueurTCPLocal.isConnected()) {
				System.out.println("PylosPartie.tourSuivant() envoyer à autre client !");
				NetBuffer message = new NetBuffer();
				message.writeInt(4);
				autreJoueurTCPLocal.sendMessage(message);
			} else {
				System.out.println("ERREUR : PylosPartie.tourSuivant() autre client non connecté");
			}
		}
		
		/* débug -  IA vs IA
		equipeIA = equipeIA.equipeOpposee();
		tourDe = equipeIA;
		equipeJoueur = equipeIA.equipeOpposee();*/
		
		// Jeu contre l'IA en solo local
		if (modeDeJeu == ModeDeJeu.SOLO_LOCAL && tourDe != equipeJoueur) {
			faireJouerIA();
			//tourSuivant();  faireJouerIA s'occupe de faire passer le tour si besoin (peut aussi attendre actionsGraphiques_loopEffectuerAction() s'il y a des actions graphiques unitaires à afficher)
		}
		//System.out.println("PylosPartie.tourSuivant : OKKK tourDe = " + tourDe);
		return true;
	}
	
	public void tourSuivant_force() {
		
	}
	
	private void faireJouerIA() {
		//IA_v0_stupid.joueUnCoup(TeamType.NOIR, this, 4000);
		
		IA_v40.playOnce(this, equipeIA, 4, 8000);
		
		//IA_v4.joueUnCoup(TeamType.NOIR, this, 4);
		//joueurAJoueUnPion = true;
		// peutReprendrePionsNb = 0; normallement géré par l'IA
		//System.out.println("PylosPartie : faireJouerIA()");
	}
	
	// Poser un pion à partir de sa réserve
	public boolean poseUnPionDeSaReserve(TeamType equipeQuiFaitAction, int hauteur, int xCell, int yCell) {
		if (tourDe != equipeQuiFaitAction) return false; // si ce n'est pas à cette équipe de jouer
		if (plateauActuel.isValidPawnPosition(hauteur, xCell, yCell) == false) return false;
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

		PylosPartie_actionSimple actionActuelle = new PylosPartie_actionSimple(equipeQuiFaitAction, 0, hauteur, xCell, yCell);
		NetBuffer actionActuelleAsNetBuffer = actionActuelle.writeToNetBuffer();
		listeDeMessagesAEnvoyerAuxJoueurs.add(actionActuelleAsNetBuffer);
		//System.err.println("CLI PylosPartie.poseUnPionDeSaReserve equipeQuiFaitAction = " + equipeQuiFaitAction.asInt);
		//System.err.println("CLI PylosPartie.poseUnPionDeSaReserve equipeQuiFaitAction = " + actionActuelle.equipeQuiJoueLeCoup.asInt);
		//System.err.println("CLI PylosPartie.poseUnPionDeSaReserve xCell = " + xCell);
		
		//NetBuffer actionActuelleLueAsNetBuffer = new NetBuffer();
		//PylosPartie_actionSimple actionActuelleLue = PylosPartie_actionSimple.readFromNetBuffer(actionActuelleAsNetBuffer);
		//System.err.println("CLI VERIF PylosPartie.poseUnPionDeSaReserve equipeQuiFaitAction = " + actionActuelleLue.equipeQuiJoueLeCoup.asInt);
		//System.err.println("CLI VERIF PylosPartie.poseUnPionDeSaReserve xCell = " + actionActuelleLue.xCell);
		
		
		
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
		if (plateauActuel.isValidPawnPosition(hauteur, xCell, yCell) == false) return false;
		if (plateauActuel.isValidPawnPosition(hauteur_initiale, xCell_initiale, yCell_initiale) == false) return false;
		if (plateauActuel.canPlaceAtPosition(hauteur, xCell, yCell) == false) return false; // position invalide, ou pas de cellule en-dessous
		boolean peutBougerPion = plateauActuel.canMovePawn(hauteur, xCell, yCell, hauteur_initiale, xCell_initiale, yCell_initiale);
		if (peutBougerPion == false) return false;
		plateauActuel.deplacerUnPion_forcerDepuisPartie(equipeQuiFaitAction, hauteur, xCell, yCell, hauteur_initiale, xCell_initiale, yCell_initiale);
		
		
		PylosPartie_actionSimple actionActuelle = new PylosPartie_actionSimple(equipeQuiFaitAction, 1, hauteur, xCell, yCell, hauteur_initiale, xCell_initiale, yCell_initiale);
		NetBuffer actionActuelleAsNetBuffer = actionActuelle.writeToNetBuffer();
		listeDeMessagesAEnvoyerAuxJoueurs.add(actionActuelleAsNetBuffer);
		
		/* = ce qui est en-dessous :
		// Je supprime l'ancien pion
		setCell(hauteur_initiale, xCell_initiale, yCell_initiale, TeamType.AUCUNE);
		// J'ajoute le nouveau pion
		setCell(hauteur, xCell, yCell, equipeQuiFaitAction);
		*/
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
	
	// objet retour doit contenir : possible ou non, reprendre des pions ou non.
	/*public PylosPartie_evaluationCoup evaluer_deplacerUnPion(TeamType equipeQuiFaitAction, int hauteur, int xCell, int yCell, int hauteur_initiale, int xCell_initiale, int yCell_initiale) {
		PylosPartie_evaluationCoup evaluationCoup = new PylosPartie_evaluationCoup();
		
		//if (tourDe != equipeQuiFaitAction) return evaluationCoup; // si ce n'est pas à cette équipe de jouer
		//if (joueurAJoueUnPion) return false; // déjà joué ce tour, impossible de rejouer
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
	}*/
	
	
	

	public boolean reprendUnPion(TeamType equipeQuiFaitAction, int hauteur, int xCell, int yCell, boolean verifierVariables) {
		// equipeQuiFaitAction == tourDe, obligatoirement
		
		if (verifierVariables) {
			if (plateauActuel.isValidPawnPosition(hauteur, xCell, yCell) == false) return false;
			if (tourDe != equipeQuiFaitAction) return false; // si ce n'est pas à cette équipe de jouer
			if (peutReprendrePionsNb <= 0) return false;
			boolean peutBougerPion = plateauActuel.canMovePawn(hauteur, xCell, yCell, -1, -1, -1);
			if (peutBougerPion == false) return false;
			TeamType equipeSurLaCase = plateauActuel.getCellTeam(hauteur, xCell, yCell);
			if (equipeSurLaCase != equipeQuiFaitAction) return false;
		}
		
		
		setCell(hauteur, xCell, yCell, TeamType.AUCUNE);
		if (equipeQuiFaitAction == TeamType.BLANC) nbJetonsBlanc++;
		if (equipeQuiFaitAction == TeamType.NOIR)  nbJetonsNoir++;
		
		PylosPartie_actionSimple actionActuelle = new PylosPartie_actionSimple(equipeQuiFaitAction, 2, hauteur, xCell, yCell);
		NetBuffer actionActuelleAsNetBuffer = actionActuelle.writeToNetBuffer();
		listeDeMessagesAEnvoyerAuxJoueurs.add(actionActuelleAsNetBuffer);
		
		peutReprendrePionsNb--;
		
		if (verifierVariables) {
			
			if ((nbJetonsBlanc == 0 || nbJetonsNoir == 0) && peutReprendrePionsNb <= 1 && joueurAJoueUnPion) {
				peutReprendrePionsNb = 0;
				tourSuivant();
			}
		}
		return true;
	}
	
	/** Passe le tour automatiquement si c'est possible : TODO
	 * @return
	 */
	public void tourSuivant_automatique() {
		// OK System.out.println("tourSuivant_automatique : ");
		if (peutReprendrePionsNb == 0 && joueurAJoueUnPion) {
			tourSuivant();
			return;
		}
		
		boolean leJoueurActuelPeutFaireQuelqueChose = IA_v40.ceJoueurPeutFaireQuelqueChosePendantSonTour(this, tourDe);
		if (leJoueurActuelPeutFaireQuelqueChose == false && peutReprendrePionsNb == 0) {
			tourSuivant();
			return;
		}
	}
	
	public TeamType getEquipeAdverse() {
		if (equipeJoueur == TeamType.NOIR) return TeamType.BLANC;
		if (equipeJoueur == TeamType.BLANC) return TeamType.NOIR;
		return TeamType.AUCUNE;
	}
	
	private long actionsGraphiques_intervalMsEntreActions = 1000;
	private long actionsGraphiques_tempsDerniereAction = 0;
	//public boolean actionsGraphiques_doitPasserleTourALaFin;
	private ArrayList<Graphique_pionAModifier> actionsGraphiques_listeDePionsAModifier = new ArrayList<Graphique_pionAModifier>();
	
	public void actionsGraphiques_ajouterCase(int hauteur, int xCell, int yCell, TeamType equipe) {
		Graphique_pionAModifier pion = new Graphique_pionAModifier();
		pion.hauteur = hauteur;
		pion.xCell = xCell;
		pion.yCell = yCell;
		pion.equipe = equipe;
		actionsGraphiques_listeDePionsAModifier.add(pion);
	}
	
	public void actionsGraphiques_setTimer() {
		actionsGraphiques_tempsDerniereAction = System.currentTimeMillis();
	}

	public boolean actionsGraphiques_loopEffectuerAction() {
		//System.out.println("PylosPartie.actionsGraphiques_loopEffectuerAction : tourDe = " + tourDe);
		if (actionsGraphiques_listeDePionsAModifier.size() == 0) return false;
		
		if (actionsGraphiques_tempsDerniereAction + actionsGraphiques_intervalMsEntreActions < System.currentTimeMillis()) {
			Graphique_pionAModifier pion = actionsGraphiques_listeDePionsAModifier.get(0);
			actionsGraphiques_listeDePionsAModifier.remove(0);
			this.setCell(pion.hauteur, pion.xCell, pion.yCell, pion.equipe);
			actionsGraphiques_tempsDerniereAction = System.currentTimeMillis();
			if (actionsGraphiques_listeDePionsAModifier.size() == 0) {
				//if (actionsGraphiques_doitPasserleTourALaFin)
				//System.err.println("PylosPartie.actionsGraphiques_loopEffectuerAction TOUR SUIVANT : tourDe = " + tourDe);
				tourSuivant(); // implicite
				return false;
			}
			return true;
		}
		return true;
	}
	
	public boolean actionsGraphiques_peutPasserLeTour() {
		if (actionsGraphiques_listeDePionsAModifier.size() == 0) return true;
		return false;
	}

	public IA_v40_coup ajouterCoupAEffectuerLentementParIA_coup = null;
	public long ajouterCoupAEffectuerLentementParIA_lastTimeDone = 0;
	public long ajouterCoupAEffectuerLentementParIA_maxTimerMs = 50;
	
	// Pour afficher lentement les actions de l'IA !
	public void actionsGraphiques_loopJeuIA() {
		if (ajouterCoupAEffectuerLentementParIA_coup == null) return;
		if (ajouterCoupAEffectuerLentementParIA_lastTimeDone + ajouterCoupAEffectuerLentementParIA_maxTimerMs > System.currentTimeMillis()) return; // pas encore !
		
		
		//etapesGraphiquesRestantes = 
		boolean passerLeTour = IA_v40.appliquerCoupSurPartieActuelle_static(this, equipeIA, ajouterCoupAEffectuerLentementParIA_coup, true);
		//compteurEtapeGraphique++;
		
		ajouterCoupAEffectuerLentementParIA_lastTimeDone = System.currentTimeMillis();
		
		ajouterCoupAEffectuerLentementParIA_coup = null;
	}
	

	
	
	
	
	
	/* OBSOLETE 
	public void loop_envoyerAuTCPInternet(TCPClient envoiTCP) { // RoomInternetHandler.instance.clientTCP
		if (modeDeJeu != ModeDeJeu.INTERNET) return;
		//System.out.println("PylosPartie.loop_envoyerAuTCPInternet");
		
		int nbMessagesEnvoyer = listeDeMessagesAEnvoyerAuxJoueurs.size();
		for (int iMessage = 0; iMessage < nbMessagesEnvoyer; iMessage++) {
			NetBuffer coupAsBuffer = listeDeMessagesAEnvoyerAuxJoueurs.get(iMessage);
			byte[] coupAsByteArray = coupAsBuffer.convertToByteArray(); // j'aurais aussi pu tout regrouper... !
			NetBuffer messageAEnvoyer = new NetBuffer();
			messageAEnvoyer.writeInt(100);
			messageAEnvoyer.writeByteArray(coupAsByteArray);
			envoiTCP.sendMessage(messageAEnvoyer);
			System.out.println("PylosPartie.loop_envoyerAuTCPInternet : envoyer une action simple !!");
			
			
			
			
		}
		listeDeMessagesAEnvoyerAuxJoueurs.clear();
	}
	
	/ ** 
	 *  @param receptionTCP
	 *  @param listeMessagesNonTraites messages à traiter par la suite par autre chose qu'un objet PylosPartie
	 *  @return
	 * /
	public ArrayList<PylosPartie_actionSimple> loop_recevoirDeTCPInternet(TCPClient receptionTCP, ArrayList<NetBuffer> listeMessagesNonTraites) {
		if (modeDeJeu != ModeDeJeu.INTERNET) return null;
		
		ArrayList<PylosPartie_actionSimple> arrayResult = new ArrayList<PylosPartie_actionSimple>();
		
		for (int miniI = 0; miniI < 100; miniI++) {
			NetBuffer nouveauMessage = receptionTCP.getNewMessage();
			if (nouveauMessage == null) break;
			int messageType = nouveauMessage.readInt();
			
			// Recevoir un nouveau coup joué
			if (messageType == 100) {
				byte[] actionAsByteArray = nouveauMessage.readByteArray();
				NetBuffer actionAsNetBuffer = new NetBuffer(actionAsByteArray);
				// Maintenant, faire jouer le coup :
				PylosPartie_actionSimple actionSimple = PylosPartie_actionSimple.readFromNetBuffer(actionAsNetBuffer);
				arrayResult.add(actionSimple);
				
				loop_recevoirDeTCPInternet_traiterAction(actionSimple);
				System.out.println("PylosPartie.loop_recevoirDeTCPInternet : recevoir une action simple !!");
			} else {
				nouveauMessage.rewind();
				listeMessagesNonTraites.add(nouveauMessage);
			}
		}
		
		return arrayResult;
	}
	
	private void loop_recevoirDeTCPInternet_traiterAction(PylosPartie_actionSimple actionSimple) {
		
		//System.out.println("GameHandler.loopReseauLocal, message reçu : messageID = " + messageID);
		
		if (actionSimple.typeAction == 0) { // poser un pion à partir de sa réserve
			TeamType equipeQuiJoueLeCoup = actionSimple.equipeQuiJoueLeCoup;
			poseUnPionDeSaReserve(equipeQuiJoueLeCoup, actionSimple.hauteur, actionSimple.xCell, actionSimple.yCell);
		}
		
		if (actionSimple.typeAction == 1) { // déplacer un pion
			TeamType equipeQuiJoueLeCoup = actionSimple.equipeQuiJoueLeCoup;
			deplacerUnPion(equipeQuiJoueLeCoup, actionSimple.hauteur, actionSimple.xCell, actionSimple.yCell, actionSimple.hauteur_init, actionSimple.xCell_init, actionSimple.yCell_init);
		}
		
		if (actionSimple.typeAction == 2) { // reprendre un pion
			TeamType equipeQuiJoueLeCoup = actionSimple.equipeQuiJoueLeCoup;
			reprendUnPion(equipeQuiJoueLeCoup, actionSimple.hauteur, actionSimple.xCell, actionSimple.yCell);
		}
		
		if (actionSimple.typeAction == 3) { // passer son tour
			tourSuivant();
			if (depuisServeurInternet_doitFaireJouerIA == true) {
				faireJouerIA();
				System.out.println("PylosPartie.loop_recevoirDeTCPInternet_traiterAction : faireJouerIA");
				// + envoyer les infos !!
				depuisServeurInternet_doitFaireJouerIA = false;
			}
			
		}
	}*/
	//public boolean depuisServeurInternet_doitFaireJouerIA = false;
	
	
	
	/** Récupérer qui joue, qui peut jouer, quel est le numéro du tour actuel. (resynchronisation totale pour les clients)
	 *  @return
	 */
	public NetBuffer ecrireVariablesPrincipales() {
		NetBuffer result = new NetBuffer();
		result.writeInt(nbJetonsBlanc);
		result.writeInt(nbJetonsNoir);
		result.writeInt(tourDe.asInt);
		result.writeInt(varianteDuJeu.asInt);
		result.writeInt(numeroDeTour);
		result.writeBool(joueurAJoueUnPion);
		result.writeInt(peutReprendrePionsNb);
		return result;
	}
	
	public void lireVariablesPrincipales(NetBuffer fromBuffer) {
		nbJetonsBlanc = fromBuffer.readInt();
		nbJetonsNoir = fromBuffer.readInt();
		tourDe = TeamType.fromInt(fromBuffer.readInt());
		varianteDuJeu = PylosPartieVariante.fromInt(fromBuffer.readInt());
		numeroDeTour = fromBuffer.readInt();
		joueurAJoueUnPion = fromBuffer.readBool();
		peutReprendrePionsNb = fromBuffer.readInt();
	}
	
	

	public TeamType equipeGagnante() {
		int hauteurMax = plateauActuel.a1Grid.length;
		if (hauteurMax == 0) return TeamType.AUCUNE;
		PylosGrid lastGrid = plateauActuel.a1Grid[hauteurMax - 1];
		return lastGrid.getTeamAtCellPosition(0, 0);
	}
	
	public boolean pyramideRemplie() {
		if (equipeGagnante() != TeamType.AUCUNE) return true;
		return false;
	}
	
	
	
	
	
	
	
	
	
	
}

// Pour l'affichage lent des actions à l'écran
class Graphique_pionAModifier {
	public int hauteur, xCell, yCell;
	public TeamType equipe;
}



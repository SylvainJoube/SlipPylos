package commun.partie.nonGraphique;




public class PylosPartie {
	
	public int nbJetonsBlanc = 0;//15;
	public int nbJetonsNoir = 0;//15;
	public int nbCasesCote = 4; // nombre de cases par c�t�
	private int nbJetonsTotal;
	public int hauteurMax = nbCasesCote - 1;
	//public PylosGrid[] a1Grid;
	public PylosGridArray plateauActuel;
	public TeamType tourDe = TeamType.BLANC;
	//public IA_v1 ia = new IA_v1(TeamType.NOIR, this);
	public TeamType equipeJoueur = TeamType.BLANC;
	public boolean joueurAUtiliseSaReserve = false; // 1 utilisation de la r�serve de pions par tour, max
	
	public int computeNbJetonsTotal() {
		int nbJetons = 0;
		for (int lenCote = nbCasesCote; lenCote >= 1; lenCote--) {
			nbJetons += lenCote * lenCote;
		}
		return nbJetons;
	}
	
	public PylosPartie() { // constructeur
		plateauActuel = new PylosGridArray(nbCasesCote);
		nbJetonsTotal = computeNbJetonsTotal();
		nbJetonsBlanc = nbJetonsTotal / 2;
		nbJetonsNoir = nbJetonsTotal / 2;
		
		
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
	
	public void tourSuivant() {
		if (nbJetonsBlanc <= 0) return;
		if (nbJetonsNoir <= 0) return;
		
		joueurAUtiliseSaReserve = false;
		if (tourDe == TeamType.BLANC)
			tourDe = TeamType.NOIR;
		else
			tourDe = TeamType.BLANC;
		
		/*if (tourDe == ia.equipe) {
			ia.joueUnCoup();
			System.out.println("PylosPartie : Tour suivant");
			tourSuivant();
		}*/
		
		if (tourDe == TeamType.NOIR) {
			
			IA_v4.joueUnCoup(TeamType.NOIR, this, 4);
			System.out.println("PylosPartie : Tour suivant (l'IA vient de jouer)");
			try {
				Thread.sleep(40);
			} catch (InterruptedException e) {
				
			}
			tourSuivant();
		}
		/*if (tourDe == TeamType.BLANC) {
			IA_v4.joueUnCoup(TeamType.BLANC, this, 4);
			System.out.println("PylosPartie : Tour suivant");
			try {
				Thread.sleep(40);
			} catch (InterruptedException e) {
				
			}
			tourSuivant();
		}*/
		
	}
	
	
}

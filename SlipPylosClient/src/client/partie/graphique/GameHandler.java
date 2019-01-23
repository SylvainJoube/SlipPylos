package client.partie.graphique;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;

import client.listeners.CustomPoint;
//import client.listeners.CustomPoint;
import client.listeners.ListenerMouseEvent;
import client.listeners.Listeners;
//import client.listeners.MouseIsAbovePionSelection;
//import client.listeners.GenericMouseListener;
//import client.listeners.GenericMouseMotionListener;
import client.outils.graphiques.BoxPosition;
import client.outils.graphiques.GraphicsHandler;
import client.outils.graphiques.PImage;
import client.roomReseauLocalAttente.RoomReseauLocalAttenteHandler;
import commun.partie.nonGraphique.*;
import slip.network.buffers.NetBuffer;
import slip.network.tcp.TCPClient;





public class GameHandler {
	
	///** True if the game is currently "running", i.e. the game loop is looping */
	//private boolean gameRunning = true;
	protected boolean waitingForKeyPress = false;
	protected String message = "Bienvenue !";
	
	//public boolean joueurVeutDeplacerUnPion = false; // quand le joueur essaie de déplacer un pion pour le monter
	public int cellWidth = CellDetection.cellWidth;//100; // largeur d'une case de jeu
	public int cellHeight = CellDetection.cellHeight;//100; // hauteur d'une case de jeu
	//int cellSpaceBetweenCells = 4;
	public int xGrid = 60;
	public int yGrid = 110;
	public int gridTotalHeight = PylosPartie.nbCasesCote * CellDetection.cellHeight;
	
	public static PylosPartie partieActuelle = null;
	public static GameHandler jeuActuel = null;
	public PylosCellResult poserUnPionIci; // position à laquelle je afficher le pion à poser, indique aussi si je peux poser le pion ou non
	public PylosCell pickUpCell = null;    // pion que je peux reprendre, calculé dans refreshWithMousePosition();
	public PylosCell dragCell = null;      // bouger un des pions du joueur
	public PylosCell pionDuPlateauSurvole = null; // mettre en surbrillance une case (souris passe dessus)
	
	public int xDessinJetons = xGrid;
	public int yDessinJetons = yGrid + gridTotalHeight + 10;
	// Calculé dans drawAllJetonsNb() :
	public int xDessinJetonsJoueur = 0;
	public int yDessinJetonsJoueur = 0;
	public VolonteJoueur volonteJoueur = VolonteJoueur.MAIN_LIBRE;
	public static boolean highlightPionSelecton = false;
	
	//public BoxPosition tourSuivantPos = new BoxPosition(400, 10, +138, +42);
	public static boolean tourSuivantHighlight = false;
	private int xTourSuivant, yTourSuivant;
	
	public int lastMouseX = 0;
	public int lastMouseY = 0;
	
	/** Constructeur, crée l'objet partie
	 */
	public GameHandler(ModeDeJeu arg_modeDeJeu, TeamType cEstLeTourDe, TeamType equipeJoueurActuel) {
		GameHandler.jeuActuel = this;
		GameHandler.partieActuelle = new PylosPartie(arg_modeDeJeu, cEstLeTourDe, equipeJoueurActuel); // créer la partie avant les Listener
	}
	
	/*public void setAsCurrentRoom() {
		if (jeuActuel != null) {
			GraphicsHandler ginstance = GraphicsHandler.getMainInstance();
			ginstance.addMouseListener(new GenericMouseListener());
			ginstance.addMouseMotionListener(new GenericMouseMotionListener());
		}
	}*/
	
	public CustomPoint cellPosToCoordinates(int xPosInCanvas, int yPosInCanvas, int layerLevel) { // layerLevel : niveau (0 - 3) sur lequel on est
		CustomPoint pt = new CustomPoint(-1, -1);
		int xOffset = layerLevel * cellWidth / 2;
		int yOffset = layerLevel * cellHeight / 2;
		double xPosInGridLayer = xPosInCanvas - xOffset;
		double yPosInGridLayer = yPosInCanvas - yOffset;
		pt.x = (int) Math.floor(xPosInGridLayer / (double) xPosInCanvas);
		pt.y = (int) Math.floor(yPosInGridLayer / (double) yPosInCanvas);
		return pt;
	}
	
	
	/** Actualiser ce que le joueur peut faire : cellule sur laquelle le joueur est, pion qu'il peut reprendre...
	 */
	public void refreshWithMousePosition() {
		PylosPoint mousePos = new PylosPoint(GameHandler.jeuActuel.lastMouseX,  GameHandler.jeuActuel.lastMouseY);
		PylosPoint gridPos = new PylosPoint(GameHandler.jeuActuel.xGrid,  GameHandler.jeuActuel.yGrid);
		
		GameHandler.jeuActuel.pionDuPlateauSurvole = null;
		GameHandler.highlightPionSelecton = false;
		GameHandler.tourSuivantHighlight = false;
		GameHandler.jeuActuel.poserUnPionIci = null;
		GameHandler.jeuActuel.pickUpCell = null;
		
		// Si ce n'est pas au tour du joueur, il ne peut rien faire
		if (GameHandler.partieActuelle.tourDe != GameHandler.partieActuelle.equipeJoueur) {
			return;
		}
		
		// Si le joueur doit reprendre un pion, je l'oblige à reprendre un ou plusieurs pions (il peut aussi passer son tour)
		if (partieActuelle.peutReprendrePionsNb > 0)
			GameHandler.jeuActuel.volonteJoueur = VolonteJoueur.REPRENDRE_UN_PION;
		
		// Si c'est l'équipe du joueur qui joue, 
		
		// Si 
		if (GameHandler.jeuActuel.volonteJoueur == VolonteJoueur.MAIN_LIBRE
			|| GameHandler.jeuActuel.volonteJoueur == VolonteJoueur.REPRENDRE_UN_PION)
		if (GameHandler.partieActuelle.tourDe == GameHandler.partieActuelle.equipeJoueur) {
			PylosCellResult res = CellDetection.getCellUnderMouse(mousePos, gridPos, GameHandler.partieActuelle, GameHandler.partieActuelle.equipeJoueur, -1);
			GameHandler.jeuActuel.pionDuPlateauSurvole = res;
			
			// Si le joueur doit reprendre un pion, je regarde s'il peut reprendre le pion sélectionné : GameHandler.jeuActuel.highlightCell
			if (res != null)
			if (GameHandler.jeuActuel.volonteJoueur == VolonteJoueur.REPRENDRE_UN_PION
				&& GameHandler.partieActuelle.plateauActuel.canMovePawn(res, null)) {
				GameHandler.jeuActuel.pickUpCell = res;
			}
		}
		
		if (MouseIsAbovePionSelection.check(GameHandler.jeuActuel.lastMouseX,  GameHandler.jeuActuel.lastMouseY) && !GameHandler.partieActuelle.joueurAJoueUnPion) {
			GameHandler.highlightPionSelecton = true;
		}
		
		if (GameHandler.jeuActuel.volonteJoueur == VolonteJoueur.PION_EN_MAIN_DEPUIS_RESERVE) {
			// Poser un pion venant de sa réserve
			PylosCellResult res = CellDetection.getCellUnderMouse(mousePos, gridPos, GameHandler.partieActuelle, TeamType.AUCUNE, -1);
			GameHandler.jeuActuel.poserUnPionIci = res;
		}
		
		if (GameHandler.jeuActuel.volonteJoueur == VolonteJoueur.DEPLACER_UN_PION) {
			//LogWriter.Log("GameHandler.refreshWithMousePosition : deplacerUnPion == true");
			// Condition sur la hauteur : si je veux déplacer un pion, il faut que la destination soit plus haute
			int hateurMinimale = GameHandler.jeuActuel.dragCell.hauteur + 1;
			PylosCellResult res = CellDetection.getCellUnderMouse(mousePos, gridPos, GameHandler.partieActuelle, TeamType.AUCUNE, hateurMinimale);
			if (res != null) {
				boolean canMovePawn = partieActuelle.plateauActuel.canMovePawn(res, GameHandler.jeuActuel.dragCell);
				res.peutPoserIci = ( res.peutPoserIci && canMovePawn );
			}
			GameHandler.jeuActuel.poserUnPionIci = res;
		}
		
	}
	
	// Dessin du jeton avec écrit le nombre de jetons restants dessus
	public void drawJetonNb(Graphics2D g, Image jetonImage, int nb, int x, int y, Color fontColor, boolean highlight) {
		if (jetonImage == null)
			return;
		String nbStr;
		nbStr = Integer.toString(nb);
		FontMetrics fMetrics = g.getFontMetrics();
		int strWidth  = fMetrics.stringWidth(nbStr);
		int strHeight = fMetrics.getHeight();
		//int imgWidth = jetonImage.getWidth(null);
		Point imageCenter = new Point();
		imageCenter.x = x + Math.round(jetonImage.getWidth(null) / 2);
		imageCenter.y = y + Math.round(jetonImage.getHeight(null) / 2);
		int strWidthHalf = Math.round(strWidth / 2);
		int strHeightHalf = Math.round(strHeight / 2);
		//g.drawImage(jetonImage, x, y, null);
		
		float[] scales = { 1f, 1f, 1f, 1f };
		float[] offsets = new float[4];
		RescaleOp rop = new RescaleOp(scales, offsets, null);
		g.drawImage((BufferedImage) jetonImage, rop, x, y);
		//g2d.drawImage(buffimg, rop, 0, 0);
		if (highlight) {
			double jetonHighlightAlpha = ((Math.cos(jetonNbTime) + 1) / 2) * 0.9 + 0.1;
			PImage.drawImageAlpha(GraphicsHandler.getMainGraphics(), jetonHighlightImg, x, y, jetonHighlightAlpha);
		}
		
		Color oldColor = g.getColor();
		g.setColor(fontColor);
		g.drawString(nbStr, imageCenter.x - strWidthHalf, imageCenter.y - strHeightHalf);
		g.setColor(oldColor);
		
	} // g.getFontMetrics().stringWidth(message)
	
	

	private Image jetonNoirImg;
	private Image jetonBlancImg;
	//private Image jetonNoirImg_highlight = RessourceManager.LoadImage("images/JetonNoirH.png");
	//private Image jetonBlancImg_highlight = RessourceManager.LoadImage("images/JetonBlancH.png");
	private Image jetonErreurImg;
	private Image tourSuivantImg;
	private Image jetonHighlightImg;
	private Image jetonReprendreImg;
	private void loadImages() {
		jetonNoirImg = RessourceManager.LoadImage("images/JetonNoir.png");
		jetonBlancImg = RessourceManager.LoadImage("images/JetonBlanc.png");
		//jetonNoirImg_highlight = RessourceManager.LoadImage("images/JetonNoirH.png");
		//jetonBlancImg_highlight = RessourceManager.LoadImage("images/JetonBlancH.png");
		jetonErreurImg = RessourceManager.LoadImage("images/JetonErreur.png");
		tourSuivantImg = RessourceManager.LoadImage("images/Bouton_tourSuivant.png");
		jetonHighlightImg = RessourceManager.LoadImage("images/JetonHighlight.png");
		jetonReprendreImg = RessourceManager.LoadImage("images/ReprendrePion.png");
	}
	
	private Graphics2D currentGraphics = null;
	
	private double jetonNbTime = 0;
	private void drawAllJetonsNb() {

		jetonNbTime += 0.25;
		
		int xBlanc = xDessinJetons;
		int yBlanc = yDessinJetons;
		int xNoir = xBlanc + CellDetection.cellWidth + 10;
		int yNoir = yBlanc;// + CellDetection.cellHeight;
		xTourSuivant = xNoir + CellDetection.cellWidth + 14;
		yTourSuivant = yNoir + 26; // drawAllJetonsNb() à faire avant drawTourSuivant()
		
		if (partieActuelle.equipeJoueur == TeamType.NOIR) {
			xDessinJetonsJoueur = xNoir;
			yDessinJetonsJoueur = yNoir;
		}
		if (partieActuelle.equipeJoueur == TeamType.BLANC) {
			xDessinJetonsJoueur = xBlanc;
			yDessinJetonsJoueur = yBlanc;
		}
		
		drawJetonNb(currentGraphics, jetonNoirImg, partieActuelle.nbJetonsNoir, xNoir, yNoir, Color.WHITE, partieActuelle.tourDe == TeamType.NOIR);
		drawJetonNb(currentGraphics, jetonBlancImg, partieActuelle.nbJetonsBlanc, xBlanc, yBlanc, Color.BLACK, partieActuelle.tourDe == TeamType.BLANC);
		if (highlightPionSelecton) {
			if (partieActuelle.equipeJoueur == TeamType.BLANC) {
				PImage.drawImageAlpha(currentGraphics, jetonHighlightImg, xBlanc, yBlanc, 1);
			}
			if (partieActuelle.equipeJoueur == TeamType.NOIR) {
				PImage.drawImageAlpha(currentGraphics, jetonHighlightImg, xNoir, yNoir, 1);
			}
		}
	}
	
	private void drawGridLines() {
		// Dessin de la grille
		Color gridColor = Color.WHITE;
		currentGraphics.setColor(gridColor);
		int xGridMax = xGrid + partieActuelle.nbCasesCote * cellWidth;
		int yGridMax = yGrid + partieActuelle.nbCasesCote * cellHeight;
		for (int xCell = 0; xCell <= partieActuelle.nbCasesCote; xCell++) {
			int currentXGrid = xGrid + xCell * cellWidth;
			currentGraphics.drawLine(currentXGrid , yGrid, currentXGrid, yGridMax);
		}
		for (int yCell = 0; yCell <= partieActuelle.nbCasesCote; yCell++) {
			int currentYGrid = yGrid + yCell * cellWidth;
			currentGraphics.drawLine(xGrid , currentYGrid, xGridMax, currentYGrid);
		}
	}
	
	private void drawPawnsOnGrid() {
		// Pout toutes les grilles du plateau (dans l'ordre de hauteur)
		for (int iGrid = 0; iGrid <= partieActuelle.hauteurMax; iGrid++) {
			PylosGrid currentGrid = partieActuelle.plateauActuel.a1Grid[iGrid];
			int gridXOffsetPx = CellDetection.getGridXOffsetPx(iGrid);
			int gridYOffsetPx = CellDetection.getGridYOffsetPx(iGrid);
			//System.out.println("gameLoop() :  iGrid = " + iGrid + "    gridXOffsetPx = " + " gridYOffsetPx = " + gridYOffsetPx);
			// Pour tous les pions de la grille
			for (int xCell = 0; xCell < currentGrid.gridWidth;  xCell++)
			for (int yCell = 0; yCell < currentGrid.gridHeight; yCell++) {
				int xCellPx = xCell * CellDetection.cellWidth  + gridXOffsetPx + xGrid;
				int yCellPx = yCell * CellDetection.cellHeight + gridYOffsetPx + yGrid;
				PylosCell currentCell = currentGrid.a2Cell[xCell][yCell];
				
				float drawWithAlpha = 1;
				if (currentCell.estIdentique(dragCell)) {
					drawWithAlpha = 0.4f;
				}
				switch (currentCell.occupeePar) {
					case BLANC : PImage.drawImageAlpha(currentGraphics, jetonBlancImg, xCellPx, yCellPx, drawWithAlpha); break; //g.drawImage(jetonBlancImg, xCellPx, yCellPx, null); break;
					case NOIR : PImage.drawImageAlpha(currentGraphics, jetonNoirImg, xCellPx, yCellPx, drawWithAlpha); break;
					default : break;
				}
				
				if (currentCell.estIdentique(pionDuPlateauSurvole)) {
					PImage.drawImageAlpha(currentGraphics, jetonHighlightImg, xCellPx, yCellPx, 1);
				}
				
				if (currentCell.estIdentique(pickUpCell)) {
					PImage.drawImageAlpha(currentGraphics, jetonReprendreImg, xCellPx, yCellPx, 1);
				}
			}
		}
	}
	
	public void drawPoserPion() {
		// 1) Si une case est trouvée pour le pion, je le dessine dans la case
		//    la position n'est pas fircément valide, cf poserUnPionIci.peutPoserIci.
		if (poserUnPionIci != null) {
			PylosPoint pos = CellDetection.getPosInGridFromCellRes(poserUnPionIci);
			int xDraw = pos.x + xGrid;
			int yDraw = pos.y + yGrid;
			
			//System.out.println("gameLoop() : currentCellUnderMouse.hauteur =  " + currentCellUnderMouse.hauteur);
			Image imageDrawn = null;
			switch (partieActuelle.equipeJoueur) {
			case NOIR : imageDrawn = jetonNoirImg; break;
			case BLANC : imageDrawn = jetonBlancImg; break;
			default : break;
			}
			if (imageDrawn != null) {
				PImage.drawImageAlpha(currentGraphics, imageDrawn, xDraw, yDraw, 0.5);
			}
			
			/*if (partieActuelle.equipeJoueur == TeamType.NOIR) g.drawImage(jetonNoirImg, xDraw, yDraw, null);
			if (partieActuelle.equipeJoueur == TeamType.BLANC) g.drawImage(jetonBlancImg, xDraw, yDraw, null);*/

			//System.out.println("GameHandler.drawPoserPion : test...");
			//System.out.println("GameHandler.drawPoserPion : poserUnPionIci == null ? -> " + (poserUnPionIci == null));
			//System.out.println("GameHandler.drawPoserPion : poserUnPionIci.peutPoserIci =  " + (poserUnPionIci.peutPoserIci)); il y avait une NullPointerException ici avant l'implémentation du GameEventHandler
			
			
			if (! poserUnPionIci.peutPoserIci)
				PImage.drawImageAlpha(currentGraphics, jetonErreurImg, xDraw, yDraw, 0.5);//g.drawImage(jetonErreurImg, xDraw, yDraw, null);
			
		} else {
			// 2) Si aucune case n'est trouvée pour le pion, je le dessine sous ma sourie, centré
			if (volonteJoueur == VolonteJoueur.PION_EN_MAIN_DEPUIS_RESERVE || volonteJoueur == VolonteJoueur.DEPLACER_UN_PION) {
				Image imageDrawn = null;
				switch (partieActuelle.equipeJoueur) {
				case NOIR : imageDrawn = jetonNoirImg; break;
				case BLANC : imageDrawn = jetonBlancImg; break;
				default : break;
				}
				if (imageDrawn != null) {
					PImage.drawImageAlpha_centered(currentGraphics, imageDrawn, lastMouseX, lastMouseY, 0.3);
					PImage.drawImageAlpha_centered(currentGraphics, jetonErreurImg, lastMouseX, lastMouseY, 0.3);
				}
			}
		}
	}
	
	// drawAllJetonsNb() à faire avant drawTourSuivant()
	public void drawTourSuivant() {
		/*
		int imgWidth = PImage.getImageWidth(tourSuivantImg);
		int imgHeight = PImage.getImageHeight(tourSuivantImg);
		BoxPosition pos = new BoxPosition(xTourSuivant, yTourSuivant, xTourSuivant + imgWidth, yTourSuivant + imgHeight);
		int xMouse = Listeners.frame_getMouseX();
		int yMouse = Listeners.frame_getMouseY();
		int posOffset = 0;
		boolean goTourSuivant = false;
		if (pos.isInside(xMouse,  yMouse)) {
			posOffset = 2;
			double colorFactor = 0.1;
			PImage.drawImageColorAlpha(currentGraphics, tourSuivantImg, xTourSuivant, yTourSuivant, colorFactor, colorFactor, colorFactor, 1);
			if (Listeners.frame_mouseReleased()) { // clic sur le bouton "passer le tour"
				goTourSuivant = true;
			}
		}
		PImage.drawImageAlpha(currentGraphics, tourSuivantImg, xTourSuivant - posOffset, yTourSuivant - posOffset, 1);
		*/
		
		boolean clickable = partieActuelle.tourDe == partieActuelle.equipeJoueur;
		boolean goTourSuivant = PImage.checkImageAsButton(clickable, currentGraphics, tourSuivantImg, xTourSuivant, yTourSuivant);
		
		
		
		if (goTourSuivant) {
			PylosPartie partie = partieActuelle;
			boolean peutChangerDeTour = true;
			if (!partie.joueurAJoueUnPion) {
				LogWriter.Log("MyMouseListener.mousePressed : Impossible de passer votre tour lorsque vous n'avez pas encore joué !");
				peutChangerDeTour = false;
			}
			if (partieActuelle.peutReprendrePionsNb > 1) {
				LogWriter.Log("MyMouseListener.mousePressed : Impossible de passer votre tour lorsque vous n'avez repris au moins un des pions qui vons sont dûs !");
				peutChangerDeTour = false;
			}
			if (peutChangerDeTour) {
				partie.tourSuivant();
				if (partieActuelle.modeDeJeu == ModeDeJeu.RESEAU_LOCAL) reseauLocal_passeSonTour();
			}
		}
	}
	
	
	// Sera appelé par GraphicsHandler
	public static void staticGameLoop() {
		if (GameHandler.jeuActuel == null) return;
		GameHandler.jeuActuel.gameLoop();
	}
	
	// Sera appelé par GraphicsHandler
	public void gameLoop() {
		currentGraphics = GraphicsHandler.getMainGraphics();

		GameEventHandler.staticLoop();
		// Chargement des images nécessaires à l'affichage
		loadImages();
		// Dessin du nombre restant de jetons
		drawAllJetonsNb();
		// Dessin des lignes composant la grille
		drawGridLines();
		// Affichage des pions 1 à 1 pour l'IA
		partieActuelle.actionsGraphiques_loopEffectuerAction();
		// Dessin des pions dans la grille
		drawPawnsOnGrid();
		// Dessin de la future position du pion à poser
		drawPoserPion();
		// Dessin du bouton "tour suivant"
		drawTourSuivant();
		// Boucle si le jeu est en réseau local
		loopReseauLocal();
		
		
		
		/*
		// if we're waiting for an "any key" press then draw the 
		// current message 
		if (waitingForKeyPress) {
			currentGraphics.setColor(Color.white);
			currentGraphics.drawString(message,(800-currentGraphics.getFontMetrics().stringWidth(message))/2,250);
			currentGraphics.drawString("Press any key",(800-currentGraphics.getFontMetrics().stringWidth("Press any key"))/2,300);
		}
		*/
	}
	
	public void mousePressed(ListenerMouseEvent mouseEvent) {
		//LogWriter.Log("MyMouseMotionListener.mousePressed :  mouseEvent.mouseX = " + mouseEvent.mouseX + " mouseEvent.mouseY = " + mouseEvent.mouseY);
		
		GameHandler.jeuActuel.lastMouseX = mouseEvent.mouseX;
		GameHandler.jeuActuel.lastMouseY = mouseEvent.mouseY;
		
		GameHandler.jeuActuel.refreshWithMousePosition();
		
		//boolean volonteJoueurPionEnMain = false;
		// Si c'est le tour du joueur, et qu'il n'a pas encore joué de pion, je regarde s'il peut en prendre un
		if (GameHandler.partieActuelle.equipeJoueur == GameHandler.partieActuelle.tourDe
		 && ! GameHandler.partieActuelle.joueurAJoueUnPion ) { // si le joueur n'a pas encore joué de pion
			
			if (MouseIsAbovePionSelection.check(mouseEvent.mouseX, mouseEvent.mouseY)) {
				GameHandler.jeuActuel.volonteJoueur = VolonteJoueur.PION_EN_MAIN_DEPUIS_RESERVE;
				//volonteJoueurPionEnMain = true;
				//LogWriter.Log("MyMouseListener.mousePressed :  prendre un pion !");
			}
			// Tenter de déplacer un pion de son jeu pour le remonter
			if (GameHandler.jeuActuel.volonteJoueur == VolonteJoueur.MAIN_LIBRE
			 && GameHandler.jeuActuel.dragCell == null
			 && GameHandler.jeuActuel.pionDuPlateauSurvole != null) {
				//LogWriter.Log("MyMouseListener.mousePressed :  déplacer un pion !");
				PylosGridArray plateauActuel = GameHandler.partieActuelle.plateauActuel;
				PylosCell tryMoveCell = GameHandler.jeuActuel.pionDuPlateauSurvole;
				boolean canMovePawn = plateauActuel.canMovePawn(tryMoveCell, GameHandler.jeuActuel.dragCell);
				if (canMovePawn) {
					GameHandler.jeuActuel.dragCell = GameHandler.jeuActuel.pionDuPlateauSurvole;
					GameHandler.jeuActuel.volonteJoueur = VolonteJoueur.DEPLACER_UN_PION;
					//LogWriter.Log("MyMouseListener.mousePressed :  déplacer un pion OK OK OK !");
				}
			}
		}
	}
	
	public void mouseReleased(ListenerMouseEvent mouseEvent) {
		//LogWriter.Log("MyMouseMotionListener.mouseReleased :  mouseEvent.mouseX = " + mouseEvent.mouseX + " mouseEvent.mouseY = " + mouseEvent.mouseY);
		GameHandler.jeuActuel.lastMouseX = mouseEvent.mouseX;
		GameHandler.jeuActuel.lastMouseY = mouseEvent.mouseY;

		GameHandler.jeuActuel.refreshWithMousePosition();
		// met à jour GameHandler.jeuActuel.poserUnPionIci
		
		if (GameHandler.jeuActuel.volonteJoueur == VolonteJoueur.PION_EN_MAIN_DEPUIS_RESERVE
			|| GameHandler.jeuActuel.volonteJoueur == VolonteJoueur.DEPLACER_UN_PION) {
			//PylosPoint mousePos = new PylosPoint(mouseEvent.mouseX, mouseEvent.mouseY);
			//PylosPoint gridPos = new PylosPoint(GameHandler.jeuActuel.xGrid,  GameHandler.jeuActuel.yGrid);
			// Condition sur la hauteur : si je veux déplacer un pion, il faut que la destination soit plus haute
			//int hateurMinimale = -1;
			//if (GameHandler.jeuActuel.volonteJoueur == VolonteJoueur.DEPLACER_UN_PION)
			//	hateurMinimale = GameHandler.jeuActuel.dragCell.hauteur + 1;
			
			
			PylosCellResult essayerDePoserPionIci = GameHandler.jeuActuel.poserUnPionIci;
			
			TeamType equipeJoueur = GameHandler.partieActuelle.equipeJoueur;
			
			// --- Poser un pion ---
			if (essayerDePoserPionIci != null)
			if (essayerDePoserPionIci.peutPoserIci) { // poser un pion ici

				PylosCell dragCell = GameHandler.jeuActuel.dragCell; // peut être null
				
				switch (GameHandler.jeuActuel.volonteJoueur) {
				case DEPLACER_UN_PION :
					if (dragCell == null) {
						System.err.println("ERREUR GRAVE GameHaldler.mouseReleased : volonteJoueur == DEPLACER_UN_PION et dragCell == null.");
						return;
					}
					GameHandler.partieActuelle.deplacerUnPion(equipeJoueur, essayerDePoserPionIci.hauteur, essayerDePoserPionIci.xCell, essayerDePoserPionIci.yCell, dragCell.hauteur, dragCell.xCell, dragCell.yCell);
					if (partieActuelle.modeDeJeu == ModeDeJeu.RESEAU_LOCAL) reseauLocal_deplacePion(essayerDePoserPionIci.hauteur, essayerDePoserPionIci.xCell, essayerDePoserPionIci.yCell, dragCell.hauteur, dragCell.xCell, dragCell.yCell);
					break;
				case PION_EN_MAIN_DEPUIS_RESERVE :
					GameHandler.partieActuelle.poseUnPionDeSaReserve(equipeJoueur, essayerDePoserPionIci.hauteur, essayerDePoserPionIci.xCell, essayerDePoserPionIci.yCell);
					if (partieActuelle.modeDeJeu == ModeDeJeu.RESEAU_LOCAL) reseauLocal_posePionDeSaReserve(essayerDePoserPionIci.hauteur, essayerDePoserPionIci.xCell, essayerDePoserPionIci.yCell);
					break;
				default : break;
				}
				
				
				/* mis dans PylosPartie
				GameHandler.partieActuelle.setCell(essayerDePoserPionIci.hauteur, essayerDePoserPionIci.xCell, essayerDePoserPionIci.yCell, equipeJoueur);
				GameHandler.partieActuelle.joueurAJoueUnPion = true;
				
				if (GameHandler.jeuActuel.volonteJoueur != VolonteJoueur.DEPLACER_UN_PION) {
					if (equipeJoueur == TeamType.BLANC) GameHandler.partieActuelle.nbJetonsBlanc--;
					if (equipeJoueur == TeamType.NOIR)  GameHandler.partieActuelle.nbJetonsNoir--;
				} else {
					// Je supprime l'ancien pion
					PylosCell dragCell = GameHandler.jeuActuel.dragCell;
					GameHandler.partieActuelle.setCell(dragCell.hauteur, dragCell.xCell, dragCell.yCell, TeamType.AUCUNE);
				}
				
				if (GameHandler.partieActuelle.plateauActuel.willFormSameColorRectangle(essayerDePoserPionIci.hauteur, essayerDePoserPionIci.xCell, essayerDePoserPionIci.yCell, equipeJoueur)) {
					GameHandler.partieActuelle.peutReprendrePionsNb = 2;
				}
				//GameHandler.partieActuelle.tourSuivant();
				if (GameHandler.partieActuelle.nbJetonsBlanc == 0 || GameHandler.partieActuelle.nbJetonsNoir == 0) {
					GameHandler.partieActuelle.tourSuivant();
				}*/
			}
		}
		
		if (GameHandler.jeuActuel.volonteJoueur == VolonteJoueur.REPRENDRE_UN_PION) {
			if (GameHandler.jeuActuel.pickUpCell != null) { // actualisé juste au-dessus via : GameHandler.jeuActuel.refreshWithMousePosition();
				// Reprendre ce pion
				PylosCell pickUpCell = jeuActuel.pickUpCell;
				
				partieActuelle.reprendUnPion(partieActuelle.equipeJoueur, pickUpCell.hauteur, pickUpCell.xCell, pickUpCell.yCell);
				if (partieActuelle.modeDeJeu == ModeDeJeu.RESEAU_LOCAL) reseauLocal_reprendUnPion(pickUpCell.hauteur, pickUpCell.xCell, pickUpCell.yCell);
				
				/*GameHandler.partieActuelle.setCell(pickUpCell.hauteur, pickUpCell.xCell, pickUpCell.yCell, TeamType.AUCUNE);
				TeamType equipeJoueur = GameHandler.partieActuelle.equipeJoueur;
				if (equipeJoueur == TeamType.BLANC) GameHandler.partieActuelle.nbJetonsBlanc++;
				if (equipeJoueur == TeamType.NOIR)  GameHandler.partieActuelle.nbJetonsNoir++;
				GameHandler.partieActuelle.peutReprendrePionsNb--;*/
				
				GameHandler.jeuActuel.refreshWithMousePosition(); // actualisation fonctionnelle et graphique
			}
		}
		
		
		//if (!volonteJoueurPionEnMain) {
		//	GameHandler.jeuActuel.volonteJoueur = VolonteJoueur.MAIN_LIBRE;
		//}
		GameHandler.jeuActuel.volonteJoueur = VolonteJoueur.MAIN_LIBRE;
		
		
		GameHandler.jeuActuel.dragCell = null;
		GameHandler.jeuActuel.refreshWithMousePosition();
		partieActuelle.tourSuivant_automatique();
	}
	

	public void mouseMoved(ListenerMouseEvent mouseEvent) {
		//LogWriter.Log("MyMouseMotionListener.mouseMoved :  mouseEvent.mouseX = " + mouseEvent.mouseX + " mouseEvent.mouseY = " + mouseEvent.mouseY);
		GameHandler.jeuActuel.lastMouseX = mouseEvent.mouseX;
		GameHandler.jeuActuel.lastMouseY = mouseEvent.mouseY;
		GameHandler.jeuActuel.refreshWithMousePosition();
		
	}
	
	/** Boucle effectuée si le jeu actuel est de type réseau local.
	 */
	public void loopReseauLocal() {
		if (partieActuelle.modeDeJeu != ModeDeJeu.RESEAU_LOCAL) return; // seulement exécuté si partie en réseau local
		if (reseauLocal_verifieConnexion() == false) return;
		TCPClient tcp = RoomReseauLocalAttenteHandler.autreJoueurLocalTCPClient;
		// L'autre client est supposé complètement honnête, c'est du réseau local, il n'y a pas grand chose à gagner si on triche.
		NetBuffer receivedMessage = tcp.getNewMessage();
		if (receivedMessage == null) return; // rien à faire
		int messageID = receivedMessage.readInt();
		
		//System.out.println("GameHandler.loopReseauLocal, message reçu : messageID = " + messageID);
		
		if (messageID == 1) { // poser un pion à partir de sa réserve
			int hauteur = receivedMessage.readInt();
			int xCell = receivedMessage.readInt();
			int yCell = receivedMessage.readInt();
			TeamType equipeAutreJoueur = partieActuelle.getEquipeAdverse();
			partieActuelle.poseUnPionDeSaReserve(equipeAutreJoueur, hauteur, xCell, yCell);
		}

		if (messageID == 2) { // déplacer un pion
			int hauteur = receivedMessage.readInt();
			int xCell = receivedMessage.readInt();
			int yCell = receivedMessage.readInt();
			int hauteur_init = receivedMessage.readInt();
			int xCell_init = receivedMessage.readInt();
			int yCell_init = receivedMessage.readInt();
			TeamType equipeAutreJoueur = partieActuelle.getEquipeAdverse();
			partieActuelle.deplacerUnPion(equipeAutreJoueur, hauteur, xCell, yCell, hauteur_init, xCell_init, yCell_init);
		}

		if (messageID == 3) { // reprendre un pion
			int hauteur = receivedMessage.readInt();
			int xCell = receivedMessage.readInt();
			int yCell = receivedMessage.readInt();
			TeamType equipeAutreJoueur = partieActuelle.getEquipeAdverse();
			partieActuelle.reprendUnPion(equipeAutreJoueur, hauteur, xCell, yCell);
		}

		if (messageID == 4) { // passer son tour
			partieActuelle.tourSuivant();
		}
	}
	
	private boolean reseauLocal_verifieConnexion() {
		//System.out.println("GameHandler.reseauLocal_verifieConnexion");
		if (partieActuelle.modeDeJeu != ModeDeJeu.RESEAU_LOCAL) return false; // seulement exécuté si partie en réseau local
		TCPClient tcp = RoomReseauLocalAttenteHandler.autreJoueurLocalTCPClient;
		if (tcp == null) {
			GraphicsHandler.roomGoTo_menuChoixTypePartie();
			return false;
		}
		if (tcp.isConnected() == false) {
			GraphicsHandler.roomGoTo_menuChoixTypePartie();
			return false;
		}
		return true;
	}
	
	private void reseauLocal_envoieMessage(NetBuffer message) {
		//System.out.println("GameHandler.reseauLocal_envoieMessage");
		if (reseauLocal_verifieConnexion() == false) return;
		TCPClient tcp = RoomReseauLocalAttenteHandler.autreJoueurLocalTCPClient;
		tcp.sendMessage(message);
	}
	
	private void reseauLocal_posePionDeSaReserve(int hauteur, int xCell, int yCell) {
		//System.out.println("GameHandler.reseauLocal_posePionDeSaReserve");
		NetBuffer message = new NetBuffer();
		message.writeInt(1);
		message.writeInt(hauteur);
		message.writeInt(xCell);
		message.writeInt(yCell);
		reseauLocal_envoieMessage(message);
	}
	private void reseauLocal_deplacePion(int hauteur, int xCell, int yCell, int hauteur_init, int xCell_init, int yCell_init) {
		//System.out.println("GameHandler.reseauLocal_deplacePion");
		NetBuffer message = new NetBuffer();
		message.writeInt(2);
		message.writeInt(hauteur);
		message.writeInt(xCell);
		message.writeInt(yCell);
		message.writeInt(hauteur_init);
		message.writeInt(xCell_init);
		message.writeInt(yCell_init);
		reseauLocal_envoieMessage(message);
	}

	private void reseauLocal_reprendUnPion(int hauteur, int xCell, int yCell) {
		//System.out.println("GameHandler.reseauLocal_reprendUnPion");
		NetBuffer message = new NetBuffer();
		message.writeInt(3);
		message.writeInt(hauteur);
		message.writeInt(xCell);
		message.writeInt(yCell);
		reseauLocal_envoieMessage(message);
	}
	
	private void reseauLocal_passeSonTour() {
		//System.out.println("GameHandler.reseauLocal_passeSonTour");
		NetBuffer message = new NetBuffer();
		message.writeInt(4);
		reseauLocal_envoieMessage(message);
	}
	
	
}

class MouseIsAbovePionSelection {
	public static boolean check(int xMouse, int yMouse) {
		int xDraw = GameHandler.jeuActuel.xDessinJetonsJoueur;
		int yDraw = GameHandler.jeuActuel.yDessinJetonsJoueur;
		
		if ((xDraw <= xMouse)
		&& (xDraw + CellDetection.cellWidth > xMouse)
		&& (yDraw <= yMouse)
		&& (yDraw + CellDetection.cellHeight > yMouse)) {
			return true;
		}
		return false;
	}
}
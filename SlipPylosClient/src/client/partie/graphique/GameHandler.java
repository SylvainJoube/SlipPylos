package client.partie.graphique;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;

import client.outils.graphiques.PImage;
import commun.partie.nonGraphique.*;





public class GameHandler {
	
	///** True if the game is currently "running", i.e. the game loop is looping */
	//private boolean gameRunning = true;
	protected boolean waitingForKeyPress = false;
	protected String message = "Bienvenue !";
	
	//public boolean joueurVeutDeplacerUnPion = false; // quand le joueur essaie de déplacer un pion pour le monter
	public int cellWidth = CellDetection.cellWidth;//100; // largeur d'une case de jeu
	public int cellHeight = CellDetection.cellHeight;//100; // hauteur d'une case de jeu
	//int cellSpaceBetweenCells = 4;
	public int xGrid = 120;
	public int yGrid = 120;
	
	public static PylosPartie partieActuelle = null;
	public static GameHandler jeuActuel = null;
	public PylosCellResult poserUnPionIci; // position à laquelle je afficher le pion à poser, indique aussi si je peux poser le pion ou non
	public PylosCell pickUpCell = null;    // pion que je peux reprendre, calculé dans refreshWithMousePosition();
	public PylosCell dragCell = null;      // bouger un des pions du joueur
	public PylosCell highlightCell = null; // mettre en surbrillance une case (souris passe dessus)
	

	public int xDessinJetons = 10;
	public int yDessinJetons = yGrid;
	// Calculé dans drawAllJetonsNb() :
	public int xDessinJetonsJoueur = 0;
	public int yDessinJetonsJoueur = 0;
	public VolonteJoueur volonteJoueur = VolonteJoueur.MAIN_LIBRE;
	public static boolean highlightPionSelecton = false;
	
	public static BoxPosition tourSuivantPos = new BoxPosition(400, 10, 500, 50);
	public static boolean tourSuivantHighlight = false;
	
	
	public int lastMouseX = 0;
	public int lastMouseY = 0;
	
	/** Constructeur, crée l'objet partie
	 */
	public GameHandler(ModeDeJeu arg_modeDeJeu) {
		GameHandler.jeuActuel = this;
		GameHandler.partieActuelle = new PylosPartie(arg_modeDeJeu); // créer la partie avant les Listener
	}
	
	public void setAsCurrentRoom() {
		if (jeuActuel != null) {
			GraphicsHandler ginstance = GraphicsHandler.getMainInstance();
			ginstance.addMouseListener(new MyMouseListener());
			ginstance.addMouseMotionListener(new MyMouseMotionListener());
		}
	}
	
	
	
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
		
		GameHandler.jeuActuel.highlightCell = null;
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
		
		// 
		if (GameHandler.jeuActuel.volonteJoueur == VolonteJoueur.MAIN_LIBRE
			|| GameHandler.jeuActuel.volonteJoueur == VolonteJoueur.REPRENDRE_UN_PION)
		if (GameHandler.partieActuelle.tourDe == GameHandler.partieActuelle.equipeJoueur) {
			PylosCellResult res = CellDetection.getCellUnderMouse(mousePos, gridPos, GameHandler.partieActuelle, GameHandler.partieActuelle.equipeJoueur, -1);
			GameHandler.jeuActuel.highlightCell = res;
			
			// Si le joueur doit reprendre un pion, je regarde s'il peut reprendre le pion sélectionné : GameHandler.jeuActuel.highlightCell
			if (res != null)
			if (GameHandler.jeuActuel.volonteJoueur == VolonteJoueur.REPRENDRE_UN_PION
				&& GameHandler.partieActuelle.plateauActuel.canMovePawn(res, null)) {
				GameHandler.jeuActuel.pickUpCell = res;
			}
			
			//if (res != null) {
			//	System.out.println("MyMouseMotionListener.mouseMoved() : res!=null   " + res);
			//}
		}
		
		if (MouseIsAbovePionSelection.check(GameHandler.jeuActuel.lastMouseX,  GameHandler.jeuActuel.lastMouseY) && !GameHandler.partieActuelle.joueurAJoueUnPion) {
			GameHandler.highlightPionSelecton = true;
		}// else GameHandler.highlightPionSelecton = false;
		
		
		if (GameHandler.tourSuivantPos.isInside(GameHandler.jeuActuel.lastMouseX,  GameHandler.jeuActuel.lastMouseY)) {
			GameHandler.tourSuivantHighlight = true;
		}// else GameHandler.tourSuivantHighlight = false;
		
		if (GameHandler.jeuActuel.volonteJoueur == VolonteJoueur.PION_EN_MAIN
			|| GameHandler.jeuActuel.volonteJoueur == VolonteJoueur.DEPLACER_UN_PION) {
			
			// Condition sur la hauteur : si je veux déplacer un pion, il faut que la destination soit plus haute
			int hateurMinimale = -1;
			if (GameHandler.jeuActuel.volonteJoueur == VolonteJoueur.DEPLACER_UN_PION)
				hateurMinimale = GameHandler.jeuActuel.dragCell.hauteur + 1;
			
			
			PylosCellResult res = CellDetection.getCellUnderMouse(mousePos, gridPos, GameHandler.partieActuelle, TeamType.AUCUNE, hateurMinimale);
			if (res != null) {
				boolean canMovePawn = partieActuelle.plateauActuel.canMovePawn(res, GameHandler.jeuActuel.dragCell);
				res.peutPoserIci = ( res.peutPoserIci && canMovePawn );
			}
			GameHandler.jeuActuel.poserUnPionIci = res;
			
			
		}// else
		//	GameHandler.jeuActuel.poserUnPionIci = null;
		
		// Reprendre un pion : regarder si je peux prendre le pion sous la souris
		
		
	}
	
	// Dessin du jeton avec écrit le nombre de jetons restants dessus
	public void drawJetonNb(Graphics2D g, Image jetonImage, int nb, int x, int y, Color fontColor) {
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
		
		Color oldColor = g.getColor();
		g.setColor(fontColor);
		g.drawString(nbStr, imageCenter.x - strWidthHalf, imageCenter.y - strHeightHalf);
		g.setColor(oldColor);
		
	} // g.getFontMetrics().stringWidth(message)
	
	

	private Image jetonNoirImg = RessourceManager.LoadImage("images/JetonNoir.png");
	private Image jetonBlancImg = RessourceManager.LoadImage("images/JetonBlanc.png");
	//private Image jetonNoirImg_highlight = RessourceManager.LoadImage("images/JetonNoirH.png");
	//private Image jetonBlancImg_highlight = RessourceManager.LoadImage("images/JetonBlancH.png");
	private Image jetonErreurImg = RessourceManager.LoadImage("images/JetonErreur.png");
	private Image tourSuivantImg = RessourceManager.LoadImage("images/TourSuivant.png");
	private Image jetonHighlightImg = RessourceManager.LoadImage("images/JetonHighlight.png");
	private Image jetonReprendreImg = RessourceManager.LoadImage("images/ReprendrePion.png");
	private void loadImages() {
		jetonNoirImg = RessourceManager.LoadImage("images/JetonNoir.png");
		jetonBlancImg = RessourceManager.LoadImage("images/JetonBlanc.png");
		//jetonNoirImg_highlight = RessourceManager.LoadImage("images/JetonNoirH.png");
		//jetonBlancImg_highlight = RessourceManager.LoadImage("images/JetonBlancH.png");
		jetonErreurImg = RessourceManager.LoadImage("images/JetonErreur.png");
		tourSuivantImg = RessourceManager.LoadImage("images/TourSuivant.png");
		jetonHighlightImg = RessourceManager.LoadImage("images/JetonHighlight.png");
		jetonReprendreImg = RessourceManager.LoadImage("images/ReprendrePion.png");
	}
	
	private Graphics2D currentGraphics = null;
	
	private void drawAllJetonsNb() {

		int xNoir = xDessinJetons;
		int yNoir = xDessinJetons;
		int xBlanc = xNoir;// + CellDetection.cellWidth;
		int yBlanc = yNoir + CellDetection.cellHeight;
		
		if (partieActuelle.equipeJoueur == TeamType.NOIR) {
			xDessinJetonsJoueur = xDessinJetons;
			yDessinJetonsJoueur = yDessinJetons;
		}
		if (partieActuelle.equipeJoueur == TeamType.BLANC) {
			xDessinJetonsJoueur = xDessinJetons;
			yDessinJetonsJoueur = yDessinJetons;
		}
		
		drawJetonNb(currentGraphics, jetonNoirImg, partieActuelle.nbJetonsNoir, xNoir, yNoir, Color.WHITE);
		drawJetonNb(currentGraphics, jetonBlancImg, partieActuelle.nbJetonsBlanc, xBlanc, yBlanc, Color.BLACK);
		if (highlightPionSelecton) {
			
			if (partieActuelle.equipeJoueur == TeamType.BLANC) {
				PImage.drawImageAlpha(currentGraphics, jetonHighlightImg, xBlanc, yBlanc, 1);
			}
			if (partieActuelle.equipeJoueur == TeamType.NOIR) {
				PImage.drawImageAlpha(currentGraphics, jetonHighlightImg, xNoir, yNoir, 1);
			}
			
			//drawJetonNb(currentGraphics, jetonBlancImg_highlight, partieActuelle.nbJetonsBlanc, xDessinJetonsJoueur, yDessinJetonsJoueur, Color.BLACK);
			
			/*if (partieActuelle.equipeJoueur == TeamType.BLANC) {
				drawJetonNb(currentGraphics, jetonBlancImg_highlight, partieActuelle.nbJetonsBlanc, xDessinJetonsJoueur, yDessinJetonsJoueur, Color.BLACK);
			}
			if (partieActuelle.equipeJoueur == TeamType.NOIR) {
				drawJetonNb(currentGraphics, jetonNoirImg_highlight, partieActuelle.nbJetonsBlanc, xDessinJetonsJoueur, yDessinJetonsJoueur, Color.BLACK);
			}*/
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
				
				if (currentCell.estIdentique(highlightCell)) {
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
			if (volonteJoueur == VolonteJoueur.PION_EN_MAIN || volonteJoueur == VolonteJoueur.DEPLACER_UN_PION) {
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
		// Dessin des pions dans la grille
		drawPawnsOnGrid();
		// Dessin de la future position du pion à poser
		drawPoserPion();
		
		
		currentGraphics.drawImage(tourSuivantImg, tourSuivantPos.x1, tourSuivantPos.y1, null);
		
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
		if (!GameHandler.partieActuelle.joueurAJoueUnPion) { // si le joueur n'a pas encore joué de pion
			
			if (MouseIsAbovePionSelection.check(mouseEvent.mouseX, mouseEvent.mouseY)) {
				GameHandler.jeuActuel.volonteJoueur = VolonteJoueur.PION_EN_MAIN;
				//volonteJoueurPionEnMain = true;
				//LogWriter.Log("MyMouseListener.mousePressed :  prendre un pion !");
			}
			// Tenter de déplacer un pion de son jeu pour le remonter
			if (GameHandler.jeuActuel.volonteJoueur == VolonteJoueur.MAIN_LIBRE)
			if (GameHandler.jeuActuel.dragCell == null)
			if (GameHandler.jeuActuel.highlightCell != null) {
				//LogWriter.Log("MyMouseListener.mousePressed :  déplacer un pion !");
				PylosGridArray plateauActuel = GameHandler.partieActuelle.plateauActuel;
				PylosCell tryMoveCell = GameHandler.jeuActuel.highlightCell;
				boolean canMovePawn = plateauActuel.canMovePawn(tryMoveCell, GameHandler.jeuActuel.dragCell);
				if (canMovePawn) {
					GameHandler.jeuActuel.dragCell = GameHandler.jeuActuel.highlightCell;
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
		
		if (GameHandler.jeuActuel.volonteJoueur == VolonteJoueur.PION_EN_MAIN
			|| GameHandler.jeuActuel.volonteJoueur == VolonteJoueur.DEPLACER_UN_PION) {
			//PylosPoint mousePos = new PylosPoint(mouseEvent.mouseX, mouseEvent.mouseY);
			//PylosPoint gridPos = new PylosPoint(GameHandler.jeuActuel.xGrid,  GameHandler.jeuActuel.yGrid);
			// Condition sur la hauteur : si je veux déplacer un pion, il faut que la destination soit plus haute
			//int hateurMinimale = -1;
			//if (GameHandler.jeuActuel.volonteJoueur == VolonteJoueur.DEPLACER_UN_PION)
			//	hateurMinimale = GameHandler.jeuActuel.dragCell.hauteur + 1;
			
			
			PylosCellResult peutPoserPionIci = GameHandler.jeuActuel.poserUnPionIci;
			
			TeamType equipeJoueur = GameHandler.partieActuelle.equipeJoueur;
			
			if (peutPoserPionIci != null)
			if (peutPoserPionIci.peutPoserIci) { // poser un pion ici
				
				GameHandler.partieActuelle.setCell(peutPoserPionIci.hauteur, peutPoserPionIci.xCell, peutPoserPionIci.yCell, equipeJoueur);
				GameHandler.partieActuelle.joueurAJoueUnPion = true;
				
				if (GameHandler.jeuActuel.volonteJoueur != VolonteJoueur.DEPLACER_UN_PION) {
					if (equipeJoueur == TeamType.BLANC) GameHandler.partieActuelle.nbJetonsBlanc--;
					if (equipeJoueur == TeamType.NOIR)  GameHandler.partieActuelle.nbJetonsNoir--;
				} else {
					// Je supprime l'ancien pion
					PylosCell dragCell = GameHandler.jeuActuel.dragCell;
					GameHandler.partieActuelle.setCell(dragCell.hauteur, dragCell.xCell, dragCell.yCell, TeamType.AUCUNE);
				}
				
				if (GameHandler.partieActuelle.plateauActuel.willFormSameColorRectangle(peutPoserPionIci.hauteur, peutPoserPionIci.xCell, peutPoserPionIci.yCell, equipeJoueur)) {
					GameHandler.partieActuelle.peutReprendrePionsNb = 2;
				}
				//GameHandler.partieActuelle.tourSuivant();
			}
		}
		
		if (GameHandler.jeuActuel.volonteJoueur == VolonteJoueur.REPRENDRE_UN_PION) {
			if (GameHandler.jeuActuel.pickUpCell != null) { // actualisé juste au-dessus via : GameHandler.jeuActuel.refreshWithMousePosition();
				// Reprendre ce pion
				PylosCell pickUpCell = GameHandler.jeuActuel.pickUpCell;
				GameHandler.partieActuelle.setCell(pickUpCell.hauteur, pickUpCell.xCell, pickUpCell.yCell, TeamType.AUCUNE);
				TeamType equipeJoueur = GameHandler.partieActuelle.equipeJoueur;
				if (equipeJoueur == TeamType.BLANC) GameHandler.partieActuelle.nbJetonsBlanc++;
				if (equipeJoueur == TeamType.NOIR)  GameHandler.partieActuelle.nbJetonsNoir++;
				
				GameHandler.partieActuelle.peutReprendrePionsNb--;
				GameHandler.jeuActuel.refreshWithMousePosition(); // actualisation fonctionnelle et graphique
			}
		}
		
		
		//if (!volonteJoueurPionEnMain) {
		//	GameHandler.jeuActuel.volonteJoueur = VolonteJoueur.MAIN_LIBRE;
		//}
		GameHandler.jeuActuel.volonteJoueur = VolonteJoueur.MAIN_LIBRE;
		PylosPartie partie = GameHandler.partieActuelle;
		if (GameHandler.tourSuivantPos.isInside(mouseEvent.mouseX, mouseEvent.mouseY)) {
			boolean peutChangerDeTour = true;
			if (!partie.joueurAJoueUnPion) {
				LogWriter.Log("MyMouseListener.mousePressed : Impossible de passer votre tour lorsque vous n'avez pas encore joué !");
				peutChangerDeTour = false;
			}
			
			if (GameHandler.partieActuelle.peutReprendrePionsNb > 1) {
				LogWriter.Log("MyMouseListener.mousePressed : Impossible de passer votre tour lorsque vous n'avez repris au moins un des pions qui vons sont dûs !");
				peutChangerDeTour = false;
			}
			
			if (peutChangerDeTour)
			if (partie.tourDe == partie.equipeJoueur) {
				partie.tourSuivant();
			}
		}
		
		GameHandler.jeuActuel.dragCell = null;
		GameHandler.jeuActuel.refreshWithMousePosition();
	}
	

	public void mouseMoved(ListenerMouseEvent mouseEvent) {
		//LogWriter.Log("MyMouseMotionListener.mouseMoved :  mouseEvent.mouseX = " + mouseEvent.mouseX + " mouseEvent.mouseY = " + mouseEvent.mouseY);
		GameHandler.jeuActuel.lastMouseX = mouseEvent.mouseX;
		GameHandler.jeuActuel.lastMouseY = mouseEvent.mouseY;
		GameHandler.jeuActuel.refreshWithMousePosition();
		
	}
	
	
}
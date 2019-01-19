package client.partie.graphique;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.ArrayList;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;

import client.outils.graphiques.PImage;
import commun.partie.nonGraphique.*;





public class GameHandler  extends Canvas {
	
	/** The strategy that allows us to use accelerate page flipping */
	private BufferStrategy strategy;
	/** True if the game is currently "running", i.e. the game loop is looping */
	private boolean gameRunning = true;
	protected boolean waitingForKeyPress = false;
	protected String message = "Bienvenue !";

	public final int windowWidth = 1400;
	public final int windowHeight = 960;
	
	//public boolean joueurVeutDeplacerUnPion = false; // quand le joueur essaie de déplacer un pion pour le monter
	public int cellWidth = CellDetection.cellWidth;//100; // largeur d'une case de jeu
	public int cellHeight = CellDetection.cellHeight;//100; // hauteur d'une case de jeu
	//int cellSpaceBetweenCells = 4;
	public int xGrid = 120;
	public int yGrid = 120;
	
	public static PylosPartie partieActuelle = null;
	public static GameHandler jeuActuel = null;
	public PylosCellResult poserUnPionIci; // 
	public int xDessinJetonsJoueur = 110;
	public int yDessinJetonsJoueur = 10;
	public VolonteJoueur volonteJoueur = VolonteJoueur.MAIN_LIBRE;
	public static boolean highlightPionSelecton = false;

	public static BoxPosition tourSuivantPos = new BoxPosition(400, 10, 500, 50);
	public static boolean tourSuivantHighlight = false;

	public PylosCell dragCell = null; // bouger un des pions du joueur
	public PylosCell highlightCell = null; // mettre en surbrillance une case
	
	public int lastMouseX = 0;
	public int lastMouseY = 0;
	
	
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
	
	
	/** True if game logic needs to be applied this loop, normally as a result of a game event */
	//private boolean logicRequiredThisLoop = false;
	
	public void initWindow() {
		JFrame container = new JFrame("Jeu de pylos en java");
		JPanel panel = (JPanel) container.getContentPane(); // get hold the content of the frame and set up the resolution of the game
		panel.setPreferredSize(new Dimension(windowWidth, windowHeight));
		panel.setLayout(null);
		setBounds(0, 0, windowWidth, windowHeight); // setup our canvas size and put it into the content of the frame
		panel.add(this);
		setIgnoreRepaint(true); // Tell AWT not to bother repainting our canvas since we're going to do that ourself in accelerated mode
		container.pack(); // finally make the window visible 
		container.setResizable(false);
		container.setVisible(true);
		// add a listener to respond to the user closing the window. If they do we'd like to exit the game
		container.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		// create the buffering strategy which will allow AWT to manage our accelerated graphics
		createBufferStrategy(2);
		strategy = getBufferStrategy();
		LogWriter.Log("GameHandler.InitWindow : currentPath = " + System.getProperty("user.dir"));
		
		partieActuelle = new PylosPartie(); // créer la partie avant les Listener
		
		this.addMouseListener(new MyMouseListener());
		//LogWriter.Log("GameHandler.InitWindow : log2");
		this.addMouseMotionListener(new MyMouseMotionListener());
		//LogWriter.Log("GameHandler.InitWindow : log3");
		
	}
	
	public void refreshWithMousePosition() {
		PylosPoint mousePos = new PylosPoint(GameHandler.jeuActuel.lastMouseX,  GameHandler.jeuActuel.lastMouseY);
		PylosPoint gridPos = new PylosPoint(GameHandler.jeuActuel.xGrid,  GameHandler.jeuActuel.yGrid);
		
		GameHandler.jeuActuel.highlightCell = null;
		// Si c'est l'équipe du joueur qui joue, 
		if (GameHandler.jeuActuel.volonteJoueur == VolonteJoueur.MAIN_LIBRE)
		if (GameHandler.partieActuelle.tourDe == GameHandler.partieActuelle.equipeJoueur) {
			PylosCellResult res = CellDetection.getCellUnderMouse(mousePos, gridPos, GameHandler.partieActuelle, GameHandler.partieActuelle.equipeJoueur, -1);
			GameHandler.jeuActuel.highlightCell = res;
			//if (res != null) {
			//	System.out.println("MyMouseMotionListener.mouseMoved() : res!=null   " + res);
			//}
		}
		
		if (MouseIsAbovePionSelection.check(GameHandler.jeuActuel.lastMouseX,  GameHandler.jeuActuel.lastMouseY) && !GameHandler.partieActuelle.joueurAJoueUnPion) {
			GameHandler.highlightPionSelecton = true;
		} else GameHandler.highlightPionSelecton = false;
		
		
		if (GameHandler.tourSuivantPos.isInside(GameHandler.jeuActuel.lastMouseX,  GameHandler.jeuActuel.lastMouseY)) {
			GameHandler.tourSuivantHighlight = true;
		} else GameHandler.tourSuivantHighlight = false;
		
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
			
			
		} else
			GameHandler.jeuActuel.poserUnPionIci = null;
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
		int imgWidth = jetonImage.getWidth(null);
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
	
	
	public void gameLoop() {
		long lastLoopTime = System.currentTimeMillis();
		
		
		
		// keep looping round till the game ends
		while (gameRunning) {
			// work out how long its been since the last update, this will be used to calculate how far the entities should move this loop
			long delta = System.currentTimeMillis() - lastLoopTime;
			lastLoopTime = System.currentTimeMillis();
			
			// Get hold of a graphics context for the accelerated surface and blank it out
			Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
			g.setColor(Color.darkGray);
			g.fillRect(0, 0, windowWidth, windowHeight);
			Point mousePos = MouseInfo.getPointerInfo().getLocation();

			//Image img = RessourceManager.LoadImage("monImage.png");
			//g.drawImage(img, 0, 0, null);
			
			// Affichage du nombre de jetons restants

			Image jetonNoirImg = RessourceManager.LoadImage("images/JetonNoir.png");
			Image jetonBlancImg = RessourceManager.LoadImage("images/JetonBlanc.png");
			Image jetonNoirImg_highlight = RessourceManager.LoadImage("images/JetonNoirH.png");
			Image jetonBlancImg_highlight = RessourceManager.LoadImage("images/JetonBlancH.png");
			Image jetonErreurImg = RessourceManager.LoadImage("images/JetonErreur.png");
			Image tourSuivantImg = RessourceManager.LoadImage("images/TourSuivant.png");
			Image jetonHighlight = RessourceManager.LoadImage("images/JetonHighlight.png");
			
			drawJetonNb(g, jetonNoirImg, partieActuelle.nbJetonsNoir, 10, 10, Color.WHITE);
			drawJetonNb(g, jetonBlancImg, partieActuelle.nbJetonsBlanc, xDessinJetonsJoueur, yDessinJetonsJoueur, Color.BLACK);
			if (highlightPionSelecton) {
				if (partieActuelle.equipeJoueur == TeamType.BLANC) {
					drawJetonNb(g, jetonBlancImg_highlight, partieActuelle.nbJetonsBlanc, xDessinJetonsJoueur, yDessinJetonsJoueur, Color.BLACK);
				}
				if (partieActuelle.equipeJoueur == TeamType.NOIR) {
					drawJetonNb(g, jetonNoirImg_highlight, partieActuelle.nbJetonsBlanc, xDessinJetonsJoueur, yDessinJetonsJoueur, Color.BLACK);
				}
			}
			
			// Dessin de la grille
			Color gridColor = Color.WHITE;
			g.setColor(gridColor);
			int xGridMax = xGrid + partieActuelle.nbCasesCote * cellWidth;
			int yGridMax = yGrid + partieActuelle.nbCasesCote * cellHeight;
			for (int xCell = 0; xCell <= partieActuelle.nbCasesCote; xCell++) {
				int currentXGrid = xGrid + xCell * cellWidth;
				g.drawLine(currentXGrid , yGrid, currentXGrid, yGridMax);
			}
			for (int yCell = 0; yCell <= partieActuelle.nbCasesCote; yCell++) {
				int currentYGrid = yGrid + yCell * cellWidth;
				g.drawLine(xGrid , currentYGrid, xGridMax, currentYGrid);
			}
			
			// Dessin des pions dans la grille

			// Pout toutes les grilles (dans l'ordre de hauteur)
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
						case BLANC : PImage.drawImageAlpha(g, jetonBlancImg, xCellPx, yCellPx, drawWithAlpha); break; //g.drawImage(jetonBlancImg, xCellPx, yCellPx, null); break;
						case NOIR : PImage.drawImageAlpha(g, jetonNoirImg, xCellPx, yCellPx, drawWithAlpha); break;
						default : break;
					}

					if (currentCell.estIdentique(highlightCell)) {
						PImage.drawImageAlpha(g, jetonHighlight, xCellPx, yCellPx, 1);
					}

					
					
					
						
						/*switch (currentCell.occupeePar) {
						case BLANC : g.drawImage(jetonBlancImg_highlight, xCellPx, yCellPx, null); break;
						case NOIR : g.drawImage(jetonNoirImg_highlight, xCellPx, yCellPx, null); break;
						default : break;
						}*/
					
				}
				
				
			}
			
			
			Color col = new Color(200, 20, 200);
			g.setColor(col);
			
			g.drawString("Mouse pos : " + mousePos.x + ", " + mousePos.y, 10, 100);
			
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
					PImage.drawImageAlpha(g, imageDrawn, xDraw, yDraw, 0.5);
				}
				
				/*if (partieActuelle.equipeJoueur == TeamType.NOIR) g.drawImage(jetonNoirImg, xDraw, yDraw, null);
				if (partieActuelle.equipeJoueur == TeamType.BLANC) g.drawImage(jetonBlancImg, xDraw, yDraw, null);*/
				
				
				if (!poserUnPionIci.peutPoserIci)
					PImage.drawImageAlpha(g, jetonErreurImg, xDraw, yDraw, 0.5);//g.drawImage(jetonErreurImg, xDraw, yDraw, null);
					
				
			} else {
				if (volonteJoueur == VolonteJoueur.PION_EN_MAIN || volonteJoueur == VolonteJoueur.DEPLACER_UN_PION) {
					Image imageDrawn = null;
					switch (partieActuelle.equipeJoueur) {
					case NOIR : imageDrawn = jetonNoirImg; break;
					case BLANC : imageDrawn = jetonBlancImg; break;
					default : break;
					}
					if (imageDrawn != null) {
						PImage.drawImageAlpha_centered(g, imageDrawn, lastMouseX, lastMouseY, 0.3);
						PImage.drawImageAlpha_centered(g, jetonErreurImg, lastMouseX, lastMouseY, 0.3);
					}
				}
			}
			
			
			g.drawImage(tourSuivantImg, tourSuivantPos.x1, tourSuivantPos.y1, null);
			
			
			
			// if we're waiting for an "any key" press then draw the 
			// current message 
			if (waitingForKeyPress) {
				g.setColor(Color.white);
				g.drawString(message,(800-g.getFontMetrics().stringWidth(message))/2,250);
				g.drawString("Press any key",(800-g.getFontMetrics().stringWidth("Press any key"))/2,300);
			}
			
			
			
			// finally, we've completed drawing so clear up the graphics
			// and flip the buffer over
			g.dispose();
			strategy.show();
			
			// finally pause for a bit. Note: this should run us at about
			// 100 fps but on windows this might vary each loop due to
			// a bad implementation of timer
			try { Thread.sleep(16); } catch (Exception e) {}
		}
	}

}
package client.room1;

import java.awt.Graphics2D;
import java.awt.Image;
import java.util.ArrayList;

import client.listeners.GenericMouseListener;
import client.listeners.GenericMouseMotionListener;
import client.listeners.Listeners;
import client.outils.graphiques.BoxPosition;
import client.outils.graphiques.GraphicsHandler;
import client.outils.graphiques.PImage;
import client.partie.graphique.RessourceManager;
import client.partie.graphique.RoomType;
import commun.partie.nonGraphique.ModeDeJeu;


class Room1Button {
	
	public int id;
	//public RoomType goToRoom;
	public int x, y;
	Image displayImage;
	//public RoomType goToRoom;
	
	public Room1Button(int arg_id, int arg_x, int arg_y, Image arg_displayImage) {
		//goToRoom = arg_goToRoom;
		id = arg_id;
		x = arg_x;
		y = arg_y;
		displayImage = arg_displayImage;
	}
	
	
}

public class Room1Handler {
	
	public final static Room1Handler instance = new Room1Handler(); // unique instance
	
	private double time = 0;
	private int buttonWidth = 400;
	private int buttonHeight = 42;
	private int buttonHeightSpace = 8;
	
	/*
	public void setAsCurrentRoom() {
		GraphicsHandler ginstance = GraphicsHandler.getMainInstance();
		ginstance.addMouseListener(new GenericMouseListener());
		ginstance.addMouseMotionListener(new GenericMouseMotionListener());
	}*/
	
	private Graphics2D currentGraphics;
	private ArrayList<Room1Button> buttonList = new ArrayList<Room1Button>();
	public Room1Handler() {
		initButtons();
	}
	
	/** Initialiser les boutons et leur position
	 */
	private void initButtons() {
		loadImages();
		Room1Button button;
		int xButton = (int) ((GraphicsHandler.windowWidth - buttonWidth)/2);
		int yButton = 200;
		// Jouer en solo -> écran suivant partie locale contre IA, choix de la difficulté
		button = new Room1Button(1, xButton, yButton, spr_MenuSelectionJeu_solo);
		buttonList.add(button);
		yButton += buttonHeight + buttonHeightSpace;
		// Jouer en hot-seat -> écran suivant partie en hot seat
		button = new Room1Button(2, xButton, yButton, spr_MenuSelectionJeu_hotSeat);
		buttonList.add(button);
		yButton += buttonHeight + buttonHeightSpace;
		// Jouer en réseau local -> écran suivant héberger ou rejoindre
		button = new Room1Button(3, xButton, yButton, spr_MenuSelectionJeu_reseauLocal);
		buttonList.add(button);
		yButton += buttonHeight + buttonHeightSpace;
		// Jouer via Internet -> écran suivant "se connecter"
		button = new Room1Button(4, xButton, yButton, spr_MenuSelectionJeu_internet);
		buttonList.add(button);
		yButton += buttonHeight + buttonHeightSpace;
	}
	
	public void loop() {
		currentGraphics = GraphicsHandler.getMainGraphics();
		
		//loadImages();

		int xMouse = Listeners.frame_getMouseX();
		int yMouse = Listeners.frame_getMouseY();
		
		int mouseOverButtonIndex = -1;
		Room1Button mouseOverButton = null;
		int defaultPosOffset = 3;
		for (int iButton = 0; iButton < buttonList.size(); iButton++) {
			Room1Button button = buttonList.get(iButton);
			int posOffset = 0;
			BoxPosition box = new BoxPosition(button.x, button.y, button.x + buttonWidth, button.y + buttonHeight);
			if (box.isInside(xMouse,  yMouse)) { // souris
				mouseOverButtonIndex = iButton;
				mouseOverButton = button;
				posOffset = defaultPosOffset;
				if (Listeners.frame_mouseReleased() && iButton == 0) {
					// Changer de salle : 
					//GraphicsHandler.getMainInstance().currentRoomType = RoomType.PARTIE;
					GraphicsHandler.roomGoTo_game(ModeDeJeu.SOLO_LOCAL);
				}
				if (Listeners.frame_mouseReleased() && iButton == 1) {
					// Changer de salle : 
					//GraphicsHandler.getMainInstance().currentRoomType = RoomType.PARTIE;
					GraphicsHandler.roomGoTo_game(ModeDeJeu.HOT_SEAT);
				}
				if (Listeners.frame_mouseReleased() && iButton == 2) {
					// Changer de salle : 
					//GraphicsHandler.getMainInstance().currentRoomType = RoomType.PARTIE;
					GraphicsHandler.roomGoTo_menuReseauLocal();
				}
				
				double colorIntensity = 0.1;
				PImage.drawImageColorAlpha(currentGraphics, button.displayImage, button.x, button.y, colorIntensity, colorIntensity, colorIntensity, 1);
			}
			PImage.drawImageAlpha(currentGraphics, button.displayImage, button.x - posOffset, button.y - posOffset, 1);
		}
		time += 0.2;
		if (mouseOverButton != null) {
			double imgAlpha = ((Math.cos(time) + 1) / 2) * 0.2 + 0.2;
			PImage.drawImageAlpha(currentGraphics, spr_MenuSelectionJeu_bright, mouseOverButton.x - defaultPosOffset, mouseOverButton.y - defaultPosOffset, imgAlpha);
		}
		 
		//PImage.drawImageAlpha(currentGraphics, spr_MenuSelectionJeu_solo, 10, 10, 1);
		//time = time % 2 * Math.PI;
		
	}
	
	public static void staticLoop() {
		if (instance != null) {
			instance.loop();
		}
	}
	
	private Image spr_MenuSelectionJeu_hotSeat = null;
	private Image spr_MenuSelectionJeu_internet = null;
	private Image spr_MenuSelectionJeu_reseauLocal = null;
	private Image spr_MenuSelectionJeu_solo = null;
	private Image spr_MenuSelectionJeu_bright = null;
	private Image spr_Bouton_retour = null;
	private void loadImages() {
		spr_MenuSelectionJeu_hotSeat = RessourceManager.LoadImage("images/MenuSelectionJeu_hotSeat.png");
		spr_MenuSelectionJeu_internet = RessourceManager.LoadImage("images/MenuSelectionJeu_internet.png");
		spr_MenuSelectionJeu_reseauLocal = RessourceManager.LoadImage("images/MenuSelectionJeu_reseauLocal.png");
		spr_MenuSelectionJeu_solo = RessourceManager.LoadImage("images/MenuSelectionJeu_solo.png");
		spr_MenuSelectionJeu_bright = RessourceManager.LoadImage("images/MenuSelectionJeu_bright.png");
		
		
	}
	
	
	
}

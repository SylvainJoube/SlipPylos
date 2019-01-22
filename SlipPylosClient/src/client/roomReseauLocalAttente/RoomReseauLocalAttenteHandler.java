package client.roomReseauLocalAttente;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.Random;

import javax.swing.JPanel;
import javax.swing.JTextField;

import client.outils.graphiques.GraphicsHandler;
import slip.network.tcp.TCPServer;

/**
 * Gère la salle où paramétrer un serveur de réseau local / rejoindre un serveur
 * Deux gros boutons à gauche : créer un serveur et rejoindre un serveur
 * 
 * OU, encore plus simple :
 * -> affichage de mon identifiant de salon (192....)
 * -> affichage de l'interface pour rejoindre un autre salon
 * 
 */
public class RoomReseauLocalAttenteHandler {
	
	private Graphics2D currentGraphics;
	
	private JTextField inputSalonHost = null;
	private JTextField inputSalonPort = null;
	private int xInputSalonPort;
	private int xAdresseServeurLocal, yAdresseServeurLocal;
	private int xInputSalon, yInputSalon;
	private int xBoutonRejoindreServeur, yBoutonRejoindreServeur;
	private static RoomReseauLocalAttenteHandler instance = new RoomReseauLocalAttenteHandler();
	private boolean alreadyAddedToPanel = false;
	private Font textFieldFont;
	private Font nameFont;
	private Color nameColor;
	private TCPServer localServer;
	public static boolean estLeServeur = false; // true si c'est le serveur, false si c'est le client
	
	// Impossible de créer une instance de l'extérieur
	private RoomReseauLocalAttenteHandler() {
		inputSalonHost = new JTextField("");
		inputSalonPort = new JTextField("");
		xInputSalon = 500;
		yInputSalon = 100;
		int inputSalonHostWidth = 200;
		int inputSalonHostHeight = 30;
		int inputSalonPortWidth = 60;
		inputSalonHost.setBounds(xInputSalon, yInputSalon, inputSalonHostWidth, inputSalonHostHeight);
		int xInputSalonPort = xInputSalon + inputSalonHostWidth + 10;
		inputSalonPort.setBounds(xInputSalonPort, yInputSalon, inputSalonPortWidth, inputSalonHostHeight);
		xBoutonRejoindreServeur = xInputSalonPort + inputSalonHostHeight + 10;
		yBoutonRejoindreServeur = yInputSalon;
		textFieldFont = new Font("TimesRoman", Font.BOLD, 16);
		nameFont = new Font("TimesRoman", Font.BOLD, 20);
		nameColor = new Color(236, 232, 230);
		xAdresseServeurLocal = 100;
		yAdresseServeurLocal = 100;
		
	}
	
	public static void addOnceToPanel(JPanel panel) {
		if (instance.alreadyAddedToPanel) return;
		panel.add(instance.inputSalonHost);
		panel.add(instance.inputSalonPort);
		instance.inputSalonHost.setVisible(false);
		instance.inputSalonPort.setVisible(false);
		instance.alreadyAddedToPanel = true;
		
		Color col = new Color(236, 232, 230);
		instance.inputSalonHost.setBackground(col);
		//exempleTextField.setSelectedTextColor(Color.RED);
		instance.inputSalonHost.setForeground(Color.black);
		instance.inputSalonHost.setBorder(null);
		instance.inputSalonHost.setFont(instance.textFieldFont);
		//currentGraphics.setFont(); 
		//currentGraphics.drawString(currentRoomName, xRoomName, yRoomName);

		instance.inputSalonPort.setBackground(col);
		//exempleTextField.setSelectedTextColor(Color.RED);
		instance.inputSalonPort.setForeground(Color.black);
		instance.inputSalonPort.setBorder(null);
		instance.inputSalonPort.setFont(instance.textFieldFont);
		//Font ft = exempleTextField.getFont();
	}
	
	public static void setVisibleFields(boolean visible) {
		instance.inputSalonHost.setVisible(visible);
		instance.inputSalonPort.setVisible(visible);
	}
	
	public static void startLocalServer() {
		Random rand = new Random();
		rand.setSeed(System.currentTimeMillis());
		int minPortNumber = 10000;
		int maxPortNumber = 40000;
		int plagePorts = maxPortNumber - minPortNumber;
		int choosenPort;
		int maxTrialNb = 20;
		boolean success = false;
		for (int iTrial = 0; iTrial < maxTrialNb; iTrial++) {
			choosenPort = rand.nextInt() % (plagePorts) + minPortNumber;
			choosenPort = 8887;
			instance.localServer = new TCPServer(choosenPort);
			if (instance.localServer.isListening()) {
				success = true;
				break;
			}
		}
		if (success == false) instance.localServer = null;
	}
	
	public static void stopLocalServer() {
		if (instance.localServer == null) return;
		instance.localServer.stop();
		instance.localServer = null;
	}
	
	public static void staticLoop() {
		instance.loop();
	}
	
	public void loop() {
		currentGraphics = GraphicsHandler.getMainGraphics();
		
		// Dessin de mon numéro de salon (serveur TCP ouvert)
		// Dessin de l'adresse de salon à rejoindre
		currentGraphics.setFont(nameFont);
		currentGraphics.setColor(nameColor);
		
		boolean unableToStartServer = false;
		String salonAddr = null;
		
		if (localServer == null) {
			unableToStartServer = true;
		} else {
			salonAddr = TCPServer.getLocalHostAddress(false);
			if (salonAddr == null) {
				unableToStartServer = true;
			}
		}
		
		if (unableToStartServer) {
			currentGraphics.drawString("ERREUR : Impossible de lancer le serveur local.", xAdresseServeurLocal, yAdresseServeurLocal);
		} else {
			currentGraphics.drawString("Adresse du salon à donner : " + salonAddr + " - " + localServer.getListeningPort(), xAdresseServeurLocal, yAdresseServeurLocal);
		}
		
		// Actaliser le serveur local pour savoir si un client vient de s'y connecter
		// Appuyer sur le bouton "rejoindre un serveur" et essayer de s'y connecter, actualiser la réponse (tant que pas de réponse, impossible d'envoyer une nouvelle requête)
		// Si nouvelle réponse, bien vérifier que c'est un client Pylos de la bonne version, et lancer une partie en réseau local.
		
		// xBoutonRejoindreServeur
		
	}
}

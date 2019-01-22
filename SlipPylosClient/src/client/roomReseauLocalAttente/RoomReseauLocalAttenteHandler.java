package client.roomReseauLocalAttente;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.Random;

import javax.swing.JPanel;
import javax.swing.JTextField;

import client.outils.graphiques.GraphicsHandler;
import client.outils.graphiques.PImage;
import client.partie.graphique.RessourceManager;
import commun.partie.nonGraphique.ModeDeJeu;
import slip.network.buffers.NetBuffer;
import slip.network.tcp.TCPClient;
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
	
	private final int pylosVersion = 1;
	private final String verificationJeuDePylos = "Vérification réseau local - Jeu de Pylos en java par Slipsoft"; // Le message d'un nouveau socket réseau local connecté devra être celui-ci !
	
	private static long randomNumberPourNePasSAccepterSoiMeme = 0; // mis à jour dans startLocalServer();
	
	private JTextField inputSalonHost = null;
	private JTextField inputSalonPort = null;
	private int xInputSalonPort;
	private int xAdresseServeurLocal, yAdresseServeurLocal;
	private int xInputSalon, yInputSalon;
	private String tryToConnect_currentMessage = "";
	private int xConnectMessage, yConnectMessage;
	private int xBoutonRejoindreServeur, yBoutonRejoindreServeur;
	private static RoomReseauLocalAttenteHandler instance = new RoomReseauLocalAttenteHandler();
	private boolean alreadyAddedToPanel = false;
	private Font textFieldFont;
	private Font nameFont, connextionMessageFont;
	private Color nameColor;
	private TCPServer localServer;
	public static boolean estLeServeur = false; // true si c'est le serveur, false si c'est le client
	
	
	private Image spr_Bouton_rejoindre = null;
	
	// Impossible de créer une instance de l'extérieur
	private RoomReseauLocalAttenteHandler() {
		inputSalonHost = new JTextField("");
		inputSalonPort = new JTextField("");
		spr_Bouton_rejoindre = RessourceManager.LoadImage("images/Bouton_rejoindre.png");
		System.out.println("RoomReseauLocalAttenteHandler : spr_Bouton_rejoindre  " + spr_Bouton_rejoindre);
		xInputSalon = 100;
		yInputSalon = 360;
		int inputSalonHostWidth = 200;
		int inputSalonHostHeight = 30;
		int inputSalonPortWidth = 100;
		inputSalonHost.setBounds(xInputSalon, yInputSalon, inputSalonHostWidth, inputSalonHostHeight);
		int xInputSalonPort = xInputSalon + inputSalonHostWidth + 10;
		inputSalonPort.setBounds(xInputSalonPort, yInputSalon, inputSalonPortWidth, inputSalonHostHeight);
		xBoutonRejoindreServeur = xInputSalonPort + inputSalonPortWidth + 10;
		yBoutonRejoindreServeur = yInputSalon - 2;
		xConnectMessage = xInputSalon + 20;
		yConnectMessage = yBoutonRejoindreServeur + 52;
		
		textFieldFont = new Font("TimesRoman", Font.BOLD, 24);
		nameFont = new Font("TimesRoman", Font.BOLD, 26);
		connextionMessageFont = new Font("TimesRoman", Font.BOLD, 20);
		nameColor = new Color(236, 232, 230);
		xAdresseServeurLocal = 100;
		yAdresseServeurLocal = 200;
		
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
		randomNumberPourNePasSAccepterSoiMeme = rand.nextLong();
		int minPortNumber = 10000;
		int maxPortNumber = 40000;
		int plagePorts = maxPortNumber - minPortNumber;
		int choosenPort;
		int maxTrialNb = 20;
		boolean success = false;
		for (int iTrial = 0; iTrial < maxTrialNb; iTrial++) {
			choosenPort = (rand.nextInt() % (plagePorts)) + minPortNumber;
			//ok ! choosenPort = 8887;
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
	
	private int currentState = 0; // flemme de créer une énumération pour ça.
	// 0 = en attente d'un client, pas de tentative de connexion à un serveur
	// 1 = tentative de connexion à un serveur : attente de la réponse
	
	private TCPClient tryToConnectToServer = null;
	private long time_firstConnection = 0;
	private long time_tempsAttenteMsMessageMaximale = 4 * 1000;
	
	public void loop() {
		currentGraphics = GraphicsHandler.getMainGraphics();
		
		// Dessin de mon numéro de salon (serveur TCP ouvert)
		// Dessin de l'adresse de salon à rejoindre
		currentGraphics.setFont(nameFont);
		currentGraphics.setColor(nameColor);
		
		
		// Affichage de l'adresse du salon local (mode serveur)
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
			int salonPort = localServer.getListeningPort();
			currentGraphics.drawString("Pour inviter quelqu'un, voici votre adresse de salon :", xAdresseServeurLocal, yAdresseServeurLocal);
			currentGraphics.drawString(salonAddr + " - " + salonPort, xAdresseServeurLocal + 20, yAdresseServeurLocal + 29);
		}
		
		currentGraphics.drawString("Pour rejoindre un salon local, entrez son adresse :", xInputSalon, yInputSalon - 18);

		currentGraphics.setFont(connextionMessageFont);
		currentGraphics.drawString(tryToConnect_currentMessage, xConnectMessage, yConnectMessage);
		currentGraphics.setFont(nameFont);
		currentGraphics.setColor(nameColor);
		
		// Tentative de connexion à un serveur
		if (currentState == 1) {
			if (tryToConnectToServer == null) {
				currentState = 0;
			} else {
				if (tryToConnectToServer.isStillActive() == false) {
					currentState = 0;
					tryToConnectToServer = null;
					tryToConnect_currentMessage = "Echec de la connexion, hôte invalide.";
				}
			}
		}
		
		
		if (currentState == 1) {
			boolean hasToDisconnect = false;
			NetBuffer messageFromServer = tryToConnectToServer.getNewMessage();
			if (messageFromServer != null) {
				String messageType = messageFromServer.readString();
				if (messageType.equals(verificationJeuDePylos)) { // le premier message reçu doit être celui qui indique que c'est bien un client "pylos en local"
					int receivedVersion = messageFromServer.readInt();
					if (pylosVersion != receivedVersion) {
						// Message d'erreur "pas la bonne version"
						tryToConnect_currentMessage = "ERREUR de la connexion : L'autre jeu semble ne pas avoir la même version.";
						hasToDisconnect = true;
					} else {
						// La connexion est en place, il ne reste plus qu'à lancer la partie ensemble !
						// -> lancer la partie
						estLeServeur = false;
						GraphicsHandler.roomGoTo_game(ModeDeJeu.RESEAU_LOCAL);
						tryToConnect_currentMessage = "C'est parti, on joue !";
					}
				} else {
					hasToDisconnect = true;
					tryToConnect_currentMessage = "Connecté, mais le serveur n'est probablement pas un serveur Pylos valide. Message de bienvenue invalide.";
				}
			}
			// Temps d'attente du premier message dépassé
			if (time_firstConnection + time_tempsAttenteMsMessageMaximale < System.currentTimeMillis()) {
				hasToDisconnect = true;
				tryToConnect_currentMessage = "Erreur : Le serveur a mis trop de temps à répondre.";
			}
			
			if (hasToDisconnect) {
				tryToConnectToServer.stop();
				tryToConnectToServer = null;
				currentState = 0;
			}
		}
		
		// Pas de connexion en attente, aucun client connecté au serveur
		if (currentState == 0) {
			boolean clicked = PImage.checkImageAsButton((currentState == 0), currentGraphics, spr_Bouton_rejoindre, xBoutonRejoindreServeur, yBoutonRejoindreServeur);
			
			// Cliquer sur le bouton pour rejoindre un serveur
			if (clicked) {
				String host = inputSalonHost.getText();
				int port = -1;
				String portAsString = inputSalonPort.getText();
				try {
					port = Integer.parseInt(portAsString, 10);
				} catch (Exception e) { }
				time_firstConnection = System.currentTimeMillis();
				
				tryToConnectToServer = new TCPClient(host, port);
				currentState = 1;
				tryToConnect_currentMessage = "Connexion en cours...";
			}
		}
		
		
		
		// Accepter un autre joueur (le serveur ne peut pas s'accepter lui-même, il est dans l'état "connexion" qui est incompatible avec l'état
		if (currentState == 0 && localServer != null) {
			TCPClient newClient = localServer.accept();
			
			
			// Pour que le serveur local soit accessible même s'il est floodé, je fais une compétition entre les clients : le premier qui arrive à se connecter et qui est valide est celui avec lequel je vais jouer.
			if (newClient != null) {
				
				// -> liste des clients valides
				
				
				// Si le client est ce serveur, impossible de rejoindre !
				if (newClient.getRemoteIP().equals(TCPServer.getLocalHostAddress_noNull(true)) && newClient.getRemotePort() == localServer.getListeningPort()) {
					newClient.stop();
					newClient = null;
					System.out.println("RoomReseauLocalAttenteHandler : il se connecte à lui-même !");
				} else {
					// Envoi du message de bienvenue
					NetBuffer messageToClient = new NetBuffer();
					messageToClient.writeString(verificationJeuDePylos);
					messageToClient.writeInt(pylosVersion);
					newClient.sendMessage(messageToClient);
					
					System.out.println("RoomReseauLocalAttenteHandler : newClient - co : " + newClient.isConnected() + "  active = " + newClient.isStillActive() + " IP = " + newClient.getRemoteIP() + " port = " +newClient.getRemotePort());
					//System.out.println("RoomReseauLocalAttenteHandler : newClient - GroDodo");
					/*newClient.stop();
					try {
						Thread.sleep(1000);
					} catch (Exception e) { }
					System.out.println("RoomReseauLocalAttenteHandler : newClient - co : " + newClient.isConnected() + "  active = " + newClient.isStillActive());
					*/
				}
			}
			
			// Boucler sur la liste des clients, accepter le permier client valide et jouer avec !
			
		}
		
		// Dessin du bouton "rejoindre"
		//PImage.drawImageAlpha(graphics, image, x, y, alpha);
		
		//Bouton_rejoindre
		
		
		
		// Affichage 
		
		
		// Actaliser le serveur local pour savoir si un client vient de s'y connecter
		// Appuyer sur le bouton "rejoindre un serveur" et essayer de s'y connecter, actualiser la réponse (tant que pas de réponse, impossible d'envoyer une nouvelle requête)
		// Si nouvelle réponse, bien vérifier que c'est un client Pylos de la bonne version, et lancer une partie en réseau local.
		
		// xBoutonRejoindreServeur
		
	}
}

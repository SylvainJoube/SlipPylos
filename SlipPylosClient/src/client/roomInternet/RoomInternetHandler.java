package client.roomInternet;

import java.awt.Graphics2D;
import java.awt.Image;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import client.outils.graphiques.GraphicsHandler;
import client.outils.graphiques.PImage;
import client.partie.graphique.RessourceManager;
import commun.partie.nonGraphique.ModeDeJeu;
import commun.partie.nonGraphique.TeamType;
import slip.network.buffers.NetBuffer;
import slip.network.tcp.TCPClient;

public class RoomInternetHandler {
	
	
	
	public static final RoomInternetHandler instance = new RoomInternetHandler();
	
	private static final String serverIP = "localhost";//"pylos.jeanpierre.moe";//"192.168.0.23";// // = localhost
	private static final int serverPort = 3393;
	private final String verificationServeurPylosStr = "Je suis un serveur PYLOS version 1";
	private final String verificationClientPylosStr = "Je suis un client PYLOS version 1";
	private final String verification_clientValideStr = "client pylos validé, version 1";
	
	private int etapeConnexion = 0; // 0 aucun état, 1 connexion au serveur en cours, 2 saisir ses identifiants, 3 recevoir confirmation de ses identifiants, 4 bien connecté au serveur
	private boolean connexionReussie = false; // <- affichage moche "identifiants incorrects" et retour à la salle de choix du type de partie.
	public TCPClient clientTCP = null;
	private long dateExpirationTimer; // pour arrêter d'attendre une réponse du serveur si celle-ci met trop de temps à arriver
	private final long tempsAttenteMaxConnexionInitiale = 4 * 1000;
	private final long tempsAttenteMaxPremierMessage = 2 * 1000;
	private boolean ilYAUnePartieEnCours = false;
	private String monNomDeCompte, monMotDePasse;
	public int nombreDeClientsConnectesAuServeur = 0;
	
	
	private void reinitialiserTout_instance(boolean resteDansRoomInternetOuPartieInternet) {
		
		
		if (resteDansRoomInternetOuPartieInternet == false) {
			/*if (ilYAUnePartieEnCours) {
				quitterPartieEnCours();
				try {
					Thread.sleep(1000); // le temps de signaler au serveur que la partie en cours est quittée
				} catch (Exception e) { }
			}*/ // sera déconnecté de toute façon
			
			etapeConnexion = 0; // traîté juste après, pour déconnecter le client s'il est connecté
		}
		
		// Si reste dans cette salle (menu internet ou partie internet)
		// (quitter la partie en cours s'il y en a une : fait tout seul), se déconnecter
		boolean joueurDejaAuthentifie = false;
		if (clientTCP != null) {
			// si une connexion est active, c'est peut-être que je faisais une partie : 
			if (clientTCP.isConnected()) {
				if ( etapeConnexion >= 4)
					joueurDejaAuthentifie = true;
			} else { // TCP non connecté mais non null
				joueurDejaAuthentifie = false;
			}
			// Déconnexion de force du serveur si le joueur n'est pas authentifié
			if (joueurDejaAuthentifie == false) {
				clientTCP.stop();
				clientTCP = null;
			}
		} else joueurDejaAuthentifie = false;
		
		if (joueurDejaAuthentifie == false) {
			etapeConnexion = 0; // joueur non authentifié, je recommence le processus
		}
		
	}
	
	public static void reinitialiserTout(boolean resteDansRoomInternetOuPartieInternet) {
		instance.reinitialiserTout_instance(resteDansRoomInternetOuPartieInternet);
	}
	
	/** Demande ses identifiants au joueur (de manière bloquante moche, mais rapide à faire, je manque de temps !)
	 *  
	 */
	public void demanderIdentifiants() {
		JPanel panel = new JPanel();
		JLabel affichageNomDeCompte = new JLabel("Nom de compte ->");
		JLabel affichageMotDePasse = new JLabel("Mot de passe ->");
		JTextField nomDeCompte = new JTextField(20);
		JPasswordField motDePasse = new JPasswordField(20);
		//motDePasse.setBounds(10, 10, 170, 2000); // 70 200
		panel.add(affichageNomDeCompte);
		panel.add(nomDeCompte);
		panel.add(affichageMotDePasse);
		panel.add(motDePasse);
		String[] options = new String[]{"Valider ", "Retour - Annuler"};
		int option = JOptionPane.showOptionDialog(null, panel, "Entrez vos identifiants internet PYLOS",
		                         JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE,
		                         null, options, options[0]);
		/*if(option == 0) // pressing OK button
		{
		    char[] password = motDePasse.getPassword();
		    System.out.println("Your password is: " + new String(password));
		}*/
		if(option == 0) // pressing OK button
		{
			
		    String nomDeCompteStr = nomDeCompte.getText();
		    char[] motDePasseChr = motDePasse.getPassword();
		    String motDePasseStr = new String(motDePasseChr);

			if (loop_connectionLost()) return;
		    
		    NetBuffer envoiAuServeur = new NetBuffer();
		    envoiAuServeur.writeString(nomDeCompteStr);
		    envoiAuServeur.writeString(motDePasseStr);
		    clientTCP.sendMessage(envoiAuServeur);
		    monNomDeCompte = nomDeCompteStr;
		    monMotDePasse = motDePasseStr;
		    // etapeConnexion = 5 fait dans loop_etape4()
		    //System.out.println("Your password is: " + new String(password));
		} else {
			etapeConnexion = 0;
			clientTCP = null;
			GraphicsHandler.roomGoTo_menuChoixTypePartie();
		}
		
		
		
		//String test1 = JOptionPane.showInputDialog("Please input mark for test 1: ");
	}
	
	/** Fonction exécutée depuis GraphicsHandler.loop()
	 */
	public void loop() {
		
		// 1) Se connecter au serveur, vérifier que la connexion est valide
		// 2) 
		if (etapeConnexion == 0) loop_etape0(); // Connexion initialie au serveur
		if (etapeConnexion == 1) loop_etape1(); // Attente de connexion au serveur + envoi du message pour valider le client quand connecté
		if (etapeConnexion == 2) loop_etape2(); // Connecté, attente du premier message
		if (etapeConnexion == 3) loop_etape3(); // Serveur validé, attente de validation du client
		if (etapeConnexion == 4) loop_etape4(); // Client et serveur pylos valides, demande de ses identifiants à l'utilisateur
		if (etapeConnexion == 5) loop_etape5(); // Attente de la réponse du serveur (identifiants valides ou non)
		if (etapeConnexion == 6) loop_etape6(); // Bien authentifié au serveur !
		
		if (etapeConnexion > 3) if (loop_connectionLost()) return;
		
		loopGraphique();
	}
	
	
	/** Tenter de se connecter au serveur
	 */
	private void loop_etape0() {
		clientTCP = new TCPClient(serverIP, serverPort);
		etapeConnexion = 1;
		dateExpirationTimer = System.currentTimeMillis() + tempsAttenteMaxConnexionInitiale;
	}
	private void loop_etape1() {
		if (clientTCP == null) {
			etapeConnexion = 0;
			return;
		}
		
		if (clientTCP.isStillActive() == false) { // = loop_connectionLost()
			etapeConnexion = 0;
			clientTCP = null;
			afficherMessageMoche("Echec de la connexion, serveur hôte invalide, introuvable.", JOptionPane.ERROR_MESSAGE);
			GraphicsHandler.roomGoTo_menuChoixTypePartie();
			return;
		}
		
		if (dateExpirationTimer < System.currentTimeMillis()) {
			clientTCP.stop();
			etapeConnexion = 0;
			clientTCP = null;
			afficherMessageMoche("Impossible de se connecter au serveur : temps d'attente de la réponse dépassé.", JOptionPane.ERROR_MESSAGE);
			GraphicsHandler.roomGoTo_menuChoixTypePartie();
			return;
		}
		
		if (clientTCP.isConnected()) { // connexion ok !
			etapeConnexion = 2;
			dateExpirationTimer = System.currentTimeMillis() + tempsAttenteMaxPremierMessage;
			NetBuffer sendMessage = new NetBuffer(); // envoi du message pour valider le client
			sendMessage.writeString(verificationClientPylosStr);
			clientTCP.sendMessage(sendMessage);
			return;
		}
		
		
	}
	
	/** Connecté, attente du premier message
	 */
	private void loop_etape2() {
		
		if (loop_connectionLost()) return;
		
		if (dateExpirationTimer < System.currentTimeMillis()) {
			clientTCP.stop();
			etapeConnexion = 0;
			clientTCP = null;
			afficherMessageMoche("Connecté au serveur, mais le serveur a mis trop de temps à répondre. Temps d'attente de la réponse dépassé.", JOptionPane.ERROR_MESSAGE);
			GraphicsHandler.roomGoTo_menuChoixTypePartie();
			return;
		}
		
		NetBuffer receivedMessage = clientTCP.getNewMessage();
		if (receivedMessage == null) return;
		
		// Vérification que le serveur est bien un serveur pylos
		String verificationServeur = receivedMessage.readStr();
		
		if (verificationServeurPylosStr.equals(verificationServeur)) { // serveur valide
			etapeConnexion = 3;
		} else { // mauvais message reçu
			clientTCP.stop();
			etapeConnexion = 0;
			clientTCP = null;
			afficherMessageMoche("Le serveur n'a pas répondu le bon message. Votre version de PYLOS est sans doute obsolète. Veuillez la télécharger du site officiel.", JOptionPane.ERROR_MESSAGE);
			GraphicsHandler.roomGoTo_menuChoixTypePartie();
			return;
		}
	}

	/** Serveur validé, attente de validation du client
	 */
	private void loop_etape3() {
		
		if (loop_connectionLost()) return;
		
		NetBuffer receivedMessage = clientTCP.getNewMessage();
		if (receivedMessage == null) return;
		
		String strClientValide = receivedMessage.readString();
		if (verification_clientValideStr.equals(strClientValide)) {
			etapeConnexion = 4;
		} else {
			clientTCP.stop();
			etapeConnexion = 0;
			clientTCP = null;
			afficherMessageMoche("Le serveur a refusé votre connexion, votre client PYLOS n'est probablement pas à jour.", JOptionPane.ERROR_MESSAGE);
			GraphicsHandler.roomGoTo_menuChoixTypePartie();
			return;
		}
	}
	
	/** Client et serveur pylos valides, demande de ses identifiants à l'utilisateur
	 */
	private void loop_etape4() {
		
		if (loop_connectionLost()) return;
		
		demanderIdentifiants();
		etapeConnexion = 5;
	}
	
	/** Attente de la réponse du serveur (identifiants valides ou non)
	 */
	private void loop_etape5() {
		
		if (loop_connectionLost()) return;
		
		NetBuffer receivedMessage = clientTCP.getNewMessage();
		if (receivedMessage == null) return;
		
		boolean succesConnexion = receivedMessage.readBool();
		
		if (succesConnexion) {
			nombreDeClientsConnectesAuServeur = receivedMessage.readInt();
			etapeConnexion = 6;
			afficherMessageMoche("Bienvenue, " + monNomDeCompte + " Actuellement, il y a " + nombreDeClientsConnectesAuServeur + " connecté(s) !", JOptionPane.INFORMATION_MESSAGE);
			
		} else {
			clientTCP.stop();
			etapeConnexion = 0;
			clientTCP = null;
			afficherMessageMoche("Echec de l'authentification : nom de compte ou mot de passe incorrect. Inscrivez-vous sur le site ! (récupérer un mot de passe par mail : pas encore fait malheureusement)", JOptionPane.ERROR_MESSAGE);
			GraphicsHandler.roomGoTo_menuChoixTypePartie();
			return;
		}
		
	}
	

	/** Bien authentifié au serveur !
	 */
	private void loop_etape6() {

		if (loop_connectionLost()) return;
		
		NetBuffer receivedMessage = clientTCP.getNewMessage();
		if (receivedMessage == null) return;
		
		int messageType = receivedMessage.readInt();
		
		// Rejoindre une partie :
		if (messageType == 90) {
			TeamType cEstLeTourDe = TeamType.fromInt(receivedMessage.readInt());
			TeamType equipeJoueurActuel = TeamType.fromInt(receivedMessage.readInt());
			// Noms des joueurs...
			System.out.println("loop_etape6 !");
			GraphicsHandler.roomGoTo_game(ModeDeJeu.INTERNET, cEstLeTourDe, equipeJoueurActuel);
		}
		
		
		
		
	}
	
	
	
	/** 
	 * @return true si le client n'est pas/plus connecté
	 */
	public boolean loop_connectionLost() {
		if (clientTCP == null) {
			etapeConnexion = 0;
			return true;
		}
		
		// Plus actif ou plus connecté : quitter l'interface internet
		if (clientTCP.isStillActive() == false || clientTCP.isConnected() == false) {
			etapeConnexion = 0;
			clientTCP = null;
			afficherMessageMoche("Perte de a connexion au serveur.", JOptionPane.ERROR_MESSAGE);
			GraphicsHandler.roomGoTo_menuChoixTypePartie();
			return true;
		}
		
		return false; // toujours connecté
	}
	
	public static void staticLoop() {
		instance.loop();
	}
	
	public static void quitterPartieEnCours() {
		instance.quitterPartieEnCours_instance();
	}

	// TODO 
	// TODO 
	// TODO 
	/** Quitte la partie en cours, s'il y en avait une en cours (sans se déconnecter du serveur)
	 */
	private void quitterPartieEnCours_instance() {
		
	}
	
	/** C'est moche, c'est bloquant, mais c'est plus rapide à faire qu'une belle interface intégrée en jeu.
	 *  @param message message à afficher
	 */
	private static void afficherMessageMoche(String message, int typeMessage) {
		//String test1 = 
		JOptionPane.showMessageDialog(null, message, "information - PylosOnline! (peut-être PylosOffline d'ailleurs...)", typeMessage);
	}
	
	private RoomInternetHandler_typePartieRecherchee partieRecherchee = RoomInternetHandler_typePartieRecherchee.AUCUNE;
	
	private void loopGraphique() {
		
		if (etapeConnexion < 6) return;
		
		Graphics2D currentGraphics = GraphicsHandler.getMainGraphics();
		
		int xButton = 100;
		int yButton = 200;
		int buttonTotalHeight = 60;

		boolean chercherPartieClassee = PImage.checkImageAsButton(true, currentGraphics, spr_InternetTrouverPartieClassee, xButton, yButton);
		// Afficher un texte si je suis incrit dans la liste de recherche de ce type de partie
		yButton += buttonTotalHeight;
		
		boolean chercherPartieNonClassee = PImage.checkImageAsButton(true, currentGraphics, spr_InternetTrouverPartieNonClassee, xButton, yButton);
		// Afficher un texte si je suis incrit dans la liste de recherche de ce type de partie
		yButton += buttonTotalHeight;
		
		boolean chercherPartieClasseeIA = PImage.checkImageAsButton(true, currentGraphics, spr_InternetTrouverPartieClasseeIA, xButton, yButton);
		// Afficher un texte si je suis incrit dans la liste de recherche de ce type de partie
		yButton += buttonTotalHeight;

		if (chercherPartieClassee) { // demande au serveur
			NetBuffer message = new NetBuffer();
			message.writeInt(10);
			clientTCP.sendMessage(message); // partieRecherchee sera mis à jour via loop_etape6()
		}

		if (chercherPartieNonClassee) { // demande au serveur
			NetBuffer message = new NetBuffer();
			message.writeInt(11);
			clientTCP.sendMessage(message); // partieRecherchee sera mis à jour via loop_etape6()
		}

		if (chercherPartieClasseeIA) { // demande au serveur
			NetBuffer message = new NetBuffer();
			message.writeInt(12);
			clientTCP.sendMessage(message); // partieRecherchee sera mis à jour via loop_etape6()
		}
		
		
		// Dessin des boutons, choix 
	}
	
	// Appelé depuis GraphicsHandler.roomGoTo_internet();
	private static Image spr_InternetTrouverPartieClassee;
	private static Image spr_InternetTrouverPartieNonClassee;
	private static Image spr_InternetTrouverPartieClasseeIA;
	public static void loadImages() {
		spr_InternetTrouverPartieClassee = RessourceManager.LoadImage("images/InternetTrouverPartieClassee.png");
		spr_InternetTrouverPartieNonClassee = RessourceManager.LoadImage("images/InternetTrouverPartieNonClassee.png");
		spr_InternetTrouverPartieClasseeIA = RessourceManager.LoadImage("images/InternetTrouverPartieClasseeIA.png");
		
	}
	
	
}

enum RoomInternetHandler_typePartieRecherchee {
	AUCUNE,
	CLASSEE_VS_JOUEUR,
	NON_CLASSEE_VS_JOUEUR,
	CLASSEE_VS_IA; // si aucun joueur
}







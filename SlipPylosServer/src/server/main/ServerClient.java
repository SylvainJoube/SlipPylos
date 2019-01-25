package server.main;

import java.util.ArrayList;

import commun.partie.nonGraphique.PylosPartie;
import commun.partie.nonGraphique.TeamType;
import server.database.DatabaseRequest;
import server.database.DatabaseUser;
import server.database.Modele;
import slip.network.buffers.NetBuffer;
import slip.network.tcp.TCPClient;

public class ServerClient {
	
	public TCPClient client;
	public int ID;
	public static int NextClientID = 1;
	public boolean deconnecterLeClientDesQuePossible = false;
	public int etapeConnexion_serveur = 0; // presque même chose que RoomInternetHandler.etapeConnexion
	public final int maxMessagesParClientEtParIteration = 5; // pour empêcher qu'un client flood le serveur
	
	GestionPartie gestionPartieActuelle = null;
	public TeamType monEquipe;
	
	public String monMotDePasse, monNomDeCompte;
	public int nombreVictoires, nombreDefaites, scoreTotal;
	
	public ServerClient(TCPClient arg_client) {
		client = arg_client;
		ID = NextClientID;
		NextClientID++;
	}
	
	public void envoyerMessageInitial() {
		if (doitEtreDeconnecte()) return;
		NetBuffer message = new NetBuffer();
		message.writeString(MainServer.verificationServeurPylosStr);
		client.sendMessage(message);
	}
	
	public boolean doitEtreDeconnecte() {
		if (client == null) return true;
		if (client.isConnected() == false) return true;
		if (deconnecterLeClientDesQuePossible) return true;
		return false;
	}
	
	public void disconnect() {
		EcouteClients.instance.supprimerDeToutesLesListesDeRecherche(this);
		if (client == null) return;
		client.stop();
		client = null;
	}
	
	public void loop() {
		// Ecouter les nouveaux messages
		if (etapeConnexion_serveur <= 3) {
			for (int iMessage = 0; iMessage < maxMessagesParClientEtParIteration; iMessage++) { // Pour écouter plusieurs messages à la fois, si besoin
				boolean nouveauMessageRecu = ecouteNouveauMessage();
				if (! nouveauMessageRecu) break; // plus de messages, inutile de continuer
			}
			
			// 
			if (etapeConnexion_serveur == 2) {
				recevoirReponseDemandeConnexionDB();
			}
		}
		
		if (etapeConnexion_serveur == 4) {
			loop_etape4();
		}
		
	}
	
	public boolean ecouteNouveauMessage() {
		if (doitEtreDeconnecte()) return false;
		
		NetBuffer message = client.getNewMessage();
		
		if (message == null) return false;
		
		// IMPORTANT : nombre en ordre décroissant pour que ça ne bug pas !!
		
		if (etapeConnexion_serveur == 3) nouveauMessage_etape3(message);
		
		//if (etapeConnexion_serveur == 2) nouveauMessage_etape2(message);
		// pas d'étape 2 : c'est l'attente de la base de données
		if (etapeConnexion_serveur == 1) nouveauMessage_etape1(message); // Attente des identifiants du joueur
		if (etapeConnexion_serveur == 0) nouveauMessage_etape0(message); // Vérification que c'est bien un client pylos
		
		return true;
	}
	
	private void nouveauMessage_etape0(NetBuffer message) {
		String verificationStrClient = message.readString();
		if (MainServer.verificationClientPylosStr.equals(verificationStrClient)) {
			etapeConnexion_serveur = 1;
			NetBuffer envoiMessage = new NetBuffer();
			envoiMessage.writeString("client pylos validé, version 1");
			client.sendMessage(envoiMessage);
		} else {
			deconnecterLeClientDesQuePossible = true;
			System.err.println("ServerClient.nouveauMessage_etape0 : doit déconnecter client, verificationClientPylosStr non égal à verificationStrClient = " + verificationStrClient);
		}
		System.out.println("SERVERR : ServerClient.nouveauMessage_etape0 : deconnecterLeClientDesQuePossible = " + deconnecterLeClientDesQuePossible);
	}
	

	private void nouveauMessage_etape1(NetBuffer message) {
		System.out.println("SERVERR : ServerClient.nouveauMessage_etape1 :");
		String nomDeCompte = message.readString();
		String motDePasse = message.readString();
		
		monNomDeCompte = nomDeCompte;
		monMotDePasse = motDePasse;
		// requête à la base de données d'Etienne
		envoyerRequeteConnexionDB(monNomDeCompte, monMotDePasse);
		etapeConnexion_serveur = 2;
		
		
	}
	
	private DatabaseRequest currentLoginRequest;
	//private boolean attendConfirmationIdentifiantsDB = false;
	private void envoyerRequeteConnexionDB(String nomDeCompte, String motDePasse) {
		//attendConfirmationIdentifiantsDB = true;
		
		// Envoi de la requête
		currentLoginRequest = new DatabaseRequest("on s en fiche de cette string");
		Modele.login(currentLoginRequest, nomDeCompte, motDePasse);
		
	}

	//private boolean nePasVerifierUtilisateursAvecDB = MainServer.toujoursVerifierUtilisateursAvecDB;
	
	private void recevoirReponseDemandeConnexionDB() {
		
		if (MainServer.toujoursVerifierUtilisateursAvecDB) { // si demander à la DB
			if (currentLoginRequest == null) return;
	
			if (currentLoginRequest.isCompleted() == false) return;
	
			if (currentLoginRequest.isErrored()) {
				System.out.println("ServerClient.recevoirReponseDemandeConnexionDB : ERREUR DB.  isErrored() == true");
	
				NetBuffer reponseAuClient = new NetBuffer();
				reponseAuClient.writeBool(false); // erreur DB ou login
				reponseAuClient.writeInt(EcouteClients.a1ServerClient.size());
				client.sendMessage(reponseAuClient);
				
				return;
			}
			
			// 
			Object receivedObject = currentLoginRequest.getResult();
			DatabaseUser receivedUser = (DatabaseUser) receivedObject;
		}
		
		// if (pas encore recu de réponse) return;
		// + timer
		
		// Si réponse OK, requête reçue et prête, je l'envoie au client
		// là, je fais comme si je venais de la recevoir (que la requête est "prête à être lue")
		
		NetBuffer reponseAuClient = new NetBuffer();
		//reponseAuClient.writeBool(true); // DB ok
		reponseAuClient.writeBool(true); // login OK
		reponseAuClient.writeInt(EcouteClients.a1ServerClient.size());
		client.sendMessage(reponseAuClient);
		
		nombreVictoires = 1;
		nombreDefaites = 1;
		scoreTotal = 10;
		
		etapeConnexion_serveur = 3; // écoute de ce que veut faire le client
		
		System.out.println("SERVERR : ServerClient.recevoirReponseDemandeConnexionDB go en jeu !");
		
		// SI réponse pas ok : deconnecterLeClientDesQuePossible = true; et je laisse etapeConnexion_serveur = 2;
		
	}
	
	/** Rechercher une partie. Le client est connecté et validé par le serveur, quand il est en étape 3.
	 *  @param message
	 */
	private void nouveauMessage_etape3(NetBuffer message) {
		System.out.println("SERVERR : ServerClient.nouveauMessage_etape3 :");
		int messageType = message.readInt();

		// Chercher une partie classée
		if (messageType == 10) {
			EcouteClients.instance.supprimerDeToutesLesListesDeRecherche(this);
			EcouteClients.instance.ajouterAListe_joueurClasse(this);
		}

		// Chercher une partie non classée
		if (messageType == 11) {
			EcouteClients.instance.supprimerDeToutesLesListesDeRecherche(this);
			EcouteClients.instance.ajouterAListe_joueurNonClasse(this);
			
		}

		// Chercher une partie classée contre une IA (la créer presque immédiatement, du coup)
		if (messageType == 12) {
			EcouteClients.instance.supprimerDeToutesLesListesDeRecherche(this); // Je retire le joueur des autres listes
			EcouteClients.instance.ajouterAListe_contreIA(this);
		}
		
		
	}
	
	/** Le joueur est en jeu !
	 * 
	 */
	private boolean loop_etape4() {
		if (gestionPartieActuelle == null) {
			etapeConnexion_serveur = 3; // écoute de ce que veut faire le client (choix d'une partie)
			return false;
		}
		PylosPartie partieActuelle = gestionPartieActuelle.partieActuelle;
		if (partieActuelle == null) {
			etapeConnexion_serveur = 3; // écoute de ce que veut faire le client (choix d'une partie)
			return false;
		}
		
		//System.out.println("SERVERR : ServerClient.loop_etape4 :");
		
		// + timer de déconnexion + timer pour passer un tour
		
		// Réception des actions que le joueur veut faire (idem réseau local sauf que le client ne modifie pas la partie avant d'avoir eu confirmation du serveur)
		
		NetBuffer message = client.getNewMessage();
		if (message == null) return false;
		
		int messageType = message.readInt();
		
		System.out.println("ServerClient.loop_etape4 : nouveau message, messageType = " + messageType);
		
		// Poser un pion de sa réserve
		// Si la demande est valide, je l'envoie aux clients de la partie
		if (messageType == 110) {
			int numeroDeTour = message.readInt();
			
			int equipeJoueurAsInt = message.readInt();
			TeamType equipeJoueurRecue = TeamType.fromInt(equipeJoueurAsInt);
			int hauteur = message.readInt();
			int xCell = message.readInt();
			int yCell = message.readInt();
			
			if (numeroDeTour != partieActuelle.numeroDeTour) {
				System.out.println("ServerClient.loop_etape4 : numeroDeTour != partieActuelle.numeroDeTour :  " + numeroDeTour + " != " + partieActuelle.numeroDeTour);
				return true; // peut-être un autre message en attente
			}
			
			if (equipeJoueurRecue != monEquipe) { // impossible de faire jouer l'adversaire !
				System.out.println("ServerClient.loop_etape4 : equipeJoueurRecue != monEquipe :  " + equipeJoueurRecue + " != " + monEquipe);
				return true; // peut-être un autre message en attente
			}
			
			boolean reussite = partieActuelle.poseUnPionDeSaReserve(equipeJoueurRecue, hauteur, xCell, yCell);
			// + gérer tourSuivant() à partir du serveur
			if (reussite) {
				partieActuelle.tourSuivant_automatique();
				NetBuffer envoi = new NetBuffer();
				envoi.writeInt(110);
				//envoi.writeInt(numeroDeTour);
				envoi.writeInt(equipeJoueurAsInt);
				envoi.writeInt(hauteur);
				envoi.writeInt(xCell);
				envoi.writeInt(yCell);
				NetBuffer variablesImportantesPartie = partieActuelle.ecrireVariablesPrincipales(); // prend donc en compte le changement de tour !
				envoi.writeByteArray(variablesImportantesPartie.convertToByteArray());
				gestionPartieActuelle.envoyerMessagesAuxJoueurs(envoi);
				System.out.println("ServerClient.loop_etape4 : réussite de la pose de pion !!");
			} else { // ne rien renvoyer si échec
				System.out.println("ServerClient.loop_etape4 : DEBUG : echec de la pose de pion !!");
			}
		}
		
		// Demande de déplacement de pion
		// Si la demande est valide, je l'envoie aux clients de la partie
		if (messageType == 111) {
			int numeroDeTour = message.readInt();
			int equipeJoueurAsInt = message.readInt();
			TeamType equipeJoueurRecue = TeamType.fromInt(equipeJoueurAsInt);
			int hauteur = message.readInt();
			int xCell = message.readInt();
			int yCell = message.readInt();
			int hauteur_init = message.readInt();
			int xCell_init = message.readInt();
			int yCell_init = message.readInt();
			
			if (numeroDeTour != partieActuelle.numeroDeTour) {
				System.out.println("ServerClient.loop_etape4 : numeroDeTour != partieActuelle.numeroDeTour :  " + numeroDeTour + " != " + partieActuelle.numeroDeTour);
				return true; // peut-être un autre message en attente
			}
			
			if (equipeJoueurRecue != monEquipe) { // impossible de faire jouer l'adversaire !
				System.out.println("ServerClient.loop_etape4 : equipeJoueurRecue != monEquipe :  " + equipeJoueurRecue + " != " + monEquipe);
				return true; // peut-être un autre message en attente
			}
			
			boolean reussite = partieActuelle.deplacerUnPion(equipeJoueurRecue, hauteur, xCell, yCell, hauteur_init, xCell_init, yCell_init);
			// + gérer tourSuivant() à partir du serveur
			if (reussite) {
				partieActuelle.tourSuivant_automatique();
				NetBuffer envoi = new NetBuffer();
				envoi.writeInt(111);
				//envoi.writeInt(numeroDeTour);
				envoi.writeInt(equipeJoueurAsInt);
				envoi.writeInt(hauteur);
				envoi.writeInt(xCell);
				envoi.writeInt(yCell);
				envoi.writeInt(hauteur_init);
				envoi.writeInt(xCell_init);
				envoi.writeInt(yCell_init);
				
				NetBuffer variablesImportantesPartie = partieActuelle.ecrireVariablesPrincipales(); // prend donc en compte le changement de tour !
				envoi.writeByteArray(variablesImportantesPartie.convertToByteArray());
				gestionPartieActuelle.envoyerMessagesAuxJoueurs(envoi);
				System.out.println("ServerClient.loop_etape4 : réussite du déplacement de pion !!");
			} else { // ne rien renvoyer si échec
				System.out.println("ServerClient.loop_etape4 : DEBUG : echec du déplacement de pion !!");
			}
		}
		

		// Reprendre un pion
		// Si la demande est valide, je l'envoie aux clients de la partie
		if (messageType == 112) {
			int numeroDeTour = message.readInt();
			int equipeJoueurAsInt = message.readInt();
			TeamType equipeJoueurRecue = TeamType.fromInt(equipeJoueurAsInt);
			int hauteur = message.readInt();
			int xCell = message.readInt();
			int yCell = message.readInt();

			if (numeroDeTour != partieActuelle.numeroDeTour) {
				System.out.println("ServerClient.loop_etape4 : numeroDeTour != partieActuelle.numeroDeTour :  " + numeroDeTour + " != " + partieActuelle.numeroDeTour);
				return true; // peut-être un autre message en attente
			}
			
			if (equipeJoueurRecue != monEquipe) { // impossible de faire jouer l'adversaire !
				System.out.println("ServerClient.loop_etape4 : equipeJoueurRecue != monEquipe :  " + equipeJoueurRecue + " != " + monEquipe);
				return true; // peut-être un autre message en attente
			}
			
			boolean reussite = partieActuelle.reprendUnPion(equipeJoueurRecue, hauteur, xCell, yCell, true);
			// + gérer tourSuivant() à partir du serveur
			if (reussite) {
				partieActuelle.tourSuivant_automatique();
				NetBuffer envoi = new NetBuffer();
				envoi.writeInt(112);
				//envoi.writeInt(numeroDeTour);
				envoi.writeInt(equipeJoueurAsInt);
				envoi.writeInt(hauteur);
				envoi.writeInt(xCell);
				envoi.writeInt(yCell);
				NetBuffer variablesImportantesPartie = partieActuelle.ecrireVariablesPrincipales(); // prend donc en compte le changement de tour !
				envoi.writeByteArray(variablesImportantesPartie.convertToByteArray());
				gestionPartieActuelle.envoyerMessagesAuxJoueurs(envoi);
				System.out.println("ServerClient.loop_etape4 : réussite de la reprise d'un pion !!");
			} else { // ne rien renvoyer si échec
				System.out.println("ServerClient.loop_etape4 : DEBUG : echec de la reprise d'un pion !!");
			}
		}
		
		
		
		
		
		
		/*
		ArrayList<NetBuffer> listeMessageATraiter = new ArrayList<NetBuffer>();
		gestionPartieActuelle.partieActuelle.depuisServeurInternet_doitFaireJouerIA = true;
		gestionPartieActuelle.partieActuelle.loop_recevoirDeTCPInternet(client, listeMessageATraiter); // loop_recevoirDuServeurInternet
		
		for (int iMessageATraiter = 0; iMessageATraiter < listeMessageATraiter.size(); iMessageATraiter++) {
			NetBuffer message = listeMessageATraiter.get(iMessageATraiter);
			// traiter le message du serveur : déconnexion autre joueur, fin de partie...
			
		}*/
		return true;
	}
	
	
	
	
}






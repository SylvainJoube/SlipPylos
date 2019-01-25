package server.main;

import java.util.ArrayList;

import slip.network.buffers.NetBuffer;
import slip.network.tcp.TCPClient;

public class ServerClient {
	
	public TCPClient client;
	public int ID;
	public static int NextClientID = 1;
	public boolean deconnecterLeClientDesQuePossible = false;
	public int etapeConnexion_serveur = 0; // presque même chose que RoomInternetHandler.etapeConnexion
	public final int maxMessagesParClientEtParIteration = 5; // pour empêcher qu'un client flood le serveur
	
	GestionPartie gestionPartieActuelle;
	
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
	
	//private boolean attendConfirmationIdentifiantsDB = false;
	private void envoyerRequeteConnexionDB(String nomDeCompte, String motDePasse) {
		//attendConfirmationIdentifiantsDB = true;
	}
	
	private void recevoirReponseDemandeConnexionDB() {
		
		// if (pas encore recu de réponse) return;
		// + timer
		
		// Si réponse OK, requête reçue et prête, je l'envoie au client
		// là, je fais comme si je venais de la recevoir (que la requête est "prête à être lue")
		
		NetBuffer reponseAuClient = new NetBuffer();
		reponseAuClient.writeBool(true);
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
	 *  @param message
	 */
	private void loop_etape4() {
		//System.out.println("SERVERR : ServerClient.loop_etape4 :");
		
		
		ArrayList<NetBuffer> listeMessageATraiter = new ArrayList<NetBuffer>();
		gestionPartieActuelle.partieActuelle.depuisServeurInternet_doitFaireJouerIA = true;
		gestionPartieActuelle.partieActuelle.loop_recevoirDeTCPInternet(client, listeMessageATraiter); // loop_recevoirDuServeurInternet
		
		for (int iMessageATraiter = 0; iMessageATraiter < listeMessageATraiter.size(); iMessageATraiter++) {
			NetBuffer message = listeMessageATraiter.get(iMessageATraiter);
			// traiter le message du serveur : déconnexion autre joueur, fin de partie...
			
		}
			
	}
	
	
}






package server.main;

import java.util.ArrayList;

import slip.network.tcp.TCPClient;
import slip.network.tcp.TCPServer;

/**
 * MainServerThread est supposé exister, et 
 *
 */
public class EcouteClients {
	
	
	private TCPServer serveurTCP;
	
	public static EcouteClients instance = null; // mis à jour depuis MainServerThread
	
	public static ArrayList<ServerClient> a1ServerClient = new ArrayList<ServerClient>();
	
	public EcouteClients(TCPServer arg_serveurTCP) {
		if (EcouteClients.instance != null) {
			System.err.println("ERREUR EcouteClients constructeur : une instance existe déjà.");
		}
		serveurTCP = arg_serveurTCP;
		EcouteClients.instance = this;
	}
	
	/** Boucle d'écoute des clients
	 */
	public void loop() {
		if (serveurTCP == null) return;
		if (serveurTCP.isListening() == false) return;
		
		accepterNouveauxClients();
		supprimerClientsADeconnecter();
		//ecouterMessagesClients();
		itererLesClients_loop();
		loopListesRecherche(); // mettre en relation dans une partie les clients qui recherent une partie
		
		
		
	}
	
	private void accepterNouveauxClients() {
		
		TCPClient nouveauClient = serveurTCP.accept();
		if (nouveauClient != null) {
			ServerClient serverClient = new ServerClient(nouveauClient);
			serverClient.envoyerMessageInitial();
			a1ServerClient.add(serverClient);
		}
	}
	
	private void supprimerClientsADeconnecter() {
		int iClient = 0;
		while (iClient < a1ServerClient.size()) {
			ServerClient serverClient = a1ServerClient.get(iClient);
			if (serverClient.doitEtreDeconnecte()) {
				serverClient.disconnect();
				a1ServerClient.remove(iClient);
				
			} else {
				iClient++;
			}
		}
	}
	
	/*private void ecouterMessagesClients() {
		for (ServerClient client : a1ServerClient) {
			// Pour écouter plusieurs messages à la fois, si besoin
			for (int iMessage = 0; iMessage < maxMessagesParClientEtParIteration; iMessage++) {
				boolean nouveauMessageRecu = client.ecouteNouveauMessage();
				if (! nouveauMessageRecu) break; // plus de messages, inutile de continuer
			}
		}
	}*/
	
	private void itererLesClients_loop() {
		for (ServerClient client : a1ServerClient) {
			client.loop(); // boucle du client
		}
	}

	public static ArrayList<ServerClient> a1ClientRecherche_partieVsJoueurClassee = new ArrayList<ServerClient>();
	public static ArrayList<ServerClient> a1ClientRecherche_partieVsJoueurNonClassee = new ArrayList<ServerClient>();
	public static ArrayList<ServerClient> a1ClientRecherche_partieVsIA = new ArrayList<ServerClient>();
	
	public void supprimerDeToutesLesListesDeRecherche(ServerClient servClient) {
		a1ClientRecherche_partieVsJoueurClassee.remove(servClient);
		a1ClientRecherche_partieVsJoueurNonClassee.remove(servClient);
		a1ClientRecherche_partieVsIA.remove(servClient);
	}

	public void ajouterAListe_joueurClasse(ServerClient servClient) {
		// + initialisation timer
		a1ClientRecherche_partieVsJoueurClassee.add(servClient);
	}
	
	public void ajouterAListe_joueurNonClasse(ServerClient servClient) {
		// + initialisation timer
		a1ClientRecherche_partieVsJoueurNonClassee.add(servClient);
	}
	
	public void ajouterAListe_contreIA(ServerClient servClient) {
		// + initialisation timer
		a1ClientRecherche_partieVsIA.add(servClient);
	}
	
	// Si deux joueurs recherchent une partie, je les met en relation
	public void loopListesRecherche() {
		
		// 1) Je traîte les demandes de jeu contre une IA
		// /!\ passage du client de etape3 à etape4 => supprimerDeToutesLesListesDeRecherche()
		
		
		while (a1ClientRecherche_partieVsIA.size() > 0) {
			// Lancer une partie avec une IA !
			
			ServerClient servClient = a1ClientRecherche_partieVsIA.get(0);
			a1ClientRecherche_partieVsIA.remove(0);
			// S'il n'y a pas trop de parties contre une IA lancées, j'en lance une (condition à implémenter plus tard)
			GestionPartie nouvellePartie = new GestionPartie(TypePartie.CLASSEE_VS_IA);
			nouvellePartie.debuterPartieVsIA(servClient);
			servClient.etapeConnexion_serveur = 4; // dans une partie
			servClient.gestionPartieActuelle = nouvellePartie;
			
			
			//iRecherche++;
		}

		
		while (a1ClientRecherche_partieVsJoueurClassee.size() >= 2) {
			// Lancer une partie avec une IA !

			ServerClient joueur1 = a1ClientRecherche_partieVsJoueurClassee.get(0);
			ServerClient joueur2 = a1ClientRecherche_partieVsJoueurClassee.get(1);
			a1ClientRecherche_partieVsJoueurClassee.remove(0);
			a1ClientRecherche_partieVsJoueurClassee.remove(0);
			
			GestionPartie nouvellePartie = new GestionPartie(TypePartie.CLASSEE_VS_JOUEUR);
			nouvellePartie.debuterPartieVsJoueur_classe(joueur1, joueur2);
			joueur1.etapeConnexion_serveur = 4; // dans une partie
			joueur2.etapeConnexion_serveur = 4; // dans une partie
			joueur1.gestionPartieActuelle = nouvellePartie;
			joueur2.gestionPartieActuelle = nouvellePartie;
			
		}

		
		while (a1ClientRecherche_partieVsJoueurNonClassee.size() >= 2) {
			// Lancer une partie avec une IA !

			ServerClient joueur1 = a1ClientRecherche_partieVsJoueurNonClassee.get(0);
			ServerClient joueur2 = a1ClientRecherche_partieVsJoueurNonClassee.get(1);
			a1ClientRecherche_partieVsJoueurNonClassee.remove(0);
			a1ClientRecherche_partieVsJoueurNonClassee.remove(0);
			
			GestionPartie nouvellePartie = new GestionPartie(TypePartie.NON_CLASSEE_VS_JOUEUR);
			nouvellePartie.debuterPartieVsJoueur_nonClasse(joueur1, joueur2);
			joueur1.etapeConnexion_serveur = 4; // dans une partie
			joueur2.etapeConnexion_serveur = 4; // dans une partie
			joueur1.gestionPartieActuelle = nouvellePartie;
			joueur2.gestionPartieActuelle = nouvellePartie;
			
		}
		
		
	}
	
}














package server.main;

import commun.partie.nonGraphique.IA_v40;
import server.database.Modele;

public class MainServer {
	
	
	// Choses à modifier, pour modifier les accès à la base de données (utiliser ou non + adresse locale/distante) : -------
		
		// Utiliser la base de données ou non :
		// -> si toujoursVerifierUtilisateursAvecDB = false,
		//    tout les utilisateurs qui se connectent au serveur sont validés, quel que soit le nom de compte/mot de passe saisi
		// -> si toujoursVerifierUtilisateursAvecDB = true,
		//    seuls les utilisateurs enregistrés dans la base de données sont acceptés.
		public static final boolean utiliserDB = true; // true = on a accès à la DB, false = on autorise tout le monde (toujoursVerifierUtilisateursAvecDB)
		
		// Adresses de la base de données (voir à adresseBaseDeDonnees, juste en dessous pour plus d'infos)
		public static final String adresseBaseDeDonnees_distante = "https://pylos.jeanpierre.moe";
		public static final String adresseBaseDeDonnees_locale = "http://localhost"; // ce n'est plus du HTTPS, en local, juste du HTTP (sur la même machine)
		
		// Accès à la base de données :
		// adresseBaseDeDonnees est l'adresse à laquelle il est possible de joindre la base de données.
		// -> utiliser adresseBaseDeDonnees_locale   si la base de données à utilsier est sur la même machine,
		// -> utilsier adresseBaseDeDonnees_distante pour accéder à la base de données associée au nom de domaine "pylos.jeanpierre.moe", configuré par Etienne.
		public static final String adresseBaseDeDonnees = adresseBaseDeDonnees_distante;
		
	// ------- fin des champs à modifier pour faire marcher le serveur et la base de données en local/distant
	
	
	// Port d'écoute du serveur de jeu (serveur auquel se connecte le client PYLOS)
	public static final int serverPort = 3393; // <- port à modifier si besoin
	
	
	
	// Codes utilisés pour bien vérifier que le socket auquel on s'adresse est un serveur / client PYLOS : (modifier, et tester 
	public static final String verificationServeurPylosStr = "Je suis un serveur PYLOS version 1";
	public static final String verificationClientPylosStr = "Je suis un client PYLOS version 1";
	public static final String verification_clientValideStr = "client pylos validé, version 1";
	
	
	
	public static void main(String[] args) {
		System.out.println("Serveur lancé, port " + serverPort + "   hote = " + Modele.host);
		
		IA_v40.estServeurInternet = true;
		
		// Accepter de nouveaux clients, recevoir et traiter les messages des clients. 1 seul thread (+ simple)
		// 1) Nouvelle connexion, j'ajoute à ma liste, j'attends confirmation que c'est bien un client pylos, j'envoie ma phrase de "je suis un serveur Pylos"
		// 2) Réception du message de confirmation par le client, vérification que c'est bien un client pylos
		
		// Non fait par manque de temps (pour l'instant) : RSA et AES
		// 3) Direcement après validation du client (pylos), envoie de la clef publique du serveur
		// 4) Réception de la clef AES du client, encodée via la clef publique du serveur
		// 5) Echanges encodés via la clef AES du client durant toute la durée de l'échange. (AES beaucoup + rapide que RSA, d'où l'utilisation d'AES)
		// -> Vérification de l'intégrité des messages via sha256
		
		
		MainServerThread mainThread = new MainServerThread(serverPort);
		new Thread(mainThread).start();
		
		
	}
}

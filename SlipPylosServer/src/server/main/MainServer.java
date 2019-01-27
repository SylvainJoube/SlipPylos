package server.main;

import commun.partie.nonGraphique.IA_v40;
import server.database.Modele;

public class MainServer {
	
	
	
	// Codes utilisés pour bien vérifier que le socket auquel on s'adresse est un serveur / client PYLOS : (modifier, et tester 
	public static final String verificationServeurPylosStr = "Je suis un serveur PYLOS version 1";
	public static final String verificationClientPylosStr = "Je suis un client PYLOS version 1";
	public static final String verification_clientValideStr = "client pylos validé, version 1";
	
	public static final int serverPort = 3393; // <- port à modifier si besoin
	
	public static final boolean toujoursVerifierUtilisateursAvecDB = false; // true = on a accès à la DB, false = on autorise tout le monde
	
	
	public static void main(String[] args) {
		System.out.println("Serveur lancé, port " + serverPort + "   hote = " + Modele.host);
		
		IA_v40.estServeurInternet = true;
		
		// Accepter de nouveaux clients, recevoir et traiter les messages des clients. 1 seul thread (+ simple)
		// 1) Nouvelle connexion, j'ajoute à ma liste, j'attends confirmation que c'est bien un client pylos, j'envoie ma phrase de "je suis un serveur Pylos"
		// 2) Réception du message de confirmation par le client, vérification que c'est bien un client pylos
		// 3) Direcement après validation du client (pylos), envoie de la clef publique du serveur
		// 4) Réception de la clef AES du client, encodée via la clef publique du serveur
		// 5) Echanges encodés via la clef AES du client durant toute la durée de l'échange.
		// -> Vérification de l'intégrité des messages : 
		
		// RSA et AES plus tard, déjà :
		
		MainServerThread mainThread = new MainServerThread(serverPort);
		new Thread(mainThread).start();
		
		
	}
}

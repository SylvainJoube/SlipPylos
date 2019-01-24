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
	
	public static ArrayList<ServerClient> a1ServerClient = new ArrayList<ServerClient>();
	
	public EcouteClients(TCPServer arg_serveurTCP) {
		serveurTCP = arg_serveurTCP;
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
	
}














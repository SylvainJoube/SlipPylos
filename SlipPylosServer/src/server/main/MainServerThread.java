package server.main;

import java.util.concurrent.atomic.AtomicBoolean;

import server.database.Modele;
import slip.network.tcp.TCPServer;

/**
 * Thread principal, pour ne pas bloquer la console !
 *
 */
public class MainServerThread implements Runnable {

	public static TCPServer serveurTCP;
	public AtomicBoolean stillActive = new AtomicBoolean(true);
	public AtomicBoolean hasToStop = new AtomicBoolean(false);
	public static int serverPort;
	
	public MainServerThread(int arg_serverPort) {
		serverPort = arg_serverPort;
	}
	
	
	@Override
	public void run() {
		
		serveurTCP = new TCPServer(serverPort);
		if (serveurTCP.isListening() == false) {
			System.err.println("ERREUR : MainServerThread.run : port déjà pris, impossible de lancer le serveur.");
			stillActive.set(false);
			return;
		}
		System.out.println("MainServerThread.run : Serveur lancé, port " + serverPort +  "   hote = " + Modele.host);
		
		EcouteClients ecouterLesClients = new EcouteClients(serveurTCP);
		GestionDesParties gestionDesParties = new GestionDesParties();
		
		while (hasToStop.get() == false) {
			
			ecouterLesClients.loop();
			gestionDesParties.loopGestionParties();
			
			try {
				Thread.sleep(4);
			} catch (Exception e) {
				// ne rien faire ou interrompre le thread, au choix.
				
			}
		}
		
		stillActive.set(false);
	}
	
	
	
	
}

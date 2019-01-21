package slip.network.tcp;

import java.net.ServerSocket;
import java.util.ArrayList;

import slip.network.buffers.NetBuffer;

/** Principe du TCPServer :
 * Un thread est créé pour accepter de nouveaux clients.
 * S'il y a un nouveau client (tcp de java) accepté par un TCPServer, il est mis en attente dans le buffer du serveur.
 * Un TCPClient est retourné lors de l'appel à TCPServer.accept(), ou null si aucun client n'est en attente d'acceptation.
 */
public class TCPServer {
	
	private int listenOnPort;
	private boolean isActuallyListening = false;
	private ServerSocket servSock = null;
	
	private Object clientList_notYetAccepted_Lock = new Object();
	private ArrayList<TCPClient> clientList_notYetAccepted = new ArrayList<TCPClient>(); // Liste des clients connectés mais non explicitement acceptés
	private ArrayList<TCPClient> clientList_accepted = new ArrayList<TCPClient>(); // Liste des clients acceptés via TCPServer.acceptNewClient()
	
	private TCPServerAcceptThread acceptThread;
	
	/** Ecriture d'un message d'information (log)
	 * Formatté en "date + message"
	 */
	private void log(String message) {
		String dateStr = java.time.LocalDate.now().toString()
		               + java.time.LocalTime.now().toString();
		System.out.println(dateStr + " : " + message);
	}
	
	/** Fonction privée pour actualiser l'état "à l'écoute sur le port"
	 */
	private void refreshListeningState() {
		if (acceptThread == null) {
			isActuallyListening = false;
			return;
		}
		if (!acceptThread.isStillActive()) {
			isActuallyListening = false;
			return;
		}
	}
	
	/** Constructeur (bloquant en attendant que le serveur s'ouvre (pas long))
	 * Utiliser à la suite TCPServer.isListening() pour savoir si le port a bien été ouvert.
	 * @param arg_listenOnPort le port à écouter
	 */
	public TCPServer(int arg_listenOnPort) {
		listenOnPort = arg_listenOnPort;
		servSock = null;
		try {
			servSock = new ServerSocket(listenOnPort);
			isActuallyListening = true;
			
			acceptThread = new TCPServerAcceptThread(this);
			new Thread(acceptThread).start();
		} catch (Exception e) {
			isActuallyListening = false;
		}
	}
	/** Fermer le serveur
	 */
	public void close() {
		if (acceptThread != null) try {
			acceptThread.close();
			servSock.close();
		} catch (Exception e) { }
	}
	/** Regarde si le serveur écoute bien le port défini
	 * @return vrai si le serveur écoute, false sinon
	 */
	public boolean isListening() {
		refreshListeningState();
		return isActuallyListening;
	}
	
	/** Récupérer le socket (java et bloquant) du serveur
	 * @return
	 */
	public ServerSocket getServSock() {
		return servSock;
	}
	
	/** Accepte un nouveau client, de manière thread-safe.
	 * Lors de la connexion TCP d'un client, il est automatiquement ajouté au serveur, et est mis en attente d'acceptation.
	 * Appeler cette méthode pour accepter un nouveau client et pouvoir communiquer avec lui (par la référence à TCPClient)
	 * @return un des clients en attente d'acceptation
	 */
	public TCPClient acceptNewClient() {
		synchronized (clientList_notYetAccepted_Lock) {
			if (clientList_notYetAccepted.size() == 0) return null; // Aucun client en attente
			TCPClient client = clientList_notYetAccepted.get(0);
			clientList_notYetAccepted.remove(0);
			clientList_accepted.add(client);
			return client;
		}
	}
	
	/** Identique à acceptNewClient();
	 * @return null si aucun client en attente, un TCPClient si un nouveau client s'est connecté depuis le dernier appel à cette fonction.
	 */
	public TCPClient accept() {
		return acceptNewClient();
	}
	
	/** Stopper le serveur, déconnecter tous les clients, arrêter tous les threads. VSNS
	 */
	public void stop() {
		if (acceptThread != null)
			acceptThread.close();
		synchronized(clientList_notYetAccepted_Lock) {
			for (int iClient = 0; iClient < clientList_accepted.size(); iClient++) {
				clientList_accepted.get(iClient).stop();
			}
			for (int iClient = 0; iClient < clientList_notYetAccepted.size(); iClient++) {
				clientList_notYetAccepted.get(iClient).stop();
			}
			clientList_accepted.clear();
			clientList_notYetAccepted.clear();
		}
		//System.out.println("Server STOPPED.");
	}
	
	
	/** Ajout d'un nouveau client au serveur depuis le thread TCPServerAcceptThread (acceptThread)
	 * @param newClient nouveau
	 */
	public void addClientFromAcceptThread(TCPClient newClient) {
		synchronized (clientList_notYetAccepted_Lock) {
			clientList_notYetAccepted.add(newClient);
			//System.out.println("Ajout d'un cient au serveur !");
		}
	}
	
	
	
}

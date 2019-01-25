package server.main;

import java.util.Random;

import commun.partie.nonGraphique.IA_v40;
import commun.partie.nonGraphique.ModeDeJeu;
import commun.partie.nonGraphique.PylosGridArray;
import commun.partie.nonGraphique.PylosPartie;
import commun.partie.nonGraphique.PylosPartieVariante;
import commun.partie.nonGraphique.TeamType;
import slip.network.buffers.NetBuffer;
import slip.network.tcp.TCPClient;

public class GestionPartie {
	
	public TypePartie typeDePartie = TypePartie.AUCUNE;
	public PylosPartie partieActuelle = null; // même PylosPartie que pour le client
	private static Random myStaticRandom = new Random();
	
	private ServerClient joueur1, joueur2; // joueur1 toujours valide, joueur2 uniquement si pas contre une IA
	
	public boolean terminee = false;
	
	
	
	public GestionPartie(TypePartie arg_typeDePartie) {
		typeDePartie = arg_typeDePartie;
	}
	
	public void debuterPartieVsIA(ServerClient joueur) {
		// Choix aléatoire de la première équipe qui doit jouer
		boolean leBlancJoue = myStaticRandom.nextBoolean();
		TeamType equipeQuiJoue;
		if (leBlancJoue) equipeQuiJoue = TeamType.BLANC; else equipeQuiJoue = TeamType.NOIR;
		partieActuelle = new PylosPartie(ModeDeJeu.INTERNET, equipeQuiJoue, equipeQuiJoue, null, true);
		partieActuelle.equipeJoueur = equipeQuiJoue;
		partieActuelle.equipeIA = partieActuelle.equipeJoueur.equipeOpposee();
		
		
		typeDePartie = TypePartie.CLASSEE_VS_IA;
		
		
		joueur1 = joueur;
		joueur2 = null; // c'est l'IA !!
		
		joueur.monEquipe = partieActuelle.equipeJoueur;
		
		GestionDesParties.instance.a1Partie.add(this); // sera actualisée et gérée par GestionDesParties
		
		NetBuffer messageEnvoiClient = new NetBuffer();
		messageEnvoiClient.writeInt(90);
		messageEnvoiClient.writeInt(equipeQuiJoue.asInt);
		messageEnvoiClient.writeInt(equipeQuiJoue.asInt);
		joueur.client.sendMessage(messageEnvoiClient);
		System.out.println("GestionPartie.debuterPartieVsIA : débuter partie VS IA !");
		
	}
	
	public void loop() {
		if (terminee == false) return;
		
		if (typeDePartie == TypePartie.CLASSEE_VS_IA) {
			if (partieActuelle.tourDe == partieActuelle.equipeIA) { // faire jouer l'IA, si elle peut
				IA_v40.serveurInternetUniquement_listeCoupIA.clear();
				partieActuelle.faireJouerIA(); // peut jouer plusieurs fois !!
				//ici
				
				// J'envoie l'état complet de la partie, en bourrin, par manque de temps
				
				
				
				// J'envoie les coups joués par l'IA, et l'état de la partie (non fait par manque de temps, go méthode bourrine)
				/*
				for (int iCoup = 0; iCoup < IA_v40.serveurInternetUniquement_listeCoupIA.size(); iCoup++) {
					IA_v40_coup coup = serveurInternetUniquement_listeCoupIA.get(iCoup);
					
					
					
				}*/
				
				
			}
		}
		
		
		
		
	}
	
	public void envoyerMessagesAuxJoueurs(NetBuffer message) {
		if (joueur1 != null) {
			System.out.println("GestionPartie.envoyerMessagesAuxJoueurs : joueur1 != null");
			joueur1.client.sendMessage(message);
		} else
			System.out.println("GestionPartie.envoyerMessagesAuxJoueurs : joueur1 == null");
		
		if (joueur2 != null) {
			System.out.println("GestionPartie.envoyerMessagesAuxJoueurs : joueur2 != null");
			joueur2.client.sendMessage(message);
		} else
			System.out.println("GestionPartie.envoyerMessagesAuxJoueurs : joueur2 == null");
	}
	
	/** Récupérer qui joue, qui peut jouer, quel est ne numéro du tour actuel. (resynchronisation totale pour les clients)
	 *  @return
	 */
	/*
	public NetBuffer recupererVariablesPrincipalesPartie() {
		if (partieActuelle == null) return null;
		
		NetBuffer result = new NetBuffer();
		result.writeInt(partieActuelle.nbJetonsBlanc);
		result.writeInt(partieActuelle.nbJetonsNoir);
		result.writeInt(partieActuelle.tourDe.asInt);
		result.writeInt(partieActuelle.varianteDuJeu.asInt);
		result.writeInt(partieActuelle.numeroDeTour);
		result.writeBool(partieActuelle.joueurAJoueUnPion);
		result.writeInt(partieActuelle.peutReprendrePionsNb);
		return result;
		
	}*/
	
}








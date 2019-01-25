package server.main;

import java.util.Random;

import commun.partie.nonGraphique.ModeDeJeu;
import commun.partie.nonGraphique.PylosPartie;
import commun.partie.nonGraphique.TeamType;
import slip.network.buffers.NetBuffer;

public class GestionPartie {
	
	public TypePartie typeDePartie = TypePartie.AUCUNE;
	public PylosPartie partieActuelle;
	private static Random myStaticRandom = new Random();
	
	
	public GestionPartie(TypePartie arg_typeDePartie) {
		typeDePartie = arg_typeDePartie;
	}
	
	public void debuterPartieVsIA(ServerClient joueur) {
		// Choix aléatoire de la première équipe qui doit jouer
		boolean leBlancJoue = myStaticRandom.nextBoolean();
		TeamType equipeQuiJoue;
		if (leBlancJoue) equipeQuiJoue = TeamType.BLANC; else equipeQuiJoue = TeamType.NOIR;
		partieActuelle = new PylosPartie(ModeDeJeu.INTERNET, equipeQuiJoue, equipeQuiJoue);
		
		GestionDesParties.instance.a1Partie.add(this); // sera actualisée et gérée par GestionDesParties
		
		NetBuffer messageEnvoiClient = new NetBuffer();
		messageEnvoiClient.writeInt(90);
		messageEnvoiClient.writeInt(equipeQuiJoue.asInt);
		messageEnvoiClient.writeInt(equipeQuiJoue.asInt);
		joueur.client.sendMessage(messageEnvoiClient);
		System.out.println("GestionPartie.debuterPartieVsIA : débuter partie VS IA !");
		
	}
	
	
}

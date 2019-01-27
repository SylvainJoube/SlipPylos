package server.main;

import java.util.ArrayList;
import java.util.Random;

import commun.partie.nonGraphique.IA_v40;
import commun.partie.nonGraphique.ModeDeJeu;
import commun.partie.nonGraphique.PylosGridArray;
import commun.partie.nonGraphique.PylosPartie;
import commun.partie.nonGraphique.PylosPartieVariante;
import commun.partie.nonGraphique.PylosPartie_actionSimple;
import commun.partie.nonGraphique.TeamType;
import slip.network.buffers.NetBuffer;
import slip.network.tcp.TCPClient;

public class GestionPartie {
	
	public TypePartie typeDePartie = TypePartie.AUCUNE;
	public PylosPartie partieActuelle = null; // même PylosPartie que pour le client
	private static Random myStaticRandom = new Random();
	private int monID;
	
	private static int globalPartieID_next = 1;
	
	private ServerClient joueur1, joueur2; // joueur1 toujours valide, joueur2 uniquement si pas contre une IA
	
	public boolean terminee = false;
	
	public long canDoOtherThingsWhenTime = 0;
	public boolean doitEnvoyerVariablesPartieDesQuePossible = false;
	
	private ArrayList<PylosPartie_actionSimple> listeActionsSimplesEnAttenteDEnvoi = new ArrayList<PylosPartie_actionSimple>();
	
	private static int timerMsIA = 300;
	
	
	public GestionPartie(TypePartie arg_typeDePartie) {
		typeDePartie = arg_typeDePartie;
		monID = globalPartieID_next;
		globalPartieID_next++;
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
		envoyerMessagesAuxJoueurs(messageEnvoiClient);
		//joueur.client.sendMessage(messageEnvoiClient);
		System.out.println("GestionPartie.debuterPartieVsIA : débuter partie VS IA !");
		
	}

	public void debuterPartieVsJoueur_classe(ServerClient arg_joueur1, ServerClient arg_joueur2) {
		// Choix aléatoire de la première équipe qui doit jouer
		boolean leBlancJoue = myStaticRandom.nextBoolean();
		TeamType equipeQuiJoue;
		if (leBlancJoue) equipeQuiJoue = TeamType.BLANC; else equipeQuiJoue = TeamType.NOIR;
		partieActuelle = new PylosPartie(ModeDeJeu.INTERNET, equipeQuiJoue, equipeQuiJoue, null, true);
		partieActuelle.equipeJoueur = equipeQuiJoue;
		partieActuelle.equipeIA = TeamType.AUCUNE;
		
		typeDePartie = TypePartie.CLASSEE_VS_JOUEUR;
		
		joueur1 = arg_joueur1;
		joueur2 = arg_joueur2;

		joueur1.monEquipe = partieActuelle.equipeJoueur;
		joueur2.monEquipe = joueur1.monEquipe.equipeOpposee();
		
		GestionDesParties.instance.a1Partie.add(this); // sera actualisée et gérée par GestionDesParties

		NetBuffer messageEnvoiJoueur1 = new NetBuffer();
		messageEnvoiJoueur1.writeInt(90);
		messageEnvoiJoueur1.writeInt(equipeQuiJoue.asInt);
		messageEnvoiJoueur1.writeInt(joueur1.monEquipe.asInt);
		joueur1.client.sendMessage(messageEnvoiJoueur1);

		NetBuffer messageEnvoiJoueur2 = new NetBuffer();
		messageEnvoiJoueur2.writeInt(90);
		messageEnvoiJoueur2.writeInt(equipeQuiJoue.asInt);
		messageEnvoiJoueur2.writeInt(joueur2.monEquipe.asInt);
		joueur2.client.sendMessage(messageEnvoiJoueur2);
		
		//envoyerMessagesAuxJoueurs(messageEnvoiClient);
		System.out.println("GestionPartie.debuterPartieVsJoueur_classee : débuter partie VS joueur mode classé !");
		
	}

	public void debuterPartieVsJoueur_nonClasse(ServerClient arg_joueur1, ServerClient arg_joueur2) {
		// Choix aléatoire de la première équipe qui doit jouer
		boolean leBlancJoue = myStaticRandom.nextBoolean();
		TeamType equipeQuiJoue;
		if (leBlancJoue) equipeQuiJoue = TeamType.BLANC; else equipeQuiJoue = TeamType.NOIR;
		partieActuelle = new PylosPartie(ModeDeJeu.INTERNET, equipeQuiJoue, equipeQuiJoue, null, true);
		partieActuelle.equipeJoueur = equipeQuiJoue;
		partieActuelle.equipeIA = TeamType.AUCUNE;
		
		typeDePartie = TypePartie.NON_CLASSEE_VS_JOUEUR;
		
		joueur1 = arg_joueur1;
		joueur2 = arg_joueur2;

		joueur1.monEquipe = partieActuelle.equipeJoueur;
		joueur2.monEquipe = joueur1.monEquipe.equipeOpposee();
		
		GestionDesParties.instance.a1Partie.add(this); // sera actualisée et gérée par GestionDesParties

		NetBuffer messageEnvoiJoueur1 = new NetBuffer();
		messageEnvoiJoueur1.writeInt(90);
		messageEnvoiJoueur1.writeInt(equipeQuiJoue.asInt);
		messageEnvoiJoueur1.writeInt(joueur1.monEquipe.asInt);
		joueur1.client.sendMessage(messageEnvoiJoueur1);

		NetBuffer messageEnvoiJoueur2 = new NetBuffer();
		messageEnvoiJoueur2.writeInt(90);
		messageEnvoiJoueur2.writeInt(equipeQuiJoue.asInt);
		messageEnvoiJoueur2.writeInt(joueur2.monEquipe.asInt);
		joueur2.client.sendMessage(messageEnvoiJoueur2);
		
		//envoyerMessagesAuxJoueurs(messageEnvoiClient);
		System.out.println("GestionPartie.debuterPartieVsJoueur_classee : débuter partie VS joueur mode classé !");
		
	}
	
	
	
	public void loop() {
		if (terminee) return;
		
		if (canDoOtherThingsWhenTime > System.currentTimeMillis()) return;
		
		if (typeDePartie == TypePartie.CLASSEE_VS_IA) {
			
			// Traîter les actions de l'IA, lentement, sans bloquer le thread (évidemment)
			if (listeActionsSimplesEnAttenteDEnvoi.size() != 0) {
				PylosPartie_actionSimple action = listeActionsSimplesEnAttenteDEnvoi.get(0);
				listeActionsSimplesEnAttenteDEnvoi.remove(0);
				if (listeActionsSimplesEnAttenteDEnvoi.size() != 0)
					canDoOtherThingsWhenTime = System.currentTimeMillis() + timerMsIA;
				//System.out.println("GestionPartie.loop - traitemet action IA : action.typeAction = " + action.typeAction );
				if (action.typeAction == 0) { // poser pion de sa réserve
					
					NetBuffer envoi = new NetBuffer();
					envoi.writeInt(110);
					//envoi.writeInt(numeroDeTour);
					envoi.writeInt(action.equipeQuiJoueLeCoup.asInt);
					envoi.writeInt(action.hauteur);
					envoi.writeInt(action.xCell);
					envoi.writeInt(action.yCell);
					envoi.writeBool(false); // pas d'envoi des infos de la partie, il faudra attendre !
					//NetBuffer variablesImportantesPartie = partieActuelle.ecrireVariablesPrincipales(); // prend donc en compte le changement de tour !
					//envoi.writeByteArray(variablesImportantesPartie.convertToByteArray());
					envoyerMessagesAuxJoueurs(envoi);
					
					
				}
				
				if (action.typeAction == 1) { // déplacer un pion
					
					NetBuffer envoi = new NetBuffer();
					envoi.writeInt(111);
					//envoi.writeInt(numeroDeTour);
					envoi.writeInt(action.equipeQuiJoueLeCoup.asInt);
					envoi.writeInt(action.hauteur);
					envoi.writeInt(action.xCell);
					envoi.writeInt(action.yCell);
					envoi.writeInt(action.hauteur_init);
					envoi.writeInt(action.xCell_init);
					envoi.writeInt(action.yCell_init);
					envoi.writeBool(false); // pas d'envoi des infos de la partie, il faudra attendre !
					//NetBuffer variablesImportantesPartie = partieActuelle.ecrireVariablesPrincipales(); // prend donc en compte le changement de tour !
					//envoi.writeByteArray(variablesImportantesPartie.convertToByteArray());
					envoyerMessagesAuxJoueurs(envoi);
					
				}

				if (action.typeAction == 2) { // reprendre un pion
					
					NetBuffer envoi = new NetBuffer();
					envoi.writeInt(112);
					//envoi.writeInt(numeroDeTour);
					envoi.writeInt(action.equipeQuiJoueLeCoup.asInt);
					envoi.writeInt(action.hauteur);
					envoi.writeInt(action.xCell);
					envoi.writeInt(action.yCell);
					envoi.writeBool(false); // pas d'envoi des infos de la partie, il faudra attendre !
					//NetBuffer variablesImportantesPartie = partieActuelle.ecrireVariablesPrincipales(); // prend donc en compte le changement de tour !
					//envoi.writeByteArray(variablesImportantesPartie.convertToByteArray());
					envoyerMessagesAuxJoueurs(envoi);
					
				}
				return;
			}
			
			
			if (partieActuelle.tourDe == partieActuelle.equipeIA) { // faire jouer l'IA, si elle peut
				
				//listeActionsSimplesEnAttenteDEnvoi.clear();
				//System.out.println("001 GestionPartie.loop - IA joue !! " + partieActuelle.tourDe);
				IA_v40.serveurInternetUniquement_listeActionsSimplesIA.clear(); // serveurInternetUniquement_listeCoupIA
				partieActuelle.faireJouerIA(); // peut jouer plusieurs fois, d'où la liste IA_v40.serveurInternetUniquement_listeCoupIA !!
				listeActionsSimplesEnAttenteDEnvoi.addAll(IA_v40.serveurInternetUniquement_listeActionsSimplesIA);
				IA_v40.serveurInternetUniquement_listeActionsSimplesIA.clear();
				doitEnvoyerVariablesPartieDesQuePossible = true; // i.e. quand l'IA aura joué lentement !
				//System.out.println("002 GestionPartie.loop - IA joue !! " + partieActuelle.tourDe);
				
				//System.out.println("GestionPartie.loop - VS IA, l'IA a joué !");
				canDoOtherThingsWhenTime = System.currentTimeMillis() + timerMsIA; // pour ne pas être trop brusque !
				return; // ne rien faire de plus après que l'IA ait joué (et ne surtout pas aller, un peu plus bas, à "if (doitEnvoyerVariablesPartieDesQuePossible)" )
				
				/*
				NetBuffer message = new NetBuffer();
				message.writeInt(119); // liste d'actions simples jouées (à jouer lentement, donc)
				
				int nbActions = IA_v40.serveurInternetUniquement_listeActionsSimplesIA.size();
				message.writeInt(nbActions);
				for (PylosPartie_actionSimple action : IA_v40.serveurInternetUniquement_listeActionsSimplesIA) {
					action.writeToNetBuffer(message);
				}
				
				//ici
				// Ajout des coups joués par l'IA au buffer, pour un affichage plus lent
				// Tout les coups + état de la partie à la fin, le client applique les coups un par un, et envoie au serveur
				
				
				//message.writeInt(120); // nouvel état de la partie (ou passer le tour)
				partieActuelle.ecrireTouteLaPartieDansBuffer(message);
				envoyerMessagesAuxJoueurs(message);
				
				*/
				
				
				// J'envoie l'état complet de la partie, en bourrin, par manque de temps
				
				
				
				// J'envoie les coups joués par l'IA, et l'état de la partie (non fait par manque de temps, go méthode bourrine)
				/*
				for (int iCoup = 0; iCoup < IA_v40.serveurInternetUniquement_listeCoupIA.size(); iCoup++) {
					IA_v40_coup coup = serveurInternetUniquement_listeCoupIA.get(iCoup);
					
					
					
				}*/
				
				//IA_v40.serveurInternetUniquement_listeActionsSimplesIA.clear();
			}
		}
		

		if (doitEnvoyerVariablesPartieDesQuePossible) {
			doitEnvoyerVariablesPartieDesQuePossible = false;
			NetBuffer envoi = new NetBuffer();
			envoi.writeInt(114); // envoi des variables de la partie (resynchronisation, au cas où)
			NetBuffer variablesImportantesPartie = partieActuelle.ecrireVariablesPrincipales(); // prend donc en compte le changement de tour !
			envoi.writeByteArray(variablesImportantesPartie.convertToByteArray());
			envoyerMessagesAuxJoueurs(envoi);
		}
		
		
		
	}
	
	public void envoyerMessagesAuxJoueurs(NetBuffer message) {
		//System.out.println("GestionPartie.envoyerMessagesAuxJoueurs : monID="+monID + " joueur1="+joueur1);
		if (joueur1 != null) if (joueur1.client != null) if (joueur1.client.isConnected()) {
			
			joueur1.client.sendMessage(message);
		}// else
		//	System.out.println("GestionPartie.envoyerMessagesAuxJoueurs : joueur1 == null");
		
		if (joueur2 != null) if (joueur2.client != null) if (joueur2.client.isConnected()) {
			//System.out.println("GestionPartie.envoyerMessagesAuxJoueurs : joueur2 != null");
			joueur2.client.sendMessage(message);
		} //else
		//	System.out.println("GestionPartie.envoyerMessagesAuxJoueurs : joueur2 == null");
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








package commun.partie.nonGraphique;

import java.util.ArrayList;

//Fonctions d'élagage : liste des scores des noeuds : je ne retiens que les 40 ou 100 meilleurs noeuds, peu importe la profondeur : score minimal que doit avoir un noeud pour ne pas être abandonné
// -> augmentation pseudo-linéaire et non exponentielle
public class IA_elagage1 {
	
	private IA_elagage1_listeNoeudsProfondeurDonnee[] listeNoeudProfondeurDonnee;
	private int profondeurMax;
	private int nombreDeScoresParProfondeur;
	
	/** Constructeur, nouvelle instance de IA_elagage1 qui s'occupe de l'élagage tout au long du calcul des chemins possibles pour l'IA
	 * @param arg_profondeurMax profondeur maximale autorisée (pour allouer la mémoire)
	 * @param arg_nombreDeScoresParProfondeur nombre de noeuds retenus maximum, à une profondeur donnée
	 */
	public IA_elagage1(int arg_profondeurMax, int arg_nombreDeScoresParProfondeur) {
		nombreDeScoresParProfondeur = arg_nombreDeScoresParProfondeur;
		profondeurMax = arg_profondeurMax;
		listeNoeudProfondeurDonnee = new IA_elagage1_listeNoeudsProfondeurDonnee[arg_profondeurMax];
		for (int profondeurNoeud = 0; profondeurNoeud < arg_profondeurMax; profondeurNoeud++) {
			listeNoeudProfondeurDonnee[profondeurNoeud] = new IA_elagage1_listeNoeudsProfondeurDonnee(profondeurNoeud, nombreDeScoresParProfondeur);
		}
	}
	
	public boolean testerScoreNoeud(int arg_profondeur, int score, boolean chercheMeilleurScorePourIA) {
		if (profondeurMax < arg_profondeur) {
			System.err.println("ERREUR IA_elagage1.testerScoreNoeud : profondeurMax("+profondeurMax+") < arg_profondeur("+arg_profondeur+")");
			return true;
		}
		return listeNoeudProfondeurDonnee[arg_profondeur].ajouterScoreNoeud(score, chercheMeilleurScorePourIA);
	}
	
	
	
}
// Utilisation de listes statiques, pour ne pas avoir à travailler avec la mémoire (plus rapide et optilisé ainsi)
class IA_elagage1_listeNoeudsProfondeurDonnee {
	
	public final int profondeurActuelle;
	public int[] listeScoreNoeuds; // Liste de taille statique pour optimiser le nombre d'affectation en mémoire
	public int tailleListeScoreNoeds = 0; 
	private final int nombreDeScoresMaxParProfondeur;
	
	/** 
	 *  @param arg_profondeurActuelle
	 *  @param arg_nombreDeScoresParProfondeur  doit être > 0
	 */
	public IA_elagage1_listeNoeudsProfondeurDonnee(int arg_profondeurActuelle, int arg_nombreDeScoresMaxParProfondeur) {
		nombreDeScoresMaxParProfondeur = arg_nombreDeScoresMaxParProfondeur;
		profondeurActuelle = arg_profondeurActuelle;
		listeScoreNoeuds = new int[arg_nombreDeScoresMaxParProfondeur];
	}
	
	public boolean ajouterScoreNoeud(int arg_score, boolean chercheMeilleurScore) {
		// Ajoute un noeud, retourne true s'il est ajouté à la liste, false sinon
		
		// si la liste n'est pas pleine, je l'ajoute et je renvoie vrai
		if (listeScoreNoeuds.length > tailleListeScoreNoeds) { // nombreDeScoresMaxParProfondeur
			listeScoreNoeuds[tailleListeScoreNoeds] = arg_score;
			tailleListeScoreNoeds++;
			return true;
		} else { // si la liste est pleine
			
			if (chercheMeilleurScore) {
				
				// Cas 1 : je cherche le meilleur score pour l'IA, je ne retiens que les meilleurs noeuds,
				//         je cherche le score le plus faible, s'il est plus faible que le score actuel, je le remplace
				int scoreLePlusFaible = 0; // pour ne pas avoir de warning
				int scoreLePlusFaibleIndex = -1;
				for (int i = 0; i < listeScoreNoeuds.length; i++) {
					int score = listeScoreNoeuds[i];
					if (scoreLePlusFaibleIndex == -1 || score < scoreLePlusFaible) {
						scoreLePlusFaibleIndex = i;
						scoreLePlusFaible = score;
					}
				}
				// Je compare le score le plus faible au score actuel, s'il est plus petit, je le remplace
				if (scoreLePlusFaible < arg_score) {
					listeScoreNoeuds[scoreLePlusFaibleIndex] = arg_score;
					return true; // noeud valide
				} else
					return false; // noeud de score trop faible
			
			} else { // chercher le moins bon score pour l'IA, c'est à adversaire de jouer
				
				// Cas 2 : je cherche le moins bon score pour l'IA, je ne retiens que les moins bons noeuds,
				//         je cherche le score le plus élevé, s'il est plus grand que le score actuel, je le remplace
				int scoreLePlusEleve = 0; // pour ne pas avoir de warning
				int scoreLePlusEleveIndex = -1;
				for (int i = 0; i < listeScoreNoeuds.length; i++) {
					int score = listeScoreNoeuds[i];
					if (scoreLePlusEleveIndex == -1 || score > scoreLePlusEleve) {
						scoreLePlusEleveIndex = i;
						scoreLePlusEleve = score;
					}
				}
				// Je compare le score le plus élevé au score actuel, s'il est plus grand, je le remplace
				if (scoreLePlusEleve > arg_score) {
					listeScoreNoeuds[scoreLePlusEleveIndex] = arg_score;
					return true; // noeud valide
				} else
					return false; // noeud de score trop faible
			}
		}
	}
	
	
}










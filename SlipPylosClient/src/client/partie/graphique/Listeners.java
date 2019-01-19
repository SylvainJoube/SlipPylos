package client.partie.graphique;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import commun.partie.nonGraphique.PylosCell;
import commun.partie.nonGraphique.PylosCellResult;
import commun.partie.nonGraphique.PylosGridArray;
import commun.partie.nonGraphique.PylosPartie;
import commun.partie.nonGraphique.PylosPoint;
import commun.partie.nonGraphique.TeamType;

public class Listeners {
	
}

class CustomPoint extends Point {
	public boolean validPoint = false;
	public CustomPoint(int x, int y) {
		super(x, y);
	}
}

class MouseIsAbovePionSelection {
	public static boolean check(int xMouse, int yMouse) {
		int xDraw = GameHandler.jeuActuel.xDessinJetonsJoueur;
		int yDraw = GameHandler.jeuActuel.yDessinJetonsJoueur;
		
		if ((xDraw <= xMouse)
		&& (xDraw + CellDetection.cellWidth > xMouse)
		&& (yDraw <= yMouse)
		&& (yDraw + CellDetection.cellHeight > yMouse)) {
			return true;
		}
		return false;
	}
}

class MyMouseListener implements MouseListener {
	
	@Override public void mouseExited(MouseEvent event) {
		//LogWriter.Log("MyMouseListener.mouseExited :  event.getX = " + event.getX() + "event.getY = " + event.getY());
	}
	@Override public void mousePressed(MouseEvent event) {
		//LogWriter.Log("MyMouseListener.mousePressed :  event.getX = " + event.getX() + "event.getY = " + event.getY());
		// 
		GameHandler.jeuActuel.lastMouseX = event.getX();
		GameHandler.jeuActuel.lastMouseY = event.getY();
		
		GameHandler.jeuActuel.refreshWithMousePosition();
		
		//boolean volonteJoueurPionEnMain = false;
		if (!GameHandler.partieActuelle.joueurAJoueUnPion) { // si le joueur n'a pas encore joué de pion
			
			if (MouseIsAbovePionSelection.check(event.getX(), event.getY())) {
				GameHandler.jeuActuel.volonteJoueur = VolonteJoueur.PION_EN_MAIN;
				//volonteJoueurPionEnMain = true;
				//LogWriter.Log("MyMouseListener.mousePressed :  prendre un pion !");
			}
			
			
			// Tenter de déplacer un pion de son jeu pour le remonter
			if (GameHandler.jeuActuel.volonteJoueur == VolonteJoueur.MAIN_LIBRE)
			if (GameHandler.jeuActuel.dragCell == null)
			if (GameHandler.jeuActuel.highlightCell != null) {
				//LogWriter.Log("MyMouseListener.mousePressed :  déplacer un pion !");
				PylosGridArray plateauActuel = GameHandler.partieActuelle.plateauActuel;
				PylosCell tryMoveCell = GameHandler.jeuActuel.highlightCell;
				boolean canMovePawn = plateauActuel.canMovePawn(tryMoveCell, GameHandler.jeuActuel.dragCell);
				if (canMovePawn) {
					GameHandler.jeuActuel.dragCell = GameHandler.jeuActuel.highlightCell;
					GameHandler.jeuActuel.volonteJoueur = VolonteJoueur.DEPLACER_UN_PION;
					//LogWriter.Log("MyMouseListener.mousePressed :  déplacer un pion OK OK OK !");
				}
				
				
			}
		}
		
	}
	
	@Override public void mouseReleased(MouseEvent event) {
		
		GameHandler.jeuActuel.lastMouseX = event.getX();
		GameHandler.jeuActuel.lastMouseY = event.getY();

		GameHandler.jeuActuel.refreshWithMousePosition();
		// met à jour GameHandler.jeuActuel.poserUnPionIci
		
		if (GameHandler.jeuActuel.volonteJoueur == VolonteJoueur.PION_EN_MAIN
			|| GameHandler.jeuActuel.volonteJoueur == VolonteJoueur.DEPLACER_UN_PION) {
			PylosPoint mousePos = new PylosPoint(event.getX(),  event.getY());
			PylosPoint gridPos = new PylosPoint(GameHandler.jeuActuel.xGrid,  GameHandler.jeuActuel.yGrid);
			// Condition sur la hauteur : si je veux déplacer un pion, il faut que la destination soit plus haute
			int hateurMinimale = -1;
			if (GameHandler.jeuActuel.volonteJoueur == VolonteJoueur.DEPLACER_UN_PION)
				hateurMinimale = GameHandler.jeuActuel.dragCell.hauteur + 1;
			
			
			PylosCellResult peutPoserPionIci = GameHandler.jeuActuel.poserUnPionIci;
			
			//PylosCellResult cellUnderMouse = CellDetection.getCellUnderMouse(mousePos, gridPos, GameHandler.partieActuelle, TeamType.AUCUNE, hateurMinimale);
			TeamType equipeJoueur = GameHandler.partieActuelle.equipeJoueur;
			
			//boolean peutPoserSonPion = true;
			/*
			if (cellUnderMouse == null)
				peutPoserSonPion = false;
			else {
				if (cellUnderMouse.occupeePar != TeamType.AUCUNE) peutPoserSonPion = false;
				if (! cellUnderMouse.peutPoserIci)  peutPoserSonPion = false;
			}*/
			
			// Si je peux poser mon pion et qu'il s'agit d'un déplacement de pion, je vérifie que l'ancienne position du pion est inférieure à sa nouvelle
			/* déjà vérifié, et mis dans cellUnderMouse.peutPoserIci
			 * if (peutPoserSonPion && (GameHandler.jeuActuel.volonteJoueur == VolonteJoueur.DEPLACER_UN_PION)) {
				PylosCell dragCell = GameHandler.jeuActuel.dragCell;
				if (dragCell.hauteur >= cellUnderMouse.hauteur) {
					peutPoserSonPion = false;
				}
			}*/
			
			// Le joueur ne peut poser un pion que s'il n'a pas utilisé sa réserve
			/*if ((GameHandler.jeuActuel.volonteJoueur == VolonteJoueur.PION_EN_MAIN)
					&& GameHandler.partieActuelle.joueurAJoueUnPion) {
				peutPoserSonPion = false;
			}*/
			//peutPoserSonPion = 
			
			
			if (peutPoserPionIci != null)
			if (peutPoserPionIci.peutPoserIci) { // poser un pion ici
				
				GameHandler.partieActuelle.setCell(peutPoserPionIci.hauteur, peutPoserPionIci.xCell, peutPoserPionIci.yCell, equipeJoueur);
				GameHandler.partieActuelle.joueurAJoueUnPion = true;
				
				if (GameHandler.jeuActuel.volonteJoueur != VolonteJoueur.DEPLACER_UN_PION) {
					if (equipeJoueur == TeamType.BLANC) GameHandler.partieActuelle.nbJetonsBlanc--;
					if (equipeJoueur == TeamType.NOIR)  GameHandler.partieActuelle.nbJetonsNoir--;
				} else {
					// Je supprime l'ancien pion
					PylosCell dragCell = GameHandler.jeuActuel.dragCell;
					GameHandler.partieActuelle.setCell(dragCell.hauteur, dragCell.xCell, dragCell.yCell, TeamType.AUCUNE);
				}
				
				if (GameHandler.partieActuelle.plateauActuel.willFormSameColorRectangle(peutPoserPionIci.hauteur, peutPoserPionIci.xCell, peutPoserPionIci.yCell, equipeJoueur)) {
					GameHandler.partieActuelle.peutReprendrePionsNb = 2;
					
					/*
					if ((equipeJoueur == TeamType.BLANC) && (GameHandler.partieActuelle.nbJetonsNoir > 0)) {
						
						GameHandler.partieActuelle.nbJetonsBlanc++;
						GameHandler.partieActuelle.nbJetonsNoir--;
					}
					if ((equipeJoueur == TeamType.NOIR) && (GameHandler.partieActuelle.nbJetonsBlanc > 0)) {
						GameHandler.partieActuelle.nbJetonsBlanc--;
						GameHandler.partieActuelle.nbJetonsNoir++;
					}*/
					
				}
				//GameHandler.partieActuelle.tourSuivant();
			}
		}
		
		if (GameHandler.jeuActuel.volonteJoueur == VolonteJoueur.REPRENDRE_UN_PION) {
			if (GameHandler.jeuActuel.pickUpCell != null) { // actualisé juste au-dessus via : GameHandler.jeuActuel.refreshWithMousePosition();
				// Reprendre ce pion
				PylosCell pickUpCell = GameHandler.jeuActuel.pickUpCell;
				GameHandler.partieActuelle.setCell(pickUpCell.hauteur, pickUpCell.xCell, pickUpCell.yCell, TeamType.AUCUNE);
				TeamType equipeJoueur = GameHandler.partieActuelle.equipeJoueur;
				if (equipeJoueur == TeamType.BLANC) GameHandler.partieActuelle.nbJetonsBlanc++;
				if (equipeJoueur == TeamType.NOIR)  GameHandler.partieActuelle.nbJetonsNoir++;
				
				GameHandler.partieActuelle.peutReprendrePionsNb--;
				GameHandler.jeuActuel.refreshWithMousePosition(); // actualisation fonctionnelle et graphique
			}
		}
		
		
		//if (!volonteJoueurPionEnMain) {
		//	GameHandler.jeuActuel.volonteJoueur = VolonteJoueur.MAIN_LIBRE;
		//}
		GameHandler.jeuActuel.volonteJoueur = VolonteJoueur.MAIN_LIBRE;
		PylosPartie partie = GameHandler.jeuActuel.partieActuelle;
		if (GameHandler.tourSuivantPos.isInside(event.getX(), event.getY())) {
			boolean peutChangerDeTour = true;
			if (!partie.joueurAJoueUnPion) {
				LogWriter.Log("MyMouseListener.mousePressed : Impossible de passer votre tour lorsque vous n'avez pas encore joué !");
				peutChangerDeTour = false;
			}
			
			if (GameHandler.partieActuelle.peutReprendrePionsNb > 1) {
				LogWriter.Log("MyMouseListener.mousePressed : Impossible de passer votre tour lorsque vous n'avez repris au moins un des pions qui vons sont dûs !");
				peutChangerDeTour = false;
			}
			
			if (peutChangerDeTour)
			if (partie.tourDe == partie.equipeJoueur) {
				partie.tourSuivant();
			}
		}
		
		GameHandler.jeuActuel.dragCell = null;
		GameHandler.jeuActuel.refreshWithMousePosition();

	}
	@Override public void mouseClicked(MouseEvent event) {
		
	}
	@Override public void mouseEntered(MouseEvent event) {
		
	}
}

class MyMouseMotionListener implements MouseMotionListener {

	@Override
	public void mouseDragged(MouseEvent event) {
		mouseMoved(event);
		//LogWriter.Log("MyMouseMotionListener.mouseDragged :  event.getX = " + event.getX() + "event.getY = " + event.getY());
	}
	@Override
	public void mouseMoved(MouseEvent event) {
		//LogWriter.Log("MyMouseMotionListener.mouseMoved :  event.getX = " + event.getX() + "event.getY = " + event.getY());
		GameHandler.jeuActuel.lastMouseX = event.getX();
		GameHandler.jeuActuel.lastMouseY = event.getY();
		
		GameHandler.jeuActuel.refreshWithMousePosition();
		
		/*PylosPoint mousePos = new PylosPoint(GameHandler.jeuActuel.lastMouseX,  GameHandler.jeuActuel.lastMouseY);
		PylosPoint gridPos = new PylosPoint(GameHandler.jeuActuel.xGrid,  GameHandler.jeuActuel.yGrid);
		
		
		GameHandler.jeuActuel.highlightCell = null;
		// Si c'est l'équipe du joueur qui joue, 
		if (GameHandler.jeuActuel.volonteJoueur == VolonteJoueur.MAIN_LIBRE)
		if (GameHandler.partieActuelle.tourDe == GameHandler.partieActuelle.equipeJoueur) {
			PylosCellResult res = CellDetection.getCellUnderMouse(mousePos, gridPos, GameHandler.partieActuelle, GameHandler.partieActuelle.equipeJoueur);
			GameHandler.jeuActuel.highlightCell = res;
			//if (res != null) {
			//	System.out.println("MyMouseMotionListener.mouseMoved() : res!=null   " + res);
			//}
		}
		
		if (MouseIsAbovePionSelection.check(event.getX(), event.getY()) && !GameHandler.partieActuelle.joueurAUtiliseSaReserve) {
			GameHandler.highlightPionSelecton = true;
		} else GameHandler.highlightPionSelecton = false;
		
		
		if (GameHandler.tourSuivantPos.isInside(GameHandler.jeuActuel.lastMouseX,  GameHandler.jeuActuel.lastMouseY)) {
			GameHandler.tourSuivantHighlight = true;
		} else GameHandler.tourSuivantHighlight = false;
		
		if (GameHandler.jeuActuel.volonteJoueur == VolonteJoueur.PION_EN_MAIN) {
			PylosCellResult res = CellDetection.getCellUnderMouse(mousePos, gridPos, GameHandler.partieActuelle, TeamType.AUCUNE);
			GameHandler.jeuActuel.currentCellUnderMouse = res;
		} else
			GameHandler.jeuActuel.currentCellUnderMouse = null;*/
		//if (res != null)
			//System.out.println("MyMouseMotionListener.mouseMoved : res.hauteur =  " + res.hauteur + "  res.peutPoserIci = " + res.peutPoserIci + "  res.occupeePar = " + res.occupeePar);
	}
	
}
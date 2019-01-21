package client.partie.graphique;

import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/** RessourceManager
 * gère les ressources du jeu : images, et plus tard, sons
 * cet objet est unique, une seule instance est cr�e **/
public class RessourceManager {
	/** Seule instance de cette classe **/
	//public static RessourceManager instance = new RessourceManager();
	
	private static RessourceManager mainTnstance = new RessourceManager();
	
	//private HashMap
	public RessourceManager() {
		super();
	}
	
	// Liste des images chargées
	private static HashMap<String, BufferedImage> a1LoadedImage = new HashMap<String, BufferedImage>();
	
	public static BufferedImage LoadImage(String imagePathOnDisk) {
		// Je regarde si je n'ai pas déjà chargé l'image
		BufferedImage alreadyLoadedImage = GetImageFromPath(imagePathOnDisk);
		if (alreadyLoadedImage != null)
			return alreadyLoadedImage;
		
		// getClassLoader().getResource permet de charger l'image du bon endroit.
		URL url = mainTnstance.getClass().getClassLoader().getResource(imagePathOnDisk);
		if (url == null) {
			LogWriter.Log("RessourceManager.LoadImage : erreur, l'image n'existe pas : imagePathOnDisk = " + imagePathOnDisk + "  : url == null");
			return null;
		}
		
		
		// J'essaie de charger l'image depuis le disque
		/*File localFile = new File(imagePathOnDisk);
		if (! localFile.exists()) {
			LogWriter.Log("RessourceManager : erreur, l'image n'existe pas : imagePathOnDisk = " + imagePathOnDisk);
			return null; // le fichier n'existe pas sur le disque
		}*/
		
		
		
		// create an accelerated image of the right size to store our sprite in
		/*GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
		Image image = gc.createCompatibleImage(sourceImage.getWidth(),sourceImage.getHeight(),Transparency.BITMASK);
		
		// draw our source image into the accelerated image
		image.getGraphics().drawImage(sourceImage,0,0,null);*/
		
		
		BufferedImage buffImage = null;
		try {
			buffImage = ImageIO.read(url);//(localFile); // chargement de l'image
		} catch (IOException except) {
			LogWriter.Log("EXCEPTION RessourceManager.LoadImage : imagePathOnDisk = " + imagePathOnDisk);
			except.printStackTrace();
			return null;
		}
		
		if (buffImage != null) { // ajout à la HashMap si l'image a bien été chargée
			a1LoadedImage.put(imagePathOnDisk, buffImage);
		}
		return buffImage;
	}
	// Retrouver une image deuis le chemin d'accès disque
	public static BufferedImage GetImageFromPath(String imagePathOnDisk) {
		BufferedImage localImage = a1LoadedImage.get(imagePathOnDisk);
		return localImage;
	}
	
	
	
}











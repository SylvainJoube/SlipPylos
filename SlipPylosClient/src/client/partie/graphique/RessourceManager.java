package client.partie.graphique;

import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/** RessourceManager
 * g�re les ressources du jeu : images, et plus tard, sons
 * cet objet est unique, une seule instance est cr�e **/
public class RessourceManager {
	/** Seule instance de cette classe **/
	//public static RessourceManager instance = new RessourceManager();
	
	//private HashMap
	public RessourceManager() {
		super();
	}
	
	// Liste des images charg�es
	private static HashMap<String, BufferedImage> a1LoadedImage = new HashMap<String, BufferedImage>();
	
	public static BufferedImage LoadImage(String imagePathOnDisk) {
		// Je regarde si je n'ai pas d�j� charg� l'image
		BufferedImage alreadyLoadedImage = GetImageFromPath(imagePathOnDisk);
		if (alreadyLoadedImage != null)
			return alreadyLoadedImage;
		
		// J'essaie de charger l'image depuis le disque
		File localFile = new File(imagePathOnDisk);
		if (! localFile.exists()) {
			LogWriter.Log("RessourceManager : erreur, l'image n'existe pas : imagePathOnDisk = " + imagePathOnDisk);
			return null; // le fichier n'existe pas sur le disque
		}
		
		BufferedImage buffImage = null;
		try {
			buffImage = ImageIO.read(localFile); // chargement de l'image
		} catch (IOException except) {
			LogWriter.Log("EXCEPTION RessourceManager.LoadImage : imagePathOnDisk = " + imagePathOnDisk);
			except.printStackTrace();
			return null;
		}
		
		if (buffImage != null) { // ajout � la HashMap si l'image a bien �t� charg�e
			a1LoadedImage.put(imagePathOnDisk, buffImage);
		}
		return buffImage;
	}
	// Retrouver une image deuis le chemin d'acc�s disque
	public static BufferedImage GetImageFromPath(String imagePathOnDisk) {
		BufferedImage localImage = a1LoadedImage.get(imagePathOnDisk);
		return localImage;
	}
	
	
	
}











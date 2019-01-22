package client.outils.graphiques;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;

import client.listeners.Listeners;

public class PImage {
	
	public static void drawImageAlpha(Graphics2D graphics, Image image, int x, int y, double alpha) {
		if (graphics == null) return;
		if (image == null) return;
		float alpha_float = (float) alpha;
		float[] scales = { alpha_float, alpha_float, alpha_float, alpha_float };
		float[] offsets = new float[4];
		RescaleOp rop = new RescaleOp(scales, offsets, null);
		graphics.drawImage((BufferedImage) image, rop, x, y);
	}
	
	public static void drawImageAlpha_centered(Graphics2D graphics, Image image, int x, int y, double alpha) {
		if (graphics == null) return;
		if (image == null) return;
		int imgWidth = image.getWidth(null);
		int imgHeight = image.getHeight(null);
		int newX = x - (int)imgWidth/2;
		int newY = y - (int)imgHeight/2;
		drawImageAlpha(graphics, image, newX, newY, alpha);
	}
	
	public static void drawImageColorAlpha(Graphics2D graphics, Image image, int x, int y, double red, double green, double blue, double alpha) {
		if (graphics == null) return;
		if (image == null) return;
		float[] scales = { (float)red, (float)green, (float)blue, (float)alpha };
		float[] offsets = new float[4];
		RescaleOp rop = new RescaleOp(scales, offsets, null);
		graphics.drawImage((BufferedImage) image, rop, x, y);
	}

	public static int getImageWidth(Image image) {
		if (image == null) return 0;
		try {
			return image.getWidth(null);
		} catch (Exception e) { }
		return 0;
	}
	
	public static int getImageHeight(Image image) {
		if (image == null) return 0;
		try {
			return image.getHeight(null);
		} catch (Exception e) { }
		return 0;
	}
	
	private static double checkImageAsButton_time = 0;
	/** Dessiner une image, et la traiter comme un bouton
	 * @param graphics
	 * @param image
	 * @param x
	 * @param y
	 * @param needMouseReleased
	 * @param defaultPosOffset
	 * @return
	 */
	public static boolean checkImageAsButton(boolean canBeClicked, Graphics2D graphics, Image image, int x, int y, boolean needMouseReleased, int defaultPosOffset) {
		if (image == null) return false;
		if (graphics == null) return false;
		
		if (canBeClicked == false) {
			PImage.drawImageAlpha(graphics, image, x, y, 1);
			return false;
		}
		
		int xMouse = Listeners.frame_getMouseX();
		int yMouse = Listeners.frame_getMouseY();
		//int defaultPosOffset = 3;
		int posOffset = 0;
		boolean estEnSurbrillance = false;
		int imgWidth = getImageWidth(image);
		int imgHeight = getImageHeight(image);
		BoxPosition box = new BoxPosition(x, y, x + imgWidth, y + imgHeight);
		if (box.isInside(xMouse,  yMouse)) { // souris
			checkImageAsButton_time += 0.2; // si deux boutons sont survolés en même temps, l'effet sera plus rapide !
			posOffset = defaultPosOffset;
			double colorIntensity = 0.1;
			PImage.drawImageColorAlpha(graphics, image, x, y, colorIntensity, colorIntensity, colorIntensity, 1);
			estEnSurbrillance = true;
		}
		PImage.drawImageAlpha(graphics, image, x - posOffset, y - posOffset, 1);
		if (estEnSurbrillance) {
			double imgAlpha = ((Math.cos(checkImageAsButton_time) + 1) / 2) * 0.2 + 0.2;
			double colorIntensity = 1 + imgAlpha;
			PImage.drawImageColorAlpha(graphics, image, x - posOffset, y - posOffset, colorIntensity, colorIntensity, colorIntensity, 1);
		}
		if (estEnSurbrillance) {
			if (needMouseReleased) {
				if (Listeners.frame_mouseReleased())
					return true;
				return false;
			} else
				return true;
		}
		return false;
	}
	
	public static boolean checkImageAsButton(boolean canBeClicked, Graphics2D graphics, Image image, int x, int y) {
		return checkImageAsButton(canBeClicked, graphics, image, x, y, true, 3);
	}
}

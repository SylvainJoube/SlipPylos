package client.outils.graphiques;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;

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
}

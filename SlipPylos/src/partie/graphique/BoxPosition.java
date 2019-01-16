package partie.graphique;

/**
 * Détection des collisions simples
 * Classe générique
 *
 */

public class BoxPosition {
	int x1, y1, x2, y2;
	
	public BoxPosition(int arg_x1, int arg_y1, int arg_x2, int arg_y2) {
		x1 = arg_x1;
		x2 = arg_x2;
		y1 = arg_y1;
		y2 = arg_y2;
	}
	
	public boolean isInside(int x, int y) {
		if ((x >= x1)
		&&  (x <= x2)
		&&  (y >= y1)
		&&  (y <= y2)) {
			return true;
		}
		return false;
	}
}

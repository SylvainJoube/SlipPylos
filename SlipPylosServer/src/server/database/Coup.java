package server.database;

public class Coup {
	public String couleurPion;
	public int hauteur;
	public int xPos;
	public int yPos;

	public Coup(String couleurPion, int hauteur, int xPos, int yPos) {
		super();
		this.couleurPion = couleurPion;
		this.hauteur = hauteur;
		this.xPos = xPos;
		this.yPos = yPos;
	}
}

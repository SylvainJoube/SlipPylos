package server.database;

public class DatabaseUser {
	
	private String login;
	private String username;
	private String jsonWebToken;
	private int score;
	private int nbParties;
	private int nbVictoires;
	private int nbDefaites;

	public DatabaseUser(String login, String username, String jsonWebToken, int score, int nbParties, int nbVictoires,
			int nbDefaites) {
		super();
		this.login = login;
		this.username = username;
		this.jsonWebToken = jsonWebToken;
		this.score = score;
		this.nbParties = nbParties;
		this.nbVictoires = nbVictoires;
		this.nbDefaites = nbDefaites;
	}

	public String getLogin() {
		return login;
	}
	public String getUsername() {
		return username;
	}
	public String getJsonWebToken() {
		return jsonWebToken;
	}
	public int getScore() {
		return score;
	}
	public int getNbParties() {
		return nbParties;
	}
	public int getNbVictoires() {
		return nbVictoires;
	}
	public int getNbDefaites() {
		return nbDefaites;
	}
	
}

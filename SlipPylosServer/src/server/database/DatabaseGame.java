package server.database;

import java.util.ArrayList;

public class DatabaseGame {
	
	private String whitePlayer;
	private String blackPlayer;
	private String winner;
	private ArrayList<Coup> moveList;
	
	public DatabaseGame(String whitePlayer, String blackPlayer, String winner, ArrayList<Coup> moveList) {
		super();
		this.whitePlayer = whitePlayer;
		this.blackPlayer = blackPlayer;
		this.winner = winner;
		this.moveList = moveList;
	}

	public String getWhitePlayer() {
		return whitePlayer;
	}

	public String getBlackPlayer() {
		return blackPlayer;
	}

	public String getWinner() {
		return winner;
	}

	public ArrayList<Coup> getMoveList() {
		return moveList;
	}
	
	
	
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.database;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.net.http.HttpRequest;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

/**
 *
 * @author etienne
 */
public class Modele {
	
	private static String secret = 
	"009F07FFD80EB5B0520F7E822D8601908D85DBC9BD5C19DE84859C7FE6F15A894ED518F15871BB6FC48FFCE641B0E849A68D7CC12A90FA05966F99C34C91CE19";
	

	private final static String public_host = "https://pylos.jeanpierre.moe"; 
	private final static String local_host = "http://localhost"; // <- Bien vérifier cete ligne ! 
	
	public static String host = local_host; // Modifier cette URL pour la rendre locale
	
	
	

    public static void login(DatabaseRequest request, String login, String password) {
        try {
        	
        	HttpClient client = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_2)
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .build();

            var jObject = new JsonObject();
            jObject.addProperty("login", login);
            jObject.addProperty("password", password);
         
            var body = jObject.toString();
            
            var loginRequest = HttpRequest.newBuilder()
                .uri(URI.create(host + "/api/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

            client.sendAsync(loginRequest, HttpResponse.BodyHandlers.ofString()).thenAccept(loginResponse -> {
                if (loginResponse.statusCode() != 200) {
                    request.setCompleted(true);
                    request.setErrored(true);
                    request.setError(loginResponse.body());
                    return;
                } else {
                	var resLogin = new JsonParser().parse(loginResponse.body()).getAsJsonObject();
                	var token = resLogin.get("token").getAsString();
                	if (token.equals("")) {
                        request.setCompleted(true);
                        request.setErrored(true);
                        request.setError("unknownError");
                        return;
            		} else {
            			getUserInfo(request, token);
            			return;
            		}
                }
            });
        } catch (Exception e) {
            request.setCompleted(true);
            request.setErrored(true);
            request.setError("unknownError");
            return;
        }
    }

    public static void getUserInfo(DatabaseRequest request, String jsonWebToken) {
        try {
        	HttpClient client = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_2)
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .build();
            var infoRequest = HttpRequest.newBuilder()
                    .uri(URI.create(host + "/api/getPlayerInfo"))
                    .header("Content-Type", "application/json")
                    .header("authorization", jsonWebToken)
                    .build();
            client.sendAsync(infoRequest, HttpResponse.BodyHandlers.ofString()).thenAccept(infoResponse -> {
                if (infoResponse.statusCode() != 200) {
                    request.setCompleted(true);
                    request.setErrored(true);
                    request.setError(infoResponse.body());
                    return;
                } else {
                	var resInfo = new JsonParser().parse(infoResponse.body()).getAsJsonObject();
                	var login = resInfo.get("login").getAsString();
                	var username = resInfo.get("username").getAsString();
                	var score = resInfo.get("score").getAsInt();
                	var nbParties = resInfo.get("nbParties").getAsInt();
                	var nbVictoires = resInfo.get("nbVictoires").getAsInt();
                	var nbDefaites = resInfo.get("nbDefaites").getAsInt();
                	var user = new DatabaseUser(login, username, jsonWebToken, score, nbParties, nbVictoires, nbDefaites);
                	request.setCompleted(true);
                	request.setResult(user);
                }
            });
        } catch (Exception e) {
            request.setCompleted(true);
            request.setErrored(true);
            request.setError("unknownError");
            return;
        }
    }
    
    public static void endGame(DatabaseRequest request, String loginPlayerWhite, String loginPlayerBlack, String loginWinner, 
    		int winnerScore, int loserScore, ArrayList<Coup> moveList) {
    	try {
        	HttpClient client = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_2)
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .build();
        	var moveListAsJson = new Gson().toJson(moveList);

            var endGameBodyRequest = new JsonObject();
            endGameBodyRequest.addProperty("loginPlayerWhite", loginPlayerWhite);
            endGameBodyRequest.addProperty("loginPlayerBlack", loginPlayerBlack);
            endGameBodyRequest.addProperty("winner", loginWinner);
            endGameBodyRequest.addProperty("winnerScore", winnerScore);
            endGameBodyRequest.addProperty("loserScore", loserScore);
            endGameBodyRequest.addProperty("moveList", moveListAsJson);
        	
        	
        	var victoryRequest = HttpRequest.newBuilder()
        		.uri(URI.create(host + "/api/gameRegister"))
        		.header("Content-Type", "application/json")
        		.header("authorization", secret)
        		.POST(HttpRequest.BodyPublishers.ofString(endGameBodyRequest.toString()))
        		.build();
  
            client.sendAsync(victoryRequest, HttpResponse.BodyHandlers.ofString()).thenAccept(endGameResponse -> {
                if (endGameResponse.statusCode() != 200) {
                    request.setCompleted(true);
                    request.setErrored(true);
                    request.setError(endGameResponse.body());
                    return;
                } else {
                	request.setCompleted(true);
                	request.setResult(true);
                }
            });
    		
    	} catch (Exception e) {
            request.setCompleted(true);
            request.setErrored(true);
            request.setError("unknownError");
            return;
    		
    	}
	}
    
    public static void getUserGame(DatabaseRequest request, String jsonWebToken) {
        try {
        	HttpClient client = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_2)
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .build();
            var infoRequest = HttpRequest.newBuilder()
                    .uri(URI.create(host + "/api/getPlayerGames"))
                    .header("Content-Type", "application/json")
                    .header("authorization", jsonWebToken)
                    .build();
            client.sendAsync(infoRequest, HttpResponse.BodyHandlers.ofString()).thenAccept(infoResponse -> {
                if (infoResponse.statusCode() != 200) {
                    request.setCompleted(true);
                    request.setErrored(true);
                    request.setError(infoResponse.body());
                    return;
                } else {
                	var resGames = new JsonParser().parse(infoResponse.body()).getAsJsonObject();
                	
                	var gamesAsJsonArray = resGames.get("games").getAsJsonArray();
                	var databaseGameList = new ArrayList<DatabaseGame>();
                	for (var gameAsJson : gamesAsJsonArray) {
                		var moveList = new ArrayList<Coup>();
                		var loginPlayerWhite = gameAsJson.getAsJsonObject().get("loginPlayerWhite").getAsString();
                		var loginPlayerBlack = gameAsJson.getAsJsonObject().get("loginPlayerBlack").getAsString();
                		var winner = gameAsJson.getAsJsonObject().get("winner").getAsString();
                    	var moveListAsjsonArray = gameAsJson.getAsJsonObject().get("moveList").getAsJsonArray();
                    	for (var moveAsJson : moveListAsjsonArray) {
                    		var couleurPion = moveAsJson.getAsJsonObject().get("couleurPion").getAsString();
                    		var hauteur = moveAsJson.getAsJsonObject().get("hauteur").getAsInt();
                    		var xPos = moveAsJson.getAsJsonObject().get("xPos").getAsInt();
                    		var yPos = moveAsJson.getAsJsonObject().get("yPos").getAsInt();
                    		moveList.add(new Coup(couleurPion, hauteur, xPos, yPos));
                    	}
                    	var game = new DatabaseGame(loginPlayerWhite, loginPlayerBlack, winner, moveList);
                    	databaseGameList.add(game);
                	}
            		request.setCompleted(true);
            		request.setResult(databaseGameList);
                }
            });
        } catch (Exception e) {
            request.setCompleted(true);
            request.setErrored(true);
            request.setError("unknownError");
            return;
        }
    }
}

package server.database;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
/**
 *
 * @author etienne
 */
public class HtppTest {
    /**
     * @param args the command line arguments
     */
    
    public static void main(String[] args) {
        var lock = new Object();
        var requestList = new ArrayList<DatabaseRequest>();
        var moveList = new ArrayList<Coup>();
        var i = 0;
        
        while (i < 10) {
        	if(i%2 == 0) {
            	moveList.add(new Coup("white", 1, i, i));
        	} else {
            	moveList.add(new Coup("black", 1, i, i));
        	}
        	i++;
        }
        
        var request1 = new DatabaseRequest("gameRegister");
        Modele.endGame(request1, "zbebman", "mememan", "mememan", 128, 64, moveList);
        requestList.add(request1);
//        
        var request2 = new DatabaseRequest("login");
        Modele.login(request2, "zbebman", "zbebboi");
        requestList.add(request2);
        
        var request3 = new DatabaseRequest("getGames");
        Modele.getUserGame(request3, "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJsb2dpbiI6InpiZWJtYW4iLCJpYXQiOjE1NDgzNzYzODQsImV4cCI6MTU0ODQ2Mjc4NH0.ZTQDDxEVWfCaxqVtqXTt2zb7xaP9N7VoYOkLAzV-oSM");
        requestList.add(request3);
        while(requestList.size() != 0) {
        	
        	int iRequest = 0;
        	while(iRequest < requestList.size()) {
        		
        		DatabaseRequest request = requestList.get(iRequest);
        		
        		if (request.isCompleted()) {
        			if (request.isErrored()) {
                                    System.out.println("Error in request : " + request.getError() + " " + request.getType());
        			} else {
                                    System.out.println("Request completed ! " + request.getType());
        			}
        			requestList.remove(iRequest);
        			continue; // sans incrémenter iRequest
        		}
        		iRequest++;
        	}
        }
    }
}

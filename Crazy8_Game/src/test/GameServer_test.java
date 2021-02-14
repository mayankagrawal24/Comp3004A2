package test;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class GameServer_test implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int turnsMade;
	private int maxTurns;

	Server[] playerServer = new Server[4];
	Player_test[] players = new Player_test[4];

	ServerSocket ss;

	//Game game = new Game();
	int numPlayers;
	
	//new variables added
	CardDeck_test gameDeck;
	boolean directionCC = true;        //clockwise direction of play by default
	boolean skipNextTurn = false;      //to handle case of queen
	boolean twoCase = false;           //to handle if last card was 2
	int twoCaseNumCards = 0;
	boolean isWinner = false;
	
	String topCard;
	int gameNum = 0;
	int currentPlayerTurnIndex = 0;
	String newSuit = "";                      //used for when an 8 is played
	int roundStartedIndex = 0;
	String currentRoundWinner = "";
	//GameMessage message = new GameMessage();

	public static void main(String args[]) throws Exception {
		GameServer_test sr = new GameServer_test();

		sr.acceptConnections();
		sr.gameLoop();
	}

	public GameServer_test() {
		System.out.println("Starting game server");
		numPlayers = 0;
		turnsMade = 0;
		maxTurns = 13;
		// initialize the players list with new players
		for (int i = 0; i < players.length; i++) {
			players[i] = new Player_test(" ");
		}

		try {
			ss = new ServerSocket(3333);
		} catch (IOException ex) {
			System.out.println("Server Failed to open");
		}

	}

	/*
	 * -----------Networking stuff ----------
	 * 
	 */
	public void acceptConnections() throws ClassNotFoundException {
		try {
			System.out.println("Waiting for players...");
			while (numPlayers < 4) {
				Socket s = ss.accept();
				numPlayers++;

				Server server = new Server(s, numPlayers);

				// send the player number
				server.dOut.writeInt(server.playerId);
				server.dOut.flush();

				// get the player name
				Player_test in = (Player_test) server.dIn.readObject();
				System.out.println("Player " + server.playerId + " ~ " + in.name + " ~ has joined");
				// add the player to the player list
				players[server.playerId - 1] = in;
				playerServer[numPlayers - 1] = server;
			}
			System.out.println("Four players have joined the game");

			// start the server threads
			for (int i = 0; i < playerServer.length; i++) {
				Thread t = new Thread(playerServer[i]);
				t.start();
			}
			// start their threads
		} catch (IOException ex) {
			System.out.println("Could not connect 4 players");
		}
	}
	public void gameLoop() {
		int roundCounter = 0;
		while(true) {
			roundCounter++;
			roundLoop();
			//send the winner & scores to the players
			String roundOverMsg = "";
			roundOverMsg += ("Scores for all players after the round" + roundCounter + "\n");
			for (int i = 0; i < players.length; i++) {
				roundOverMsg +=(players[i].name + " : " + players[i].score);
				roundOverMsg += "\n";
			}
			roundOverMsg += ("The winner of the round is: " + currentRoundWinner);
			roundOverMsg += "\n";
			
			for (int i = 0; i < players.length; i++) {
				playerServer[i].sendRoundOverMsg(roundOverMsg);
			}
			//System.out.println("Finished sending round over MSGs");
			
			//tell players if there is another round 
			if (isGameOver()) {
				// tell the player to stop looping and display the game winner
				for (int i = 0; i < players.length; i++) {
					//System.out.println("SENT ROUND STATE OF 1");
					playerServer[i].sendNewRoundState(1);
				}
				String winner = getGameWinner();
				String gameOverMsg = "";
				gameOverMsg += ("Scores for all players after game end\n");
				for (int i = 0; i < players.length; i++) {
					gameOverMsg +=(players[i].name + " : " + players[i].score);
					gameOverMsg += "\n";
				}
				
				roundOverMsg += ("The winner of the Game is: " + winner);
				System.out.println(roundOverMsg);
				for (int i = 0; i < players.length; i++) {
					playerServer[i].sendGameOverMsg(gameOverMsg);
				}
				break;
				
			}
			else {
				//tell player to play another round
				for (int i = 0; i < players.length; i++) {
					//System.out.println("SENT ROUND STATE OF 0");
					playerServer[i].sendNewRoundState(0);
					players[i].clearPlayerHand();
				}
			}
			roundStartedIndex ++;
			//break;
		}
		
	}

	public void roundLoop() {
		//first look to send a player their cards
		Scanner input = new Scanner(System.in);
		currentPlayerTurnIndex = roundStartedIndex;
		directionCC = true;
		skipNextTurn = false;
		twoCase = false;
		twoCaseNumCards = 0;
		isWinner = false;
		gameDeck = new CardDeck_test();
		for (int x = 0; x < 5; x++) {
			for(int y = 0; y < players.length; y++) {
				players[y].addCard(takeCardFromTopOfDeck());
			}
		}
			//System.out.println("SERVER SENDING HANDS TO PLAYERS");
			//int choicdsddssd = input.nextInt();
			playerServer[0].sendInitalHand(players[0]);
			playerServer[1].sendInitalHand(players[1]);
			playerServer[2].sendInitalHand(players[2]);
			playerServer[3].sendInitalHand(players[3]);
			//System.out.println("Sent all inital Hands");
			
			//generate the top card to start play and make sure it is not 2
	        do {
	            topCard = takeCardFromTopOfDeck();

	            // Up card cannot be an "8". Throw it back in the deck somewhere.
	            if (topCard.charAt(0) == '8') {
	                // Randomly place somewhere back in the deck
	                Random rand = new Random();
	                gameDeck.deck.add(rand.nextInt(gameDeck.deck.size()), topCard);
	            }
	        } while (topCard.charAt(0) == '8');
	        //topCard = "QH";
	        
	        
	        System.out.println("THE STARTING TOP card is: " + topCard);
	        //choicdsddssd = input.nextInt();
			while (!isWinner) {

				turnsMade++;
				
				// send the round number
				System.out.println("*****************************************");
				System.out.println("Turn Number " + turnsMade);
				//System.out.println("THIS IS THE NEW SUIT: " + newSuit);
				
				
				//send the turn info to all players
				for (int i = 0; i < players.length; i++) {
					playerServer[i].sendNewTurnMessage(new GameMessage_test(this.topCard, players[currentPlayerTurnIndex].name, currentDirectionStr(), newSuit));
				}
				
				//send info to the specific player to play their turn
				if(skipNextTurn) {
					playerServer[currentPlayerTurnIndex].sendStartTurnState(2);
					skipNextTurn = false;
				}
				else if(twoCase) {
					//System.out.println("TOP CARD IS a 2, NEED TO HANDLE");
					playerServer[currentPlayerTurnIndex].sendStartTurnState(3);
					playerServer[currentPlayerTurnIndex].sendNewSuit();
					playerServer[currentPlayerTurnIndex].sendTwoCaseNumCards(twoCaseNumCards);
					
					//receive the state of the player for the 2 cards
					int draw2CardState = playerServer[currentPlayerTurnIndex].receive2CardDrawState();
					//take the new top card
					System.out.println("2 case State from server is " + draw2CardState);
					if (draw2CardState == 0) {
						updateServerInfoAfterPlayerTurn();
					}
					//give 2 cards to player
					else if(draw2CardState == 1) {
						ArrayList<String> drawnCards2Case = new ArrayList<String>(10);
						for (int x = 0; x < twoCaseNumCards; x++) {
							String newCard = takeCardFromTopOfDeck();
							drawnCards2Case.add(newCard);
						}
						playerServer[currentPlayerTurnIndex].sendDrawn2CaseCards(drawnCards2Case);
						
						int drawCardState = playerServer[currentPlayerTurnIndex].receiveDrawCardState();
						
						if (drawCardState == 0) {
							//System.out.println("NO REquest to draw");
							updateServerInfoAfterPlayerTurn();
						}
						//handle the draw request from the player.
						else if(drawCardState == 1) {
							//System.out.println("WANT TO DRAW");
							ArrayList<String> drawnCards = new ArrayList<String>(3);
							for (int x = 0; x < 3; x++) {
								String newCard = takeCardFromTopOfDeck();
								drawnCards.add(newCard);
								if (mustPlayCard(newCard, topCard, newSuit)) {
									break;
								}
							}
							
							//send the drawn cards to the player
							playerServer[currentPlayerTurnIndex].sendDrawnCards(drawnCards);
							updateServerInfoAfterPlayerTurn();
						}
						
					}
					
					
				}
				else {
					playerServer[currentPlayerTurnIndex].sendStartTurnState(1);
					playerServer[currentPlayerTurnIndex].sendNewSuit();
					//receiving the draw cards request
					int drawCardState = playerServer[currentPlayerTurnIndex].receiveDrawCardState();
					
					if (drawCardState == 0) {
						//System.out.println("NO REquest to draw");
						updateServerInfoAfterPlayerTurn();
					}
					//handle the draw request from the player.
					else if(drawCardState == 1) {
						//System.out.println("WANT TO DRAW");
						ArrayList<String> drawnCards = new ArrayList<String>(3);
						for (int x = 0; x < 3; x++) {
							String newCard = takeCardFromTopOfDeck();
							drawnCards.add(newCard);
							if (mustPlayCard(newCard, topCard, newSuit)) {
								break;
							}
						}
						
						//send the drawn cards to the player
						playerServer[currentPlayerTurnIndex].sendDrawnCards(drawnCards);
						updateServerInfoAfterPlayerTurn();
					}
					
				}
				
				//System.out.println("Getting round overState");
				int roundOverState = playerServer[currentPlayerTurnIndex].receiveRoundOverState();
				
				//send not your turn state to remaining players
				if (roundOverState == 0) {
					//System.out.println("SEnding that the round will continute state");
					for (int i = 0; i < players.length; i++) {
						if (i != currentPlayerTurnIndex) {
							playerServer[i].sendStartTurnState(0);
						}
					}
				}
				else if (roundOverState == 1) {
					currentRoundWinner = players[currentPlayerTurnIndex].name;
					for (int i = 0; i < players.length; i++) {
						if (i != currentPlayerTurnIndex) {
							//tell players to send back their scores
							//System.out.println("SENDING END ROUND STATE TO NOT YOUR TURN PLAYERS " + i);
							playerServer[i].sendStartTurnState(4);
							int playerNewScore = playerServer[i].receiveUpdatedPlayerScore();
							players[i].score = playerNewScore;
						}
					}
					
					//System.out.println("SCORES FOR PLAYERS AFTER ROUND 1");
					for (int i = 0; i < players.length; i++) {
						System.out.println(players[i].name + " : " + players[i].score);
					}
					System.out.println("The winner of the round is: " + currentRoundWinner);
					isWinner = true;
					break;
				}
				
				updateCurrentPlayerIndex();
			} 
			System.out.println("Round LOOP ENDED");

	}
	
    public String takeCardFromTopOfDeck() {
    	
        //String card = gameDeck.deck.get(0);
    	Scanner cardScanner = new Scanner(System.in);
    	System.out.println("Enter the card you would like to force: ");
    	String card = cardScanner.nextLine();
        //gameDeck.deck.remove(0);
        return card;
    }
    
    public String currentDirectionStr() {
    	if (directionCC) {
    		return "Left";
    	}
    	return "Right";
    }
    
    public void updateCurrentPlayerIndex() {
    	if (directionCC) {
    		if(currentPlayerTurnIndex < 3) {
    			currentPlayerTurnIndex++;
    		}
    		else {
    			currentPlayerTurnIndex = 0;
    		}
    	}
    	else {
    		if(currentPlayerTurnIndex > 0) {
    			currentPlayerTurnIndex--;
    		}
    		else {
    			currentPlayerTurnIndex = 3;
    		}
    	}
    	
    }
    
    public void changeDirectionOfPlay() {
    	if (directionCC == true) {
    		directionCC = false;
    	}
    	else {
    		directionCC = true;
    	}
    }
    
    public void updateServerInfoAfterPlayerTurn() {
    	int isNewCard = playerServer[currentPlayerTurnIndex].receiveIsNewCard();
    	String newTopCard = playerServer[currentPlayerTurnIndex].receiveNewTopCard();
		String updatedNewSuit = playerServer[currentPlayerTurnIndex].receiveNewSuit();
		System.out.println("The new top card received is " + newTopCard);
		System.out.println("The new top SUIT received is " + updatedNewSuit);
		if (newTopCard.charAt(0) == '1' && newTopCard.charAt(1) != '0' && isNewCard == 0) {
			System.out.println("CHANGING THE DIRECTION");
			changeDirectionOfPlay();
		}
		
		else if (newTopCard.charAt(0) == 'Q' && isNewCard == 0) {
			skipNextTurn = true;
		}
		
		else if (newTopCard.charAt(0) == '2' && isNewCard == 0) {
			twoCase = true;
			twoCaseNumCards += 2;
		}
		else if(newTopCard.charAt(0) != '2' || isNewCard == 1) {
			twoCase = false;
			twoCaseNumCards = 0;
		}
		
		this.topCard = newTopCard;
		this.newSuit = updatedNewSuit;
		System.out.println("OUT OF UPDATING SERVER INFO");
    }
    
    public boolean mustPlayCard(String pulledCard, String topCard, String newSuit) {
    	
    	if (pulledCard.charAt(0) == '8') {
    		return true;
    	}
    	else if(newSuit != ""  && pulledCard.charAt(pulledCard.length() - 1) == newSuit.charAt(0)) {
    		return true;
    	}
    	//bascially deal with 10
    	else if (newSuit == "" && (pulledCard.length() == 3 || topCard.length() == 3)) {
    		if (pulledCard.length() == topCard.length()) {
    			return true;
    		}
    		else if (pulledCard.charAt(pulledCard.length() - 1) == topCard.charAt(topCard.length() - 1)) {
    			return true;
    		}
    		else {
    			return false;
    		}
    	}
    	else if (newSuit == "" && (pulledCard.charAt(0) == topCard.charAt(0) || pulledCard.charAt(1) == topCard.charAt(1))) {
    		return true;
    	}
    	return false;
    }
    
    public boolean isGameOver () {
    	boolean playerBust = false;
    	for (int i = 0; i < players.length; i++) {
    		if (players[i].score >= 100) {
    			playerBust = true;
    			System.out.println("END THE GAME, A PLAYER HAS OVER 100 POINTS");
    		}
    	}
    	return playerBust;
    }
    
    public String getGameWinner() {
    	String winner = "";
    	int lowestScore = 1000;
    	for (int i = 0; i < players.length; i++) {
    		if (players[0].score < lowestScore) {
    			winner = players[i].name;
    			lowestScore = players[i].score;
    		}
    	}
    	return winner;
    }

	public class Server implements Runnable {
		private Socket socket;
		private ObjectInputStream dIn;
		private ObjectOutputStream dOut;
		private int playerId;

		public Server(Socket s, int playerid) {
			socket = s;
			playerId = playerid;
			try {
				dOut = new ObjectOutputStream(socket.getOutputStream());
				dIn = new ObjectInputStream(socket.getInputStream());
			} catch (IOException ex) {
				System.out.println("Server Connection failed");
			}
		}

		/*
		 * run function for threads --> main body of the thread will start here
		 */
		public void run() {
			try {
				while (true) {
				}

			} catch (Exception ex) {
				{
					System.out.println("Run failed");
					ex.printStackTrace();
				}
			}
		}

		/*
		 * send the scores to other players
		 */
		public void sendPlayers(Player_test[] pl) {
			try {
				for (Player_test p : pl) {
					dOut.writeObject(p);
					dOut.flush();
				}

			} catch (IOException ex) {
				System.out.println("Score sheet not sent");
				ex.printStackTrace();
			}

		}

		/*
		 * receive scores of other players
		 */
		public void sendTurnNo(int r) {
			try {
				dOut.writeInt(r);
				dOut.flush();
			} catch (Exception e) {
				System.out.println("Score sheet not received");
				e.printStackTrace();
			}
		}


		/*
		 * send scores of other players
		 */

		public void sendInitalHand(Player_test pl) {
			try {
				ArrayList<String> tempHand = pl.getHand();
				//here send each card to the player thread
				for (int i = 0; i < tempHand.size(); i++) {
					dOut.writeUTF(tempHand.get(i));;
				}
				dOut.flush();
			} 
			catch (Exception e) {
				System.out.println("Intial Cards not sent to player");
				e.printStackTrace();
			}
		}
		
		public void sendNewTurnMessage(GameMessage_test message) {
			try {
					dOut.writeObject(message);
					dOut.flush();
				
			} 
			catch (Exception e) {
				System.out.println("Could not send new Turn message to all players");
				e.printStackTrace();
			}
		}
		
		public void sendStartTurnState(int state) {
			//System.out.println("Sending start turn State");
			try {
				dOut.writeInt(state);
				dOut.flush();
			}
			catch (Exception e) {
				System.out.println("Could not send start turn State to current player");
				e.printStackTrace();
			}
		}
		
		public String receiveNewTopCard() {
			//System.out.println("Receiving the new Top card after players turn");
			try {
				return (String) dIn.readUTF();
			}
			catch (Exception e) {
				System.out.println("Could not receive the new top card");
				e.printStackTrace();
			}
			return "";
		}
		
		public String receiveNewSuit() {
			//System.out.println("Receiving the new SUIT after players turn");
			try {
				return (String) dIn.readUTF();
			}
			catch (Exception e) {
				System.out.println("Could not receive the new SUIT");
				e.printStackTrace();
			}
			return "";
		}
		
		public void sendNewSuit() {
			//System.out.println("Sending New suit to current player");
			try {
				dOut.writeUTF(newSuit);
				dOut.flush();
			}
			catch (Exception e) {
				System.out.println("Could not send New suit to current player");
				e.printStackTrace();
			}
		}
		
		public int receiveDrawCardState() {
			//System.out.println("Receiving the Draw Card Request from player");
			try {
				return (int) dIn.readInt();
			}
			catch (Exception e) {
				System.out.println("Could not receive DRAWCARD STate From current player");
				e.printStackTrace();
			}
			return 0;
		}
		
		public void sendDrawnCards(ArrayList<String> drawnCards) {
			try {
				dOut.writeInt(drawnCards.size());
				for(int i = 0; i < drawnCards.size(); i++) {
					dOut.writeUTF(drawnCards.get(i));
				}
				dOut.flush();
			}
			catch (Exception e) {
				System.out.println("Could not send NewState to current player");
				e.printStackTrace();
			}
		}
		
		public int receiveIsNewCard() {
			//System.out.println("Receiving Parameter that a new card was played");
			try {
				return (int) dIn.readInt();
			}
			catch (Exception e) {
				System.out.println("Could not receive Parameter that new card was played");
				e.printStackTrace();
			}
			return 0;
		}
		
		public void sendTwoCaseNumCards(int numCards) {
			//System.out.println("Sending how many cards player needs to play for 2 case");
			try {
				dOut.writeInt(numCards);
				dOut.flush();
			}
			catch (Exception e) {
				System.out.println("Could not send num 2 cards to current player");
				e.printStackTrace();
			}
		}
		
		public int receive2CardDrawState() {
			//System.out.println("Receieving State of 2 Card issue on Player");
			try {
				return (int) dIn.readInt();
			}
			catch (Exception e) {
				System.out.println("Could not receive State of 2 Card issue on Player");
				e.printStackTrace();
			}
			return 0;
		}
		
		public void sendDrawn2CaseCards(ArrayList<String> drawnCards) {
			//System.out.println("Sending 2 case drawn cards");
			try {
				dOut.writeInt(drawnCards.size());
				for(int i = 0; i < drawnCards.size(); i++) {
					dOut.writeUTF(drawnCards.get(i));
				}
				dOut.flush();
			}
			catch (Exception e) {
				System.out.println("Could not send the 2 case drawn cards");
				e.printStackTrace();
			}
		}
		
		public int receiveRoundOverState() {
			//System.out.println("Receieving State of round over from current Player");
			try {
				return (int) dIn.readInt();
			}
			catch (Exception e) {
				System.out.println("Could not receive State of round over from Player");
				e.printStackTrace();
			}
			return 0;
		}
		
		public int receiveUpdatedPlayerScore() {
			//System.out.println("Receieving score of a losing Player");
			try {
				return (int) dIn.readInt();
			}
			catch (Exception e) {
				System.out.println("Could not receive score of a losing Player");
				e.printStackTrace();
			}
			return 0;
		}
		
		public void sendRoundOverMsg(String msg) {
			//System.out.println("Sending the Round over msg");
			try {
				dOut.writeUTF(msg);
				dOut.flush();
			}
			catch (Exception e) {
				System.out.println("Could not send Round over msg");
				e.printStackTrace();
			}
		}
		
		public void sendGameOverMsg(String msg) {
			//System.out.println("Sending the Game over msg");
			try {
				dOut.writeUTF(msg);
				dOut.flush();
			}
			catch (Exception e) {
				System.out.println("Could not send Game over msg");
				e.printStackTrace();
			}
		}
		
		public void sendNewRoundState(int state) {
			//System.out.println("Sending the New round State");
			try {
				dOut.writeInt(state);
				dOut.flush();
			}
			catch (Exception e) {
				System.out.println("Could not send new Round state");
				e.printStackTrace();
			}
		}
		
	}

}

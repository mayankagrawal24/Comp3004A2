//package com.a1.yahtzeeGame;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

public class GameServer implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int turnsMade;
	private int maxTurns;

	Server[] playerServer = new Server[4];
	Player[] players = new Player[4];

	ServerSocket ss;

	Game game = new Game();
	int numPlayers;
	
	//new variables added
	CardDeck gameDeck = new CardDeck();
	boolean directionCC = true;        //clockwise direction of play by default
	boolean skipNextTurn = false;      //to handle case of queen
	boolean twoCase = false;           //to handle if last card was 2
	boolean isWinner = false;
	
	String topCard;
	int gameNum = 0;
	int currentPlayerTurnIndex = 0;
	String newSuit = "";                      //used for when an 8 is played
	//GameMessage message = new GameMessage();

	public static void main(String args[]) throws Exception {
		GameServer sr = new GameServer();

		sr.acceptConnections();
		sr.gameLoop();
	}

	public GameServer() {
		System.out.println("Starting game server");
		numPlayers = 0;
		turnsMade = 0;
		maxTurns = 13;
		// initialize the players list with new players
		for (int i = 0; i < players.length; i++) {
			players[i] = new Player(" ");
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
				Player in = (Player) server.dIn.readObject();
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
		//first look to send a player their cards
		for (int x = 0; x < 5; x++) {
			for(int y = 0; y < players.length; y++) {
				players[y].addCard(takeCardFromTopOfDeck());
			}
		}
			System.out.println("SERVER SENDING HANDS TO PLAYERS");
			playerServer[0].sendInitalHand(players[0]);
			playerServer[1].sendInitalHand(players[1]);
			playerServer[2].sendInitalHand(players[2]);
			playerServer[3].sendInitalHand(players[3]);

			
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
			while (!isWinner) {

				turnsMade++;
				
				// send the round number
				System.out.println("*****************************************");
				System.out.println("Turn Number " + turnsMade);
				System.out.println("THIS IS THE NEW SUIT: " + newSuit);
				
				
				//send the turn info to all players
				for (int i = 0; i < players.length; i++) {
					//message.setGameMessage(this.topCard, players[currentPlayerTurnIndex].name, currentDirectionStr());
					playerServer[i].sendNewTurnMessage(new GameMessage(this.topCard, players[currentPlayerTurnIndex].name, currentDirectionStr(), newSuit));
				}
				
				//send info to the specific player to play their turn
				if(skipNextTurn) {
					playerServer[currentPlayerTurnIndex].sendStartTurnState(2);
					skipNextTurn = false;
				}
				else if(twoCase) {
					
				}
				else {
					playerServer[currentPlayerTurnIndex].sendStartTurnState(1);
					playerServer[currentPlayerTurnIndex].sendNewSuit();
					//add some kind of receive
					String newTopCard = playerServer[currentPlayerTurnIndex].receiveNewTopCard();
					String updatedNewSuit = playerServer[currentPlayerTurnIndex].receiveNewSuit();
					System.out.println("The new top card received is " + newTopCard);
					System.out.println("The new top SUIT received is " + updatedNewSuit);
					if (newTopCard.charAt(0) == '1') {
						System.out.println("CHANGING THE DIRECTION");
						changeDirectionOfPlay();
					}
					
					else if (newTopCard.charAt(0) == 'Q') {
						skipNextTurn = true;
					}
					
					else if (newTopCard.charAt(0) == '2') {
						
					}
					
					this.topCard = newTopCard;
					this.newSuit = updatedNewSuit;
				}
				
				//send not your turn state to remaining players
				for (int i = 0; i < players.length; i++) {
					if (i != currentPlayerTurnIndex) {
						playerServer[i].sendStartTurnState(0);
					}
				}
				updateCurrentPlayerIndex();
			} 


	}
	
    public String takeCardFromTopOfDeck() {
        String card = gameDeck.deck.get(0);
        gameDeck.deck.remove(0);
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
		public void sendPlayers(Player[] pl) {
			try {
				for (Player p : pl) {
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
		 * receive scores of other players
		 */
		public int[] receiveScores() {
			try {
				int[] sc = new int[15];
				for (int i = 0; i < 15; i++) {
					sc[i] = dIn.readInt();
				}
				return sc;
			} catch (Exception e) {
				System.out.println("Score sheet not received");
				e.printStackTrace();
			}
			return null;
		}

		/*
		 * send scores of other players
		 */

		public void sendInitalHand(Player pl) {
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
		
		public void sendNewTurnMessage(GameMessage message) {
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
			try {
				dOut.writeInt(state);
				dOut.flush();
			}
			catch (Exception e) {
				System.out.println("Could not send State to current player");
				e.printStackTrace();
			}
		}
		
		public String receiveNewTopCard() {
			System.out.println("Receiving the new Top card after players turn");
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
			System.out.println("Receiving the new SUIT after players turn");
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
			try {
				dOut.writeUTF(newSuit);
				dOut.flush();
			}
			catch (Exception e) {
				System.out.println("Could not send NewState to current player");
				e.printStackTrace();
			}
		}
	}

}

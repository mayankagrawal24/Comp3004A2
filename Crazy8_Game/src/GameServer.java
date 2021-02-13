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
	GameMessage message = new GameMessage();

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
		
		try {

			//playerServer[0].sendPlayers(players);
			//playerServer[1].sendPlayers(players);
			//playerServer[2].sendPlayers(players);
			//playerServer[3].sendPlayers(players);
			System.out.println("SERVER SENDING HANDS TO PLAYERS");
			playerServer[0].sendInitalHand(players[0]);
			//System.out.println("Server hand of : " + players[0].name + " : ");
			//players[0].printPlayerHand();
			playerServer[1].sendInitalHand(players[1]);
			//System.out.println("Server hand of : " + players[1].name + " : ");
			//players[1].printPlayerHand();
			playerServer[2].sendInitalHand(players[2]);
			//System.out.println("Server hand of : " + players[2].name + " : ");
			//players[2].printPlayerHand();
			playerServer[3].sendInitalHand(players[3]);
			//System.out.println("Server hand of : " + players[3].name + " : ");
			//players[3].printPlayerHand();
			
			//System.out.println("There are this many cards left in the extra deck: " + gameDeck.deck.size());
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
	        
	        System.out.println("THE STARTING TOP card is: " + topCard);
			while (!isWinner) {

				turnsMade++;
				
				// send the round number
				System.out.println("*****************************************");
				System.out.println("Turn Number " + turnsMade);
				
				
				//send the turn info to all players
				for (int i = 0; i < players.length; i++) {
					message.setGameMessage(topCard, players[i].getHand(), players[currentPlayerTurnIndex].name, currentDirectionStr());
					playerServer[i].sendNewTurnMessage();
				}
				
				//send info to the specific player to play their turn
				if(skipNextTurn) {
					
				}
				else if(twoCase) {
					
				}
				else {
					playerServer[currentPlayerTurnIndex].sendStartTurnState(1);
					//add some kind of receive
					String newTopCard = playerServer[currentPlayerTurnIndex].receiveNewTopCard();
				}
				
				//send not your turn state to remaining players
				for (int i = 0; i < players.length; i++) {
					if (i != currentPlayerTurnIndex) {
						playerServer[i].sendStartTurnState(0);
					}
				}
				
				playerServer[0].sendTurnNo(turnsMade);
				playerServer[0].sendScores(players);
				players[0].setScoreSheet(playerServer[0].receiveScores());
				System.out.println("Player 1 completed turn and their score is " + players[0].getScore());

				playerServer[1].sendTurnNo(turnsMade);
				playerServer[1].sendScores(players);
				players[1].setScoreSheet(playerServer[1].receiveScores());
				System.out.println("Player 2 completed turn and their score is " + players[1].getScore());

				playerServer[2].sendTurnNo(turnsMade);
				playerServer[2].sendScores(players);
				players[2].setScoreSheet(playerServer[2].receiveScores());
				System.out.println("Player 3 completed turn and their score is " + players[2].getScore());

			}
			// add the upper bonus
			players[0].setScoreSheet(14, game.upperBonus(players[0].getUpperScore()));
			players[1].setScoreSheet(14, game.upperBonus(players[1].getUpperScore()));
			players[2].setScoreSheet(14, game.upperBonus(players[2].getUpperScore()));

			playerServer[0].sendTurnNo(-1);
			playerServer[1].sendTurnNo(-1);
			playerServer[2].sendTurnNo(-1);

			// send final score sheet after bonus added
			playerServer[0].sendScores(players);
			playerServer[1].sendScores(players);
			playerServer[2].sendScores(players);

			Player p = game.getWinner(players);
			System.out.println("The winner is " + p.name);
			for (int i = 0; i < playerServer.length; i++) {
				playerServer[i].dOut.writeObject(p);
				playerServer[i].dOut.flush();

			}
		} catch (IOException e) {
			e.printStackTrace();
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
		public void sendScores(Player[] pl) {
			try {
				for (int i = 0; i < pl.length; i++) {
					for (int j = 0; j < pl[i].getScoreSheet().length; j++) {
						dOut.writeInt(pl[i].getScoreSheet()[j]);
					}
				}
				dOut.flush();
			} catch (Exception e) {
				System.out.println("Score sheet not sent");
				e.printStackTrace();
			}
		}
		
		public void sendInitalHand(Player pl) {
			try {
				ArrayList<String> tempHand = pl.getHand();
				//here send each card to the player thread
				for (int i = 0; i < pl.getNumCardsInHand(); i++) {
					dOut.writeUTF(tempHand.get(i));;
				}
				dOut.flush();
			} 
			catch (Exception e) {
				System.out.println("Intial Cards not sent to player");
				e.printStackTrace();
			}
		}
		
		public void sendNewTurnMessage() {
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
			try {
				return (String) dIn.readUTF();
			}
			catch (Exception e) {
				System.out.println("Could not send State to current player");
				e.printStackTrace();
			}
			return "";
		}
	}

}

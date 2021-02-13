//package com.a1.yahtzeeGame;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.Scanner;
import java.util.Collections;

public class Player implements Serializable {

	/*
	 * score sheet is saved as a hashmap upper one, two, three, four, five, six
	 * lower 3ok, 4ok, full, sst, lst, yahtzee, chance, lowerbonus, upperbonus
	 */

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String name;

	int playerId = 0;

	Game game = new Game();
	
	//new variables needed for each player
	private int score = 0;
	private ArrayList<String> playerHand = new ArrayList<>(52);
	private int numCardsInHand = 0;
	
	static Client clientConnection;

	Player[] players = new Player[4];


	public Player getPlayer() {
		return this;
	}

	/*
	 * ----------Network Stuff------------
	 */

	/*
	 * send the to do to test server
	 */
	public void sendStringToServer(String str) {
		clientConnection.sendString(str);
	}

	public void connectToClient() {
		clientConnection = new Client();
	}

	public void connectToClient(int port) {
		clientConnection = new Client(port);
	}

	public void initializePlayers() {
		for (int i = 0; i < 4; i++) {
			players[i] = new Player(" ");
		}
	}

	public void startGame() {
		Scanner input = new Scanner(System.in);
		//players = clientConnection.receivePlayer();
		clientConnection.receiveInitalHand();
		printPlayerHand();
		while (true) {
			GameMessage newTurnMessage = clientConnection.receiveNewTurnMessage();
			newTurnMessage.printGameMessage();
			printPlayerHand();
			int currentState = clientConnection.receiveStartTurnState();
			
			//play a normal round there was no special case in the last round
			if (currentState == 1) {
				System.out.println(getHandAndChoices());
				String topCard = newTurnMessage.getTopCard();
				boolean validPlay = false;
				while (!validPlay) {
					try {
						int userChoice = input.nextInt();
						if (isValidPlay(userChoice, topCard)) {
							topCard = playerHand.get(userChoice);
							playerHand.remove(userChoice);
							if(topCard.charAt(0) == 8) {
								System.out.println("HANDLE THE 8 CASE");
							}
							//send back the new top card and update the player hand on the server copy of player
							
							clientConnection.sendNewTopCard(topCard);
							System.out.println("UPDaTED HAND AFTER TURN");
							printPlayerHand();
							//clientConnection.sendUpdatedPlayerHand(playerHand);
							validPlay = true;
						}
						else {
							System.out.println("You cannot play that card. Please try again.");
						}
					}
                    catch (IndexOutOfBoundsException e) {
                        System.out.println("Invalid selection. Please try again.");
                    }
				}
				
				//validate the users choice (bounds check & can play card on top card check)
				
			}
			else if(currentState == 0) {
				//do nothing as you are not playing this turn
			}
			
		}

	}

	
	public void addCard(String c) {
		this.playerHand.add(c);
		this.numCardsInHand = this.numCardsInHand + 1;
	}
	
	public void addToScore(int s) {
		this.score = this.score + s;
	}
	
	public ArrayList<String> getHand(){
		return this.playerHand;
	}
	
	public int getNumCardsInHand(){
		return this.numCardsInHand;
	}
	
	public void printPlayerHand() {
		System.out.print("Cards in player " + name + "'s hand are: ");
		for (int i = 0; i < playerHand.size(); i++) {
			System.out.print(playerHand.get(i) +  " ");
		}
		System.out.println("");
	}
	
    public String getHandAndChoices() {
        String niceHand = "Your hand ...... ";
        ArrayList<Integer> lengths = new ArrayList<>();

        // Row 1
        for (int i = 0; i < this.playerHand.size(); i++) {
            niceHand += this.playerHand.get(i) + ",  ";
            lengths.add(this.playerHand.get(i).length());
        }
        niceHand = niceHand.substring(0, niceHand.length() -3);
        niceHand+= ", Draw a card.";
        // Row 2
        niceHand += "\nYour choices ... ";

        for (int i = 0; i < lengths.size(); i++) {
            int padding = lengths.get(i);

            // One less space in formatting
            if (i >= 10) {
                niceHand = niceHand.substring(0, niceHand.length() -1);
            }

            niceHand += ("(" + (i) + ")")
                    + String.join("", Collections.nCopies(padding, " "));
        }
        niceHand = niceHand.trim();
        niceHand += "      (" + lengths.size() + ")";
        return niceHand.trim();
    }
    
    public boolean isValidPlay(int userIndex, String topCard) {
    	String userCardChoice = playerHand.get(userIndex);
    	if (userCardChoice.charAt(0) == 8) {
    		return true;
    	}
    	else if (userCardChoice.charAt(0) == topCard.charAt(0) || userCardChoice.charAt(1) == topCard.charAt(1)) {
    		return true;
    	}
    	return false;
    }

	private class Client {
		Socket socket;
		private ObjectInputStream dIn;
		private ObjectOutputStream dOut;

		public Client() {
			try {
				socket = new Socket("localhost", 3333);
				dOut = new ObjectOutputStream(socket.getOutputStream());
				dIn = new ObjectInputStream(socket.getInputStream());

				playerId = dIn.readInt();

				System.out.println("Connected as " + playerId);
				sendPlayer();

			} catch (IOException ex) {
				System.out.println("Client failed to open");
			}
		}

		public Client(int portId) {
			try {
				socket = new Socket("localhost", portId);
				dOut = new ObjectOutputStream(socket.getOutputStream());
				dIn = new ObjectInputStream(socket.getInputStream());

				playerId = dIn.readInt();

				System.out.println("Connected as " + playerId);
				sendPlayer();

			} catch (IOException ex) {
				System.out.println("Client failed to open");
			}
		}

		/*
		 * function to send the score sheet to the server
		 */
		public void sendPlayer() {
			try {
				dOut.writeObject(getPlayer());
				dOut.flush();
			} catch (IOException ex) {
				System.out.println("Player not sent");
				ex.printStackTrace();
			}
		}

		/*
		 * function to send strings
		 */
		public void sendString(String str) {
			try {
				dOut.writeUTF(str);
				dOut.flush();
			} catch (IOException ex) {
				System.out.println("Player not sent");
				ex.printStackTrace();
			}
		}

		public void receiveInitalHand() {
			System.out.println("Receiving the intial hand");
			try {
				for (int i = 0; i < 5; i++) {
					addCard(dIn.readUTF());
				}
			System.out.println("FINSIHED RECEVING INITIAL HAND");

			} 
			catch (IOException e) {
				System.out.println("Score sheet not received");
				e.printStackTrace();
			}
		}
		
		public GameMessage receiveNewTurnMessage() {
			System.out.println("\n\nReceiving the New Turn Message");
			GameMessage tempMessage = null;
			try {
					tempMessage = (GameMessage) dIn.readObject();
					return tempMessage;

			} 
			catch (IOException e) {
				System.out.println("COULD NOT RECEIVE NEW TURN MESSAGE");
				e.printStackTrace();
			}
			catch (ClassNotFoundException e) {
				System.out.println("Message class not found");
				e.printStackTrace();
			}
			return tempMessage;
		}
		public int receiveStartTurnState() {
			System.out.println("\nReceiving Start Turn State");
			try {
				return (int) dIn.readInt();
			} 
			catch (IOException e) {
				System.out.println("Score sheet not received");
				e.printStackTrace();
			}
			return -1;
		}
		
		public void sendNewTopCard(String tCard) {
			System.out.println("Sending new top card to the server");
			try {
				dOut.writeUTF(tCard);
				dOut.flush();
			} catch (IOException ex) {
				System.out.println("NEW TOP CARD NOT SENT");
				ex.printStackTrace();
			}
		}
		public void sendUpdatedPlayerHand(ArrayList<String> hand) {
			System.out.println("Sending Updated Player hand to the server");
			try {
				dOut.writeObject(hand);
				dOut.flush();
			} catch (IOException ex) {
				System.out.println("Updated player hand NOT SENT");
				ex.printStackTrace();
			}
		}
		

	}

	/*
	 * ---------Constructor and Main class-----------
	 */

	/*
	 * constructor takes the name of the player and sets the score to 0
	 */
	public Player(String n) {
		name = n;
	}

	public static void main(String args[]) {
		Scanner myObj = new Scanner(System.in);
		System.out.print("What is your name ? ");
		String name = myObj.next();
		Player p = new Player(name);
		p.initializePlayers();
		p.connectToClient();
		p.startGame();
		myObj.close();
	}
}

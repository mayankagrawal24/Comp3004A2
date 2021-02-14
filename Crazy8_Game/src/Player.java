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

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String name;

	int playerId = 0;
	String suits[] = {"H", "S", "D", "C"};

	Game game = new Game();
	
	//new variables needed for each player
	public int score = 0;
	private ArrayList<String> playerHand = new ArrayList<>(52);
	private int numCardsInHand = 0;
	private boolean isRoundOverVal = false;
	
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
		
		while(true) {
			startRound();
			//Receive the winner and scores of the last round
			System.out.println(clientConnection.receiveRoundWinnerMsg());
			int newRoundState = clientConnection.receiveNewRoundState();
			if (newRoundState == 0) {
				System.out.println("\n\n------- Starting a NEW ROUND ------- \n\n");
				playerHand.clear();
			}
			else if(newRoundState == 1) {
				System.out.println(clientConnection.receiveGameWinnerMsg());
				break;
			}
			//receive if we want to start another round
			//deal with the while loop
		}
	}

	public void startRound() {
		isRoundOverVal = false;
		Scanner input = new Scanner(System.in);
		//players = clientConnection.receivePlayer();
		clientConnection.receiveInitalHand();
		printPlayerHand();
		while (!isRoundOverVal) {
			GameMessage newTurnMessage = clientConnection.receiveNewTurnMessage();
			newTurnMessage.printGameMessage();
			printPlayerHand();
			int currentState = clientConnection.receiveStartTurnState();
			
			//if top card was a 2
			if (currentState == 3) {
				//playerHand.add("2D");
				printPlayerHand();
				String topCard2Case = newTurnMessage.getTopCard();
				String newSuit2Case = clientConnection.receiveNewSuit();
				int numCardsToPlay = clientConnection.receiveNumCardsFor2Case();
				int currentValid = 0;
				
				for (int x = 0; x < playerHand.size(); x++) {
					if(isValidPlay(x, topCard2Case, newSuit2Case)) {
						currentValid++;
					}
				}
				//get them to play the number of cards
				if (currentValid >= numCardsToPlay) {
					//send a state saying what is happening in the 2 case
					//System.out.println("WE DONT HAVE TO DRAW");
					clientConnection.send2CaseState(0);
					
					String playedCard = "";
					while (numCardsToPlay > 0) {
						//System.out.println("INSIDE LOOP");
						System.out.println(getHandAndChoices());
						boolean validPlay = false;
						while (!validPlay) {
							try {
								int userChoice = input.nextInt();
								if (isValidPlay(userChoice, topCard2Case, newSuit2Case)) {
									playedCard = playerHand.get(userChoice);
									playerHand.remove(userChoice);
									numCardsToPlay--;
									System.out.println("Removing " + playedCard);
									validPlay = true;
								}
								else if (userChoice == playerHand.size()) {
									System.out.println("Cannot draw Card");
								}
								else {
									System.out.println("You cannot play that card. Please try again.");
								}
							}
		                    catch (IndexOutOfBoundsException e) {
		                        System.out.println("Invalid selection. Please try again.");
		                    }
						}
					}
					
					//play the last card actually back to game and it will have effects
					clientConnection.sendIsNewCard(0);
					clientConnection.sendNewTopCard(playedCard);
					clientConnection.sendNewSuit(newSuit2Case);
					isRoundOverVal = isRoundOver();
					if (isRoundOverVal == true) {break;}
					
				}
				//make them draw and play their turn
				else {
					//System.out.println("WE HAVE TO DRAW");
					clientConnection.send2CaseState(1);
					clientConnection.receive2CaseCards();
					
					//now play regular turn
					//printPlayerHand();
					playNormalTurn(topCard2Case,"");
					isRoundOverVal = isRoundOver();
					if (isRoundOverVal == true) {break;}
				}
			}
			//play a normal round there was no special case in the last round
			else if (currentState == 1) {
				//playerHand.add("2S");
				String newSuit = clientConnection.receiveNewSuit();
				String topCard = newTurnMessage.getTopCard();
				playNormalTurn(topCard, newSuit);
				isRoundOverVal = isRoundOver();
				if (isRoundOverVal == true) {break;}

				
			}
			else if(currentState == 2) {
				System.out.println("Last player played a queen! Your turn is being Skipped");
				clientConnection.sendRoundOverState(0);
			}
			else if(currentState == 0) {
				//do nothing as you are not playing this turn
				System.out.println("Do nothing, not your turn");
			}
			//another player has won, calculate your score and send it to the sever
			else if (currentState == 4) {
				calculateRoundScore();
				clientConnection.sendLoserScore();
				break;
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
    
    public String getHandAndChoices2Case() {
        String niceHand = "Your hand ...... ";
        ArrayList<Integer> lengths = new ArrayList<>();

        // Row 1
        for (int i = 0; i < this.playerHand.size(); i++) {
            niceHand += this.playerHand.get(i) + ",  ";
            lengths.add(this.playerHand.get(i).length());
        }
        niceHand = niceHand.substring(0, niceHand.length() -3);
        // Row 2
        niceHand += "\nYour choices ... ";

        for (int i = 0; i < lengths.size(); i++) {
            int padding = lengths.get(i);

            // One less space in formatting
            if (i >= 10) {
                niceHand = niceHand.substring(0, niceHand.length() -1);
            }
        }
        niceHand = niceHand.trim();
        niceHand += "      (" + lengths.size() + ")";
        return niceHand.trim();
    }
  
    
    public String getSuitsAndChoices() {
    	String choices = "Suits .......... ";
    	for (int i = 0; i < suits.length; i++) {
    		choices += " " + suits[i] + " , ";
    	}
    	choices = choices.substring(0, choices.length() - 2);
    	choices += "\nYour choices ... ";
        for (int i = 0; i < suits.length; i++) {
            choices += ("(" + (i) + ")") + "  ";
        }
    	return choices;
    }
    
    public boolean isValidPlay(int userIndex, String topCard, String newSuit) {
    	
    	if (userIndex == playerHand.size()) {
    		return false;
    	}
    	
    	String userCardChoice = playerHand.get(userIndex);
    	
    	if (userCardChoice.charAt(0) == '8') {
    		return true;
    	}
    	else if(newSuit != ""  && userCardChoice.charAt(userCardChoice.length() - 1) == newSuit.charAt(0)) {
    		return true;
    	}
    	//basically deal with 10
    	else if (newSuit == "" && (userCardChoice.length() == 3 || topCard.length() == 3)) {
    		if (userCardChoice.length() == topCard.length()) {
    			return true;
    		}
    		else if (userCardChoice.charAt(userCardChoice.length() - 1) == topCard.charAt(topCard.length() - 1)) {
    			return true;
    		}
    		else {
    			return false;
    		}
    	}
    	else if (newSuit == "" && (userCardChoice.charAt(0) == topCard.charAt(0) || userCardChoice.charAt(1) == topCard.charAt(1))) {
    		return true;
    	}
    	return false;
    }
    
    public void calculateRoundScore () {
    	int tempScore = 0;
    	for (int i = 0; i < playerHand.size(); i++) {
    		String currentCard = playerHand.get(i);
    		if (currentCard.charAt(0) == 'K') {tempScore += 10;}
    		else if (currentCard.charAt(0) == 'Q') {tempScore += 10;}
    		else if (currentCard.charAt(0) == 'J') {tempScore += 10;}
    		else if (currentCard.length() == 3 && currentCard.charAt(0) == '1' && currentCard.charAt(1) == '0') {tempScore += 10;}
    		else if (currentCard.charAt(0) == '8') {tempScore += 50;}
    		else {
    			tempScore += Character.getNumericValue(currentCard.charAt(0));
    			}
    	}
    	//System.out.println(name + " Score is calcualted to being " + tempScore);
    	score += tempScore;
    	System.out.println(name + " Score is calcualted to being (ACTUAL)" + score);
    }
    
    public void clearPlayerHand() {
    	playerHand.clear();
    }
    
    public boolean isRoundOver() {
		//this player has emptied their hand and won this round
		if (playerHand.size() == 0) {
			//send a state to the server saying that round is over
			clientConnection.sendRoundOverState(1);
			return true;    //break out of turn loop
		}
		else {
			//send state to server saying that round is not over
			clientConnection.sendRoundOverState(0);
			return false;
		}
    }
    
    public void playNormalTurn(String topCard, String newSuit) {
    	Scanner input = new Scanner(System.in);
		System.out.println(getHandAndChoices());
		boolean validPlay = false;
		while (!validPlay) {
			try {
				int userChoice = input.nextInt();
				if (isValidPlay(userChoice, topCard, newSuit)) {
					topCard = playerHand.get(userChoice);
					playerHand.remove(userChoice);
					if(topCard.charAt(0) == '8') {
						System.out.println("You Played and 8. Please Pick a new Suit");
						System.out.println(getSuitsAndChoices());
						 boolean validSuit = false;
                            do {
                                try {
                                    newSuit = suits[input.nextInt()];
                                    validSuit = true;
                                }
                                catch (IndexOutOfBoundsException e) {
                                    System.out.println("Invalid selection. Please try again.");
                                }
                            }
                            while (!validSuit);
					}
					else {
						newSuit = "";
					}
					//send back the new top card and update the player hand on the server copy of player
					clientConnection.sendStatetoDrawCard(0); // don't want to draw cards, turn is done
					clientConnection.sendIsNewCard(0);
					clientConnection.sendNewTopCard(topCard);
					clientConnection.sendNewSuit(newSuit);
					System.out.println("Updated hand after turn");
					printPlayerHand();
					//clientConnection.sendUpdatedPlayerHand(playerHand);
					validPlay = true;
				}
				else if (userChoice == playerHand.size()) {
					//they want to draw a card
					int newCardPlayed = 1;
					//System.out.println("USer wants to draw a card");
					//send a request to server to draw cards and get back a [] of strings with the cards.
					clientConnection.sendStatetoDrawCard(1);
					clientConnection.receiveDrawnCards();
					System.out.println("Updated hand after drawing cards: ");
					printPlayerHand();
					
					//now try and play the last card, if works then normal, otherwise say skipping turn 
					if (isValidPlay(playerHand.size() - 1, topCard, newSuit)) {
						topCard = playerHand.get(playerHand.size() - 1);
						playerHand.remove(playerHand.size() - 1);
						System.out.println("Must play " + topCard + "\n");
						if(topCard.charAt(0) == '8') {
							System.out.println("You Played and 8. Please Pick a new Suit");
							System.out.println(getSuitsAndChoices());
							 boolean validSuit = false;
                                do {
                                    try {
                                        newSuit = suits[input.nextInt()];
                                        validSuit = true;
                                    }
                                    catch (IndexOutOfBoundsException e) {
                                        System.out.println("Invalid selection. Please try again.");
                                    }
                                }
                                while (!validSuit);
						}
						else {
							newSuit = "";
						}
						newCardPlayed = 0;
					}
					else {
						System.out.println("Cannot play any of the drawn Cards! Skipping Turn");
						newCardPlayed = 1;
					}
					clientConnection.sendIsNewCard(newCardPlayed);
					clientConnection.sendNewTopCard(topCard);
					clientConnection.sendNewSuit(newSuit);
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
				System.out.println("newSuit not sent");
				ex.printStackTrace();
			}
		}

		public void receiveInitalHand() {
			//System.out.println("Receiving the intial hand");
			try {
				for (int i = 0; i < 2; i++) {
					addCard(dIn.readUTF());
				}
			//System.out.println("FINSIHED RECEVING INITIAL HAND");

			} 
			catch (IOException e) {
				System.out.println("intial hand not received");
				e.printStackTrace();
			}
		}
		
		public GameMessage receiveNewTurnMessage() {
			//System.out.println("\n\nReceiving the New Turn Message");
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
			//System.out.println("\nReceiving Start Turn State");
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
			//System.out.println("Sending new top card to the server");
			try {
				dOut.writeUTF(tCard);
				dOut.flush();
			} catch (IOException ex) {
				System.out.println("NEW TOP CARD NOT SENT");
				ex.printStackTrace();
			}
		}
		public void sendUpdatedPlayerHand(ArrayList<String> hand) {
			//System.out.println("Sending Updated Player hand to the server");
			try {
				dOut.writeObject(hand);
				dOut.flush();
			} catch (IOException ex) {
				System.out.println("Updated player hand NOT SENT");
				ex.printStackTrace();
			}
		}
		
		public String receiveNewSuit() {
			//System.out.println("\nReceiving The NEW Suit");
			try {
				return (String) dIn.readUTF();
			} 
			catch (IOException e) {
				System.out.println("New Suit not received");
				e.printStackTrace();
			}
			return "";
		}
		
		public void sendNewSuit(String nSuit) {
			//System.out.println("Sending the new SUit to the server");
			try {
				dOut.writeUTF(nSuit);
				dOut.flush();
			} catch (IOException ex) {
				System.out.println("New Suit NOT SENT");
				ex.printStackTrace();
			}
		}
		
		//1 is they want to draw a card
		//0 is they do not want to draw card
		public void sendStatetoDrawCard(int state) {
			//System.out.println("Sending the DRAW CARD STATE to the server");
			try {
				dOut.writeInt(state);
				dOut.flush();
			} catch (IOException ex) {
				System.out.println("Draw Card State NOT SENT");
				ex.printStackTrace();
			}
		}
		
		public void receiveDrawnCards() {
			//System.out.println("\nReceiving Drawn Cards");
			try {
				int numCardsDrawn = (int) dIn.readInt();
				String card = "";
				for (int i = 0; i < numCardsDrawn; i++) {
					card = (String) dIn.readUTF();
					playerHand.add(card);
					System.out.println("Drew " + card);
				}
			} 
			catch (IOException e) {
				System.out.println("New Suit not received");
				e.printStackTrace();
			}

		}
		
		public void sendIsNewCard(int state) {
			//System.out.println("Sending the IS NEW CARD STATE to the server");
			try {
				dOut.writeInt(state);
				dOut.flush();
			} catch (IOException ex) {
				System.out.println("IS NEW CARD State NOT SENT");
				ex.printStackTrace();
			}
		}
		
		public int receiveNumCardsFor2Case() {
			//System.out.println("\nRECEVING 2 CAse num cards");
			try {
				return (int) dIn.readInt();
			} 
			catch (IOException e) {
				System.out.println("COULD NOT RECEive 2 CAse num cards");
				e.printStackTrace();
			}
			return 2;
		}

		//0 is no drawing 
		//1 is draw cards
		public void send2CaseState(int state) {
			//System.out.println("Sending 2 CASE state to server");
			try {
				dOut.writeInt(state);
				dOut.flush();
			} catch (IOException ex) {
				System.out.println("2 CASE state NOT SENT");
				ex.printStackTrace();
			}
		}
		
		public void receive2CaseCards() {
			//System.out.println("\n2 Case Drawn Cards");
			try {
				int numCardsDrawn = (int) dIn.readInt();
				String card = "";
				for (int i = 0; i < numCardsDrawn; i++) {
					card = (String) dIn.readUTF();
					playerHand.add(card);
					System.out.println("Drew " + card);
				}
			} 
			catch (IOException e) {
				System.out.println("2 Case drawn cards not received");
				e.printStackTrace();
			}
		}
		
		public void sendRoundOverState(int state) {
			//System.out.println("Sending the round over state");
			try {
				dOut.writeInt(state);
				dOut.flush();
			} catch (IOException ex) {
				System.out.println("Round over state NOT SENT");
				ex.printStackTrace();
			}
		}
		
		public void sendLoserScore() {
			//System.out.println("Sending the Loser Score");
			try {
				dOut.writeInt(score);
				dOut.flush();
			} catch (IOException ex) {
				System.out.println("Loser score NOT SENT");
				ex.printStackTrace();
			}
		}
		
		public String receiveRoundWinnerMsg() {
			//System.out.println("\nReceving the round winner MSG");
			String s = "UNSET";
			try {
				
				return (String) dIn.readUTF();
			} 
			catch (IOException e) {
				System.out.println("round winner MSG not received");
				e.printStackTrace();
			}
			return s;
		}
		public String receiveGameWinnerMsg() {
			//System.out.println("\nReceving the GAME winner MSG");
			String s = "UNSET GW";
			try {
				
				return (String) dIn.readUTF();
			} 
			catch (IOException e) {
				System.out.println("GAME winner MSG not received");
				e.printStackTrace();
			}
			return s;
		}
		
		public int receiveNewRoundState() {
			//System.out.println("\nReceving the new Round State");
			try {
				
				return (int) dIn.readInt();
			} 
			catch (IOException e) {
				System.out.println("round winner MSG not received");
				e.printStackTrace();
			}
			return 0;
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

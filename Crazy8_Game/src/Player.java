//package com.a1.yahtzeeGame;

import java.io.IOException;
import java.util.ArrayList;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.Scanner;

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
	//delete this in the future
	private int[] scoreSheet = new int[15];
	
	//new variables needed for each player
	private int score = 0;
	private ArrayList<String> playerHand = new ArrayList<>(52);
	private int numCardsInHand = 0;
	
	
	
	

	static Client clientConnection;

	Player[] players = new Player[4];
//	private ArrayList<String> scoreSheetKey = new ArrayList<String>(Arrays.asList("one", "two", "three", "four", "five",
//			"six", "3ok", "4ok", "full", "sst", "lst", "yahtzee", "chance", "bonus"));

	/*
	 * play a round of the game
	 */
	public int[] playRound(int[] dieRoll) {
		Scanner myObj = new Scanner(System.in);
		int count = 0; // reroll 3 times
		int stop = 0;

		game.printDieRoll(dieRoll);
		while (stop == 0) {
			System.out.println("Select an action: ");
			if (count < 3) {
				System.out.println("(1) Choose dice number to roll again");
				System.out.println("(2) Roll all again");
			}
			System.out.println("(3) Score this round");

			int act = myObj.nextInt();
			if (act == 1 && count < 3) {
				System.out.println("Select the die to hold (Ones not held get rerolled): (1,2...) ");
				String[] die = (myObj.next()).replaceAll("\\s", "").split(",");

				dieRoll = game.reRollNotHeld(dieRoll, die);
				System.out.println("New Roll: ");
				game.printDieRoll(dieRoll);
			}

			if (act == 2 && count < 3) {
				for (int i = 0; i < dieRoll.length; i++) {
					dieRoll = game.rerollDice(dieRoll, i);

				}
				System.out.println("New Roll: ");
				game.printDieRoll(dieRoll);
			}
			count++;
			if (act == 3) {
//				set yahtzee bonus if applicable 
				setScoreSheet(13, game.yahtzeeBonus(scoreSheet, dieRoll));

//				get the score for the option requested 
//				check if its been stored already before adding else ask for another number
				int r = 0;
				while (r != -1) {
					System.out.println("Where do you want to score this round? (1/2/3...)");
					r = myObj.nextInt();
//					add the yahtzee bonus if the roll was yahtzee and yahtzee is full
					if (game.isScoreSheetPositionEmpty(scoreSheet, r)) {
						setScoreSheet(scoreRound(r, dieRoll));
						r = -1;
					} else {
						System.out.println("The position is filled. Try another number");
					}
				}
				stop = 1;
			}
		}
		return this.scoreSheet;

	}

	public int[] scoreRound(int r, int[] dieRoll) {
		if (r == 7)
			setScoreSheet(6, game.scoreThreeOfAKind(dieRoll));
		else if (r == 8)
			setScoreSheet(7, game.scoreFourOfAKind(dieRoll));
		else if (r == 9)
			setScoreSheet(8, game.scoreFullHouse(dieRoll));
		else if (r == 10)
			setScoreSheet(9, game.scoreSmallStraight(dieRoll));
		else if (r == 11)
			setScoreSheet(10, game.scoreLargeStraight(dieRoll));
		else if (r == 12)
			setScoreSheet(11, game.scoreYahtzee(dieRoll));
		else if (r == 13) {
			setScoreSheet(12, game.scoreChance(dieRoll));
		} else
			setScoreSheet(r - 1, game.scoreUpper(dieRoll, r));
		return getScoreSheet();
	}

	public int getScore() {
		int sc = getLowerScore() + getUpperScore();
		if (getScoreSheet()[13] >= 0)
			sc += scoreSheet[13];
		if (getScoreSheet()[14] >= 0)
			sc += scoreSheet[14];
		return sc;
	}

	/*
	 * loop through the first 6 elements of the score sheet and return
	 */
	public int getUpperScore() {
		int count = 0;
		for (int i = 0; i < 6; i++) {
			if (this.getScoreSheet()[i] >= 0)
				count += this.scoreSheet[i];
		}
		return count;
	}

	/*
	 * sum of elements 6 - 13 including the yahtzee bonus
	 */
	public int getLowerScore() {
		int count = 0;
		for (int i = 6; i < 13; i++) {
			if (this.getScoreSheet()[i] >= 0)
				count += this.scoreSheet[i];
		}
		return count;
	}

	public int[] getScoreSheet() {
		return scoreSheet;
	}

	public void setScoreSheet(int cat, int score) {
		this.scoreSheet[cat] = score;
	}

	public void setScoreSheet(int[] ss) {
		this.scoreSheet = ss;
	}

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

	/*
	 * update turns
	 */
	public void printPlayerScores(Player[] pl) {
		// print the score sheets

		if (playerId == 1) {
			game.printScoreSheet(pl[0]);
			game.printScoreSheet(pl[1]);
			game.printScoreSheet(pl[2]);
		} else if (playerId == 2) {
			game.printScoreSheet(pl[1]);
			game.printScoreSheet(pl[0]);
			game.printScoreSheet(pl[2]);
		} else {
			game.printScoreSheet(pl[2]);
			game.printScoreSheet(pl[0]);
			game.printScoreSheet(pl[1]);
		}
	}

	public void startGame() {
		// receive players once for names

		//players = clientConnection.receivePlayer();
		clientConnection.receiveInitalHand();
		printPlayerHand();
		while (true) {
			clientConnection.receiveNewTurnMessage();
			//turnMessage.printGameMessage();
			int round = clientConnection.receiveRoundNo();
			if (round == -1)
				break;
			System.out.println("\n \n \n ********Round Number " + round + "********");
			int[][] pl = clientConnection.receiveScores();
			for (int i = 0; i < 3; i++) {
				players[i].setScoreSheet(pl[i]);
			}
			printPlayerScores(players);
			int[] dieRoll = game.rollDice();
			clientConnection.sendScores(playRound(dieRoll));
		}

	}

	public Player returnWinner() {
		try {
			int[][] pl = clientConnection.receiveScores();
			for (int i = 0; i < 3; i++) {
				players[i].setScoreSheet(pl[i]);
			}
			printPlayerScores(players);
			Player win = (Player) clientConnection.dIn.readObject();
			if (playerId == win.playerId) {
				System.out.println("You win!");
			} else {
				System.out.println("The winner is " + win.name);
			}

			System.out.println("Game over!");
			return win;

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
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

		/*
		 * receive scoresheet
		 */
		public void sendScores(int[] scores) {
			try {
				for (int i = 0; i < scores.length; i++) {
					dOut.writeInt(scores[i]);
				}
				dOut.flush();

			} catch (IOException e) {
				System.out.println("Score sheet not received");
				e.printStackTrace();
			}
		}

		/*
		 * receive scores of other players
		 */
		public Player[] receivePlayer() {
			Player[] pl = new Player[4];
			try {
				System.out.println("RECIVING THE PLAYERS");
				Player p = (Player) dIn.readObject();
				pl[0] = p;
				p = (Player) dIn.readObject();
				pl[1] = p;
				p = (Player) dIn.readObject();
				pl[2] = p;
				p = (Player) dIn.readObject();
				pl[3] = p;
				return pl;

			} catch (IOException e) {
				System.out.println("Score sheet not received");
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				System.out.println("class not found");
				e.printStackTrace();
			}
			return pl;
		}

		/*
		 * receive scores of other players
		 */
		public int[][] receiveScores() {
			try {
				int[][] sc = new int[4][15];
				for (int j = 0; j < 4; j++) {
					for (int i = 0; i < 15; i++) {
						sc[j][i] = dIn.readInt();
					}
					System.out.println();
				}

				return sc;
			} catch (Exception e) {
				System.out.println("Score sheet not received");
				e.printStackTrace();
			}
			return null;
		}

		/*
		 * receive scores of other players
		 */
		public int receiveRoundNo() {
			try {
				return dIn.readInt();

			} catch (IOException e) {
				System.out.println("Score sheet not received");
				e.printStackTrace();
			}
			return 0;
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
		
		public void receiveNewTurnMessage() {
			System.out.println("\n\nReceiving the New Turn Message");
			GameMessage tempMessage;
			try {
					tempMessage = (GameMessage) dIn.readObject();
					tempMessage.printGameMessage();

			} 
			catch (IOException e) {
				System.out.println("Score sheet not received");
				e.printStackTrace();
			}
			catch (ClassNotFoundException e) {
				System.out.println("class not found");
				e.printStackTrace();
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
		for (int i = 0; i < scoreSheet.length; i++) {
			scoreSheet[i] = -1;
		}
	}

	public static void main(String args[]) {
		Scanner myObj = new Scanner(System.in);
		System.out.print("What is your name ? ");
		String name = myObj.next();
		Player p = new Player(name);
		p.initializePlayers();
		p.connectToClient();
		p.startGame();
		p.returnWinner();
		myObj.close();
	}
}

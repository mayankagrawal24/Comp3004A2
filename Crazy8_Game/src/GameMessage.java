import java.io.Serializable;
import java.util.ArrayList;

public class GameMessage implements Serializable {
	private static final long serialVersionUID = 1L;
	private String currentTopCard;
	private ArrayList<String> passingPlayerCards;
	private String currentPlayerName;
	private String directionOfPlay;
	
	public GameMessage() {
		this.currentTopCard = "";
		this.passingPlayerCards = new ArrayList<String>(52);
		this.currentPlayerName = "";
		this.directionOfPlay = ""; 
	}
	
	public void setGameMessage(String tCard, ArrayList<String> cards, String currentPlayer, String direction) {
		this.currentTopCard = tCard;
		this.passingPlayerCards = cards;
		this.currentPlayerName = currentPlayer;
		this.directionOfPlay = direction; 
	}
	
	public void printGameMessage() {
		System.out.println("Current top card is: " + currentTopCard);
		System.out.println("The direction of play is: " + directionOfPlay);
		System.out.println("Whose turn it is : " + currentPlayerName);
		System.out.print("Your hand is : ");
		for (int i = 0; i < passingPlayerCards.size(); i++) {
			System.out.print(passingPlayerCards.get(i) + " ");
		}
		System.out.println("\n");
		
	}
	
	public String getTopCard() {return currentTopCard;}
}

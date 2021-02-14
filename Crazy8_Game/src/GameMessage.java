import java.io.Serializable;
import java.util.ArrayList;

public class GameMessage implements Serializable {
	private static final long serialVersionUID = 1L;
	private String currentTopCard;
	private String currentPlayerName;
	private String directionOfPlay;
	private String suitOfEightCase;
	
	public GameMessage(String tCard, String currentPlayer, String direction, String suit) {
		this.currentTopCard = tCard;
		this.currentPlayerName = currentPlayer;
		this.directionOfPlay = direction; 
		this.suitOfEightCase = suit;
	}
	
	public void setGameMessage(String tCard, String currentPlayer, String direction) {
		this.currentTopCard = tCard;
		this.currentPlayerName = currentPlayer;
		this.directionOfPlay = direction; 
	}
	
	public void printGameMessage() {
		System.out.println("\n\nCurrent top card is: " + currentTopCard);
		System.out.println("The direction of play is: " + directionOfPlay);
		System.out.println("Whose turn it is : " + currentPlayerName);
		if(this.suitOfEightCase != "") {
			System.out.println("New Suit Requested: " + suitOfEightCase);
		}
		
	}
	
	public String getTopCard() {return currentTopCard;}
}

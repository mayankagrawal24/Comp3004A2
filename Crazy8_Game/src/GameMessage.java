import java.io.Serializable;
import java.util.ArrayList;

public class GameMessage implements Serializable {
	private static final long serialVersionUID = 1L;
	private String currentTopCard;
	private String currentPlayerName;
	private String directionOfPlay;
	
	public GameMessage(String tCard, String currentPlayer, String direction) {
		this.currentTopCard = tCard;
		this.currentPlayerName = currentPlayer;
		this.directionOfPlay = direction; 
	}
	
	public void setGameMessage(String tCard, String currentPlayer, String direction) {
		this.currentTopCard = tCard;
		this.currentPlayerName = currentPlayer;
		this.directionOfPlay = direction; 
	}
	
	public void printGameMessage() {
		System.out.println("Current top card is: " + currentTopCard);
		System.out.println("The direction of play is: " + directionOfPlay);
		System.out.println("Whose turn it is : " + currentPlayerName);
		//System.out.println("\n");
		
	}
	
	public String getTopCard() {return currentTopCard;}
}

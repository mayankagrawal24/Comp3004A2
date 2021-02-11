import java.util.ArrayList;
import java.util.Collections;

public class CardDeck {

    private ArrayList<String> deck;
    //private String symbol;

    /**
     * The four different types of cards in a standard deck
     */
    public static final String[] CARD_SUITS = {
            "H", "S", "D", "C"
    };

    /**
     * The different face values for each card
     */
    public static final String[] CARD_RANKS = {
            "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"
    };

    /**
     * Class constructor
     *
     * @param type Heart, spade, diamond, or club
     * @param value 2, 3, 4, 5, 6, 7, 8, 9, 10, Jack, Queen, King, or Ace
     */
    private CardDeck() {
    	createDeck();
    }


    /**
     * Create and return a shuffled ArrayList of Cards
     *
     * @return ArrayList of 52 Cards
     */
    public void createDeck() {

        int deckSize = CARD_RANKS.length * CARD_SUITS.length;
        this.deck = new ArrayList<>(deckSize);

        for (int i = 0; i < CARD_RANKS.length; i++) {
            for (int j = 0; j < CARD_SUITS.length; j++) {
                this.deck.add(CARD_RANKS[i] + CARD_SUITS[j]);
 
            }
        }

        // Shuffle 
        Collections.shuffle(deck);

    }


   
    public static void main(String args[]) {
    	CardDeck deck = new CardDeck();
    	for (int i = 0 ; i < deck.deck.size(); i++) {
    		System.out.println(deck.deck.get(i));
    	}
    	
    	//System.out.println("SIZE:L " + deck.deck.size());
    	
    }
}
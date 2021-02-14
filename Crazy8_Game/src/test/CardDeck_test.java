package test;
import java.util.ArrayList;
import java.util.Collections;

public class CardDeck_test {

    public ArrayList<String> deck;
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
            "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"
    };

    /**
     * Class constructor
     *
     * @param type Heart, spade, diamond, or club
     * @param value 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, Jack, Queen, King
     */
    public CardDeck_test() {
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
    	CardDeck_test deck = new CardDeck_test();
    	for (int i = 0 ; i < deck.deck.size(); i++) {
    		System.out.println(deck.deck.get(i));
    		if (deck.deck.get(i).charAt(0) == '8') {
    			System.out.print("VALUE IS 8");
    		}
    	}
    	
        String card = deck.deck.get(0);
        deck.deck.remove(0);
    	
        System.out.println("Card Removed: " + card);
    	System.out.println("SIZE:L " + deck.deck.size());
    	
    	
    }
}
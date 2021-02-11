
//package com.a1.yahtzeeGame;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Hello world!
 *
 */
public class Game implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
//	private ArrayList<String> scoreSheetKey = new ArrayList<String>(Arrays.asList("one", "two", "three", "four", "five",
//			"six", "3ok", "4ok", "full", "sst", "lst", "yahtzee", "chance", "bonus"));

	/*
	 * reroll die which have not been held
	 */
	public int[] reRollNotHeld(int[] dieRoll, String[] held) {
		ArrayList<Integer> rolls = new ArrayList<Integer>(Arrays.asList(0, 1, 2, 3, 4));
		for (String s : held) {
			int rem = Integer.parseInt(s) - 1;
			rolls.remove(rolls.indexOf(rem));
		}
		// remove the index from the ones to be rolled
		for (int s : rolls) {
			dieRoll = rerollDice(dieRoll, (s));
		}
		return dieRoll;
	}

	/*
	 * returns the winner of the game when passed a list of players
	 */
	public Player getWinner(Player[] pl) {
		Player temp = pl[1];
		if (pl[0].getScore() >= pl[1].getScore())
			temp = pl[0];
		if (pl[2].getScore() >= temp.getScore())
			return pl[2];
		return temp;
	}

	/*
	 * counts the number of upper for a given number and list of die rolls
	 */
	public int scoreUpper(int[] roll, int nu) {
		int count = 0;
		for (int i : roll) {
			if (i == nu) {
				count += nu;
			}
		}
		return count;
	}

	/*
	 * checks for x of a kind
	 */
	public boolean isOfAKind(int x, int[] dieRoll) {
		HashMap<Integer, Integer> dict = new HashMap<Integer, Integer>();
		for (int i : dieRoll) {
			if (dict.containsKey(i)) {
				dict.put(i, dict.get(i) + 1);
			} else {
				dict.put(i, 1);
			}
		}
		Collection<Integer> val = dict.values();
		if (Collections.max(val) >= x) {
			return true;
		}
		return false;
	}

	/*
	 * returns the sum of the entire roll
	 */
	public int sumOfRoll(int[] dieRoll) {
		int count = 0;
		for (int i : dieRoll) {
			count += i;
		}
		return count;
	}

	/*
	 * checks if its a small / large straight arranges it in increasing order,
	 * subtracts the smallest number and then checks for sequence
	 */
	public boolean isStraight(String size, int[] dieRoll) {
		SortedSet<Integer> asList = new TreeSet<Integer>();
		for (int i : dieRoll) {
			asList.add(i);
		}

		int s;
		if (size == "s") {
			s = dieRoll.length - 1;
		} else {
			s = dieRoll.length;
		}

		int fst = asList.first();

		if (asList.size() < s) {
			return false;
		}

		for (int i = 0; i < s; i++) {
			int temp = asList.first() - fst;
			asList.remove(asList.first());
			if (i != temp)
				return false;
		}
		return true;

	}

	/*
	 * checks if its a full house goes through the elements and makes a dictionary
	 * of the keys and counts the number of times it occurs
	 */
	public boolean isFullHouse(int[] dieRoll) {
		HashMap<Integer, Integer> dict = new HashMap<Integer, Integer>();
		for (int i : dieRoll) {
			if (dict.containsKey(i)) {
				dict.put(i, dict.get(i) + 1);
			} else {
				dict.put(i, 1);
			}
		}
		Collection<Integer> val = dict.values();
		if (val.contains(2) && val.contains(3) || val.contains(5)) {
			return true;
		}
		return false;
	}

	/*
	 * checks for a yahtzee (5 of a kind) same way as it checks fo full house
	 */
	public boolean isYahtzee(int[] dieRoll) {
		HashMap<Integer, Integer> dict = new HashMap<Integer, Integer>();
		for (int i : dieRoll) {
			if (dict.containsKey(i)) {
				dict.put(i, dict.get(i) + 1);
			} else {
				dict.put(i, 1);
			}
		}
		Collection<Integer> val = dict.values();
		if (val.contains(5)) {
			return true;
		}
		return false;
	}

	/*
	 * score 3 of a kind
	 */
	public int scoreThreeOfAKind(int[] dieRoll) {
		if (isOfAKind(3, dieRoll)) {
			return sumOfRoll(dieRoll);
		}
		return 0;
	}

	/*
	 * score 4 of a kind
	 */
	public int scoreFourOfAKind(int[] dieRoll) {
		if (isOfAKind(4, dieRoll)) {
			return sumOfRoll(dieRoll);
		}
		return 0;
	}

	/*
	 * checks for a full house and returns a score
	 */
	public int scoreFullHouse(int[] dieRoll) {
		if (isFullHouse(dieRoll))
			return 25;
		return 0;
	}

	/*
	 * score small straight - 30 points
	 */
	public int scoreSmallStraight(int[] dieRoll) {
		if (isStraight("s", dieRoll))
			return 30;
		return 0;
	}

	/*
	 * score large straight - 40 points
	 */
	public int scoreLargeStraight(int[] dieRoll) {
		if (isStraight("l", dieRoll))
			return 40;
		return 0;
	}

	/*
	 * score yahtzee - 50 points
	 */
	public int scoreYahtzee(int[] dieRoll) {
		if (isYahtzee(dieRoll))
			return 50;
		return 0;
	}

	/*
	 * score chance - just the sum of the die
	 */

	public int scoreChance(int[] dieRoll) {
		return sumOfRoll(dieRoll);
	}

	/*
	 * adds 35 if upper score is over 63 or adds 0
	 */
	public int upperBonus(int upperScore) {
		if (upperScore >= 63)
			return 35;
		return 0;
	}

	/*
	 * if yahtzee slot is not null in the score sheet --> then add a bonus
	 */
	public int yahtzeeBonus(int[] scoreSheet, int[] dieRoll) {
		if (scoreSheet[11] > 0 && isYahtzee(dieRoll))
			return 100;
		return 0;
	}

	public int[] rollDice() {
		int[] die = new int[5];
		for (int i = 0; i < 5; i++) {
			int rand = (int) (Math.random() * 6 + 1);
			die[i] = rand;
		}
		return die;
	}

	public int[] rerollDice(int[] dieRoll, int i) {
		dieRoll[i] = (int) (Math.random() * 6 + 1);
		return dieRoll;
	}

	/*
	 * checks if the value of the score sheet is full or not
	 */
	public boolean isScoreSheetPositionEmpty(int[] scoreSheet, int val) {
		if (scoreSheet[val - 1] == -1) {
			return true;
		}
		return false;
	}

	/*
	 * print the die roll in a clear way
	 */
	public void printDieRoll(int[] dieRoll) {
		System.out.println(" ___    ___    ___    ___    ___  ");
		System.out.println("| " + dieRoll[0] + " |  | " + dieRoll[1] + " |  | " + dieRoll[2] + " |  | " + dieRoll[3]
				+ " |  | " + dieRoll[4] + " | ");
		System.out.println("|___|  |___|  |___|  |___|  |___|  ");

	}

	/*
	 * prints the score sheet
	 */
	public void printScoreSheet(Player p) {
		String[] sc = new String[p.getScoreSheet().length];
		for (int i = 0; i < p.getScoreSheet().length; i++) {
			if (p.getScoreSheet()[i] == -1) {
				sc[i] = "-";
			} else {
				sc[i] = "" + p.getScoreSheet()[i];
			}
		}
		System.out.println(
				"|---------------------------------------------------------------------------------------------------------------------------------------|");

		System.out.println("| Scores for player : " + p.name + "\t \t \t \t \t \t \t \t \t \t \t \t \t \t|");
		System.out.println("| Upper Scores: \t \t \t \t \t \t \t \t \t \t \t \t \t \t \t|");
		System.out.println("| (1) One : " + sc[0] + "  | (2) Two : " + sc[1] + "  | (3) Three : " + sc[2]
				+ "  | (4) Four : " + sc[3] + "  | (5) Five : " + sc[4] + "  | (6) Six : " + sc[5] + "  | Bonus : "
				+ sc[14] + "\t \t \t|");
		System.out.println(
				"|---------------------------------------------------------------------------------------------------------------------------------------|");
		System.out.println("| Lower Scores:\t \t \t \t \t \t \t \t \t \t \t \t \t \t \t \t|");
		System.out.println("| (7) Three Of A Kind : " + sc[6] + "   | (8) Four Of A Kind : " + sc[7]
				+ "  | (9) Full House : " + sc[8] + "  | (10) Small Straight : " + sc[9] + "  | (11) Large Straight : "
				+ sc[10] + "\t| \n| (12) Yahtzee : " + sc[11] + "  | (13) Chance : " + sc[12] + "  | Bonus : " + sc[13]
				+ "\t \t \t \t \t \t \t \t \t \t \t| ");
		System.out.println(
				"|---------------------------------------------------------------------------------------------------------------------------------------|");

		System.out.println("| Total Scores: \t \t \t \t \t \t \t \t \t \t \t \t \t \t \t|");
		System.out.println("| Total Upper : " + p.getUpperScore() + "\t \t \t \t \t \t \t \t \t \t \t \t \t \t \t|");
		System.out.println("| Total Lower : " + p.getLowerScore() + "\t \t \t \t \t \t \t \t \t \t \t \t \t \t \t|");
		System.out.println("| Total Score : " + p.getScore() + "\t \t \t \t \t \t \t \t \t \t \t \t \t \t \t|");
		System.out.println(
				"|---------------------------------------------------------------------------------------------------------------------------------------|");

	}
}
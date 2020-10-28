package uno;

import java.util.Scanner;
/**
 *
 * @author Tim Barber
 */
public class driver {

    public static void main(String[] args) {
        System.out.println("\nTim Barber\tSep 29, 2020\tCST-105"); //Header
        final boolean RULE_STACKING_SAME = true;
        final boolean RULE_STACKING_ALL = RULE_STACKING_SAME && true;
        final boolean RULE_DRAW_TILL_PLAYABLE = true;

        Deck deck = new Deck();
        Scanner stdin = new Scanner(System.in);

        System.out.println("How many players? ");
        final int numPlayers = stdin.nextInt();
        


    }

    private static void dealCards()

}
package uno;

import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author Tim Barber
 */
public class UNOConsoleDriver {

    public static void main(String[] args) {
        printHeader();
        final boolean RULE_STACKING_SAME = true;
        final boolean RULE_STACKING_ALL = RULE_STACKING_SAME && true;
        final boolean RULE_DRAW_TILL_PLAYABLE = true;

        UNODeck deck = new UNODeck();
        Scanner stdin = new Scanner(System.in);

        //System.out.print("How many players? (2 - 10): ");
        //UNOEngine engine = new UNOEngine(stdin.nextInt());
        UNOEngine engine = new UNOEngine(2);
        engine.prepareGame();
        displayHand(engine);
        engine.beginGame();
        displayTopCard(engine);
        displayAndReceiveChoices(engine);
    }

    private static void displayTopCard(UNOEngine e) {
        System.out.println("Card to play on: " + e.getTopCard());
    }

    private static String getResponse(String prompt, String regex) {
        String response = "";
        Scanner s = new Scanner(System.in);
        while (!response.matches(regex)) {
            response = s.nextLine();
        }
        return response;
    }

    private static String generateRegexForHand(UNOHand hand) {
        String regex = "/";
        String contents = hand.toCompactString();
        for (int i = 0; i < contents.length() / 2; i++) {
            String card = contents.substring(i * 2, i * 2 + 2);
        }
        return regex;
    }

    private static void displayAndReceiveChoices(UNOEngine e) {
        boolean hasPlus2 = e.getCurrentHand().hasPlus2();
        boolean hasPlus4 = e.getCurrentHand().hasPlus4();
        boolean hasPlus = e.getCurrentHand().hasPlus();
        if (e.getPendingCardsToDraw() > 0) {
            if (e.isRULE_STACKING_ALL()) {
                if (hasPlus) {
                    String out = "Your choices are to (d)raw " + e.getPendingCardsToDraw() + " cards or to defer the penalty to the next player by (p)laying an additional ";
                    if (hasPlus2 && !hasPlus4) {
                        out += "+2 card from your hand on top.";
                    } else if (hasPlus2 && hasPlus4) {
                        out += "+2 or +4 card from your hand on top.";
                    } else if (!hasPlus2 && hasPlus4) {
                        out += "+4 card from your hand on top.";
                    }
                    System.out.println(out);

                    String response = getResponse("Valid responses: (d)raw or (p)lay", "\\b([d]|draw)\\b|\\b((p|play){1}\\s*(((([y]|yellow)|([g]|green)|([b]|blue)|([r]|red)){1}\\s*([0-9]|s|skip|r|reverse){1})|((\\+\\s*[24]{1}){1})|(w|wild){1})\\b)"); // regex for drawing or playing, valid responses include, but are certainly not limited to: draw, d, play red 3, pr3, p r3, play +2, play wild, pw, and so on and so forth
                    response = response.trim();
                    if (response.contains("d")) {

                    }
                } else {
                    e.receivePendingCards();
                }
            } else {
                if (e.isRULE_STACKING_SAME()) {
                    if (e.getNum2CardsToDraw() > 0) {
                        if (e.getCurrentHand().hasPlus2()) {
                            System.out.println("Your choices are to draw " + e.getPendingCardsToDraw() + " cards or to defer to the next player along with an additional +2 card from your hand on top.");
                        } else {
                            e.receivePendingCards();
                        }
                    } else {
                        if (e.getCurrentHand().hasPlus4()) {
                            System.out.println("Your choices are to draw " + e.getPendingCardsToDraw() + " cards or to defer to the next player along with an additional +4 card from your hand on top.");
                        } else {
                            e.receivePendingCards();
                        }
                    }
                } else {
                    e.receivePendingCards();
                }
            }
        } else {
            ArrayList<UNOCard> options = e.getCurrentHand().getMatches(e.getTopCard(), e.getCurrentColor());
            if (options.size() > 1) {
                System.out.print("The cards you can play are: ");
            } else {
                System.out.print("The only card you can play is: ");
            }
            for (int i = 0; i < options.size() - 1; i++) {
                System.out.print(options.get(i));
                if (options.size() > 2) {
                    System.out.print(", ");
                }
            }
            if (options.size() > 1) {
                if (options.size() == 2) {
                    System.out.print(" ");
                }
                System.out.print("and ");
            }
            System.out.println(options.get(options.size() - 1) + ".");
        }
    }

    private static void displayHand(UNOEngine e) {
        UNOHand current = e.getCurrentHand();
        System.out.println("Player " + (e.getCurrentPlayer() + 1) + " " + current.toString());
    }

    private static void printHeader() {
        System.out.println(" ▄████████  ▄██████▄  ███▄▄▄▄      ▄████████  ▄██████▄   ▄█          ▄████████      ███    █▄  ███▄▄▄▄    ▄██████▄  \n"
                + "███    ███ ███    ███ ███▀▀▀██▄   ███    ███ ███    ███ ███         ███    ███      ███    ███ ███▀▀▀██▄ ███    ███ \n"
                + "███    █▀  ███    ███ ███   ███   ███    █▀  ███    ███ ███         ███    █▀       ███    ███ ███   ███ ███    ███ \n"
                + "███        ███    ███ ███   ███   ███        ███    ███ ███        ▄███▄▄▄          ███    ███ ███   ███ ███    ███ \n"
                + "███        ███    ███ ███   ███ ▀███████████ ███    ███ ███       ▀▀███▀▀▀          ███    ███ ███   ███ ███    ███ \n"
                + "███    █▄  ███    ███ ███   ███          ███ ███    ███ ███         ███    █▄       ███    ███ ███   ███ ███    ███ \n"
                + "███    ███ ███    ███ ███   ███    ▄█    ███ ███    ███ ███▌    ▄   ███    ███      ███    ███ ███   ███ ███    ███ \n"
                + "████████▀   ▀██████▀   ▀█   █▀   ▄████████▀   ▀██████▀  █████▄▄██   ██████████      ████████▀   ▀█   █▀   ▀██████▀  \n"
                + "                                                        ▀                                                           ");
        System.out.println("\nConsole UNO, created by Tim Barber for CST-105\nUpdated Dec 10, 2020\nThis line left intentionally blank. (except for this (and that))\n"); //Header
    }

}

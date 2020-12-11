package uno;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 *
 * @author Tim Barber
 */
public class UNOConsoleDriver {

    private static final boolean DEBUGMODE = false;

    public static void main(String[] args) {
        Logger log = new Logger("logs/");
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

        log.log("Regex for current hand: \"" + generateRegexForHand(engine.getCurrentHand().getMatches(engine.getTopCard(), engine.getCurrentColor())) + "\"");

        /*
         * Used for debugging regex generator
         *
         * UNOHand temp = generateHand(7);
         * System.out.println(generateRegexForHand(temp.getArrayList()));
         * System.out.println(getResponse("Playable cards: " + temp.toString() +
         * " | ", generateRegexForHand(temp.getArrayList())));
         *
         */
    }

    private static void displayTopCard(UNOEngine e) {
        System.out.println("Card to play on: " + e.getTopCard());
    }

    private static UNOHand generateHand(int handSize) {
        UNODefaultDeck deck = new UNODefaultDeck();
        deck.setNumPlayers(1);
        deck.shuffleDrawPile();
        deck.dealCards(handSize);
        return deck.getCurrentHand(0);
    }

    private static String getResponse(String prompt, String regex) {
        String response = "";
        Scanner s = new Scanner(System.in);

        while (!Pattern.matches(regex, response)) {
            System.out.print(prompt);
            response = s.nextLine().trim().toLowerCase();
            if (DEBUGMODE) {
                System.out.println("\"" + response + "\"");
            }
        }
        return response;
    }

    private static String generateRegexForHand(ArrayList<UNOCard> cards) {
        String regex = "";

        String drawPartition = "\\b(d|draw)\\b|";
        regex += drawPartition;
        String contents = UNOHand.toCompactString(cards);
        String[] colors = {"", "", "", ""}; // color order is alphabetical, b, g, r, y
        String wilds = "";
        boolean blues = false;
        boolean greens = false;
        boolean reds = false;
        boolean yellows = false;

        for (int i = 0; i < contents.length() / 2; i++) {
            String card = contents.substring(i * 2, i * 2 + 2);
            char color = card.charAt(0);
            char type = card.charAt(1);
            if (color == 'w') {
                wilds = wilds + type;
            } else {
                switch (color) {
                    case 'b':
                        colors[0] = colors[0] + type;
                        blues = true;
                        break;
                    case 'g':
                        colors[1] = colors[1] + type;
                        greens = true;
                        break;
                    case 'r':
                        colors[2] = colors[2] + type;
                        reds = true;
                        break;
                    case 'y':
                        colors[3] = colors[3] + type;
                        yellows = true;
                        break;
                }
            }
        }

        String playPartition = "(\\b((p|play){1}\\s*(";
        for (int i = 0; i < 4; i++) {
            String colorSet = colors[i];
            if (!colorSet.isEmpty()) {
                String actualColor = "";
                switch (i) {
                    case 0:
                        actualColor = "blue";
                        break;
                    case 1:
                        actualColor = "green";
                        break;
                    case 2:
                        actualColor = "red";
                        break;
                    case 3:
                        actualColor = "yellow";
                        break;
                }
                playPartition += "((" + actualColor.charAt(0) + "|" + actualColor + ")\\s*([" + colorSet + "]";
                if (colorSet.contains("s")) {
                    playPartition += "|skip";
                }
                if (colorSet.contains("r")) {
                    playPartition += "|reverse";
                }
                playPartition += "){1})";
                boolean isLastColor = (i == 0 && !greens && !reds && !yellows && wilds.isEmpty()) || (i == 1 && !reds && !yellows && wilds.isEmpty()) || (i == 2 && !yellows && wilds.isEmpty()) || (i == 3 && wilds.isEmpty());
                if (!isLastColor) {
                    playPartition += "|";
                }
            }
        }
        if (!wilds.isEmpty()) {
            playPartition += "(";
            for (int i = 0; i < wilds.length(); i++) {
                if (wilds.charAt(i) == '+') { // card is +4
                    playPartition += "((w|wild(\\+4|[+4]))|(\\+4))";
                } else { // card is normal wild card
                    playPartition += "(w|wild)";
                }
                if (i < wilds.length() - 1) {
                    playPartition += "|";
                }
            }
            playPartition += ")";
        }
        playPartition += "))\\b)";
        regex += playPartition;
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
            } else if (options.isEmpty()) {
                System.out.print("You cannot play any cards. ");
                if (e.isRULE_DRAW_TILL_PLAYABLE()) {
                    System.out.print("You must draw until you have a playable card.");
                    while (e.getCurrentHand().getMatches(e.getTopCard(), e.getCurrentColor()).isEmpty()) {
                        e.drawCurrentPlayerCards(1);
                    }
                    if (e.isRULE_SKIP_AFTER_DRAW()) {
                        return;
                    }
                } else {
                    System.out.print("You must draw a card");
                    e.drawCurrentPlayerCards(1);
                    if (e.isRULE_SKIP_AFTER_DRAW()) {
                        return;
                    }
                }
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
        System.out.println("\nConsole UNO, created by Tim Barber for CST-105\nUpdated Dec 11, 2020\nThis line left intentionally blank. (except for this (and that))\n"); //Header
    }

}

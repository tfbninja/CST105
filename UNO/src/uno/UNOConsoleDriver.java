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
    public static Logger log = new Logger("logs/");

    public static void main(String[] args) {
        printHeader();
        final boolean RULE_STACKING_SAME = true;
        final boolean RULE_STACKING_ALL = RULE_STACKING_SAME && true;
        final boolean RULE_DRAW_TILL_PLAYABLE = true;

        UNODeck deck = new UNODeck();
        Scanner stdin = new Scanner(System.in);

        //print("How many players? (2 - 10): ");
        //UNOEngine engine = new UNOEngine(stdin.nextInt());
        UNOEngine engine = new UNOEngine(2);
        engine.prepareGame();
        displayHand(engine);
        engine.beginGame();

        while (!engine.hasAPlayerEmptiedHand()) {
            displayTopCard(engine);
            displayHand(engine);
            displayAndReceiveChoices(engine);
            engine.assignNextPlayer();
            engine.assessPileSizes();
        }

        log.debug("Regex for current hand: \"" + generateRegexForHand(engine.getCurrentPlayerMatches()) + "\"");

        log.closeLog();

        /*
         * Used for debugging regex generator
         *
         * UNOHand temp = generateHand(7);
         * println(generateRegexForHand(temp.toArrayList()));
         * println(getResponse("Playable cards: " + temp.toString() +
         * " | ", generateRegexForHand(temp.toArrayList())));
         *
         */
    }

    private static void displayTopCard(UNOEngine e) {
        println("Card to play on: " + e.getTopCard());
    }

    private static void forceEndGame(String reason) {
        log.log("Ending game, reason: " + reason, 1);
        log.closeLog();
        System.exit(0);
    }

    private static String getResponse(String prompt, String regex) {
        String response = "default";
        Scanner s = new Scanner(System.in);
        log.debug("Supplied regex: \"" + regex + "\"");

        do {
            if (!response.equals("default")) {
                if (response.equals("exit")) {
                    forceEndGame("User typed \"exit\".");
                }
                print("\"" + response + "\" is not a valid choice. Use either the first letter or the whole word of the command you want to run. " + prompt);
            } else {
                print(prompt);
            }
            response = s.nextLine().trim().toLowerCase();
            log.debug("rawish response is \"" + response + "\"");

        } while (!Pattern.matches(regex, response));
        log.debug("response was accepted as a regex match, returning as simplified version");
        String simplified = simplifyUserResponse(response);
        return simplified;
    }

    private static void print(String message) {
        log.log("Printing to screen: \"" + message + "\"", -1);
        System.out.print(message);
    }

    private static void println(String message) {
        print(message + "\n");
    }

    private static void displayAndReceiveChoices(UNOEngine e) {
        boolean hasPlus2 = e.getCurrentHand().hasPlus2();
        boolean hasPlus4 = e.getCurrentHand().hasPlus4();
        boolean hasPlus = e.getCurrentHand().hasPlus();
        print("P" + (e.getCurrentPlayer() + 1) + ": "); // for player 1, prints: "P1: "
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
                    println(out);

                    String response = getResponse("Valid responses: (d)raw or (p)lay: ", generateRegexForHand(e.getCurrentPlayerMatches())); // regex for drawing or playing, valid responses include, but are certainly not limited to: draw, d, play red 3, pr3, p r3, play +2, play wild, pw, and so on and so forth
                    if (response.equals("d")) {
                        doPendingDraw(e);
                    } else if (response.charAt(0) == 'p') {
                        UNOCard chosen = parseCompactPlayCommand(response);
                        if (chosen.isPlus4()) {
                            e.playCurrentPlayersWildCard(UNOCard.PLUS4, response);
                        } else {
                            e.playCurrentPlayersNonWildCard(chosen);
                        }
                    }
                } else {
                    println("Automatically drew " + e.getPendingCardsToDraw() + " cards, given no other option.");
                    e.receivePendingCards();
                }
            } else {
                if (e.isRULE_STACKING_SAME()) {
                    if (e.getNum2CardsToDraw() > 0) {
                        if (e.getCurrentHand().hasPlus2()) {
                            println("Your choices are to draw " + e.getPendingCardsToDraw() + " cards or to defer to the next player along with an additional +2 card from your hand on top.");
                        } else {
                            e.receivePendingCards();
                        }
                    } else {
                        if (e.getCurrentHand().hasPlus4()) {
                            println("Your choices are to draw " + e.getPendingCardsToDraw() + " cards or to defer to the next player along with an additional +4 card from your hand on top.");
                        } else {
                            e.receivePendingCards();
                        }
                    }
                } else {
                    e.receivePendingCards();
                }
            }
        } else {
            ArrayList<UNOCard> options = e.getCurrentPlayerMatches();
            String tempDebugOptions = "";
            for (UNOCard c : options) {
                tempDebugOptions += c.toString() + " ";
            }
            log.debug("Options for P" + (e.getCurrentPlayer() + 1) + " are \"" + tempDebugOptions + "\".");
            if (options.size() > 1) {
                print("The cards you can play are: ");
            } else if (options.isEmpty()) {
                print("You cannot play any cards. ");
                if (e.isRULE_DRAW_TILL_PLAYABLE()) {
                    println("You must draw until you have a playable card.");
                    while (e.getCurrentPlayerMatches().isEmpty()) {
                        doDrawCard(e);
                        displayAndReceiveChoices(e);
                    }
                    displayHand(e);
                } else {
                    println("You must draw a card.");
                    doDrawCard(e);
                }
            } else {
                print("The only card you can play is: ");
            }
            options = e.getCurrentPlayerMatches();
            for (int i = 0; i < options.size() - 1; i++) {
                print(options.get(i).toString());
                if (options.size() > 2) {
                    print(", ");
                }
            }
            if (options.size() > 1) {
                if (options.size() == 2) {
                    print(" ");
                }
                print("and ");
            }
            println(options.get(options.size() - 1) + ".");
            String response = getResponse("Will you (d)raw or (p)lay a card? ", generateRegexForHand(e.getCurrentPlayerMatches()));
            if (response.charAt(0) == 'd') {
                doDrawCard(e);
                displayHand(e);
                return;
            } else if (response.charAt(0) == 'p') {
                log.debug("P" + (e.getCurrentPlayer() + 1) + "'s response: \"" + response + "\"");
                UNOCard chosen = parseCompactPlayCommand(response);
                if (chosen.isWild()) {
                    println("You've played a wild card which means you get to choose the new color to match.");
                    e.playCurrentPlayersWildCard(chosen, getNewColor());
                } else {
                    e.playCurrentPlayersNonWildCard(chosen);
                }
            }
        }
    }

    private static String getNewColor() {
        String response = "default";
        Scanner s = new Scanner(System.in);
        do {
            println("Enter the new color, (b)lue, (g)reen, (r)ed, or (y)ellow: ");
            response = s.nextLine().trim().toLowerCase();
            log.debug("response to getNewColor(): \"" + response + "\"");
        } while ("bgry".indexOf(response.charAt(0)) == -1); // while the first char of the response is not one of the valid colors, ask for a valid response
        switch (response.charAt(0)) {
            case 'b':
                return "blue";
            case 'g':
                return "green";
            case 'r':
                return "red";
            case 'y':
                return "yellow";
        }
        return response;
    }

    private static UNOCard parseCompactPlayCommand(String cmd) {
        switch (cmd) {
            case "pb0": // Thank you again, Dennis: https://bit.ly/2JOoayC
                return UNOCard.BLUE0;
            case "pb1":
                return UNOCard.BLUE1;
            case "pb2":
                return UNOCard.BLUE2;
            case "pb3":
                return UNOCard.BLUE3;
            case "pb4":
                return UNOCard.BLUE4;
            case "pb5":
                return UNOCard.BLUE5;
            case "pb6":
                return UNOCard.BLUE6;
            case "pb7":
                return UNOCard.BLUE7;
            case "pb8":
                return UNOCard.BLUE8;
            case "pb9":
                return UNOCard.BLUE9;
            case "pbs":
                return UNOCard.BLUESKIP;
            case "pbr":
                return UNOCard.BLUEREVERSE;
            case "pb+2":
                return UNOCard.BLUEPLUS2;
            case "pg0":
                return UNOCard.GREEN0;
            case "pg1":
                return UNOCard.GREEN1;
            case "pg2":
                return UNOCard.GREEN2;
            case "pg3":
                return UNOCard.GREEN3;
            case "pg4":
                return UNOCard.GREEN4;
            case "pg5":
                return UNOCard.GREEN5;
            case "pg6":
                return UNOCard.GREEN6;
            case "pg7":
                return UNOCard.GREEN7;
            case "pg8":
                return UNOCard.GREEN8;
            case "pg9":
                return UNOCard.GREEN9;
            case "pgs":
                return UNOCard.GREENSKIP;
            case "pgr":
                return UNOCard.GREENREVERSE;
            case "pg+2":
                return UNOCard.GREENPLUS2;
            case "pr0":
                return UNOCard.RED0;
            case "pr1":
                return UNOCard.RED1;
            case "pr2":
                return UNOCard.RED2;
            case "pr3":
                return UNOCard.RED3;
            case "pr4":
                return UNOCard.RED4;
            case "pr5":
                return UNOCard.RED5;
            case "pr6":
                return UNOCard.RED6;
            case "pr7":
                return UNOCard.RED7;
            case "pr8":
                return UNOCard.RED8;
            case "pr9":
                return UNOCard.RED9;
            case "prs":
                return UNOCard.REDSKIP;
            case "prr":
                return UNOCard.REDREVERSE;
            case "pr+2":
                return UNOCard.REDPLUS2;
            case "py0":
                return UNOCard.YELLOW0;
            case "py1":
                return UNOCard.YELLOW1;
            case "py2":
                return UNOCard.YELLOW2;
            case "py3":
                return UNOCard.YELLOW3;
            case "py4":
                return UNOCard.YELLOW4;
            case "py5":
                return UNOCard.YELLOW5;
            case "py6":
                return UNOCard.YELLOW6;
            case "py7":
                return UNOCard.YELLOW7;
            case "py8":
                return UNOCard.YELLOW8;
            case "py9":
                return UNOCard.YELLOW9;
            case "pys":
                return UNOCard.YELLOWSKIP;
            case "pyr":
                return UNOCard.YELLOWREVERSE;
            case "py+2":
                return UNOCard.YELLOWPLUS2;
            case "pw":
                return UNOCard.WILD;
            case "p+4":
                return UNOCard.PLUS4; // Welcome to the shadow realm, Jimbo.
        }
        return null;
    }

    private static ArrayList<UNOCard> doPendingDraw(UNOEngine e) {
        ArrayList<UNOCard> out = e.drawCurrentPlayerCards(e.getPendingCardsToDraw());
        for (UNOCard c : out) {
            println("Drew a " + c.toString() + ".");
        }
        return out;
    }

    private static UNOCard doDrawCard(UNOEngine e) {
        UNOCard out = e.drawCurrentPlayerCards(1).get(0);
        println("Drew a " + out.toString() + ".");
        return out;
    }

    private static void displayPlayableCards(UNOEngine e) {
        ArrayList<UNOCard> options = e.getCurrentPlayerMatches();
        for (int i = 0; i < options.size() - 1; i++) {
            print(options.get(i).toString());
            if (options.size() > 2) {
                print(", ");
            }
        }
        if (options.size() > 1) {
            if (options.size() == 2) {
                print(" ");
            }
            print("and ");
        }
        println(options.get(options.size() - 1) + ".");
    }

    private static String simplifyUserResponse(String userResponse) {
        String compactUserResponse = userResponse;
        log.debug("simplifyUserResponse() received \"" + userResponse + "\" as the only argument, proceeding with abbreviations and removing whitespace.");
        compactUserResponse = compactUserResponse.replaceFirst("\\b(p|play)\\s*\\b", "p");
        compactUserResponse = compactUserResponse.replaceFirst("\\b(d|draw)\\s*\\b", "d");
        compactUserResponse = compactUserResponse.replaceFirst("\\b(b|blue)\\s*\\b", "b");
        compactUserResponse = compactUserResponse.replaceFirst("\\b(g|green)\\s*\\b", "g");
        compactUserResponse = compactUserResponse.replaceFirst("\\b(r|red)\\s*\\b", "r");
        compactUserResponse = compactUserResponse.replaceFirst("\\b(y|yellow)\\s*\\b", "y");
        compactUserResponse = compactUserResponse.replaceFirst("\\b(\\+|plus)\\s*\\b", "+");
        compactUserResponse = compactUserResponse.replaceFirst("\\b(two)\\s*\\b", "2");
        compactUserResponse = compactUserResponse.replaceFirst("\\b(four)\\s*\\b", "4");
        log.debug("simplifyUserResponse() has finished abbreviating and will be returning a final result of \"" + compactUserResponse + "\"");
        return compactUserResponse;
    }

    private static void displayHand(UNOEngine e) {
        UNOHand current = e.getCurrentHand();
        println("Player " + (e.getCurrentPlayer() + 1) + " " + current.toString());
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
        String plus2SubPartition = "";

        for (int i = 0; i < contents.length() / 2; i++) {
            String card = contents.substring(i * 2, i * 2 + 2);
            char color = card.charAt(0);
            char type = card.charAt(1);
            if (color == 'w') {
                wilds = wilds + type;
            } else {
                if (type == '+') {
                    plus2SubPartition = "|(\\+2|\\+)";
                }
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
                playPartition += "((" + actualColor.charAt(0) + "|" + actualColor + ")\\s*([" + colorSet.replaceAll("\\+", "") + "]";
                playPartition = playPartition.replaceAll("\\[\\]", "");
                if (colorSet.contains("s")) {
                    playPartition += "|skip";
                }
                if (colorSet.contains("r")) {
                    playPartition += "|reverse";
                }
                playPartition += plus2SubPartition;
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

    private static UNOHand generateHand(int handSize) {
        UNODefaultDeck deck = new UNODefaultDeck();
        deck.setNumPlayers(1);
        deck.shuffleDrawPile();
        deck.dealCards(handSize);
        return deck.getCurrentHand(0);
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

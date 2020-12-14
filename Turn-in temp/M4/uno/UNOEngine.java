package uno;

import java.util.ArrayList;

/**
 *
 * @author Tim Barber
 */
public class UNOEngine {

    private final boolean RULE_STACKING_SAME;
    private final boolean RULE_STACKING_ALL;
    private final boolean RULE_SKIP_AFTER_DRAW;
    private final boolean RULE_DRAW_TILL_PLAYABLE;
    private final int HAND_SIZE;

    private final int NUM_PLAYERS;

    private UNODeck deck;
    private int currentPlayer = 0; // current Player is from 0 - (number of players - 1)
    private int direction = 1; // if reversed, changed to -1

    private int num2CardsToDraw = 0; // counts how many +2's are played in a row
    private int num4CardsToDraw = 0; // counts how many +4's are played in a row
    private boolean drewCards = false;
    private boolean playedCard = false;

    public UNOEngine(int players) {
        NUM_PLAYERS = players;
        RULE_STACKING_SAME = true;
        RULE_STACKING_ALL = RULE_STACKING_SAME && true;
        RULE_DRAW_TILL_PLAYABLE = true;
        RULE_SKIP_AFTER_DRAW = true;
        HAND_SIZE = 7;
        deck = new UNODefaultDeck();
    }

    public UNOEngine(int players, boolean ruleStackingSame, boolean ruleStackingAll, boolean ruleDrawTillPlayable, boolean ruleSkipAfterDraw, int handSize, UNODeck deck) {
        NUM_PLAYERS = players;
        RULE_STACKING_SAME = ruleStackingSame;
        RULE_STACKING_ALL = ruleStackingSame && ruleStackingAll;
        RULE_DRAW_TILL_PLAYABLE = ruleDrawTillPlayable;
        RULE_SKIP_AFTER_DRAW = ruleSkipAfterDraw;
        HAND_SIZE = handSize;
        this.deck = deck;
    }

    public boolean isRULE_DRAW_TILL_PLAYABLE() {
        return RULE_DRAW_TILL_PLAYABLE;
    }

    public boolean isRULE_SKIP_AFTER_DRAW() {
        return RULE_SKIP_AFTER_DRAW;
    }

    public boolean isRULE_STACKING_ALL() {
        return RULE_STACKING_ALL;
    }

    public boolean isRULE_STACKING_SAME() {
        return RULE_STACKING_SAME;
    }

    public void prepareGame() {
        deck.shuffleDrawPile();
        deck.setNumPlayers(NUM_PLAYERS);
        deck.dealCards(HAND_SIZE);
    }

    public void beginGame() {
        UNOCard result;
        do {
            result = deck.flipTopCard();
        } while (deck.getTopDiscardCard().isWild());
        if (result.isReverse()) {
            println("First card is a reverse, order of play has been reversed.");
            direction = -direction;
        } else if (result.isPlus2()) { // according to UNO rules, if a card is wild you just skip it and do the next one, so no need for a +4 scenario
            num2CardsToDraw++;
        } else if (result.isSkip()) {
            println("First card is a skip, skipping P1.");
            assignNextPlayer();
        }
        flush();
    }

    private void flush() {
        UNOConsoleDriver.log.flushCCBuffer();
    }

    private void printFlush(String message) {
        print(message);
        flush();
    }

    private void print(String message) {
        UNOConsoleDriver.log.ccBuffer(message.replaceAll("\n", " // "));
        System.out.print(message);
    }

    private void println(String message) {
        print(message + "\n");
    }

    private void debug(String message) {
        UNOConsoleDriver.log.debug(message);
    }

    public int[] getPlayersBySmallestHand() { // FIXME not done
        int[] out = new int[NUM_PLAYERS];
        for (int i = 0; i < NUM_PLAYERS; i++) {
            out[i] = deck.getHandSizes()[i];
        }
        return out;
    }

    public void assignNextPlayer() {
        debug("Current player 0 indexed is " + currentPlayer);
        currentPlayer += direction;
        debug("Current player 0 indexed is now " + currentPlayer);
        //currentPlayer = currentPlayer % NUM_PLAYERS;
        // { <- encompassed in these brackets is the replacement for the commented out line above
        while (currentPlayer < 0) {
            currentPlayer += NUM_PLAYERS;
        }
        while (currentPlayer > NUM_PLAYERS - 1) {
            currentPlayer -= NUM_PLAYERS;
        }
        // }
        debug("Current player 0 indexed is now " + currentPlayer);
        drewCards = false;
        playedCard = false;

        if ((num2CardsToDraw > 0 || num4CardsToDraw > 0) && !RULE_STACKING_SAME) {
            deck.drawCardsForPlayer(currentPlayer, getPendingCardsToDraw());
            drewCards = true;
        }
    }

    public boolean hasCurrentPlayerDrawnOrPlayed() {
        return drewCards || playedCard;
    }

    public ArrayList<UNOCard> drawCurrentPlayerCards(int amt) {
        ArrayList<UNOCard> out = deck.drawCardsForPlayer(this.currentPlayer, amt);
        drewCards = true;
        return out;
    }

    public UNOHand getCurrentHand() {
        return deck.getCurrentHand(currentPlayer);
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public UNOCard getTopCard() {
        return deck.getTopDiscardCard();
    }

    public String getCurrentColor() {
        return deck.getCurrentColor();
    }

    public boolean getDrewCards() {
        return drewCards;
    }

    public ArrayList<UNOCard> receivePendingCards() {
        ArrayList<UNOCard> out = deck.drawCardsForPlayer(currentPlayer, getPendingCardsToDraw());
        drewCards = true;
        num2CardsToDraw = 0;
        num4CardsToDraw = 0;
        if (RULE_SKIP_AFTER_DRAW) {
            assignNextPlayer();
        }
        return out;
    }

    public ArrayList<UNOCard> getCurrentPlayerMatches() {
        ArrayList<UNOCard> out = new ArrayList<>();
        if (getPendingCardsToDraw() > 0) {
            if (this.RULE_STACKING_ALL) {
                if (deck.getCurrentHand(currentPlayer).hasPlus()) {
                    for (UNOCard c : deck.getCurrentHand(currentPlayer).toArrayList()) {
                        if (c.isPlus()) {
                            out.add(c);
                        }
                    }
                } else {
                    return null;
                }
            } else if (this.RULE_STACKING_SAME) {
                if (deck.getCurrentHand(currentPlayer).hasType(getTopCard())) {
                    for (UNOCard c : deck.getCurrentHand(currentPlayer).toArrayList()) {
                        if (c.getType().equals(getTopCard().getType())) {
                            out.add(c);
                        }
                    }
                } else {
                    return null;
                }
            }
        } else {
            for (UNOCard c : deck.getCurrentHand(currentPlayer).toArrayList()) {
                if (c.getColor().equals(getCurrentColor())) {
                    out.add(c);
                } else if (c.isValid(getTopCard())) {
                    out.add(c);
                }
            }
        }
        return out;
    }

    public int getPendingCardsToDraw() {
        return num2CardsToDraw * 2 + num4CardsToDraw * 4;
    }

    public int getNum2CardsToDraw() {
        return num2CardsToDraw;
    }

    public int getNum4CardsToDraw() {
        return num4CardsToDraw;
    }

    public boolean hasAPlayerEmptiedHand() {
        for (int i = 0; i < NUM_PLAYERS; i++) {
            if (deck.getCurrentHand(i).getSize() == 0) {
                return true;
            }
        }
        return false;
    }

    public void assessPileSizes() {
        if (deck.getDrawPileSize() < 30) {
            UNOConsoleDriver.log.log("Reincorporating discard pile into draw pile, as size is under 30, drawPile size is " + deck.getDrawPileSize() + ".");
            deck.reincorporateDiscardPile();
            UNOConsoleDriver.log.log("Reincorporated discard pile into draw pile, drawPile size is now " + deck.getDrawPileSize() + ".");
        }
    }

    public boolean playCurrentPlayersNonWildCard(UNOCard card) {
        if (getPendingCardsToDraw() != 0 && card.isPlus2()) {
            if (RULE_STACKING_SAME && num2CardsToDraw > 0) {
                num2CardsToDraw++;
            } else if (RULE_STACKING_ALL) {
                num2CardsToDraw++;
            }
        } else if (getPendingCardsToDraw() > 0 && !card.isPlus2()) {
            return false;
        }

        if (card.isWild()) {
            return false;
        } else {
            if (deck.playNonWildCard(currentPlayer, card)) {
                playedCard = true;
                println("P" + (getCurrentPlayer() + 1) + " played " + card.toString());
                if (card.isSkip()) {
                    println("Player " + ((getCurrentPlayer() + direction) % NUM_PLAYERS + 1) + " has been skipped.");
                    currentPlayer++;
                } else if (card.isReverse()) {
                    println("Order of play has been reversed.");
                    if (NUM_PLAYERS > 2) {
                        direction = -direction;
                    } else {
                        currentPlayer++;
                    }
                } else if (card.isPlus2() && getPendingCardsToDraw() == 0) {
                    num2CardsToDraw++;
                }
                return true;
            }

        }
        return false;
    }

    // returns 1-indexed winner
    public int getWinner() {
        for (int i : deck.getHandSizes()) {
            if (i == 0) {
                return i + 1;
            }
        }
        return -1; // shouldn't happen...hopefully
    }

    public boolean playCurrentPlayersWildCard(UNOCard card, String newColor) {
        if (getPendingCardsToDraw() > 0 && card.isPlus4()) {
            if (RULE_STACKING_SAME && num4CardsToDraw > 0) {
                num4CardsToDraw++;
            } else if (RULE_STACKING_ALL) {
                num4CardsToDraw++;
            }
        } else if (getPendingCardsToDraw() > 0 && !card.isPlus4()) {
            return false;
        }
        if (!card.isWild()) {
            return false;
        } else {
            if (deck.playWildCard(currentPlayer, card, newColor)) {
                playedCard = true;
                println("P" + (getCurrentPlayer() + 1) + " played " + card.toString());
                println("The new color is now " + deck.getCurrentColor());
                if (card.isPlus4() && getPendingCardsToDraw() == 0) {
                    num4CardsToDraw++;
                }
                return true;
            }
        }

        return false;
    }

    public String getState() {
        String out = "State of UNOEngine:\n";
        out += "House rules:\n";
        out += "\tAllow stacking of same plus cards, +2 on +2 and +4 on +4: " + RULE_STACKING_SAME + "\n";
        out += "\tAllow stacking of all plus cards, +2 on +4 and v.v.: " + RULE_STACKING_ALL + "\n";
        out += "\tIs player's turn revoked after being forced to draw via a +2 or +4 card: " + RULE_SKIP_AFTER_DRAW + "\n";
        out += "\tWhen a player has no playable cards, are they forced to draw until they do? If not they only draw one card: " + RULE_DRAW_TILL_PLAYABLE + "\n";
        out += "\tInitial hand size: " + HAND_SIZE + "\n";
        out += "Number of players: " + NUM_PLAYERS + "\n";
        out += "Current player, 0-indexed: " + currentPlayer + "\n";
        out += "Direction of play, should either be 1 or -1: " + direction + "\n";
        out += "Number of +2 cards pending a draw by the next player: " + num2CardsToDraw + "\n";
        out += "Number of +4 cards pending a draw by the next player: " + num4CardsToDraw + "\n";
        out += "Total number of cards that need to be drawn: " + (num2CardsToDraw * 2 + num4CardsToDraw * 4) + "\n";
        out += "Has current player drawn any cards this round: " + drewCards + "\n";
        out += "Has the current player played any cards this round: " + playedCard + "\n";

        return out;
    }
}

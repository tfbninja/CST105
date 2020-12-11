package uno;

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
        UNOCard result = deck.flipTopCard();
        if (result.isReverse()) {
            direction = -direction;
        } else if (result.isPlus2()) {
            num2CardsToDraw++;
        } else if (result.isPlus4()) {
            num4CardsToDraw++;
        } else if (result.isSkip()) {
            assignNextPlayer();
        }
    }

    public void assignNextPlayer() {
        currentPlayer += direction;
        drewCards = false;
        if (currentPlayer >= NUM_PLAYERS) {
            currentPlayer = 0;
        }
        if ((num2CardsToDraw > 0 || num4CardsToDraw > 0) && !RULE_STACKING_SAME) {
            deck.drawCardsForPlayer(currentPlayer, getPendingCardsToDraw());
            drewCards = true;
        }
    }

    public void drawCurrentPlayerCards(int amt) {
        deck.drawCardsForPlayer(this.currentPlayer, amt);
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

    public void receivePendingCards() {
        deck.drawCardsForPlayer(currentPlayer, getPendingCardsToDraw());
        drewCards = true;
        num2CardsToDraw = 0;
        num4CardsToDraw = 0;
        if (RULE_SKIP_AFTER_DRAW) {
            assignNextPlayer();
        }
    }

    public int getPendingCardsToDraw() {
        return num2CardsToDraw + num4CardsToDraw;
    }

    public int getNum2CardsToDraw() {
        return num2CardsToDraw;
    }

    public int getNum4CardsToDraw() {
        return num4CardsToDraw;
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
                if (card.isSkip()) {
                    currentPlayer++;
                } else if (card.isReverse()) {
                    direction = -direction;
                } else if (card.isPlus2() && getPendingCardsToDraw() == 0) {
                    num2CardsToDraw++;
                }
                return true;
            }

        }
        return false;
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
                if (card.isPlus4() && getPendingCardsToDraw() == 0) {
                    num4CardsToDraw++;
                }
                return true;
            }
        }

        return false;
    }

}

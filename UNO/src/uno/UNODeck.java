package uno;

import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author Tim Barber
 */
public class UNODeck {

    private ArrayList<UNOCard> drawPile;
    private ArrayList<UNOCard> discardPile;
    private ArrayList<UNOHand> hands;

    private String currentColor = "";

    public UNODeck() {
        drawPile = new ArrayList<>();
        discardPile = new ArrayList<>();
        hands = new ArrayList<>();
    }

    public void addCardsToDrawPile(ArrayList<UNOCard> cards) {
        for (UNOCard card : cards) {
            addCardToDrawPile(card);
        }
    }

    public void addCardToDrawPile(UNOCard card) {
        drawPile.add(card);
    }

    public void reincorporateDiscardPile() {
        UNOCard topCard = removeTopDiscardCard();
        Collections.shuffle(discardPile);
        while (discardPile.size() > 0) {
            drawPile.add(0, removeTopDiscardCard());
        }
        discardPile.add(topCard);
    }

    public UNOHand getCurrentHand(int player) {
        return hands.get(player);
    }

    public void drawCardsForPlayer(int player, int amt) {
        for (int i = 0; i < amt; i++) {
            hands.get(player).receiveCard(removeTopDiscardCard());
        }
    }

    public UNOCard getTopDiscardCard() {
        return discardPile.get(discardPile.size() - 1);
    }

    public String getCurrentColor() {
        return currentColor;
    }

    public void shuffleDrawPile() {
        Collections.shuffle(drawPile);
    }

    public boolean playNonWildCard(int playerNum, UNOCard card) {
        UNOCard temp = hands.get(playerNum).playCard(card, currentColor);
        if (temp != null) {
            if (card.isValid(getTopDiscardCard(), currentColor)) {
                discardPile.add(temp);
                return true;
            }
        }
        return false;
    }

    public boolean playWildCard(int playerNum, UNOCard card, String newColor) {
        UNOCard temp = hands.get(playerNum).playCard(card, currentColor);
        if (temp != null) {
            if (card.isValid(getTopDiscardCard(), currentColor)) {
                discardPile.add(temp);
                currentColor = newColor;
                return true;
            }
        }
        return false;
    }

    public void setNumPlayers(int amt) {
        hands.clear();
        for (int i = 0; i < amt; i++) {
            hands.add(new UNOHand());
        }
    }

    public void dealCards(int numCardsPerHand) {
        for (int count = 0; count < numCardsPerHand; count++) {
            for (int player = 0; player < hands.size(); player++) {
                hands.get(player).receiveCard(removeTopDrawCard());
            }
        }
    }

    public UNOCard removeTopDiscardCard() {
        UNOCard topCard = discardPile.get(discardPile.size() - 1);
        discardPile.remove(discardPile.size() - 1);
        return topCard;
    }

    public UNOCard removeTopDrawCard() {
        UNOCard topCard = drawPile.get(drawPile.size() - 1);
        drawPile.remove(drawPile.size() - 1);
        return topCard;
    }

    public UNOCard getTopDrawCard() {
        return drawPile.get(drawPile.size() - 1);
    }

    public UNOCard flipTopCard() {
        do {
            discardPile.add(removeTopDrawCard());
        } while (getTopDiscardCard().isWild());
        return discardPile.get(discardPile.size() - 1);
    }
}

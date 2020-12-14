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

    public int getDrawPileSize() {
        return drawPile.size();
    }

    public UNOHand getCurrentHand(int player) {
        return hands.get(player);
    }

    public int[] getHandSizes() {
        int[] handSizesOrderedByPlayerNum = new int[hands.size()];
        for (int i = 0; i < hands.size(); i++) {
            handSizesOrderedByPlayerNum[i] = hands.get(i).getSize();
        }
        return handSizesOrderedByPlayerNum;
    }

    public ArrayList<UNOCard> drawCardsForPlayer(int player, int amt) {
        ArrayList<UNOCard> out = new ArrayList<>();
        for (int i = 0; i < amt; i++) {
            out.add(removeTopDrawCard());
            hands.get(player).receiveCard(out.get(out.size() - 1));
        }
        return out;
    }

    public UNOCard getTopDiscardCard() {
        return discardPile.get(discardPile.size() - 1);
    }

    public String getCurrentColor() {
        return currentColor;
    }

    public void shuffleDrawPile() {
        UNOConsoleDriver.log.log("Shuffling deck.");
        Collections.shuffle(drawPile);
    }

    public boolean playNonWildCard(int playerNum, UNOCard card) {
        UNOCard temp = hands.get(playerNum).playCard(card);
        if (temp != null) {
            discardPile.add(temp);
            return true;
        }
        return false;
    }

    public boolean playWildCard(int playerNum, UNOCard card, String newColor) {
        UNOCard temp = hands.get(playerNum).playCard(card);
        if (temp != null) {
            discardPile.add(temp);
            currentColor = newColor;
            return true;
        }
        UNOConsoleDriver.log.log("UNODeck received null card when playing card to a temp variable in playWildCard(), toString() of arg: \"" + card.toString() + "\", toString() of temp: \"" + temp.toString() + "\"");
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
        discardPile.add(removeTopDrawCard());
        return discardPile.get(discardPile.size() - 1);
    }

    public String getState() {
        String out = "State of UNODeck:\n";
        out += "Draw pile:\n";
        out += "\tsize: " + drawPile.size() + "\n";
        out += "\tcontents: ";
        for (UNOCard c : drawPile) {
            out += c.toString() + " ";
        }
        out += "\n";
        out += "Discard pile:\n";
        out += "\tsize: " + discardPile.size() + "\n";
        out += "\tcontents: ";
        for (UNOCard c : discardPile) {
            out += c.toString() + " ";
        }
        out += "\n";
        out += "Hands:\n";
        int index = 0;
        for (UNOHand h : hands) {
            out += "\tHand " + index + ":\n";
            out += "\t\tsize: " + h.getSize() + "\n";
            out += "\t\tcontents: ";
            for (UNOCard c : h.toArrayList()) {
                out += c.toString() + " ";
            }
            out += "\n";
            index++;
        }
        out += "current color: " + currentColor + "\n";
        return out;
    }
}

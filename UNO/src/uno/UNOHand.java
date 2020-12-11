package uno;

import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author Tim Barber
 */
public class UNOHand {

    private ArrayList<UNOCard> hand;

    public UNOHand() {
        hand = new ArrayList<>();
    }

    public int getSize() {
        return hand.size();
    }

    public boolean hasMatch(UNOCard card, String currentcolor) {
        for (UNOCard c : hand) {
            if (c.isValid(card, currentcolor)) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<UNOCard> getMatches(UNOCard card, String currentcolor) {
        ArrayList<UNOCard> out = new ArrayList<>();
        for (UNOCard c : hand) {
            if (c.isValid(card, currentcolor)) {
                out.add(c);
            }
        }
        return out;
    }

    public boolean hasSkip() {
        for (UNOCard c : hand) {
            if (c.isSkip()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasReverse() {
        for (UNOCard c : hand) {
            if (c.isReverse()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasPlus() {
        return hasPlus2() || hasPlus4();
    }

    public boolean hasPlus2() {
        for (UNOCard c : hand) {
            if (c.isPlus2()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasPlus4() {
        for (UNOCard c : hand) {
            if (c.isPlus4()) {
                return true;
            }
        }
        return false;
    }

    public void receiveCard(UNOCard card) {
        hand.add(card);
    }

    public UNOCard playCard(UNOCard currentCard, String currentColor) {
        boolean cardFound = false;
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).isValid(currentCard, currentColor)) {
                cardFound = true;
                hand.remove(i);
            }
        }
        if (!cardFound) {
            return null;
        }
        return currentCard;
    }

    public void sortByColor() {
        Collections.sort(hand);
    }

    @Override
    public String toString() {
        sortByColor();
        String out = "Contents of hand: ";
        for (int i = 0; i < hand.size() - 1; i++) {
            out += hand.get(i).toString() + ", ";
        }
        out += hand.get(hand.size() - 1).toString();
        return out;
    }
}

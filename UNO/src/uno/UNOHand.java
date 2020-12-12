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

    /*
     * see the desc for UNOCard.isValid(UNOCard)
     */
    public boolean hasMatch(UNOCard card) {
        for (UNOCard c : hand) {
            if (c.isValid(card)) {
                return true;
            }
        }
        return false;
    }

    /*
     * see the desc for UNOCard.isValid(UNOCard)
     */
    public ArrayList<UNOCard> getMatches(UNOCard card) {
        ArrayList<UNOCard> out = new ArrayList<>();
        for (UNOCard c : hand) {
            if (c.isValid(card)) {
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

    public void receiveCards(ArrayList<UNOCard> cards) {
        for (UNOCard card : cards) {
            hand.add(card);
        }
    }

    public boolean hasType(UNOCard card) {
        for (UNOCard c : hand) {
            if (c.getType().equals(card.getType())) {
                return true;
            }
        }
        return false;
    }

    public UNOCard playCard(UNOCard currentCard) {
        boolean cardFound = false;
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).isSameAs(currentCard)) {
                cardFound = true;
                hand.remove(i);
                break;
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

    public ArrayList<UNOCard> toArrayList() {
        return hand;
    }


    /*
     * Returns a string with 2 characters per card, no spaces, so for a hand of
     * a blue 1, blue 3, green 0, red 2, yellow0, yellow skip, and +4, the
     * output would be "b1b3g0r2y0ysw+", the plus 2 and plus 4 cards can be
     * differentiated by the fact that the "color" of a plus 2 card is an actual
     * color, while the "color" of a +4 card is "wild."
     */
    public String toCompactString() {
        sortByColor();
        String out = "";
        for (UNOCard c : hand) {
            out += c.getColor().charAt(0);
            out += c.getType().charAt(0);
        }
        return out;
    }

    public static String toCompactString(ArrayList<UNOCard> cards) {
        UNOHand temp = new UNOHand();
        temp.receiveCards(cards);
        return temp.toCompactString();
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

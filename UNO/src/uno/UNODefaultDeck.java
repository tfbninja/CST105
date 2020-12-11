package uno;

import java.util.ArrayList;

/**
 *
 * @author Tim Barber
 */
public class UNODefaultDeck extends UNODeck {

    private ArrayList<UNOCard> defaultCards = new ArrayList<>();

    public UNODefaultDeck() {
        String[] colors = {"blue", "red", "green", "yellow"};
        for (String c : colors) {
            defaultCards.add(new UNOCard(c, "0"));
            for (int i = 0; i < 2; i++) {
                for (int n = 0; n < 10; n++) {
                    defaultCards.add(new UNOCard(c, String.valueOf(n)));
                }
                defaultCards.add(new UNOCard(c, "+2"));
                defaultCards.add(new UNOCard(c, "skip"));
                defaultCards.add(new UNOCard(c, "reverse"));
            }
        }
        for (int i = 0; i < 4; i++) {
            defaultCards.add(new UNOCard("wild", "wild"));
            defaultCards.add(new UNOCard("wild", "+4"));
        }
        addCardsToDrawPile(defaultCards);
    }
}

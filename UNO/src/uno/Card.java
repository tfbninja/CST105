package uno;

/**
 *
 * @author Tim Barber
 */
public class Card {

    private String color;
    private String type;
    private final String[] COLORS = {"blue", "red", "green", "yellow", "wild"};
    private final String[] TYPES = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "skip", "reverse", "wild", "+2", "+4"};

    public Card(String color, String type) {
        this.color = color;
        this.type = type;
    }

    public String[] getAllColors() {
        return COLORS;
    }

    public String[] getAllTypes() {
        return TYPES;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isValid(Card bottom, Card top, String currentColor) {
        if (top.getColor().equals("wild")) {
            return true;
        } else if (top.getColor().equals(currentColor)) {
            return true;
        } else if (top.getType().equals(bottom.getType())) {
            return true;
        }
        return false;
    }

}

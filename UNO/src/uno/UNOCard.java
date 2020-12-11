package uno;

/**
 *
 * @author Tim Barber
 */
public class UNOCard implements Comparable {

    private String color;
    private String type;
    private final String[] COLORS = {"blue", "green", "red", "yellow", "wild"};
    private final String[] TYPES = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "skip", "reverse", "wild", "+2", "+4"};

    public UNOCard(String color, String type) {
        this.color = color;
        this.type = type;
    }

    @Override
    public int compareTo(Object o) {
        UNOCard other = (UNOCard) o;
        return getRanking() - other.getRanking();
    }

    public int getRanking() {
        int indexOfColor = 1;
        for (String c : COLORS) {
            if (this.color.equals(c)) {
                break;
            }
            indexOfColor++;
        }
        int indexOfType = 1;
        for (String t : TYPES) {
            if (this.type.equals(t)) {
                break;
            }
            indexOfType++;
        }
        int rank = indexOfColor * 100 + indexOfType;
        return rank;
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

    public boolean isValid(UNOCard otherCard, String currentColor) {
        if (getColor().equals("wild")) {
            return true;
        } else if (getColor().equals(currentColor)) {
            return true;
        } else if (getColor().equals(otherCard.getColor())) {
            return true;
        } else if (getType().equals(otherCard.getType())) {
            return true;
        }
        return false;
    }

    public boolean isNumber() {
        return getType().length() == 1;
    }

    public boolean isSpecial() {
        return !isNumber();
    }

    public boolean isWild() {
        return getColor().equals("wild");
    }

    public boolean isSkip() {
        return getType().equals("skip");
    }

    public boolean isReverse() {
        return getType().equals("reverse");
    }

    public boolean isTurnChanging() {
        return isReverse() || isSkip();
    }

    public boolean isPlus2() {
        return getType().equals("+2");
    }

    public boolean isPlus4() {
        return getType().equals("+4");
    }

    public boolean isPlus() {
        return isPlus2() || isPlus4();
    }

    public boolean isBlue() {
        return getColor().equals("blue");
    }

    public boolean isRed() {
        return getColor().equals("red");
    }

    public boolean isGreen() {
        return getColor().equals("green");
    }

    public boolean isYellow() {
        return getColor().equals("yellow");
    }

    public boolean isColored() {
        // i PrOmIsE i'M nOt RaCiSt
        return !getType().equals("wild");
    }

    @Override
    public String toString() {
        if (isPlus4()) {
            return "+4";
        } else if (isWild()) {
            return "wild";
        }
        return getColor() + " " + getType();
    }
}

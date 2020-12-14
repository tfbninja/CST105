package uno;

/**
 *
 * @author Tim Barber
 */
public class UNOCard implements Comparable {

    private String color;
    private String type;
    public static final String[] REAL_COLORS = {"blue", "green", "red", "yellow"};
    public static final String[] COLORS = {"blue", "green", "red", "yellow", "wild"};
    public static final String[] TYPES = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "skip", "reverse", "wild", "+2", "+4"};
    public static final UNOCard PLUS4 = new UNOCard("wild", "+4");
    public static final UNOCard WILD = new UNOCard("wild", "wild");
    public static final UNOCard BLUE0 = new UNOCard("blue", "0"); // thank you, Dennis
    public static final UNOCard BLUE1 = new UNOCard("blue", "1"); // https://bit.ly/2KfCdNk
    public static final UNOCard BLUE2 = new UNOCard("blue", "2");
    public static final UNOCard BLUE3 = new UNOCard("blue", "3");
    public static final UNOCard BLUE4 = new UNOCard("blue", "4");
    public static final UNOCard BLUE5 = new UNOCard("blue", "5");
    public static final UNOCard BLUE6 = new UNOCard("blue", "6");
    public static final UNOCard BLUE7 = new UNOCard("blue", "7");
    public static final UNOCard BLUE8 = new UNOCard("blue", "8");
    public static final UNOCard BLUE9 = new UNOCard("blue", "9");
    public static final UNOCard BLUESKIP = new UNOCard("blue", "skip");
    public static final UNOCard BLUEREVERSE = new UNOCard("blue", "reverse");
    public static final UNOCard BLUEPLUS2 = new UNOCard("blue", "+2");
    public static final UNOCard GREEN0 = new UNOCard("green", "0");
    public static final UNOCard GREEN1 = new UNOCard("green", "1");
    public static final UNOCard GREEN2 = new UNOCard("green", "2");
    public static final UNOCard GREEN3 = new UNOCard("green", "3");
    public static final UNOCard GREEN4 = new UNOCard("green", "4");
    public static final UNOCard GREEN5 = new UNOCard("green", "5");
    public static final UNOCard GREEN6 = new UNOCard("green", "6");
    public static final UNOCard GREEN7 = new UNOCard("green", "7");
    public static final UNOCard GREEN8 = new UNOCard("green", "8");
    public static final UNOCard GREEN9 = new UNOCard("green", "9");
    public static final UNOCard GREENSKIP = new UNOCard("green", "skip");
    public static final UNOCard GREENREVERSE = new UNOCard("green", "reverse");
    public static final UNOCard GREENPLUS2 = new UNOCard("green", "+2");
    public static final UNOCard RED0 = new UNOCard("red", "0");
    public static final UNOCard RED1 = new UNOCard("red", "1");
    public static final UNOCard RED2 = new UNOCard("red", "2");
    public static final UNOCard RED3 = new UNOCard("red", "3");
    public static final UNOCard RED4 = new UNOCard("red", "4");
    public static final UNOCard RED5 = new UNOCard("red", "5");
    public static final UNOCard RED6 = new UNOCard("red", "6");
    public static final UNOCard RED7 = new UNOCard("red", "7");
    public static final UNOCard RED8 = new UNOCard("red", "8");
    public static final UNOCard RED9 = new UNOCard("red", "9");
    public static final UNOCard REDSKIP = new UNOCard("red", "skip");
    public static final UNOCard REDREVERSE = new UNOCard("red", "reverse");
    public static final UNOCard REDPLUS2 = new UNOCard("red", "+2");
    public static final UNOCard YELLOW0 = new UNOCard("yellow", "0");
    public static final UNOCard YELLOW1 = new UNOCard("yellow", "1");
    public static final UNOCard YELLOW2 = new UNOCard("yellow", "2");
    public static final UNOCard YELLOW3 = new UNOCard("yellow", "3");
    public static final UNOCard YELLOW4 = new UNOCard("yellow", "4");
    public static final UNOCard YELLOW5 = new UNOCard("yellow", "5");
    public static final UNOCard YELLOW6 = new UNOCard("yellow", "6");
    public static final UNOCard YELLOW7 = new UNOCard("yellow", "7");
    public static final UNOCard YELLOW8 = new UNOCard("yellow", "8");
    public static final UNOCard YELLOW9 = new UNOCard("yellow", "9");
    public static final UNOCard YELLOWSKIP = new UNOCard("yellow", "skip");
    public static final UNOCard YELLOWREVERSE = new UNOCard("yellow", "reverse");
    public static final UNOCard YELLOWPLUS2 = new UNOCard("yellow", "+2");

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

    public static String[] getAllColors() {
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

    public boolean isSameAs(UNOCard other) {
        return other.getColor().equals(getColor()) && other.getType().equals(getType());
    }

    /*
     * At first I was making this method have like 3 arguments taken mostly from
     * the UNOEngine, like currentColor and needsToDraw, but I decided that was
     * the wrong way around, UNOCards shouldn't concern themselves with the
     * state of the game, that's up to the Engine. So now this method only
     * reports whether itself would be a valid play on another card, given no
     * context.
     */
    public boolean isValid(UNOCard otherCard) {
        if (getColor().equals("wild")) {
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

    public String toInstruction() {
        if (isPlus4()) {
            return "+4";
        } else if (isPlus2()) {
            return getColor().charAt(0) + "+2";
        } else if (isWild()) {
            return "w";
        }
        return getColor().charAt(0) + "" + getType().charAt(0);
    }

    public String toCompactString() {
        if (isPlus4()) {
            return "+4";

        } else if (isWild()) {
            return "w";
        }
        return getColor().charAt(0) + "" + getType().charAt(0);
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

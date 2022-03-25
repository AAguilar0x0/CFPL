import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public abstract class Quotation {
    public static final char SINGLE0 = '\'';
    // '
    public static final char SINGLE1 = '\u2018';
    // ‘
    public static final char SINGLE2 = '\u2019';
    // ’
    public static final char SINGLE3 = '\u201B';
    // ‛
    public static final char DOUBLE0 = '\"';
    // "
    public static final char DOUBLE1 = '\u201C';
    // “
    public static final char DOUBLE2 = '\u201D';
    // ”
    public static final char DOUBLE3 = '\u201F';
    // ‟

    private static final Set<Character> singleQuotations = new HashSet<Character>();
    private static final Set<Character> doubleQuotations = new HashSet<Character>();

    static {
        for (Field e : Quotation.class.getDeclaredFields()) {
            try {
                if (e.getName().contains("SINGLE"))
                    singleQuotations.add((char) e.get(null));
                else if (e.getName().contains("DOUBLE"))
                    doubleQuotations.add((char) e.get(null));

            } catch (IllegalArgumentException | IllegalAccessException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    }

    public static boolean equalsSingleQuote(char character) {
        return singleQuotations.contains(character);
    }

    public static boolean equalsDoubleQuote(char character) {
        return doubleQuotations.contains(character);
    }
}

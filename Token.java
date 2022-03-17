import java.util.HashMap;

public class Token {
    public static final HashMap<String, TokenType> reservedWords = new HashMap<String, TokenType>() {
        {
            put("AND", TokenType.AND);
            put("OR", TokenType.OR);
            put("NOT", TokenType.NOT);
            put("OUTPUT", TokenType.OUTPUT);
            put("INPUT", TokenType.INPUT);
            put("VAR", TokenType.VAR);
            put("AS", TokenType.AS);
            put("INT", TokenType.INT);
            put("BOOL", TokenType.BOOL);
            put("FLOAT", TokenType.FLOAT);
            put("CHAR", TokenType.CHAR);
            put("START", TokenType.START);
            put("STOP", TokenType.STOP);
            put("IF", TokenType.IF);
            put("ELSE", TokenType.ELSE);
            put("WHILE", TokenType.WHILE);
        }
    };

    public static final HashMap<String, TokenType> reservedCharacters = new HashMap<String, TokenType>() {
        {
            put("(", TokenType.LEFT_PARENTHESIS);
            put(")", TokenType.RIGHT_PARENTHESIS);
            put("[", TokenType.LEFT_BRACE);
            put("]", TokenType.RIGHT_BRACE);
            put(",", TokenType.COMMA);
            put("=", TokenType.ASSIGNMENT);
            put(":", TokenType.COLON);
            put("#", TokenType.OCTOTHORPE);
            put("&", TokenType.AMPERSAND);
            put("+", TokenType.ADDITION);
            put("-", TokenType.SUBTRACTION);
            put("*", TokenType.MULTIPLICATION);
            put("/", TokenType.DIVISION);
            put("%", TokenType.MODULO);
            put(">", TokenType.GREATER);
            put("<", TokenType.LESSER);
        }
    };

    final TokenType type;
    final String lexeme;
    final Object literal;
    final int line;

    public Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    public String toString() {
        return String.format("Token(%s,\"%s\",%s,%d)", type, lexeme, literal, line);
    }
}

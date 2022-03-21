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

    public static final HashMap<TokenType, String> tokenTypeToLexeme = new HashMap<TokenType, String>() {
        {
            put(TokenType.LEFT_PARENTHESIS, "(");
            put(TokenType.RIGHT_PARENTHESIS, ")");
            put(TokenType.LEFT_BRACE, "[");
            put(TokenType.RIGHT_BRACE, "]");
            put(TokenType.COMMA, ",");
            put(TokenType.ASSIGNMENT, "=");
            put(TokenType.COLON, ":");
            put(TokenType.OCTOTHORPE, "#");
            put(TokenType.AMPERSAND, "&");
            put(TokenType.ADDITION, "+");
            put(TokenType.SUBTRACTION, "-");
            put(TokenType.MULTIPLICATION, "*");
            put(TokenType.DIVISION, "/");
            put(TokenType.MODULO, "%");
            put(TokenType.GREATER, ">");
            put(TokenType.LESSER, "<");
            put(TokenType.GREATER_EQUAL, ">=");
            put(TokenType.LESSER_EQUAL, "<=");
            put(TokenType.EQUAL, "==");
            put(TokenType.NOT_EQUAL, "<>");
            put(TokenType.AND, "AND");
            put(TokenType.OR, "OR");
            put(TokenType.NOT, "NOT");
            put(TokenType.OUTPUT, "OUTPUT");
            put(TokenType.INPUT, "INPUT");
            put(TokenType.VAR, "VAR");
            put(TokenType.AS, "AS");
            put(TokenType.INT, "INT");
            put(TokenType.BOOL, "BOOL");
            put(TokenType.FLOAT, "FLOAT");
            put(TokenType.CHAR, "CHAR");
            put(TokenType.START, "START");
            put(TokenType.STOP, "STOP");
            put(TokenType.IF, "IF");
            put(TokenType.ELSE, "ELSE");
            put(TokenType.WHILE, "WHILE");
        }
    };

    public static boolean checkType(TokenType expected, Object actual) {
        switch (expected) {
            case BOOL:
                return "java.lang.Boolean".equals(actual.getClass().getName());
            case CHAR:
                return "java.lang.Character".equals(actual.getClass().getName());
            case FLOAT:
                return "java.lang.Double".equals(actual.getClass().getName());
            case INT:
                return "java.lang.Integer".equals(actual.getClass().getName());
            default:
                return false;
        }
    }

    final TokenType type;
    final String lexeme;
    final Object literal;
    final int line;
    final int column;

    public Token(TokenType type, String lexeme, Object literal, int line, int column) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
        this.column = column;
    }

    public String toString() {
        return String.format("Token(%s,\"%s\",%s,%d)", type, lexeme, literal, line);
    }
}

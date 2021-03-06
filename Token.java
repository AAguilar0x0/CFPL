import java.util.HashMap;
import java.util.HashSet;

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

    public static final HashSet<TokenType> logicalComparisonOperators = new HashSet<TokenType>() {
        {
            add(TokenType.GREATER);
            add(TokenType.LESSER);
            add(TokenType.GREATER_EQUAL);
            add(TokenType.LESSER_EQUAL);
            add(TokenType.EQUAL);
            add(TokenType.NOT_EQUAL);
            add(TokenType.AND);
            add(TokenType.OR);
            add(TokenType.NOT);
        }
    };

    public static boolean checkType(Object value, TokenType... variableType) {
        boolean result = false;
        types: for (TokenType tokenType : variableType) {
            switch (tokenType) {
                case BOOL:
                    if (value instanceof TokenType) {
                        TokenType instance = (TokenType) value;
                        result = instance == TokenType.BOOL || instance == TokenType.BOOL_LIT;
                        break types;
                    }
                    result = value instanceof Boolean;
                    break types;
                case CHAR:
                    if (value instanceof TokenType) {
                        TokenType instance = (TokenType) value;
                        result = instance == TokenType.CHAR || instance == TokenType.CHAR_LIT;
                        break types;
                    }
                    result = value instanceof Character;
                    break types;
                case FLOAT:
                    if (value instanceof TokenType) {
                        TokenType instance = (TokenType) value;
                        result = instance == TokenType.FLOAT || instance == TokenType.FLOAT_LIT;
                        break types;
                    }
                    result = value instanceof Double;
                    break types;
                case INT:
                    if (value instanceof TokenType) {
                        TokenType instance = (TokenType) value;
                        result = instance == TokenType.INT || instance == TokenType.INT_LIT;
                        break types;
                    }
                    result = value instanceof Integer;
                    break types;
                default:
                    result = false;
            }
        }
        return result;
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

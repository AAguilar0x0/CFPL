public enum TokenType {
    LEFT_PARENTHESIS, RIGHT_PARENTHESIS, // ( )
    LEFT_BRACE, RIGHT_BRACE, // []

    COMMA, ASSIGNMENT, COLON, // , = :
    OCTOTHORPE, AMPERSAND, // # &
    ADDITION, SUBTRACTION, MULTIPLICATION, DIVISION, MODULO, // + - * / %

    GREATER, LESSER, // > <
    GREATER_EQUAL, LESSER_EQUAL, // >= <=
    EQUAL, NOT_EQUAL, // == <>

    IDENTIFIER,

    CHAR_LIT,
    INT_LIT,
    FLOAT_LIT,
    BOOL_LIT,
    STR_LIT,

    // RESERVED WORDS
    AND, OR, NOT,
    OUTPUT, INPUT,
    VAR, AS,
    INT, BOOL, FLOAT, CHAR,
    START, STOP,
    IF, ELSE, WHILE, EOL, EOF
}

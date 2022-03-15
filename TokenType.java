public enum TokenType {
    LEFT_PARENTHESIS, RIGHT_PARENTHESIS, // ( )
    LEFT_BRACE, RIGHT_BRACE, // []

    COMMA, ASSIGNMENT, COLON, // , = :
    OCTOTHORPE, AMPERSAND, // # &
    ADDITION, SUBTRACTION, MULTIPLICATION, DIVISION, MODULO,

    GREATER, LESSER, // > <
    GREATER_EQUAL, LESSER_EQUAL, // >= <=
    EQUAL, NOT_EQUAL, // == <>

    IDENTIFIER, // ^([A-Za-z+_+$][A-Za-z+_+$]*)

    CHAR_LIT, // ^('.*')
    INT_LIT, // ^((-)[0-9]{1,32})
    FLOAT_LIT, // ^() ambot unsay regex ani
    BOOL_LIT, // ^(TRUE|FALSE)
    STR_LIT,

    // RESERVED WORDS
    AND, OR, NOT,
    OUTPUT, INPUT,
    VAR, AS,
    INT, BOOL, FLOAT, CHAR,
    START, STOP,
    IF, ELSE, WHILE, EOF,
    ENDL
}

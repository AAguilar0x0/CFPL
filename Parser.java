import java.util.ArrayList;
import java.util.List;

public class Parser {
    private List<Token> tokens;
    private int current = 0;
    List<Stmt> statements = new ArrayList<>();
    String sourceCode;

    public Parser(List<Token> tokens, String sourceCode) {
        this.tokens = tokens;
        this.sourceCode = sourceCode;
    }

    private String getCodeAtLine(int lineNumber) {
        int start = 0;
        int end = 0;
        int line = 0;
        for (int i = 0; i < sourceCode.length(); i++) {
            if (sourceCode.charAt(i) == '\n') {
                if (line > 0)
                    start = end + 1;
                end = i;
                if (line == lineNumber)
                    break;
                line++;
            }
        }
        return sourceCode.substring(start, end);
    }

    private void disregardEOL() {
        if (match(TokenType.EOL))
            ;
    }

    List<Stmt> parse() throws Exception {
        // List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }

        return statements;
        // return expression();
    }

    private Expr expression() throws Exception {
        return assignment();
        // return equality();
    }

    private Stmt declaration() throws Exception {
        // try {
        if (match(TokenType.VAR))
            return varDeclaration();

        return statement();
        // } catch (Exception error) {
        // synchronize();
        // return null;
        // }
    }

    private Stmt statement() throws Exception {
        if (match(TokenType.IF))
            return ifStatement();
        if (match(TokenType.OUTPUT))
            return printStatement();
        if (match(TokenType.INPUT))
            return inputStatement();
        if (match(TokenType.WHILE))
            return whileStatement();
        if (match(TokenType.START))
            return new Stmt.Block(block());

        return expressionStatement();
    }

    private Stmt ifStatement() throws Exception {
        consume(TokenType.LEFT_PARENTHESIS, "Expected '(' after 'if'.");
        Expr condition = expression();
        consume(TokenType.RIGHT_PARENTHESIS, "Expected ')' after if condition.");
        consume(TokenType.EOL, "Expected new line after ')'.");
        consume(TokenType.START, "Expected START after if condition.");
        consume(TokenType.EOL, "Expected new line after if START.");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        consume(TokenType.STOP, "Expected STOP for code block.");
        consume(TokenType.EOL, "Expected new line after if STOP.");
        if (match(TokenType.ELSE)) {
            consume(TokenType.EOL, "Expected new line after if ELSE.");
            consume(TokenType.START, "Expected START after if condition.");
            consume(TokenType.EOL, "Expected new line after if START.");
            elseBranch = statement();
            consume(TokenType.STOP, "Expected STOP for code block.");
            consume(TokenType.EOL, "Expected new line after if STOP.");
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt printStatement() throws Exception {
        consume(TokenType.COLON, "Expected ':' after 'OUTPUT'.");
        Expr value = expression();
        consume(TokenType.EOL, "Expected new line after expression.");
        return new Stmt.Print(value);
    }

    private Stmt inputStatement() throws Exception {
        consume(TokenType.COLON, "Expected ':' after 'OUTPUT'.");
        List<Expr.Variable> variables = new ArrayList<Expr.Variable>();
        variables.add(new Expr.Variable(consume(TokenType.IDENTIFIER, "Expected identifier for input")));
        while (match(TokenType.COMMA))
            variables.add(new Expr.Variable(consume(TokenType.IDENTIFIER, "Expected identifier for input")));
        consume(TokenType.EOL, "Expected new line after expression.");
        return new Stmt.Input(variables.toArray(new Expr.Variable[0]));
    }

    private Expr.Literal getDefaultLiteral(TokenType type) {
        Object value;
        switch (type) {
            case BOOL:
                value = false;
                break;
            case CHAR:
                value = '\0';
                break;
            case FLOAT:
                value = (double) 0;
                break;
            case INT:
                value = (int) 0;
                break;
            default:
                value = null;
        }
        return new Expr.Literal(value);
    }

    private boolean checkType(TokenType expected, Object actual) {
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

    private Stmt varDeclaration() throws Exception {
        Token name;
        if (check(TokenType.IDENTIFIER))
            name = consume(TokenType.IDENTIFIER, "Expected variable name.");
        else if (Token.reservedWords.containsKey(peek().lexeme))
            throw error(peek(), "Expected valid variable name but got reserved keyword.");
        else
            throw error(peek(), "Expected valid variable name.");
        TokenType type;

        int tempCurrent = current;
        while (!match(TokenType.AS, TokenType.EOL, TokenType.START))
            current++;
        if (match(TokenType.BOOL, TokenType.CHAR, TokenType.FLOAT, TokenType.INT))
            type = previous().type;
        else
            throw error(name, "Expected declaration variable data type.");
        current = tempCurrent;

        Expr initializer = null;
        if (match(TokenType.ASSIGNMENT)) {
            initializer = expression();
            if (initializer instanceof Expr.Literal) {
                Expr.Literal initial = (Expr.Literal) initializer;
                if (type == TokenType.FLOAT && checkType(TokenType.INT, initial.value))
                    initializer = new Expr.Literal((double) initial.value);
                else if (!checkType(type, initial.value))
                    throw error(name, String.format("Expected %s type.", type));
            }
        } else
            initializer = getDefaultLiteral(type);

        Stmt.Var returnVar = new Stmt.Var(name, initializer);

        boolean manyDeclaration = false;
        while (match(TokenType.COMMA)) {
            if (!manyDeclaration)
                statements.add(new Stmt.Var(name, initializer));
            manyDeclaration = true;
            name = consume(TokenType.IDENTIFIER, "Expected variable name.");
            initializer = null;
            if (match(TokenType.ASSIGNMENT)) {
                initializer = expression();
                if (initializer instanceof Expr.Literal) {
                    Expr.Literal initial = (Expr.Literal) initializer;
                    if (type == TokenType.FLOAT && checkType(TokenType.INT, initial.value))
                        initializer = new Expr.Literal((double) initial.value);
                    else if (!checkType(type, initial.value))
                        throw error(name, String.format("Expected %s type.", type));
                }
            } else
                initializer = getDefaultLiteral(type);
            statements.add(new Stmt.Var(name, initializer));
        }

        if (consume(TokenType.AS, "Expected declaration variable data type.") != null
                && !match(TokenType.BOOL, TokenType.CHAR, TokenType.FLOAT, TokenType.INT))
            throw error(name, "Expected declaration variable data type.");
        consume(TokenType.EOL, "Expected new line after declaration.");

        if (manyDeclaration)
            returnVar = (Stmt.Var) statements.remove(statements.size() - 1);

        return returnVar;
    }

    private Stmt whileStatement() throws Exception {
        consume(TokenType.LEFT_PARENTHESIS, "Expected '(' after 'while'.");
        Expr condition = expression();
        consume(TokenType.RIGHT_PARENTHESIS, "Expected ')' after condition.");
        consume(TokenType.EOL, "Expected new line after ')'.");
        consume(TokenType.START, "Expected STOP for code block.");
        consume(TokenType.EOL, "Expected new line after START.");
        Stmt body = statement();
        consume(TokenType.STOP, "Expected STOP for code block.");
        consume(TokenType.EOL, "Expected new line after STOP.");

        return new Stmt.While(condition, body);
    }

    private Stmt expressionStatement() throws Exception {
        Expr expr = expression();
        if (check(TokenType.STOP))
            ;
        else
            consume(TokenType.EOL, "Expected new line after expression.");
        return new Stmt.Expression(expr);
    }

    private List<Stmt> block() throws Exception {
        List<Stmt> statements = new ArrayList<>();
        consume(TokenType.EOL, "Missing new line after START");
        while (!check(TokenType.STOP) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(TokenType.STOP, "Expected STOP after block.");
        consume(TokenType.EOL, "Missing new line after STOP");
        return statements;
    }

    private Expr assignment() throws Exception {
        Expr expr = or();

        if (match(TokenType.ASSIGNMENT)) {
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable) expr).name;
                return new Expr.Assign(name, value);
            }

            error(equals, "Invalid assignment target.");
        }

        return expr;
    }

    private Expr or() throws Exception {
        Expr expr = and();

        while (match(TokenType.OR)) {
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr and() throws Exception {
        Expr expr = equality();

        while (match(TokenType.AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr equality() throws Exception {
        Expr expr = comparison();

        while (match(TokenType.NOT_EQUAL, TokenType.EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd())
            return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd())
            current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private Expr comparison() throws Exception {
        Expr expr = term();

        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESSER, TokenType.LESSER_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() throws Exception {
        Expr expr = factor();

        while (match(TokenType.SUBTRACTION, TokenType.ADDITION, TokenType.AMPERSAND)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() throws Exception {
        Expr expr = unary();

        while (match(TokenType.DIVISION, TokenType.MULTIPLICATION, TokenType.MODULO)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() throws Exception {
        if (match(TokenType.ADDITION, TokenType.SUBTRACTION)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return primary();
    }

    private Expr primary() throws Exception {
        if (match(TokenType.INT_LIT, TokenType.FLOAT_LIT, TokenType.BOOL_LIT, TokenType.CHAR_LIT, TokenType.STR_LIT)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(TokenType.IDENTIFIER)) {
            return new Expr.Variable(previous());
        }

        if (match(TokenType.LEFT_PARENTHESIS)) {
            Expr expr = expression();
            consume(TokenType.RIGHT_PARENTHESIS, "Expected ')' after expression.");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expected expression.");
    }

    private Token consume(TokenType type, String message) throws Exception {
        if (check(type))
            return advance();

        throw error(peek(), message);
    }

    private Exception error(Token token, String message) {
        String lineCode = getCodeAtLine(token.line);

        return new Exception(
                String.format("%s\nline-number %d on %s '%s'\n%s", message, token.line, token.type, token.lexeme,
                        lineCode));
    }
}

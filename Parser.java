import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parser {
    boolean varDeclarations = true;
    boolean isDeclaring = false;
    boolean inControlStructure = false;
    boolean inScope = false;
    int scopeCounter = 0;
    CFPL cfpl;
    private List<Token> tokens;
    private int current = 0;
    List<ParsingStatement> statements = new ArrayList<>();
    private final Map<String, TokenType> variablesType = new HashMap<String, TokenType>();

    public Parser(CFPL cfpl) {
        this.cfpl = cfpl;
    }

    private ParsingExpression.Literal getDefaultLiteral(TokenType type) {
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

        return new ParsingExpression.Literal(value);
    }

    List<ParsingStatement> parse(List<Token> tokens) throws Exception {
        this.tokens = tokens;
        while (!isAtEnd())
            statements.add(parseDeclaration());

        return statements;
    }

    private ParsingStatement parseDeclaration() throws Exception {
        if (compareMultipleThenNext(TokenType.VAR))
            return parseVariableDeclaration();

        return parseStatement();
    }

    private ParsingStatement parseVariableDeclaration() throws Exception {
        if (!isDeclaring)
            isDeclaring = true;
        if (!varDeclarations)
            throw cfpl.newError(getPrevious(), "Misplaced variable declaration.");
        Token name;
        if (compareCurrent(TokenType.IDENTIFIER))
            name = expectThenNext(TokenType.IDENTIFIER, "Expected variable name.");
        else if (Token.reservedWords.containsKey(getCurrent().lexeme))
            throw cfpl.newError(getCurrent(), "Expected valid variable name but got reserved keyword.");
        else
            throw cfpl.newError(getCurrent(), "Expected valid variable name.");
        TokenType type;

        int tempCurrent = current;
        while (!compareMultipleThenNext(TokenType.AS, TokenType.EOL, TokenType.START))
            current++;
        if (compareMultipleThenNext(TokenType.BOOL, TokenType.CHAR, TokenType.FLOAT, TokenType.INT))
            type = getPrevious().type;
        else
            throw cfpl.newError(name, "Expected declaration variable data type.");
        current = tempCurrent;

        ParsingExpression initializer = null;
        if (compareMultipleThenNext(TokenType.ASSIGNMENT)) {
            initializer = parseExpression();
            if (initializer instanceof ParsingExpression.Literal) {
                ParsingExpression.Literal initial = (ParsingExpression.Literal) initializer;
                if (type == TokenType.FLOAT && Token.checkType(initial.value, TokenType.INT)) {
                    double x = Double.parseDouble(initial.value.toString());
                    initializer = new ParsingExpression.Literal(x);
                } else if (!Token.checkType(initial.value, type))
                    throw cfpl.newError(name, String.format("Expected '%s' type.", type));
            }
        } else
            initializer = getDefaultLiteral(type);

        ParsingStatement.Var returnVar = new ParsingStatement.Var(name, initializer);

        if (!variablesType.containsKey(name.lexeme))
            variablesType.put(name.lexeme, type);
        else
            throw cfpl.newError(name, String.format("Variable name '%s' is already declared.", name.lexeme));

        boolean manyDeclaration = false;
        while (compareMultipleThenNext(TokenType.COMMA)) {
            if (!manyDeclaration) {
                manyDeclaration = true;
                statements.add(new ParsingStatement.Var(name, initializer));
            }
            name = expectThenNext(TokenType.IDENTIFIER, "Expected variable name.");
            initializer = null;
            if (compareMultipleThenNext(TokenType.ASSIGNMENT)) {
                initializer = parseExpression();
                if (initializer instanceof ParsingExpression.Literal) {
                    ParsingExpression.Literal initial = (ParsingExpression.Literal) initializer;
                    if (type == TokenType.FLOAT && Token.checkType(initial.value, TokenType.INT)) {
                        double x = Double.parseDouble(initial.value.toString());
                        initializer = new ParsingExpression.Literal(x);
                    } else if (!Token.checkType(initial.value, type))
                        throw cfpl.newError(name, String.format("Expected '%s' type.", type));
                }
            } else
                initializer = getDefaultLiteral(type);
            if (!variablesType.containsKey(name.lexeme))
                variablesType.put(name.lexeme, type);
            else
                throw cfpl.newError(name, String.format("Variable name '%s' is already declared.", name.lexeme));
            statements.add(new ParsingStatement.Var(name, initializer));
        }

        if (expectThenNext(TokenType.AS, "Expected declaration variable data type.") != null
                && !compareMultipleThenNext(TokenType.BOOL, TokenType.CHAR, TokenType.FLOAT, TokenType.INT))
            throw cfpl.newError(name, "Expected declaration variable data type.");
        expectThenNext(TokenType.EOL, "Expected new line after declaration.");
        if (isDeclaring)
            isDeclaring = false;

        if (manyDeclaration)
            returnVar = (ParsingStatement.Var) statements.remove(statements.size() - 1);

        return returnVar;
    }

    private ParsingStatement parseStatement() throws Exception {
        if (compareMultipleThenNext(TokenType.START))
            return new ParsingStatement.Block(parseBlock());
        if (!inScope)
            throw cfpl.newError(getCurrent(), "Statement is out of scope.");
        if (compareMultipleThenNext(TokenType.IF))
            return parseIf();
        if (compareMultipleThenNext(TokenType.OUTPUT))
            return parseOutput();
        if (compareMultipleThenNext(TokenType.INPUT))
            return parseInput();
        if (compareMultipleThenNext(TokenType.WHILE))
            return parseWhile();

        return parseExpressionStatement();
    }

    private ParsingStatement parseExpressionStatement() throws Exception {
        if (!inScope && !isDeclaring)
            throw cfpl.newError(getCurrent(), "Out of scope expression is only allowed in variable declaration.");
        ParsingExpression expr = parseExpression();
        expectThenNext(TokenType.EOL, "Expected new line after expression.");

        return new ParsingStatement.Expression(expr);
    }

    private ParsingExpression parseExpression() throws Exception {
        return parseAssignment();
    }

    private ParsingExpression parseAssignment() throws Exception {
        ParsingExpression expr = parseConcatenation();
        if (compareMultipleThenNext(TokenType.ASSIGNMENT)) {
            Token equals = getPrevious();
            ParsingExpression value = parseAssignment();
            if (expr instanceof ParsingExpression.Variable) {
                Token name = ((ParsingExpression.Variable) expr).name;
                TokenType type;
                type = variablesType.get(name.lexeme);
                if (value instanceof ParsingExpression.Literal
                        && !Token.checkType(((ParsingExpression.Literal) value).value, type))
                    throw cfpl.newError(name, String.format("Expected '%s' type.", type));
                return new ParsingExpression.Assign(name, value, type);
            }
            throw cfpl.newError(equals, "Invalid assignment target.");
        } else if (compareMultipleThenNext(TokenType.BOOL_LIT, TokenType.CHAR_LIT, TokenType.FLOAT_LIT,
                TokenType.INT_LIT, TokenType.STR_LIT, TokenType.IDENTIFIER)) {
            throw cfpl.newError(getPrevious(), "Missing expression operator.");
        }

        return expr;
    }

    private ParsingExpression parseConcatenation() throws Exception {
        ParsingExpression expr = parseLogicalOr();
        while (compareMultipleThenNext(TokenType.AMPERSAND)) {
            Token operator = getPrevious();
            ParsingExpression right = parseLogicalOr();
            expr = new ParsingExpression.Binary(expr, operator, right);
        }

        return expr;
    }

    private ParsingExpression parseLogicalOr() throws Exception {
        ParsingExpression expr = parseLogicalAnd();
        while (compareMultipleThenNext(TokenType.OR)) {
            Token operator = getPrevious();
            ParsingExpression right = parseLogicalAnd();
            expectLogicalExpressions(right);
            expr = new ParsingExpression.Logical(expr, operator, right);
        }

        return expr;
    }

    private ParsingExpression parseLogicalAnd() throws Exception {
        ParsingExpression expr = parseEquality();
        while (compareMultipleThenNext(TokenType.AND)) {
            Token operator = getPrevious();
            ParsingExpression right = parseEquality();
            expectLogicalExpressions(right);
            expr = new ParsingExpression.Logical(expr, operator, right);
        }

        return expr;
    }

    private ParsingExpression parseEquality() throws Exception {
        ParsingExpression expr = parseComparison();
        while (compareMultipleThenNext(TokenType.NOT_EQUAL, TokenType.EQUAL)) {
            Token operator = getPrevious();
            ParsingExpression right = parseComparison();
            expr = new ParsingExpression.Binary(expr, operator, right);
        }

        return expr;
    }

    private ParsingExpression parseComparison() throws Exception {
        ParsingExpression expr = parseTerm();
        while (compareMultipleThenNext(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESSER,
                TokenType.LESSER_EQUAL)) {
            Token operator = getPrevious();
            ParsingExpression right = parseTerm();
            expr = new ParsingExpression.Binary(expr, operator, right);
        }

        return expr;
    }

    private ParsingExpression parseTerm() throws Exception {
        ParsingExpression expr = parseFactor();
        while (compareMultipleThenNext(TokenType.SUBTRACTION, TokenType.ADDITION)) {
            Token operator = getPrevious();
            ParsingExpression right = parseFactor();
            expr = new ParsingExpression.Binary(expr, operator, right);
        }

        return expr;
    }

    private ParsingExpression parseFactor() throws Exception {
        ParsingExpression expr = parseUnary();
        while (compareMultipleThenNext(TokenType.DIVISION, TokenType.MULTIPLICATION, TokenType.MODULO)) {
            Token operator = getPrevious();
            ParsingExpression right = parseUnary();
            expr = new ParsingExpression.Binary(expr, operator, right);
        }

        return expr;
    }

    private ParsingExpression parseUnary() throws Exception {
        if (compareMultipleThenNext(TokenType.ADDITION, TokenType.SUBTRACTION, TokenType.NOT)) {
            Token operator = getPrevious();
            ParsingExpression right = parseUnary();
            if (operator.type == TokenType.NOT)
                expectLogicalExpressions(right);
            return new ParsingExpression.Unary(operator, right);
        }

        return parsePrimary();
    }

    private ParsingExpression parsePrimary() throws Exception {
        if (compareMultipleThenNext(TokenType.INT_LIT, TokenType.FLOAT_LIT, TokenType.BOOL_LIT, TokenType.CHAR_LIT,
                TokenType.STR_LIT))
            return new ParsingExpression.Literal(getPrevious().literal);
        if (compareMultipleThenNext(TokenType.IDENTIFIER)) {
            if (!varDeclarations && !variablesType.containsKey(getPrevious().lexeme))
                throw cfpl.newError(getPrevious(), String.format("Undefined variable '%s'.", getPrevious().lexeme));
            return new ParsingExpression.Variable(getPrevious());
        }
        if (compareMultipleThenNext(TokenType.LEFT_PARENTHESIS)) {
            ParsingExpression expr = parseExpression();
            expectThenNext(TokenType.RIGHT_PARENTHESIS, "Expected ')' after expression.");
            return new ParsingExpression.Grouping(expr);
        }

        throw cfpl.newError(getCurrent(), "Expected expression.");
    }

    private ParsingStatement parseIf() throws Exception {
        Token ifToken = getPrevious();
        expectThenNext(TokenType.LEFT_PARENTHESIS, "Expected '(' after 'if'.");
        ParsingExpression condition = parseExpression();
        expectTokenAndEOLNext(TokenType.RIGHT_PARENTHESIS, "Expected ')' after condition.");
        expectTokenAndEOL(TokenType.START, "Expected 'START' before code block.");
        inControlStructure = true;
        ParsingStatement thenBranch = parseStatement();
        ParsingStatement elseBranch = null;
        if (compareMultipleThenNext(TokenType.ELSE)) {
            expectThenNext(TokenType.EOL, "Expected new line after if 'ELSE'.");
            expectTokenAndEOL(TokenType.START, "Expected 'START' before code block.");
            inControlStructure = true;
            elseBranch = parseStatement();
        }

        return new ParsingStatement.If(condition, thenBranch, elseBranch, ifToken);
    }

    private ParsingStatement parseOutput() throws Exception {
        expectThenNext(TokenType.COLON, "Expected ':' after 'OUTPUT'.");
        ParsingExpression value = parseExpression();
        expectThenNext(TokenType.EOL, "Expected new line after expression.");

        return new ParsingStatement.Print(value);
    }

    private ParsingStatement parseInput() throws Exception {
        expectThenNext(TokenType.COLON, "Expected ':' after 'INPUT'.");
        List<ParsingExpression.Variable> variables = new ArrayList<ParsingExpression.Variable>();
        variables.add(
                new ParsingExpression.Variable(expectThenNext(TokenType.IDENTIFIER, "Expected identifier for input")));
        while (compareMultipleThenNext(TokenType.COMMA))
            variables.add(new ParsingExpression.Variable(
                    expectThenNext(TokenType.IDENTIFIER, "Expected identifier for input")));
        expectThenNext(TokenType.EOL, "Expected new line after expression.");

        return new ParsingStatement.Input(variables.toArray(new ParsingExpression.Variable[0]));
    }

    private ParsingStatement parseWhile() throws Exception {
        expectThenNext(TokenType.LEFT_PARENTHESIS, "Expected '(' after 'while'.");
        ParsingExpression condition = parseExpression();
        expectTokenAndEOLNext(TokenType.RIGHT_PARENTHESIS, "Expected ')' after condition.");
        expectTokenAndEOL(TokenType.START, "Expected 'START' before code block.");
        inControlStructure = true;
        ParsingStatement body = parseStatement();

        return new ParsingStatement.While(condition, body);
    }

    private List<ParsingStatement> parseBlock() throws Exception {
        if (inScope && !inControlStructure)
            throw cfpl.newError(getPrevious(), "Nested scope is invalid.");
        if (!inScope && scopeCounter > 0)
            throw cfpl.newError(getPrevious(), "Multiple scope is invalid.");
        boolean isScope = false;
        if (varDeclarations && !inScope) {
            isScope = true;
            inScope = true;
            scopeCounter++;
            varDeclarations = false;
        }
        List<ParsingStatement> statements = new ArrayList<>();
        expectThenNext(TokenType.EOL, "Missing new line after START");
        inControlStructure = false;
        while (!compareCurrent(TokenType.STOP) && !isAtEnd())
            statements.add(parseDeclaration());
        expectTokenAndEOLNext(TokenType.STOP, "Expected 'STOP' after code block.");
        if (isScope)
            inScope = false;

        return statements;
    }

    private Object expectLogicalExpressions(ParsingExpression expectFrom) throws Exception {
        TokenType type = TokenType.BOOL;
        Token erroneous = getPrevious();
        if (expectFrom instanceof ParsingExpression.Grouping)
            return expectLogicalExpressions(((ParsingExpression.Grouping) expectFrom).expression);
        if (expectFrom instanceof ParsingExpression.Unary) {
            ParsingExpression.Unary instance = (ParsingExpression.Unary) expectFrom;
            if (instance.operator.type == TokenType.NOT)
                return expectLogicalExpressions(instance.right);
        }
        try {
            if (expectFrom instanceof ParsingExpression.Logical)
                return null;
            if (expectFrom instanceof ParsingExpression.Binary) {
                ParsingExpression.Binary instance = (ParsingExpression.Binary) expectFrom;
                if (!Token.logicalComparisonOperators.contains(instance.operator.type)) {
                    erroneous = instance.operator;
                    throw new Exception();
                }
            } else if (expectFrom instanceof ParsingExpression.Literal) {
                ParsingExpression.Literal instance = (ParsingExpression.Literal) expectFrom;
                if (!Token.checkType(instance.value, type))
                    throw new Exception();
            } else if (expectFrom instanceof ParsingExpression.Variable) {
                ParsingExpression.Variable instance = (ParsingExpression.Variable) expectFrom;
                if (!Token.checkType(type, variablesType.get(instance.name.lexeme))) {
                    erroneous = instance.name;
                    throw new Exception();
                }
            } else
                throw new Exception();
        } catch (Exception e) {
            throw cfpl.newError(erroneous, String.format("Expected '%s' evaluation result.", type));
        }
        return null;
    }

    private void expectTokenAndEOLNext(TokenType type, String expectMessage) throws Exception {
        expectThenNext(type, expectMessage);
        expectThenNext(TokenType.EOL,
                String.format("Missing new line after \'%s\'", Token.tokenTypeToLexeme.get(type)));
    }

    private void expectTokenAndEOL(TokenType type, String expectMessage) throws Exception {
        int tempCurrent = current;
        expectTokenAndEOLNext(type, expectMessage);
        current = tempCurrent;
    }

    private Token expectThenNext(TokenType type, String message) throws Exception {
        if (compareCurrent(type))
            return next();

        throw cfpl.newError(getCurrent(), message);
    }

    private boolean compareMultipleThenNext(TokenType... types) {
        for (TokenType type : types) {
            if (compareCurrent(type)) {
                next();
                return true;
            }
        }

        return false;
    }

    private boolean compareCurrent(TokenType type) {
        if (isAtEnd())
            return false;
        return getCurrent().type == type;
    }

    private Token next() {
        if (!isAtEnd())
            current++;
        return getPrevious();
    }

    private boolean isAtEnd() {
        return getCurrent().type == TokenType.EOF;
    }

    private Token getCurrent() {
        return tokens.get(current);
    }

    private Token getPrevious() {
        return tokens.get(current - 1);
    }
}

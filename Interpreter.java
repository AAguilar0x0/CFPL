import java.util.List;
import java.util.Scanner;

class Interpreter implements ParsingExpression.Visitor<Object>,
        ParsingStatement.Visitor<Void> {
    private Storage global = new Storage();

    @Override
    public Object literal(ParsingExpression.Literal expr) {
        return expr.value;
    }

    @Override
    public Object logical(ParsingExpression.Logical expr) throws Exception {
        Object left = evaluate(expr.left);
        if (expr.operator.type == TokenType.OR) {
            if (truthFullness(left))
                return left;
        } else {
            if (!truthFullness(left))
                return left;
        }

        return evaluate(expr.right);
    }

    @Override
    public Object unary(ParsingExpression.Unary expr) throws Exception {
        Object right = evaluate(expr.right);
        switch (expr.operator.type) {
            case ADDITION:
                if (right instanceof Double)
                    return (double) right;
                if (right instanceof Integer)
                    return (int) right;
                break;
            case SUBTRACTION:
                if (right instanceof Double)
                    return -(double) right;
                if (right instanceof Integer)
                    return -(int) right;
                break;
        }

        return null;
    }

    private boolean truthFullness(Object object) {
        if (object == null)
            return false;
        if (object instanceof Boolean)
            return (boolean) object;

        return true;
    }

    @Override
    public Object variable(ParsingExpression.Variable expr) throws Exception {
        return global.get(expr.name);
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null)
            return true;
        if (a == null)
            return false;

        return a.equals(b);
    }

    @Override
    public Object grouping(ParsingExpression.Grouping expr) throws Exception {
        return evaluate(expr.expression);
    }

    private Object evaluate(ParsingExpression expr) throws Exception {
        return expr.visit(this);
    }

    private void execute(ParsingStatement stmt) throws Exception {
        stmt.visit(this);
    }

    void executeBlock(List<ParsingStatement> statements) throws Exception {
        for (ParsingStatement statement : statements)
            execute(statement);
    }

    @Override
    public Void block(ParsingStatement.Block stmt) throws Exception {
        executeBlock(stmt.statements);

        return null;
    }

    @Override
    public Void expression(ParsingStatement.Expression stmt) throws Exception {
        evaluate(stmt.expression);

        return null;
    }

    @Override
    public Void ifS(ParsingStatement.If stmt) throws Exception {
        if (truthFullness(evaluate(stmt.condition)))
            execute(stmt.thenBranch);
        else if (stmt.elseBranch != null)
            execute(stmt.elseBranch);

        return null;
    }

    @Override
    public Void print(ParsingStatement.Print stmt) throws Exception {
        Object value = evaluate(stmt.expression);
        System.out.print(stringify(value));

        return null;
    }

    @Override
    public Void input(ParsingStatement.Input stmt) throws Exception {
        Scanner scanner = new Scanner(System.in);
        for (ParsingExpression.Variable v : stmt.variables) {
            Object value = global.get(v.name);
            if ("java.lang.Character".equals(value.getClass().getName()))
                global.assign(v.name, (char) scanner.nextLine().charAt(0));
            else if ("java.lang.Double".equals(value.getClass().getName()))
                global.assign(v.name, (double) scanner.nextDouble());
            else if ("java.lang.Integer".equals(value.getClass().getName()))
                global.assign(v.name, (int) scanner.nextInt());
            else {
                scanner.close();
                throw new Exception(String.format("Unsupported input data type: %s", v.name.type));
            }
        }
        scanner.close();

        return null;
    }

    @Override
    public Void var(ParsingStatement.Var stmt) throws Exception {
        Object value = null;
        if (stmt.initializer != null)
            value = evaluate(stmt.initializer);
        global.define(stmt.name.lexeme, value);

        return null;
    }

    @Override
    public Void whileS(ParsingStatement.While stmt) throws Exception {
        while (truthFullness(evaluate(stmt.condition)))
            execute(stmt.body);

        return null;
    }

    @Override
    public Object assign(ParsingExpression.Assign expr) throws Exception {
        Object value = evaluate(expr.value);
        global.assign(expr.name, value);

        return value;
    }

    @Override
    public Object binary(ParsingExpression.Binary expr) throws Exception {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);
        switch (expr.operator.type) {
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                if (left instanceof Double && right instanceof Double)
                    return (double) left > (double) right;
                if (left instanceof Integer && right instanceof Integer)
                    return (int) left > (int) right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                if (left instanceof Double && right instanceof Double)
                    return (double) left >= (double) right;
                if (left instanceof Integer && right instanceof Integer)
                    return (int) left >= (int) right;
            case LESSER:
                checkNumberOperands(expr.operator, left, right);
                if (left instanceof Double && right instanceof Double)
                    return (double) left < (double) right;
                if (left instanceof Integer && right instanceof Integer)
                    return (int) left < (int) right;
            case LESSER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                if (left instanceof Double && right instanceof Double)
                    return (double) left <= (double) right;
                if (left instanceof Integer && right instanceof Integer)
                    return (int) left <= (int) right;
            case NOT_EQUAL:
                return !isEqual(left, right);
            case EQUAL:
                return isEqual(left, right);
            case SUBTRACTION:
                checkNumberOperands(expr.operator, left, right);
                checkNumberOperand(expr.operator, right);
                if (left instanceof Double && right instanceof Double)
                    return (double) left - (double) right;
                if (left instanceof Integer && right instanceof Integer)
                    return (int) left - (int) right;
            case ADDITION:
                checkNumberOperands(expr.operator, left, right);
                if (left instanceof Double && right instanceof Double)
                    return (double) left + (double) right;
                if (left instanceof Integer && right instanceof Integer)
                    return (int) left + (int) right;
            case DIVISION:
                checkNumberOperands(expr.operator, left, right);
                if (left instanceof Double && right instanceof Double)
                    return (double) left / (double) right;
                if (left instanceof Integer && right instanceof Integer)
                    return (int) left / (int) right;
            case MULTIPLICATION:
                checkNumberOperands(expr.operator, left, right);
                if (left instanceof Double && right instanceof Double)
                    return (double) left * (double) right;
                if (left instanceof Integer && right instanceof Integer)
                    return (int) left * (int) right;
            case MODULO:
                if (left instanceof Integer && right instanceof Integer)
                    return (int) left % (int) right;
                else
                    throw new Exception(String.format("Operand must be an integer: %s - %s", left, right));
            case AMPERSAND:
                return left.toString() + right.toString();
        }

        return null;
    }

    private void checkNumberOperand(Token operator, Object operand) throws Exception {
        if (operand instanceof Double)
            return;

        throw new Exception(String.format("Operand must be a number: %s", operand));
    }

    private void checkNumberOperands(Token operator,
            Object left, Object right) throws Exception {
        if (left instanceof Double && right instanceof Double || left instanceof Integer && right instanceof Integer)
            return;

        throw new Exception(String.format("Operand must be a number: %s - %s", left, right));
    }

    void interpret(List<ParsingStatement> statements) throws Exception {
        for (ParsingStatement statement : statements)
            execute(statement);
    }

    private String stringify(Object object) {
        if (object == null)
            return "null";

        return object.toString();
    }
}
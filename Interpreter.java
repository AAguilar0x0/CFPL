import java.util.List;
import java.util.Scanner;

class Interpreter implements ParsingExpression.Visitor<Object>,
        ParsingStatement.Visitor<Void> {
    private CFPL cfpl;
    private Storage global = new Storage();

    public Interpreter(CFPL cfpl) {
        this.cfpl = cfpl;
    }

    @Override
    public Object literal(ParsingExpression.Literal expr) {
        return expr.value;
    }

    @Override
    public Object logical(ParsingExpression.Logical expr) throws Exception {
        Object left = evaluate(expr.left);
        try {
            if (expr.operator.type == TokenType.OR) {
                if (toBoolean(left))
                    return left;
            } else {
                if (!toBoolean(left))
                    return left;
            }
        } catch (Exception e) {
            throw cfpl.newError(expr.operator, e.getMessage());
        }

        return evaluate(expr.right);
    }

    @Override
    public Object unary(ParsingExpression.Unary expr) throws Exception {
        Object right = evaluate(expr.right);
        switch (expr.operator.type) {
            case NOT:
                try {
                    return !toBoolean(right);
                } catch (Exception e) {
                    throw cfpl.newError(expr.operator, e.getMessage());
                }
            case ADDITION:
                checkNumberOperand(expr.operator, right);
                if (right instanceof Double)
                    return (double) right;
                if (right instanceof Integer)
                    return (int) right;
                break;
            case SUBTRACTION:
                checkNumberOperand(expr.operator, right);
                if (right instanceof Double)
                    return -(double) right;
                if (right instanceof Integer)
                    return -(int) right;
                break;
        }

        return null;
    }

    private boolean toBoolean(Object object) throws Exception {
        if (object instanceof Boolean)
            return (boolean) object;

        throw new Exception("Operand must be a boolean.");
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
        Object condition = evaluate(stmt.condition);
        try {
            if (toBoolean(condition))
                execute(stmt.thenBranch);
            else if (stmt.elseBranch != null)
                execute(stmt.elseBranch);
        } catch (Exception e) {
            throw cfpl.newError(stmt.ifToken, e.getMessage());
        }

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
                throw cfpl.newError(v.name, "Unsupported input data type.");
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
        while (toBoolean(evaluate(stmt.condition)))
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
                    throw cfpl.newError(expr.operator, "Operand must be an integer.");
            case AMPERSAND:
                return left.toString() + right.toString();
        }

        return null;
    }

    private void checkNumberOperand(Token operator, Object operand) throws Exception {
        if (operand instanceof Double || operand instanceof Integer)
            return;

        throw cfpl.newError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator,
            Object left, Object right) throws Exception {
        if (left instanceof Double && right instanceof Double || left instanceof Integer && right instanceof Integer)
            return;

        throw cfpl.newError(operator, "Operand must be a number.");
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
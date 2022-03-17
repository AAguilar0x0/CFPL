import java.util.List;
import java.util.Scanner;

class Interpreter implements Expr.Visitor<Object>,
        Stmt.Visitor<Void> {
    private Environment environment = new Environment();

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) throws Exception {
        Object left = evaluate(expr.left);

        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left))
                return left;
        } else {
            if (!isTruthy(left))
                return left;
        }

        return evaluate(expr.right);
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) throws Exception {
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case ADDITION:
                if (right instanceof Double) {
                    return (double) right;
                }
                if (right instanceof Integer) {
                    return (int) right;
                }
                break;
            case SUBTRACTION:
                if (right instanceof Double) {
                    return -(double) right;
                }
                if (right instanceof Integer) {
                    return -(int) right;
                }
                break;
        }

        // Unreachable.
        return null;
    }

    private boolean isTruthy(Object object) {
        if (object == null)
            return false;
        if (object instanceof Boolean)
            return (boolean) object;
        return true;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) throws Exception {
        return environment.get(expr.name);
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null)
            return true;
        if (a == null)
            return false;

        return a.equals(b);
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) throws Exception {
        return evaluate(expr.expression);
    }

    private Object evaluate(Expr expr) throws Exception {
        return expr.accept(this);
    }

    private void execute(Stmt stmt) throws Exception {
        stmt.accept(this);
    }

    void executeBlock(List<Stmt> statements
    // , Environment environment
    ) throws Exception {
        // Environment previous = this.environment;
        try {
            // this.environment = environment;

            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            // this.environment = previous;
        }
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) throws Exception {
        executeBlock(stmt.statements
        // , new Environment(environment)
        );
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) throws Exception {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) throws Exception {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) throws Exception {
        Object value = evaluate(stmt.expression);
        System.out.print(stringify(value));
        return null;
    }

    @Override
    public Void visitInputStmt(Stmt.Input stmt) throws Exception {
        Scanner scanner = new Scanner(System.in);
        for (Expr.Variable v : stmt.variables) {
            Object value = environment.get(v.name);
            if ("java.lang.Character".equals(value.getClass().getName()))
                environment.assign(v.name, (char) scanner.nextLine().charAt(0));
            else if ("java.lang.Double".equals(value.getClass().getName()))
                environment.assign(v.name, (double) scanner.nextDouble());
            else if ("java.lang.Integer".equals(value.getClass().getName()))
                environment.assign(v.name, (int) scanner.nextInt());
            else {
                scanner.close();
                throw new Exception(String.format("Unsupported input data type: %s", v.name.type));
            }
        }
        scanner.close();

        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) throws Exception {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }

        environment.define(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) throws Exception {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body);
        }
        return null;
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) throws Exception {
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) throws Exception {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                if (left instanceof Double && right instanceof Double) {
                    return (double) left > (double) right;
                }
                if (left instanceof Integer && right instanceof Integer) {
                    return (int) left > (int) right;
                }
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                if (left instanceof Double && right instanceof Double) {
                    return (double) left >= (double) right;
                }
                if (left instanceof Integer && right instanceof Integer) {
                    return (int) left >= (int) right;
                }
            case LESSER:
                checkNumberOperands(expr.operator, left, right);
                if (left instanceof Double && right instanceof Double) {
                    return (double) left < (double) right;
                }
                if (left instanceof Integer && right instanceof Integer) {
                    return (int) left < (int) right;
                }
            case LESSER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                if (left instanceof Double && right instanceof Double) {
                    return (double) left <= (double) right;
                }
                if (left instanceof Integer && right instanceof Integer) {
                    return (int) left <= (int) right;
                }
            case NOT_EQUAL:
                return !isEqual(left, right);
            case EQUAL:
                return isEqual(left, right);
            case SUBTRACTION:
                checkNumberOperands(expr.operator, left, right);
                checkNumberOperand(expr.operator, right);
                if (left instanceof Double && right instanceof Double) {
                    return (double) left - (double) right;
                }
                if (left instanceof Integer && right instanceof Integer) {
                    return (int) left - (int) right;
                }
            case ADDITION:
                checkNumberOperands(expr.operator, left, right);
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                }
                if (left instanceof Integer && right instanceof Integer) {
                    return (int) left + (int) right;
                }
            case DIVISION:
                checkNumberOperands(expr.operator, left, right);
                if (left instanceof Double && right instanceof Double) {
                    return (double) left / (double) right;
                }
                if (left instanceof Integer && right instanceof Integer) {
                    return (int) left / (int) right;
                }
            case MULTIPLICATION:
                checkNumberOperands(expr.operator, left, right);
                if (left instanceof Double && right instanceof Double) {
                    return (double) left * (double) right;
                }
                if (left instanceof Integer && right instanceof Integer) {
                    return (int) left * (int) right;
                }
            case MODULO:
                if (left instanceof Integer && right instanceof Integer)
                    return (int) left % (int) right;
                else
                    throw new Exception(String.format("Operand must be an integer: %s - %s", left, right));
            case AMPERSAND:
                return left.toString() + right.toString();
        }

        // Unreachable.
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

    void interpret(List<Stmt> statements) throws Exception {
        for (Stmt statement : statements) {
            execute(statement);
        }
    }

    private String stringify(Object object) {
        if (object == null)
            return "null";
        return object.toString();
    }
}
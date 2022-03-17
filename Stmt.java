// program        → declaration* EOF ;
// declaration    → varDecl
//                | statement ;
// varDecl        → "VAR" IDENTIFIER ( "=" expression )? "EOL" ;
// statement      → exprStmt
//                | printStmt
//                | inputStmt
//                | ifStmt
//                | whileStmt
//                | block ;
// exprStmt       → expression "EOL" ;
// printStmt      → "PRINT" expression "EOL" ;
// inputStmt      → "OUTPUT" ":" expression "EOL" ;
// ifStmt         → "IF" "(" expression ")" "START" statement "STOP"
//                ( "ELSE" "START" statement "STOP" )? ;
// whileStmt      → "WHILE" "(" expression ")" "START" statement "STOP" ;
// block          → "START" declaration* "STOP" ;

import java.util.List;

abstract class Stmt {
  interface Visitor<R> {
    R visitBlockStmt(Block stmt) throws Exception;
    R visitExpressionStmt(Expression stmt) throws Exception;
    R visitIfStmt(If stmt) throws Exception;
    R visitPrintStmt(Print stmt) throws Exception;
    R visitInputStmt(Input stmt) throws Exception;
    R visitVarStmt(Var stmt) throws Exception;
    R visitWhileStmt(While stmt) throws Exception;
  }
  static class Block extends Stmt {
    Block(List<Stmt> statements) {
      this.statements = statements;
    }

    @Override
    <R> R accept(Visitor<R> visitor)  throws Exception {
      return visitor.visitBlockStmt(this);
    }

    final List<Stmt> statements;
  }
  static class Expression extends Stmt {
    Expression(Expr expression) {
      this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor)  throws Exception {
      return visitor.visitExpressionStmt(this);
    }

    final Expr expression;
  }
  static class If extends Stmt {
    If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
      this.condition = condition;
      this.thenBranch = thenBranch;
      this.elseBranch = elseBranch;
    }

    @Override
    <R> R accept(Visitor<R> visitor)  throws Exception {
      return visitor.visitIfStmt(this);
    }

    final Expr condition;
    final Stmt thenBranch;
    final Stmt elseBranch;
  }
  static class Print extends Stmt {
    Print(Expr expression) {
      this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor)  throws Exception {
      return visitor.visitPrintStmt(this);
    }

    final Expr expression;
  }
  static class Input extends Stmt {
    Input(Expr.Variable[] variables) {
      this.variables = variables;
    }

    @Override
    <R> R accept(Visitor<R> visitor)  throws Exception {
      return visitor.visitInputStmt(this);
    }

    final Expr.Variable[] variables;
  }
  static class Var extends Stmt {
    Var(Token name, Expr initializer) {
      this.name = name;
      this.initializer = initializer;
    }

    @Override
    <R> R accept(Visitor<R> visitor)  throws Exception {
      return visitor.visitVarStmt(this);
    }

    final Token name;
    final Expr initializer;
  }
  static class While extends Stmt {
    While(Expr condition, Stmt body) {
      this.condition = condition;
      this.body = body;
    }

    @Override
    <R> R accept(Visitor<R> visitor)  throws Exception {
      return visitor.visitWhileStmt(this);
    }

    final Expr condition;
    final Stmt body;
  }

  abstract <R> R accept(Visitor<R> visitor) throws Exception;
}

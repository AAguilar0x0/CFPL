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

abstract class ParsingStatement {
  interface Visitor<R> {
    R block(Block stmt) throws Exception;

    R expression(Expression stmt) throws Exception;

    R ifS(If stmt) throws Exception;

    R print(Print stmt) throws Exception;

    R input(Input stmt) throws Exception;

    R var(Var stmt) throws Exception;

    R whileS(While stmt) throws Exception;
  }

  static class Block extends ParsingStatement {
    Block(List<ParsingStatement> statements) {
      this.statements = statements;
    }

    @Override
    <R> R visit(Visitor<R> visitor) throws Exception {
      return visitor.block(this);
    }

    final List<ParsingStatement> statements;
  }

  static class Expression extends ParsingStatement {
    Expression(ParsingExpression expression) {
      this.expression = expression;
    }

    @Override
    <R> R visit(Visitor<R> visitor) throws Exception {
      return visitor.expression(this);
    }

    final ParsingExpression expression;
  }

  static class If extends ParsingStatement {
    If(ParsingExpression condition, ParsingStatement thenBranch, ParsingStatement elseBranch) {
      this.condition = condition;
      this.thenBranch = thenBranch;
      this.elseBranch = elseBranch;
    }

    @Override
    <R> R visit(Visitor<R> visitor) throws Exception {
      return visitor.ifS(this);
    }

    final ParsingExpression condition;
    final ParsingStatement thenBranch;
    final ParsingStatement elseBranch;
  }

  static class Print extends ParsingStatement {
    Print(ParsingExpression expression) {
      this.expression = expression;
    }

    @Override
    <R> R visit(Visitor<R> visitor) throws Exception {
      return visitor.print(this);
    }

    final ParsingExpression expression;
  }

  static class Input extends ParsingStatement {
    Input(ParsingExpression.Variable[] variables) {
      this.variables = variables;
    }

    @Override
    <R> R visit(Visitor<R> visitor) throws Exception {
      return visitor.input(this);
    }

    final ParsingExpression.Variable[] variables;
  }

  static class Var extends ParsingStatement {
    Var(Token name, ParsingExpression initializer) {
      this.name = name;
      this.initializer = initializer;
    }

    @Override
    <R> R visit(Visitor<R> visitor) throws Exception {
      return visitor.var(this);
    }

    final Token name;
    final ParsingExpression initializer;
  }

  static class While extends ParsingStatement {
    While(ParsingExpression condition, ParsingStatement body) {
      this.condition = condition;
      this.body = body;
    }

    @Override
    <R> R visit(Visitor<R> visitor) throws Exception {
      return visitor.whileS(this);
    }

    final ParsingExpression condition;
    final ParsingStatement body;
  }

  abstract <R> R visit(Visitor<R> visitor) throws Exception;
}

// expression     → assignment ;
// assignment     → IDENTIFIER "=" assignment | logical_or ;
// logic_or       → logic_and ( "OR" logic_and )* ;
// logic_and      → equality ( "AND" equality )* ;
// equality       → comparison ( ( "<>" | "==" ) comparison )* ;
// comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
// term           → factor ( ( "-" | "+" | "&" ) factor )* ;
// factor         → unary ( ( "/" | "*" | "%" ) unary )* ;
// unary          → ( "+" | "-" ) unary | primary ;
// primary        → INT
//                | FLOAT
//                | BOOL
//                | CHAR
//                | STRING
//                | "(" expression ")"
//                | IDENTIFIER ;

abstract class Expr {
  interface Visitor<R> {
    R visitAssignExpr(Assign expr) throws Exception;
    R visitBinaryExpr(Binary expr) throws Exception;
    R visitGroupingExpr(Grouping expr) throws Exception;
    R visitLiteralExpr(Literal expr) throws Exception;
    R visitLogicalExpr(Logical expr) throws Exception;
    R visitUnaryExpr(Unary expr) throws Exception;
    R visitVariableExpr(Variable expr) throws Exception;
  }
  static class Assign extends Expr {
    Assign(Token name, Expr value) {
      this.name = name;
      this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor)  throws Exception {
      return visitor.visitAssignExpr(this);
    }

    final Token name;
    final Expr value;
  }
  static class Binary extends Expr {
    Binary(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor)  throws Exception {
      return visitor.visitBinaryExpr(this);
    }

    final Expr left;
    final Token operator;
    final Expr right;
  }
  static class Grouping extends Expr {
    Grouping(Expr expression) {
      this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor)  throws Exception {
      return visitor.visitGroupingExpr(this);
    }

    final Expr expression;
  }
  static class Literal extends Expr {
    Literal(Object value) {
      this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor)  throws Exception {
      return visitor.visitLiteralExpr(this);
    }

    final Object value;
  }
  static class Logical extends Expr {
    Logical(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor)  throws Exception {
      return visitor.visitLogicalExpr(this);
    }

    final Expr left;
    final Token operator;
    final Expr right;
  }
  static class Unary extends Expr {
    Unary(Token operator, Expr right) {
      this.operator = operator;
      this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor)  throws Exception {
      return visitor.visitUnaryExpr(this);
    }

    final Token operator;
    final Expr right;
  }
  static class Variable extends Expr {
    Variable(Token name) {
      this.name = name;
    }

    @Override
    <R> R accept(Visitor<R> visitor)  throws Exception {
      return visitor.visitVariableExpr(this);
    }

    final Token name;
  }

  abstract <R> R accept(Visitor<R> visitor) throws Exception;
}

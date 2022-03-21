// expression     → assignment ;
// assignment     → IDENTIFIER "=" assignment | logical_or ;
// logic_or       → logic_and ( "OR" logic_and )* ;
// logic_and      → equality ( "AND" equality )* ;
// equality       → comparison ( ( "<>" | "==" ) comparison )* ;
// comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
// term           → factor ( ( "-" | "+" | "&" ) factor )* ;
// factor         → unary ( ( "/" | "*" | "%" ) unary )* ;
// unary          → ( "+" | "-" | "NOT" ) unary | primary ;
// primary        → INT
//                | FLOAT
//                | BOOL
//                | CHAR
//                | STRING
//                | "(" expression ")"
//                | IDENTIFIER ;

abstract class ParsingExpression {
  interface Visitor<R> {
    R assign(Assign expr) throws Exception;

    R binary(Binary expr) throws Exception;

    R grouping(Grouping expr) throws Exception;

    R literal(Literal expr) throws Exception;

    R logical(Logical expr) throws Exception;

    R unary(Unary expr) throws Exception;

    R variable(Variable expr) throws Exception;
  }

  static class Assign extends ParsingExpression {
    Assign(Token name, ParsingExpression value, TokenType type) {
      this.name = name;
      this.value = value;
      this.type = type;
    }

    @Override
    <R> R visit(Visitor<R> visitor) throws Exception {
      return visitor.assign(this);
    }

    final Token name;
    final ParsingExpression value;
    final TokenType type;
  }

  static class Binary extends ParsingExpression {
    Binary(ParsingExpression left, Token operator, ParsingExpression right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    @Override
    <R> R visit(Visitor<R> visitor) throws Exception {
      return visitor.binary(this);
    }

    final ParsingExpression left;
    final Token operator;
    final ParsingExpression right;
  }

  static class Grouping extends ParsingExpression {
    Grouping(ParsingExpression expression) {
      this.expression = expression;
    }

    @Override
    <R> R visit(Visitor<R> visitor) throws Exception {
      return visitor.grouping(this);
    }

    final ParsingExpression expression;
  }

  static class Literal extends ParsingExpression {
    Literal(Object value) {
      this.value = value;
    }

    @Override
    <R> R visit(Visitor<R> visitor) throws Exception {
      return visitor.literal(this);
    }

    final Object value;
  }

  static class Logical extends ParsingExpression {
    Logical(ParsingExpression left, Token operator, ParsingExpression right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    @Override
    <R> R visit(Visitor<R> visitor) throws Exception {
      return visitor.logical(this);
    }

    final ParsingExpression left;
    final Token operator;
    final ParsingExpression right;
  }

  static class Unary extends ParsingExpression {
    Unary(Token operator, ParsingExpression right) {
      this.operator = operator;
      this.right = right;
    }

    @Override
    <R> R visit(Visitor<R> visitor) throws Exception {
      return visitor.unary(this);
    }

    final Token operator;
    final ParsingExpression right;
  }

  static class Variable extends ParsingExpression {
    Variable(Token name) {
      this.name = name;
    }

    @Override
    <R> R visit(Visitor<R> visitor) throws Exception {
      return visitor.variable(this);
    }

    final Token name;
  }

  abstract <R> R visit(Visitor<R> visitor) throws Exception;
}

import java.util.HashMap;
import java.util.Map;

class Environment {
    final Environment enclosing;
    private final Map<String, Object> values = new HashMap<>();

    Environment() {
        enclosing = null;
    }

    // Environment(Environment enclosing) {
    // this.enclosing = enclosing;
    // }

    Object get(Token name) throws Exception {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }

        throw new Exception("Undefined variable '" + name.lexeme + "'.");
    }

    void assign(Token name, Object value) throws Exception {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
            return;
        }

        throw new Exception("Undefined variable '" + name.lexeme + "'.");
    }

    void define(String name, Object value) {
        values.put(name, value);
    }
}
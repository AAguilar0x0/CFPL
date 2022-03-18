import java.util.HashMap;
import java.util.Map;

class Storage {
    final Storage scope;
    private final Map<String, Object> variables = new HashMap<>();

    Storage() {
        scope = null;
    }

    // Environment(Environment enclosing) {
    // this.enclosing = enclosing;
    // }

    Object get(Token name) throws Exception {
        if (variables.containsKey(name.lexeme)) {
            return variables.get(name.lexeme);
        }

        throw new Exception("Undefined variable '" + name.lexeme + "'.");
    }

    void assign(Token name, Object value) throws Exception {
        if (variables.containsKey(name.lexeme)) {
            variables.put(name.lexeme, value);
            return;
        }

        throw new Exception("Undefined variable '" + name.lexeme + "'.");
    }

    void define(String name, Object value) {
        variables.put(name, value);
    }
}
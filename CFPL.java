import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;

public class CFPL {
    private Lexer lexer;
    private Parser parser;
    private Interpreter interpret;
    private String sourceCode;

    public CFPL(String filePath) {
        File file = new File(filePath);
        sourceCode = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String temp = "";
            while ((temp = br.readLine()) != null) {
                sourceCode += temp + "\n";
            }
            br.close();
        } catch (Exception e) {
            System.out.println("[Error] File not found.");
        }
    }

    public String getSourceCode() {
        return sourceCode;
    }

    private String getCodeAtLine(int lineNumber) {
        int start = 0;
        int end = 0;
        int line = 0;
        for (int i = 0; i < sourceCode.length(); i++) {
            if (sourceCode.charAt(i) == '\n') {
                if (line > 0)
                    start = end + 1;
                end = i;
                if (line == lineNumber)
                    break;
                line++;
            }
        }

        return sourceCode.substring(start, end);
    }

    public Exception newError(Token token, String message) {
        String lineCode = getCodeAtLine(token.line);

        return new Exception(
                String.format("%s\nline-number %d on %s '%s'.\n%s", message, token.line, token.type, token.lexeme,
                        lineCode));
    }

    public Exception newError(int line, String atFault, String message) {
        String lineCode = getCodeAtLine(line);

        return new Exception(
                String.format("%s\nline-number %d on %s.\n%s", message, line, atFault, lineCode));
    }

    public void execute() throws Exception {
        try {
            lexer = new Lexer(this);
            List<Token> tokens = lexer.run();
            parser = new Parser(this);
            List<ParsingStatement> statements = parser.parse(tokens);
            interpret = new Interpreter(this);
            interpret.interpret(statements);
        } catch (Exception e) {
            System.out.println(String.format("\n[Error] %s", e.getMessage()));
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Usage: CFPL <file path>");
            System.exit(64);
        }
        CFPL cfpl = new CFPL(args[0]);
        // CFPL cfpl = new CFPL("./tests/0.txt");
        cfpl.execute();
    }
}

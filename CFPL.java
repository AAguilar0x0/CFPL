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

    public void execute() throws Exception {
        try {
            lexer = new Lexer(sourceCode);
            List<Token> tokens = lexer.run();
            parser = new Parser(tokens, sourceCode);
            List<Stmt> statements = parser.parse();
            interpret = new Interpreter();
            interpret.interpret(statements);
        } catch (Exception e) {
            System.out.println(String.format("[Error] %s", e.getMessage()));
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

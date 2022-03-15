public class CFPL {
    private String sourceCode;
    private Lexer lexer;

    public CFPL(String sourceCode) {
        this.sourceCode = sourceCode;
        lexer = new Lexer(sourceCode);
    }

    public String execute() {
        try {
            lexer.run();
            return lexer.toString();
        } catch (Exception e) {
            return String.format("[Error] %s", e.getMessage());
        }
    }
}

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class Main {
    public static void main(String[] args) throws Exception {
        File file = new File("./tests/0.txt");
        String sourceCode = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String temp = "";
            while ((temp = br.readLine()) != null)
                sourceCode += temp + "\n";
            br.close();
        } catch (Exception e) {
            System.out.println("File not found");
        }
        CFPL cfpl = new CFPL(sourceCode);
        System.out.println(cfpl.execute());
    }
}
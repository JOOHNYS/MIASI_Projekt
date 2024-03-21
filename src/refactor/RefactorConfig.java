package refactor;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class RefactorConfig {
    String quote = "\"";

    public RefactorConfig() {}

    public void readConfig() {
        Properties prop = new Properties();
        try (FileInputStream in = new FileInputStream("config.yaml")) {
            prop.load(in);
            String quote = prop.getProperty("quote");
            if (quote.equals("\"") || quote.equals("'")) {
                this.quote = quote;
            } else {
                throw new IllegalArgumentException("Invalid quote character in config file. It should be either \" or '");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getQuote() {
        return quote;
    }
}

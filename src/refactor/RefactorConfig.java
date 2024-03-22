package refactor;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

public class RefactorConfig {
    String quote = "\"";

    public RefactorConfig() {}

    public void readConfig() {
        Properties prop = new Properties();
        try (FileInputStream in = new FileInputStream("config.yaml")) {
            prop.load(in);
            String quote = prop.getProperty("quote");
            if (Objects.equals(quote, "\"single\"")) {
                this.quote = "'";
            } else if (Objects.equals(quote, "\"double\"")) {
                this.quote = "\"";
            } else {
                System.out.println("Invalid quote value in config.yaml");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getQuote() {
        return quote;
    }
}

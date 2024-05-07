package refactor;

import java.io.*;
import java.util.Scanner;

public class CommentFixer {
    // Refactor hash comments to double slash comments
    public static void fixHashComments(String fileName) throws IOException {
        // Read file and refactor comments to temp file
        var baseFile = new File(fileName);
        Scanner scanner = new Scanner(baseFile);
        PrintWriter writer = new PrintWriter("temp.txt");
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.startsWith("#")) {
                line = line.replaceFirst("#", "//");
            }
            writer.println(line);
        }
        scanner.close();
        writer.close();

        // Clear base file and write refactored comments
        new FileWriter(fileName).close();
        File tempFile = new File("temp.txt");
        BufferedReader reader = new BufferedReader(new FileReader(tempFile));
        writer = new PrintWriter(fileName);
        while (reader.ready()) {
            writer.println(reader.readLine());
        }
        reader.close();
        writer.close();

        // Delete temp file
        var result = tempFile.delete();
        if (!result) {
            throw new IOException("Failed to refactor comments.");
        }
        System.out.println("Comments refactored successfully.");
    }
}

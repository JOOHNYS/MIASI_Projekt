import grammar.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import refactor.RefactorVisitor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class App {
    public static void main(String[] args) {
        CharStream inp = null;
        try {
            inp = CharStreams.fromFileName("input.js");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        JavaScriptLexer lexer = new JavaScriptLexer(inp);
        CommonTokenStream tokens = new CommonTokenStream((TokenSource) lexer);
        JavaScriptParser parser = new JavaScriptParser(tokens);
        ParseTree tree = parser.program(); // Parse the input

        RefactorVisitor visitor = new RefactorVisitor(inp);
        String refactoredCode = visitor.visit(tree); // Refactor the code
        System.out.println(refactoredCode); // Print the refactored code

        // write the refactored code to a file
//        try {
//            Files.write(Paths.get("output.js"), refactoredCode.getBytes());
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

    }
}
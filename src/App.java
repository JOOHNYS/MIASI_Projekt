import grammar.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import refactor.RefactorConfig;
import refactor.RefactorVisitor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

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

        RefactorConfig config = new RefactorConfig();
        config.readConfig();

        RefactorVisitor visitor = new RefactorVisitor(inp, config, tokens);
        visitor.visit(tree); // Refactor the code

        try (PrintWriter writer = new PrintWriter("output.js")) {
            writer.println(visitor.rewriter.getText());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

    }
}
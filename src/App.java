import grammar.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import refactor.RefactorConfig;
import refactor.RefactorVisitor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
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

        RefactorConfig config = new RefactorConfig();
        config.readConfig();

        RefactorVisitor visitor = new RefactorVisitor(inp, config);
        visitor.visit(tree); // Refactor the code

        String modifiedCode = reconstructCode(tree, lexer);
        try (PrintWriter writer = new PrintWriter("output.js")) {
            writer.println(modifiedCode);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    // Reconstruct code from parse tree
    private static String reconstructCode(ParseTree tree, Lexer lexer) {
        StringBuilder codeBuilder = new StringBuilder();
        for (int i = 0; i < tree.getChildCount(); i++) {
            ParseTree child = tree.getChild(i);
            codeBuilder.append(getNodeText(child, lexer));
        }
        return codeBuilder.toString();
    }

    // Get text representation of a parse tree node
    private static String getNodeText(ParseTree node, Lexer lexer) {
        if (node instanceof TerminalNode) {
            Token symbol = ((TerminalNode) node).getSymbol();
            // Exclude <EOF> token
            if (symbol.getType() != Token.EOF) {
                return symbol.getText();
            }
        } else {
            StringBuilder text = new StringBuilder();
            for (int i = 0; i < node.getChildCount(); i++) {
                ParseTree child = node.getChild(i);
                text.append(getNodeText(child, lexer));
            }
            // Append newline if the node is a statement
            if (node instanceof JavaScriptParser.StatementContext) {
                text.append("\n");
            }
            return text.toString();
        }
        return "";
    }
}